package it.unifi.hierarchical.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;

import org.oristool.models.gspn.chains.DTMC;
import org.oristool.models.gspn.chains.DTMCStationary;

import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.State;
import it.unifi.hierarchical.utils.NumericalUtils;
import it.unifi.hierarchical.utils.StateUtils;


/**
 * Notes: 
 * - we assume the embedded DTMC to be irreducible 
 * @author marco
 *
 */
public class HierarchicalSMPAnalysis {
    
    private static final double DTMC_STRUCTURE_ALLOWED_EPSILON = 0.00000001;

    private HierarchicalSMP model;
    private Map<State, NumericalValues> sojournTimeDistributions;
    private Map<Region, NumericalValues> regionSojournTimeDistributions;
    private Map<Region, TransientAnalyzer> regionTransientProbabilities;
    private Map<State, Double> meanSojournTimes;
    private Map<State, Double> emcSolution;
    
    public HierarchicalSMPAnalysis(HierarchicalSMP model) {
        this.model = model;
    }
    
    public Map<String, Double> evaluateSteadyState(double timeStep, double timeLimit) {
        //1- Sojourn times distribution
        evaluateSojournTimeDistributions(timeStep, timeLimit);

        //2- Mean sojourn times 
        evaluateMeanSojournTimes(timeStep, timeLimit);
        
        //3- Solve EMC
        solveEmbeddedDTMC();
        
        //4- steady state (Use solution of 2 and 3 to evaluate steady)
        Map<String, Double> result = evalueteSS();        
        return result;
    }


    private void evaluateSojournTimeDistributions(double timeStep, double timeLimit) {
        SojournTimeEvaluatorVisitor visitor = new SojournTimeEvaluatorVisitor(timeStep, timeLimit);
        model.getInitialState().accept(visitor);
        this.sojournTimeDistributions = visitor.getSojournTimeDistributions();
        this.regionSojournTimeDistributions = visitor.getRegionSojournTimeDistributions();
        this.regionTransientProbabilities = visitor.getRegionTransientProbabilities();
    }

    
    private void evaluateMeanSojournTimes(double timeStep, double timeLimit) {
        MeanSojournTimeEvaluatorVisitor visitor = new MeanSojournTimeEvaluatorVisitor(model.getInitialState(), sojournTimeDistributions, regionSojournTimeDistributions, regionTransientProbabilities, timeLimit);
        model.getInitialState().accept(visitor);
        this.meanSojournTimes = visitor.getMeanSojournTimes();
    }

    
    private void solveEmbeddedDTMC(){
        //3.1- Build DTMC
        DTMC<State> dtmc = buildEDTMC();
        //3.2- Evaluate steady state considering only branching probabilities and neglecting sojourn times
        this.emcSolution = evaluateDTMCSteadyState(dtmc);
    }
    
    private DTMC<State> buildEDTMC() {
        DTMC<State> dtmc = DTMC.create();

        //Initial state
        dtmc.initialStates().add(model.getInitialState());
        dtmc.initialProbs().add(1.0);
        
        //transition probabilities
        Set<State> visited = new HashSet<>();
        Stack<State> toBeVisited = new Stack<>();
        toBeVisited.add(model.getInitialState());
        dtmc.probsGraph().addNode(model.getInitialState());
        while(!toBeVisited.isEmpty()) {
            State current = toBeVisited.pop();
            visited.add(current);
            
            //CASE OF COMPOSITE STATE WITH EXIT STATES ON THE BORDER
            if(StateUtils.isCompositeWithBorderExit(current)) {
                CompositeState cState = (CompositeState)current;
                //Add missing children to the dtmc
                for(Entry<State, List<State>> e:cState.getNextStatesConditional().entrySet()) {
                    for (State successor : e.getValue()) {
                        if(visited.contains(successor) || toBeVisited.contains(successor))
                            continue;
                        dtmc.probsGraph().addNode(successor);
                        toBeVisited.push(successor);    
                    }
                }
                //Evaluate successors probabilities
                List<NumericalValues> distributions = new ArrayList<>();
                for (Region region : cState.getRegions()) {
                    distributions.add(regionSojournTimeDistributions.get(region));
                }
                
                List<Double> fireFirstProb = NumericalUtils.evaluateFireFirstProbabilities(distributions);
                Map<State, Double> fireFirstProbMap = new HashMap<>();
                for (int r=0; r < cState.getRegions().size(); r++) {
                    State endState = StateUtils.findEndState(cState.getRegions().get(r));
                    fireFirstProbMap.put(endState, fireFirstProb.get(r));
                }
                //Add edges
                for(State exitState: cState.getNextStatesConditional().keySet()) {
                    for(int b = 0; b < cState.getBranchingProbsConditional().get(exitState).size(); b++) {
                        double prob = cState.getBranchingProbsConditional().get(exitState).get(b) * fireFirstProbMap.get(exitState);
                        addEdgeValue(dtmc, current, cState.getNextStatesConditional().get(exitState).get(b), prob);
                    }
                }
            //STANDARD CASE
            }else {
                //Add missing children to the dtmc
                for(State successor:current.getNextStates()) {
                    if(visited.contains(successor) || toBeVisited.contains(successor))
                        continue;
                    dtmc.probsGraph().addNode(successor);
                    toBeVisited.push(successor);
                }
                //Add edges
                for(int i = 0; i <current.getBranchingProbs().size(); i++) {
                    addEdgeValue(dtmc, current, current.getNextStates().get(i), current.getBranchingProbs().get(i));
                }                
            }
        }
        return dtmc;
    }
    
    /**
     * If it not exists, create an edge in the DTMC between from and to with specified value.
     * If the edge already exists
     */
    private static void addEdgeValue(DTMC<State> dtmc, State from, State to, double prob) {
        Optional<Double> old = dtmc.probsGraph().edgeValue(from, to);
        double newProb = prob;
        if(old.isPresent()) {
            newProb+=old.get().doubleValue();
        }
        dtmc.probsGraph().putEdgeValue(from, to, newProb);
    }
    
    private Map<State, Double> evaluateDTMCSteadyState(DTMC<State> dtmc) {
        DTMCStationary<State> DTMCss = DTMCStationary.<State>builder().epsilon(DTMC_STRUCTURE_ALLOWED_EPSILON).build();
        return DTMCss.apply(dtmc.probsGraph());
    }
    

    private Map<String, Double> evalueteSS() {        
        //4.1- At higher level use the standard solution method for SS of an SMP
        Map<String, Double> ss = new HashMap<String, Double>();
        double denominator = 0.0;
        for (State higherLevelState : emcSolution.keySet()) {
            denominator += meanSojournTimes.get(higherLevelState) * emcSolution.get(higherLevelState);
        }
        
        for (State higherLevelState : emcSolution.keySet()) {
            double numerator = meanSojournTimes.get(higherLevelState)* emcSolution.get(higherLevelState);
            ss.put(higherLevelState.getName(), numerator / denominator);
        }
        
        //4.2- At lower level recursively go down:
        //The SS of a sub-state in a region can be obtained by multiplying the steady-state probability of the surrounding composite state with the
        //fraction of mean sojourn time in the sub-state and the surrounding composite state
        for (State higherLevelState : emcSolution.keySet()) {
            SubstatesSteadyStateEvaluatorVisitor visitor = 
                    new SubstatesSteadyStateEvaluatorVisitor(
                            ss.get(higherLevelState.getName()),
                            meanSojournTimes.get(higherLevelState)
                            , meanSojournTimes);
            higherLevelState.accept(visitor);
            ss.putAll(visitor.getSubStateSSProbs());
        }
        
        return ss;
    }

}

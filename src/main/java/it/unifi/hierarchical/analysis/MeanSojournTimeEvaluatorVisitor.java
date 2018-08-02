package it.unifi.hierarchical.analysis;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oristool.math.OmegaBigDecimal;

import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.ExitState;
import it.unifi.hierarchical.model.FinalState;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.SimpleState;
import it.unifi.hierarchical.model.State;
import it.unifi.hierarchical.model.visitor.StateVisitor;
import it.unifi.hierarchical.utils.NumericalUtils;
import it.unifi.hierarchical.utils.StateUtils;

//2.1- Navigate on the higher level searching for all possible state
//2.2- For each one, evaluate the mean based on sojourn time distribution,
//     if composite,
public class MeanSojournTimeEvaluatorVisitor implements StateVisitor{

    private Map<State, NumericalValues> sojournTimeDistributions;
    private Map<Region, NumericalValues> regionSojournTimeDistributions;
    private Map<Region, TransientAnalyzer> regionTransientProbabilities;
    private Map<State, Region> parentRegions;
    private Map<Region, CompositeState> parentStates;
    private Map<State, Double> meanSojournTimes;
    private double timeLimit;
    
    private Map<Region, Map<State, NumericalValues>> absorbingProbabilities;//Given a region that contain a state, give the distribution of time to be absorbed in such state
    private Map<Region, NumericalValues> shiftedExitDistributions;//Given a target region, given the minimum exit distribution of parallel regions 
    
    public MeanSojournTimeEvaluatorVisitor(State initialState, Map<State, NumericalValues> sojournTimeDistributions, Map<Region, NumericalValues> regionSojournTimeDistributions, Map<Region, TransientAnalyzer> regionTransientProbabilities, double timeLimit) {
        this.sojournTimeDistributions = sojournTimeDistributions;
        this.regionSojournTimeDistributions = regionSojournTimeDistributions;
        this.regionTransientProbabilities = regionTransientProbabilities;
        ModelStructureAnalyzer modelStructure = new ModelStructureAnalyzer(initialState);
        this.parentRegions = modelStructure.getParentRegions();
        this.parentStates = modelStructure.getParentStates();
        this.meanSojournTimes = new HashMap<>();
        this.absorbingProbabilities = new HashMap<>();
        this.shiftedExitDistributions = new HashMap<>();
        this.timeLimit = timeLimit;
    }

    @Override
    public void visit(SimpleState state) {
        evaluateStateMeanSojournTime(state);
    }

    @Override
    public void visit(CompositeState state) {
        evaluateStateMeanSojournTime(state);
        for (Region region : state.getRegions()) {
            if(meanSojournTimes.containsKey(region.getInitialState()))
                continue;
            region.getInitialState().accept(this);
        }
    }

    @Override
    public void visit(FinalState state) {
        //Do nothing!
    }

    @Override
    public void visit(ExitState state) {
        //Do nothing!
    }
    
    private void evaluateStateMeanSojournTime(State state) {
        if(state.getDepth() == 0) {//Top level sojourn time --> not affected by exit of any other region
            evaluateTopLevelStateSojournTime(state);
        }else { //Not top level sojourn time --> affected by exit in parallel regions
            evaluateLowerLevelStateSojournTime(state);    
        }
        
        //Evaluate mean sojourn time also for successor states if required
        if(StateUtils.isCompositeWithBorderExit(state)) {
            CompositeState cState = (CompositeState) state;
            for(State exitState : cState.getNextStatesConditional().keySet()) {
                List<State> successors = cState.getNextStatesConditional().get(exitState);
                for (State successor : successors) {
                    if(meanSojournTimes.containsKey(successor))
                        continue;
                    successor.accept(this);
                }    
            }
        }else {
            for(State successor: state.getNextStates()) {
                if(meanSojournTimes.containsKey(successor))
                    continue;
                successor.accept(this);
            }
        }
    }

    public Map<State, Double> getMeanSojournTimes() {
        return meanSojournTimes;
    }
    
    private void evaluateTopLevelStateSojournTime(State state) {
        NumericalValues sojournDistrubution = sojournTimeDistributions.get(state);
        double timeStep = sojournDistrubution.getStep();
        double mean = 0;
        for (int t = 0; t < sojournDistrubution.getValues().length; t++) {
            mean+= (1-sojournDistrubution.getValues()[t]) * timeStep;
        }
        meanSojournTimes.put(state, mean);
    }
    
    private void evaluateLowerLevelStateSojournTime(State state) {
        //1- Get region transient probabilities. Since it is not a top level state, it must belong to a region
        Region parentRegion = parentRegions.get(state);
        TransientAnalyzer parentRegionAnalysis = regionTransientProbabilities.get(parentRegion);
        double timeStep = parentRegionAnalysis.getTimeStep();
        double[] transientProbs = parentRegionAnalysis.getProbsFromTo(parentRegion.getInitialState(), state).getValues();
        
        //2- Get exit distributions of parallel regions at same level of the direct parent composite state
        List<NumericalValues> exitDistributions = new ArrayList<>();
        State parentState = parentStates.get(parentRegion);
        if(!(parentState instanceof CompositeState))
            throw new IllegalStateException("A non composite state can't contains regions!");
        CompositeState parentCState = (CompositeState) parentState;
        for (Region r : parentCState.getRegions()) {
            if(r.getType().equals(Region.RegionType.FINAL))//If final, neglect parallel regions since they don't affect sojourn time
                continue;
            if(r.equals(parentRegion))//Consider only other regions, not the current one
                continue;
            NumericalValues regDistro = regionSojournTimeDistributions.get(r);
            exitDistributions.add(regDistro);
        }

        //3- Get exit distribution of parallel regions at higher level. Note that distribution must have the same time origin and thus need to be shifted
        if(state.getDepth() > 1) {
            //3.1 Get parent exit distribution of previous level region
            Region previousParentRegion = parentRegions.get(parentStates.get(parentRegion)); 
            NumericalValues parentExitDistribution = shiftedExitDistributions.get(previousParentRegion);

            //3.2 evaluate time to be absorbed in current state in the current region
            if(absorbingProbabilities.get(previousParentRegion) == null)
                absorbingProbabilities.put(previousParentRegion, new HashMap<>());
            
            if(absorbingProbabilities.get(previousParentRegion).get(parentState) == null) {
                //Is the first time that this probabilities is evaluated
                List<State> smpStates = StateUtils.getReachableStates(previousParentRegion.getInitialState());
                TransientAnalyzer analyzer = 
                        //new SMPAnalyzer(smpStates, sojournTimeDistributions, timeLimit, timeStep, parentState);
                        new SMPAnalyzerWithBorderExitStates(previousParentRegion.getInitialState(), sojournTimeDistributions, regionSojournTimeDistributions, timeLimit, timeStep, parentState);
                double[] absorbingResult = analyzer.getProbsFromTo(previousParentRegion.getInitialState(), parentState).getValues();
                absorbingProbabilities.get(previousParentRegion).put(parentState, new NumericalValues(absorbingResult, timeStep));
            }
            NumericalValues absorbingProbs = absorbingProbabilities.get(previousParentRegion).get(parentState);
            
            //3.3 evaluate sojourn time conditioned to be absorbed (shift and project)
            NumericalValues parallelExitDistribution = NumericalUtils.shiftAndProjectAndMinimum(
                    absorbingProbs, 
                    Arrays.asList(parentExitDistribution));
            
            //3.5 add to the "exitDistributions" array
            exitDistributions.add(parallelExitDistribution);
        }
        
        //TODO it should consider also the probability to reach the parent? to be verified. Perhaps it is already
        //included in the steady state probability?
        //4- Evaluate reaching probability of target sub-state, given that we are in the parent state
        double reachingProbability = 1;
        if(state.getDepth() > 1) {
            Region previousParentRegion = parentRegions.get(parentStates.get(parentRegion));
            NumericalValues absorbingProbs = absorbingProbabilities.get(previousParentRegion).get(parentState);
            NumericalValues parentRegionExitProbs = shiftedExitDistributions.get(previousParentRegion);
            double[] absPDF = NumericalUtils.computePDFFromCDF(absorbingProbs.getValues(), new BigDecimal("" + timeStep));
            double[] exitPDF = NumericalUtils.computePDFFromCDF(parentRegionExitProbs.getValues(), new BigDecimal("" + timeStep));
            //Evaluate probability that absorbingProbs is faster
            reachingProbability = 0;
            for(int t1=0; t1<absPDF.length; t1++) {
                for(int t2=0; t2<exitPDF.length; t2++) {
                    if(t1 < t2)
                        reachingProbability+= absPDF[t1] * exitPDF[t2] * timeStep * timeStep;
                }
            }
        }
        
        //5- Evaluate the exit distribution as the minimum and save it for lower regions
        double[] finalExitDistribution = new double[NumericalUtils.computeStepNumber(new OmegaBigDecimal("" + timeLimit), new BigDecimal("" + timeStep))];
        if(exitDistributions.size() == 0) {
            Arrays.fill(finalExitDistribution, 0.0);    
        }else if(exitDistributions.size() == 1) {
            finalExitDistribution = exitDistributions.get(0).getValues();
        }else {
            for (int t = 0; t < finalExitDistribution.length; t++) {
                double product = 1;
                for(NumericalValues exitDistro: exitDistributions) {
                    product*= (1 - exitDistro.getValues()[t]);
                }
                finalExitDistribution[t] = 1 - product;
            }
        }
        shiftedExitDistributions.put(parentRegion, new NumericalValues(finalExitDistribution, timeStep));
  
        //6- Evaluate the mean sojourn time
        double mean = 0;
        for (int t = 0; t < transientProbs.length; t++) {
            mean+= transientProbs[t] * (1 - finalExitDistribution[t]) * timeStep ;
        }
        mean = mean * reachingProbability;
        meanSojournTimes.put(state, mean);
    }

}

package it.unifi.hierarchical.analysis;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oristool.math.OmegaBigDecimal;

import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.State;
import it.unifi.hierarchical.model.visitor.StateVisitor;
import it.unifi.hierarchical.utils.NumericalUtils;
import it.unifi.hierarchical.utils.StateUtils;

/**
 * Extend the SMPAnalyzer so as to handle the case of composite states with exits on the border
 */
public class SMPAnalyzerWithBorderExitStates implements TransientAnalyzer{
    
    private Map<Region, NumericalValues> regionSojournTimeDistributions;
    private List<State> states;
    private List<State> stateWithExitConditioning;
    private Map<State, List<RegionState>> compositeStateToRegionStates;
    private SMPAnalyzer analyzer;
    private double timeLimit;
    private double timeStep;
    private State absorbingState;

    public SMPAnalyzerWithBorderExitStates(State initialState, Map<State, NumericalValues> sojournTimeDistributions, Map<Region, NumericalValues> regionSojournTimeDistributions, double timeLimit, double timeStep) {
        this(initialState, sojournTimeDistributions, regionSojournTimeDistributions, timeLimit, timeStep, null);
    }
    
    public SMPAnalyzerWithBorderExitStates(State initialState, Map<State, NumericalValues> sojournTimeDistributions, Map<Region, NumericalValues> regionSojournTimeDistributions, double timeLimit, double timeStep, State absorbingState) {
        this.regionSojournTimeDistributions = regionSojournTimeDistributions;
        this.timeLimit = timeLimit;
        this.timeStep = timeStep;
        this.absorbingState = absorbingState;
        //1- Find normal states
        this.states = StateUtils.getReachableStates(initialState);
        
        //2- Create the state list considering the conditioning
        this.stateWithExitConditioning = getStateWithExitConditioning(states);
        
        //3-Add regionState to the sojournTimeDistributions map
        Map<State, NumericalValues> augmentedSojournTimeDistributions = new HashMap<>();
        for (State state : stateWithExitConditioning) {
            if(state instanceof RegionState) {
                augmentedSojournTimeDistributions.put(state, regionSojournTimeDistributions.get(((RegionState)state).getRegion()));
            }else {
                augmentedSojournTimeDistributions.put(state, sojournTimeDistributions.get(state));
            }
        }
        
        //4- solve SMP
        this.analyzer = new SMPAnalyzer(stateWithExitConditioning, augmentedSojournTimeDistributions, timeLimit, timeStep, absorbingState);

        //TODO
        //1- cosa succede se lo stato iniziale è un composite da convertire in Regioni? probabilita di inizio?
        //2- come si riconverte nella soluzione aggregata?
        
    }
    
    @Override
    public NumericalValues getProbsFromTo(State from, State to) {
        //Aggregation not required
        if(!isCompositeWithBorderExit(to)) {
            return analyzer.getProbsFromTo(from, to);
        }
        
        //Agregation required
        int steps = NumericalUtils.computeStepNumber(new OmegaBigDecimal(""+timeLimit), new BigDecimal(""+timeStep));
        List<RegionState> mappedState = compositeStateToRegionStates.get(to);
        double[] result = new double[steps];
        for (RegionState rState : mappedState) {
            NumericalValues probs = analyzer.getProbsFromTo(from, rState);
            for(int t=0; t<result.length; t++) {
                result[t]+= probs.getValues()[t];
            }
        }
        return new NumericalValues(result, timeStep);
    }
    
    @Override
    public double getTimeLimit() {
        return timeLimit;
    }

    @Override
    public double getTimeStep() {
        return timeStep;
    }

    @Override
    public List<State> getStates() {
        return states;
    }

    public State getAbsorbingState() {
        return absorbingState;
    }
    
    /**
     * For each composite state that has exits on border, create a dummy state for each region.
     * For each state that has a composite state having exits on border, change the branching probs considering probability that a region is faster
     * Note: if the composite state having exits on border, is the absorbing one, don't convert it
     */
    private List<State> getStateWithExitConditioning(List<State> states) {
        this.compositeStateToRegionStates = new HashMap<>();
        //1- Convert State to a set of RegionState where required 
        List<State> convertedStates = new ArrayList<>();
        for (State state : states) {
            if(isCompositeWithBorderExit(state)) { 
                List<RegionState> regionStates = new ArrayList<>();
                for(Region region: ((CompositeState)state).getRegions()){
                    RegionState rState = new RegionState(region, ((CompositeState)state));
                    convertedStates.add(rState);
                    regionStates.add(rState);
                }
                compositeStateToRegionStates.put(state, regionStates);
                continue;
            }else {
                convertedStates.add(state);    
            }            
        }
        //2- Change branching probabilities according to the preselection probability
        List<State> stateWithExitConditioning = new ArrayList<>();
        for (State state : convertedStates) {
            stateWithExitConditioning.add(handleExitConditioning(state));
        }
        return stateWithExitConditioning;
    }
    
    private State handleExitConditioning(State state) {
        State newState = state.makeCopy();
        List<Double> branchingProbs = new ArrayList<>();
        List<State> nextStates = new ArrayList<>();
        for (int b = 0; b< state.getNextStates().size(); b++) {
            if(isCompositeWithBorderExit(state)) {
                List<Region> regions = ((CompositeState)state).getRegions();
                List<Double> probFaster = evaluateProbabilityFasterRegions(regions);
                for(int r=0; r < regions.size(); r++) {
                    double p = state.getBranchingProbs().get(b) * probFaster.get(r);
                    nextStates.add(new RegionState(regions.get(r), ((CompositeState)state)));
                    branchingProbs.add(p);
                }
            }else {
                nextStates.add(state.getNextStates().get(b));
                branchingProbs.add(state.getBranchingProbs().get(b));
            }
        }
        newState.setNextStates(nextStates, branchingProbs);
        return newState;
    }
    
    //Evaluate a probability that one region finish first
    private List<Double> evaluateProbabilityFasterRegions(List<Region> regions){
        List<NumericalValues> distributions = new ArrayList<>();
        for (Region region : regions) {
            distributions.add(regionSojournTimeDistributions.get(region));
        }
        
        return NumericalUtils.evaluateFireFirstProbabilities(distributions);
    }
    
    private boolean isCompositeWithBorderExit(State state) {
        if(     state instanceof CompositeState && 
                ((CompositeState)state).hasExitStatesOnBorder() &&
                !state.equals(absorbingState)) {
            return true;
        }
        return false;
    }
    
  
    private class RegionState extends State{
        
        private Region region;
        private CompositeState state;

        protected RegionState(Region region, CompositeState state) {
            super(state.getName() + "-REGION-" + state.getRegions().indexOf(region), state.getDepth());
            this.region = region;
            this.state = state;
        }

        @Override
        public void accept(StateVisitor visitor) {
            throw new UnsupportedOperationException("Region state can't accept visitors");
        }
        
        protected Region getRegion() {
            return region;
        }
        
        @Override
        public State makeCopy() {
            return new RegionState(region, state);
        } 
    }

}

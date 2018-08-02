package it.unifi.hierarchical.model;

import java.util.List;
import java.util.Map;

import it.unifi.hierarchical.model.visitor.StateVisitor;

public class CompositeState extends State {

    private List<Region> regions;
    
    
    //Exit states on the border
    private boolean exitStatesOnBorder;
    Map<State, List<State>> nextStatesConditional; 
    Map<State,List<Double>> branchingProbsConditional;
    
    
    /**
     * Use this constructor if the composite state has final states or exit states not on border
     */
    public CompositeState(String name, List<Region> regions, List<State> nextStates, List<Double> branchingProbs, int depth) {
        super(name, depth);
        if(regions == null || regions.size() == 0)
            throw new IllegalArgumentException("Each composite state require at least one region");
        this.setNextStates(nextStates, branchingProbs);
        this.regions = regions;
        this.exitStatesOnBorder = false;
    }

    /**
     * Use this constructor if the composite state has exit states on border
     */
    public CompositeState(String name, List<Region> regions, Map<State, List<State>> nextStatesConditional, Map<State,List<Double>> branchingProbsConditional, int depth) {
        super(name, depth);
        if(regions == null || regions.size() == 0)
            throw new IllegalArgumentException("Each composite state require at least one region");
        //Don't set next states
        this.regions = regions;
        this.exitStatesOnBorder = true;
        this.nextStatesConditional = nextStatesConditional;
        this.branchingProbsConditional = branchingProbsConditional;
    }
    
    public List<Region> getRegions() {
        return regions;
    }

    @Override
    public String toString() {
        return "CompositeState [numberOfRegions=" + regions.size() + ", numberOfNextStates=" + (nextStates != null? nextStates.size():0) + ", depth=" + depth
                + ", name=" + name + ", hasExitStatesOnBorder=" + exitStatesOnBorder + "]";
    }

    @Override
    public void accept(StateVisitor visitor) {
        visitor.visit(this);
    }
    
    public boolean hasExitStatesOnBorder() {
        return exitStatesOnBorder;
    }

    public Map<State, List<State>> getNextStatesConditional() {
        return nextStatesConditional;
    }

    public void setNextStatesConditional(Map<State, List<State>> nextStatesConditional) {
        this.nextStatesConditional = nextStatesConditional;
    }

    public Map<State, List<Double>> getBranchingProbsConditional() {
        return branchingProbsConditional;
    }

    public void setBranchingProbsConditional(Map<State, List<Double>> branchingProbsConditional) {
        this.branchingProbsConditional = branchingProbsConditional;
    }
    
    @Override
    public State makeCopy() {
        return new CompositeState(name, regions, nextStates, branchingProbs, depth);
    }
    
}

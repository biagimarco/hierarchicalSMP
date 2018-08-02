package it.unifi.hierarchical.model;

public class HierarchicalSMP {
    
    private State initialState;

    public HierarchicalSMP(State initialState) {
        this.initialState = initialState;
    }

    public State getInitialState() {
        return initialState;
    }

    @Override
    public String toString() {
        return "HierarchicalSMP [\n\tinitialState=" + initialState + "\n]";
    }
   
}

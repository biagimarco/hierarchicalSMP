package it.unifi.hierarchical.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.unifi.hierarchical.model.visitor.StateVisitor;

public abstract class State {

    private final static double EPSILON = 0.000001;
    
    protected List<State> nextStates;
    protected List<Double> branchingProbs;
    protected int depth;
    protected String name;
    
    protected State(String name, int depth) {
        if(name == null || name.trim().equals(""))
            throw new IllegalArgumentException("Name can't be empty");
        
        this.name = name;
        this.depth = depth;
        this.nextStates = new ArrayList<>();
        this.branchingProbs = new ArrayList<>();
    }
    
    public abstract State makeCopy();

    public void setNextStates(List<State> nextStates, List<Double> branchingProbs) {
        if(nextStates== null && branchingProbs == null) {
            this.nextStates = new ArrayList<>();
            this.branchingProbs = new ArrayList<>();
            return;
        }
        if(nextStates == null || branchingProbs == null)
            throw new IllegalArgumentException("nextStates and branching probs must have the same size");
        if(nextStates.size() != branchingProbs.size())
            throw new IllegalArgumentException("nextStates and branching probs must have the same size");
        
        double totalProb = branchingProbs.stream().reduce(0.0, (x,y) -> x+y);
        if(totalProb -1.0 > EPSILON)
            throw new IllegalArgumentException("Total branching probability must be 1");
        this.nextStates = nextStates;
        this.branchingProbs = branchingProbs;
        
    }
    
    public List<State> getNextStates() {
        return Collections.unmodifiableList(nextStates);
    }

    public List<Double> getBranchingProbs() {
        return Collections.unmodifiableList(branchingProbs);
    }

    public int getDepth() {
        return depth;
    }
    
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + depth;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        State other = (State) obj;
        if (depth != other.depth)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
    
    public abstract void accept(StateVisitor visitor);
 
}

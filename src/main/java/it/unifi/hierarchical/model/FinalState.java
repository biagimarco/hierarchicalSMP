package it.unifi.hierarchical.model;

import java.util.UUID;

import it.unifi.hierarchical.model.visitor.StateVisitor;

public class FinalState extends State{

    private FinalState(String name, int depth) {
        super(name, depth);
    }
    
    public FinalState(int depth) {
        super("final" + UUID.randomUUID().toString(), depth);
    }
    
    @Override
    public String toString() {
        return "FinalState [depth=" + depth + ", name=" + name + "]";
    }
    
    @Override
    public void accept(StateVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public State makeCopy() {
        return new FinalState(name, depth);
    }
}

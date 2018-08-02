package it.unifi.hierarchical.model;

import java.util.UUID;

import it.unifi.hierarchical.model.visitor.StateVisitor;

public class ExitState extends State{

    private ExitState(String name, int depth) {
        super(name, depth);
    }
    
    public ExitState(int depth) {
        super("exit" + UUID.randomUUID().toString(), depth);
    }

    @Override
    public String toString() {
        return "ExitState [depth=" + depth + ", name=" + name + "]";
    }
    
    @Override
    public void accept(StateVisitor visitor) {
        visitor.visit(this);
    }
    
    @Override
    public State makeCopy() {
        return new ExitState(name, depth);
    }
}

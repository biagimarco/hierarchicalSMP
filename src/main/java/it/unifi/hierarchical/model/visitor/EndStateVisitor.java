package it.unifi.hierarchical.model.visitor;

import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.ExitState;
import it.unifi.hierarchical.model.FinalState;
import it.unifi.hierarchical.model.SimpleState;

public class EndStateVisitor implements StateVisitor {
    
    
    private boolean endState = false;

    @Override
    public void visit(SimpleState state) {
        endState = false;

    }

    @Override
    public void visit(CompositeState state) {
        endState = false;

    }

    @Override
    public void visit(FinalState state) {
        endState = true;

    }

    @Override
    public void visit(ExitState state) {
        endState = true;

    }

    public boolean isEndState() {
        return endState;
    }

    
    
}

package it.unifi.hierarchical.model.visitor;

import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.ExitState;
import it.unifi.hierarchical.model.FinalState;
import it.unifi.hierarchical.model.SimpleState;

public interface StateVisitor {

    public void visit(SimpleState state);
    public void visit(CompositeState state);
    public void visit(FinalState state);
    public void visit(ExitState state);
}

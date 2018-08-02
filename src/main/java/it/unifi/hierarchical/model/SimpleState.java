package it.unifi.hierarchical.model;

import java.util.List;

import org.oristool.math.function.Function;

import it.unifi.hierarchical.model.visitor.StateVisitor;

public class SimpleState extends State{

    private Function density;
    
    public SimpleState(String name, Function density, List<State> nextStates, List<Double> branchingProbs, int depth) {
        super(name, depth);
        this.setNextStates(nextStates, branchingProbs);
        this.density = density;
    }

    public Function getDensity() {
        return density;
    }

    @Override
    public String toString() {
        return "SimpleState [density=" + density + ", numberOfNextStates=" + (nextStates != null? nextStates.size():0) + ", depth=" + depth
                + ", name=" + name + "]";
    }
    
    @Override
    public void accept(StateVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public State makeCopy() {
        return new SimpleState(name, density, nextStates, branchingProbs, depth);
    }
}

package it.unifi.hierarchical.analysis;

import java.util.List;

import it.unifi.hierarchical.model.State;

public interface TransientAnalyzer {

    public double getTimeLimit();

    public double getTimeStep();

    public List<State> getStates();

    public NumericalValues getProbsFromTo(State from, State to);
    
}

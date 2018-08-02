package it.unifi.hierarchical.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.State;
import it.unifi.hierarchical.model.visitor.EndStateVisitor;

public class StateUtils {

    public static State findEndState(Region r) {
        return findEndState(getReachableStates(r.getInitialState()));
    }
    
    /**
     * Assume to have a single end state
     */
    public static State findEndState(List<State> states) {
        for (int i = 0; i < states.size(); i++) {
            State state = states.get(i);
            EndStateVisitor visitor = new EndStateVisitor();
            state.accept(visitor);
            if (visitor.isEndState())
                return state;
        }
        throw new IllegalStateException("End state not found");
    }

    /**
     * Return a list of reachable state. If a state is composite, it is considered a single state and its internal structure is not visited
     */
    public static List<State> getReachableStates(State initialState) {
        List<State> states = new ArrayList<>();
        Stack<State> toBeVisited = new Stack<>();
        toBeVisited.add(initialState);
        while (!toBeVisited.isEmpty()) {
            State current = toBeVisited.pop();
            states.add(current);
            for (State successor : current.getNextStates()) {
                if (!states.contains(successor))
                    toBeVisited.add(successor);
            }
        }

        return states;
    }
    
    public static State searchStateByName(List<State> states, String name) {
        for (State state : states) {
            if(state.getName().equals(name))
               return state; 
        }
        throw new IllegalArgumentException("State not found!");
    }
    
    public static boolean isCompositeWithBorderExit(State state) {
        if(     state instanceof CompositeState && 
                ((CompositeState)state).hasExitStatesOnBorder()) {
            return true;
        }
        return false;
    }

}

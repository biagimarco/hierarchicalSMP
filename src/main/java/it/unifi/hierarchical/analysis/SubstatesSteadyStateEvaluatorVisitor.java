package it.unifi.hierarchical.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.ExitState;
import it.unifi.hierarchical.model.FinalState;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.SimpleState;
import it.unifi.hierarchical.model.State;
import it.unifi.hierarchical.model.visitor.EndStateVisitor;
import it.unifi.hierarchical.model.visitor.StateVisitor;

public class SubstatesSteadyStateEvaluatorVisitor implements StateVisitor{

    private double parentLevelSSProb;
    private double parentLevelMeanSojourn;
    private Map<State, Double> meanSojournTimes;
    private Map<String, Double> subStateSSProbs;
    
    public SubstatesSteadyStateEvaluatorVisitor(double parentLevelSSProb, double parentLevelMeanSojourn, Map<State, Double> meanSojournTimes){
        this.parentLevelSSProb = parentLevelSSProb;
        this.parentLevelMeanSojourn = parentLevelMeanSojourn;
        this.subStateSSProbs = new HashMap<>();
        this.meanSojournTimes = meanSojournTimes;
    }
    
    @Override
    public void visit(SimpleState state) {}

    @Override
    public void visit(CompositeState state) {
        //Recursively use the visitor
        List<Region> regions = state.getRegions();
        for (Region region : regions) {
            Set<State> visited = new HashSet<>();
            Stack<State> toBeVisited = new Stack<>();
            toBeVisited.push(region.getInitialState());
            while(!toBeVisited.isEmpty()) {
                State current = toBeVisited.pop();
                visited.add(current);
                subStateSSProbs.put(current.getName(), parentLevelSSProb * meanSojournTimes.get(current)/parentLevelMeanSojourn);
                for (State successor : current.getNextStates()) {
                    if(!visited.contains(successor) || !toBeVisited.contains(successor)) {
                        //Check if its an exit state
                        EndStateVisitor visitor = new EndStateVisitor();
                        successor.accept(visitor);
                        if(!visitor.isEndState())
                            toBeVisited.add(successor);
                    }
                }
                
                SubstatesSteadyStateEvaluatorVisitor newVisitor = new SubstatesSteadyStateEvaluatorVisitor(
                        subStateSSProbs.get(current.getName()), 
                        meanSojournTimes.get(current), 
                        meanSojournTimes);
                current.accept(newVisitor);
                subStateSSProbs.putAll(newVisitor.getSubStateSSProbs());
            }
        }
    }

    @Override
    public void visit(FinalState state) {}

    @Override
    public void visit(ExitState state) {}
    
    public Map<String, Double> getSubStateSSProbs() {
        return subStateSSProbs;
    }

}

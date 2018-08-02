package it.unifi.hierarchical.analysis;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.oristool.math.OmegaBigDecimal;

import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.ExitState;
import it.unifi.hierarchical.model.FinalState;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.Region.RegionType;
import it.unifi.hierarchical.model.SimpleState;
import it.unifi.hierarchical.model.State;
import it.unifi.hierarchical.model.visitor.StateVisitor;
import it.unifi.hierarchical.utils.NumericalUtils;
import it.unifi.hierarchical.utils.StateUtils;

public class SojournTimeEvaluatorVisitor implements StateVisitor{
    
    private Map<State, NumericalValues> sojournTimeDistributions;
    private Map<Region, NumericalValues> regionSojournTimeDistributions;
    private Map<Region, TransientAnalyzer> regionTransientProbabilities;
    private Set<State> evaluated;
    private double timeStep;
    private double timeLimit;
    
    public SojournTimeEvaluatorVisitor(double timeStep, double timeLimit){
        this.sojournTimeDistributions = new HashMap<>();
        this.regionSojournTimeDistributions = new HashMap<>();
        this.regionTransientProbabilities = new HashMap<>();
        this.evaluated = new HashSet<>();
        this.timeStep = timeStep;
        this.timeLimit = timeLimit;
    }

    @Override
    public void visit(SimpleState state) {
        evaluated.add(state);
        //Evaluate its sojourn time distribution
        double[] values = NumericalUtils.evaluateFunction(state.getDensity(), new OmegaBigDecimal(""+timeLimit), new BigDecimal(""+timeStep));
        values = NumericalUtils.computeCDFFromPDF(values,  new BigDecimal(""+timeStep));
        sojournTimeDistributions.put(state, new NumericalValues(values, timeStep));
        
        //Visit successors not yet visited
        List<State> successors = state.getNextStates();
        for (State successor : successors) {
            if(evaluated.contains(successor))
                continue;
            successor.accept(this);
        }
    }

    @Override
    public void visit(CompositeState state) {
        evaluated.add(state);
        //Evaluate children sojourn time distribution
        List<Region> regions = state.getRegions();
        for (Region region : regions) {
            region.getInitialState().accept(this);
        }
        
        //Evaluate regions distribution
        Map<Region, NumericalValues> regionsSojournTimeDistributions = new HashMap<>();
        for (Region region : regions) {
            NumericalValues regionSojournTimeDistribution = evaluateRegionSojournTime(region);
            regionsSojournTimeDistributions.put(region, regionSojournTimeDistribution);
        }
        
        //Evaluate composite state distribution
        RegionType type = regions.get(0).getType();
        NumericalValues sojournTimeDistribution = null;
        switch (type) {
        case EXIT:
            sojournTimeDistribution = NumericalUtils.minCDF(regionsSojournTimeDistributions.values());
            break;
        case FINAL:
            sojournTimeDistribution = NumericalUtils.maxCDF(regionsSojournTimeDistributions.values());
            break;
        }
        sojournTimeDistributions.put(state, sojournTimeDistribution);
        //Visit successors not yet visited
        if(StateUtils.isCompositeWithBorderExit(state)) {
            CompositeState cState = (CompositeState) state;
            for(State exitState : cState.getNextStatesConditional().keySet()) {
                List<State> successors = cState.getNextStatesConditional().get(exitState);
                for (State successor : successors) {
                    if(evaluated.contains(successor))
                        continue;
                    successor.accept(this);
                }    
            }
        }else {
            List<State> successors = state.getNextStates();
            for (State successor : successors) {
                if(evaluated.contains(successor))
                    continue;
                successor.accept(this);
            }    
        }
    }

    private NumericalValues evaluateRegionSojournTime(Region region) {
        State initialState = region.getInitialState();
        List<State> smpStates = StateUtils.getReachableStates(region.getInitialState());
        State endState = StateUtils.findEndState(smpStates);
        TransientAnalyzer analyzer = 
                //new SMPAnalyzer(smpStates, sojournTimeDistributions, timeLimit, timeStep);
                new SMPAnalyzerWithBorderExitStates(region.getInitialState(), sojournTimeDistributions, regionSojournTimeDistributions, timeLimit, timeStep);
        NumericalValues sojournTimeDistribution = analyzer.getProbsFromTo(initialState, endState);
        regionSojournTimeDistributions.put(region, sojournTimeDistribution);
        regionTransientProbabilities.put(region, analyzer);
        return sojournTimeDistribution;
    }
    
    @Override
    public void visit(FinalState state) {
        evaluated.add(state);
        sojournTimeDistributions.put(state, null);
    }

    @Override
    public void visit(ExitState state) {
        evaluated.add(state);
        sojournTimeDistributions.put(state, null);
    }

    public Map<State, NumericalValues> getSojournTimeDistributions() {
        return sojournTimeDistributions;
    }
    
    public Map<Region, NumericalValues> getRegionSojournTimeDistributions() {
        return regionSojournTimeDistributions;
    }
    
    public Map<Region, TransientAnalyzer> getRegionTransientProbabilities(){
        return regionTransientProbabilities;
    }
    
}

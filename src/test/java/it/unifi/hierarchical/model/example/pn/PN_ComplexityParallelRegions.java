package it.unifi.hierarchical.model.example.pn;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.oristool.math.function.Function;
import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;
import org.oristool.petrinet.Transition;

public class PN_ComplexityParallelRegions {
    public static void build(PetriNet net, Marking marking, int nRegions, int nStates, Function distro) {
        
        List<Place> initialRegionsStates = new ArrayList<>();
        List<Place> finalRegionsStates = new ArrayList<>();
        
        for (int nr = 1; nr <=nRegions; nr++) {
            Place initialState = net.addPlace("S"+nr+"_1");
            initialRegionsStates.add(initialState);
            List<Place> currentRegionStates = new ArrayList<>();
            currentRegionStates.add(initialState);
            for (int ns = 2; ns <=nStates; ns++) {
                Transition t = net.addTransition("t_"+nr+"_"+(ns -1) +"_to_" +nr+"_"+ns );
                t.addFeature(StochasticTransitionFeature.of(distro));
                Place nextState = net.addPlace("S"+nr+"_"+ns);
                net.addPrecondition(currentRegionStates.get(currentRegionStates.size() -1), t);
                net.addPostcondition(t, nextState);
                currentRegionStates.add(nextState);
            }
            
            Place finalState = net.addPlace("S"+nr+"_FINAL");
            finalRegionsStates.add(finalState);
            
            Transition t = net.addTransition("t_"+nr+"_"+nStates +"_to_" +nr+"_FINAL" );
            t.addFeature(StochasticTransitionFeature.of(distro));
            
            net.addPrecondition(currentRegionStates.get(currentRegionStates.size() -1), t);
            net.addPostcondition(t, finalState);
            
            marking.setTokens(initialState, 1);
        }
        
        Transition tRestart = net.addTransition("t_restart");
        tRestart.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
        
        for (Place place : finalRegionsStates) {
            net.addPrecondition(place, tRestart);    
        }
        
        for (Place place : initialRegionsStates) {
            net.addPostcondition(tRestart, place);
        }
        
      }
}

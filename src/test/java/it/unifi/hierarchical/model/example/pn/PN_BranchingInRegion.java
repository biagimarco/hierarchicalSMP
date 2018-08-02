package it.unifi.hierarchical.model.example.pn;

import java.math.BigDecimal;

import org.oristool.models.pn.PostUpdater;
import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;
import org.oristool.petrinet.Transition;

public class PN_BranchingInRegion {

    public static void build(PetriNet net, Marking marking) {

        //Generating Nodes
        Place S0 = net.addPlace("S0");
        Place S2_1 = net.addPlace("S2_1");
        Place S2_2 = net.addPlace("S2_2");
        Place S2_3 = net.addPlace("S2_3");
        Place S2_4 = net.addPlace("S2_4");
        Place S2_5 = net.addPlace("S2_5");
        Place S2_Final1 = net.addPlace("S2_Final1");
        Place S2_Final2 = net.addPlace("S2_Final2");
        Transition restart = net.addTransition("restart");
        Transition s0 = net.addTransition("s0");
        Transition s2_1 = net.addTransition("s2_1");
        Transition s2_2 = net.addTransition("s2_2");
        Transition s2_3 = net.addTransition("s2_3");
        Transition s2_4 = net.addTransition("s2_4");
        Transition switch1 = net.addTransition("switch1");
        Transition switch2 = net.addTransition("switch2");

        //Generating Connectors
        net.addPostcondition(s0, S2_2);
        net.addPostcondition(s2_4, S2_Final1);
        net.addPrecondition(S2_Final2, restart);
        net.addPrecondition(S2_4, s2_4);
        net.addPrecondition(S2_3, s2_3);
        net.addPostcondition(s2_2, S2_Final2);
        net.addPostcondition(switch2, S2_4);
        net.addPostcondition(switch1, S2_3);
        net.addPostcondition(s0, S2_1);
        net.addPrecondition(S2_Final1, restart);
        net.addPrecondition(S2_2, s2_2);
        net.addPrecondition(S0, s0);
        net.addPostcondition(s2_3, S2_Final1);
        net.addPrecondition(S2_5, switch1);
        net.addPrecondition(S2_5, switch2);
        net.addPrecondition(S2_1, s2_1);
        net.addPostcondition(s2_1, S2_5);

        //Generating Properties
        marking.setTokens(S0, 1);
        marking.setTokens(S2_1, 0);
        marking.setTokens(S2_2, 0);
        marking.setTokens(S2_3, 0);
        marking.setTokens(S2_4, 0);
        marking.setTokens(S2_5, 0);
        marking.setTokens(S2_Final1, 0);
        marking.setTokens(S2_Final2, 0);
        restart.addFeature(new PostUpdater("S0=1;S2_1=0;S2_2=0;S2_3=0;S2_4=0;S2_5=0", net));
        restart.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
        restart.addFeature(new Priority(0));
        s0.addFeature(StochasticTransitionFeature.newUniformInstance(new BigDecimal("0"), new BigDecimal("1")));
        s2_1.addFeature(StochasticTransitionFeature.newUniformInstance(new BigDecimal("0"), new BigDecimal("1")));
        s2_2.addFeature(StochasticTransitionFeature.newUniformInstance(new BigDecimal("1"), new BigDecimal("4")));
        s2_3.addFeature(StochasticTransitionFeature.newUniformInstance(new BigDecimal("2"), new BigDecimal("3")));
        s2_4.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("2"), MarkingExpr.from("1", net)));
        s2_4.addFeature(new Priority(0));
        switch1.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("0.75", net)));
        switch1.addFeature(new Priority(0));
        switch2.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("0.25", net)));
        switch2.addFeature(new Priority(0));
      }
}

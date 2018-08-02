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

public class PN_ManyParallelRegions {
  public static void build(PetriNet net, Marking marking) {

    //Generating Nodes
    Place Reset = net.addPlace("Reset");
    Place S2 = net.addPlace("S2");
    Place S4 = net.addPlace("S4");
    Place S5 = net.addPlace("S5");
    Place S6 = net.addPlace("S6");
    Place S7 = net.addPlace("S7");
    Place S8 = net.addPlace("S8");
    Transition s2 = net.addTransition("s2");
    Transition s4 = net.addTransition("s4");
    Transition s5 = net.addTransition("s5");
    Transition s6 = net.addTransition("s6");
    Transition s7 = net.addTransition("s7");
    Transition s8 = net.addTransition("s8");
    Transition t6 = net.addTransition("t6");

    //Generating Connectors
    net.addInhibitorArc(Reset, s5);
    net.addInhibitorArc(Reset, s8);
    net.addInhibitorArc(Reset, s6);
    net.addInhibitorArc(Reset, s2);
    net.addInhibitorArc(Reset, s7);
    net.addInhibitorArc(Reset, s4);
    net.addPostcondition(s6, Reset);
    net.addPostcondition(s4, Reset);
    net.addPrecondition(S5, s5);
    net.addPostcondition(s2, S4);
    net.addPostcondition(s5, Reset);
    net.addPostcondition(s8, Reset);
    net.addPrecondition(S7, s7);
    net.addPrecondition(S4, s4);
    net.addPrecondition(S2, s2);
    net.addPrecondition(S6, s6);
    net.addPostcondition(s7, Reset);
    net.addPrecondition(S8, s8);
    net.addPostcondition(s2, S5);
    net.addPrecondition(Reset, t6);

    //Generating Properties
    marking.setTokens(Reset, 0);
    marking.setTokens(S2, 1);
    marking.setTokens(S4, 0);
    marking.setTokens(S5, 0);
    marking.setTokens(S6, 1);
    marking.setTokens(S7, 1);
    marking.setTokens(S8, 1);
    s2.addFeature(StochasticTransitionFeature.newUniformInstance(new BigDecimal("1"), new BigDecimal("2")));
    s4.addFeature(StochasticTransitionFeature.newUniformInstance(new BigDecimal("1"), new BigDecimal("5")));
    s5.addFeature(StochasticTransitionFeature.newUniformInstance(new BigDecimal("2"), new BigDecimal("4")));
    s6.addFeature(StochasticTransitionFeature.newUniformInstance(new BigDecimal("1"), new BigDecimal("8")));
    s7.addFeature(StochasticTransitionFeature.newUniformInstance(new BigDecimal("5"), new BigDecimal("9")));
    s8.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("7.9"), MarkingExpr.from("1", net)));
    s8.addFeature(new Priority(0));
    t6.addFeature(new PostUpdater("Reset=0;S5=0;S4=0;S2=1;S6=1;S7=1;S8=1", net));
    t6.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
    t6.addFeature(new Priority(0));
  }
}
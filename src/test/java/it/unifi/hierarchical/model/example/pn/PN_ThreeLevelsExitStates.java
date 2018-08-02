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

public class PN_ThreeLevelsExitStates {
  public static void build(PetriNet net, Marking marking) {

    //Generating Nodes
    Place Restart = net.addPlace("Restart");
    Place p0 = net.addPlace("p0");
    Place p1 = net.addPlace("p1");
    Place p2 = net.addPlace("p2");
    Place p3 = net.addPlace("p3");
    Place p4 = net.addPlace("p4");
    Place p5 = net.addPlace("p5");
    Transition restart = net.addTransition("restart");
    Transition s2 = net.addTransition("s2");
    Transition s4 = net.addTransition("s4");
    Transition s6 = net.addTransition("s6");
    Transition s7 = net.addTransition("s7");
    Transition s8 = net.addTransition("s8");
    Transition s9 = net.addTransition("s9");

    //Generating Connectors
    net.addPostcondition(restart, p1);
    net.addPrecondition(p1, s9);
    net.addPostcondition(s2, p3);
    net.addPostcondition(s2, p2);
    net.addPrecondition(p2, s4);
    net.addPrecondition(Restart, restart);
    net.addPrecondition(p3, s8);
    net.addPostcondition(restart, p0);
    net.addPostcondition(s8, Restart);
    net.addPrecondition(p0, s2);
    net.addPostcondition(s9, Restart);
    net.addPostcondition(s4, p5);
    net.addPostcondition(s4, p4);
    net.addPostcondition(s7, Restart);
    net.addPostcondition(s6, Restart);
    net.addPrecondition(p5, s7);
    net.addPrecondition(p4, s6);
    net.addInhibitorArc(Restart, s9);
    net.addInhibitorArc(Restart, s8);
    net.addInhibitorArc(Restart, s7);
    net.addInhibitorArc(Restart, s6);
    net.addInhibitorArc(Restart, s4);
    net.addInhibitorArc(Restart, s2);

    //Generating Properties
    marking.setTokens(Restart, 0);
    marking.setTokens(p0, 1);
    marking.setTokens(p1, 1);
    marking.setTokens(p2, 0);
    marking.setTokens(p3, 0);
    marking.setTokens(p4, 0);
    marking.setTokens(p5, 0);
    restart.addFeature(new PostUpdater("p0=1;p1=1;p2=0;p3=0;p4=0;p5=0;Restart=0", net));
    restart.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
    restart.addFeature(new Priority(0));
    s2.addFeature(StochasticTransitionFeature.newUniformInstance(new BigDecimal("1"), new BigDecimal("2")));
    s4.addFeature(StochasticTransitionFeature.newUniformInstance(new BigDecimal("1"), new BigDecimal("2")));
    s6.addFeature(StochasticTransitionFeature.newUniformInstance(new BigDecimal("2"), new BigDecimal("4")));
    s7.addFeature(StochasticTransitionFeature.newUniformInstance(new BigDecimal("1"), new BigDecimal("5")));
    s8.addFeature(StochasticTransitionFeature.newUniformInstance(new BigDecimal("1"), new BigDecimal("6")));
    s9.addFeature(StochasticTransitionFeature.newUniformInstance(new BigDecimal("2"), new BigDecimal("8")));
  }
}

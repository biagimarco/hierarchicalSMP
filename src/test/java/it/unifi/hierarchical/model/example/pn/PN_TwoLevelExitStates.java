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

public class PN_TwoLevelExitStates {
  public static void build(PetriNet net, Marking marking) {

    //Generating Nodes
    Place p0 = net.addPlace("p0");
    Place p1 = net.addPlace("p1");
    Place p2 = net.addPlace("p2");
    Place p3 = net.addPlace("p3");
    Place p4 = net.addPlace("p4");
    Transition t0 = net.addTransition("t0");
    Transition t1 = net.addTransition("t1");
    Transition t2 = net.addTransition("t2");
    Transition t3 = net.addTransition("t3");
    Transition t4 = net.addTransition("t4");

    //Generating Connectors
    net.addInhibitorArc(p2, t0);
    net.addInhibitorArc(p2, t1);
    net.addInhibitorArc(p2, t3);
    net.addPrecondition(p1, t1);
    net.addPostcondition(t4, p3);
    net.addPostcondition(t2, p1);
    net.addPostcondition(t4, p4);
    net.addPostcondition(t1, p2);
    net.addPostcondition(t0, p2);
    net.addPostcondition(t3, p2);
    net.addPrecondition(p4, t3);
    net.addPrecondition(p0, t0);
    net.addPostcondition(t2, p0);
    net.addPrecondition(p3, t2);
    net.addPrecondition(p2, t4);

    //Generating Properties
    marking.setTokens(p0, 0);
    marking.setTokens(p1, 0);
    marking.setTokens(p2, 0);
    marking.setTokens(p3, 1);
    marking.setTokens(p4, 1);
    t0.addFeature(StochasticTransitionFeature.newUniformInstance(new BigDecimal("2"), new BigDecimal("4")));
    t1.addFeature(StochasticTransitionFeature.newUniformInstance(new BigDecimal("2"), new BigDecimal("4")));
    t2.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("4"), MarkingExpr.from("1", net)));
    t2.addFeature(new Priority(0));
    t3.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("7"), MarkingExpr.from("1", net)));
    t3.addFeature(new Priority(0));
    t4.addFeature(new PostUpdater("p4=1;p3=1;p0=0;p1=0;", net));
    t4.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
    t4.addFeature(new Priority(0));
  }
}

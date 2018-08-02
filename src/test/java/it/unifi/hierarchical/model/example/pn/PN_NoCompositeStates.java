package it.unifi.hierarchical.model.example.pn;

import java.math.BigDecimal;
import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;
import org.oristool.petrinet.Transition;

public class PN_NoCompositeStates {
  public static void build(PetriNet net, Marking marking) {

    //Generating Nodes
    Place BranchState = net.addPlace("BranchState");
    Place State1 = net.addPlace("State1");
    Place State2 = net.addPlace("State2");
    Place State3 = net.addPlace("State3");
    Place State4 = net.addPlace("State4");
    Transition t0 = net.addTransition("t0");
    Transition t1 = net.addTransition("t1");
    Transition t2 = net.addTransition("t2");
    Transition t3 = net.addTransition("t3");
    Transition t4 = net.addTransition("t4");
    Transition t5 = net.addTransition("t5");

    //Generating Connectors
    net.addPrecondition(State1, t0);
    net.addPostcondition(t0, State2);
    net.addPrecondition(State2, t1);
    net.addPostcondition(t1, State3);
    net.addPrecondition(State3, t2);
    net.addPostcondition(t2, BranchState);
    net.addPrecondition(BranchState, t4);
    net.addPrecondition(BranchState, t5);
    net.addPostcondition(t5, State4);
    net.addPrecondition(State4, t3);
    net.addPostcondition(t3, State1);
    net.addPostcondition(t4, State1);

    //Generating Properties
    marking.setTokens(BranchState, 0);
    marking.setTokens(State1, 1);
    marking.setTokens(State2, 0);
    marking.setTokens(State3, 0);
    marking.setTokens(State4, 0);
    t0.addFeature(StochasticTransitionFeature.newUniformInstance(new BigDecimal("2"), new BigDecimal("3")));
    t1.addFeature(StochasticTransitionFeature.newUniformInstance(new BigDecimal("3"), new BigDecimal("4")));
    t2.addFeature(StochasticTransitionFeature.newUniformInstance(new BigDecimal("1"), new BigDecimal("2")));
    t3.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("2"), MarkingExpr.from("1", net)));
    t3.addFeature(new Priority(0));
    t4.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("0.3", net)));
    t4.addFeature(new Priority(0));
    t5.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("0.7", net)));
    t5.addFeature(new Priority(0));
  }
}

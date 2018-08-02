package it.unifi.hierarchical.history;

import org.oristool.math.function.Function;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;
import org.oristool.petrinet.Transition;

public class HistoryPNModel {

    public static void build(PetriNet net, Marking marking, Function dens_T1_1, Function dens_T1_2, Function dens_T2_1, Function dens_T2_2, Function dens_T3_1, Function dens_T3_2) {

        //Generating Nodes
        Place E1 = net.addPlace("E1");
        Place E2 = net.addPlace("E2");
        Place E3 = net.addPlace("E3");
        Place R1_1 = net.addPlace("R1_1");
        Place R1_2 = net.addPlace("R1_2");
        Place R2_1 = net.addPlace("R2_1");
        Place R2_2 = net.addPlace("R2_2");
        Place R3_1 = net.addPlace("R3_1");
        Place R3_2 = net.addPlace("R3_2");
        Transition t1_1 = net.addTransition("t1_1");
        Transition t1_2 = net.addTransition("t1_2");
        Transition t2_1 = net.addTransition("t2_1");
        Transition t2_2 = net.addTransition("t2_2");
        Transition t3_1 = net.addTransition("t3_1");
        Transition t3_2 = net.addTransition("t3_2");

        //Generating Connectors
        net.addInhibitorArc(E3, t2_1);
        net.addInhibitorArc(E1, t2_1);
        net.addInhibitorArc(E3, t1_1);
        net.addInhibitorArc(E2, t3_1);
        net.addInhibitorArc(E1, t3_1);
        net.addInhibitorArc(E3, t2_2);
        net.addInhibitorArc(E1, t3_2);
        net.addInhibitorArc(E2, t3_2);
        net.addInhibitorArc(E3, t1_2);
        net.addInhibitorArc(E2, t1_1);
        net.addInhibitorArc(E2, t1_2);
        net.addInhibitorArc(E1, t2_2);
        net.addPrecondition(R1_1, t1_1);
        net.addPostcondition(t2_1, R2_2);
        net.addPostcondition(t2_2, E2);
        net.addPrecondition(R2_1, t2_1);
        net.addPostcondition(t3_2, E3);
        net.addPostcondition(t3_1, R3_2);
        net.addPostcondition(t1_1, R1_2);
        net.addPrecondition(R1_2, t1_2);
        net.addPrecondition(R3_2, t3_2);
        net.addPostcondition(t1_2, E1);
        net.addPrecondition(R2_2, t2_2);
        net.addPrecondition(R3_1, t3_1);

        //Generating Properties
        marking.setTokens(E1, 0);
        marking.setTokens(E2, 0);
        marking.setTokens(E3, 0);
        marking.setTokens(R1_1, 1);
        marking.setTokens(R1_2, 0);
        marking.setTokens(R2_1, 1);
        marking.setTokens(R2_2, 0);
        marking.setTokens(R3_1, 1);
        marking.setTokens(R3_2, 0);
        t1_1.addFeature(StochasticTransitionFeature.of(dens_T1_1));
        t1_2.addFeature(StochasticTransitionFeature.of(dens_T1_2));
        t2_1.addFeature(StochasticTransitionFeature.of(dens_T2_1));
        t2_2.addFeature(StochasticTransitionFeature.of(dens_T2_2));
        t3_1.addFeature(StochasticTransitionFeature.of(dens_T3_1));
        t3_2.addFeature(StochasticTransitionFeature.of(dens_T3_2));
      }
}

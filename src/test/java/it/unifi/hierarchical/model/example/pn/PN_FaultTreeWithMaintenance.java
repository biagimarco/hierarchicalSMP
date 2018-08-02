package it.unifi.hierarchical.model.example.pn;

import java.math.BigDecimal;

import org.oristool.math.function.Function;
import org.oristool.models.pn.PostUpdater;
import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;
import org.oristool.petrinet.Transition;

public class PN_FaultTreeWithMaintenance {

    public static void build(PetriNet net, Marking marking, Function aFail, Function bFail, Function cFail, Function dFail, Function maintenancePeriod, Function repair, Function preventiveMaintenance) {

        //Generating Nodes
        Place A = net.addPlace("A");
        Place AFailed = net.addPlace("AFailed");
        Place B = net.addPlace("B");
        Place BFailed = net.addPlace("BFailed");
        Place C = net.addPlace("C");
        Place CFailed = net.addPlace("CFailed");
        Place D = net.addPlace("D");
        Place DFailed = net.addPlace("DFailed");
        Place PreventiveMaintenance = net.addPlace("PreventiveMaintenance");
        Place Repair = net.addPlace("Repair");
        Place WaitingMainteance = net.addPlace("WaitingMaintenance");
        Transition abFail = net.addTransition("abFail");
        Transition cdFail = net.addTransition("cdFail");
        Transition maintenanceClock = net.addTransition("maintenanceClock");
        Transition t0 = net.addTransition("t0");
        Transition t1 = net.addTransition("t1");
        Transition t2 = net.addTransition("t2");
        Transition t3 = net.addTransition("t3");
        Transition t4 = net.addTransition("t4");
        Transition t5 = net.addTransition("t5");

        //Generating Connectors
        net.addPrecondition(A, t0);
        net.addPrecondition(B, t1);
        net.addPrecondition(C, t2);
        net.addPrecondition(D, t3);
        net.addPostcondition(t0, AFailed);
        net.addPostcondition(t1, BFailed);
        net.addPostcondition(t2, CFailed);
        net.addPostcondition(t3, DFailed);
        net.addPrecondition(AFailed, abFail);
        net.addPrecondition(BFailed, abFail);
        net.addPostcondition(abFail, Repair);
        net.addPrecondition(CFailed, cdFail);
        net.addPrecondition(DFailed, cdFail);
        net.addPostcondition(cdFail, Repair);
        net.addPrecondition(WaitingMainteance, maintenanceClock);
        net.addPostcondition(maintenanceClock, PreventiveMaintenance);
        net.addPrecondition(Repair, t4);
        net.addPrecondition(PreventiveMaintenance, t5);

        //Generating Properties
        marking.setTokens(A, 1);
        marking.setTokens(AFailed, 0);
        marking.setTokens(B, 1);
        marking.setTokens(BFailed, 0);
        marking.setTokens(C, 1);
        marking.setTokens(CFailed, 0);
        marking.setTokens(D, 1);
        marking.setTokens(DFailed, 0);
        marking.setTokens(PreventiveMaintenance, 0);
        marking.setTokens(Repair, 0);
        marking.setTokens(WaitingMainteance, 1);
        abFail.addFeature(new PostUpdater("A=0;B=0;C=0;D=0;AFailed=0;BFailed=0;CFailed=0;DFailed=0;WaitingMaintenance=0", net));
        abFail.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
        abFail.addFeature(new Priority(0));
        cdFail.addFeature(new PostUpdater("A=0;B=0;C=0;D=0;AFailed=0;BFailed=0;CFailed=0;DFailed=0;WaitingMaintenance=0", net));
        cdFail.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
        cdFail.addFeature(new Priority(0));
        maintenanceClock.addFeature(new PostUpdater("A=0;B=0;C=0;D=0;AFailed=0;BFailed=0;CFailed=0;DFailed=0;", net));
        maintenanceClock.addFeature(StochasticTransitionFeature.of(maintenancePeriod));
        maintenanceClock.addFeature(new Priority(0));
        t0.addFeature(StochasticTransitionFeature.of(aFail));
        t1.addFeature(StochasticTransitionFeature.of(bFail));
        t2.addFeature(StochasticTransitionFeature.of(cFail));
        t3.addFeature(StochasticTransitionFeature.of(dFail));
        t4.addFeature(new PostUpdater("A=1;B=1;C=1;D=1;WaitingMaintenance=1", net));
        t4.addFeature(StochasticTransitionFeature.of(repair));
        t5.addFeature(new PostUpdater("A=1;B=1;C=1;D=1;WaitingMaintenance=1", net));
        t5.addFeature(StochasticTransitionFeature.of(preventiveMaintenance));
      }
}

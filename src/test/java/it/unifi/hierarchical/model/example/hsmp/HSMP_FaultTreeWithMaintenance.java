package it.unifi.hierarchical.model.example.hsmp;

import java.util.Arrays;
import java.util.Map;

import org.oristool.math.function.Function;

import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.ExitState;
import it.unifi.hierarchical.model.FinalState;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.Region.RegionType;
import it.unifi.hierarchical.model.SimpleState;
import it.unifi.hierarchical.model.State;

public class HSMP_FaultTreeWithMaintenance {

    public static HierarchicalSMP build(Function aFail, Function bFail, Function cFail, Function dFail, Function maintenancePeriod, Function repair, Function preventiveMaintenance) {

        //Level depth 2
        int depth = 2;
        State stateA = new SimpleState(
                "A", 
                aFail, 
                Arrays.asList(new FinalState(depth)), 
                Arrays.asList(1.0), 
                depth);
        
        State stateB = new SimpleState(
                "B", 
                bFail, 
                Arrays.asList(new FinalState(depth)), 
                Arrays.asList(1.0), 
                depth);
        
        Region region1_AND1 = new Region(stateA, RegionType.FINAL);
        Region region2_AND1 = new Region(stateB, RegionType.FINAL);
        
        State stateC = new SimpleState(
                "C", 
                cFail, 
                Arrays.asList(new FinalState(depth)), 
                Arrays.asList(1.0), 
                depth);
        
        State stateD = new SimpleState(
                "D", 
                dFail, 
                Arrays.asList(new FinalState(depth)), 
                Arrays.asList(1.0), 
                depth);
        
        Region region1_AND2 = new Region(stateC, RegionType.FINAL);
        Region region2_AND2 = new Region(stateD, RegionType.FINAL);
        
        //Level depth 1
        depth = 1;
        State exitFailure1 = new ExitState(depth);
        State exitFailure2 = new ExitState(depth);
        State exitRepair = new ExitState(depth);

        State stateAND1 = new CompositeState(
                "AND1",  
                Arrays.asList(region1_AND1, region2_AND1), 
                Arrays.asList(exitFailure1), 
                Arrays.asList(1.0),
                depth);
        
        State stateAND2 = new CompositeState(
                "AND2",  
                Arrays.asList(region1_AND2, region2_AND2), 
                Arrays.asList(exitFailure2), 
                Arrays.asList(1.0),
                depth);
        
        State stateWait = new SimpleState(
                "WaitingMaintenance", 
                maintenancePeriod, 
                Arrays.asList(exitRepair), 
                Arrays.asList(1.0),
                depth);
        
        Region region1_OR = new Region(stateAND1, RegionType.EXIT);
        Region region2_OR = new Region(stateAND2, RegionType.EXIT);
        Region region3_OR = new Region(stateWait, RegionType.EXIT);
        
        //Level depth 0
        depth = 0;
        
        State stateRepair = new SimpleState(
                "Repair", 
                repair,
                null, 
                null, 
                depth);
        
        State statePreventive = new SimpleState(
                "PreventiveMaintenance", 
                preventiveMaintenance,
                null, 
                null, 
                depth);
        
        
        State stateOR = new CompositeState(
                "OR",  
                Arrays.asList(region1_OR, region2_OR, region3_OR), 
                Map.of(
                        exitFailure1,
                        Arrays.asList(stateRepair),
                        exitFailure2,
                        Arrays.asList(stateRepair),
                        exitRepair,
                        Arrays.asList(statePreventive)), 
                Map.of(
                        exitFailure1,
                        Arrays.asList(1.0),
                        exitFailure2,
                        Arrays.asList(1.0),
                        exitRepair,
                        Arrays.asList(1.0)), 
                depth);

        stateRepair.setNextStates(Arrays.asList(stateOR), Arrays.asList(1.0));
        statePreventive.setNextStates(Arrays.asList(stateOR), Arrays.asList(1.0));
        
        return new HierarchicalSMP(stateOR);
        
    }

}

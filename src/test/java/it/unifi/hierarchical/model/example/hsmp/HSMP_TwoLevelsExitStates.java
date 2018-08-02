package it.unifi.hierarchical.model.example.hsmp;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.function.GEN;

import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.ExitState;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.Region.RegionType;
import it.unifi.hierarchical.model.SimpleState;
import it.unifi.hierarchical.model.State;

public class HSMP_TwoLevelsExitStates {

    public static HierarchicalSMP build() {
        
        //Level depth 2
        int depth = 2;
        State state4 = new SimpleState(
                "State4", 
                GEN.newUniform(new OmegaBigDecimal("2"), new OmegaBigDecimal("4")), 
                Arrays.asList(new ExitState(depth)), 
                Arrays.asList(1.0), 
                depth);
        
        State state5 = new SimpleState(
                "State5", 
                GEN.newUniform(new OmegaBigDecimal("2"), new OmegaBigDecimal("4")), 
                Arrays.asList(new ExitState(depth)), 
                Arrays.asList(1.0), 
                depth);
        
        Region region1_3 = new Region(state4, RegionType.EXIT);
        Region region2_3 = new Region(state5, RegionType.EXIT);
        
        //Level depth 1
        depth = 1;
        State state3 = new CompositeState(
                "State3",  
                Arrays.asList(region1_3, region2_3), 
                Arrays.asList(new ExitState(depth)), 
                Arrays.asList(1.0), 
                depth);
        
        State state2 = new SimpleState(
                "State2", 
                GEN.newDeterministic(new BigDecimal("4")),
                Arrays.asList(state3), 
                Arrays.asList(1.0), 
                depth);
        
        State state6 = new SimpleState(
                "State6", 
                GEN.newDeterministic(new BigDecimal("7")),
                Arrays.asList(new ExitState(depth)), 
                Arrays.asList(1.0), 
                depth);
        
        Region region1_1 = new Region(state2, RegionType.EXIT);
        Region region2_1 = new Region(state6, RegionType.EXIT);

        //Level depth 0
        depth = 0;
        List<State> nextStates = null;//Required to avoid ambiguity
        State state1 = new CompositeState(
                "State1",  
                Arrays.asList(region1_1, region2_1), 
                nextStates, 
                null, 
                depth);
        state1.setNextStates(Arrays.asList(state1), Arrays.asList(1.0));
        
        return new HierarchicalSMP(state1);
    }
}

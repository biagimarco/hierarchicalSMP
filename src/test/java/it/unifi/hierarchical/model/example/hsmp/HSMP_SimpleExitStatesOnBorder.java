package it.unifi.hierarchical.model.example.hsmp;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.function.GEN;

import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.ExitState;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.Region.RegionType;
import it.unifi.hierarchical.model.SimpleState;
import it.unifi.hierarchical.model.State;

public class HSMP_SimpleExitStatesOnBorder {

  public static HierarchicalSMP build() {
        
        //Level depth 1
        int depth = 1;
        State Ea = new ExitState(depth);
        State state3 = new SimpleState(
                "State3", 
                GEN.newUniform(new OmegaBigDecimal("3"), new OmegaBigDecimal("5")), 
                Arrays.asList(Ea), 
                Arrays.asList(1.0), 
                depth);
        
        State Eb = new ExitState(depth);
        State state4 = new SimpleState(
                "State4", 
                GEN.newUniform(new OmegaBigDecimal("2"), new OmegaBigDecimal("4")), 
                Arrays.asList(Eb), 
                Arrays.asList(1.0), 
                depth);
        
        Region region1_2 = new Region(state3, RegionType.EXIT);
        Region region2_2 = new Region(state4, RegionType.EXIT);
        
        //Level depth 0
        depth = 0;
        List<State> nextStates = null;//Required to avoid ambiguity
        
        State state5 = new SimpleState(
                "State5", 
                GEN.newDeterministic(new BigDecimal("2")),
                nextStates, 
                null, 
                depth);
        State state6 = new SimpleState(
                "State6", 
                GEN.newDeterministic(new BigDecimal("3")),
                nextStates, 
                null, 
                depth);
                
        State state2 = new CompositeState(
                "State2",  
                Arrays.asList(region1_2, region2_2), 
                Map.of(
                        Ea,
                        Arrays.asList(state5),
                        Eb,
                        Arrays.asList(state6)), 
                Map.of(
                        Ea,
                        Arrays.asList(1.0),
                        Eb,
                        Arrays.asList(1.0)), 
                depth);
        
        State state1 = new SimpleState(
                "State1", 
                GEN.newDeterministic(new BigDecimal("4")),
                Arrays.asList(state2), 
                Arrays.asList(1.0), 
                depth);
       
        state5.setNextStates(Arrays.asList(state1), Arrays.asList(1.0));
        state6.setNextStates(Arrays.asList(state1), Arrays.asList(1.0));
        
        return new HierarchicalSMP(state1);
    }
    
}

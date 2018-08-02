package it.unifi.hierarchical.model.example.hsmp;

import java.math.BigDecimal;
import java.util.Arrays;

import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.function.GEN;

import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.SimpleState;

public class HSMP_NoCompositeStates {

    public static HierarchicalSMP build() {
        
        SimpleState state1 = new SimpleState(
                "State1", 
                GEN.newUniform(new OmegaBigDecimal("2"), new OmegaBigDecimal("3")), 
                null, 
                null,
                0);
        SimpleState state2 = new SimpleState(
                "State2", 
                GEN.newUniform(new OmegaBigDecimal("3"), new OmegaBigDecimal("4")), 
                null, 
                null,
                0);
        SimpleState state3 = new SimpleState(
                "State3", 
                GEN.newUniform(new OmegaBigDecimal("1"), new OmegaBigDecimal("2")), 
                null, 
                null,
                0);
        SimpleState state4 = new SimpleState(
                "State4", 
                GEN.newDeterministic(new BigDecimal("2")), 
                null, 
                null,
                0);
        
        state1.setNextStates(Arrays.asList(state2), Arrays.asList(1.0));
        state2.setNextStates(Arrays.asList(state3), Arrays.asList(1.0));
        state3.setNextStates(Arrays.asList(state1, state4), Arrays.asList(0.3, 0.7));
        state4.setNextStates(Arrays.asList(state1), Arrays.asList(1.0));
        
        return new HierarchicalSMP(state1);
    }
    
}

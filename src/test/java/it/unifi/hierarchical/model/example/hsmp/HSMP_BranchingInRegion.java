package it.unifi.hierarchical.model.example.hsmp;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.function.GEN;

import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.FinalState;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.Region.RegionType;
import it.unifi.hierarchical.model.SimpleState;
import it.unifi.hierarchical.model.State;

public class HSMP_BranchingInRegion {

    public static HierarchicalSMP build() {
        
        
        //Level depth 1
        int  depth = 1;

        FinalState S2_final1 = new FinalState(depth);
        
        State S2_3 = new SimpleState(
                "S2_3",
                GEN.newUniform(new OmegaBigDecimal("2"), new OmegaBigDecimal("3")),
                Arrays.asList(S2_final1),
                Arrays.asList(1.0),
                depth);
        
        State S2_4 = new SimpleState(
                "S2_4",
                GEN.newDeterministic(new BigDecimal("2")),
                Arrays.asList(S2_final1),
                Arrays.asList(1.0),
                depth);
        
        State S2_1 = new SimpleState(
                "S2_1", 
                GEN.newUniform(new OmegaBigDecimal("0"), new OmegaBigDecimal("1")), 
                Arrays.asList(S2_3, S2_4), 
                Arrays.asList(0.75,0.25),
                depth);
        
        State S2_2 = new SimpleState(
                "S2_2",
                GEN.newUniform(new OmegaBigDecimal("1"), new OmegaBigDecimal("4")),
                Arrays.asList(new FinalState(depth)),
                Arrays.asList(1.0),
                depth);
        
        Region r1 = new Region(S2_1, RegionType.FINAL);
        Region r2 = new Region(S2_2, RegionType.FINAL);

        //Level depth 0
        depth = 0;
        
        List<State> nextStates = null;
        State S2 = new CompositeState(
                "S2", 
                Arrays.asList(r1, r2), 
                nextStates, 
                null, 
                depth);
        
        State S0 = new SimpleState(
                "S0",
                GEN.newUniform(new OmegaBigDecimal("0"), new OmegaBigDecimal("1")),
                Arrays.asList(S2),
                Arrays.asList(1.0),
                depth);
        
        S2.setNextStates(
                Arrays.asList(S0),
                Arrays.asList(1.0));
        
        return new HierarchicalSMP(S0);
        
    }
}

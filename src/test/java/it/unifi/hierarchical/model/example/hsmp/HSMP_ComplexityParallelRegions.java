package it.unifi.hierarchical.model.example.hsmp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.function.Function;
import org.oristool.math.function.GEN;

import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.FinalState;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.SimpleState;
import it.unifi.hierarchical.model.State;
import it.unifi.hierarchical.model.Region.RegionType;

public class HSMP_ComplexityParallelRegions {

    public static HierarchicalSMP build(int nRegions, int nStates, Function distro) {
        
        int depth = 1;
        
        List<Region> regions = new ArrayList<>();
        for (int nr = 1; nr <=nRegions; nr++) {
            List<State> states = new ArrayList<>();
            for (int ns = 1; ns <=nStates; ns++) {
                State s = new SimpleState(
                        "S"+nr+"_"+ns,
                        distro,
                        states.size()==0? Arrays.asList(new FinalState(depth)) : Arrays.asList(states.get(ns -2)) ,
                        Arrays.asList(1.0),
                        depth);      
                states.add(s);
            }
            
            regions.add(new Region(states.get(states.size() -1), RegionType.FINAL));
        }
        
        depth = 0;
        List<State> nextStates = null;
        State sComposite = new CompositeState(
                "S_Comp", 
                regions, 
                nextStates, 
                null, 
                depth);
        sComposite.setNextStates(Arrays.asList(sComposite), Arrays.asList(1.0));
        
        return new HierarchicalSMP(sComposite);
        
    }
}

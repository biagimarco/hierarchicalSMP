package it.unifi.hierarchical.history;

import java.util.Arrays;
import java.util.List;

import org.oristool.math.function.Function;

import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.ExitState;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.Region.RegionType;
import it.unifi.hierarchical.model.SimpleState;
import it.unifi.hierarchical.model.State;

/**
 * Note that this model doesn't really contains an history state. It is only used to verify the correctness of the evaluation
 *
 */
public class HistoryHSMPModel {

    public static HierarchicalSMP build(Function dens_T1_1, Function dens_T1_2, Function dens_T2_1, Function dens_T2_2, Function dens_T3_1, Function dens_T3_2) {
        // Level depth 1
        int depth = 1;
        State stateR12 = new SimpleState("R1_2", dens_T1_2,
                Arrays.asList(new ExitState(depth)), Arrays.asList(1.0), depth);

        State stateR11 = new SimpleState("R1_1", dens_T1_1,
                Arrays.asList(stateR12), Arrays.asList(1.0), depth);
        
        Region region1 = new Region(stateR11, RegionType.EXIT);

        State stateR22 = new SimpleState("R2_2", dens_T2_2,
                Arrays.asList(new ExitState(depth)), Arrays.asList(1.0), depth);

        State stateR21 = new SimpleState("R2_1", dens_T2_1,
                Arrays.asList(stateR22), Arrays.asList(1.0), depth);
        
        Region region2 = new Region(stateR21, RegionType.EXIT);

        State stateR32 = new SimpleState("R3_2", dens_T3_2,
                Arrays.asList(new ExitState(depth)), Arrays.asList(1.0), depth);

        State stateR31 = new SimpleState("R3_1", dens_T3_1,
                Arrays.asList(stateR32), Arrays.asList(1.0), depth);
        
        Region region3 = new Region(stateR31, RegionType.EXIT);

        // Level depth 0
        depth = 0;
        List<State> nextStates = null;
        State parentComp = new CompositeState("State3", Arrays.asList(region1, region2, region3), nextStates,
                null, depth);
        parentComp.setNextStates(Arrays.asList(parentComp), Arrays.asList(1.0));
        
        
        return new HierarchicalSMP(parentComp);
    }

}

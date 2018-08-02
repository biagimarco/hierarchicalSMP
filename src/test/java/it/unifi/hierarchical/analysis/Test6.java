package it.unifi.hierarchical.analysis;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.Map;

import org.junit.Test;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.SteadyStateSolution;
import org.oristool.models.stpn.steady.RegSteadyState;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.PetriNet;

import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.example.hsmp.HSMP_BranchingInRegion;
import it.unifi.hierarchical.model.example.pn.PN_BranchingInRegion;

/**
 * Test to check correctness of analysis when branching is present inside a composite state
 */
public class Test6 {
    
    private static final double TIME_STEP = 0.001;
    private static final double TIME_LIMIT = 5;//Need to be chosen according to the model, based on which is the maximum time elapsed in a region or a state
    private static final double DELTA = 0.01;
    
    @Test
    public void test6() {
        
        //HSMP
        //Build the model
        HierarchicalSMP model = HSMP_BranchingInRegion.build();
        
        //Analyze
        Date start = new Date();
        HierarchicalSMPAnalysis analysis = new HierarchicalSMPAnalysis(model);
        Map<String, Double> ssHSMP = analysis.evaluateSteadyState(TIME_STEP, TIME_LIMIT);
        Double resultHSMP1 = ssHSMP.get("S0");
        Double resultHSMP2 = ssHSMP.get("S2_3");
        Double resultHSMP3 = ssHSMP.get("S2_4");
        Double resultHSMP4 = ssHSMP.get("S2_1");
        Double resultHSMP5 = ssHSMP.get("S2_2");
        Double resultHSMP6 = ssHSMP.get("S2");
        Date end = new Date();
        System.out.println("Time Hierarchical SMP analysis:" + (end.getTime() - start.getTime()) + "ms");
        
        //PN
        PetriNet net = new PetriNet();
        Marking m = new Marking();
        PN_BranchingInRegion.build(net, m);
        
        start = new Date();
        RegSteadyState analysisPN = RegSteadyState.builder().build();
        SteadyStateSolution<Marking> ssPN = analysisPN.compute(net, m);
        RewardRate reward1 = RewardRate.fromString("S0");
        RewardRate reward2 = RewardRate.fromString("S2_3");
        RewardRate reward3 = RewardRate.fromString("S2_4");
        RewardRate reward4 = RewardRate.fromString("S2_1");
        RewardRate reward5 = RewardRate.fromString("S2_2");
        RewardRate reward6 = RewardRate.fromString("If(S2_2+S2_Final2>0,1,0)");
        SteadyStateSolution<RewardRate> rewardPN = SteadyStateSolution.computeRewards(ssPN, reward1, reward2, reward3, reward4, reward5, reward6);
        double resultPN1 = rewardPN.getSteadyState().get(reward1).doubleValue();
        double resultPN2 = rewardPN.getSteadyState().get(reward2).doubleValue();
        double resultPN3 = rewardPN.getSteadyState().get(reward3).doubleValue();
        double resultPN4 = rewardPN.getSteadyState().get(reward4).doubleValue();
        double resultPN5 = rewardPN.getSteadyState().get(reward5).doubleValue();
        double resultPN6 = rewardPN.getSteadyState().get(reward6).doubleValue();
        end = new Date();
        System.out.println("Time Regenerative SS  analysis:" + (end.getTime() - start.getTime()) + "ms");
        
        //Compare results
        System.out.println("Hierarchical SMP analysis result for state 1: " + resultHSMP1);
        System.out.println("Regenerative SS  analysis result for state 1: " + resultPN1);
        System.out.println("Hierarchical SMP analysis result for state 2: " + resultHSMP2);
        System.out.println("Regenerative SS  analysis result for state 2: " + resultPN2);
        System.out.println("Hierarchical SMP analysis result for state 3: " + resultHSMP3);
        System.out.println("Regenerative SS  analysis result for state 3: " + resultPN3);
        System.out.println("Hierarchical SMP analysis result for state 4: " + resultHSMP4);
        System.out.println("Regenerative SS  analysis result for state 4: " + resultPN4);
        System.out.println("Hierarchical SMP analysis result for state 5: " + resultHSMP5);
        System.out.println("Regenerative SS  analysis result for state 5: " + resultPN5);
        System.out.println("Hierarchical SMP analysis result for state 6: " + resultHSMP6);
        System.out.println("Regenerative SS  analysis result for state 6: " + resultPN6);
        
        assertEquals(resultHSMP1, resultPN1, DELTA);
        assertEquals(resultHSMP2, resultPN2, DELTA);
        assertEquals(resultHSMP3, resultPN3, DELTA);
        assertEquals(resultHSMP4, resultPN4, DELTA);
        assertEquals(resultHSMP5, resultPN5, DELTA);
        assertEquals(resultHSMP6, resultPN6, DELTA);

    }

}

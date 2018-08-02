package it.unifi.hierarchical.analysis;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import org.junit.Test;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.expression.Variable;
import org.oristool.math.function.EXP;
import org.oristool.math.function.Function;
import org.oristool.math.function.GEN;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.SteadyStateSolution;
import org.oristool.models.stpn.steady.RegSteadyState;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.PetriNet;

import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.example.hsmp.HSMP_FaultTreeWithMaintenance;
import it.unifi.hierarchical.model.example.pn.PN_FaultTreeWithMaintenance;

public class TestFaultTreeSensitiveAnalysis {
    
    //Time unit is days
    private static final double TIME_STEP = 0.025;
    private static final double DELTA = 0.001;
    
    private static final Function aFail = new EXP(Variable.X, new BigDecimal("" + (1.0/180.0)));
    private static final Function bFail = new EXP(Variable.X, new BigDecimal("" + (1.0/240.0)));
    private static final Function cFail = new EXP(Variable.X, new BigDecimal("" + (1.0/180.0)));
    private static final Function dFail = new EXP(Variable.X, new BigDecimal("" + (1.0/360.0)));

    private static final Function repair = GEN.newUniform(new OmegaBigDecimal("1"), new OmegaBigDecimal("3"));
    private static final Function preventiveMaintenance = GEN.newUniform(new OmegaBigDecimal("0"), new OmegaBigDecimal("1"));
    
    @Test
    public void testFaultTreeSensitiveAnalysis() {
        
        for(int i = 15; i<= 360; i= i + 15) {
            System.out.println("Testing period: "+ i);
            testConfiguration(i);
            System.out.println("\n\n");
        }
       
    }
    
    private void testConfiguration(int integerMaintenancePeriod) {
 
        int timeLimit = 720;//This will cause a small truncation approximation
        Function maintenancePeriod = GEN.newDeterministic( new BigDecimal("" + integerMaintenancePeriod));
        //HSMP
        //Build the model
        HierarchicalSMP model = HSMP_FaultTreeWithMaintenance.build(aFail, bFail, cFail, dFail, maintenancePeriod, repair, preventiveMaintenance);
        
        //Analyze
        Date start = new Date();
        HierarchicalSMPAnalysis analysis = new HierarchicalSMPAnalysis(model);
        Map<String, Double> ssHSMP = analysis.evaluateSteadyState(TIME_STEP, timeLimit);
        Double resultHSMP1 = ssHSMP.get("PreventiveMaintenance");
        Double resultHSMP2 = ssHSMP.get("Repair");
        Double resultHSMP3 = ssHSMP.get("A");
        Double resultHSMP4 = ssHSMP.get("B");
        Double resultHSMP5 = ssHSMP.get("C");
        Double resultHSMP6 = ssHSMP.get("D");
        Double resultHSMP7 = ssHSMP.get("AND1");
        Double resultHSMP8 = ssHSMP.get("AND2");
        Double resultHSMP9 = ssHSMP.get("OR");
        Double resultHSMP10 = ssHSMP.get("WaitingMaintenance");
        Date end = new Date();
        System.out.println("Time Hierarchical SMP analysis:" + (end.getTime() - start.getTime()) + "ms");
        
        //PN
        PetriNet net = new PetriNet();
        Marking m = new Marking();
        PN_FaultTreeWithMaintenance.build(net, m, aFail, bFail, cFail, dFail, maintenancePeriod, repair, preventiveMaintenance);
        
        start = new Date();
        RegSteadyState analysisPN = RegSteadyState.builder().build();
        SteadyStateSolution<Marking> ssPN = analysisPN.compute(net, m);
        RewardRate reward1 = RewardRate.fromString("PreventiveMaintenance");
        RewardRate reward2 = RewardRate.fromString("Repair");
        RewardRate reward3 = RewardRate.fromString("A");
        RewardRate reward4 = RewardRate.fromString("B");
        RewardRate reward5 = RewardRate.fromString("C");
        RewardRate reward6 = RewardRate.fromString("D");
        RewardRate reward7 = RewardRate.fromString("If(A+B>0,1,0)");
        RewardRate reward8 = RewardRate.fromString("If(C+D>0,1,0)");
        RewardRate reward9 = RewardRate.fromString("WaitingMaintenance");
        RewardRate reward10 = RewardRate.fromString("WaitingMaintenance");
        SteadyStateSolution<RewardRate> rewardPN = SteadyStateSolution.computeRewards(ssPN, reward1, reward2, reward3, reward4, reward5, reward6, reward7, reward8, reward9, reward10);
        double resultPN1 = rewardPN.getSteadyState().get(reward1).doubleValue();
        double resultPN2 = rewardPN.getSteadyState().get(reward2).doubleValue();
        double resultPN3 = rewardPN.getSteadyState().get(reward3).doubleValue();
        double resultPN4 = rewardPN.getSteadyState().get(reward4).doubleValue();
        double resultPN5 = rewardPN.getSteadyState().get(reward5).doubleValue();
        double resultPN6 = rewardPN.getSteadyState().get(reward6).doubleValue();
        double resultPN7 = rewardPN.getSteadyState().get(reward7).doubleValue();
        double resultPN8 = rewardPN.getSteadyState().get(reward8).doubleValue();
        double resultPN9 = rewardPN.getSteadyState().get(reward9).doubleValue();
        double resultPN10 = rewardPN.getSteadyState().get(reward10).doubleValue();
        end = new Date();
        System.out.println("Time Regenerative SS  analysis:" + (end.getTime() - start.getTime()) + "ms");
        
        //Compare results
//        System.out.println("Hierarchical SMP analysis result for state 1: " + resultHSMP1);
//        System.out.println("Regenerative SS  analysis result for state 1: " + resultPN1);
//        System.out.println("Hierarchical SMP analysis result for state 2: " + resultHSMP2);
//        System.out.println("Regenerative SS  analysis result for state 2: " + resultPN2);
//        System.out.println("Hierarchical SMP analysis result for state 3: " + resultHSMP3);
//        System.out.println("Regenerative SS  analysis result for state 3: " + resultPN3);
//        System.out.println("Hierarchical SMP analysis result for state 4: " + resultHSMP4);
//        System.out.println("Regenerative SS  analysis result for state 4: " + resultPN4);
//        System.out.println("Hierarchical SMP analysis result for state 5: " + resultHSMP5);
//        System.out.println("Regenerative SS  analysis result for state 5: " + resultPN5);
//        System.out.println("Hierarchical SMP analysis result for state 6: " + resultHSMP6);
//        System.out.println("Regenerative SS  analysis result for state 6: " + resultPN6);
//        System.out.println("Hierarchical SMP analysis result for state 7: " + resultHSMP7);
//        System.out.println("Regenerative SS  analysis result for state 7: " + resultPN7);
//        System.out.println("Hierarchical SMP analysis result for state 8: " + resultHSMP8);
//        System.out.println("Regenerative SS  analysis result for state 8: " + resultPN8);
//        System.out.println("Hierarchical SMP analysis result for state 9: " + resultHSMP9);
//        System.out.println("Regenerative SS  analysis result for state 9: " + resultPN9);
//        System.out.println("Hierarchical SMP analysis result for state 10: " + resultHSMP10);
//        System.out.println("Regenerative SS  analysis result for state 10: " + resultPN10);
        
        assertEquals(resultHSMP1, resultPN1, DELTA);
        assertEquals(resultHSMP2, resultPN2, DELTA);
        assertEquals(resultHSMP3, resultPN3, DELTA);
        assertEquals(resultHSMP4, resultPN4, DELTA);
        assertEquals(resultHSMP5, resultPN5, DELTA);
        assertEquals(resultHSMP6, resultPN6, DELTA);
        assertEquals(resultHSMP7, resultPN7, DELTA);
        assertEquals(resultHSMP8, resultPN8, DELTA);
        assertEquals(resultHSMP9, resultPN9, DELTA);
        assertEquals(resultHSMP10, resultPN10, DELTA);
        
        System.out.println("HSMP");
        System.out.println("Unusefull unavailability: " + resultHSMP1);
        System.out.println("Repair unavailability: " + resultHSMP2);
        System.out.println("Regen");
        System.out.println("Unusefull unavailability: " + resultPN1);
        System.out.println("Repair unavailability: " + resultPN2);
        
    }

}

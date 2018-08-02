package it.unifi.hierarchical.history;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.function.Function;
import org.oristool.math.function.GEN;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trans.RegTransient;
import org.oristool.models.stpn.trees.DeterministicEnablingState;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.PetriNet;

import it.unifi.hierarchical.analysis.NumericalValues;
import it.unifi.hierarchical.analysis.SMPAnalyzer;
import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.SimpleState;
import it.unifi.hierarchical.model.State;
import it.unifi.hierarchical.utils.NumericalUtils;
import it.unifi.hierarchical.utils.StateUtils;

public class TestHistoryState {

    private static final double DELTA = 0.01;
    private static final BigDecimal TIME_LIMIT = new BigDecimal("6");
    private static final BigDecimal TIME_STEP = new BigDecimal("0.002");
    private static final BigDecimal TIME_STEP_SIRIO = new BigDecimal("0.01");
    
    private static final String select_exit = "E3";
  
    private static final int select_firstRegionIndex = 0;
    private static final int select_secondRegionIndex = 1;
    private static final int select_exitRegionIndex = 2;

    @Test
    public void test(){
        for(int t11 = 1; t11 <= 3; t11++) {
            for(int t12 = 1; t12 <= 3; t12++) {
                for(int t21 = 1; t21 <= 3; t21++) {
                    for(int t22 = 1; t22 <= 3; t22++) {
                        for(int t31 = 1; t31 <= 3; t31++) {
                            for(int t32 = 1; t32 <= 3; t32++) {
                                System.out.println("Testing case " + t11 + ", "+ t12 + ", "+ t21 + ", "+ t22 + ", "+ t31 + ", "+ t32);
                                Function dens_T1_1 = GEN.newUniform(new OmegaBigDecimal("0"), new OmegaBigDecimal("" + t11));
                                Function dens_T1_2 = GEN.newUniform(new OmegaBigDecimal("0"), new OmegaBigDecimal("" + t12));
                                Function dens_T2_1 = GEN.newUniform(new OmegaBigDecimal("0"), new OmegaBigDecimal("" + t21));
                                Function dens_T2_2 = GEN.newUniform(new OmegaBigDecimal("0"), new OmegaBigDecimal("" + t22));
                                Function dens_T3_1 = GEN.newUniform(new OmegaBigDecimal("0"), new OmegaBigDecimal("" + t31));
                                Function dens_T3_2 = GEN.newUniform(new OmegaBigDecimal("0"), new OmegaBigDecimal("" + t32));
                                System.out.println("Testing <R1_1, R2_1>");
                                testCase(dens_T1_1, dens_T1_2, dens_T2_1, dens_T2_2, dens_T3_1, dens_T3_2, "R1_1", "R2_1");
                                System.out.println("Testing <R1_2, R2_1>");
                                testCase(dens_T1_1, dens_T1_2, dens_T2_1, dens_T2_2, dens_T3_1, dens_T3_2, "R1_2", "R2_1");
                                System.out.println("Testing <R1_1, R2_2>");
                                testCase(dens_T1_1, dens_T1_2, dens_T2_1, dens_T2_2, dens_T3_1, dens_T3_2, "R1_1", "R2_2");
                                System.out.println("Testing <R1_2, R2_2>");
                                testCase(dens_T1_1, dens_T1_2, dens_T2_1, dens_T2_2, dens_T3_1, dens_T3_2, "R1_2", "R2_2");
                                System.out.println("Case completed");
                            }
                        }
                    }
                }
            }
        }
    }
    
    private static void testCase(Function dens_T1_1, Function dens_T1_2, Function dens_T2_1, Function dens_T2_2, Function dens_T3_1, Function dens_T3_2, String select_firstNoExitRegion, String select_secondNoExitRegion) {
        //1- Build a PN representing 3 regions with 2 states each
        PetriNet pn = new PetriNet();
        Marking m = new Marking();
        HistoryPNModel.build(pn, m, dens_T1_1, dens_T1_2, dens_T2_1, dens_T2_2, dens_T3_1, dens_T3_2);
        
        //2- Evaluate probability to have a particular marking when third exit is reached using standard transient analysis
        RegTransient analysis = RegTransient.builder()
                .timeBound(TIME_LIMIT)
                .timeStep(TIME_STEP_SIRIO)
                .build();
        
        TransientSolution<DeterministicEnablingState, Marking> solution = analysis.compute(pn, m);
        
        RewardRate reward1 = RewardRate.fromString("If("+select_firstNoExitRegion+"+"+select_secondNoExitRegion+"+"+select_exit+">2,1,0)");
        TransientSolution<DeterministicEnablingState, RewardRate> result = TransientSolution.computeRewards(false, solution, reward1);
        double expectedFinalProb = result.getSolution()
                [result.getSolution().length -1]
                [result.getRegenerations().indexOf(solution.getInitialRegeneration())]
                [result.getColumnStates().indexOf(reward1)];
        System.out.println("Expected probability configuration: " + expectedFinalProb);
        
        //3- Evaluate probability to have a particular marking when third exit is reached using the new derived formula
        //3.1- Evaluate transient probability and time to exit of first two regions
        HierarchicalSMP hsmpModel = HistoryHSMPModel.build(dens_T1_1, dens_T1_2, dens_T2_1, dens_T2_2, dens_T3_1, dens_T3_2);
        
        //First region
        Region r1 = ((CompositeState)hsmpModel.getInitialState()).getRegions().get(select_firstRegionIndex);
        List<State> states = StateUtils.getReachableStates(r1.getInitialState());
        Map<State, NumericalValues> sojournTimes = new HashMap<>();
        for (State state : states) {
            if(!(state instanceof SimpleState))
               continue;
            double[] eval = NumericalUtils.evaluateFunction(((SimpleState) state).getDensity(), new OmegaBigDecimal(TIME_LIMIT), TIME_STEP);
            double[] values = NumericalUtils.computeCDFFromPDF(eval, TIME_STEP);
            sojournTimes.put(state, new NumericalValues(values, TIME_STEP.doubleValue()));
        }
        SMPAnalyzer analyzer = new SMPAnalyzer(states, sojournTimes, TIME_LIMIT.doubleValue(), TIME_STEP.doubleValue());
        
        NumericalValues transientProbs1 = analyzer.getProbsFromTo(r1.getInitialState(), StateUtils.searchStateByName(states, select_firstNoExitRegion)); 
        
        //Second region
        Region r2 = ((CompositeState)hsmpModel.getInitialState()).getRegions().get(select_secondRegionIndex);
        states = StateUtils.getReachableStates(r2.getInitialState());
        sojournTimes = new HashMap<>();
        for (State state : states) {
            if(!(state instanceof SimpleState))
               continue;
            double[] eval = NumericalUtils.evaluateFunction(((SimpleState) state).getDensity(), new OmegaBigDecimal(TIME_LIMIT), TIME_STEP);
            double[] values = NumericalUtils.computeCDFFromPDF(eval, TIME_STEP);
            sojournTimes.put(state, new NumericalValues(values, TIME_STEP.doubleValue()));
        }
        analyzer = new SMPAnalyzer(states, sojournTimes, TIME_LIMIT.doubleValue(), TIME_STEP.doubleValue());
        
        NumericalValues transientProbs2 = analyzer.getProbsFromTo(r2.getInitialState(), StateUtils.searchStateByName(states, select_secondNoExitRegion));
        
        //3.2- Evaluate exit distribution of last region
        Region r3 = ((CompositeState)hsmpModel.getInitialState()).getRegions().get(select_exitRegionIndex);
        states = StateUtils.getReachableStates(r3.getInitialState());
        sojournTimes = new HashMap<>();
        for (State state : states) {
            if(!(state instanceof SimpleState))
               continue;
            double[] eval = NumericalUtils.evaluateFunction(((SimpleState) state).getDensity(), new OmegaBigDecimal(TIME_LIMIT), TIME_STEP);
            double[] values = NumericalUtils.computeCDFFromPDF(eval, TIME_STEP);
            sojournTimes.put(state, new NumericalValues(values, TIME_STEP.doubleValue()));
        }
        analyzer = new SMPAnalyzer(states, sojournTimes, TIME_LIMIT.doubleValue(), TIME_STEP.doubleValue());
        NumericalValues exitProbs3 = analyzer.getProbsFromTo(r3.getInitialState(), StateUtils.findEndState(r3));
       
        //3.4- Evaluate probability of specific configuration
        double[] dExit = NumericalUtils.computePDFFromCDF(exitProbs3.getValues(), TIME_STEP);//Density of exit

        double evaluatedFinalProb = 0;
        int nt = NumericalUtils.computeStepNumber(new OmegaBigDecimal(TIME_LIMIT), TIME_STEP);
        for(int t = 0; t < nt; t++) {
            evaluatedFinalProb+= transientProbs1.getValues()[t] * transientProbs2.getValues()[t] * dExit[t] * TIME_STEP.doubleValue(); 
        }
        
        System.out.println("Evaluated probability configuration: " + evaluatedFinalProb);
        
        //4- Compare evaluated measures        
        assertEquals(expectedFinalProb, evaluatedFinalProb, DELTA);
    } 
     
}

package it.unifi.hierarchical.analysis;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.function.Function;
import org.oristool.math.function.GEN;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.SteadyStateSolution;
import org.oristool.models.stpn.steady.RegSteadyState;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.PetriNet;

import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.example.hsmp.HSMP_ComplexityParallelRegions;
import it.unifi.hierarchical.model.example.pn.PN_ComplexityParallelRegions;


/**
 * Note: some problems are present with the thread executor. To have reliable measures is better
 * to launch each case separately...
 *
 */
public class TestComplexity {

    private enum TEST_TYPE{
        HSMP,
        REGEN
    };
    
    private static final int TIMEOUT_ANALYSIS_S = 120;//2 minutes
    private static final double TIME_STEP = 0.01;
    
    private static final Function distro = GEN.newUniform(new OmegaBigDecimal("0"), new OmegaBigDecimal("1"));
    
    private static final TEST_TYPE testType = TEST_TYPE.REGEN;
    
    @Test
    public void testComplexity() throws InterruptedException{
        ExecutorService executor = Executors.newSingleThreadExecutor();
        for(int nRegions = 4; nRegions <= 4; nRegions++) {
            for(int nStates = 3; nStates <= 3; nStates++) {
                System.out.println("Case with " + nRegions + " regions and " + nStates + " states");
                if(testType.equals(TEST_TYPE.HSMP)) {
                    HSMPAnalysisThread taskH = new HSMPAnalysisThread(nRegions, nStates);
                    Future<Long> future = executor.submit(taskH);
                    try {
                       Long result = future.get(TIMEOUT_ANALYSIS_S, TimeUnit.SECONDS);
                       System.out.println("HSMP analysis:" + (result/1000000000) + "s");
                    } catch (TimeoutException ex) {
                       // handle the timeout
                        System.out.println("Timeout");
                    } catch (InterruptedException e) {
                       // handle the interrupts
                        System.out.println("Interrupt");
                    } catch (ExecutionException e) {
                       // handle other exceptions
                        System.out.println("Exec exception");
                    } finally {
                       future.cancel(true);
                    }
                }else if(testType.equals(TEST_TYPE.REGEN)) {
                    SirioAnalysisThread taskS = new SirioAnalysisThread(nRegions, nStates);
                    Future<Long> future = executor.submit(taskS);
                    try {
                        Long result = future.get(TIMEOUT_ANALYSIS_S, TimeUnit.SECONDS);
                       System.out.println("Regen analysis:" + (((Long)result)/1000000000) + "s");
                    } catch (TimeoutException ex) {
                       // handle the timeout
                        System.out.println("Timeout");
                    } catch (InterruptedException e) {
                       // handle the interrupts
                        System.out.println("Interrupt");
                    } catch (ExecutionException e) {
                       // handle other exceptions
                        System.out.println("Exec exception");
                    } finally {
                       future.cancel(true);
                    }
                }
            }
        }
        
    }
    
    
    private class HSMPAnalysisThread implements Callable<Long>{
        
        private int nRegions;
        private int nStates;
        
        public HSMPAnalysisThread(int nRegions, int nStates) {
            this.nRegions = nRegions;
            this.nStates = nStates;
        }

        @Override
        public Long call() throws Exception {
            long start = System.nanoTime();
            //Time limit is chosen according to the model, based on which is the maximum time elapsed in a region or a state
            double timeLimit = nStates;
            //HSMP
            //Build the model
            HierarchicalSMP model = HSMP_ComplexityParallelRegions.build(nRegions, nStates, distro);
            
            //Analyze
            HierarchicalSMPAnalysis analysis = new HierarchicalSMPAnalysis(model);
            analysis.evaluateSteadyState(TIME_STEP, timeLimit);
            return (System.nanoTime() - start);
        }

    }
    
    private class SirioAnalysisThread implements Callable<Long>{
        
        private int nRegions;
        private int nStates;
        
        public SirioAnalysisThread(int nRegions, int nStates) {
            this.nRegions = nRegions;
            this.nStates = nStates;
        }
        
        @Override
        public Long call() throws Exception {
            long start = System.nanoTime();
            //PN
            PetriNet net = new PetriNet();
            Marking m = new Marking();
            PN_ComplexityParallelRegions.build(net, m, nRegions, nStates, distro);
            
            RegSteadyState analysisPN = RegSteadyState.builder().build();
            SteadyStateSolution<Marking> ssPN = analysisPN.compute(net, m);
            RewardRate reward1 = RewardRate.fromString("p3");
            SteadyStateSolution<RewardRate> rewardPN = SteadyStateSolution.computeRewards(ssPN, reward1);
            rewardPN.getSteadyState().get(reward1).doubleValue();
            return (System.nanoTime() - start);
        }

    }
   
    @Test
    public void testSingleExecution() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        HSMPAnalysisThread taskH = new HSMPAnalysisThread(3, 6);
        Future<Long> future = executor.submit(taskH);
        try {
           Long result = future.get(TIMEOUT_ANALYSIS_S, TimeUnit.SECONDS);
           System.out.println("HSMP analysis:" + (result/1000000) + "ms");
        } catch (TimeoutException ex) {
           // handle the timeout
            System.out.println("Timeout");
        } catch (InterruptedException e) {
           // handle the interrupts
            System.out.println("Interrupt");
        } catch (ExecutionException e) {
           // handle other exceptions
            System.out.println("Exec exception");
        } finally {
           future.cancel(true); // may or may not desire this
        }
    }
    
}

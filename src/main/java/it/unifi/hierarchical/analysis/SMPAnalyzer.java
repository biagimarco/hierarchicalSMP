package it.unifi.hierarchical.analysis;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.oristool.math.OmegaBigDecimal;

import it.unifi.hierarchical.model.State;
import it.unifi.hierarchical.utils.NumericalUtils;

public class SMPAnalyzer implements TransientAnalyzer{

    private List<State> states;
    private double[][][] probs;
    private double timeLimit;
    private double timeStep;
    private State absorbingState;
    
    
    public SMPAnalyzer(List<State> states, Map<State, NumericalValues> sojournTimeDistributions, double timeLimit, double timeStep) {
        this(states, sojournTimeDistributions, timeLimit, timeStep, null);
    }
    
    public SMPAnalyzer(List<State> states, Map<State, NumericalValues> sojournTimeDistributions, double timeLimit, double timeStep, State absorbingState) {
        this.states = states;
        this.timeLimit = timeLimit;
        this.timeStep = timeStep;
        this.absorbingState = absorbingState;
        int ticks = NumericalUtils.computeStepNumber(new OmegaBigDecimal(""+timeLimit), new BigDecimal(""+timeStep));

        //Evaluate kernel
        double[][][] kernel = new double[states.size()][states.size()][ticks];
        
        for(int i = 0; i < states.size(); i++) {//From
            State from = states.get(i);
            NumericalValues sojourn = sojournTimeDistributions.get(from);
            double[] sojournDistrib;
            if(sojourn == null || from.equals(absorbingState)) { //absorbing state
                sojournDistrib = new double[ticks];
                continue;
            }
            else
                sojournDistrib = sojourn.getValues();
            
            for(int q=0; q < from.getNextStates().size(); q++) {//To
                State to = from.getNextStates().get(q);
                int j = states.indexOf(to);
                double p_ij = from.getBranchingProbs().get(q);
                
                for(int t = 0; t < ticks; t++) {
                    kernel[i][j][t] = p_ij * sojournDistrib[t];
                }
            }
        }

        //Init transient probs array
        probs = new double[states.size()][states.size()][ticks];
        for (int i = 0; i < probs.length; i++) {
            probs[i][i][0] = 1;
        }
       
        //Evaluate transient probabilities of the SMP
        for(int t = 1; t < ticks; t++) {
            for(int i = 0; i < states.size(); i++) {//From
                State from = states.get(i);
                NumericalValues sojourn = sojournTimeDistributions.get(from);
                double[] sojournDistrib;
                if(sojourn == null || from.equals(absorbingState)) //absorbing state
                    sojournDistrib = new double[ticks];
                else
                    sojournDistrib = sojourn.getValues();                
                for(int j = 0; j < states.size(); j++) {//To
                    if(j==i) 
                        probs[i][i][t]+= (1 - sojournDistrib[t]);
                    for(int k = 0; k < states.size(); k++) {
                        for(int u = 1; u < t; u++) {//Integral
                            probs[i][j][t]+=  
                                    (kernel[i][k][u] - kernel[i][k][u - 1])* 
                                    probs[k][j][t - u];    
                        }
                    }
                }
            }
        }        
    }

    @Override
    public List<State> getStates() {
        return states;
    }

    public double[][][] getTransientProbabilities() {
        return probs;
    }

    @Override
    public double getTimeLimit() {
        return timeLimit;
    }

    @Override
    public double getTimeStep() {
        return timeStep;
    }
    
    public State getAbsorbingState() {
        return absorbingState;
    }

    @Override
    public NumericalValues getProbsFromTo(State from, State to) {
        int fromStateIndex = states.indexOf(from);
        int toStateIndex = states.indexOf(to);
        double[] result = probs[fromStateIndex][toStateIndex];
        return new NumericalValues(result, timeStep);
    }

}

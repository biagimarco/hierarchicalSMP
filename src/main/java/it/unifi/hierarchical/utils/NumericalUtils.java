package it.unifi.hierarchical.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.expression.Variable;
import org.oristool.math.function.EXP;
import org.oristool.math.function.Function;
import org.oristool.math.function.GEN;
import org.oristool.math.function.PartitionedGEN;

import it.unifi.hierarchical.analysis.NumericalValues;

public class NumericalUtils {

    public static double[] convolveCDFs(double[] first, double[] second, double step) {
        double[] firstPDF = computePDFFromCDF(first, new BigDecimal("" + step));
        double[] secondPDF = computePDFFromCDF(second, new BigDecimal("" + step));
        double[] resultPDF = convolvePDFs(firstPDF, secondPDF, step);
        return computeCDFFromPDF(resultPDF, new BigDecimal("" + step));
    }
    
    public static double[] convolvePDFs(double[] first, double[] second, double step) {

        if (first == null || second == null)
            throw new IllegalArgumentException("Convolution parameter can't be null");

        final int firstLen = first.length;
        final int secondLen = second.length;

        if (firstLen == 0 || secondLen == 0) {
            throw new IllegalArgumentException("Convolution parameter can't be empty");
        }
        if (firstLen != secondLen) {
            throw new IllegalArgumentException("Convolution parameter must have the same length");
        }

        int resultLength = firstLen;

        double[] result = new double[resultLength];
        // Evaluate every element of the final result
        for (int i = 0; i < resultLength; i++) {
            result[i] = 0;
            // For every element of the second array. If j>i is not evaluated because it will have a negative index
            for (int j = 0; j <= i; j++) {
                result[i] += first[j] * step * second[i - j];
            }
        }
        return result;
    }
    
    public static NumericalValues maxCDF(Collection<NumericalValues> distributions) {
        
        double[] resultCDF = new double[distributions.iterator().next().getValues().length];
        List<NumericalValues> distributionsList = new ArrayList<>(distributions);
        
        for (int i = 0; i < resultCDF.length; i++) {
            resultCDF[i] = distributionsList.get(0).getValues()[i];
            for(int d = 1; d < distributionsList.size(); d++) {
                resultCDF[i] *= distributionsList.get(d).getValues()[i];
            }
        }
        return new NumericalValues(resultCDF, distributions.iterator().next().getStep());
    }
    
    public static NumericalValues minPDF(Collection<NumericalValues> pdfs) {
        
        List<NumericalValues> distributions = new ArrayList<>();
        
        for (NumericalValues pdf : pdfs) {
            BigDecimal stepBD = new BigDecimal(""+pdf.getStep());
            distributions.add(new NumericalValues(computeCDFFromPDF(pdf.getValues(), stepBD), pdf.getStep()));
        }
        
        NumericalValues minCDF = minCDF(distributions);
        BigDecimal stepBD = new BigDecimal(""+minCDF.getStep());
        
        return new NumericalValues(computePDFFromCDF(minCDF.getValues(),stepBD), minCDF.getStep());
    }
    
    public static NumericalValues minCDF(Collection<NumericalValues> distributions) {
        
        double[] resultCDF = new double[distributions.iterator().next().getValues().length];
        List<NumericalValues> distributionsList = new ArrayList<>(distributions);
        
        for (int i = 0; i < resultCDF.length; i++) {
            resultCDF[i] = 1 - distributionsList.get(0).getValues()[i];
            for(int d = 1; d < distributionsList.size(); d++) {
                resultCDF[i] *= 1 - distributionsList.get(d).getValues()[i];
            }
            
            resultCDF[i] = 1 - resultCDF[i]; 
        }
        
        return new NumericalValues(resultCDF, distributions.iterator().next().getStep());
    }

    public static double[] maxCDF(double[] firstCDF, double[] secondCDF, BigDecimal step) {
        final int firstLen = firstCDF.length;
        final int secondLen = secondCDF.length;
        if (firstLen != secondLen) {
            throw new IllegalArgumentException("Distributions values vectors must have the same length");
        }

        double[] resultCDF = new double[firstCDF.length];
        for (int i = 0; i < resultCDF.length; i++) {
            resultCDF[i] = firstCDF[i] * secondCDF[i];
        }

        return resultCDF;
    }
    
    public static double[] maxPDF(double[] firstPDF, double[] secondPDF, BigDecimal step) {

        final int firstLen = firstPDF.length;
        final int secondLen = secondPDF.length;
        if (firstLen != secondLen) {
            throw new IllegalArgumentException("Distributions values vectors must have the same length");
        }

        double[] firstCDF = NumericalUtils.computeCDFFromPDF(firstPDF, step);
        double[] secondCDF = NumericalUtils.computeCDFFromPDF(secondPDF, step);

        double[] resultCDF = new double[firstCDF.length];
        for (int i = 0; i < resultCDF.length; i++) {
            resultCDF[i] = firstCDF[i] * secondCDF[i];
        }

        double[] result = NumericalUtils.computePDFFromCDF(resultCDF, step);

        return result;
    }


    public static double[] normalizePDF(double[] original, int outOfBoundSamplesCounter, BigDecimal step) {
        double[] normalized = new double[original.length];
        double sum = (double) outOfBoundSamplesCounter;
        for (int i = 0; i < original.length; i++) {
            sum += original[i];
        }
        for (int i = 0; i < original.length; i++) {
            normalized[i] = (original[i] / sum) / step.doubleValue();
        }

        return normalized;
    }

    public static double[] computeCDFFromPDF(double[] pdf, BigDecimal step) {
        double[] cdf = new double[pdf.length];
        for (int i = 0; i < pdf.length; i++) {
            double previousValue = i == 0 ? 0.0 : cdf[i - 1];
            double newValue = previousValue + pdf[i] * step.doubleValue();
            cdf[i] = newValue > 1 ? 1 : newValue; //Reduce possible errors 
        }

        return cdf;
    }

    public static double[] computePDFFromCDF(double[] cdf, BigDecimal step) {
        double[] pdf = new double[cdf.length];
        for (int i = 0; i < cdf.length; i++) {
            double previousValue = i == 0 ? 0.0 : cdf[i - 1];
            pdf[i] = (cdf[i] - previousValue) / step.doubleValue();
        }

        return pdf;
    }    
    
    public static int computeStepNumber(OmegaBigDecimal timeLimit, BigDecimal timeStep) {
        return timeLimit.divide(timeStep, MathContext.DECIMAL128).intValue() + 1;
    }
    
    public static double[] evaluateFunction(Function density, OmegaBigDecimal timeLimit, BigDecimal timeStep) {
        if(density instanceof EXP)
            return evaluateEXP((EXP) density, timeLimit, timeStep);
        else if(density instanceof GEN)
            return evaluateGEN((GEN) density, timeLimit, timeStep);
        else if(density instanceof PartitionedGEN)
            return evaluatePartitionedGEN((PartitionedGEN)density, timeLimit, timeStep);
        else
            throw new IllegalArgumentException("Unsupported type!");
    }
    
    
    private static double[] evaluateGEN(GEN density, OmegaBigDecimal timeLimit, BigDecimal timeStep) {
        int stepsNumber = computeStepNumber(timeLimit, timeStep);
        double[] values = new double[stepsNumber];
        
        for (int t = 0; t < stepsNumber; t++) {
            double time = t * timeStep.doubleValue();
            Map<Variable, OmegaBigDecimal> timePoint = new HashMap<Variable, OmegaBigDecimal>();
            timePoint.put(Variable.X, new OmegaBigDecimal("" + time));
            
            if (isDeterministic(density)) {
                // Special case: IMMEDIATE or DETERMINISTIC
                if (density.getDomain().contains(timePoint))
                    values[t] = BigDecimal.ONE.divide(timeStep, MathContext.DECIMAL128).doubleValue();
                else
                    values[t] = 0;
                continue;
            }
            
            if (density.getDomain().contains(timePoint)) {
                values[t] = density.getDensity().evaluate(timePoint).doubleValue();
            }
        }
        
        return values;
    }

    private static double[] evaluateEXP(EXP density, OmegaBigDecimal timeLimit, BigDecimal timeStep) {
        int stepsNumber = computeStepNumber(timeLimit, timeStep);
        double[] values = new double[stepsNumber];
        
        for (int t = 0; t < stepsNumber; t++) {
            double time = t * timeStep.doubleValue();
            Map<Variable, OmegaBigDecimal> timePoint = new HashMap<Variable, OmegaBigDecimal>();
            timePoint.put(Variable.X, new OmegaBigDecimal("" + time));
            
            if (density.getDomain().contains(timePoint)) {
                values[t] = density.getDensity().evaluate(timePoint).doubleValue();
            }
        }
        
        return values;
    }

    public static double[] evaluatePartitionedGEN(PartitionedGEN density, OmegaBigDecimal timeLimit, BigDecimal timeStep) {
        List<GEN> functions = density.getFunctions();

        int stepsNumber = computeStepNumber(timeLimit, timeStep);
        double[] values = new double[stepsNumber];

        for (int t = 0; t < stepsNumber; t++) {
            double time = t * timeStep.doubleValue();
            Map<Variable, OmegaBigDecimal> timePoint = new HashMap<Variable, OmegaBigDecimal>();
            timePoint.put(Variable.X, new OmegaBigDecimal("" + time));

            if (isDeterministic(density)) {
                // Special case: IMMEDIATE or DETERMINISTIC
                GEN function = functions.get(0);
                if (function.getDomain().contains(timePoint))
                    values[t] = BigDecimal.ONE.divide(timeStep, MathContext.DECIMAL128).doubleValue();
                else
                    values[t] = 0;
                continue;
            }

            for (GEN function : functions) {
                if (function.getDomain().contains(timePoint)) {
                    values[t] = function.getDensity().evaluate(timePoint).doubleValue();
                }
            }
        }

        return values;
    }
    
    public static boolean isDeterministic(PartitionedGEN pGen) {
        if (pGen.getFunctions().size() != 1)
            return false;
        GEN gen = pGen.getFunctions().get(0);

        if (gen.getDomain().getCoefficient(Variable.X, Variable.TSTAR)
                .compareTo(gen.getDomain().getCoefficient(Variable.TSTAR, Variable.X).negate()) == 0)
            return true;
        return false;
    }
    
    public static boolean isDeterministic(GEN gen) {
        if (gen.getDomain().getCoefficient(Variable.X, Variable.TSTAR)
                .compareTo(gen.getDomain().getCoefficient(Variable.TSTAR, Variable.X).negate()) == 0)
            return true;
        return false;
    }
    
    //Not generic version--> easier than the version with an unspecified version of dimension
    public static NumericalValues shiftAndProjectAndMinimum(NumericalValues fired, List<NumericalValues> others) {
        if(others.size() == 1) {
            return shiftAndProjectAndMinimumS1(fired, others.get(0));
        }else {
            throw new UnsupportedOperationException("Not yet supported joint distribution of dimension " + others.size());
        }
            
    }
    
    private static NumericalValues shiftAndProjectAndMinimumS1(NumericalValues fired, NumericalValues first) {
        double[] firedPDF = computePDFFromCDF(fired.getValues(), new BigDecimal("" + fired.getStep()));
        double[] firstPDF = computePDFFromCDF(first.getValues(), new BigDecimal("" + first.getStep()));
        
        //Successor probability
        double p0 = 0;
        for (int t1 = 0; t1 < firstPDF.length; t1++) {
            for (int t2 = 0; t2 < t1; t2++) {
                p0+= firedPDF[t2] * firstPDF[t1] * fired.getStep() * first.getStep();
            }
        }
        
        //Shift and project
        double[] shiftedPDF = new double[firstPDF.length];
        
        for (int t1 = 0; t1 < firstPDF.length; t1++) {
            for (int t0 = 0; t0 < firedPDF.length; t0++) {
                if(t1- t0 >= 0)//Guarantee to be in the domain where fired distribution fire first
                    shiftedPDF[t1 - t0] += firedPDF[t0] * firstPDF[t1] * first.getStep();
            }
        }
        
        
        //Normalizing using p0
        for(int t=0; t < firstPDF.length; t++) {
            shiftedPDF[t] = shiftedPDF[t] / p0;
        }
        
        //Evaluate minimum
        //Do nothing here since its only a single r.v. distribution
        
        double[] resultCDF = computeCDFFromPDF(shiftedPDF, new BigDecimal("" + first.getStep()));
        
        return new NumericalValues(resultCDF, first.getStep());
    }
    
    /**
     * Given N independent distributions, evaluate the probability that each of that fire first
     */
    public static List<Double> evaluateFireFirstProbabilities(List<NumericalValues> distributions) {
        
        if(distributions.size() == 0)
            return null;
        else if(distributions.size() == 1)
            return Arrays.asList(1.0);
        else if(distributions.size() == 2)
            return evaluateFireFirstProbabilitiesS2(distributions.get(0), distributions.get(1));
        else if(distributions.size() == 3)
            return evaluateFireFirstProbabilitiesS3(distributions.get(0), distributions.get(1), distributions.get(2));
        else
            throw new UnsupportedOperationException("Evaluation of successor probabilities for N>3 dimensions not yet implemented");
    }

    private static List<Double> evaluateFireFirstProbabilitiesS2(NumericalValues aCDF, NumericalValues bCDF) {
        double[] aPDF = computePDFFromCDF(aCDF.getValues(), new BigDecimal("" + aCDF.getStep()));
        double[] bPDF = computePDFFromCDF(bCDF.getValues(), new BigDecimal("" + bCDF.getStep()));
        double p0 = 0;
        for (int t1 = 0; t1 < bPDF.length; t1++) {
            for (int t2 = 0; t2 < t1; t2++) {
                p0+= aPDF[t2] * bPDF[t1] * aCDF.getStep() * bCDF.getStep();
            }
        }
        
        return Arrays.asList(p0, 1-p0);
    }
    
    private static List<Double> evaluateFireFirstProbabilitiesS3(NumericalValues aCDF, NumericalValues bCDF, NumericalValues cCDF) {
        //Evaluate A first
        NumericalValues minBC = minCDF(Arrays.asList(bCDF, cCDF));
        double pa = evaluateFireFirstProbabilitiesS2(aCDF, minBC).get(0);
        
        NumericalValues minAC = minCDF(Arrays.asList(aCDF, cCDF));
        double pb = evaluateFireFirstProbabilitiesS2(bCDF, minAC).get(0);
        
        return Arrays.asList(pa, pb, 1 - pa - pb);
    }    

}

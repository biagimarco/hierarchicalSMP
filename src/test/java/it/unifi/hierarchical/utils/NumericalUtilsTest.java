package it.unifi.hierarchical.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.expression.Variable;
import org.oristool.math.function.GEN;
import org.oristool.math.function.PartitionedGEN;
import org.oristool.math.function.StateDensityFunction;
import org.oristool.models.stpn.trees.StochasticStateFeature;

import it.unifi.hierarchical.analysis.NumericalValues;

public class NumericalUtilsTest {

    @Test    
    public void shiftAndProjectAndMinimumTest1() {
        NumericalValues firedCDF = new NumericalValues(new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}, 0.2);//DET(1)
        NumericalValues otherCDF = new NumericalValues(new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.2, 0.4, 0.6, 0.8, 1.0}, 0.2);//UNI[1,2]
        
        NumericalValues result = NumericalUtils.shiftAndProjectAndMinimum(firedCDF, Arrays.asList(otherCDF));
        
        double[] expectedsResult = new double[] {0.0, 0.2, 0.4, 0.6, 0.8, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};//UNI[0,1]
        
        assertArrayEquals(expectedsResult, result.getValues(), 0.001);
    }
    
    @Test    
    public void shiftAndProjectAndMinimumTest2() {
        double step = 0.01;
        double timeLimit = 10;
        GEN a = GEN.newUniform(new OmegaBigDecimal("1"), new OmegaBigDecimal("6"));
        GEN b = GEN.newUniform(new OmegaBigDecimal("1"), new OmegaBigDecimal("2"));
        
        compareShiftAndProject(step, timeLimit, a, b);
        
    }
    
    @Test    
    public void shiftAndProjectAndMinimumTest3() {
        double step = 0.01;
        double timeLimit = 10;
        GEN a = GEN.newUniform(new OmegaBigDecimal("3"), new OmegaBigDecimal("6"));
        GEN b = GEN.newUniform(new OmegaBigDecimal("1"), new OmegaBigDecimal("2"));
        
        compareShiftAndProject(step, timeLimit, a, b);
    }
    
    private void compareShiftAndProject(double step, double timeLimit, GEN a, GEN b) {
        int ticks = NumericalUtils.computeStepNumber(new OmegaBigDecimal("" + timeLimit), new BigDecimal("" + step));
        double[] sirioResult = new double[ticks];
        double[] numericalResult = new double[ticks];
        //Sirio analytical evaluation
        StochasticStateFeature feature = new StochasticStateFeature(BigDecimal.ZERO, 0);
        StateDensityFunction density = feature.getStateDensity();
        density.addContinuousVariable(new Variable("a"), a);
        density.addContinuousVariable(new Variable("b"), b);
        
        density.conditionAllToBound(new Variable("b"), new HashSet<>(Arrays.asList(new Variable("a"))), OmegaBigDecimal.ZERO);
        density.shiftAndProject(new Variable("b"));
        
        PartitionedGEN sirioResultDistribution = density.getPartitionedGen();
        sirioResultDistribution.substitute(new Variable("a"), Variable.X);
        sirioResult = NumericalUtils.evaluatePartitionedGEN(sirioResultDistribution, new OmegaBigDecimal("" + timeLimit), new BigDecimal("" + step));
        sirioResult = NumericalUtils.computeCDFFromPDF(sirioResult, new BigDecimal("" + step));
        
        //Numerical evaluation        
        
        NumericalValues aNumerical = 
                new NumericalValues(
                        NumericalUtils.computeCDFFromPDF(
                                NumericalUtils.evaluateFunction(
                                        a, new OmegaBigDecimal("" + timeLimit), new BigDecimal("" + step)),
                                new BigDecimal("" + step)),
                        step);
        NumericalValues bNumerical = 
                new NumericalValues(
                        NumericalUtils.computeCDFFromPDF(
                                NumericalUtils.evaluateFunction(
                                        b, new OmegaBigDecimal("" + timeLimit), new BigDecimal("" + step)),
                                new BigDecimal("" + step)),
                        step);
         
        numericalResult = NumericalUtils.shiftAndProjectAndMinimum(bNumerical, Arrays.asList(aNumerical)).getValues();
        
        for (int t = 0; t < numericalResult.length; t++) {
            assertEquals(sirioResult[t], numericalResult[t], 0.01);
        }
    }
    
    /**
     * This test was created to check how much numerical errors affect the computation of the mean of a distributions
     */
    @Test
    public void testMeanEvaluation() {
        GEN distro = GEN.newUniform(new OmegaBigDecimal("0"), new OmegaBigDecimal("1"));
        double timeStep = 0.001;
        double[] values = NumericalUtils.evaluateFunction(distro, new OmegaBigDecimal(""+6), new BigDecimal(""+timeStep));
        
        values = NumericalUtils.computeCDFFromPDF(values, new BigDecimal(""+timeStep));
        
        double mean = 0;
        for (int t = 0; t < values.length; t++) {
            mean+= (1-values[t]) * timeStep;
        }
        assertEquals(0.5, mean, 0.001);
    }

    
}

package it.unifi.hierarchical.analysis;

import java.util.Arrays;

public class NumericalValues {

    private double[] values;
    private double step;
    
    public NumericalValues(double[] values, double step) {
        this.values = values;
        this.step = step;
    }

    public double[] getValues() {
        return values;
    }

    public double getStep() {
        return step;
    }

    @Override
    public String toString() {
        return "NumericalValues [values=" + Arrays.toString(values) + ", step=" + step + "]";
    }
 
}

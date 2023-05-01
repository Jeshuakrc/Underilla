package com.jkantrell.mc.underilla.core.vector;

public class DoubleVector extends Vector<Double> {

    //CONSTRUCTORS
    public DoubleVector(Double x, Double y, Double z) {
        super(x, y, z);
    }
    public DoubleVector(Vector<Double> original) {
        super(original);
    }


    //IMPLEMENTATIONS
    @Override
    public DoubleVector clone() {
        return new DoubleVector(this);
    }
    @Override
    protected Double add(Double a, Double b) {
        return a + b;
    }
}

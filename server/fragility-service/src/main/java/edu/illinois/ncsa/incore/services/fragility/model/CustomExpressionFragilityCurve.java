package edu.illinois.ncsa.incore.services.fragility.model;

public class CustomExpressionFragilityCurve extends FragilityCurve {
    public String expression;

    public CustomExpressionFragilityCurve() {
        super();
    }

    public CustomExpressionFragilityCurve(String expression, String label) {
        super(label);
        this.expression = expression;
    }
}


package edu.illinois.ncsa.incore.semantic.units.conversion.operations;

public abstract class ReversibleOperation extends Operation {
    public abstract double invokeInverse(Number value);
    public abstract ReversibleOperation getInverseOperation();
}

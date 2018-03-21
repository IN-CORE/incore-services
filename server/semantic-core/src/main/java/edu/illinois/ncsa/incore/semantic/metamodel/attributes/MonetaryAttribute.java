
package edu.illinois.ncsa.incore.semantic.metamodel.attributes;

import edu.illinois.ncsa.incore.semantic.metamodel.concepts.MonetaryConcept;

import javax.money.CurrencyUnit;

public class MonetaryAttribute<T extends Number> extends Attribute<T> {
    public MonetaryConcept conceptReference;
    public CurrencyUnit currency;

    @Override
    public MonetaryConcept getConceptReference() {
        return conceptReference;
    }
}


package edu.illinois.ncsa.incore.semantic.metamodel.concepts;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

public class MonetaryConcept extends ValueConcept<Number> {
    public List<Currency> commonCurrencies = new ArrayList<>();

    public List<Currency> getCommonCurrencies() {
        return commonCurrencies;
    }
}

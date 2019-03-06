/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

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

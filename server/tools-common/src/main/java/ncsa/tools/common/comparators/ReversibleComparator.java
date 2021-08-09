/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.comparators;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Base class providing sign value which can be used to reverse
 * the comparison values if set (to get descending sort order).
 *
 * @author Albert L. Rossi
 */
public abstract class ReversibleComparator implements Serializable, Comparator {
    private static final long serialVersionUID = 1030L;

    protected int sign = 1;

    /**
     * @param b if true, sign for reversing comparison values will be set.
     */
    public void setDescending(boolean b) {
        if (b)
            sign = -1;
        else
            sign = 1;
    }
}

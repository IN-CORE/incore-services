/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.comparators;

public class ToStringComparator extends ReversibleComparator {
    public int compare(Object arg0, Object arg1) {
        if (arg0 == null)
            return sign;
        if (arg1 == null)
            return -sign;
        String s0 = arg0.toString();
        String s1 = arg1.toString();
        return s0.compareTo(s1) * sign;
    }
}

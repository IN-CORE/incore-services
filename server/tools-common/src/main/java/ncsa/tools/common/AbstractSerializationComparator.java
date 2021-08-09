/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;

public abstract class AbstractSerializationComparator implements Comparator {
    public int compare(Object arg0, Object arg1) {
        if (arg0 == null)
            return 1;
        if (arg1 == null)
            return -1;

        return normalizeArgument(arg0).compareTo(normalizeArgument(arg1));
    }

    protected abstract String normalizeArgument(Object arg);

    protected String defaultNormalization(Object arg) throws RuntimeException {
        if (arg instanceof Map.Entry)
            arg = ((Map.Entry) arg).getKey();

        if ((arg instanceof Map || arg instanceof Collection))
            arg = ObjectUtils.identityToString(arg);

        else if (!(arg instanceof String))
            arg = ObjectUtils.identityToString(arg);

        return arg.toString();
    }
}

/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.types.filters;

import ncsa.tools.common.Filter;
import ncsa.tools.common.TypeFilterable;
import ncsa.tools.common.util.ComparisonUtils;

public class TypeFilter implements Filter {
    private String type = null;

    public void setType(String s) {
        type = s;
    }

    public String getType() {
        return type;
    }

    /**
     * @return true if object satisfies filter; false otherwise.
     */
    public boolean matches(Object o) {
        if (!(o instanceof TypeFilterable))
            return false;
        String t = ((TypeFilterable) o).getType();
        return ComparisonUtils.matches(this, t);
    }
}

/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common;

import ncsa.tools.common.exceptions.FailedComparisonException;

/**
 * A general filter interface.
 *
 * @author Albert L. Rossi
 */
public interface Filter {
    /**
     * @return true if object satisfies filter; false otherwise.
     * @throws FailedComparisonException
     */
    boolean matches(Object o) throws FailedComparisonException;

}

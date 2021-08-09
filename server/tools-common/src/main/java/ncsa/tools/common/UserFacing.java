/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common;

import org.dom4j.Element;

public interface UserFacing extends ElementConfigurable {
    public final static String ELEM_PROP = "property"; //$NON-NLS-1$
    public static final String ATTR_NAME = "name"; //$NON-NLS-1$
    public static final String ATTR_VALUE = "value"; //$NON-NLS-1$

    public Element asElement();
}

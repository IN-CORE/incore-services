/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.types.filters;

import ncsa.tools.common.Filter;
import ncsa.tools.common.util.ComparisonUtils;

public class ClassFilter implements Filter
{
	private Class clzz = null;

	public void setTypeFromString(String s) throws ClassNotFoundException
	{
		clzz = Class.forName(s);
	}

	public void setTypeFromClass(Class c)
	{
		clzz = c;
	}

	public Class getType()
	{
		return clzz;
	}

	public String getTypeString()
	{
		return clzz == null ? null : clzz.toString();
	}

	/**
	 * @return true if object satisfies filter; false otherwise.
	 */
	public boolean matches(Object o)
	{
		return ComparisonUtils.matches(this, o.getClass());
	}
}
/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.exceptions;

import ncsa.tools.common.exceptions.BaseCommonException;

public class ParseException extends BaseCommonException
{
	private static final long serialVersionUID = 2013L;

	public ParseException()
	{
		super();
	}

	public ParseException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ParseException(String message)
	{
		super(message);
	}

	public ParseException(Throwable cause)
	{
		super(cause);
	}
}

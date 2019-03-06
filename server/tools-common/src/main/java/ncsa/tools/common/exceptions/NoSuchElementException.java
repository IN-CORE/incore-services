/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.exceptions;

public class NoSuchElementException extends BaseCommonException
{
	private static final long serialVersionUID = 2012L;

	public NoSuchElementException()
	{
		super();
	}

	public NoSuchElementException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public NoSuchElementException(String message)
	{
		super(message);
	}

	public NoSuchElementException(Throwable cause)
	{
		super(cause);
	}
}

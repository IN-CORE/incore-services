/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtils
{
	public static String getFormattedErrorString(String message, Throwable t)
	{
		StringBuffer b = new StringBuffer();

		b.append(message);
		b.append(" -- "); //$NON-NLS-1$
		b.append(t.getClass().getName()).append(" -- "); //$NON-NLS-1$
		b.append(t.getLocalizedMessage());

		return b.toString();
	}

	/*
	 * ////////////////////////////////////////////////////////////////////////
	 * EXCEPTION MESSAGES //
	 * /////////////////////////////////////////////////////////////////////
	 */

	/**
	 * Tries to match the given String by recurring on nested exception
	 * messages.
	 * 
	 * @param t
	 *            a Throwable
	 * @param toMatch
	 *            message being sought.
	 * @return true if message occurs in some nested exception; false
	 *         otherwise.
	 */
	public static boolean checkException(Throwable t, String toMatch)
	{
		String message = null;
		Throwable throwable = t;
		while (throwable != null) {
			message = throwable.getMessage();
			if (message != null && -1 < message.indexOf(toMatch))
				return true;
			throwable = throwable.getCause();
		}
		return false;
	} // checkException

	/**
	 * Redirects a stack trace into a String.
	 * 
	 * @param t
	 *            from which to get trace.
	 * @return the stack trace as String.
	 */
	public static String getStackTrace(Throwable t)
	{
		if (t == null)
			return null;
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString();
	} // getStackTrace
}
/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.types;

import ncsa.tools.common.NCSAConstants;

/**
 * A normalizing descriptor for file format types. Used mainly
 * for determining the sequence of type conversions for binary files.
 * Should be used only when reading from a file which was originally written
 * as binary.
 * <P>
 * 
 * Format types correspond to the various read methods in java.io.RandomAccessFile, plus "whitespace" (WSPC), "newline" (NEWL) and
 * "return" (CRET).
 * 
 * @author Albert L. Rossi
 */
public class FileFormatType
{
	private int type = NCSAConstants.UNDEFINED;

	/**
	 * @return type value.
	 */
	public int getType()
	{
		return type;
	}

	public void setType(String s)
	{
		setText(s);
	}

	/**
	 * @param s
	 *            options: "boolean", "byte", "unsigned byte",
	 *            "char", "utf", "double", "float", "int", "long",
	 *            "short", "unsigned short", "whitespace",
	 *            "newline", and "return".
	 * 
	 */
	public void setText(String s)
	{
		if (s.equalsIgnoreCase("boolean"))
			type = NCSAConstants.BOOL;
		else if (s.equalsIgnoreCase("byte"))
			type = NCSAConstants.BYTE;
		else if (s.equalsIgnoreCase("unsigned byte"))
			type = NCSAConstants.UBYT;
		else if (s.equalsIgnoreCase("char"))
			type = NCSAConstants.CHAR;
		else if (s.equalsIgnoreCase("utf"))
			type = NCSAConstants.UTFC;
		else if (s.equalsIgnoreCase("double"))
			type = NCSAConstants.DOUB;
		else if (s.equalsIgnoreCase("float"))
			type = NCSAConstants.FLOT;
		else if (s.equalsIgnoreCase("int"))
			type = NCSAConstants.INTG;
		else if (s.equalsIgnoreCase("long"))
			type = NCSAConstants.LONG;
		else if (s.equalsIgnoreCase("short"))
			type = NCSAConstants.SHRT;
		else if (s.equalsIgnoreCase("unsigned short"))
			type = NCSAConstants.USHT;
		else if (s.equalsIgnoreCase("whitespace"))
			type = NCSAConstants.WSPC;
		else if (s.equalsIgnoreCase("newline"))
			type = NCSAConstants.NEWL;
		else if (s.equalsIgnoreCase("return"))
			type = NCSAConstants.CRET;
	} // setText

}

/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.util;

import java.util.List;

import org.apache.log4j.Logger;

import ncsa.tools.common.exceptions.InitializationException;
import ncsa.tools.common.exceptions.ScanException;

/**
 * Functionality adapted and modified from org.apache.tools.ant.FileScanner
 * and org.apache.tools.ant.DirectoryScanner.
 * 
 * @author Albert L. Rossi
 */
public abstract class AbstractScanner
{
	protected static Logger logger = Logger.getLogger(AbstractScanner.class);

	protected String[] includes = null;
	protected String[] excludes = null;

	/*
	 * flags for indicating what data to preserve.
	 */
	protected boolean retainIncluded = true;
	protected boolean retainExcluded = false;
	protected boolean retainNotIncluded = false;
	protected boolean isCaseSensitive = true;
	protected boolean fullMetadata = false;
	protected boolean sortDescending = false;
	protected boolean failonerror = false;

	/**
	 * Abstract class.
	 */
	protected AbstractScanner()
	{
	}

	/**
	 * Will do a complete scan (i.e., will recur on any scannable parts).
	 * 
	 * @throws
	 */
	public abstract void fullScan() throws ScanException;

	/**
	 * Will do only a top-level scan, with no recursion.
	 * 
	 * @return list of subparts left to scan.
	 */
	public abstract List shallowScan() throws ScanException;

	/**
	 * Stream-like functionality. Should only be called subsequent
	 * to a call to shallowScan (or else it will return nothing).
	 * 
	 * @return list of subparts left to scan.
	 */
	public abstract List scanNext() throws ScanException;

	/**
	 * @param b
	 *            if true, returned ListResult will contain listings which
	 *            match an include pattern (default = true).
	 */
	public void setRetainIncluded(boolean b)
	{
		retainIncluded = b;
	}

	/**
	 * @param b
	 *            if true, returned ListResult will contain listings which
	 *            match both an include and exclude pattern (default = false).
	 */
	public void setRetainExcluded(boolean b)
	{
		retainExcluded = b;
	}

	/**
	 * @param b
	 *            if true, returned ListResult will contain listings which do
	 *            not match any of the includes patterns (default = false).
	 */
	public void setRetainNotIncluded(boolean b)
	{
		retainNotIncluded = b;
	}

	/**
	 * @param b
	 *            if true, sorts in descending order (default = false).
	 * 
	 */
	public void setDescending(boolean b)
	{
		sortDescending = b;
	}

	/**
	 * Whether or not the scan should produce complete metadata results
	 * or not. If this flag is false, methods returning metadata objects
	 * should return an empty array.
	 * 
	 * @param b
	 *            if true, stores full metadata object; else stores only the
	 *            path / name (default = false).
	 */
	public void setFullMetadata(boolean b)
	{
		fullMetadata = b;
	}

	/**
	 * @param b
	 *            if true, and the file or directory does not exist,
	 *            causes scanner to throw an exception.
	 */
	public void setFailonerror(boolean b)
	{
		failonerror = b;
	}

	/**
	 * Closes streams and connections, if any.
	 */
	public abstract void close();

	/*
	 * /////////////////////////////////////////////////////////////////////////
	 * AUXILIARY METHODS //
	 * //////////////////////////////////////////////////////////////////////
	 */

	/**
	 * @param b
	 *            if true, matching is case sensitive (default = true).
	 */
	protected void setCaseSensitive(boolean b)
	{
		isCaseSensitive = b;
	}

	/**
	 * Sets the list of include patterns to use.
	 * 
	 * @param includes
	 *            array of exclude patterns. May be <code>null</code>.
	 *            If a non-<code>null</code> array is
	 *            given, all elements must be non-<code>null</code>.
	 */
	protected void setIncludes(String[] includes)
	{
		if (includes == null) {
			this.includes = null;
		} else {
			this.includes = includes;
		}
	} // setIncludes

	/**
	 * Sets the list of exclude patterns to use.
	 * 
	 * @param excludes
	 *            array of exclude patterns. May be <code>null</code>.
	 *            If a non-<code>null</code> array is
	 *            given, all elements must be non-<code>null</code>.
	 */
	protected void setExcludes(String[] excludes)
	{
		if (excludes == null) {
			this.excludes = null;
		} else {
			this.excludes = excludes;
		}
	} // setExcludes

	/**
	 * Tests whether or not a name matches the start of at least one include
	 * pattern.
	 * 
	 * @param name
	 *            The name to match. Must not be <code>null</code>.
	 * @return <code>true</code> when the name matches against the start of at
	 *         least one include pattern, or <code>false</code> otherwise.
	 */
	protected boolean couldHoldIncluded(String name)
	{
		for (int i = 0; i < includes.length; i++) {
			if (matchPatternStart(includes[i], name, isCaseSensitive)) {
				return true;
			}
		}
		return false;
	} // couldHoldIncluded

	/**
	 * To be implemented by concrete classes.
	 */
	protected abstract void init() throws InitializationException;

	/*
	 * /////////////////////////////////////////////////////////////////////////
	 * STATIC PROTECTED METHODS //
	 * //////////////////////////////////////////////////////////////////////
	 */

	/**
	 * Tests whether or not a string matches against a pattern.
	 * The pattern may contain two special characters:<br>
	 * '*' means zero or more characters<br>
	 * '?' means one and only one character
	 * 
	 * @param pattern
	 *            The pattern to match against.
	 *            Must not be <code>null</code>.
	 * @param str
	 *            The string which must be matched against the pattern.
	 *            Must not be <code>null</code>.
	 * 
	 * @return <code>true</code> if the string matches against the pattern,
	 *         or <code>false</code> otherwise.
	 */
	protected static boolean match(String pattern, String str)
	{
		return SelectorUtils.match(pattern, str);
	}

	/**
	 * Tests whether or not a given path matches the start of a given
	 * pattern up to the first "**".
	 * <p>
	 * This is not a general purpose test and should only be used if you can live with false positives. For example,
	 * <code>pattern=**\a</code> and <code>str=b</code> will yield <code>true</code>.
	 * 
	 * @param pattern
	 *            The pattern to match against. Must not be <code>null</code>.
	 * @param str
	 *            The path to match, as a String. Must not be <code>null</code>.
	 * 
	 * @return whether or not a given path matches the start of a given
	 *         pattern up to the first "**".
	 */
	protected static boolean matchPatternStart(String pattern, String str)
	{
		return SelectorUtils.matchPatternStart(pattern, str);
	}

	/**
	 * Tests whether or not a given path matches the start of a given
	 * pattern up to the first "**".
	 * <p>
	 * This is not a general purpose test and should only be used if you can live with false positives. For example,
	 * <code>pattern=**\a</code> and <code>str=b</code> will yield <code>true</code>.
	 * 
	 * @param pattern
	 *            The pattern to match against. Must not be <code>null</code>.
	 * @param str
	 *            The path to match, as a String. Must not be <code>null</code>.
	 * @param caseSensitive
	 *            Whether or not matching should be performed
	 *            case sensitively.
	 * 
	 * @return whether or not a given path matches the start of a given
	 *         pattern up to the first "**".
	 */
	protected static boolean matchPatternStart(String pattern, String str, boolean isCaseSensitive)
	{
		return SelectorUtils.matchPatternStart(pattern, str, isCaseSensitive);
	}

	/**
	 * Tests whether or not a given path matches a given pattern.
	 * 
	 * @param pattern
	 *            The pattern to match against. Must not be <code>null</code>.
	 * @param str
	 *            The path to match, as a String. Must not be <code>null</code>.
	 * 
	 * @return <code>true</code> if the pattern matches against the string,
	 *         or <code>false</code> otherwise.
	 */
	protected static boolean matchPath(String pattern, String str)
	{
		return SelectorUtils.matchPath(pattern, str);
	}

	/**
	 * Tests whether or not a given path matches a given pattern.
	 * 
	 * @param pattern
	 *            The pattern to match against. Must not be <code>null</code>.
	 * @param str
	 *            The path to match, as a String. Must not be <code>null</code>.
	 * @param caseSensitive
	 *            Whether or not matching should be performed
	 *            case sensitively.
	 * 
	 * @return <code>true</code> if the pattern matches against the string,
	 *         or <code>false</code> otherwise.
	 */
	protected static boolean matchPath(String pattern, String str, boolean isCaseSensitive)
	{
		return SelectorUtils.matchPath(pattern, str, isCaseSensitive);
	}

	/**
	 * Tests whether or not a string matches against a pattern.
	 * The pattern may contain two special characters:<br>
	 * '*' means zero or more characters<br>
	 * '?' means one and only one character
	 * 
	 * @param pattern
	 *            The pattern to match against.
	 *            Must not be <code>null</code>.
	 * @param str
	 *            The string which must be matched against the pattern.
	 *            Must not be <code>null</code>.
	 * @param caseSensitive
	 *            Whether or not matching should be performed
	 *            case sensitively.
	 * 
	 * 
	 * @return <code>true</code> if the string matches against the pattern,
	 *         or <code>false</code> otherwise.
	 */
	protected static boolean match(String pattern, String str, boolean isCaseSensitive)
	{
		return SelectorUtils.match(pattern, str, isCaseSensitive);
	}
}

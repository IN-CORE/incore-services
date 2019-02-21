/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.util;

/**
 * The methods in this class are basically the same as those in
 * org.apache.tools.ant.types.selectors.SelectorUtils, but with
 * all platform-dependent calls to the File.separator replaced by
 * '/'.
 * 
 * @author Albert L. Rossi
 * @author Arnout J. Kuiper
 * @author Magesh Umasankar
 */
public final class SelectorUtils
{
	/**
	 * Static utility class; cannot be constructed.
	 */
	private SelectorUtils()
	{
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
	public static boolean matchPatternStart(String pattern, String str)
	{
		return matchPatternStart(pattern, str, true);
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
	 * @param isCaseSensitive
	 *            Whether or not matching should be performed
	 *            case sensitively.
	 * 
	 * @return whether or not a given path matches the start of a given
	 *         pattern up to the first "**".
	 */
	public static boolean matchPatternStart(String pattern, String str, boolean isCaseSensitive)
	{
		// When str starts with a "/", pattern has to start with a
		// "/".
		// When pattern starts with a "/", str has to start with a
		// "/".
		if (str.startsWith("/") != pattern.startsWith("/")) {
			return false;
		}

		String[] patDirs = tokenizePathAsArray(pattern);
		String[] strDirs = tokenizePathAsArray(str);

		int patIdxStart = 0;
		int patIdxEnd = patDirs.length - 1;
		int strIdxStart = 0;
		int strIdxEnd = strDirs.length - 1;

		// up to first '**'
		while (patIdxStart <= patIdxEnd && strIdxStart <= strIdxEnd) {
			String patDir = patDirs[patIdxStart];
			if (patDir.equals("**")) {
				break;
			}
			if (!match(patDir, strDirs[strIdxStart], isCaseSensitive)) {
				return false;
			}
			patIdxStart++;
			strIdxStart++;
		}

		if (strIdxStart > strIdxEnd) {
			// String is exhausted
			return true;
		} else if (patIdxStart > patIdxEnd) {
			// String not exhausted, but pattern is. Failure.
			return false;
		} else {
			// pattern now holds ** while string is not exhausted
			// this will generate false positives but we can live with that.
			return true;
		}
	} // matchPatternStart

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
	public static boolean matchPath(String pattern, String str)
	{
		return matchPath(pattern, str, true);
	}

	/**
	 * Tests whether or not a given path matches a given pattern.
	 * 
	 * @param pattern
	 *            The pattern to match against. Must not be <code>null</code>.
	 * @param str
	 *            The path to match, as a String. Must not be <code>null</code>.
	 * @param isCaseSensitive
	 *            Whether or not matching should be performed
	 *            case sensitively.
	 * 
	 * @return <code>true</code> if the pattern matches against the string,
	 *         or <code>false</code> otherwise.
	 */
	public static boolean matchPath(String pattern, String str, boolean isCaseSensitive)
	{
		// When str starts with a "/", pattern has to start with a
		// "/".
		// When pattern starts with a "/", str has to start with a
		// "/".
		if (str.startsWith("/") != pattern.startsWith("/")) {
			return false;
		}

		String[] patDirs = tokenizePathAsArray(pattern);
		String[] strDirs = tokenizePathAsArray(str);

		int patIdxStart = 0;
		int patIdxEnd = patDirs.length - 1;
		int strIdxStart = 0;
		int strIdxEnd = strDirs.length - 1;

		// up to first '**'
		while (patIdxStart <= patIdxEnd && strIdxStart <= strIdxEnd) {
			String patDir = patDirs[patIdxStart];
			if (patDir.equals("**")) {
				break;
			}
			if (!match(patDir, strDirs[strIdxStart], isCaseSensitive)) {
				patDirs = null;
				strDirs = null;
				return false;
			}
			patIdxStart++;
			strIdxStart++;
		}

		if (strIdxStart > strIdxEnd) {
			// String is exhausted
			for (int i = patIdxStart; i <= patIdxEnd; i++) {
				if (!patDirs[i].equals("**")) {
					patDirs = null;
					strDirs = null;
					return false;
				}
			}
			return true;
		}

		if (patIdxStart > patIdxEnd) {
			// String not exhausted, but pattern is. Failure.
			patDirs = null;
			strDirs = null;
			return false;
		}

		// up to last '**'
		while (patIdxStart <= patIdxEnd && strIdxStart <= strIdxEnd) {
			String patDir = patDirs[patIdxEnd];
			if (patDir.equals("**")) {
				break;
			}
			if (!match(patDir, strDirs[strIdxEnd], isCaseSensitive)) {
				patDirs = null;
				strDirs = null;
				return false;
			}
			patIdxEnd--;
			strIdxEnd--;
		}
		if (strIdxStart > strIdxEnd) {
			// String is exhausted
			for (int i = patIdxStart; i <= patIdxEnd; i++) {
				if (!patDirs[i].equals("**")) {
					patDirs = null;
					strDirs = null;
					return false;
				}
			}
			return true;
		}

		while (patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd) {
			int patIdxTmp = -1;
			for (int i = patIdxStart + 1; i <= patIdxEnd; i++) {
				if (patDirs[i].equals("**")) {
					patIdxTmp = i;
					break;
				}
			}
			if (patIdxTmp == patIdxStart + 1) {
				// '**/**' situation, so skip one
				patIdxStart++;
				continue;
			}
			// Find the pattern between padIdxStart & padIdxTmp in str between
			// strIdxStart & strIdxEnd
			int patLength = (patIdxTmp - patIdxStart - 1);
			int strLength = (strIdxEnd - strIdxStart + 1);
			int foundIdx = -1;
			strLoop: for (int i = 0; i <= strLength - patLength; i++) {
				for (int j = 0; j < patLength; j++) {
					String subPat = patDirs[patIdxStart + j + 1];
					String subStr = strDirs[strIdxStart + i + j];
					if (!match(subPat, subStr, isCaseSensitive)) {
						continue strLoop;
					}
				}

				foundIdx = strIdxStart + i;
				break;
			}

			if (foundIdx == -1) {
				patDirs = null;
				strDirs = null;
				return false;
			}

			patIdxStart = patIdxTmp;
			strIdxStart = foundIdx + patLength;
		}

		for (int i = patIdxStart; i <= patIdxEnd; i++) {
			if (!patDirs[i].equals("**")) {
				patDirs = null;
				strDirs = null;
				return false;
			}
		}

		return true;
	} // matchPath

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
	public static boolean match(String pattern, String str)
	{
		return match(pattern, str, true);
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
	 * @param isCaseSensitive
	 *            Whether or not matching should be performed
	 *            case sensitively.
	 * 
	 * 
	 * @return <code>true</code> if the string matches against the pattern,
	 *         or <code>false</code> otherwise.
	 */
	public static boolean match(String pattern, String str, boolean isCaseSensitive)
	{
		char[] patArr = pattern.toCharArray();
		char[] strArr = str.toCharArray();
		int patIdxStart = 0;
		int patIdxEnd = patArr.length - 1;
		int strIdxStart = 0;
		int strIdxEnd = strArr.length - 1;
		char ch;

		boolean containsStar = false;
		for (int i = 0; i < patArr.length; i++) {
			if (patArr[i] == '*') {
				containsStar = true;
				break;
			}
		}

		if (!containsStar) {
			// No '*'s, so we make a shortcut
			if (patIdxEnd != strIdxEnd) {
				return false; // Pattern and string do not have the same size
			}
			for (int i = 0; i <= patIdxEnd; i++) {
				ch = patArr[i];
				if (ch != '?') {
					if (isCaseSensitive && ch != strArr[i]) {
						return false; // Character mismatch
					}
					if (!isCaseSensitive && Character.toUpperCase(ch) != Character.toUpperCase(strArr[i])) {
						return false; // Character mismatch
					}
				}
			}
			return true; // String matches against pattern
		}

		if (patIdxEnd == 0) {
			return true; // Pattern contains only '*', which matches anything
		}

		// Process characters before first star
		while ((ch = patArr[patIdxStart]) != '*' && strIdxStart <= strIdxEnd) {
			if (ch != '?') {
				if (isCaseSensitive && ch != strArr[strIdxStart]) {
					return false; // Character mismatch
				}
				if (!isCaseSensitive && Character.toUpperCase(ch) != Character.toUpperCase(strArr[strIdxStart])) {
					return false; // Character mismatch
				}
			}
			patIdxStart++;
			strIdxStart++;
		}
		if (strIdxStart > strIdxEnd) {
			// All characters in the string are used. Check if only '*'s are
			// left in the pattern. If so, we succeeded. Otherwise failure.
			for (int i = patIdxStart; i <= patIdxEnd; i++) {
				if (patArr[i] != '*') {
					return false;
				}
			}
			return true;
		}

		// Process characters after last star
		while ((ch = patArr[patIdxEnd]) != '*' && strIdxStart <= strIdxEnd) {
			if (ch != '?') {
				if (isCaseSensitive && ch != strArr[strIdxEnd]) {
					return false; // Character mismatch
				}
				if (!isCaseSensitive && Character.toUpperCase(ch) != Character.toUpperCase(strArr[strIdxEnd])) {
					return false; // Character mismatch
				}
			}
			patIdxEnd--;
			strIdxEnd--;
		}
		if (strIdxStart > strIdxEnd) {
			// All characters in the string are used. Check if only '*'s are
			// left in the pattern. If so, we succeeded. Otherwise failure.
			for (int i = patIdxStart; i <= patIdxEnd; i++) {
				if (patArr[i] != '*') {
					return false;
				}
			}
			return true;
		}

		// process pattern between stars. padIdxStart and patIdxEnd point
		// always to a '*'.
		while (patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd) {
			int patIdxTmp = -1;
			for (int i = patIdxStart + 1; i <= patIdxEnd; i++) {
				if (patArr[i] == '*') {
					patIdxTmp = i;
					break;
				}
			}
			if (patIdxTmp == patIdxStart + 1) {
				// Two stars next to each other, skip the first one.
				patIdxStart++;
				continue;
			}
			// Find the pattern between padIdxStart & padIdxTmp in str between
			// strIdxStart & strIdxEnd
			int patLength = (patIdxTmp - patIdxStart - 1);
			int strLength = (strIdxEnd - strIdxStart + 1);
			int foundIdx = -1;
			strLoop: for (int i = 0; i <= strLength - patLength; i++) {
				for (int j = 0; j < patLength; j++) {
					ch = patArr[patIdxStart + j + 1];
					if (ch != '?') {
						if (isCaseSensitive && ch != strArr[strIdxStart + i + j]) {
							continue strLoop;
						}
						if (!isCaseSensitive && Character.toUpperCase(ch) != Character.toUpperCase(strArr[strIdxStart + i + j])) {
							continue strLoop;
						}
					}
				}

				foundIdx = strIdxStart + i;
				break;
			}

			if (foundIdx == -1) {
				return false;
			}

			patIdxStart = patIdxTmp;
			strIdxStart = foundIdx + patLength;
		}

		// All characters in the string are used. Check if only '*'s are left
		// in the pattern. If so, we succeeded. Otherwise failure.
		for (int i = patIdxStart; i <= patIdxEnd; i++) {
			if (patArr[i] != '*') {
				return false;
			}
		}
		return true;
	} // match

	/**
	 * Breaks a path up into an array of path elements, tokenizing on <code>"/"</code>.
	 * 
	 * @param path
	 *            Path to tokenize. Must not be <code>null</code>.
	 * 
	 * @return path elements from the tokenized path
	 */
	private static String[] tokenizePathAsArray(String path)
	{
		char sep = '/';
		int start = 0;
		int len = path.length();
		int count = 0;
		for (int pos = 0; pos < len; pos++) {
			if (path.charAt(pos) == sep) {
				if (pos != start) {
					count++;
				}
				start = pos + 1;
			}
		}
		if (len != start) {
			count++;
		}
		String[] l = new String[count];
		count = 0;
		start = 0;
		for (int pos = 0; pos < len; pos++) {
			if (path.charAt(pos) == sep) {
				if (pos != start) {
					String tok = path.substring(start, pos);
					l[count++] = tok;
				}
				start = pos + 1;
			}
		}
		if (len != start) {
			String tok = path.substring(start);
			l[count/* ++ */] = tok;
		}
		return l;
	} // tokenizePathAsArray

}

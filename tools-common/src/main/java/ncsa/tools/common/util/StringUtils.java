package ncsa.tools.common.util;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ncsa.tools.common.types.Property;

/**
 * Convenience wrappers around some Java String functionality.
 * 
 * @author Albert L. Rossi
 */
public class StringUtils
{
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

	/**
	 * Static utility class; cannot be constructed.
	 */
	private StringUtils()
	{
	}

	/*
	 * ////////////////////////////////////////////////////////////////////////
	 * STRING MUTATION //
	 * /////////////////////////////////////////////////////////////////////
	 */

	/**
	 * @param prefix
	 *            to be discarded.
	 * @param full
	 *            string from which to extract suffix.
	 * @return suffix, with first letter changed to lower case, or null
	 *         if prefix equals full.
	 */
	public static String getLCSuffix(String prefix, String full)
	{
		int first = prefix == null ? 0 : prefix.length();
		if (first == full.length())
			return null;
		int second = first + 1;
		String initialLetter = full.substring(first, second).toLowerCase();
		return initialLetter.concat(full.substring(second));
	} // getLCSuffix

	/**
	 * @param len
	 *            number of spaces.
	 * @return string consisting of exactly len white-space characters.
	 */
	public static String getSpaces(int len)
	{
		StringBuffer sb = new StringBuffer(len);
		for (int i = 0; i < len; i++)
			sb.append(" ");
		return sb.toString();
	} // getSpaces

	/**
	 * Appends the second String to the indicated prefix of the first.
	 * 
	 * @param buffer
	 *            to append to.
	 * @param prefixLength
	 *            length of buffer to retain (-1 = ALL).
	 * @param toAppend
	 *            new suffix.
	 * @return concatenated result.
	 * @throws IndexOutOfBoundsException
	 *             if the indicated prefix length exceeds
	 *             the actual length of the original String.
	 */
	public static String appendToPrefix(StringBuffer buffer, int prefixLength, String toAppend) throws IndexOutOfBoundsException
	{
		int bufferLength = buffer.length();

		if (prefixLength > bufferLength)
			throw new IndexOutOfBoundsException("prefix length " + prefixLength + " greater than buffer length " + bufferLength);

		if (prefixLength < 0)
			prefixLength = bufferLength;

		if (bufferLength > prefixLength)
			buffer.delete(prefixLength, bufferLength);

		buffer.append(toAppend);

		return buffer.toString();
	} // appendToPrefix

	/**
	 * Normalizes all non-alphanumeric characters to "_".
	 * 
	 * @param sequence
	 *            to normalize.
	 * @return the normalized sequence.
	 */
	public static StringBuffer normalizeNonAlphanumeric(StringBuffer sequence)
	{
		for (int i = 0; i < sequence.length(); i++) {
			char next = sequence.charAt(i);
			if ((next < 'a' || next > 'z') && (next < 'A' || next > 'Z') && (next < '0' || next > '9'))
				sequence.setCharAt(i, '_');
		}
		return sequence;
	} // normalizeNonAlphanumeric

	/**
	 * Appends a sequence suffix to the base.
	 * 
	 * @param base
	 *            id to append to.
	 * @param seqno
	 *            suffix to use.
	 * @return concatenated result.
	 */
	public static String getSequenceId(String base, long seqno)
	{
		StringBuffer buf = new StringBuffer(base);
		buf.append("-");
		buf.append(seqno);
		return buf.toString();
	} // getSequenceId

	/**
	 * Returns an id with a pretty good chance of being unique, by
	 * appending to the given prefix the date, host and a random long.
	 * 
	 * @param prefix
	 *            to use as id base.
	 * @return id generated from prefix.
	 */
	public static String getRandomizedId(String prefix)
	{
		return getRandomizedId(prefix, true, true, true, true);
	} // getRandomizedId

	/**
	 * Returns an id by appending to the given prefix the date,
	 * host and a random long.
	 * 
	 * @param prefix
	 *            to use as id base.
	 * @param useTimestamp
	 *            if true, timestamp is included.
	 * @param useHost
	 *            if true, host name is included.
	 * @param useLong
	 *            if true, random is long (else, int).
	 * @return id generated from prefix.
	 */
	public static String getRandomizedId(String prefix, boolean useTimestamp, boolean useHost, boolean useLong, boolean normalize)
	{
		if (prefix == null)
			prefix = "";
		StringBuffer tokenName = new StringBuffer(prefix);
		if (useTimestamp) {
			if (tokenName.length() > 0)
				tokenName.append("_");
			tokenName.append(sdf.format(new Date(System.currentTimeMillis())));
		}
		if (useHost) {
			try {
				if (tokenName.length() > 0)
					tokenName.append("_");
				tokenName.append(InetAddress.getLocalHost().getHostName());
			} catch (UnknownHostException uhe) {
				tokenName.append("localhost");
			}
		}
		if (tokenName.length() > 0)
			tokenName.append("_");

		if (useLong)
			tokenName.append(Math.abs(SystemUtils.random.nextLong()));
		else
			tokenName.append(Math.abs(SystemUtils.random.nextInt()));

		if (normalize)
			tokenName = normalizeNonAlphanumeric(tokenName);
		return tokenName.toString();
	} // getRandomizedId

	public static String getSuffix(String name, String delim)
	{
		int firstDot = name.indexOf(delim);
		if (firstDot >= 0)
			return name.substring(firstDot);
		return "";
	}

	public static String getPrefix(String name, String delim)
	{
		int firstDot = name.indexOf(delim);
		if (firstDot >= 0)
			return name.substring(0, firstDot);
		return name;
	}

	/*
	 * ////////////////////////////////////////////////////////////////////////
	 * NUMERICAL STRINGS //
	 * /////////////////////////////////////////////////////////////////////
	 */

	/**
	 * @param number
	 *            object representing a number.
	 * @param places
	 *            to the left of decimal to fill with zeros.
	 * @return String representation of number with leading zeros added.
	 */
	public static String addLeadingZeros(Object number, int places) throws IllegalArgumentException
	{
		if (number == null)
			return null;
		if (number instanceof String) {
			try {
				Double.valueOf((String) number);
			} catch (Throwable t) {
				throw new IllegalArgumentException("addLeadingZeros, problem with string value " + number + ": " + t.getCause());
			}
		} else if (!(number instanceof Number)) {
			throw new IllegalArgumentException(number + " is not a Number");
		}

		StringBuffer suffix = new StringBuffer(0);

		long l = 0;
		int start = 0;
		int end = -1;
		int zeros = places;

		suffix.append(number);

		if (suffix.toString().equals("null"))
			throw new IllegalArgumentException("no number string to modify");

		if (suffix.charAt(0) == '-')
			start = 1;
		else
			start = 0;

		while (start < suffix.length() && suffix.charAt(start) == '0')
			suffix.deleteCharAt(start);

		end = suffix.indexOf(".");
		if (end < 0)
			end = suffix.length();

		String intPart = suffix.substring(start, end);

		if (intPart == null || intPart.length() < 1) {
			l = 0;
			if (zeros > 0 || end == suffix.length())
				suffix.insert(start, "0");
		} else {
			l = new Long(suffix.substring(start, end)).longValue();
		}

		do {
			zeros--;
			l /= 10;
		} while (l > 0);

		for (int i = 0; i < zeros; i++)
			suffix.insert(start, "0");

		return suffix.toString();
	} // addLeadingZeros

	public static String convert(String orig, int from, int to)
	{
		int place = 1;
		int decimal = 0;
		boolean minus = false;

		if (orig.startsWith("-")) {
			minus = true;
			orig = orig.substring(1);
		}

		int last = orig.length() - 1;

		for (int i = last; i >= 0; i--) {
			char c = orig.charAt(i);
			decimal += (place * charToDecimal(c));
			place *= from;
		}

		StringBuffer converted = new StringBuffer();

		while (decimal > 0) {
			int digit = decimal % to;
			decimal /= to;
			converted.insert(0, decimalToChar(digit));
		}

		if (minus)
			converted.insert(0, "-");

		return converted.toString();
	}

	public static int stringToDecimal(String value)
	{
		value = value.toLowerCase();
		int last = value.length() - 1;

		int decimalValue = 0;
		int place = 1;

		for (int i = last; i >= 0; i--) {
			char c = value.charAt(i);
			decimalValue += (place * charToDecimal(c));
			place *= 100;
		}

		return decimalValue;
	}

	private static int charToDecimal(char c)
	{
		if (c >= '0' && c <= '9') {
			return c - '0';
		} else if (c >= 'a' && c <= 'z') {
			return 10 + (c - 'a');
		}
		throw new IllegalArgumentException("cannot convert " + c + " to decimal");
	}

	private static char decimalToChar(int i)
	{
		if (i >= 0 && i < 10) {
			return (char) ('0' + i);
		} else if (i >= 10 && i < 35) {
			return (char) ('a' + i - 10);
		}
		throw new IllegalArgumentException("cannot convert " + i + " to character");
	}

	/*
	 * ////////////////////////////////////////////////////////////////////////
	 * STRING TOKENIZATION //
	 * /////////////////////////////////////////////////////////////////////
	 */

	public static String[] getTokenArray(String sequence, String[] chain, boolean paired, boolean includeDelim)
			throws IllegalArgumentException
	{
		return (String[]) tokenize(sequence, chain, paired, includeDelim).toArray(new String[0]);
	} // getTokenArray( tag )

	/**
	 * Moves through the chain of delimiters in order until one of the tokens
	 * is not found in the remainder of the string.
	 * 
	 * @param sequence
	 *            to be tokenized.
	 * @param chain
	 *            list of delimiters to use cyclically.
	 * @param paired
	 *            process delimiters in pairs,
	 *            returning token between them
	 * @param includeDelim
	 *            append delimiter to token
	 * @return List of token Strings.
	 * @throws IllegalArgumentException
	 *             if original String is null
	 */
	public static List tokenize(String sequence, String[] chain, boolean paired, boolean includeDelim) throws IllegalArgumentException
	{
		List tokens = new ArrayList();
		if (sequence == null)
			return null;
		if (chain == null)
			throw new IllegalArgumentException("no delimiter chain defined");
		if (chain.length == 0) {
			tokens.add(sequence);
			if (paired && chain.length % 2 != 0)
				throw new IllegalArgumentException("paired tokenization requires even-number of delimiters in chain");
		} else {
			int[] markers = { 0, -1 };
			int i = 0;
			int len = chain.length;
			int incr = paired ? 2 : 1;
			while (true) {
				if (paired) {
					addToken(sequence, chain[i], chain[i + 1], markers, includeDelim, tokens);
				} else {
					addToken(sequence, chain[i], markers, includeDelim, tokens);
				}
				if (markers[0] >= sequence.length())
					break;
				i = (i + incr) % len;
			}
		}
		return tokens;
	} // tokenize

	private static void addToken(String sequence, String delim, int[] markers, boolean includeDelim, List tokens)
	{
		markers[1] = sequence.indexOf(delim, markers[0]);
		if (markers[1] < 0)
			markers[1] = sequence.length();
		else if (includeDelim)
			markers[1] += delim.length();
		tokens.add(sequence.substring(markers[0], markers[1]));
		if (includeDelim)
			markers[0] = markers[1];
		else
			markers[0] = markers[1] + delim.length();
	}

	private static void addToken(String sequence, String start, String end, int[] markers, boolean includeDelim, List tokens)
	{
		markers[1] = sequence.indexOf(start, markers[0]);
		if (markers[1] < 0)
			markers[1] = sequence.length();
		else {
			if (!includeDelim)
				markers[0] = markers[1] + start.length();
			else
				markers[0] = markers[1];
			markers[1] = sequence.indexOf(end, markers[0]);
			if (markers[1] < 0)
				markers[1] = sequence.length();
			else {
				if (includeDelim)
					markers[1] = markers[1] + end.length();
			}
		}
		tokens.add(sequence.substring(markers[0], markers[1]));
		if (includeDelim)
			markers[0] = markers[1];
		else
			markers[0] = markers[1] + end.length();
	}

	public static String[] parseIntoLines(String string) throws IOException
	{
		BufferedReader br = new BufferedReader(new StringReader(string));
		List list = new ArrayList();
		String line = null;
		while (true) {
			try {
				line = br.readLine();
			} catch (EOFException eof) {
				break;
			}
			if (line == null)
				break;
			if (line.equals(""))
				continue;
			list.add(line);
		}
		return (String[]) list.toArray(new String[0]);
	}

	/*
	 * ////////////////////////////////////////////////////////////////////////
	 * SHELL VARIABLES //
	 * /////////////////////////////////////////////////////////////////////
	 */

	/**
	 * Takes a map of properties whose values could contain shell-script
	 * type references, i.e., '${...}', and orders them so that all
	 * references are to previously defined variables.
	 */
	public static void orderDeclarations(Map m, List l)
	{
		String[] keys = (String[]) m.keySet().toArray(new String[0]);
		for (int i = 0; i < keys.length; i++) {
			if (m.containsKey(keys[i])) {
				maybeAddToList((Property) m.remove(keys[i]), m, l);
			}
		}
	}

	private static void maybeAddToList(Property p, Map m, List l)
	{
		String[] refs = extractReferences(p.getValue());
		for (int i = 0; i < refs.length; i++) {
			if (m.containsKey(refs[i])) {
				Property ref = (Property) m.remove(refs[i]);
				maybeAddToList(ref, m, l);
			}
		}
		l.add(p);
	}

	private static String[] extractReferences(String value)
	{
		List l = new ArrayList();

		for (int i = 0; i < value.length(); i++) {
			if ('$' == value.charAt(i)) {
				int start = i + 2;
				while ('}' != value.charAt(i))
					i++;
				int end = i;
				l.add(value.substring(start, end));
			}
		}
		return (String[]) l.toArray(new String[0]);
	}

	/*
	 * ////////////////////////////////////////////////////////////////////////
	 * EXCEPTION MESSAGES //
	 * /////////////////////////////////////////////////////////////////////
	 */

	/**
	 * @deprecated use ExceptionUtils
	 */
	public static boolean checkException(Throwable t, String toMatch)
	{
		return ExceptionUtils.checkException(t, toMatch);
	} // checkException

	/**
	 * @deprecated use ExceptionUtils
	 */
	public static String getStackTrace(Throwable t)
	{
		return ExceptionUtils.getStackTrace(t);
	} // getStackTrace
}

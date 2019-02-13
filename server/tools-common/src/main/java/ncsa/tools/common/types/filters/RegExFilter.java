/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.types.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import ncsa.tools.common.NCSAConstants;
import ncsa.tools.common.UserFacing;
import ncsa.tools.common.util.SystemUtils;

/**
 * There are two options for usage:
 * (a) set RegExp "tags" for the beginning and ending delimiters of
 * the sequence; (b) set a single pattern to match on.
 * <P>
 * 
 * The object has a state which is one of three values: MATCHED, LOOKING -- for tagged matching --, or UNMATCHED. The apply method
 * returns the state of the filter. The result can be accessed by calling "getMatchedSequence."
 * <P>
 * 
 * For tagged matching, apply begins by locating the first sub-string of the character sequence which matches the tags. On
 * consecutive calls to apply, depending on the state of the filter, all or a portion of the sequence is appended to an internal
 * buffer. The filter maintains the remaining portion of the unmatched buffer for subsequent matching.
 * <P>
 * 
 * While the full expressivity of regular expressions is available for use in defining the tags, care must be taken to assure that
 * any eventual expansion of the expression will remain within a predefined tag-size or pattern-size limit (tag size = 128 chars,
 * pattern size = 128k); these can be reset, if necessary, using the provided methods.
 * <P>
 * 
 * To guarantee consistency, if one tag is null, the other is automatically considered to be null; in this case, anything matches.
 * <P>
 * 
 * A match count is maintained for use in the context of a priority queue; it is incremented automatically when state changes to
 * MATCHED. The count can be reset to zero.
 * <P>
 * 
 * This object is not thread-safe.
 * 
 * @see ncsa.tools.common.types.filters.RegExFilterQueue
 * @author Albert L. Rossi
 */
public class RegExFilter implements UserFacing
{
	protected static final Logger logger = Logger.getLogger(RegExFilter.class);

	private static final long serialVersionUID = 10040L;

	protected String startTag;
	protected String endTag;
	protected String pattern;
	protected int lineflag;
	protected int caseflag;
	protected int dotAll;
	protected int maxTagLength;
	protected int maxPatternLength;
	protected boolean includeTags;
	protected boolean matchLines;

	// set internally but publicly accessible
	protected StringBuffer buffer;
	protected String[] matchedSequences;
	protected int state;
	protected long count;

	// internal
	protected Pattern startPattern;
	protected Pattern endPattern;
	protected Pattern untaggedPattern;
	protected int linetype;
	protected boolean initialized;

	/**
	 * Initializes all OgreConstants.BIT_DEFAULT settings.
	 */
	public RegExFilter()
	{
		initialized = false;
		matchLines = true;
		startPattern = null;
		endPattern = null;
		untaggedPattern = null;
		startTag = null;
		endTag = null;
		pattern = null;
		buffer = new StringBuffer(0);
		count = 0;
		state = NCSAConstants.UNMATCHED;
		matchedSequences = null;
		linetype = SystemUtils.isUNIX() ? Pattern.UNIX_LINES : NCSAConstants.BIT_DEFAULT;
		lineflag = NCSAConstants.BIT_DEFAULT;
		caseflag = NCSAConstants.BIT_DEFAULT;
		maxTagLength = NCSAConstants.MAX_TAG_LENGTH;
		maxPatternLength = NCSAConstants.MAX_PATTERN_LENGTH;
		includeTags = false;
	} // constructor

	/**
	 * @param s
	 *            tag demarcating the beginning of a qualifying sequence.
	 * 
	 */
	public void setStartTag(String s)
	{
		startTag = s;
		pattern = null;
		untaggedPattern = null;
	} // setStartPattern

	/**
	 * @param s
	 *            tag demarcating the end of a qualifying sequence.
	 * 
	 */
	public void setEndTag(String s)
	{
		endTag = s;
		pattern = null;
		untaggedPattern = null;
	} // setEndPattern

	/**
	 * @param s
	 *            a java.util.regex pattern to match.
	 * 
	 */
	public void setPattern(String s)
	{
		pattern = s;
		startTag = null;
		endTag = null;
		startPattern = null;
		endPattern = null;
	} // setPattern

	/**
	 * @param b
	 *            true will set the matcher's flag to case-insensitive.
	 * 
	 */
	public void setCaseInsensitive(boolean b)
	{
		if (b)
			caseflag = Pattern.CASE_INSENSITIVE;
		else
			caseflag = NCSAConstants.BIT_DEFAULT;
	} // setCase

	/**
	 * @param s
	 *            options: "single-line" = treat match as one line;
	 *            "multiline" = treat match as multiple lines.
	 * 
	 * @throws NCSAException
	 *             if not given one of the above options.
	 * 
	 */
	public void setLine(String s) throws IllegalArgumentException
	{
		if (s.equalsIgnoreCase("single-line"))
			lineflag = NCSAConstants.BIT_DEFAULT;
		else if (s.equalsIgnoreCase("multiline"))
			lineflag = Pattern.MULTILINE;
		else
			throw new IllegalArgumentException("setLine option not recognized");
	} // setLine

	public void setDotAll(boolean b)
	{
		if (b)
			dotAll = Pattern.DOTALL;
		else
			dotAll = NCSAConstants.BIT_DEFAULT;
	}

	/**
	 * @param i
	 *            maximum length (in bytes) of a tag (DEFAULT = 128).
	 * 
	 */
	public void setMaxTagLength(int i)
	{
		maxTagLength = i;
	}

	/**
	 * @param i
	 *            maximum length (in bytes) of a pattern
	 *            (DEFAULT = 128000).
	 * 
	 */
	public void setMaxPatternLength(int i)
	{
		maxPatternLength = i;
	}

	/**
	 * @param b
	 *            true means the tags should be returned with the
	 *            matched sequence (DEFAULT = false).
	 * 
	 */
	public void setIncludeTags(boolean b)
	{
		includeTags = b;
	}

	public boolean getMatchLines()
	{
		return matchLines;
	}

	public void setMatchLines(boolean matchLines)
	{
		this.matchLines = matchLines;
	}

	public String getEndTag()
	{
		return endTag;
	}

	public String getPattern()
	{
		return pattern;
	}

	public String getStartTag()
	{
		return startTag;
	}

	public boolean getCaseInsensitive()
	{
		if (caseflag == Pattern.CASE_INSENSITIVE)
			return true;
		return false;
	}

	public String getLine()
	{
		if (lineflag == Pattern.MULTILINE)
			return "multiline";
		return "single-line";
	}

	/**
	 * @return current maximum in bytes.
	 * 
	 */
	public int getMaxTagLength()
	{
		return maxTagLength;
	}

	/**
	 * @return current maximum in bytes.
	 * 
	 */
	public int getMaxPatternLength()
	{
		return maxPatternLength;
	}

	/**
	 * @return whether tags are included in the returned sequence.
	 * 
	 */
	public boolean getIncludeTags()
	{
		return includeTags;
	}

	/**
	 * @return NCSAConstant integer value.
	 */
	public int getState()
	{
		return state;
	}

	/**
	 * @return the result of the call to apply.
	 * 
	 */
	public String[] getMatchedSequences()
	{
		return matchedSequences;
	}

	/**
	 * @return String representation of filter state.
	 */
	public String getStateString()
	{
		String current = null;
		if (state == NCSAConstants.MATCHED)
			current = "MATCHED";
		else if (state == NCSAConstants.LOOKING)
			current = "LOOKING";
		else if (state == NCSAConstants.UNMATCHED)
			current = "UNMATCHED";
		return current;
	} // getState

	/**
	 * @return current number of successful matches using this filter.
	 * 
	 */
	public long getCount()
	{
		return count;
	}

	/**
	 * Sets matched sequences to null and state to UNMATCHED.
	 */
	public void resetState()
	{
		state = NCSAConstants.UNMATCHED;
		matchedSequences = null;
	} // resetState

	/**
	 * Sets count to 0.
	 */
	public void resetCount()
	{
		count = 0;
	}

	/**
	 * If called on an already MATCHED filter,
	 * the filter's state will be reset. Hence it is
	 * advisable to process the match before using the filter
	 * on another sequence.
	 * 
	 * @param charSequence
	 *            the sequence to search for match.
	 * 
	 * @return the state flag (MATCHED, LOOKING, UNMATCHED).
	 * 
	 */
	public int apply(String charSequence)
	{
		if (!initialized)
			initialize();
		if (untaggedPattern != null)
			return findUntagged(charSequence);
		return findTagged(charSequence);
	} // matches

	/*
	 * ////////////////////////////////////////////////////////////////////////
	 * AUXILIARY METHODS //
	 * /////////////////////////////////////////////////////////////////////
	 */

	/**
	 * Compiles the patterns with flag settings.,
	 */
	protected void initialize()
	{
		int flags = getFlags();
		if (pattern != null) {
			if (flags == 0)
				untaggedPattern = Pattern.compile(pattern);
			else
				untaggedPattern = Pattern.compile(pattern, flags);
		} else {
			if (startTag != null) {
				if (flags == 0)
					startPattern = Pattern.compile(startTag);
				else
					startPattern = Pattern.compile(startTag, flags);
			}
			if (endTag != null) {
				if (flags == 0)
					endPattern = Pattern.compile(endTag);
				else
					endPattern = Pattern.compile(endTag, flags);
			}
		}
		initialized = true;
	}

	/**
	 * Does a straight RegExp pattern match on the character sequence.
	 * 
	 * @param charSequence
	 *            in which to find a match.
	 * @return the state flag (MATCHED, LOOKING, UNMATCHED).
	 */
	protected int findUntagged(String charSequence)
	{
		/*
		 * assume that a matched filter will
		 * be immediately processed by the caller
		 */
		if (state == NCSAConstants.MATCHED)
			resetState();

		// if null tag, override state to MATCHED
		if (untaggedPattern == null)
			state = NCSAConstants.MATCHED;

		int len = 0;
		Matcher matcher = null;
		boolean matched = false;

		// append the entire sequence to the internal buffer
		buffer.append(charSequence);

		// on the basis of state, update the buffer
		if (state == NCSAConstants.UNMATCHED) {
			matcher = untaggedPattern.matcher(buffer.toString());
			matched = matcher.find();
		}

		if (!matched) {
			len = buffer.length();

			/*
			 * keep only the last maximum pattern-size - 1 of the buffer
			 * (if the buffer length >= max pattern size and there was
			 * no match, only a portion of the pattern equal to a maximum
			 * of pattern-size-1 characters could be in the last part
			 * of the buffer
			 */
			if (len > (maxPatternLength - 1))
				buffer.delete(0, len - (maxPatternLength - 1));
			return NCSAConstants.UNMATCHED;
		}

		state = NCSAConstants.MATCHED;

		if (matchLines) {
			matchLines(charSequence);
		} else {
			matchAll(matcher);
		}

		logger.debug(charSequence + " matches " + pattern);
		logger.debug("findUntagged, BUFFER IS NOW '" + buffer + "'");
		return state;
	} // findUntagged

	/**
	 * Finds a qualifying sequence by first matching a start tag, then looking
	 * for an end tag. If the buffer is exhausted before an end tag is
	 * encountered, the state returned will be "LOOKING".
	 * 
	 * @param charSequence
	 *            in which to find qualifying sequence.
	 * @return the state flag (MATCHED, LOOKING, UNMATCHED).
	 */
	protected int findTagged(String charSequence)
	{
		// if null tags, return
		if (startPattern == null || endPattern == null)
			return (state = NCSAConstants.UNMATCHED);

		/*
		 * assume that a matched filter will
		 * be immediately processed by the caller
		 */
		matchedSequences = null;
		if (state == NCSAConstants.MATCHED)
			state = NCSAConstants.UNMATCHED;

		// append the entire sequence to the internal buffer
		buffer.append(charSequence);
		matchedSequences = null;
		return matchAllTags();
	} // findTagged

	/**
	 * Composes the values of the flags into a bitmap.
	 * 
	 * @return integer bitmap.
	 */
	protected int getFlags()
	{
		return linetype | lineflag | caseflag | dotAll;
	}

	private void matchLines(String charSequence)
	{
		Matcher matcher = null;
		boolean matched = false;
		StringBuffer sb = new StringBuffer(0);
		String[] lines = StringUtils.split(buffer.toString(), NCSAConstants.LINE_SEP);
		buffer.setLength(0);
		int lastLine = lines.length - 1;
		for (int i = 0; i < lastLine; i++) {
			matcher = untaggedPattern.matcher(lines[i]);
			matched = matcher.find();
			if (matched) {
				sb.append(lines[i]).append(NCSAConstants.LINE_SEP);
				count++;
			}
		}

		matcher = untaggedPattern.matcher(lines[lastLine]);
		matched = matcher.find();
		if (matched) {
			sb.append(lines[lastLine]).append(NCSAConstants.LINE_SEP);
			count++;
		} else {
			buffer.append(lines[lastLine]);
			if (charSequence.endsWith(NCSAConstants.LINE_SEP))
				buffer.append(NCSAConstants.LINE_SEP);
		}

		matchedSequences = new String[] { sb.toString() };
	}

	private void matchAll(Matcher matcher)
	{
		int end = matcher.end();
		while (matcher.find())
			end = matcher.end();
		matchedSequences = new String[] { buffer.substring(0, end) };
		buffer.delete(0, end);
		int len = buffer.length();
		if (len > (maxPatternLength - 1))
			buffer.delete(0, len - (maxPatternLength - 1));
	}

	private int matchAllTags()
	{
		int mark = 0;
		String token = null;
		String next = null;
		Matcher matcher = null;
		boolean matched = false;
		List sequences = new ArrayList();
		int len = 0;

		while (0 < (len = buffer.length())) {

			if (state == NCSAConstants.UNMATCHED) {
				matcher = startPattern.matcher(buffer.toString());
				matched = matcher.find();
				if (matched) {
					state = NCSAConstants.LOOKING;
					token = matcher.group(0);
					mark = buffer.indexOf(token);
					if (!includeTags)
						mark += token.length();
					// put the start tag at the beginning of the buffer
					buffer.delete(0, mark);
				} else {
					if (len > (maxTagLength - 1)) {
						/*
						 * keep only the last maximum tag-size - 1 of the buffer (if
						 * the buffer length >= max tag length and there was no match,
						 * only a portion of the tag equal to a maximum of tag-size-1
						 * characters could be in the last part of the buffer
						 */
						buffer.delete(0, len - (maxTagLength - 1));
					}
					break;
				}
			}

			if (state == NCSAConstants.LOOKING) {
				matcher = endPattern.matcher(buffer.toString());
				matched = matcher.find();
				if (matched) {
					state = NCSAConstants.MATCHED;
					token = matcher.group(0);
					mark = buffer.indexOf(token);
					if (includeTags)
						mark += token.length();
					len = buffer.length();
					next = buffer.substring(mark, len);
					buffer.delete(mark, len);
				} else {
					break;
				}
			}

			if (state == NCSAConstants.MATCHED) {
				logger.debug("got matched tag sequence " + buffer);
				sequences.add(buffer.toString());
				buffer.setLength(0);
				buffer.append(next);
				count++;
				state = NCSAConstants.UNMATCHED;
			}
		}

		logger.debug("matched sequences " + sequences);

		if (!sequences.isEmpty()) {
			matchedSequences = (String[]) sequences.toArray(new String[0]);
			return NCSAConstants.MATCHED;
		}

		return state;
	}

	public void initializeFromElement(Element element)
	{
		Attribute attr = element.attribute("pattern");
		if (attr != null)
			setPattern(attr.getValue());
		attr = element.attribute("startTag");
		if (attr != null)
			setStartTag(attr.getValue());
		attr = element.attribute("endTag");
		if (attr != null)
			setEndTag(attr.getValue());
		attr = element.attribute("line");
		if (attr != null) {
			try {
				setLine(attr.getValue());
			} catch (IllegalArgumentException ignored) {
			}
		}
		attr = element.attribute("caseInsensitive");
		if (attr != null)
			setCaseInsensitive(Boolean.valueOf(attr.getValue()).booleanValue());
		attr = element.attribute("includeTags");
		if (attr != null)
			setIncludeTags(Boolean.valueOf(attr.getValue()).booleanValue());
		attr = element.attribute("matchLines");
		if (attr != null)
			setMatchLines(Boolean.valueOf(attr.getValue()).booleanValue());
		attr = element.attribute("maxTagLength");
		if (attr != null)
			setMaxTagLength(Integer.parseInt(attr.getValue()));
		attr = element.attribute("maxPatternLength");
		if (attr != null)
			setMaxPatternLength(Integer.parseInt(attr.getValue()));
	}

	public Element asElement()
	{
		Element element = DocumentHelper.createElement("regex-filter");
		if (pattern != null)
			element.addAttribute("pattern", pattern);
		else {
			if (startTag != null)
				element.addAttribute("startTag", startTag);
			if (endTag != null)
				element.addAttribute("endTag", endTag);
		}
		if (getCaseInsensitive())
			element.addAttribute("caseInsensitive", "" + true);
		if (getIncludeTags())
			element.addAttribute("includeTags", "" + true);
		if (getMatchLines())
			element.addAttribute("matchLines", "" + true);
		if (lineflag != NCSAConstants.BIT_DEFAULT)
			element.addAttribute("line", getLine());
		if (maxTagLength != NCSAConstants.MAX_TAG_LENGTH)
			element.addAttribute("maxTagLength", "" + maxTagLength);
		if (maxPatternLength != NCSAConstants.MAX_PATTERN_LENGTH)
			element.addAttribute("maxPatternLength", "" + maxPatternLength);

		return element;
	}
}

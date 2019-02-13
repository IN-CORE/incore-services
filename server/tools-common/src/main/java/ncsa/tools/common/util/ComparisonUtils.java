/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import ncsa.tools.common.NCSAConstants;
import ncsa.tools.common.exceptions.FailedComparisonException;
import ncsa.tools.common.exceptions.FileReadException;
import ncsa.tools.common.exceptions.TypeConversionException;
import ncsa.tools.common.types.Predicate;
import ncsa.tools.common.types.filters.ClassFilter;
import ncsa.tools.common.types.filters.MatchClause;
import ncsa.tools.common.types.filters.MatchFilter;
import ncsa.tools.common.types.filters.MatchStatement;
import ncsa.tools.common.types.filters.TypeFilter;

/**
 * Wrapper static methods for doing arbitrary object comparisons. Also includes
 * static method for matching filter against a map.
 * 
 * @author Albert L. Rossi
 */
public class ComparisonUtils
{
	/**
	 * Static utility class; cannot be constructed.
	 */
	private ComparisonUtils()
	{
	}

	/**
	 * Compares a File to either another File, a Long or a String; checks the
	 * type parameter for the semantics of the comparison.
	 * 
	 * @see #applyFileComparator
	 */
	public static boolean compare(Object arg1, Object arg2, int type, int comparator) throws FailedComparisonException
	{

		if (arg1 instanceof File || arg2 instanceof File) {
			Throwable thrown = null;
			try {
				return applyFileComparator(arg1, arg2, type, comparator);
			} catch (IllegalArgumentException t) {
				thrown = t;
			} catch (FileReadException t) {
				thrown = t;
			}

			throw new FailedComparisonException(" file comparison failed, args: " + arg1 + ", " + arg2 + ", type " + type + ", comparator "
					+ comparator, thrown);
		}

		throw new FailedComparisonException("cannot apply file comparison to args: " + arg1 + ", " + arg2);
	} // compare( file, file, int, int )

	/**
	 * First attempts any primitive cast indicated. Then type-checks the two
	 * arguments. If both arguments are null, they are considered equal. If one
	 * of the two arguments is null, they are considered not equal (all other
	 * comparisons being invalid). According to the types of the args, the
	 * appropriate comparison is applied.
	 * 
	 * @param arg1
	 *            to compare
	 * @param arg2
	 *            to compare to.
	 * @param comparator
	 *            to use.
	 * @param cast
	 *            class to cast to (for primitive conversion; can be <code>null</code>).
	 * @return result of the comparison.
	 */
	public static boolean compare(Object arg1, Object arg2, int comparator, Class cast) throws FailedComparisonException
	{
		if (arg1 instanceof File || arg2 instanceof File)
			throw new FailedComparisonException("method cannot be used for file comparison; " + "use compare( Object, Object, int, int ) ");

		// if cast is set, attempt the cast on both args
		if (cast != null) {
			try {
				arg1 = TypeUtils.convertPrim(cast, arg1);
				arg2 = TypeUtils.convertPrim(cast, arg2);
			} catch (TypeConversionException t) {
				throw new FailedComparisonException(arg1 + ", " + arg2 + ", " + " comparator " + comparator, t);
			}
		}

		// check to see if the types match
		if (!TypeUtils.typesMatch(arg1, arg2) && !TypeUtils.typesMatch(arg1, arg2, File.class, String.class)
				&& !TypeUtils.typesMatch(arg1, arg2, File.class, Long.class))
			throw new FailedComparisonException("ECompare, argument types do not match:" + arg1 + "(" + arg1.getClass().getName()
					+ ") and " + arg2 + "(" + arg2.getClass().getName() + ")");

		// if both arguments are null, we consider them equal
		if (arg1 == null && arg2 == null) {
			if (comparator == NCSAConstants.EQ || comparator == NCSAConstants.EQUALS || comparator == NCSAConstants.GE
					|| comparator == NCSAConstants.LE) {
				return true;
			}
			return false;
		}

		// if one of the arguments is null, only NE / NEQUALS is true
		if (arg1 == null || arg2 == null) {
			if (comparator == NCSAConstants.NE || comparator == NCSAConstants.NEQUALS) {
				return true;
			}
			return false;
		}

		// apply comparator
		return applyObjectComparator(arg1, arg2, comparator);
	} // compare

	/**
	 * @param s
	 *            string representation.
	 * @return comparator as an integer value.
	 */
	public static int getComparatorValue(String s) throws IllegalArgumentException
	{
		if (s.equals("=") || s.equals("==") || s.equalsIgnoreCase("eq"))
			return NCSAConstants.EQ;
		else if (s.equals("!=") || s.equals("<>") || s.equalsIgnoreCase("ne"))
			return NCSAConstants.NE;
		else if (s.equalsIgnoreCase("equals"))
			return NCSAConstants.EQUALS;
		else if (s.equalsIgnoreCase("nequals"))
			return NCSAConstants.NEQUALS;
		else if (s.equals(">") || s.equalsIgnoreCase("gt") || s.equalsIgnoreCase("greaterthan"))
			return NCSAConstants.GT;
		else if (s.equals("<") || s.equalsIgnoreCase("lt") || s.equalsIgnoreCase("lessthan"))
			return NCSAConstants.LT;
		else if (s.equals(">=") || s.equalsIgnoreCase("ge") || s.equalsIgnoreCase("greaterthanorequalto"))
			return NCSAConstants.GE;
		else if (s.equals("<=") || s.equalsIgnoreCase("le") || s.equalsIgnoreCase("lessthanorequalto"))
			return NCSAConstants.LE;
		else if (s.equalsIgnoreCase("matches"))
			return NCSAConstants.MATCHES;
		else if ((s.equalsIgnoreCase("nmatches")) || (s.equalsIgnoreCase("notmatches")))
			return NCSAConstants.NMATCHES;
		else
			throw new IllegalArgumentException(s + " is not a recognized comparator");
	} // getComparatorValue

	/**
	 * @param comparatorValue
	 *            int value.
	 * @return a normalized string representation of the comparator.
	 */
	public static String getComparator(int comparatorValue)
	{
		switch (comparatorValue) {
		case NCSAConstants.EQ:
			return "EQ";
		case NCSAConstants.EQUALS:
			return "EQUALS";
		case NCSAConstants.NE:
			return "NE";
		case NCSAConstants.NEQUALS:
			return "NEQUALS";
		case NCSAConstants.GT:
			return "GT";
		case NCSAConstants.GE:
			return "GE";
		case NCSAConstants.LT:
			return "LT";
		case NCSAConstants.LE:
			return "LE";
		case NCSAConstants.MATCHES:
			return "MATCHES";
		case NCSAConstants.NMATCHES:
			return "NMATCHES";
		default:
			return "EQUALS";
		}
	} // getComparator

	/**
	 * Checks to see if the class of the type is assignable from the given class.
	 * 
	 * @param filter
	 *            to match.
	 * @param clzz
	 *            to check against filter.
	 * @return if the filter is <code>null</code>, returns true; if clzz
	 *         satisfies filter, returns true; else returns false.
	 */
	public static boolean matches(ClassFilter filter, Class clzz)
	{
		if (filter == null)
			return true;
		Class fclzz = filter.getType();
		if (fclzz == null)
			return true;
		return fclzz.isAssignableFrom(clzz);
	} // matches

	/**
	 * Checks to see if the type strings are equal (case-insensitive).
	 * 
	 * @param filter
	 *            to match.
	 * @param type
	 *            to check against filter.
	 * @return if the filter is <code>null</code>, returns true; if type
	 *         satisfies filter, returns true; else returns false.
	 */
	public static boolean matches(TypeFilter filter, String type)
	{
		if (filter == null)
			return true;
		String ftype = filter.getType();
		if (ftype == null)
			return true;
		return ftype.equalsIgnoreCase(type);
	} // matches

	/**
	 * Checks the match filter against the map of properties.
	 * 
	 * @param filter
	 *            to match.
	 * @param properties
	 *            map of properties against which to match.
	 * @return if the filter is <code>null</code>, returns true; if the filter
	 *         is empty (no clauses), returns the negation of the filter's 'not'
	 *         field; if property map satisfies filter, returns true; else
	 *         returns false.
	 * @throws FailedComparisonException
	 */
	public static boolean matches(MatchFilter filter, Map properties) throws FailedComparisonException
	{
		return matches(filter, properties, false);
	}

	/**
	 * Checks the match filter against the map of properties.
	 * 
	 * @param filter
	 *            to match.
	 * @param properties
	 *            map of properties against which to match.
	 * @param convertTypes
	 *            whether to try to flexibly convert the types to match String/numeric values
	 * @return if the filter is <code>null</code>, returns true; if the filter
	 *         is empty (no clauses), returns the negation of the filter's 'not'
	 *         field; if property map satisfies filter, returns true; else
	 *         returns false.
	 * @throws FailedComparisonException
	 */
	public static boolean matches(MatchFilter filter, Map properties, boolean convertTypes) throws FailedComparisonException
	{
		if (filter == null)
			return true;
		List clauses = filter.getClauses();
		if (clauses.size() == 0)
			return !filter.getNot();
		boolean filterNegation = filter.getNot();
		for (ListIterator cit = clauses.listIterator(); cit.hasNext();) {
			MatchClause clause = (MatchClause) cit.next();
			List statements = new ArrayList(clause.getElements());
			if (clause.getOrderedConstraint() != null)
				statements.add(clause.getOrderedConstraint());
			boolean qualifies = true;
			for (ListIterator sit = statements.listIterator(); sit.hasNext();) {
				MatchStatement statement = (MatchStatement) sit.next();
				String subject = statement.getSubject();
				Predicate predicate = statement.getPredicate();
				boolean valid = properties.containsKey(subject) && predicate.isValid(properties.get(subject), convertTypes);
				if (!valid) {
					qualifies = false; // statements are conjunctions
					break;
				}
			}
			if (qualifies)
				return !filterNegation; // clauses are disjunctions
		}
		return filterNegation;
	} // matches

	/*
	 * ////////////////////////////////////////////////////////////////////////
	 * AUXILIARY METHODS //
	 * /////////////////////////////////////////////////////////////////////
	 */

	/**
	 * According to the option set, tests for length, timestamp, pathname or
	 * content matching or inequality.
	 * 
	 * @return result of the comparison.
	 * @throws FileNotFoundException
	 * @throws FileReadException
	 */
	private static boolean applyFileComparator(Object arg1, Object arg2, int fileComparison, int comparator)
			throws IllegalArgumentException, FileReadException
	{
		String s1 = null;
		String s2 = null;
		Long l1 = null;
		Long l2 = null;
		boolean result = false;

		// assumes two non-null arguments
		switch (fileComparison) {
		case NCSAConstants.LENGTH:
			if (arg1 instanceof String || arg2 instanceof String)
				throw new IllegalArgumentException("Strings cannot be used in length comparisons");
			l1 = (arg1 instanceof Long ? ((Long) arg1) : new Long(((File) arg1).length()));
			l2 = (arg2 instanceof Long ? ((Long) arg2) : new Long(((File) arg2).length()));
			return applyComparator(l1, l2, comparator);
		case NCSAConstants.TIMESTAMP:
			if (arg1 instanceof String || arg2 instanceof String)
				throw new IllegalArgumentException("Strings cannot be used in timestamp comparisons");
			l1 = (arg1 instanceof Long ? ((Long) arg1) : new Long(((File) arg1).lastModified()));
			l2 = (arg2 instanceof Long ? ((Long) arg2) : new Long(((File) arg2).lastModified()));
			return applyComparator(l1, l2, comparator);
		case NCSAConstants.PATH:
			if (arg1 instanceof Long || arg2 instanceof Long)
				throw new IllegalArgumentException("Longs cannot be used in path comparisons");
			switch (comparator) {
			case NCSAConstants.EQUALS:
				if (arg1 instanceof String) {
					result = new File((String) arg1).equals(arg2);
					return result;
				} else if (arg2 instanceof String) {
					result = ((File) arg1).equals(new File((String) arg2));
					return result;
				}
				break;
			case NCSAConstants.NEQUALS:
				if (arg1 instanceof String) {
					result = !new File((String) arg1).equals(arg2);
					return result;
				} else if (arg2 instanceof String) {
					result = !((File) arg1).equals(new File((String) arg2));
					return result;
				}
				break;
			}
			s1 = (arg1 instanceof String ? (String) arg1 : ((File) arg1).getAbsolutePath());
			s2 = (arg2 instanceof String ? (String) arg2 : ((File) arg2).getAbsolutePath());
			return applyComparator(s1, s2, comparator);
		case NCSAConstants.CONTENTS:
			/*
			 * not absolutely scalable, but follows the model of other Ant
			 * tasks, where files are compared in memory
			 */
			if (arg1 instanceof Long || arg2 instanceof Long)
				throw new IllegalArgumentException("Longs cannot be used in file content comparisons");
			if (arg1 instanceof String) {
				s1 = (String) arg1;
			} else {
				byte[] b1 = FileUtils.readBytes((File) arg1, 0, NCSAConstants.UNDEFINED);
				s1 = new String(b1);
			}
			if (arg2 instanceof String) {
				s2 = (String) arg2;
			} else {
				byte[] b2 = FileUtils.readBytes((File) arg2, 0, NCSAConstants.UNDEFINED);
				s2 = new String(b2);
			}
			return applyComparator(s1, s2, comparator);
		default:
			return false;
		}
	} // applyFileComparator

	/**
	 * If the arguments are comparable, then applyComparator is called; else if
	 * the comparator is an equivalence operator, this is tested directly;
	 * otherwise, the stringified object is compared.
	 * 
	 * @return result of comparison.
	 */
	private static boolean applyObjectComparator(Object arg1, Object arg2, int comparator)
	{
		boolean result = false;

		// assumes two non-null arguments of same type
		if (arg1 instanceof Comparable) {
			Comparable c1 = (Comparable) arg1;
			Comparable c2 = (Comparable) arg2;
			return applyComparator(c1, c2, comparator);
		}

		if (arg1 instanceof Boolean) {
			switch (comparator) {
			case NCSAConstants.EQ:
			case NCSAConstants.EQUALS:
				result = arg1.equals(arg2);
				break;
			case NCSAConstants.NE:
			case NCSAConstants.NEQUALS:
				result = !arg1.equals(arg2);
				break;
			default:
				result = false;
			}
		} else {
			switch (comparator) {
			case NCSAConstants.EQ:
				result = arg1 == arg2;
				break;
			case NCSAConstants.EQUALS:
				result = arg1.equals(arg2);
				break;
			case NCSAConstants.NE:
				result = arg1 != arg2;
				break;
			case NCSAConstants.NEQUALS:
				result = !arg1.equals(arg2);
				break;
			case NCSAConstants.MATCHES:
				if ((arg1 instanceof String) && (arg2 instanceof String)) {
					result = ((String) arg1).matches((String) arg2);
				} else {
					result = false;
				}
				break;
			case NCSAConstants.NMATCHES:
				if ((arg1 instanceof String) && (arg2 instanceof String)) {
					result = !((String) arg1).matches((String) arg2);
				} else {
					result = true;
				}
				break;
			default:
				return applyComparator(arg1.toString(), arg2.toString(), comparator);
			}
		}
		return result;
	} // applyObjectComparator

	/**
	 * Applies either ==, 'equals' or 'compareTo' on the objects, according to
	 * operator. If the objects are primitive wrappers, EQ and NE are interpreted
	 * as EQUALS and NEQUALS.
	 * 
	 * @param c1
	 *            value to compare.
	 * @param c2
	 *            value to compare to.
	 * @return result of the comparison.
	 */
	private static boolean applyComparator(Comparable c1, Comparable c2, int comparator)
	{
		boolean result = false;
		// assumes two non-null arguments of same type
		switch (comparator) {
		case NCSAConstants.EQ:
			if (!TypeUtils.isPrimitiveWrapper(c1.getClass()))
				result = c1 == c2;
			else
				result = c1.equals(c2);
			break;
		case NCSAConstants.EQUALS:
			result = c1.equals(c2);
			break;
		case NCSAConstants.NE:
			if (!TypeUtils.isPrimitiveWrapper(c1.getClass()))
				result = c1 != c2;
			else
				result = !c1.equals(c2);
			break;
		case NCSAConstants.NEQUALS:
			result = !c1.equals(c2);
			break;
		case NCSAConstants.GT:
			result = c1.compareTo(c2) > 0;
			break;
		case NCSAConstants.GE:
			result = c1.compareTo(c2) >= 0;
			break;
		case NCSAConstants.LT:
			result = c1.compareTo(c2) < 0;
			break;
		case NCSAConstants.LE:
			result = c1.compareTo(c2) <= 0;
			break;
		case NCSAConstants.MATCHES:
			if ((c1 instanceof String) && (c2 instanceof String)) {
				String first = ((String) c1).trim();
				String second = ((String) c2).trim();
				result = first.matches(second);
			} else {
				result = false;
			}
			break;
		case NCSAConstants.NMATCHES:
			if ((c1 instanceof String) && (c2 instanceof String)) {
				result = !((String) c1).matches((String) c2);
			} else {
				result = true;
			}
			break;
		default:
			result = c1.equals(c2);
		}
		return result;
	} // applyComparator
}

/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang.StringUtils;

import ncsa.tools.common.exceptions.ReflectionException;
import ncsa.tools.common.types.filters.MatchClause;
import ncsa.tools.common.types.filters.MatchFilter;

public class FilterUtils
{
	private FilterUtils()
	{
	}

	/**
	 * Unions the filters into a single filter. If any of the the filters are
	 * null, it returns a timestamp filter with now = 0 (i.e., return
	 * everything).
	 * 
	 * @param filters
	 *            to be unioned.
	 * @return single filter which is the union of the filters.
	 */
	public static MatchFilter unionFilters(MatchFilter[] filters)
	{
		MatchFilter union = new MatchFilter();
		HashMap seen = new HashMap();
		if (filters != null) {
			for (int i = 0; i < filters.length; i++) {
				if (filters[i] == null)
					continue;
				MatchFilter f = filters[i].normalizeFilter();
				List clauses = f.getClauses();
				for (ListIterator cit = clauses.listIterator(); cit.hasNext();) {
					MatchClause clause = (MatchClause) cit.next();
					String key = clause.toString();
					if (seen.containsKey(key))
						continue;
					seen.put(key, key);
					union.addClause(clause);
				}
			}
		}
		return union;
	} // unionFilters

	/**
	 * Unions the filters into a single filter.
	 * 
	 * @param filters
	 *            to be unioned.
	 * @return single filter which is the union of the filters.
	 */
	public static MatchFilter unionFilters(List filters)
	{
		return unionFilters((MatchFilter[]) filters.toArray(new MatchFilter[0]));
	} // union

	/**
	 * Can be used to create filter directly from expression of the form: <br>
	 * STR := NOT? CL ( || CL )*<br>
	 * CL := ST ( && ST )*<br>
	 * ST := TYPE NAME CMP VALUE<br>
	 * TYPE := {string, int, long, double, boolean}<br>
	 * CMP := ==, equals, <, >, <=, >=, !=.<br>
	 * NAME := the name of some property or field which will be accessed<br>
	 * VALUE := the string value (possibly converted) it should be compared to
	 */
	public static MatchFilter buildFilter(String expression) throws ClassNotFoundException, ReflectionException
	{
		boolean not = false;

		if (expression == null)
			return new MatchFilter();

		if (expression.startsWith("!")) {
			not = true;
			expression = expression.substring(1);
		}

		expression = StringUtils.replace(expression, " OR ", " || ");
		String[] clauses = StringUtils.split(expression, "||");

		if (clauses == null || clauses.length == 0)
			return new MatchFilter();

		List[] subjects = new List[clauses.length];
		List[] values = new List[clauses.length];
		List[] comparators = new List[clauses.length];

		for (int i = 0; i < clauses.length; i++) {
			List types = new ArrayList();
			subjects[i] = new ArrayList();
			values[i] = new ArrayList();
			comparators[i] = new ArrayList();
			parseRule(clauses[i], new List[] { types, subjects[i], comparators[i], values[i] });
			replaceTypes(types, values[i]);
		}

		return createFilter(not, subjects, values, comparators);
	}

	// AUXILIARIES

	private static void parseRule(String rule, List[] lists)
	{
		rule = StringUtils.replace(rule, " AND ", " && ");
		String[] ands = StringUtils.split(rule, "&&");
		for (int i = 0; i < ands.length; i++) {
			String and = ands[i].trim();
			int len = and.length();
			int j = 0;
			StringBuffer sb = new StringBuffer();
			boolean inQuotes = false;
			for (int k = 0; k < len; k++) {
				char c = and.charAt(k);
				if (' ' == c && !inQuotes) {
					lists[j++].add(sb.toString());
					sb.setLength(0);
				} else if ('"' == c || '\'' == c) {
					inQuotes = !inQuotes;
				} else {
					sb.append(c);
				}
			}
			lists[j].add(sb.toString());
		}
	}

	public static Object replaceType(String type, String value) throws ClassNotFoundException, ReflectionException
	{
		Class clzz = TypeUtils.getClassForName(type);
		if (clzz.equals(String.class))
			return value;
		clzz = TypeUtils.getClassForType(clzz);
		return replaceType(clzz, value);
	}

	public static Object replaceType(Class clzz, String value) throws ReflectionException
	{
		// should only be primitives
		return ReflectUtils.callConstructor(clzz, new Class[] { String.class }, new Object[] { value });
	}

	private static void replaceTypes(List types, List values) throws ClassNotFoundException, ReflectionException
	{
		int numVals = values.size();
		for (int i = 0; i < numVals; i++) {
			String type = (String) types.get(i);
			Class clzz = TypeUtils.getClassForName(type);
			if (clzz.equals(String.class))
				continue;
			clzz = TypeUtils.getClassForType(clzz);
			// should only be primitives
			values.set(i, ReflectUtils.callConstructor(clzz, new Class[] { String.class }, new Object[] { values.get(i) }));
		}
	}

	private static MatchFilter createFilter(boolean not, List[] subjects, List[] values, List[] comparators)
	{
		MatchFilter filter = new MatchFilter();
		filter.setNot(not);
		for (int i = 0; i < subjects.length; i++) {
			MatchClause matchClause = new MatchClause();
			String[] clauseSubjects = (String[]) subjects[i].toArray(new String[0]);
			String[] clauseComparators = (String[]) comparators[i].toArray(new String[0]);
			Object[] clauseValues = values[i].toArray();
			for (int j = 0; j < clauseSubjects.length; j++) {
				matchClause.addStatement(clauseSubjects[j], clauseValues[j], clauseComparators[j]);
			}
			filter.addClause(matchClause);
		}
		return filter;
	}
}

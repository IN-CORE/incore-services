/*******************************************************************************
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Contributors:
 *     Shawn Hampton, Jong Lee, Chris Navarro, Nathan Tolbert (NCSA) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package edu.illinois.ncsa.incore.services.fragility.mapping;

import ncsa.tools.common.UserFacing;
import ncsa.tools.common.exceptions.ReflectionException;
import ncsa.tools.common.types.filters.MatchClause;
import ncsa.tools.common.types.filters.MatchFilter;
import ncsa.tools.common.types.filters.MatchStatement;
import ncsa.tools.common.util.FilterUtils;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class PropertyMatch implements UserFacing
{
	private final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

	public final static String TAG_SELF = "property-match"; //$NON-NLS-1$
	public final static String TAG_MAP = "map"; //$NON-NLS-1$
	public final static String TAG_ENTRY = "entry"; //$NON-NLS-1$
	public final static String TAG_KEY = "key"; //$NON-NLS-1$
	public final static String TAG_VALUE = "value"; //$NON-NLS-1$
	public final static String TAG_SUCCESS_VALUE = "success-value"; //$NON-NLS-1$
	public final static String TAG_FILTER = "filter"; //$NON-NLS-1$
	public final static String TAG_STATEMENT = "statement"; //$NON-NLS-1$
	public final static String TAG_RULE = "rule"; //$NON-NLS-1$

	private Map<String, String> map = new HashMap<String, String>();
	private String key;
	private MatchFilter matchFilter;

	public PropertyMatch()
	{
	}

	/**
	 * 
	 * @param key
	 * @param matchFilter
	 */
	public PropertyMatch(String key, MatchFilter matchFilter)
	{
		super();
		this.key = key;
		this.matchFilter = matchFilter;
	}

	/**
	 * 
	 * @param e
	 */
	public PropertyMatch(Element e)
	{
		initializeFromElement(e);
	}

	/**
	 * 
	 * @return
	 */
	public Map<String, String> getMap()
	{
		return map;
	}

	/**
	 * 
	 * @return
	 */
	public String getKey()
	{
		return key;
	}

	/**
	 * 
	 * @param key
	 */
	public void setKey(String key)
	{
		this.key = key;
	}

	/**
	 * 
	 * @param map
	 */
	public void setMap(Map<String, String> map)
	{
		this.map = map;
	}

	/**
	 * 
	 * @param matchFilter
	 */
	public void setMatchFilter(MatchFilter matchFilter)
	{
		this.matchFilter = matchFilter;
	}

	/**
	 * 
	 * @return
	 */
	public MatchFilter getMatchFilter()
	{
		return matchFilter;
	}

	/**
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Element asElement()
	{
		Element e = new DefaultElement(TAG_SELF);

		Element successValueElement = e.addElement(TAG_SUCCESS_VALUE);

		if (map.isEmpty()) {
			successValueElement.addAttribute(TAG_KEY, key);
		} else {
			Element mapElement = successValueElement.addElement(TAG_MAP);
			for (String key : map.keySet()) {
				String value = map.get(key);
				Element entryElement = mapElement.addElement(TAG_ENTRY);
				entryElement.addAttribute(TAG_KEY, key);
				entryElement.addAttribute(TAG_VALUE, value);
			}
		}

		Element filterElement = e.addElement(TAG_FILTER);
		MatchClause[] clauses = (MatchClause[]) matchFilter.getClauses().toArray(new MatchClause[matchFilter.getClauses().size()]);
		for (MatchClause clause : clauses) {
			Element statementElement = filterElement.addElement(TAG_STATEMENT);
			List statements = clause.getSortedStatements();
			for (Iterator iter = statements.iterator(); iter.hasNext();) {
				String statement = (String) iter.next();
				statementElement.addElement(TAG_RULE).setText(statement);
			}
		}

		return e;
	}

	/**
	 * 
	 * @param element
	 */
	public void initializeFromElement(Element element)
	{
		map.clear();

		Element successValueElement = element.element(TAG_SUCCESS_VALUE);
		key = successValueElement.attributeValue(TAG_KEY);

		if (key == null) {
			Element mapElement = successValueElement.element(TAG_MAP);
			if (mapElement != null) {
				Iterator<?> iterator = mapElement.elementIterator(TAG_ENTRY);
				while (iterator.hasNext()) {
					Element entry = (Element) iterator.next();
					String key = entry.attributeValue(TAG_KEY);
					String value = entry.attributeValue(TAG_VALUE);
					map.put(key, value);
				}
			}
		}

		Element filterElement = element.element(TAG_FILTER);
		if (filterElement != null) {
			StringBuffer buffer = new StringBuffer();
			Iterator<?> statementIterator = filterElement.elementIterator(TAG_STATEMENT);
			while (statementIterator.hasNext()) {
				Element statementElement = (Element) statementIterator.next();
				Iterator<?> ruleIterator = statementElement.elementIterator(TAG_RULE);
				while (ruleIterator.hasNext()) {
					Element ruleElement = (Element) ruleIterator.next();
					String rule = ruleElement.getTextTrim();
					buffer.append(rule);
					if (ruleIterator.hasNext())
						buffer.append(" && "); //$NON-NLS-1$
				}

				if (statementIterator.hasNext())
					buffer.append(" || "); //$NON-NLS-1$
			}

			try {
				matchFilter = FilterUtils.buildFilter(buffer.toString());
			} catch (ReflectionException e) {
				logger.error("Failed", e); //$NON-NLS-1$
			} catch (ClassNotFoundException e) {
				logger.error("Failed", e); //$NON-NLS-1$
			}
		}

	}

	/**
	 * 
	 * @return
	 */
	public String toString()
	{
		MatchFilter mf = getMatchFilter();
		List<?> clauses = mf.getClauses();
		// this is using a basic string concatenation..it's not
		// efficient at all...really only for debugging...
		String toString = ""; //$NON-NLS-1$
		for (Object clause : clauses) {
			if (clause instanceof MatchClause) {
				List<?> elements = ((MatchClause) clause).getElements();
				for (Object element : elements) {
					if (element instanceof MatchStatement) {
						toString = toString + ((MatchStatement) element).toString() + ","; //$NON-NLS-1$
					}
				}
			}
		}
		return toString;
	}
}

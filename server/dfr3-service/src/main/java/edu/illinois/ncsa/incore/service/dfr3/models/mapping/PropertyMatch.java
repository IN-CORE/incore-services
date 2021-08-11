/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 *     Shawn Hampton, Jong Lee, Chris Navarro, Nathan Tolbert (NCSA) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.dfr3.models.mapping;

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


public class PropertyMatch implements UserFacing {
    public final static String TAG_SELF = "property-match"; //$NON-NLS-1$
    public final static String TAG_MAP = "map"; //$NON-NLS-1$
    public final static String TAG_ENTRY = "entry"; //$NON-NLS-1$
    public final static String TAG_KEY = "key"; //$NON-NLS-1$
    public final static String TAG_VALUE = "value"; //$NON-NLS-1$
    public final static String TAG_SUCCESS_VALUE = "success-value"; //$NON-NLS-1$
    public final static String TAG_FILTER = "filter"; //$NON-NLS-1$
    public final static String TAG_STATEMENT = "statement"; //$NON-NLS-1$
    public final static String TAG_RULE = "rule"; //$NON-NLS-1$
    private final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    private Map<String, String> map = new HashMap<String, String>();
    private String key;
    private MatchFilter matchFilter;

    public PropertyMatch(String key, MatchFilter matchFilter) {
        super();
        this.key = key;
        this.matchFilter = matchFilter;
    }

    public PropertyMatch(Map<String, String> map, MatchFilter matchFilter) {
        super();
        this.key = null;
        this.map = map;
        this.matchFilter = matchFilter;
    }

    public PropertyMatch(Element element) {
        initializeFromElement(element);
    }

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public MatchFilter getMatchFilter() {
        return matchFilter;
    }

    public void setMatchFilter(MatchFilter matchFilter) {
        this.matchFilter = matchFilter;
    }

    @SuppressWarnings("unchecked")
    public Element asElement() {
        Element element = new DefaultElement(TAG_SELF);

        Element successValueElement = element.addElement(TAG_SUCCESS_VALUE);

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

        Element filterElement = element.addElement(TAG_FILTER);
        MatchClause[] clauses = (MatchClause[]) matchFilter.getClauses().toArray(new MatchClause[matchFilter.getClauses().size()]);
        for (MatchClause clause : clauses) {
            Element statementElement = filterElement.addElement(TAG_STATEMENT);
            List statements = clause.getSortedStatements();
            for (Iterator iter = statements.iterator(); iter.hasNext(); ) {
                String statement = (String) iter.next();
                statementElement.addElement(TAG_RULE).setText(statement);
            }
        }

        return element;
    }

    public void initializeFromElement(Element element) {
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
            StringBuilder buffer = new StringBuilder();
            Iterator<?> statementIterator = filterElement.elementIterator(TAG_STATEMENT);
            while (statementIterator.hasNext()) {
                Element statementElement = (Element) statementIterator.next();
                Iterator<?> ruleIterator = statementElement.elementIterator(TAG_RULE);
                while (ruleIterator.hasNext()) {
                    Element ruleElement = (Element) ruleIterator.next();
                    String rule = ruleElement.getTextTrim();
                    buffer.append(rule);
                    if (ruleIterator.hasNext()) {
                        buffer.append(" && "); //$NON-NLS-1$
                    }
                }

                if (statementIterator.hasNext()) {
                    buffer.append(" || "); //$NON-NLS-1$
                }
            }

            try {
                matchFilter = FilterUtils.buildFilter(buffer.toString());
            } catch (ReflectionException ex) {
                logger.error("Failed", ex); //$NON-NLS-1$
            } catch (ClassNotFoundException ex) {
                logger.error("Failed", ex); //$NON-NLS-1$
            }
        }
    }

    public String toString() {
        MatchFilter mf = getMatchFilter();
        List<?> clauses = mf.getClauses();

        StringBuilder toString = new StringBuilder(); //$NON-NLS-1$
        for (Object clause : clauses) {
            if (clause instanceof MatchClause) {
                List<?> elements = ((MatchClause) clause).getElements();
                for (Object element : elements) {
                    if (element instanceof MatchStatement) {
                        toString.append(element).append(","); //$NON-NLS-1$
                    }
                }
            }
        }

        return toString.toString();
    }
}

/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.types.filters;

import ncsa.tools.common.util.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Object to be used in the context of a MatchFilter.
 * <p>
 * <p>
 * Takes a list of statements which it considers as a conjunction.
 *
 * @author Albert L. Rossi
 * @see MatchFilter
 * @see MatchStatement
 */
public class MatchClause {
    public static final String TAG_SELF = "match-clause";

    private final List elements = new ArrayList();
    private MatchStatement orderedConstraint = null;
    private String key = null;
    private boolean not = false;

    /**
     * @param s a statement, consisting of a subject and predicate.
     */
    public MatchClause addStatement(MatchStatement s) {
        elements.add(s);
        return this;
    }

    public MatchClause addStatement(String subject, Object object, String comparator) {
        return addStatement(new MatchStatement(subject, object, comparator));
    }

    /**
     * @param s a statement, consisting of a subject and predicate,
     *          expressing an ordered relation constraint over the
     *          the entire clause (such as id > 4, timestamp > 4092483, etc. )
     *          and which is susceptible to updating.
     */
    public MatchClause addOrderedConstraint(MatchStatement s) {
        orderedConstraint = s;
        return this;
    }

    public MatchClause addOrderedConstraint(String subject, Object object, String comparator) {
        return addOrderedConstraint(new MatchStatement(subject, object, comparator));
    }

    /**
     * @param c a clause to add, whose 'not' flag will be set to true.
     */
    public MatchClause addNegatedClause(MatchClause c) {
        c.not = true;
        elements.add(c);
        return this;
    }

    /**
     * @return the elements comprising this clause.
     */
    public List getElements() {
        return elements;
    }

    /**
     * @return statement consisting of a subject and predicate,
     * expressing an ordered relation constraint over the
     * the entire clause (such as id > 4, timestamp > 4092483, etc. )
     * and which is susceptible to updating.
     */
    public MatchStatement getOrderedConstraint() {
        return orderedConstraint;
    }

    /**
     * @return true if this clause is a NegatedClause.
     */
    public boolean isNegated() {
        return not;
    }

    /**
     * Computes the key.
     *
     * @return the key for this clause, meaning a lexicographically sorted
     * concatenation of Statement strings.
     */
    public String toString() {
        computeHashKey();
        return key;
    } // toString

    /**
     * Gets a sorted list of the statements as strings and concatenates
     * each using "&&".
     */
    private void computeHashKey() {
        List sorted = new ArrayList();
        if (orderedConstraint != null)
            sorted.add(orderedConstraint.toString());
        for (ListIterator lit = elements.listIterator(); lit.hasNext(); )
            sorted.add(lit.next().toString());
        sorted = ListUtils.sort(sorted, null);
        ListIterator lit = sorted.listIterator();
        StringBuffer sb = new StringBuffer(not ? "!(" : "(");
        if (lit.hasNext()) {
            sb.append(lit.next());
        }
        while (lit.hasNext()) {
            sb.append("&&");
            sb.append(lit.next());
        }
        sb.append(")");
        key = sb.toString();
    } // computeKey

    /**
     * Gets a sorted list of the statements as strings and concatenates
     * each using "&&".
     */
    public String toExpressionString() {
        List sorted = getSortedStatements();

        ListIterator lit = sorted.listIterator();
        StringBuffer sb = new StringBuffer(not ? "!" : "");
        if (lit.hasNext()) {
            sb.append(lit.next());
        }

        while (lit.hasNext()) {
            sb.append(" && ");
            sb.append(lit.next());
        }

        return sb.toString();
    }

    public List getSortedStatements() {
        List sorted = new ArrayList();

        if (orderedConstraint != null)
            sorted.add(orderedConstraint.toExpressionString());
        for (ListIterator lit = elements.listIterator(); lit.hasNext(); ) {
            MatchStatement s = (MatchStatement) lit.next();
            sorted.add(s.toExpressionString());
        }

        sorted = ListUtils.sort(sorted, null);
        return sorted;
    }
}

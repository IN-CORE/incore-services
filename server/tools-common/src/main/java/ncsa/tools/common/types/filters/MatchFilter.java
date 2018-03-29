package ncsa.tools.common.types.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import ncsa.tools.common.Filter;
import ncsa.tools.common.exceptions.FailedComparisonException;
import ncsa.tools.common.util.ComparisonUtils;

/**
 * A filter object which should be general enough for use in multiple situations
 * where matching based on a logical predicate is required.
 * <p>
 * Takes a list of clauses which it considers as a disjunction. The clauses themselves are treated as conjunctions. The entire
 * disjunction can be negated.
 * 
 * @see MatchClause
 * @see MatchStatement
 * @author Albert L. Rossi
 */
public class MatchFilter implements Filter
{
	public static final String TAG_SELF = "match-filter";

	private boolean not = false;
	private List<MatchClause> clauses = new ArrayList();

	/**
	 * @return true if object satisfies filter; false otherwise.
	 * @throws FailedComparisonException
	 */
	public boolean matches(Object o) throws FailedComparisonException
	{
		if (!(o instanceof Map))
			return false;
		return ComparisonUtils.matches(this, (Map) o);
	} // matches

	/**
	 * Same as matches(Object), but if convertType is set to true,
	 * attempts to convert the type of objects to match the desired filter type.
	 * For example, if it expects a double, but gets a string, it will try to parse
	 * the string as a double, then compare it.
	 * 
	 * @return true if object satisfies filter; false otherwise.
	 * @throws FailedComparisonException
	 */
	public boolean matches(Object o, boolean convertType) throws FailedComparisonException
	{
		if (!convertType) {
			return matches(o);
		}

		if (!(o instanceof Map))
			return false;
		return ComparisonUtils.matches(this, (Map) o, true);
	} // matches

	/**
	 * @param b
	 *            if true, negates the entire disjunction of clauses.
	 */
	public MatchFilter setNot(boolean b)
	{
		not = b;
		return this;
	}

	/**
	 * @param c
	 *            a match clause consisting of a conjunction of statements.
	 */
	public MatchFilter addClause(MatchClause c)
	{
		clauses.add(c);
		return this;
	}

	/**
	 * @return whether the disjunction is negated or not.
	 */
	public boolean getNot()
	{
		return not;
	}

	/**
	 * @return the set of clauses in this match.
	 */
	public List getClauses()
	{
		return clauses;
	}

	/**
	 * @return true if there are no clauses.
	 */
	public boolean isEmpty()
	{
		return clauses.isEmpty();
	}

	/**
	 * @return if this filter is negated, a new filter with a single clause, all
	 *         of whose elements are the negated clauses of this filter; else
	 *         returns a shallow copy of the filter.
	 */
	public MatchFilter normalizeFilter()
	{
		MatchFilter filter = new MatchFilter();
		ListIterator lit = clauses.listIterator();
		if (not) {
			MatchClause clause = new MatchClause();
			while (lit.hasNext())
				clause.addNegatedClause((MatchClause) lit.next());
			filter.addClause(clause);
		} else {
			while (lit.hasNext())
				filter.addClause((MatchClause) lit.next());
		}
		return filter;
	} // getNormalizedFilter

	/**
	 * Used to add an ordered constraint on the filter.
	 * 
	 * @param constraint
	 *            to add to all clauses in the filter.
	 */
	public MatchFilter updateFilter(MatchStatement constraint)
	{
		for (ListIterator lit = clauses.listIterator(); lit.hasNext();)
			((MatchClause) lit.next()).addOrderedConstraint(constraint);
		return this;
	} // updateFilter

	/**
	 * @return string representation of this filter.
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		if (not)
			sb.append("!(");
		ListIterator lit = clauses.listIterator();
		if (lit.hasNext())
			sb.append(lit.next());
		while (lit.hasNext()) {
			sb.append("||");
			sb.append(lit.next());
		}
		if (not)
			sb.append(")");
		if (sb.length() == 0)
			return "true";
		return sb.toString();
	} // toString

	/**
	 * @return string representation of this filter.
	 */
	public String toExpressionString()
	{
		StringBuffer sb = new StringBuffer();
		if (not)
			sb.append("! ");
		ListIterator lit = clauses.listIterator();
		if (lit.hasNext()) {
			MatchClause c = (MatchClause) lit.next();
			sb.append(c.toExpressionString());
		}
		while (lit.hasNext()) {
			sb.append("||");
			MatchClause c = (MatchClause) lit.next();
			sb.append(c.toExpressionString());
		}
		if (not)
			sb.append("");
		if (sb.length() == 0)
			return "true";
		return sb.toString();
	} // toString

}

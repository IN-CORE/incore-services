package ncsa.tools.common.types.filters;

import org.apache.commons.lang.StringUtils;

import ncsa.tools.common.types.Predicate;
import ncsa.tools.common.util.TypeUtils;

/**
 * Object to be used in the context of a MatchClause.
 * <p>
 * Represents a logical statement of the type subject - comparator - object. The subject is understood to be not the actual object
 * to be compared, but a name or key for retrieving that object from a map or database.
 * 
 * @see MatchFilter
 * @see MatchClause
 * @author Albert L. Rossi
 */
public class MatchStatement
{
	public static final String TAG_SELF = "match-statement";

	private String subject = null;
	private Predicate predicate = null;

	public MatchStatement()
	{
	}

	public MatchStatement(String subject, Predicate predicate)
	{
		this.subject = subject;
		this.predicate = predicate;
	}

	public MatchStatement(String subject, Object value, String comparator)
	{
		this.subject = subject;
		predicate = new Predicate(value, comparator);
	}

	/**
	 * @param s
	 *            a String which is understood to represent the _name_ or _key_ of
	 *            an object to be looked up in a map of some sort.
	 */
	public MatchStatement setSubject(String s)
	{
		subject = s;
		return this;
	}

	/**
	 * @param p
	 *            representing a comparator + comparand, to be applied to the
	 *            object referenced by the 'subject' name.
	 */
	public MatchStatement addPredicate(Predicate p)
	{
		predicate = p;
		return this;
	}

	/**
	 * @return the subject name / key.
	 */
	public String getSubject()
	{
		return subject;
	}

	/**
	 * @return the predicate.
	 */
	public Predicate getPredicate()
	{
		return predicate;
	}

	/**
	 * @return string representation: ( subject + predicate ).
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(subject);
		sb.append(predicate);
		return sb.toString();
	} // toString

	/**
	 * @return string representation: ( subject + predicate ).
	 */
	public String toExpressionString()
	{
		Class c = predicate.getObject().getClass();
		Class p = TypeUtils.getTypeForClass(c);

		StringBuffer sb = new StringBuffer();

		if (p != null)
			sb.append(p.getName());
		else
			sb.append(c.getName());

		sb.append(" ");

		sb.append(quoteString(subject));
		sb.append(" ");

		sb.append(predicate.getComparator());
		sb.append(" ");

		String s = predicate.getObject().toString();
		if (p == null && !c.equals(String.class)) {
			String n = c.getName();
			int i = s.indexOf(n);
			if (i >= 0)
				sb.append(quoteString(s.substring(i + n.length())));
			else
				sb.append(quoteString(s));
		} else {
			sb.append(quoteString(s));
		}

		return sb.toString();
	} // toString

	private String quoteString(String s)
	{
		if (s.equals("") || StringUtils.contains(s, " ")) {
			return "'" + s + "'";
		}

		return s;
	}
}

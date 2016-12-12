package ncsa.tools.common.comparators;

public class ToStringComparator extends ReversibleComparator
{
	public int compare(Object arg0, Object arg1)
	{
		if (arg0 == null)
			return sign;
		if (arg1 == null)
			return -sign;
		String s0 = arg0.toString();
		String s1 = arg1.toString();
		return s0.compareTo(s1) * sign;
	}
}

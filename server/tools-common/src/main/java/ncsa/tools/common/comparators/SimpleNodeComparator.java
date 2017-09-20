package ncsa.tools.common.comparators;

import ncsa.tools.common.Node;

public class SimpleNodeComparator extends ReversibleComparator
{
	public int compare(Object arg0, Object arg1)
	{
		if (arg0 == null)
			return sign;
		if (arg1 == null)
			return -sign;

		if (arg0 instanceof Node && arg1 instanceof Node) {
			String s0 = ((Node) arg0).getName();
			String s1 = ((Node) arg1).getName();
			return s0.compareTo(s1) * sign;
		}
		return 0;
	}
}

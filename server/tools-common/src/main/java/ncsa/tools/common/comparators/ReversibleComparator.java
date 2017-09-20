package ncsa.tools.common.comparators;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Base class providing sign value which can be used to reverse
 * the comparison values if set (to get descending sort order).
 * 
 * @author Albert L. Rossi
 */
public abstract class ReversibleComparator implements Serializable, Comparator
{
	private static final long serialVersionUID = 1030L;

	protected int sign = 1;

	/**
	 * @param b
	 *            if true, sign for reversing comparison values will be set.
	 */
	public void setDescending(boolean b)
	{
		if (b)
			sign = -1;
		else
			sign = 1;
	}
}

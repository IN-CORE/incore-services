package ncsa.tools.common.comparators;

import ncsa.tools.common.AbstractSerializationComparator;

public class DefaultSerializationComparator extends AbstractSerializationComparator
{
	protected String normalizeArgument(Object arg)
	{
		return defaultNormalization(arg);
	}
}

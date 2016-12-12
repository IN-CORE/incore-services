package ncsa.tools.common;

import org.dom4j.Element;

public interface UserFacing extends ElementConfigurable
{
	public final static String ELEM_PROP = "property"; //$NON-NLS-1$
	public static final String ATTR_NAME = "name"; //$NON-NLS-1$
	public static final String ATTR_VALUE = "value"; //$NON-NLS-1$

	public Element asElement();
}

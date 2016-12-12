package ncsa.tools.common;

import java.io.Serializable;

import org.dom4j.Element;

public interface ElementConfigurable extends Serializable
{
	public void initializeFromElement(Element element);
}

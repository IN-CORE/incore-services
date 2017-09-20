package ncsa.tools.common.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import ncsa.tools.common.UserFacing;

public class ProfileDescription implements UserFacing
{
	private static final long serialVersionUID = 10022L;

	// PERSISTENCE
	private Integer id;

	// INPUT
	private Integer owner;
	private String name;
	private Property[] properties;

	// CONSTRUCTOR

	public ProfileDescription()
	{
		properties = new Property[0];
	}

	public void initializeFromElement(Element element)
	{
		Attribute a = element.attribute("name");
		if (a != null)
			name = a.getValue();
		a = element.attribute("owner");
		if (a != null)
			owner = new Integer(a.getValue());

		Element e = element.element("properties");
		if (e == null)
			e = element;
		List list = e.elements("property");
		if (list != null) {
			List props = new ArrayList();
			for (int i = 0; i < list.size(); i++) {
				Property p = new Property();
				p.initializeFromElement((Element) list.get(i));
				props.add(p);
			}
			properties = (Property[]) props.toArray(new Property[0]);
		}
	}

	public Element asElement()
	{
		Element element = DocumentHelper.createElement("profileDescription");
		if (name != null)
			element.addAttribute("name", name);
		if (owner != null)
			element.addAttribute("owner", owner.toString());
		if (properties != null) {
			Arrays.sort(properties, Property.getSetComparator());
			for (int i = 0; i < properties.length; i++)
				element.add(properties[i].asElement());
		}
		return element;
	}

	// BEAN METHODS

	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public Property[] getProperties()
	{
		return properties;
	}

	public void setProperties(Property[] properties)
	{
		this.properties = properties;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Integer getOwner()
	{
		return owner;
	}

	public void setOwner(Integer owner)
	{
		this.owner = owner;
	}
}

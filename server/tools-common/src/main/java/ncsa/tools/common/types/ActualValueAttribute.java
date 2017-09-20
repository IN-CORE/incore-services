package ncsa.tools.common.types;

import java.io.Serializable;

import ncsa.tools.common.UserFacing;
import ncsa.tools.common.util.ReflectUtils;
import ncsa.tools.common.util.TypeUtils;
import ncsa.tools.common.util.XmlUtils;

/**
 * A general attribute descriptor. Will also work inside an ogre script.
 * 
 * @author Albert L. Rossi
 */
public class ActualValueAttribute extends Property implements Serializable
{
	private static final long serialVersionUID = 1032L;

	private Object actualValue = null;

	public ActualValueAttribute()
	{
	}

	public ActualValueAttribute(Property p)
	{
		super(p);
		// actual value is lazily deserialized ...
	}

	public Property toProperty()
	{
		return toProperty(false);
	}

	public Property toProperty(boolean serialize)
	{
		String pValue = value;

		if (serialize) {
			if (actualValue == null)
				deserializeValue();
			pValue = serializeActualValue();
		}
		return new Property(name, pValue, type, category);
	}

	/**
	 * @param o
	 *            attribute value (dereferenced).
	 */
	public void setActualValue(Object o)
	{
		actualValue = o;
	}

	/**
	 * @return value of attribute.
	 */
	public Object getActualValue()
	{
		return actualValue;
	}

	public void deserializeValue()
	{
		if (type == null) {
			actualValue = value;
		} else {
			try {
				Class clzz = TypeUtils.getClassForName(type);
				if (clzz.equals(String.class)) {
					actualValue = value;
				} else if (clzz.isPrimitive() || TypeUtils.isPrimitiveWrapper(clzz)) {
					actualValue = TypeUtils.convertPrim(clzz, value);
				} else if (UserFacing.class.isAssignableFrom(clzz)) {
					actualValue = ReflectUtils.callConstructor(clzz, null, null);
					XmlUtils.deserializeUserFacingBean(value, (UserFacing) actualValue);
				} else {
					actualValue = XmlUtils.deserializeImplicitBean(value);
				}
			} catch (Throwable t) {
			}
		}
	}

	public String serializeActualValue()
	{
		String serialized = null;
		String actualValueType = null;
		if (actualValue == null)
			return null;
		actualValueType = actualValue.getClass().getName();
		if (TypeUtils.isPrimVal(actualValueType)) {
			serialized = actualValue.toString();
		} else if (actualValue instanceof UserFacing) {
			serialized = XmlUtils.serializeUserFacingBean((UserFacing) actualValue);
		} else {
			try {
				serialized = XmlUtils.serializeImplicitBean(actualValue, null);
			} catch (Throwable t) {
			}
		}
		if (serialized == null)
			serialized = actualValue.toString(); // can't do
													// anything
													// more ...
		return serialized;
	}

	public String toString()
	{
		StringBuffer buffer = new StringBuffer();

		buffer.append("( ");
		buffer.append(name);
		buffer.append(", ");
		buffer.append(value);
		buffer.append(", ");
		buffer.append(type);
		buffer.append(", ");
		buffer.append(category);
		buffer.append(", actualValue: ");
		buffer.append(actualValue);
		buffer.append(" )");

		return buffer.toString();
	}

}

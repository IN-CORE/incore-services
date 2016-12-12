package ncsa.tools.common;

import org.dom4j.Element;

public interface Updateable extends UserFacing
{
	public static final String ID_NAMESPACE = "ncsa.updateable.id";
	public static final String NEW_NAMESPACE = "ncsa.updateable.new";
	public static final String DELETED_NAMESPACE = "ncsa.updateable.deleted";

	/**
	 * The contract is that this method should be called inside the
	 * asElement() method to return the top-level element
	 * with an ID_NAMESPACE whose value is equal to whatever
	 * distinguishes this object from any siblings of the same type.
	 */
	public Element createUpdateableElement();
}

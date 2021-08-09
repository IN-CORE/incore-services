/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.types;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import ncsa.tools.common.Updateable;

public class Property implements Updateable, Serializable {
    private static final long serialVersionUID = 1033L;

    public static final String TAG_SELF = "property";
    protected static final String TAG_NAME = "name";
    protected static final String TAG_VALUE = "value";
    protected static final String TAG_TYPE = "type";
    protected static final String TAG_CATEGORY = "category";

    // PERSISTENCE
    private Integer id;

    // INPUT
    protected String name;
    protected String value;
    protected String type;
    protected String category;

    private volatile Profile parent;

    private static class SetComparator implements Comparator {
        public int compare(Object arg0, Object arg1) {
            if (arg0 == null)
                return -1;
            if (arg1 == null)
                return 1;
            if (arg0 instanceof Property && arg1 instanceof Property) {
                String name1 = ((Property) arg0).getName();
                String name2 = ((Property) arg1).getName();
                if (name1 == null)
                    return -1;
                if (name2 == null)
                    return 1;
                return name1.compareTo(name2);
            }
            return 0;
        }
    }

    private static SetComparator comparator = new SetComparator();

    public static SetComparator getSetComparator() {
        return comparator;
    }

    // CONSTRUCTORS

    public Property() {
    }

    public Property(String string) {
        if (!string.startsWith("(") || !string.endsWith(")")) {
            this.name = string;
        } else {
            String noParens = string.substring(1, string.length() - 1);
            String[] parts = noParens.split(", ");
            if (parts.length > 0)
                this.name = parts[0];
            if (parts.length > 1)
                this.value = parts[1];
            if (parts.length > 2)
                setType(parts[2]);
            if (parts.length > 3)
                setCategory(parts[3]);
        }
    }

    public Property(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public Property(String name, String value, String type) {
        this.name = name;
        this.value = value;
        setType(type);
    }

    public Property(String name, String value, String type, String category) {
        this.name = name;
        this.value = value;
        setType(type);
        setCategory(category);
    }

    public Property(Property property) {
        this.name = property.name;
        this.value = property.value;
        setType(property.type);
        setCategory(property.category);
    }

    // BEAN METHODS

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = StringUtils.stripToNull(type);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = StringUtils.stripToNull(category);
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("(");
        buffer.append(name);
        buffer.append(", ");
        buffer.append(value);
        if (type != null) {
            buffer.append(", ");
            buffer.append(type);
        }
        if (category != null) {
            buffer.append(", ");
            buffer.append(category);
        }
        buffer.append(")");
        return buffer.toString();
    }

    public void initializeFromElement(Element element) {
        Attribute a = element.attribute(TAG_NAME);
        if (a != null)
            name = a.getValue();
        a = element.attribute(TAG_TYPE);
        if (a != null)
            setType(a.getValue());
        a = element.attribute(TAG_CATEGORY);
        if (a != null)
            setCategory(a.getValue());
        a = element.attribute(TAG_VALUE);
        if (a != null)
            value = a.getValue();
        Element e = element.element(TAG_VALUE);
        if (e != null)
            value = e.getText();
    }

    public Element asElement() {
        Element element = createUpdateableElement();
        if (name != null)
            element.addAttribute(TAG_NAME, name);
        if (type != null)
            element.addAttribute(TAG_TYPE, type);
        if (category != null)
            element.addAttribute(TAG_CATEGORY, category);
        if (value != null)
            element.addElement(TAG_VALUE).addText(value).addNamespace(Updateable.ID_NAMESPACE, TAG_VALUE);
        return element;
    }

    public Element createUpdateableElement() {
        return DocumentHelper.createElement(TAG_SELF).addNamespace(Updateable.ID_NAMESPACE, name);
    }

    public boolean equalsProperty(Property p) {
        if (this == p)
            return true;

        String[] ps = {p.getName(), p.getType(), p.getValue(), p.getCategory()};
        String[] s = {name, type, value, category};

        for (int i = 0; i < s.length; i++) {
            if (s[i] == null) {
                if (ps[i] != null)
                    return false;
            } else {
                if (!s[i].equals(ps[i]))
                    return false;
            }
        }
        return true;
    } // equalsProperty

    public static Object getActualValue(Property p) {
        if (p == null)
            return null;
        ActualValueAttribute a = null;
        if (p instanceof ActualValueAttribute) {
            a = (ActualValueAttribute) p;
        } else {
            a = new ActualValueAttribute(p);
        }
        if (a.getActualValue() == null)
            a.deserializeValue();
        return a.getActualValue();
    }

    public Profile getParent() {
        return parent;
    }

    public void setParent(Profile parent) {
        this.parent = parent;
    }
}

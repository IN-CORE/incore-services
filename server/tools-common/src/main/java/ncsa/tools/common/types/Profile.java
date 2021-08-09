/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.types;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ncsa.tools.common.Filter;

public class Profile implements Filter {
    public static final String TAG_SELF = "profile";

    private static class SetComparator implements Comparator {
        public int compare(Object arg0, Object arg1) {
            if (arg0 == null)
                return -1;
            if (arg1 == null)
                return 1;
            if (arg0 instanceof Profile && arg1 instanceof Profile) {
                String name1 = ((Profile) arg0).getName();
                String name2 = ((Profile) arg1).getName();
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

    // PERSISTENCE
    private Integer id;

    // INPUT
    private Integer owner;
    private String name;
    private volatile String type; // for internal use
    private Map propertyMap = new HashMap();

    public Profile() {
    }

    public Profile(Profile profile) {
        id = null;
        owner = null;
        name = profile.getName();
        for (Iterator i = profile.propertyMap.values().iterator(); i.hasNext(); ) {
            Property p = (Property) i.next();
            propertyMap.put(p.getName(), new Property(p));
        }
    }

    // PUBLIC API

    public List getAllPropertiesOfCategory(String category) {
        List l = new ArrayList();

        Iterator iterator = propertyMap.values().iterator();
        while (iterator.hasNext()) {
            Property p = (Property) iterator.next();
            if (p != null && p.getCategory() != null && p.getCategory().equals(category)) {
                l.add(p);
            }
        }

        return l;
    }

    public void addProperty(String name, String value) {
        propertyMap.put(name, new Property(name, value));
    }

    public void addProperty(Property p) {
        if (p != null)
            propertyMap.put(p.getName(), p);
    }

    public String getValue(String name) {
        if (name == null)
            return null;
        Property p = getProperty(name);

        return (p == null) ? null : p.getValue();
    }

    public Property getProperty(String name) {
        if (name == null)
            return null;
        return (Property) propertyMap.get(name);
    }

    // FILTER

    /**
     * @param o must be another Profile object.
     * @return true if o is an improper subset of the propertyMap contained in
     * the calling object; false otherwise.
     */
    public boolean matches(Object o) {
        if (!(o instanceof Profile))
            return false;
        Profile maybeMatches = (Profile) o;

        if (propertyMap == null) {
            if (maybeMatches.getPropertyMap() == null)
                return true;
            return false;
        }

        /*
         * check propertyMap to see that all propertyMap of maybeMatches are
         * contained in this Profile object
         */
        Iterator iterator = maybeMatches.getPropertyMap().keySet().iterator();
        while (iterator.hasNext()) {
            String name = (String) iterator.next();
            Property maybeMatchesProperty = (Property) maybeMatches.getPropertyMap().get(name);

            if (!propertyMap.containsKey(name))
                return false;

            Property constraintMapProperty = getProperty(name);

            if (maybeMatchesProperty.getValue() == null && constraintMapProperty != null)
                return false;
            if (!maybeMatchesProperty.getValue().equals(constraintMapProperty.getValue()))
                return false;
        }

        return true;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("/");
        Iterator iterator = propertyMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            Property p = (Property) propertyMap.get(key);
            buffer.append("(").append(key).append(", ");
            buffer.append(p.getValue()).append(", ").append(p.getType()).append(", ").append(p.getCategory()).append(")");
        }
        buffer.append("/");

        return buffer.toString();
    }

    public ProfileDescription toDescription() {
        ProfileDescription pd = new ProfileDescription();
        pd.setName(name);
        pd.setOwner(owner);

        Property[] properties = new Property[getPropertyMap().keySet().size()];
        int i = 0;

        Iterator iterator = getPropertyMap().values().iterator();
        while (iterator.hasNext()) {
            Property property = (Property) iterator.next();
            properties[i] = property;
            i++;
        }

        pd.setProperties(properties);

        return pd;
    }

    public static Profile createFromDescription(ProfileDescription description) {
        Profile p = new Profile();
        if (description == null)
            return p;
        p.name = description.getName();
        p.owner = description.getOwner();
        Property[] props = description.getProperties();
        int len = props == null ? 0 : props.length;
        for (int i = 0; i < len; i++) {
            p.addProperty(props[i]);
        }

        return p;
    }

    // BEAN METHODS

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setPropertyMap(Map propertyMap) {
        this.propertyMap = propertyMap;
    }

    public Map getPropertyMap() {
        return propertyMap;
    }

    public Integer getOwner() {
        return owner;
    }

    public void setOwner(Integer owner) {
        this.owner = owner;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}

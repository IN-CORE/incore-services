/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.semantic.serializer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public final class ReflectionUtil {
    private ReflectionUtil() {
    }

    public static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null) {
            fields.addAll(getAllFields(type.getSuperclass()));
        }

        return fields;
    }

    public static boolean isPrimitive(Object obj) {
        return isPrimitive(obj.getClass());
    }

    public static boolean isPrimitive(Class obj) {
        return (Boolean.class.equals(obj) || Integer.class.equals(obj) || Long.class.equals(obj)
            || Short.class.equals(obj) || Double.class.equals(obj) || Float.class.equals(obj)
            || Date.class.equals(obj) || String.class.equals(obj) || Character.class.equals(obj)
            || java.net.URI.class.equals(obj));
    }
}

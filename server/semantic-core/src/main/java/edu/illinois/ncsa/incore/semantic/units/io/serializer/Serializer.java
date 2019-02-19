/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.io.serializer;

import edu.illinois.ncsa.incore.semantic.units.io.ReflectionUtil;
import edu.illinois.ncsa.incore.semantic.units.io.annotations.Attribute;
import edu.illinois.ncsa.incore.semantic.units.io.annotations.Element;
import org.apache.jena.rdf.model.*;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.List;

public class Serializer {
    public static String serialize(Object objectToSerialize) {
        if (objectToSerialize.getClass().isAnnotationPresent(Element.class)) {
            Element entityAnnotation = objectToSerialize.getClass().getAnnotation(Element.class);

            Model model = ModelFactory.createDefaultModel();
            Resource resource = model.createResource(entityAnnotation.value());

            List<Field> fields = ReflectionUtil.getAllFields(objectToSerialize.getClass());

            for (Field field : fields) {
                if (field.isAnnotationPresent(Attribute.class)) {
                    Attribute propertyAnnotation = field.getAnnotation(Attribute.class);

                    if (field.getType().isPrimitive()) {
                        try {
                            Property property = ResourceFactory.createProperty("http://test/", propertyAnnotation.value());
                            resource.addProperty(property, field.get(objectToSerialize).toString());
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } else {

                    }
                }
            }

            StringWriter out = new StringWriter();
            model.write(out, "RDF/XML");

            return out.toString();
        } else {
            throw new IllegalArgumentException("Cannot serialize object with no @RDFEntity attribute");
        }
    }
}

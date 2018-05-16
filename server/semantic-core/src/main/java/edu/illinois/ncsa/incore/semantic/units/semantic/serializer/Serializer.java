
package edu.illinois.ncsa.incore.semantic.units.semantic.serializer;

import edu.illinois.ncsa.incore.semantic.units.semantic.annotations.RDFEntity;
import edu.illinois.ncsa.incore.semantic.units.semantic.annotations.RDFProperty;
import org.apache.jena.rdf.model.*;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.List;

public class Serializer {
    public static String serialize(Object objectToSerialize) {
        if (objectToSerialize.getClass().isAnnotationPresent(RDFEntity.class)) {
            RDFEntity entityAnnotation = objectToSerialize.getClass().getAnnotation(RDFEntity.class);

            Model model = ModelFactory.createDefaultModel();
            Resource resource = model.createResource(entityAnnotation.value());

            List<Field> fields = ReflectionUtil.getAllFields(objectToSerialize.getClass());

            for (Field field : fields) {
                if (field.isAnnotationPresent(RDFProperty.class)) {
                    RDFProperty propertyAnnotation = field.getAnnotation(RDFProperty.class);

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

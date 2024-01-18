package edu.illinois.ncsa.incore.service.semantics.model;

import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Embedded
public class Column {

    private String name;

    private String titles;

    @Property("dc:description")
    private String description;

    private String datatype;

    private String required;

    @Property("qudt:unit")
    private String unit;

    public Column() {
    }

    public Column(String name, String titles, String datatype, String description, String unit, String required) {
        this.name = name;
        this.titles = titles;
        this.datatype = datatype;
        this.description = description;
        this.unit = unit;
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public String getTitles() {
        return titles;
    }

    public String getDatatype() {
        return datatype;
    }

    public String getUnit() {
        return unit;
    }

    public String getDescription() { return description; }

    public String getRequired(){ return required; }

    public Map<String, Object> constructOutput() {
        Map<String, Object> map = new HashMap<>();
        Field[] fields = Column.class.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);

            try {
                // change field name from description to dc:description, unit to qudt:unit
                if (field.getName().equals("description")) {
                    map.put("dc:description", this.getDescription());

                } else if (field.getName().equals("unit")) {
                    map.put("qudt:unit", this.getUnit());

                } else {
                    Object value = field.get(this);
                    map.put(field.getName(), value);

                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }
}

package edu.illinois.ncsa.incore.service.semantics.model;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity("Type")
public class Type {
    @Id
    @Property("_id")
    private ObjectId id;

    @Property("@context")
    private List<?> context;

    @Property("dc:license")
    private Document license;

    @Property("dc:title")
    private String title;

    @Property("dc:description")
    private String description;

    private String url;

    @Property("openvocab:versionnumber")
    private String version;

    private Columns tableSchema;

    public Type() {
    }

    public Type(String url, String title, Columns tableSchema, List<?> context) {
        this.url = url;
        this.title = title;
        this.tableSchema = tableSchema;
        this.context = context;
    }

    public String getId() {
        return (id == null) ? null : id.toString();
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public Columns getTableSchema() {
        return tableSchema;
    }

    public String getVersion() { return version; }

    public String getDescription() { return description; }

    public List<?> getContext() { return context; }

    public Document getLicense() {return license; }

    public Map<String, Object> constructOutput() {
        Map<String, Object> map = new HashMap<>();
        Field[] fields = Type.class.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);

            try {
                if (field.getName().equals("context")) {
                    Object value = field.get(this);
                    map.put("@context", value);

                } else if (field.getName().equals("id")) {
                    Object value = field.get(this).toString();
                    map.put(field.getName(), value);

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

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
import java.util.stream.Collectors;

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

    public Document getLicense() { return license; }

    public Map<String, Object> constructOutput() {
        Map<String, Object> map = new HashMap<>();
        Field[] fields = Type.class.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);

            try {
                // change field name from context to @context, license to dc:license, etc
                if (field.getName().equals("context")) {
                    map.put("@context", this.getContext());

                } else if (field.getName().equals("id")) {
                    map.put(field.getName(), this.getId());

                } else if (field.getName().equals("license")) {
                    map.put("dc:license", this.getLicense());

                } else if (field.getName().equals("title")) {
                    map.put("dc:title", this.getTitle());

                } else if (field.getName().equals("description")) {
                    map.put("dc:description", this.getDescription());

                } else if (field.getName().equals("version")) {
                    map.put("openvocab:versionnumber", this.getVersion());

                } else if (field.getName().equals("tableSchema")) {
                    List<Map<String, Object>> updatedTableSchema = this.getTableSchema().getColumns().stream()
                        .map(column -> column.constructOutput())
                        .collect(Collectors.toList());
                    Map<String, Object> columns = new HashMap<>();
                    columns.put("columns", updatedTableSchema);
                    map.put("tableSchema", columns);

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

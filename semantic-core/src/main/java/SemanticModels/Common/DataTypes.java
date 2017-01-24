package SemanticModels.Common;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public enum DataTypes {
    STRING("string", String.class),
    INT("int", int.class),
    DOUBLE("double", double.class),
    DECIMAL("decimal", BigDecimal.class),
    DATETIME("datetime", LocalDateTime.class),
    TIMESTAMP("timestamp", Timestamp.class);

    private String name;
    private Class clazz;

    DataTypes(String name, Class clazz) {
        this.name = name;
        this.clazz = clazz;
    }
}

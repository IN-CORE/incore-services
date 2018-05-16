
package edu.illinois.ncsa.incore.semantic.metamodel.common;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public enum DataType {
    STRING("string", String.class),
    INT("int", int.class),
    DOUBLE("double", double.class),
    DECIMAL("decimal", BigDecimal.class),
    DATETIME("datetime", LocalDateTime.class),
    TIMESTAMP("timestamp", Timestamp.class);

    private String name;
    private Class clazz;

    DataType(String name, Class clazz) {
        this.name = name;
        this.clazz = clazz;
    }
}

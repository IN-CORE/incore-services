
package edu.illinois.ncsa.incore.semantic.metamodel.validation.rules;

public enum ComparatorType {
    Equal("EQ", "Equal", "=", "="),
    GreaterThan("GT", "Greater Than", ">", ">"),
    GreaterThanOrEqual("GE", "Greater Than Or Equal", ">=", "\u2265"),
    LessThan("LT", "Less Than", "<", "<"),
    LessThanOrEqual("LE", "Less Than or Equal", "<=", "\u2264"),
    NotEqual("NE", "Not Equal", "!=", "\u2260");

    private String shortName;
    private String name;
    private String symbol;
    private String unicodeSymbol;

    private ComparatorType(String shortName, String name, String symbol, String unicodeSymbol) {
        this.shortName = shortName;
        this.name = name;
        this.symbol = symbol;
        this.unicodeSymbol = unicodeSymbol;
    }

    public String getShortName() {
        return shortName;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getUnicodeSymbol() {
        return unicodeSymbol;
    }
}

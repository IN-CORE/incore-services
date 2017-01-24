package SemanticModels.Units.Dimension;

import SemanticModels.Units.Common.ISymbolizable;

public abstract class Dimension implements ISymbolizable {
    private final String name;
    protected String unicodeSymbol;

    protected Dimension(String name) {
        this.name = name;
    }

    protected Dimension(String name, String unicodeSymbol) {
        this.name = name;
        this.unicodeSymbol = unicodeSymbol;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getUnicodeSymbol() {
        return this.unicodeSymbol;
    }
}

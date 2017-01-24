package SemanticModels.Units.Dimension;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DerivedDimension extends Dimension {
    private Map<BaseDimension, Integer> powerDimensions = new LinkedHashMap<>();

    public DerivedDimension(String name, Map<BaseDimension, Integer> entries) {
        super(name);
        this.powerDimensions = entries;
        this.unicodeSymbol = getSymbolString();
    }

    public DerivedDimension(String name, BaseDimension... dimension) {
        super(name);

        for (BaseDimension baseDimension : dimension) {
            powerDimensions.put(baseDimension, 1);
        }

        super.unicodeSymbol = getSymbolString();
    }

    public DerivedDimension(String name, BaseDimension dimension, int power) {
        super(name);
        this.powerDimensions.put(dimension, power);
        super.unicodeSymbol = getSymbolString();
    }

    public DerivedDimension(String name, BaseDimension dimension1, int power1, BaseDimension dimension2, int power2) {
        super(name);
        this.powerDimensions.put(dimension1, power1);
        this.powerDimensions.put(dimension2, power2);
        super.unicodeSymbol = getSymbolString();
    }

    public DerivedDimension(String name, BaseDimension dimension1, BaseDimension dimension2, int power2) {
        this(name, dimension1, 1, dimension2, power2);
    }

    public DerivedDimension(String name, BaseDimension dimension1, int power1, BaseDimension dimension2, int power2,
                            BaseDimension dimension3, int power3) {
        super(name);
        this.powerDimensions.put(dimension1, power1);
        this.powerDimensions.put(dimension2, power2);
        this.powerDimensions.put(dimension3, power3);
        super.unicodeSymbol = getSymbolString();
    }

    public DerivedDimension(String name, BaseDimension dimension1, BaseDimension dimension2,
                            BaseDimension dimension3, int power3) {
        this(name, dimension1, 1, dimension2, 1, dimension3, power3);
    }

    public DerivedDimension(String name, BaseDimension dimension1, BaseDimension dimension2, int power2,
                            BaseDimension dimension3, int power3) {
        this(name, dimension1, 1, dimension2, power2, dimension3, power3);
    }

    public DerivedDimension(String name, BaseDimension dimension1, int power1, BaseDimension dimension2, int power2,
                            BaseDimension dimension3, int power3, BaseDimension dimension4, int power4) {
        super(name);
        this.powerDimensions.put(dimension1, power1);
        this.powerDimensions.put(dimension2, power2);
        this.powerDimensions.put(dimension3, power3);
        this.powerDimensions.put(dimension4, power4);
        super.unicodeSymbol = getSymbolString();
    }

    public DerivedDimension(String name, BaseDimension dimension1, BaseDimension dimension2, int power2,
                            BaseDimension dimension3, int power3, BaseDimension dimension4, int power4) {
        this(name, dimension1, 1, dimension2, power2, dimension3, power3, dimension4, power4);
    }

    public DerivedDimension(String name, BaseDimension dimension1, int power1, BaseDimension dimension2,
                            BaseDimension dimension3, int power3, BaseDimension dimension4, int power4) {
        this(name, dimension1, power1, dimension2, 1, dimension3, power3, dimension4, power4);
    }

    public DerivedDimension(String name, BaseDimension dimension1, BaseDimension dimension2,
                            BaseDimension dimension3, int power3, BaseDimension dimension4, int power4) {
        this(name, dimension1, 1, dimension2, 1, dimension3, power3, dimension4, power4);
    }

    public DerivedDimension(String name, BaseDimension dimension1, int power1, BaseDimension dimension2, int power2,
                            BaseDimension dimension3, int power3, BaseDimension dimension4, int power4, BaseDimension dimension5,
                            int power5) {
        super(name);
        this.powerDimensions.put(dimension1, power1);
        this.powerDimensions.put(dimension2, power2);
        this.powerDimensions.put(dimension3, power3);
        this.powerDimensions.put(dimension4, power4);
        this.powerDimensions.put(dimension5, power5);
        super.unicodeSymbol = getSymbolString();
    }

    public DerivedDimension(String name, BaseDimension dimension1, BaseDimension dimension2, int power2, BaseDimension dimension3,
                            int power3, BaseDimension dimension4, int power4, BaseDimension dimension5, int power5) {
        this(name, dimension1, 1, dimension2, power2, dimension3, power3, dimension4, power4, dimension5, power5);
    }

    public DerivedDimension(String name, BaseDimension dimension1, BaseDimension dimension2, BaseDimension dimension3,
                            int power3, BaseDimension dimension4, int power4, BaseDimension dimension5, int power5) {
        this(name, dimension1, 1, dimension2, 1, dimension3, power3, dimension4, power4, dimension5, power5);
    }

    private String getSymbolString() {
        List<String> symbols = this.powerDimensions.keySet()
                                                   .stream()
                                                   .map(BaseDimension::getUnicodeSymbol)
                                                   .collect(Collectors.toList());

        return String.join("\u22c5", symbols);
    }
}

package Semantic.Units;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SIPrefix {
    public static SIPrefix yotta = new SIPrefix("yotta", "Y", "Y", "septillion", "quadrillion", 24);
    public static SIPrefix zetta = new SIPrefix("yotta", "Z", "Z", "sextillion", "thousand trillion", "trilliard", 21);
    public static SIPrefix exa = new SIPrefix("exa", "E", "E", "quintillion", "trillion", 18);
    public static SIPrefix peta = new SIPrefix("peta", "P", "P", "quadrillion", "thousand billion", "billiard", 15);
    public static SIPrefix tera = new SIPrefix("tera", "T", "T", "trillion", "billion", 12);
    public static SIPrefix giga = new SIPrefix("giga", "G", "G", "billion", "thousand million", "milliard", 9);
    public static SIPrefix mega = new SIPrefix("mega", "M", "M", "million", 6);
    public static SIPrefix kilo = new SIPrefix("kilo", "k", "k", "thousand", 3);
    public static SIPrefix hecto = new SIPrefix("hecto", "h", "h", "hundred", 2);
    public static SIPrefix deka = new SIPrefix("deka", "da", "da", "ten", 1);

    public static SIPrefix deci = new SIPrefix("deci", "d", "d", "tenth", -1);
    public static SIPrefix centi = new SIPrefix("centi", "c", "c", "hundredth", -2);
    public static SIPrefix milli = new SIPrefix("milli", "m", "m", "thousandth", -3);
    public static SIPrefix micro = new SIPrefix("micro", "u", "\u00b5", "millionth", -6);
    public static SIPrefix nano = new SIPrefix("nano", "n", "n", "billionth", "thousand millionth", -9);
    public static SIPrefix pico = new SIPrefix("pico", "p", "p", "trillionth", "billionth", -12);
    public static SIPrefix femto = new SIPrefix("femto", "f", "f", "quadrillionth", "thousand billionth", -15);
    public static SIPrefix atto = new SIPrefix("atto", "a", "a", "quintillionth", "trillionth", -18);
    public static SIPrefix zepto = new SIPrefix("zepto", "z", "z", "sextillionth", "thousand trillionth", -21);
    public static SIPrefix yocto = new SIPrefix("yocto", "y", "y", "septillionth", "quadrillionth", -24);

    public static List<SIPrefix> none = new ArrayList<>();

    public static List<SIPrefix> postivePrefixes = new ArrayList<>(Arrays.asList(
            yotta, zetta, exa, peta, tera, giga, mega, kilo, hecto, deka
    ));

    public static List<SIPrefix> negativePrefixes = new ArrayList<>(Arrays.asList(
            yocto, zepto, atto, femto, pico, nano, micro, milli, centi, deci
    ));

    public static List<SIPrefix> allPrefixes = new ArrayList<>(Arrays.asList(
            yotta, zetta, exa, peta, tera, giga, mega, kilo, hecto, deka,
            yocto, zepto, atto, femto, pico, nano, micro, milli, centi, deci
    ));

    private String name;
    private String asciiSymbol;
    private String unicodeSymbol;
    private int scale;

    private SIPrefix(String name, String asciiSymbol, String unicodeSymbol, String word, int scale) {
        this.name = name;
        this.asciiSymbol = asciiSymbol;
        this.unicodeSymbol = unicodeSymbol;

        this.scale = scale;
    }

    private SIPrefix(String name, String asciiSymbol, String unicodeSymbol, String shortScale, String longScale, int scale) {
        this.name = name;
        this.asciiSymbol = asciiSymbol;
        this.unicodeSymbol = unicodeSymbol;

        this.scale = scale;
    }

    private SIPrefix(String name, String asciiSymbol, String unicodeSymbol, String shortScale, String longScale,
                     String longScaleAlternative, int scale) {
        this(name, asciiSymbol, unicodeSymbol, shortScale, longScale, scale);
    }

    public String getName() {
        return name;
    }

    public String getAsciiSymbol() {
        return asciiSymbol;
    }

    public String getUnicodeSymbol() {
        return unicodeSymbol;
    }

    public int getScale() {
        return scale;
    }
}

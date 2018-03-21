
package edu.illinois.ncsa.incore.semantic.units;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SIPrefix extends Prefix {
    private String word;
    private String shortScaleWord;
    private String longScaleWord;
    private String altLongScaleWord;

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

    public static List<Prefix> positivePrefixes = new ArrayList<>(Arrays.asList(
            yotta, zetta, exa, peta, tera, giga, mega, kilo, hecto, deka
    ));

    public static List<Prefix> negativePrefixes = new ArrayList<>(Arrays.asList(
            yocto, zepto, atto, femto, pico, nano, micro, milli, centi, deci
    ));

    public static List<Prefix> allPrefixes = new ArrayList<>(Arrays.asList(
            yotta, zetta, exa, peta, tera, giga, mega, kilo, hecto, deka,
            yocto, zepto, atto, femto, pico, nano, micro, milli, centi, deci
    ));

    private SIPrefix(String name, String asciiSymbol, String unicodeSymbol, String word, int scale) {
        super(name, asciiSymbol, unicodeSymbol, scale, 10);
        this.word = word;
    }

    private SIPrefix(String name, String asciiSymbol, String unicodeSymbol, String shortScale, String longScale, int scale) {
        super(name, asciiSymbol, unicodeSymbol, scale, 10);
        this.shortScaleWord = shortScale;
        this.longScaleWord = longScale;
    }

    private SIPrefix(String name, String asciiSymbol, String unicodeSymbol, String shortScale, String longScale,
                     String longScaleAlternative, int scale) {
        super(name, asciiSymbol, unicodeSymbol, scale, 10);
        this.shortScaleWord = shortScale;
        this.longScaleWord = longScale;
        this.altLongScaleWord = longScaleAlternative;
    }


    public String getWord() {
        return word;
    }

    public String getShortScaleWord() {
        return shortScaleWord;
    }

    public String getLongScaleWord() {
        return longScaleWord;
    }

    public String getAltLongScaleWord() {
        return altLongScaleWord;
    }
}

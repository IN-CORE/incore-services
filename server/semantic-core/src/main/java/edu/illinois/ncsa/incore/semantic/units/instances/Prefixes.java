
package edu.illinois.ncsa.incore.semantic.units.instances;

import edu.illinois.ncsa.incore.semantic.units.Prefix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class Prefixes {
    private Prefixes() {
    }

    public static void initialize() {
    }

    // SI
    public static final Prefix yotta = new Prefix("yotta", "Y", "Y", 10, 24);
    public static final Prefix zetta = new Prefix("zetta", "Z", "Z", 10, 21);
    public static final Prefix exa = new Prefix("exa", "E", "E", 10, 18);
    public static final Prefix peta = new Prefix("peta", "P", "P", 10, 15);
    public static final Prefix tera = new Prefix("tera", "T", "T", 10, 12);
    public static final Prefix giga = new Prefix("giga", "G", "G", 10, 9);
    public static final Prefix mega = new Prefix("mega", "M", "M", 10, 6);
    public static final Prefix kilo = new Prefix("kilo", "k", "k", 10, 3);
    public static final Prefix hecto = new Prefix("hecto", "h", "h", 10, 2);
    public static final Prefix deka = new Prefix("deka", "da", "da", 10, 1);

    public static final Prefix deci = new Prefix("deci", "d", "d", 10, -1);
    public static final Prefix centi = new Prefix("centi", "c", "c", 10, -2);
    public static final Prefix milli = new Prefix("milli", "m", "m", 10, -3);
    public static final Prefix micro = new Prefix("micro", "u", "\u00b5", 10, -6);
    public static final Prefix nano = new Prefix("nano", "n", "n", 10, -9);
    public static final Prefix pico = new Prefix("pico", "p", "p", 10, -12);
    public static final Prefix femto = new Prefix("femto", "f", "f", 10, -15);
    public static final Prefix atto = new Prefix("atto", "a", "a", 10, -18);
    public static final Prefix zepto = new Prefix("zepto", "z", "z", 10, -21);
    public static final Prefix yocto = new Prefix("yocto", "y", "y", 10, -24);

    // JEDEC
    public static final Prefix jedecKilo = new Prefix("jedec_kilo", "kilo", "K", "K", 1024, 1);
    public static final Prefix jedecMega = new Prefix("jedec_mega", "mega", "M", "M", 1024, 2);
    public static final Prefix jedecGiga = new Prefix("jedec_giga", "giga", "G", "G", 1024, 3);

    // IEC
    public static final Prefix kibi = new Prefix("kibi", "Ki", "Ki", 1024, 1);
    public static final Prefix mebi = new Prefix("mebi", "Mi", "Mi", 1024, 2);
    public static final Prefix gibi = new Prefix("gibi", "Gi", "Gi", 1024, 3);
    public static final Prefix tebi = new Prefix("tebi", "Ti", "Ti", 1024, 4);
    public static final Prefix pebi = new Prefix("pebi", "Pi", "Pi", 1024, 5);
    public static final Prefix exbi = new Prefix("exbi", "Ei", "Ei", 1024, 6);
    public static final Prefix zebi = new Prefix("zebi", "Zi", "Zi", 1024, 7);
    public static final Prefix yobi = new Prefix("yobi", "Yi", "Yi", 1024, 8);

    public static final List<Prefix> None = new ArrayList<>();

    public static final List<Prefix> SI = Arrays.asList(yotta, zetta, exa, peta, tera, giga, mega, kilo, hecto, deka,
                                                        yocto, zepto, atto, femto, pico, nano, micro, milli, centi, deci);

    public static final List<Prefix> SIPositive = Arrays.asList(yotta, zetta, exa, peta, tera, giga, mega, kilo, hecto, deka);

    public static final List<Prefix> SINegative = Arrays.asList(yocto, zepto, atto, femto, pico, nano, micro, milli, centi, deci);

    public static final List<Prefix> IEC = Arrays.asList(kibi, mebi, gibi, tebi, pebi, exbi, zebi, yobi);

    // TODO remove the JEDEC prefixes? (will cause name and symbol parsing problems)
    public static final List<Prefix> JEDEC = Arrays.asList(jedecKilo, jedecMega, jedecGiga);

    public static final List<Prefix> Binary = Arrays.asList(kibi, mebi, gibi, tebi, pebi, exbi, zebi, yobi, // IEC
                                                            jedecKilo, jedecMega, jedecGiga, // JEDEC
                                                            kilo, mega, giga, tera, peta, exa, zetta, yotta); // SI

    public static final List<Prefix> All = Arrays.asList(yotta, zetta, exa, peta, tera, giga, mega, kilo, hecto, deka,
                                                         yocto, zepto, atto, femto, pico, nano, micro, milli, centi, deci,
                                                         jedecKilo, jedecMega, jedecGiga,
                                                         kibi, mebi, gibi, tebi, pebi, exbi, zebi, yobi);

    public static final String[] AllNames = new String[]{"yotta", "zetta", "exa", "peta", "tera", "giga", "mega", "kilo", "hecto", "deka",
                                                           "yocto", "zepto", "atto", "femto", "pico", "nano", "micro", "milli", "centi", "deci",
                                                           "kibi", "mebi", "gibi", "tebi", "pebi", "exbi", "zebi", "yobi"};

    public static Optional<Prefix> tryGetByResourceName(String resourceName) {
        return All.stream().filter(prefix -> prefix.getResourceName().equals(resourceName))
                  .findFirst();
    }

    public static Optional<Prefix> tryGetByName(String name) {
        return All.stream().filter(prefix -> prefix.getName().equals(name))
                  .findFirst();
    }

    public static Optional<Prefix> tryGetBySymbol(String symbol) {
        return All.stream().filter(prefix -> prefix.getSymbol().equals(symbol) || prefix.getUnicodeSymbol().equals(symbol))
                  .findFirst();
    }
}

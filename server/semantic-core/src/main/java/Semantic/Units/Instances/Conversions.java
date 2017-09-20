package Semantic.Units.Instances;

import Semantic.Units.Conversion.Engine.ConversionEngine;

public final class Conversions {
    private Conversions() {}

    static {
        ConversionEngine.registerConversion(ImperialUnits.foot, ImperialUnits.thou, 12000);
        ConversionEngine.registerConversion(ImperialUnits.foot, ImperialUnits.inch, 12);
        ConversionEngine.registerConversion(ImperialUnits.yard, ImperialUnits.foot, 3);
        ConversionEngine.registerConversion(ImperialUnits.chain, ImperialUnits.yard, 22);
        ConversionEngine.registerConversion(ImperialUnits.chain, ImperialUnits.foot, 66);
        ConversionEngine.registerConversion(ImperialUnits.furlong, ImperialUnits.chain, 10);
        ConversionEngine.registerConversion(ImperialUnits.furlong, ImperialUnits.foot, 660);
        ConversionEngine.registerConversion(ImperialUnits.mile, ImperialUnits.furlong, 8);
        ConversionEngine.registerConversion(ImperialUnits.mile, ImperialUnits.foot, 5280);
        ConversionEngine.registerConversion(ImperialUnits.league, ImperialUnits.mile, 3);
        ConversionEngine.registerConversion(ImperialUnits.league, ImperialUnits.foot, 15840);
        ConversionEngine.registerConversion(ImperialUnits.fathom, ImperialUnits.yard, 2.02667);
        ConversionEngine.registerConversion(ImperialUnits.fathom, ImperialUnits.foot, 6.08);
        ConversionEngine.registerConversion(ImperialUnits.cable, ImperialUnits.fathom, 100);
        ConversionEngine.registerConversion(ImperialUnits.cable, ImperialUnits.foot, 608);
        ConversionEngine.registerConversion(ImperialUnits.nauticalMile, ImperialUnits.cable, 10);
        ConversionEngine.registerConversion(ImperialUnits.nauticalMile, ImperialUnits.foot, 6080);
        ConversionEngine.registerConversion(USCustomaryUnits.link, ImperialUnits.inch, 7.92);
        ConversionEngine.registerConversion(USCustomaryUnits.link, ImperialUnits.foot, 0.66);
        ConversionEngine.registerConversion(USCustomaryUnits.rod, USCustomaryUnits.link, 25);
        ConversionEngine.registerConversion(USCustomaryUnits.rod, ImperialUnits.foot, 16.5);

        ConversionEngine.registerConversion(ImperialUnits.thou, SIUnits.metre, 0.0000254);
        ConversionEngine.registerConversion(ImperialUnits.inch, SIUnits.metre, 0.0254);
        ConversionEngine.registerConversion(ImperialUnits.foot, SIUnits.metre, 0.3048);
        ConversionEngine.registerConversion(ImperialUnits.yard, SIUnits.metre, 0.9144);
        ConversionEngine.registerConversion(ImperialUnits.chain, SIUnits.metre, 20.1168);
        ConversionEngine.registerConversion(ImperialUnits.furlong, SIUnits.metre, 201.168);
        ConversionEngine.registerConversion(ImperialUnits.mile, SIUnits.metre, 1609.344);
        ConversionEngine.registerConversion(ImperialUnits.league, SIUnits.metre, 4828.032);
        ConversionEngine.registerConversion(ImperialUnits.fathom, SIUnits.metre, 1.853184);
        ConversionEngine.registerConversion(ImperialUnits.cable, SIUnits.metre, 185.3184);
        ConversionEngine.registerConversion(ImperialUnits.nauticalMile, SIUnits.metre, 1853.184);
        ConversionEngine.registerConversion(USCustomaryUnits.link, SIUnits.metre, 0.201168);
        ConversionEngine.registerConversion(USCustomaryUnits.rod, SIUnits.metre, 5.0292);

        // ConversionEngine.registerConversion(bit, bytes, 8.0);
    }

    public static void initialize() {
        // do nothing - this will call the static
        // constructor which will initialize
    }
}

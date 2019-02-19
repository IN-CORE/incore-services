/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.instances;

import edu.illinois.ncsa.incore.semantic.units.conversion.engine.ConversionEngine;
import edu.illinois.ncsa.incore.semantic.units.conversion.operations.MultiplyOffsetOperation;
import edu.illinois.ncsa.incore.semantic.units.conversion.operations.OffsetMultiplyOperation;
import edu.illinois.ncsa.incore.semantic.units.conversion.operations.OffsetOperation;

public final class Conversions {
    private Conversions() {
    }

    static {
        // Length - US Customary
        ConversionEngine.registerConversion(ImperialUnits.foot, ImperialUnits.thou, 12000.0);
        ConversionEngine.registerConversion(ImperialUnits.foot, ImperialUnits.inch, 12.0);
        ConversionEngine.registerConversion(ImperialUnits.yard, ImperialUnits.foot, 3.0);
        ConversionEngine.registerConversion(ImperialUnits.chain, ImperialUnits.yard, 22.0);
        ConversionEngine.registerConversion(ImperialUnits.chain, ImperialUnits.foot, 66.0);
        ConversionEngine.registerConversion(ImperialUnits.furlong, ImperialUnits.chain, 10.0);
        ConversionEngine.registerConversion(ImperialUnits.furlong, ImperialUnits.foot, 660.0);
        ConversionEngine.registerConversion(ImperialUnits.mile, ImperialUnits.furlong, 8.0);
        ConversionEngine.registerConversion(ImperialUnits.mile, ImperialUnits.foot, 5280.0);
        ConversionEngine.registerConversion(ImperialUnits.league, ImperialUnits.mile, 3.0);
        ConversionEngine.registerConversion(ImperialUnits.league, ImperialUnits.foot, 15840.0);
        ConversionEngine.registerConversion(ImperialUnits.fathom, ImperialUnits.yard, 2.02667);
        ConversionEngine.registerConversion(ImperialUnits.fathom, ImperialUnits.foot, 6.08);
        ConversionEngine.registerConversion(ImperialUnits.cable, ImperialUnits.fathom, 100.0);
        ConversionEngine.registerConversion(ImperialUnits.cable, ImperialUnits.foot, 608.0);
        ConversionEngine.registerConversion(ImperialUnits.nauticalMile, ImperialUnits.cable, 10.0);
        ConversionEngine.registerConversion(ImperialUnits.nauticalMile, ImperialUnits.foot, 6080.0);
        ConversionEngine.registerConversion(USCustomaryUnits.link, ImperialUnits.inch, 7.92);
        ConversionEngine.registerConversion(USCustomaryUnits.link, ImperialUnits.foot, 0.66);
        ConversionEngine.registerConversion(USCustomaryUnits.rod, USCustomaryUnits.link, 25);
        ConversionEngine.registerConversion(USCustomaryUnits.rod, ImperialUnits.foot, 16.5);

        // Length - US to SI
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

        // CGS to SI
        ConversionEngine.registerConversion(CGSUnits.abampere, SIUnits.ampere, 10.0);
        //        ConversionEngine.registerConversion(CGSUnits.abhenry, SIDerivedUnits.henry, );
        //        ConversionEngine.registerConversion(CGSUnits.abohm, SIDerivedUnits.ohm, );
        //        ConversionEngine.registerConversion(CGSUnits.abcoulomb, SIDerivedUnits.coulomb, );


        ConversionEngine.registerConversion(ISOUnits.bytes, ISOUnits.bit, 8.0);
        ConversionEngine.registerConversion(NonSIUnits.gForce, SIDerivedUnits.metrePerSecondSquared, 9.80665);
        ConversionEngine.registerConversion(NonSIUnits.gForce, CGSUnits.gal, 980.665);
        ConversionEngine.registerConversion(SIDerivedUnits.metrePerSecondSquared, CGSUnits.gal, 100.0);


        // Temperature
        ConversionEngine.registerConversion(SIUnits.kelvin, TemperatureUnits.celsius, new OffsetOperation(-273.16));
        ConversionEngine.registerConversion(TemperatureUnits.celsius, TemperatureUnits.fahrenheit,
                                            new MultiplyOffsetOperation((9.0 / 5.0), 32.0));
        ConversionEngine.registerConversion(TemperatureUnits.celsius, TemperatureUnits.rankine,
                                            new OffsetMultiplyOperation(273.15, (9.0 / 5.0)));
        ConversionEngine.registerConversion(TemperatureUnits.celsius, TemperatureUnits.newton, (33.0 / 100.0));
        ConversionEngine.registerConversion(TemperatureUnits.celsius, TemperatureUnits.reaumur, (4.0 / 5.0));
        ConversionEngine.registerConversion(TemperatureUnits.celsius, TemperatureUnits.romer,
                                            new MultiplyOffsetOperation((21.0 / 40.0), 7.5));

    }

    //    public static List<UnitConversion> getAllDefinedUnitConversions() {
    //
    //    }
    //
    //    public static List<UnitConversion> getAllUnitConversions() {
    //
    //    }

    public static void initialize() {
        // do nothing - this will call the static
        // constructor which will initialize
    }
}

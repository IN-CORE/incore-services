/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units;

import java.util.Arrays;
import java.util.List;

public class BinaryPrefix extends Prefix {
    // IEC
    public static final BinaryPrefix kibi = new BinaryPrefix("kibi", "Ki", 1);
    public static final BinaryPrefix mebi = new BinaryPrefix("mebi", "Mi", 2);
    public static final BinaryPrefix gibi = new BinaryPrefix("gibi", "Gi", 3);
    public static final BinaryPrefix tebi = new BinaryPrefix("tebi", "Ti", 4);
    public static final BinaryPrefix pebi = new BinaryPrefix("pebi", "Pi", 5);
    public static final BinaryPrefix exbi = new BinaryPrefix("exbi", "Ei", 6);
    public static final BinaryPrefix zebi = new BinaryPrefix("zebi", "Zi", 7);
    public static final BinaryPrefix yobi = new BinaryPrefix("yobi", "Yi", 8);

    // JEDEC
    public static final BinaryPrefix kilo = kibi;
    public static final BinaryPrefix mega = mebi;
    public static final BinaryPrefix giga = gibi;
    public static final BinaryPrefix tera = tebi;
    public static final BinaryPrefix peta = pebi;
    public static final BinaryPrefix exa = exbi;
    public static final BinaryPrefix zeta = zebi;
    public static final BinaryPrefix yotta = yobi;

    private BinaryPrefix(String name, String symbol, int scale) {
        super(name, symbol, symbol, scale, 1024);
    }

    public static List<Prefix> IEC = Arrays.asList(kibi, mebi, gibi, tebi, pebi, exbi, zebi, yobi);
    public static List<Prefix> JEDEC = Arrays.asList(kilo, mega, giga, tera, peta, exa, zeta, yotta);

    public static List<Prefix> All = Arrays.asList(kibi, kilo, mebi, mega, gibi, giga, tebi, tera,
                                                   pebi, peta, exbi, exa, zebi, zeta, yobi, yotta);
}

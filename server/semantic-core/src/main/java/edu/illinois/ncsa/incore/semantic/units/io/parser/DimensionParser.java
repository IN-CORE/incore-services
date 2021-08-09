/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.io.parser;

import edu.illinois.ncsa.incore.semantic.units.dimension.BaseDimension;
import edu.illinois.ncsa.incore.semantic.units.utils.StringRepresentationUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public final class DimensionParser {
    private DimensionParser() {
    }

    public static Map<BaseDimension, Integer> parseSymbol(String symbol) {
        Map<BaseDimension, Integer> parsedDimensions = new HashMap<>();

        if (symbol.equals("1")) {
            return parsedDimensions;
        }

        String[] dimensions = StringUtils.split(symbol, " ");

        for (String dimension : dimensions) {
            int power = 1;
            String baseSymbol = dimension.substring(0, 1);
            BaseDimension base = BaseDimension.getBySymbol(baseSymbol);

            int powerIndex = dimension.indexOf("^");
            if (powerIndex != -1) {
                power = Integer.parseInt(StringUtils.substring(dimension, powerIndex));
            }

            int unicodePowerIndex = StringRepresentationUtil.indexOfUnicodePower(dimension);
            if (unicodePowerIndex != -1) {
                power = Integer.parseInt(StringUtils.substring(dimension, unicodePowerIndex));
            }

            parsedDimensions.put(base, power);
        }

        return parsedDimensions;
    }
}

/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.eq.utils;

public class EqUtil {
    public static int getReverseFaultingFactorFlag(double rakeAngle)
    {
        if (rakeAngle > 30 && rakeAngle < 150) {
            return 1;
        } else {
            return 0;
        }
    }

    public static int getNormalFaultingFactorFlag(double rakeAngle)
    {
        if (rakeAngle > -150.0 && rakeAngle < -30.0) {
            return 1;
        } else {
            return 0;
        }
    }
}

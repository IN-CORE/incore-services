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

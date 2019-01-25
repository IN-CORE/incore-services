package edu.illinois.ncsa.incore.service.hazard.models.eq.utils;

import org.apache.log4j.Logger;

public class DistanceUtil {
    private static final Logger logger = Logger.getLogger(DistanceUtil.class);

    /**
     *
     * @param azimuthAngle
     * @param dipAngle
     * @param originalDistances
     * @return
     */
    public static double[] computeTransformedDistance(double azimuthAngle, double dipAngle, double[] originalDistances)
    {
        // Convert to radians
        double azimuth = Math.toRadians(azimuthAngle);// azimuthAngle * Math.PI
        // / 180.0;

        double dip = Math.toRadians(dipAngle);// dipAngle * Math.PI / 180.0;

        double[] transformedDistances = new double[3];

        transformedDistances[0] = Math.cos(dip) * Math.cos(azimuth) * originalDistances[0]
            - Math.cos(dip) * Math.sin(azimuth) * originalDistances[1] - Math.sin(dip) * originalDistances[2];

        transformedDistances[1] = Math.sin(azimuth) * originalDistances[0] + Math.cos(azimuth) * originalDistances[1];

        transformedDistances[2] = Math.sin(dip) * Math.cos(azimuth) * originalDistances[0]
            - Math.sin(dip) * Math.sin(azimuth) * originalDistances[1] + Math.cos(dip) * originalDistances[2];

        return transformedDistances;
    }

    /**
     *
     * @param transformedDistances
     * @param ruptureWidth
     * @param ruptureLength
     * @return
     */
    public static double computeDistanceToRupturePlane(double[] transformedDistances, double ruptureWidth, double ruptureLength)
    {
        double x_new = transformedDistances[0];
        double y_new = transformedDistances[1];
        double z_new = transformedDistances[2];

        double r_rup = 0.0;
        if (Math.abs(x_new) <= (ruptureWidth / 2.0) && Math.abs(y_new) <= (ruptureLength / 2.0)) {
            // Fixed bug MAE-376 by added Math.abs( ) around z_new
            r_rup = Math.abs(z_new);
        } else if (Math.abs(x_new) > (ruptureWidth / 2.0) && Math.abs(y_new) <= (ruptureLength / 2.0)) {
            r_rup = Math.sqrt(z_new * z_new + Math.pow((Math.abs(x_new) - ruptureWidth / 2.0), 2));
        } else if (Math.abs(x_new) <= (ruptureWidth / 2.0) && Math.abs(y_new) > (ruptureLength / 2.0)) {
            r_rup = Math.sqrt(z_new * z_new + Math.pow((Math.abs(y_new) - ruptureLength / 2.0), 2));
        } else {
            r_rup = Math.sqrt(z_new * z_new + Math.pow((Math.abs(x_new) - ruptureWidth / 2.0), 2)
                + Math.pow((Math.abs(y_new) - ruptureLength / 2.0), 2));
        }

        if (r_rup < 0.0) {
            logger.debug("WARNING r_rup is less than 0"); //$NON-NLS-1$
        }

        return r_rup;
    }

    /**
     *
     * @param transformedDistances
     * @param ruptureWidth
     * @param ruptureLength
     * @param dipAngle
     * @return
     */
    public static double computeJoynerBooreDistance(double[] transformedDistances, double ruptureWidth, double ruptureLength,
                                                    double dipAngle)
    {
        double r_jb = 0.0;
        double dipAngleRadians = Math.toRadians(dipAngle);
        double x_new = transformedDistances[0];
        double y_new = transformedDistances[1];
        double z_new = transformedDistances[2];

        double x_proj = x_new * Math.cos(dipAngleRadians) + z_new * Math.sin(dipAngleRadians);
        double y_proj = y_new;

        if (Math.abs(x_proj) <= ruptureWidth * Math.cos(dipAngleRadians) / 2.0 && Math.abs(y_proj) <= ruptureLength / 2.0) {
            r_jb = 0.0;
        } else if (Math.abs(x_proj) > ruptureWidth * Math.cos(dipAngleRadians) / 2.0 && Math.abs(y_proj) <= ruptureLength / 2.0) {
            r_jb = Math.abs(x_proj) - ruptureWidth * Math.cos(dipAngleRadians) / 2.0;
        } else if (Math.abs(x_proj) <= ruptureWidth * Math.cos(dipAngleRadians) / 2.0 && Math.abs(y_proj) > ruptureLength / 2.0) {
            r_jb = Math.abs(y_proj) - ruptureLength / 2.0;
        } else {
            r_jb = Math.sqrt(Math.pow(Math.abs(x_proj) - ruptureWidth * Math.cos(dipAngleRadians) / 2.0, 2)
                + Math.pow(Math.abs(y_proj) - ruptureLength / 2.0, 2));
        }
        return r_jb;
    }

    /**
     * Calculates the closest distance to the surface projection of the top edge of the coseismic
     * rupture plane measured perpendicular to its average strike [km]. Based on the paper "Estimating Unknown Input Parameters
     * when Implementing the NGA Ground-Motion Prediction Equations in Engineering Practice"
     *
     * @param Rjb
     *            The closest distance to the surface projection of the coseismic rupture plane (Joyner-Boore distance) in km.
     * @param Ztor
     *            The depth to the top of the coseismic rupture plane in km.
     * @param W
     *            Down-dip width of the rupture plane in km.
     * @param dip
     *            The average dip of the rupture plane in degrees.
     * @param azimuth
     *            Azimuth Angle in degrees.
     * @param Rrup
     *            The closest distance to the coseismic rupture plane in km.
     * @return The closest distance to the surface projection of the top edge of the coseismic rupture plane measured perpendicular
     *         to its average strike in km.
     */
    public static double computeRx(double Rjb, double Ztor, double W, double dip, double azimuth, double Rrup)
    {
        // Validate Azimuth ranges from -180 to 180 [-180, 180]
        // Validate dip 0 - 90 [0, 90)
        // Validate Ztor > 0
        // Validate Rjb > 0
        // Validate W >= 0

        // Define angles in terms of radians
        double dipInRads = Math.toRadians(dip);
        double azimuthInRads = Math.toRadians(azimuth);
        double Rx;

        // Non-vertical fault
        if (dipInRads != 90) {
            if (azimuthInRads >= 0 && azimuthInRads <= 180 && azimuthInRads != 90) {
                // Case 2 and 8
                if (Rjb * Math.abs(Math.tan(azimuthInRads)) <= W * Math.cos(dipInRads)) {
                    // Equation 7
                    Rx = Rjb * Math.tan(azimuthInRads);
                } else { // Case 3 and 9
                    // Equation 8
                    Rx = Rjb * Math.tan(azimuthInRads)
                        * Math.cos(azimuthInRads - Math.asin((W * Math.cos(dipInRads) * Math.cos(azimuthInRads) / Rjb)));
                }
            } else if (azimuth == 90) {
                // Case 6
                if (Rjb > 0) {
                    // Equation 9
                    Rx = Rjb + W * Math.cos(dipInRads);
                } else {
                    if (Rrup < Ztor * (1 / Math.cos(dipInRads))) {
                        // Equation 10
                        Rx = Math.sqrt(Math.pow(Rrup, 2) - Math.pow(Ztor, 2));
                    } else {
                        // Equation 11
                        Rx = Rrup * (1 / Math.sin(azimuthInRads)) - Ztor * (1 / Math.tan(dipInRads));
                    }
                }
            } else { // Case 1, 4 and 7 ( -180 <= azimuth < 0 )
                // Equation 12
                Rx = Rjb * Math.sin(azimuthInRads);
            }
        } else { // vertical strike-slip fault (azimuth = 90)
            // Equation 13
            Rx = Rjb * Math.sin(azimuthInRads);
        }

        return Rx;
    }

}

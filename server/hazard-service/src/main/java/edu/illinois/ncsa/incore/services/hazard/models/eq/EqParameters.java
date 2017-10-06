package edu.illinois.ncsa.incore.services.hazard.models.eq;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.mongodb.morphia.annotations.Transient;

import javax.xml.bind.annotation.XmlTransient;
import java.util.HashMap;
import java.util.Map;

public class EqParameters {
    private double srcLatitude;
    private double srcLongitude;

    // Removed this helper variable due to serialization exception
//    private Site epicenter;
    private double magnitude;
    private double coseismicRuptureDepth;
    private double dipAngle;
    private double azimuthAngle;
    private double rakeAngle;
    private double seismogenicDepth;
    private double depth;

    /** Depth to 2.5 km/sec shear wave velocity */
    private double depth2p5KmPerSecShearWaveVelocity;
    /** Depth to the 1.0 km/sec shear wave velocity, in meters */
    private double shearWaveDepth1p0;

    // private double vs_30;
    private Map<String, String> faultTypeMap;
    private String region;

    public EqParameters()
    {
        region = "Global";
        magnitude = 0.0;
        coseismicRuptureDepth = 0.0;
        dipAngle = 0.0;
        azimuthAngle = 0.0;
        rakeAngle = 0.0;
        seismogenicDepth = 0.0;
        depth2p5KmPerSecShearWaveVelocity = 2.0;
        faultTypeMap = new HashMap<>();
        shearWaveDepth1p0 = 0.0;
        depth = 0.0;
        srcLatitude = -1;
        srcLongitude = -1;
    }


    /**
     *
     * @return
     */
    public double getMagnitude()
    {
        return magnitude;
    }

    /**
     *
     * @param magnitude
     */
    public void setMagnitude(double magnitude)
    {
        this.magnitude = magnitude;
    }

    /**
     *
     * @return Depth to the top of the rupture (km)
     */
    public double getCoseismicRuptureDepth()
    {
        return coseismicRuptureDepth;
    }

    /**
     *
     * @param coseismicRuptureDepth
     */
    public void setCoseismicRuptureDepth(double coseismicRuptureDepth)
    {
        this.coseismicRuptureDepth = coseismicRuptureDepth;
    }

    /**
     *
     * @return
     */
    public double getDipAngle()
    {
        return dipAngle;
    }

    /**
     *
     * @param dipAngle
     */
    public void setDipAngle(double dipAngle)
    {
        this.dipAngle = dipAngle;
    }

    /**
     *
     * @return
     */
    public double getAzimuthAngle()
    {
        return azimuthAngle;
    }

    /**
     *
     * @param azimuthAngle
     */
    public void setAzimuthAngle(double azimuthAngle)
    {
        this.azimuthAngle = azimuthAngle;
    }

    /**
     *
     * @return
     */
    public double getRakeAngle()
    {
        return rakeAngle;
    }

    /**
     *
     * @param rakeAngle
     */
    public void setRakeAngle(double rakeAngle)
    {
        this.rakeAngle = rakeAngle;
    }

    /**
     *
     * @return
     */
    public double getSeismogenicDepth()
    {
        return seismogenicDepth;
    }

    /**
     *
     * @param seismogenicDepth
     */
    public void setSeismogenicDepth(double seismogenicDepth)
    {
        this.seismogenicDepth = seismogenicDepth;
    }

    /**
     *
     * @return
     */
    public double getDepth2p5KmPerSecShearWaveVelocity()
    {
        return depth2p5KmPerSecShearWaveVelocity;
    }

    /**
     *
     * @param depth2p5KmPerSecShearWaveVelocity
     */
    public void setDepth2p5KmPerSecShearWaveVelocity(double depth2p5KmPerSecShearWaveVelocity)
    {
        this.depth2p5KmPerSecShearWaveVelocity = depth2p5KmPerSecShearWaveVelocity;
    }

    public String getRegion()
    {
        return region;
    }

    public void setRegion(String region)
    {
        this.region = region;
    }

    /**
     *
     * @return
     */
    public Map<String, String> getFaultTypeMap()
    {
        return faultTypeMap;
    }

    /**
     *
     * @param faultTypeMap
     */
    public void setFaultTypeMap(Map<String, String> faultTypeMap)
    {
        this.faultTypeMap = faultTypeMap;
    }

    /**
     *
     * @param attenuationId
     * @return
     */
    public String getFaultType(String attenuationId)
    {
        if (faultTypeMap.containsKey(attenuationId)) {
            return faultTypeMap.get(attenuationId);
        } else {
            return null;
        }
    }

    /**
     *
     * @return
     */
    public double getShearWaveDepth1p0()
    {
        return shearWaveDepth1p0;
    }

    /**
     *
     * @param shearWaveDepth1p0
     */
    public void setShearWaveDepth1p0(double shearWaveDepth1p0)
    {
        this.shearWaveDepth1p0 = shearWaveDepth1p0;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }

    public double getDepth() {
        return depth;
    }

    public double getSrcLatitude() {
        return srcLatitude;
    }

    public void setSrcLatitude(double srcLatitude) {
        this.srcLatitude = srcLatitude;
    }

    public double getSrcLongitude() {
        return srcLongitude;
    }

    public void setSrcLongitude(double srcLongitude) {
        this.srcLongitude = srcLongitude;
    }

//    public Site getEpicenter() {
//        if(epicenter == null && srcLatitude != -1 && srcLongitude != -1) {
//            epicenter = new Site(new GeometryFactory().createPoint(new Coordinate(srcLongitude, srcLatitude)), depth);
//        }
//
//        return epicenter;
//    }

}


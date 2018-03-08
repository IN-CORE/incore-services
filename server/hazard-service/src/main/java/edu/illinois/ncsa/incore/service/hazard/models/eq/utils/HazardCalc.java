/*******************************************************************************
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.eq.utils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import edu.illinois.ncsa.incore.service.hazard.models.eq.ScenarioEarthquake;
import edu.illinois.ncsa.incore.service.hazard.models.eq.Site;
import edu.illinois.ncsa.incore.service.hazard.models.eq.attenuations.BaseAttenuation;
import edu.illinois.ncsa.incore.service.hazard.models.eq.site.NEHRPSiteAmplification;
import edu.illinois.ncsa.incore.service.hazard.models.eq.site.SiteAmplification;
import edu.illinois.ncsa.incore.service.hazard.models.eq.types.SeismicHazardResult;
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.gce.arcgrid.ArcGridFormat;
import org.geotools.gce.arcgrid.ArcGridWriteParams;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.Envelope2D;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.media.jai.RasterFactory;
import java.awt.*;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class HazardCalc {

    private static GeometryFactory factory = new GeometryFactory();

    public static SeismicHazardResult getGroundMotionAtSite(ScenarioEarthquake earthquake, Map<BaseAttenuation, Double> attenuations, Site site, String hazardType, String demand, int spectrumOverride, boolean amplifyHazard) throws Exception {
        Iterator<BaseAttenuation> iterator = attenuations.keySet().iterator();

        double hazardValue = 0.0;
        String closestHazardPeriod = hazardType;
        while (iterator.hasNext()) {
            BaseAttenuation model = iterator.next();
            double weight = attenuations.get(model);
            SeismicHazardResult matchedResult = model.getValueClosestMatch(hazardType, site);
            hazardValue += (Math.log(matchedResult.getHazardValue()) * weight);

            closestHazardPeriod = matchedResult.getPeriod();
        }

        hazardValue = Math.exp(hazardValue);

        // TODO check if site class dataset is defined for amplifying hazard
        int siteClass = HazardUtil.getSiteClassAsInt(earthquake.getDefaultSiteClass());

        SiteAmplification siteAmplification = null;
        if (amplifyHazard) {
            // TODO need to add check for if VS already accounted for soil type

            // TODO add check for Rix Fernandez, no need to amplify

            // TODO Add support for other amplification methods besides NEHRP

            if (earthquake.getSiteAmplification().equalsIgnoreCase("NEHRP")) {
                siteAmplification = new NEHRPSiteAmplification();

                if (closestHazardPeriod.equalsIgnoreCase(HazardUtil.PGV)) {
                    double pga = getGroundMotionAtSite(earthquake, attenuations, site, "PGA", demand, spectrumOverride, false).getHazardValue();
                    hazardValue *= siteAmplification.getSiteAmplification(site, pga, siteClass, closestHazardPeriod);
                } else {
                    // Note, hazard value input should be PGA if amplifying PGV hazard because NEHRP uses PGA coefficients for amplifying PGV
                    // and the range for interpretation is in units of g
                    hazardValue *= siteAmplification.getSiteAmplification(site, hazardValue, siteClass, closestHazardPeriod);
                }
            }

        }

        return new SeismicHazardResult(hazardValue, closestHazardPeriod, demand);
    }

    public static GridCoverage getEarthquakeHazardRaster(ScenarioEarthquake scenarioEarthquake, Map<BaseAttenuation, Double> attenuations, double minX, double minY, double maxX, double maxY, double gridSpacing, String period, String demand, boolean amplifyHazard) throws Exception {
        int width = 0;
        int height = 0;

        double dx = (maxX - minX);
        double dy = (maxY - minY);

        dx = Math.ceil(dx / gridSpacing) * gridSpacing;
        dy = Math.ceil(dy / gridSpacing) * gridSpacing;

        maxX = minX + dx;
        maxY = minY + dy;

        if (gridSpacing != 0) {
            long widthLong = Math.round(Math.abs((maxX - minX) / gridSpacing)); // + 1;
            long heightLong = Math.round(Math.abs((maxY - minY) / gridSpacing)); // + 1;
            if ((widthLong > Integer.MAX_VALUE) || (heightLong > Integer.MAX_VALUE)) {
                System.out.println("Overflow....too many points to fit in an int"); //$NON-NLS-1$
            }
            // adjustMaxMin();
            width = (int) widthLong;
            height = (int) heightLong;

        }

        float cellsize = (float) gridSpacing;
        float startX = (float) minX + ((float) gridSpacing / 2.0f);
        float startY = (float) maxY - ((float) gridSpacing / 2.0f);
        Site localSite = null;

        CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
        GridCoverageFactory gcFactory = CoverageFactoryFinder.getGridCoverageFactory(null);
        WritableRaster raster = RasterFactory.createBandedRaster(DataBuffer.TYPE_FLOAT, width, height, 1, null);

        Envelope envelope = new Envelope2D(crs, minX, minY, width * cellsize, height * cellsize);

        for (int y = 0; y < height; y++) {

            startX = (float) minX + (cellsize / 2.0f);
            for (int x = 0; x < width; x++) {
                localSite = new Site(factory.createPoint(new Coordinate(startX, startY)));
                double hazardValue = getGroundMotionAtSite(scenarioEarthquake, attenuations, localSite, period, demand, 0, amplifyHazard).getHazardValue();

                raster.setSample(x, y, 0, hazardValue);

                startX += (float) gridSpacing;
            }
            startY -= gridSpacing;
        }

        Color[] colors = new Color[]{Color.BLUE, Color.CYAN, Color.WHITE, Color.YELLOW, Color.RED};
        GridCoverage gc = gcFactory.create("Hazard Coverage", raster, envelope, null, null, null, new Color[][]{colors}, null);
        return gc;
    }

    public static void getEarthquakeHazardAsAsciiGrid(GridCoverage gridCoverage, File rasterFile) throws IOException {
        GridCoverageWriter writer = new ArcGridFormat().getWriter(rasterFile);

        final ArcGridWriteParams wp = new ArcGridWriteParams();
        wp.setSourceBands(new int[]{0});

        ParameterValueGroup params = writer.getFormat().getWriteParameters();
        params.parameter("GRASS").setValue(false);
        params.parameter(AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString()).setValue(wp);

        GeneralParameterValue[] gpv = {params.parameter("GRASS"), params.parameter(AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString())};
        writer.write(gridCoverage, gpv);
        writer.dispose();
    }

    public static void getEarthquakeHazardAsGeoTiff(GridCoverage gridCoverage, File tiffFile) throws IOException {
        GeoTiffWriter writer = new GeoTiffWriter(tiffFile);

        final ArcGridWriteParams wp = new ArcGridWriteParams();
        wp.setSourceBands(new int[]{0});
        ParameterValueGroup params = writer.getFormat().getWriteParameters();
        params.parameter(AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString()).setValue(wp);

        GeneralParameterValue[] gpv = {params.parameter(AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString())};
        writer.write(gridCoverage, gpv);
        writer.dispose();
    }

}

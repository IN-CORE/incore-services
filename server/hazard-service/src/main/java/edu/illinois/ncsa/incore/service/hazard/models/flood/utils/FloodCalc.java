/*******************************************************************************
 * Copyright (c) 2020 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.flood.utils;

import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.service.hazard.exception.UnsupportedHazardException;
import edu.illinois.ncsa.incore.service.hazard.models.eq.types.IncorePoint;
import edu.illinois.ncsa.incore.service.hazard.models.eq.utils.HazardUtil;
import edu.illinois.ncsa.incore.service.hazard.models.flood.Flood;
import edu.illinois.ncsa.incore.service.hazard.models.flood.FloodDataset;
import edu.illinois.ncsa.incore.service.hazard.models.flood.FloodHazardDataset;
import edu.illinois.ncsa.incore.service.hazard.models.flood.types.FloodHazardResult;
import edu.illinois.ncsa.incore.service.hazard.utils.GISUtil;
import org.apache.log4j.Logger;
import org.geotools.coverage.grid.GridCoverage2D;
import org.opengis.coverage.PointOutsideCoverageException;
import org.opengis.coverage.grid.GridCoverage;

import javax.ws.rs.core.Response;
import java.util.LinkedList;
import java.util.List;

public class FloodCalc {
    public static final Logger log = Logger.getLogger(FloodCalc.class);

    public static FloodHazardResult getFloodHazardValue(Flood flood, String demandType, String demandUnits,
        IncorePoint location, String user) throws UnsupportedHazardException {
        if (flood instanceof FloodDataset) {
            FloodDataset floodDataset = (FloodDataset) flood;
            FloodHazardDataset hazardDataset = findHazard(floodDataset.getHazardDatasets(), demandType);
            double hazardValue = 0.0;
            if (hazardDataset != null) {
                GridCoverage gc = GISUtil.getGridCoverage(hazardDataset.getDatasetId(), user);
                try {
                    hazardValue = HazardUtil.findRasterPoint(location.getLocation(), (GridCoverage2D) gc);
                    hazardValue = FloodUtil.convertHazard(hazardValue, demandType, hazardDataset.getDemandUnits(), demandUnits);
                } catch (PointOutsideCoverageException e) {
                    log.debug("Point outside tiff image.");
                } catch (IllegalAccessException e) {
                    log.debug("Illegal Access.", e);
                } catch (NoSuchFieldException e) {
                    log.debug("No Such Field", e);
                } catch (UnsupportedOperationException e){
                    throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Requested demand type or units is not accepted");
                }
                return new FloodHazardResult(location.getLocation().getY(), location.getLocation().getX(), hazardValue, demandType, demandUnits);
            }
            else {
                throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "No dataset found for the hazard. Check if requested demand type is valid");
            }
        } else {
            log.debug("Received flood is not of dataset type");
            throw new UnsupportedHazardException("Flood hazard values can only be obtained from datasets.");
        }
    }

    public static FloodHazardDataset findHazard(List<FloodHazardDataset> hazardDatasets, String demandType) {

        List<FloodHazardDataset> matches = new LinkedList<FloodHazardDataset>();
        for (FloodHazardDataset dataset : hazardDatasets) {
            if (dataset.getDemandType().equalsIgnoreCase(demandType)) {
                matches.add(dataset);
            }
        }

        // Should we consider datasets that have the same demand type, but cover different areas?
        // The assumption here is one dataset per demand type
        // We could return all the matches and look in each for the location, but for now we return the first match
        if (matches.size() > 0) {
            return matches.get(0);
        }

        return null;
    }


}

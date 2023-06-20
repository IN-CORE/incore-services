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
import org.json.JSONObject;
import org.opengis.coverage.PointOutsideCoverageException;
import org.opengis.coverage.grid.GridCoverage;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static edu.illinois.ncsa.incore.service.hazard.models.eq.utils.HazardUtil.INVALID_DEMAND;
import static edu.illinois.ncsa.incore.service.hazard.models.eq.utils.HazardUtil.INVALID_UNIT;

public class FloodCalc {
    public static final Logger log = Logger.getLogger(FloodCalc.class);

    public static FloodHazardResult getFloodHazardValue(Flood flood, String demandType, String demandUnits,
                                                        IncorePoint location, String user, String userGroups) throws UnsupportedHazardException, IOException {
        if (flood instanceof FloodDataset) {
            FloodDataset floodDataset = (FloodDataset) flood;
            FloodHazardDataset hazardDataset = findHazard(floodDataset.getHazardDatasets(), demandType);
            Double hazardValue;
            if (hazardDataset != null) {
                GridCoverage gc = GISUtil.getGridCoverage(hazardDataset.getDatasetId(), user, userGroups);

                try {
                    hazardValue = HazardUtil.findRasterPoint(location.getLocation(), (GridCoverage2D) gc);
                } catch (PointOutsideCoverageException e) {
                    hazardValue = null;
                }

                try {
                    if (hazardValue != null) {
                        hazardValue = FloodUtil.convertHazard(hazardValue, demandType, hazardDataset.getDemandUnits(), demandUnits);
                        JSONObject floodThresholds;
                        if (hazardDataset.getThreshold() != null){
                            floodThresholds = HazardUtil.toLowerKey(new JSONObject(hazardDataset.getThresholdJsonString()));
                        } else {
                            floodThresholds = HazardUtil.toLowerKey(HazardUtil.FLOOD_THRESHOLDS);
                        }

                        if (floodThresholds.has(demandType.toLowerCase())) {
                            JSONObject demandThreshold = ((JSONObject) floodThresholds.get(demandType.toLowerCase()));
                            Double threshold = demandThreshold.get("value") == JSONObject.NULL ? null : demandThreshold.getDouble("value");
                            // ignore threshold if null
                            if (threshold != null) {
                                threshold = FloodUtil.convertHazard(threshold, demandType, demandThreshold.getString("unit"), demandUnits);
                                if (hazardValue <= threshold) {
                                    hazardValue = null;
                                }
                            }
                        }
                    }
                } catch (UnsupportedOperationException | NoSuchFieldException | IllegalAccessException e) {
                    hazardValue = INVALID_UNIT;
                }
                return new FloodHazardResult(location.getLocation().getY(), location.getLocation().getX(), hazardValue, demandType,
                    demandUnits);
            } else {
                return new FloodHazardResult(location.getLocation().getY(), location.getLocation().getX(), INVALID_DEMAND, demandType,
                    demandUnits);
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

/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.hurricane.utils;

import edu.illinois.ncsa.incore.service.hazard.exception.UnsupportedHazardException;
import edu.illinois.ncsa.incore.service.hazard.models.eq.types.IncorePoint;
import edu.illinois.ncsa.incore.service.hazard.models.eq.utils.HazardUtil;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.Hurricane;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.HurricaneDataset;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.HurricaneHazardDataset;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.types.HurricaneHazardResult;
import edu.illinois.ncsa.incore.service.hazard.utils.GISUtil;
import org.apache.log4j.Logger;
import org.geotools.coverage.grid.GridCoverage2D;
import org.json.JSONObject;
import org.geotools.api.coverage.PointOutsideCoverageException;
import org.geotools.api.coverage.grid.GridCoverage;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static edu.illinois.ncsa.incore.service.hazard.models.eq.utils.HazardUtil.INVALID_DEMAND;
import static edu.illinois.ncsa.incore.service.hazard.models.eq.utils.HazardUtil.INVALID_UNIT;

public class HurricaneCalc {
    public static final Logger log = Logger.getLogger(HurricaneCalc.class);

    public static HurricaneHazardResult getHurricaneHazardValue(Hurricane hurricane, String demandType, String demandUnits,
                                                                IncorePoint location, String user, String userGroups) throws UnsupportedHazardException,
        UnsupportedOperationException, IOException {
        if (hurricane instanceof HurricaneDataset) {
            HurricaneDataset hurricaneDataset = (HurricaneDataset) hurricane;
            HurricaneHazardDataset hazardDataset = findHazard(hurricaneDataset.getHazardDatasets(), demandType);
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
                        hazardValue = HurricaneUtil.convertHazard(hazardValue, demandType, hazardDataset.getDemandUnits(), demandUnits);

                        JSONObject hurricaneThresholds;
                        if (hazardDataset.getThreshold() != null){
                            hurricaneThresholds = HazardUtil.toLowerKey(new JSONObject(hazardDataset.getThresholdJsonString()));
                        } else {
                            hurricaneThresholds = HazardUtil.toLowerKey(HazardUtil.HURRICANE_THRESHOLDS);
                        }

                        if (hurricaneThresholds.has(demandType.toLowerCase())) {
                            JSONObject demandThreshold = ((JSONObject) hurricaneThresholds.get(demandType.toLowerCase()));
                            Double threshold = demandThreshold.get("value") == JSONObject.NULL ? null : demandThreshold.getDouble("value");
                            // ignore threshold if null
                            if (threshold != null) {
                                threshold = HurricaneUtil.convertHazard(threshold, demandType, demandThreshold.getString("unit"),
                                    demandUnits);
                                if (hazardValue <= threshold) {
                                    hazardValue = null;
                                }
                            }
                        }
                    }
                } catch (UnsupportedOperationException | NoSuchFieldException | IllegalAccessException e) {
                    hazardValue = INVALID_UNIT;
                }

                return new HurricaneHazardResult(location.getLocation().getY(), location.getLocation().getX(), hazardValue, demandType,
                    demandUnits);
            } else {
                return new HurricaneHazardResult(location.getLocation().getY(), location.getLocation().getX(), INVALID_DEMAND, demandType,
                    demandUnits);
            }
        } else {
            log.debug("Received hurricane is not of dataset type");
            throw new UnsupportedHazardException("Hurricane hazard values can only be obtained from datasets.");
        }
    }

    public static HurricaneHazardDataset findHazard(List<HurricaneHazardDataset> hazardDatasets, String demandType) {

        List<HurricaneHazardDataset> matches = new LinkedList<HurricaneHazardDataset>();
        for (HurricaneHazardDataset dataset : hazardDatasets) {
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

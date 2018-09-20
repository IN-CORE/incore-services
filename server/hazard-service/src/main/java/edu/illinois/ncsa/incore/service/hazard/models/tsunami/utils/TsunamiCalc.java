/*******************************************************************************
 * Copyright (c) 2018 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.tsunami.utils;

import edu.illinois.ncsa.incore.service.hazard.exception.UnsupportedHazardException;
import edu.illinois.ncsa.incore.service.hazard.models.eq.types.IncorePoint;
import edu.illinois.ncsa.incore.service.hazard.models.eq.utils.HazardUtil;
import edu.illinois.ncsa.incore.service.hazard.models.tsunami.Tsunami;
import edu.illinois.ncsa.incore.service.hazard.models.tsunami.TsunamiDataset;
import edu.illinois.ncsa.incore.service.hazard.models.tsunami.TsunamiHazardDataset;
import edu.illinois.ncsa.incore.service.hazard.models.tsunami.types.TsunamiHazardResult;
import edu.illinois.ncsa.incore.service.hazard.utils.GISUtil;
import org.apache.log4j.Logger;
import org.geotools.coverage.grid.GridCoverage2D;
import org.opengis.coverage.PointOutsideCoverageException;
import org.opengis.coverage.grid.GridCoverage;

import java.util.LinkedList;
import java.util.List;

public class TsunamiCalc {
    public static final Logger log = Logger.getLogger(TsunamiCalc.class);

    public static TsunamiHazardResult getTsunamiHazardValue(Tsunami tsunami, String demandType, String demandUnits, IncorePoint location, String user) throws UnsupportedHazardException {
        if (tsunami instanceof TsunamiDataset) {
            TsunamiDataset tsunamiDataset = (TsunamiDataset) tsunami;
            TsunamiHazardDataset hazardDataset = findHazard(tsunamiDataset.getHazardDatasets(), demandType);
            double hazardValue = 0.0;
            if (hazardDataset != null) {
                // TODO We should consider caching these on the server side, at least temporarily
                // We can create some kind of volatile cache of directory paths, <dataset-id, <abs-path> that is wiped
                // out on service restart
                GridCoverage gc = GISUtil.getGridCoverage(hazardDataset.getDatasetId(), user);
                try {
                    hazardValue = HazardUtil.findRasterPoint(location.getLocation(), (GridCoverage2D) gc);
                    hazardValue = TsunamiUtil.convertHazard(hazardValue, demandType, hazardDataset.getDemandUnits(), demandUnits);
                } catch (PointOutsideCoverageException e) {
                    log.debug("Point outside tiff image.");
                }
            }
            return new TsunamiHazardResult(location.getLocation().getY(), location.getLocation().getX(), hazardValue, demandType, demandUnits);

        } else {
            throw new UnsupportedHazardException("Tsunami hazard values can only be obtained from datasets.");
        }
    }

    public static TsunamiHazardDataset findHazard(List<TsunamiHazardDataset> hazardDatasets, String demandType) {

        List<TsunamiHazardDataset> matches = new LinkedList<TsunamiHazardDataset>();
        for (TsunamiHazardDataset dataset : hazardDatasets) {
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

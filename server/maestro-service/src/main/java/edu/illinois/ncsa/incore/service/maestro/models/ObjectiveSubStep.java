// this is for 3. Determine Goals & Objectives

package edu.illinois.ncsa.incore.service.maestro.models;

import dev.morphia.annotations.Embedded;
import edu.illinois.ncsa.incore.service.data.models.Dataset;
import edu.illinois.ncsa.incore.service.hazard.models.eq.Earthquake;
import edu.illinois.ncsa.incore.service.hazard.models.flood.Flood;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.Hurricane;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.Tornado;
import edu.illinois.ncsa.incore.service.hazard.models.tsunami.Tsunami;

import java.util.List;

@Embedded
public class ObjectiveSubStep extends SubStep {
    // could be any of the hazard
    public List<Tornado> tornadoes;
    public List<Earthquake> earthquakes;
    public List<Tsunami> tsunamis;
    public List<Hurricane> hurricanes;
    public List<Flood> floods;
    public List<Dataset> datasets;
}

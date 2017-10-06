package edu.illinois.ncsa.incore.services.hazard.dataaccess;

import edu.illinois.ncsa.incore.services.hazard.models.eq.ScenarioEarthquake;
import org.mongodb.morphia.Datastore;

import java.util.List;

public interface IRepository {
    void initialize();
    ScenarioEarthquake getScenarioEarthquakeById(String id);
    ScenarioEarthquake addScenarioEarthquake(ScenarioEarthquake scenarioEarthquake);
    List<ScenarioEarthquake> getScenarioEarthquakes();
    Datastore getDataStore();
}

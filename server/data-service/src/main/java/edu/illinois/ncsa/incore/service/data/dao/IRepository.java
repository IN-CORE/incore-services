package edu.illinois.ncsa.incore.service.data.dao;

import edu.illinois.ncsa.incore.service.data.model.Space;
import edu.illinois.ncsa.incore.service.data.model.datawolf.domain.Dataset;
import org.mongodb.morphia.Datastore;

import java.util.List;

public interface IRepository {
    void initialize();
    Dataset getDatasetById(String id);
    List<Dataset> getAllDatasets();
    Dataset addDataset(Dataset dataset);
    Space addSpace(Space space);
    Space getSpaceByName(String name);
    Datastore getDataStore();
}

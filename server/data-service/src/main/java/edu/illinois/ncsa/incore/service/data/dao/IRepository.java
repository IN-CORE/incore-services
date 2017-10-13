package edu.illinois.ncsa.incore.service.data.dao;

import edu.illinois.ncsa.incore.service.data.model.Space;
import edu.illinois.ncsa.incore.service.data.model.datawolf.domain.Dataset;
import org.mongodb.morphia.Datastore;

import java.util.List;

public interface IRepository {
    void initialize();
    List<Dataset> getAllDatasets();
    Dataset getDatasetById(String id);
    Dataset addDataset(Dataset dataset);
    Dataset updateDataset(String datasetId, String propName, String propValue);
    List<Space> getAllSpaces();
    Space addSpace(Space space);
    Space getSpaceByName(String name);
    Datastore getDataStore();
}

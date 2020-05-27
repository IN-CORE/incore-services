package edu.illinois.ncsa.incore.service.semantics.daos;

import org.bson.Document;

import java.util.List;
import java.util.Optional;

public interface IDatasetTypeDAO {
    void initialize();

    List<Document> getDatasetTypes();

    Optional<List<Document>> getDatasetTypeByUri(String uri, String version);

    String postDatasetType(Document datasetType);

    Optional<List<Document>> searchDatasetType(String datasetTypeName);

    String deleteDatasetType(String id);
}

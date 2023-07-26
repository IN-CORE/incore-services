package edu.illinois.ncsa.incore.service.semantics.daos;

import org.bson.Document;

import java.util.List;
import java.util.Optional;

public interface ITypeDAO {
    void initialize();

    List<Document> getTypes();

    Optional<List<Document>> getTypeByName(String name, String version);

    Document postType(Document type);

    Optional<List<Document>> searchType(String typeName);

    Document deleteType(String name);

    Boolean hasType(String name);
}

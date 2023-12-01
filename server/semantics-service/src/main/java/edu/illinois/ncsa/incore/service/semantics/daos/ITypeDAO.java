package edu.illinois.ncsa.incore.service.semantics.daos;

import edu.illinois.ncsa.incore.service.semantics.model.Type;
import org.bson.Document;

import java.util.List;

public interface ITypeDAO {
    void initialize();

    List<Type> getTypes();

    List<Type> getTypeByName(String name, String version);

    Document postType(Document type);

    List<Type> searchType(String typeName);

    Type deleteType(String name);

    Boolean hasType(String name);
}

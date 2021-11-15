package edu.illinois.ncsa.incore.common.dao;

import org.bson.Document;

import java.util.List;

public interface ICommonRepository {

    void initialize();

    List<Document> getAllDemandDefinitions();

    Document getDemandDefinitionById(String id);
}

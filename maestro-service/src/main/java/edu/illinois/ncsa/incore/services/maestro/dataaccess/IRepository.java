package edu.illinois.ncsa.incore.services.maestro.dataaccess;

import edu.illinois.ncsa.incore.services.maestro.model.Analysis;
import org.mongodb.morphia.Datastore;

import java.util.List;

public interface IRepository {
    void initialize();
    List<Analysis> getAllAnalyses();
    Analysis getAnalysisById(String id);
    String addAnalysis(Analysis analysis);
    Datastore getDataStore();
}

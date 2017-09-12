package edu.illinois.ncsa.incore.services.fragility.dataaccess;

import edu.illinois.ncsa.incore.services.fragility.model.FragilitySet;
import org.mongodb.morphia.Datastore;

import java.util.List;

public interface IRepository {
    void initialize();
    List<FragilitySet> getFragilities();
    Datastore getDataStore();
}

package edu.illinois.ncsa.incore.services.maestro.dataaccess;

import edu.illinois.ncsa.incore.services.maestro.model.Service;
import org.mongodb.morphia.Datastore;

import java.util.List;

public interface IRepository {
    void initialize();
    List<Service> getAllServices();
    Service getServiceById(String id);
    String addService(Service service);
    Datastore getDataStore();
}

package mocks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.services.maestro.dataaccess.IRepository;
import edu.illinois.ncsa.incore.services.maestro.model.Service;
import org.mockito.Mockito;
import org.mongodb.morphia.Datastore;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MockRepository implements IRepository {
    private Datastore mockDataStore;
    private List<Service> services = new ArrayList<>();

    public MockRepository() {
        this.mockDataStore = Mockito.mock(Datastore.class, Mockito.RETURNS_DEEP_STUBS);
    }

    @Override
    public void initialize() {
        URL fragilityPath = this.getClass().getClassLoader().getResource("json/services.json");

        try {
            this.services = new ObjectMapper().readValue(fragilityPath, new TypeReference<List<Service>>(){});
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Mockito.when(mockDataStore.createQuery(Service.class)
                                  .limit(Mockito.any(Integer.class))
                                  .asList())
               .thenReturn(this.services);
    }

    @Override
    public List<Service> getFragilities() {
        return this.services;
    }

    @Override
    public Datastore getDataStore() {
        return this.mockDataStore;
    }
}

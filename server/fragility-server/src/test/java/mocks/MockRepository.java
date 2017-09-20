package mocks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.services.fragility.dataaccess.IRepository;
import edu.illinois.ncsa.incore.services.fragility.model.FragilitySet;
import org.mockito.Mockito;
import org.mongodb.morphia.Datastore;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MockRepository implements IRepository {
    private Datastore mockDataStore;
    private List<FragilitySet> fragilitySets = new ArrayList<>();

    public MockRepository() {
        this.mockDataStore = Mockito.mock(Datastore.class, Mockito.RETURNS_DEEP_STUBS);
    }

    @Override
    public void initialize() {
        URL fragilityPath = this.getClass().getClassLoader().getResource("json/fragility.json");

        try {
            this.fragilitySets = new ObjectMapper().readValue(fragilityPath, new TypeReference<List<FragilitySet>>(){});
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Mockito.when(mockDataStore.createQuery(FragilitySet.class)
                                  .limit(Mockito.any(Integer.class))
                                  .asList())
               .thenReturn(this.fragilitySets);
    }

    @Override
    public List<FragilitySet> getFragilities() {
        return this.fragilitySets;
    }

    @Override
    public Datastore getDataStore() {
        return this.mockDataStore;
    }
}

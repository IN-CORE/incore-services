package edu.illinois.ncsa.incore.service.hazard.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.service.hazard.models.tsunami.Tsunami;
import edu.illinois.ncsa.incore.service.hazard.models.tsunami.TsunamiDataset;
import org.apache.log4j.Logger;
import org.mockito.Mockito;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.FindOptions;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MockTsunamiRepository implements ITsunamiRepository {
    private static final Logger log = Logger.getLogger(MockTsunamiRepository.class);
    private Datastore mockDataStore;
    private List<Tsunami> tsunamis = new ArrayList<>();

    public MockTsunamiRepository() {
        this.mockDataStore = Mockito.mock(Datastore.class, Mockito.RETURNS_DEEP_STUBS);
    }

    @Override
    public void initialize() {
        URL tsunamiData = this.getClass().getClassLoader().getResource("json/tsunamiData.json");
        try {
            List<TsunamiDataset> tsunamiDatasets = new ObjectMapper().readValue(tsunamiData, new TypeReference<List<TsunamiDataset>>() {
            });

            tsunamis.addAll(tsunamiDatasets);
            Mockito.when(mockDataStore.createQuery(Tsunami.class).asList(new FindOptions().limit(Mockito.any(Integer.class)))).thenReturn(this.tsunamis);
        } catch (IOException e) {
            log.error("Error reading tsunamis", e);
        }
    }

    @Override
    public Tsunami getTsunamiById(String id) {
        return null;
    }

    @Override
    public Tsunami addTsunami(Tsunami tsunami) {
        return null;
    }

    @Override
    public List<Tsunami> getTsunamis() {
        return this.tsunamis;
    }

}

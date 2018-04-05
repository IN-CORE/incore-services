package edu.illinois.ncsa.incore.service.hazard.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.service.hazard.models.eq.ScenarioEarthquake;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.ScenarioTornado;
import org.apache.log4j.Logger;
import org.mockito.Mockito;
import org.mongodb.morphia.Datastore;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MockTornadoRepository implements ITornadoRepository {
    private static final Logger log = Logger.getLogger(MockRepository.class);
    private Datastore mockDataStore;
    private List<ScenarioTornado> scenarioTornadoes = new ArrayList<ScenarioTornado>();

    public MockTornadoRepository() {
        this.mockDataStore = Mockito.mock(Datastore.class, Mockito.RETURNS_DEEP_STUBS);
    }

    @Override
    public void initialize() {
        URL tornadoPath = this.getClass().getClassLoader().getResource("json/scenarioTornadoes.json");
        try {
            this.scenarioTornadoes = new ObjectMapper().readValue(tornadoPath, new TypeReference<List<ScenarioTornado>>() {
            });

            Mockito.when(mockDataStore.createQuery(ScenarioTornado.class).limit(Mockito.any(Integer.class)).asList()).thenReturn(this.scenarioTornadoes);

        } catch (IOException e) {
            log.error("Error reading tornadoes", e);
        }
    }

    @Override
    public ScenarioTornado getScenarioTornadoById(String id) {
        for (int index = 0; index < this.scenarioTornadoes.size(); index++) {
            if (this.scenarioTornadoes.get(index).getId().equalsIgnoreCase(id)) {
                return this.scenarioTornadoes.get(index);
            }
        }
        return null;
    }

    @Override
    public ScenarioTornado addScenarioTornado(ScenarioTornado scenarioTornado) {
        return null;
    }

    @Override
    public List<ScenarioTornado> getScenarioTornadoes() {
        return this.scenarioTornadoes;
    }

    @Override
    public Datastore getDataStore() {
        return this.mockDataStore;
    }


}

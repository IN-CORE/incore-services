package mocks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.services.maestro.dataaccess.IRepository;
import edu.illinois.ncsa.incore.services.maestro.model.Analysis;
import org.mockito.Mockito;
import org.mongodb.morphia.Datastore;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MockRepository implements IRepository {
    private Datastore mockDataStore;
    private List<Analysis> analyses = new ArrayList<>();

    public MockRepository() {
        this.mockDataStore = Mockito.mock(Datastore.class, Mockito.RETURNS_DEEP_STUBS);
    }

    @Override
    public void initialize() {
        URL fragilityPath = this.getClass().getClassLoader().getResource("json/analysis.json");

        try {
            this.analyses = new ObjectMapper().readValue(fragilityPath, new TypeReference<List<Analysis>>(){});
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Mockito.when(mockDataStore.createQuery(Analysis.class)
                                  .limit(Mockito.any(Integer.class))
                                  .asList())
               .thenReturn(this.analyses);
    }

    @Override
    public List<Analysis> getAllAnalyses() {
        return this.analyses;
    }

    @Override
    public Datastore getDataStore() {
        return this.mockDataStore;
    }

    @Override
    public Analysis getAnalysisById(String id) {
        return this.analyses.get(Integer.parseInt(id));
    }

    @Override
    public String addAnalysis(Analysis analysis) {
        this.analyses.add(analysis);
        return String.valueOf(this.analyses.size());
    }


}

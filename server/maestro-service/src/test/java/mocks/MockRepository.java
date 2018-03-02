/*******************************************************************************
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Indira Gutierrez (NCSA) - initial API and implementation
 *******************************************************************************/
package mocks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.service.maestro.daos.IRepository;
import edu.illinois.ncsa.incore.service.maestro.models.Analysis;
import org.mockito.Mockito;
import org.mongodb.morphia.Datastore;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MockRepository implements IRepository {
    private Datastore mockDataStore;
    private List<Analysis> analyses = new ArrayList<>();

    public MockRepository() {
        this.mockDataStore = Mockito.mock(Datastore.class, Mockito.RETURNS_DEEP_STUBS);
    }

    @Override
    public void initialize() {
        URL analysesPath = this.getClass().getClassLoader().getResource("json/analyses.json");

        try {
            this.analyses = new ObjectMapper().readValue(analysesPath, new TypeReference<List<Analysis>>(){});
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
        for(int i =0; i <this.analyses.size(); i++) {
            if(this.analyses.get(i).getId().equalsIgnoreCase(id)) {
                return this.analyses.get(i);
            }
        }
        return null;
    }

    @Override
    public Analysis addAnalysis(Analysis analysis) {
        this.analyses.add(analysis);
        return this.analyses.get(this.analyses.size() - 1);
    }

    @Override
    public List<Analysis> getAnalysis(Map<String, String> queryMap, int offset, int limit){
        List<Analysis> analyses = this.analyses;
        List<Analysis> output = new ArrayList<>();
        for(int i =0; i < analyses.size(); i++){
            boolean include = true;
            Set<String> keys = queryMap.keySet();
            for(int j = 0; j < keys.size(); j++) {
                if(keys.contains("name") && !analyses.get(i).getName().equals(queryMap.get("name"))) {
                    include = false;
                }
                if(keys.contains("category") && !analyses.get(i).getCategory().equals(queryMap.get("category"))) {
                    include = false;
                }
            }
            if(include) {
                output.add(analyses.get(i));
            }
        }

        return output;
    }

}

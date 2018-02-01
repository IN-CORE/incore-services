/*
 * ******************************************************************************
 *   Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 *   This program and the accompanying materials are made available under the
 *   terms of the BSD-3-Clause which accompanies this distribution,
 *   and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *  ******************************************************************************
 */

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
import edu.illinois.ncsa.incore.service.data.dao.IRepository;
import edu.illinois.ncsa.incore.service.data.models.Dataset;
import edu.illinois.ncsa.incore.service.data.models.FileDescriptor;
import edu.illinois.ncsa.incore.service.data.models.Space;
import edu.illinois.ncsa.incore.service.data.models.mvz.MvzDataset;
import org.mockito.Mockito;
import org.mongodb.morphia.Datastore;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MockRepository implements IRepository {
    private Datastore mockDataStore;
    private List<Dataset> datasets = new ArrayList<>();
    private List<Space> spaces = new ArrayList<>();
    private List<MvzDataset> mvzDatasets = new ArrayList<>();

    public MockRepository() {
        this.mockDataStore = Mockito.mock(Datastore.class, Mockito.RETURNS_DEEP_STUBS);
    }

    @Override
    public void initialize() {
        URL datasetsPath = this.getClass().getClassLoader().getResource("json/datasets.json");
        URL spacesPath = this.getClass().getClassLoader().getResource("json/spaces.json");
        URL mvzDatasetsPath = this.getClass().getClassLoader().getResource("json/mvzdatasets.json");

        try {
            this.datasets = new ObjectMapper().readValue(datasetsPath, new TypeReference<List<Dataset>>(){});
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try {
            this.spaces = new ObjectMapper().readValue(spacesPath, new TypeReference<List<Space>>(){});
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // not being tested in this test
//        try {
//            this.mvzDatasets = new ObjectMapper().readValue(mvzDatasetsPath, new TypeReference<List<MvzDataset>>(){});
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }


        Mockito.when(mockDataStore.createQuery(Dataset.class)
                .limit(Mockito.any(Integer.class))
                .asList())
                .thenReturn(this.datasets);
    }

    @Override
    public List<Dataset> getAllDatasets() {
        return this.datasets;
    }

    @Override
    public List<Space> getAllSpaces() {
        return this.spaces;
    }

    @Override
    public List<MvzDataset> getAllMvzDatasets() {
        return this.mvzDatasets;
    }

    @Override
    public Datastore getDataStore() {
        return this.mockDataStore;
    }

    @Override
    public Dataset getDatasetById(String id) {
        for(int i = 0; i <this.datasets.size(); i++) {
            if(this.datasets.get(i).getId().equalsIgnoreCase(id)) {
                return this.datasets.get(i);
            }
        }
        return null;
    }

    @Override
    public List<Dataset> getDatasetByTitle(String title) {
        List<Dataset> outlist = new ArrayList<>();
        for(int i = 0; i <this.datasets.size(); i++) {
            if(this.datasets.get(i).getTitle().equalsIgnoreCase(title)) {
                outlist.add(datasets.get(i));
            }
        }
        return outlist;
    }

    @Override
    public List<Dataset> getDatasetByType(String type){
        List<Dataset> outlist = new ArrayList<>();
        for(int i = 0; i <this.datasets.size(); i++) {
            if(this.datasets.get(i).getType().equalsIgnoreCase(type)) {
                outlist.add(datasets.get(i));
            }
        }
        return outlist;
    }

    @Override
    public List<Dataset> getDatasetByTypeAndTitle(String type, String title) {
        // this will not be tested in here
        return null;
    }

    @Override
    public Dataset getDatasetByFileDescriptorId(String id) {
        for(int i = 0; i <this.datasets.size(); i++) {
            List<FileDescriptor> fileDescriptors = new ArrayList<>();
            fileDescriptors = this.datasets.get(i).getFileDescriptors();
            for (int j = 0; j < fileDescriptors.size(); j++) {
                if (fileDescriptors.get(j).getId().equalsIgnoreCase(id)) {
                    {
                        return this.datasets.get(i);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Dataset addDataset(Dataset dataset) {
        this.datasets.add(dataset);
        return this.datasets.get(this.datasets.size() - 1);
    }

    @Override
    public Dataset updateDataset(String datasetId, String propName, String propValue) {
        // this will not be tested in here
        return null;
    }

    @Override
    public Space getSpaceById(String id) {
        for(int i = 0; i <this.spaces.size(); i++) {
            if(this.spaces.get(i).getId().equalsIgnoreCase(id)) {
                return this.spaces.get(i);
            }
        }
        return null;
    }

    @Override
    public Space getSpaceByName(String name) {
        for(int i = 0; i <this.spaces.size(); i++) {
            if(this.datasets.get(i).getTitle().equalsIgnoreCase(name)) {
                return this.spaces.get(i);
            }
        }
        return null;
    }

    @Override
    public Space addSpace(Space space) {
        this.spaces.add(space);
        return this.spaces.get(this.spaces.size() - 1);
    }

    @Override
    public MvzDataset getMvzDatasetById(String id) {
        // I am not testing this method in here
        return null;
    }

    @Override
    public MvzDataset addMvzDataset(MvzDataset mvzDataset) {
        return null;
    }

}

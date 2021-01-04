/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *   Indira Gutierrez (NCSA) - initial API and implementation
 *******************************************************************************/
package mocks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.morphia.query.FindOptions;
import edu.illinois.ncsa.incore.service.data.dao.IRepository;
import edu.illinois.ncsa.incore.service.data.models.Dataset;
import edu.illinois.ncsa.incore.service.data.models.DatasetType;
import edu.illinois.ncsa.incore.service.data.models.FileDescriptor;
import edu.illinois.ncsa.incore.service.data.models.mvz.MvzDataset;
import org.mockito.Mockito;
import dev.morphia.Datastore;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MockDataRepository implements IRepository {
    private Datastore mockDataStore;
    private List<Dataset> datasets = new ArrayList<>();
    private List<DatasetType> datatypes = new ArrayList<>();
    private List<MvzDataset> mvzDatasets = new ArrayList<>();

    public MockDataRepository() {
        this.mockDataStore = Mockito.mock(Datastore.class, Mockito.RETURNS_DEEP_STUBS);
    }

    @Override
    public void initialize() {
        URL datasetsPath = this.getClass().getClassLoader().getResource("json/datasets.json");
        URL DatatypesPath = this.getClass().getClassLoader().getResource("json/datatypes.json");

        try {
            this.datasets = new ObjectMapper().readValue(datasetsPath, new TypeReference<List<Dataset>>(){});
            this.datatypes = new ObjectMapper().readValue(DatatypesPath, new TypeReference<List<DatasetType>>(){});
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Mockito.when(mockDataStore.find(Dataset.class)
            .find(new FindOptions().limit(Mockito.any(Integer.class))).toList())
            .thenReturn(this.datasets);
    }

    @Override
    public List<Dataset> getAllDatasets() {
        return this.datasets;
    }

    @Override
    public List<Dataset> searchDatasets(String text) {
        List<Dataset> outList = new ArrayList<>();
        for(int i = 0; i <this.datasets.size(); i++) {
            if(this.datasets.get(i).getDescription().contains(text)) {
                outList.add(datasets.get(i));
            }
        }
        return outList;
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
            if(this.datasets.get(i).getDataType().equalsIgnoreCase(type)) {
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
    public Dataset deleteDataset(String id) {
        return null;
    }

    @Override
    public List<FileDescriptor> getAllFileDescriptors(){
        List<FileDescriptor> fileDescriptors = new ArrayList<FileDescriptor>();
        for (Dataset dataset: this.datasets) {
            List<FileDescriptor> fds = dataset.getFileDescriptors();
            fileDescriptors.addAll(fds);
        }

        return fileDescriptors;
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

    @Override
    public List<DatasetType> getDatatypes(String spaceName) {
        return this.datatypes;
    }

}

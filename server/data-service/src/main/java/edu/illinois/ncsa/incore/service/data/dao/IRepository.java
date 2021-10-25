/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Omar Elabd (NCSA) - initial API and implementation
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *******************************************************************************/

package edu.illinois.ncsa.incore.service.data.dao;

import dev.morphia.Datastore;
import edu.illinois.ncsa.incore.service.data.models.Dataset;
import edu.illinois.ncsa.incore.service.data.models.DatasetType;
import edu.illinois.ncsa.incore.service.data.models.FileDescriptor;
import edu.illinois.ncsa.incore.service.data.models.mvz.MvzDataset;

import java.util.List;

public interface IRepository {
    void initialize();

    List<Dataset> getAllDatasets(Boolean excludeHazard);

    Dataset getDatasetById(String id);

    List<Dataset> getDatasetByType(String type, Boolean excludeHazard);

    List<Dataset> getDatasetByTitle(String title, Boolean excludeHazard);

    List<Dataset> getDatasetByCreator(String creator, Boolean withHazard);

    List<Dataset> getDatasetByTypeAndTitle(String type, String title, Boolean excludeHazard);

    List<Dataset> searchDatasets(String text, Boolean excludeHazard);

    Dataset addDataset(Dataset dataset);

    Dataset updateDataset(String datasetId, String propName, String propValue);

    Dataset getDatasetByFileDescriptorId(String id);

    Dataset deleteDataset(String id);

    List<FileDescriptor> getAllFileDescriptors();

    List<MvzDataset> getAllMvzDatasets();

    MvzDataset getMvzDatasetById(String id);

    MvzDataset addMvzDataset(MvzDataset dataset);

    Datastore getDataStore();

    List<DatasetType> getDatatypes(String spaceName);
}

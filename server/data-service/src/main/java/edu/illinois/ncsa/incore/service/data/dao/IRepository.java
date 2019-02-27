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

import edu.illinois.ncsa.incore.service.data.models.FileDescriptor;
import edu.illinois.ncsa.incore.service.data.models.Space;
import edu.illinois.ncsa.incore.service.data.models.Dataset;
import edu.illinois.ncsa.incore.service.data.models.mvz.MvzDataset;
import org.mongodb.morphia.Datastore;

import java.util.List;

public interface IRepository {
    void initialize();
    List<Dataset> getAllDatasets();
    Dataset getDatasetById(String id);
    List<Dataset> getDatasetByType(String type);
    List<Dataset> getDatasetByTitle(String title);
    List<Dataset> getDatasetByTypeAndTitle(String type, String title);
    Dataset addDataset(Dataset dataset);
    Dataset updateDataset(String datasetId, String propName, String propValue);
    Dataset getDatasetByFileDescriptorId(String id);
    Dataset deleteDataset(String id);
    List<Space> getAllSpaces();
    Space addSpace(Space space);
    Space getSpaceById(String id);
    Space getSpaceByName(String name);
    Space deleteSpace(String id);
    Space removeIdFromSpace(Space space, String id);
    List<FileDescriptor> getAllFileDescriptors();
    List<MvzDataset> getAllMvzDatasets();
    MvzDataset getMvzDatasetById(String id);
    MvzDataset addMvzDataset(MvzDataset dataset);
    Datastore getDataStore();
}

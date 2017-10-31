/*
 * ******************************************************************************
 *   Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 *   This program and the accompanying materials are made available under the
 *   terms of the BSD-3-Clause which accompanies this distribution,
 *   and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 *   Contributors:
 *   Omar Elabd (NCSA) - initial API and implementation
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *  ******************************************************************************
 */

package edu.illinois.ncsa.incore.service.data.dao;

import edu.illinois.ncsa.incore.service.data.model.Space;
import edu.illinois.ncsa.incore.service.data.model.datawolf.domain.Dataset;
import edu.illinois.ncsa.incore.service.data.model.mvz.MvzDataset;
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
    List<Space> getAllSpaces();
    Space addSpace(Space space);
    Space getSpaceByName(String name);
    MvzDataset addMvzDataset(MvzDataset dataset);
    Datastore getDataStore();
}

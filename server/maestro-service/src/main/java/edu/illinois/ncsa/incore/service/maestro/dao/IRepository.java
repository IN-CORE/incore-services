/*******************************************************************************
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Indira Gutierrez (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.maestro.dao;

import edu.illinois.ncsa.incore.service.maestro.models.Analysis;
import org.mongodb.morphia.Datastore;

import java.util.List;

public interface IRepository {
    void initialize();
    List<Analysis> getAllAnalyses();
    Analysis getAnalysisById(String id);
    Analysis addAnalysis(Analysis analysis);
    Datastore getDataStore();
}

/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Indira Gutierrez (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.maestro.daos;

import edu.illinois.ncsa.incore.service.maestro.models.Analysis;
import edu.illinois.ncsa.incore.service.maestro.models.AnalysisMetadata;
import dev.morphia.morphia.Datastore;

import java.util.List;
import java.util.Map;

public interface IRepository {
    void initialize();
    List<Analysis> getAllAnalyses();
    List<Analysis> getAnalysis(Map<String, String> queryParams, int offset, int limit);
    Analysis getAnalysisById(String id);
    Analysis addAnalysis(Analysis analysis);
    Datastore getDataStore();
}

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

package edu.illinois.ncsa.incore.service.data.model.mvz;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by ywkim on 8/2/2017.
 */
public class MaevizMapping {
    private String schema;
    private List<Mapping> mapping = new LinkedList<Mapping>();

    public String getSchema() {
        return schema;
    }
    public void setSchema(String schema){
        this.schema = schema;
    }

    public List<Mapping> getMapping() { return mapping; }
    public void setMapping(List<Mapping> mapping) {
        this.mapping = mapping;
    }

}

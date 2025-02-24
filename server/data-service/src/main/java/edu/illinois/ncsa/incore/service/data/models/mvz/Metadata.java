/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *******************************************************************************/

package edu.illinois.ncsa.incore.service.data.models.mvz;

/**
 * Created by ywkim on 7/31/2017.
 */
public class Metadata {
    public TableMetadata tableMetadata = new TableMetadata();

    public TableMetadata getTableMetadata() {
        return tableMetadata;
    }

    public void setTableMetadata(TableMetadata tableMetadata) {
        this.tableMetadata = this.tableMetadata;
    }
}

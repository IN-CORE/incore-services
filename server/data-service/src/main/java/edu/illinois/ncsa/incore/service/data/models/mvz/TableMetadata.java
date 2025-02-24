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

import java.util.LinkedList;
import java.util.List;

/**
 * Created by ywkim on 8/2/2017.
 */
public class TableMetadata {
    private List<ColumnMetadata> columnMetadata = new LinkedList<ColumnMetadata>();

    public List<ColumnMetadata> getColumnMetadata() {
        return columnMetadata;
    }

    public void setColumnMetadata(List<ColumnMetadata> columnMetadata) {
        this.columnMetadata = columnMetadata;
    }
}

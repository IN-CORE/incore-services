/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Chris Navarro (NCSA) - initial API and implementation
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *******************************************************************************/

package edu.illinois.ncsa.incore.service.project.models;

/*
 * Created by ywkim on 9/26/2017.
 * This is from NCSA's DataWolf
 */

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.morphia.annotations.Embedded;

@Embedded
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileDescriptor {
    public static final long serialVersionUID = 1L;

    public String id;
    public boolean deleted = false;
    public String filename = null;
    public String mimeType = "";
    public long size = -1;
    public String dataURL;
    public String md5sum;

    public FileDescriptor() {
    }
}

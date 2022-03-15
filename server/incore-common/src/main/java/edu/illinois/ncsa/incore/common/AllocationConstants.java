/*******************************************************************************
 * Copyright (c) 2022 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Yong Wook Kim (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.common;

public class AllocationConstants {
    public static final int NUM_DATASETS = 100;
    public static final int NUM_HAZARDS = 500;
    public static final int NUM_HAZARD_DATASETS = 500;
    public static final int NUM_DFR3 = 500;
    public static final long DATASET_SIZE = 524288000L;
    public static final long HAZARD_DATASET_SIZE = 2147483648L;
    public static final String DATASET_ALLOCATION_MESSAGE =
        "You have reached the maximum number of datasets you can create for your allocation. " +
            "Your request will be aborted. You either need to increase your allocation " +
            "or delete datasets you've created.";
    public static final String HAZARD_ALLOCATION_MESSAGE =
        "You have reached the maximum number of hazards you can create for your allocation. " +
            "Your request will be aborted. You either need to increase your allocation " +
            "or delete hazards you've created.";
    public static final String HAZARD_DATASET_ALLOCATION_MESSAGE =
        "You have reached the maximum number of hazard datasets you can create for your allocation. " +
            "Your request will be aborted. You either need to increase your allocation " +
            "or delete hazard datasets you've created.";
    public static final String DATASET_ALLOCATION_FILESIZE_MESSAGE =
        "You have reached the maximum file size of datasets you can create for your allocation. " +
            "Your request will be aborted. You either need to increase your allocation " +
            "or delete datasets you've created.";
    public static final String HAZARD_DATASET_ALLOCATION_FILESIZE_MESSAGE =
        "You have reached the maximum file size of hazard datasets you can create for your allocation. " +
            "Your request will be aborted. You either need to increase your allocation " +
            "or delete hazard datasets you've created.";
    public static final String HAZARD_DFR3_ALLOCATION_MESSAGE =
        "You have reached the maximum number of dfr3 datasets you can create for your allocation. " +
            "Your request will be aborted. You either need to increase your allocation " +
            "or delete dfr3 datasets you've created.";
}

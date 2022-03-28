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
    public static final int NUM_DATASETS = 1000;
    public static final int NUM_HAZARDS = 1000;
    public static final int NUM_HAZARD_DATASETS = 1000;
    public static final int NUM_DFR3 = 1000;
    public static final long DATASET_SIZE = 21474836480L;
    public static final long HAZARD_DATASET_SIZE = 21474836480L;
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
        "You have reached the maximum number of dfr3 objects you can create for your allocation. " +
            "Your request will be aborted. You either need to increase your allocation " +
            "or delete dfr3 objects you've created.";
    public static final String ALLOCTION_ENDPOINT_NO_USERGROUP =
        "Your authentication information doesn't have the x-auth-usergroup header with the request. " +
            "It is needed to verify the user's privileges";
    public static final String ALLOCTION_ENDPOINT_NO_USERINFO =
        "Your authentication information doesn't have the x-auth-userinfo header with the request. " +
            "It is needed to verify the user's privileges";
    public static final String UNABLE_TO_PARSE_TOKEN =
        "Unable to parse the user's authentication information. " +
            "It is needed to verify the user's privileges and other information";
}

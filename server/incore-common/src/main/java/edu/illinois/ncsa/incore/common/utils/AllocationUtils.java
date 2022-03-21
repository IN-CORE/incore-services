/*******************************************************************************
 * Copyright (c) 2022 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *******************************************************************************/

package edu.illinois.ncsa.incore.common.utils;

import edu.illinois.ncsa.incore.common.AllocationConstants;
import edu.illinois.ncsa.incore.common.dao.IUserFinalQuotaRepository;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.common.models.UserAllocations;
import edu.illinois.ncsa.incore.common.models.UserFinalQuota;
import edu.illinois.ncsa.incore.common.models.UserUsages;
import edu.illinois.ncsa.incore.common.dao.IUserAllocationsRepository;


import javax.ws.rs.core.Response;

public class AllocationUtils {

    /***
     * This method receives a username and to see if the user's number of dataset is within the allocation.
     *
     * @param allocationRepository IUserAllocationsRepository injected by controller
     * @param quotaRepository IUserFinalQuotaRepository injected by controller
     * @param username string representation of username
     * @param datasetType string for representing the type of the dataset
     * @return postOk boolean if the user can post more datasets
     */
    public static Boolean canCreateAnyDataset(IUserAllocationsRepository allocationRepository,
                                           IUserFinalQuotaRepository quotaRepository,
                                           String username, String datasetType) {
        // check allocation first
        UserAllocations allocation = allocationRepository.getAllocationByUsername(username);   // get default allocation
        UserFinalQuota quota = quotaRepository.getQuotaByUsername(username);
        Boolean postOk = false;

        // if allocations is null, it means that this post should be the very first post by the user
        // so the check should be passed.
        if (allocation == null) {
            // First POST, no need to check the allocation
            // use default allocation
            postOk = true;
        } else {
            // get user's quota information
            if (quota == null) {
                quota = new UserFinalQuota();
            }

            // get user's usage status
            UserUsages usage = allocation.getUsage();

            // get user's limit
            UserUsages limit = quota.getApplicableLimits();

            // check if there is the correct limit values is there, otherwise give default values
            if (limit.getDatasets() == 0) {
                limit = AllocationUtils.setDefalutLimit(limit);
            }

            // the dataset types should only be
            // datasets, hazards, hazardDatasets, datasetSize, hazardDatasetSize, dfr3
            // check if the user's dataset number is within the allocation
            if (datasetType == "datasets") {
                if (usage.getDatasets() < limit.getDatasets()) {
                    postOk = true;
                }
            } else if (datasetType == "hazards") {
                if (usage.getHazards() < limit.getHazards()) {
                    postOk = true;
                }
            } else if (datasetType == "hazardDatasets") {
                if (usage.getHazardDatasets() < limit.getHazardDatasets()) {
                    postOk = true;
                }
            } else if (datasetType == "datasetSize") {
                if (usage.getDatasetSize() < limit.getDatasetSize()) {
                    postOk = true;
                }
            } else if (datasetType == "hazardDatasetSize") {
                if (usage.getHazardDatasetSize() < limit.getHazardDatasetSize()) {
                    postOk = true;
                }
            } else if (datasetType == "dfr3") {
                if (usage.getDfr3() < limit.getDfr3()) {
                    postOk = true;
                }
            } else {
                postOk = false;
            }
        }

        return postOk;
    }

    /***
     * This method sets up the default limit information
     *
     * @param limit
     * @return
     */
    public static UserUsages setDefalutLimit(UserUsages limit) {
        limit.setDatasets(AllocationConstants.NUM_DATASETS);
        limit.setDatasetSize(AllocationConstants.DATASET_SIZE);
        limit.setHazards(AllocationConstants.NUM_HAZARDS);
        limit.setHazardDatasets(AllocationConstants.NUM_HAZARD_DATASETS);
        limit.setHazardDatasetSize(AllocationConstants.HAZARD_DATASET_SIZE);
        limit.setDfr3(AllocationConstants.NUM_DFR3);

        return limit;
    }

    /***
     * increase the number of usage in the user allocation
     *
     * @param allocationsRepository
     * @param username
     * @param datasetType
     */
    public static void increaseUsage(IUserAllocationsRepository allocationsRepository, String username, String datasetType) {
        UserAllocations allocation = allocationsRepository.getAllocationByUsername(username);
        UserUsages usage = new UserUsages();

        if (allocation != null) {
            usage = allocation.getUsage();
        } else {
            allocation = new UserAllocations();
            allocation.setUsername(username);
        }

        // the dataset types should only be
        // datasets, hazards, hazardDatasets, datasetSize, hazardDatasetSize, dfr3
        if (datasetType == "datasets") {
            usage.setDatasets(usage.getDatasets() + 1);
            allocation.setUsage(usage);
        } else if (datasetType == "hazards") {
            usage.setHazards(usage.getHazards() + 1);
            allocation.setUsage(usage);
        } else if (datasetType == "hazardDatasets") {
            usage.setHazardDatasets(usage.getHazardDatasets() + 1);
            allocation.setUsage(usage);
        } else if (datasetType == "dfr3") {
            usage.setDfr3(usage.getDfr3() + 1);
            allocation.setUsage(usage);
        }

        UserAllocations updatedAllocation = allocationsRepository.addAllocation(allocation);
        if (updatedAllocation == null) {
            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR,
                "There was an unexpected error when trying to modify the usage of user's allocation.");
        }
    }

    /***
     * decrease the number of usage in the user allocation
     *
     * @param allocationsRepository
     * @param username
     * @param datasetType
     */
    public static void decreaseUsage(IUserAllocationsRepository allocationsRepository, String username, String datasetType) {
        UserAllocations allocation = allocationsRepository.getAllocationByUsername(username);
        UserUsages usage = new UserUsages();

        if (allocation != null) {
            usage = allocation.getUsage();
        } else {
            allocation = new UserAllocations();
            allocation.setUsername(username);
        }

        // the dataset types should only be
        // datasets, hazards, hazardDatasets, datasetSize, hazardDatasetSize, dfr3
        if (datasetType == "datasets") {
            usage.setDatasets(usage.getDatasets() - 1);
            allocation.setUsage(usage);
        } else if (datasetType == "hazards") {
            usage.setHazards(usage.getHazards() - 1);
            allocation.setUsage(usage);
        } else if (datasetType == "hazardDatasets") {
            usage.setHazardDatasets(usage.getHazardDatasets() - 1);
            allocation.setUsage(usage);
        } else if (datasetType == "dfr3") {
            usage.setDfr3(usage.getDfr3() - 1);
            allocation.setUsage(usage);
        }

        UserAllocations updatedAllocation = allocationsRepository.addAllocation(allocation);
        if (updatedAllocation == null) {
            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR,
                "There was an unexpected error when trying to modify the usage of user's allocation.");
        }
    }

    /***
     * increase the file size of dataset in the user allocation
     *
     * @param allocation
     * @param allocationsRepository
     * @param fileSize
     * @param isHazard
     */
    public static void increaseDatasetFileSize(UserAllocations allocation, IUserAllocationsRepository allocationsRepository,
                                               long fileSize, Boolean isHazard) {
        if (allocation != null) {
            UserUsages usage = allocation.getUsage();
            if (isHazard) {
                usage.setHazardDatasetSize(usage.getHazardDatasetSize() + fileSize);
            } else {
                usage.setDatasetSize(usage.getDatasetSize() + fileSize);
            }
            allocation.setUsage(usage);

            UserAllocations updatedAllocation = allocationsRepository.addAllocation(allocation);
            if (updatedAllocation == null) {
                throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "There was an unexpected error when trying to add " +
                    "the dataset's file size to user's allocation.");
            }
        }
    }

    /***
     * decrease the file size of dataset in the user allocation
     *
     * @param allocation
     * @param allocationsRepository
     * @param fileSize
     * @param isHazard
     */
    public static void decreaseDatasetFileSize(UserAllocations allocation, IUserAllocationsRepository allocationsRepository,
                                               long fileSize, Boolean isHazard) {
        if (allocation != null) {
            UserUsages usage = allocation.getUsage();
            if (isHazard) {
                if (usage.getHazardDatasetSize() - fileSize > 0) {
                    usage.setHazardDatasetSize(usage.getHazardDatasetSize() - fileSize);
                } else {
                    usage.setHazardDatasetSize(0);
                }
            } else {
                if (usage.getDatasetSize() - fileSize > 0) {
                    usage.setDatasetSize(usage.getDatasetSize() - fileSize);
                } else {
                    usage.setDatasetSize(0);
                }
            }
            allocation.setUsage(usage);

            UserAllocations updatedAllocation = allocationsRepository.addAllocation(allocation);
            if (updatedAllocation == null) {
                throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "There was an unexpected error when trying to add " +
                    "the dataset's file size to user's allocation.");
            }
        }
    }
}

/*******************************************************************************
 * Copyright (c) 2021 University of Illinois and others.  All rights reserved.
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
     * @return postOk boolean if the user can post more datasets
     */
    public static Boolean canCreateDataset(IUserAllocationsRepository allocationRepository,
                                           IUserFinalQuotaRepository quotaRepository,
                                           String username) {
        // check allocation first
        UserAllocations allocation = allocationRepository.getAllocationByUsername(username);   // get default allocation
        UserFinalQuota quota = quotaRepository.getQuotaByUsername(username);
        UserUsages usage = new UserUsages();
        UserUsages limit = new UserUsages();
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
            usage = allocation.getUsage();

            // get user's limit
            limit = quota.getApplicationLimits();

            // check if there is the correct limit values is there, otherwise give default values
            if (limit.getDatasets() == 0) {
                limit = AllocationUtils.setDefalutLimit(limit);
            }

            // check if the user's dataset number is within the allocation
            if (usage.getDatasets() < limit.getDatasets()) {
                postOk = true;
            }
        }

        return postOk;
    }

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
     * This method receives a username and to see if the user's number of hazard is within the allocation.
     *
     * @param allocationRepository IUserAllocationsRepository injected by controller
     * @param quotaRepository IUserFinalQuotaRepository injected by controller
     * @param username string representation of username
     * @return postOk boolean if the user can post more datasets
     */
    public static Boolean canCreateHazard(IUserAllocationsRepository allocationRepository,
                                          IUserFinalQuotaRepository quotaRepository,
                                          String username) {
        // check allocation first
        UserAllocations allocation = allocationRepository.getAllocationByUsername(username);   // get default allocation
        UserFinalQuota quota = quotaRepository.getQuotaByUsername(username);
        UserUsages usage = new UserUsages();
        UserUsages limit = new UserUsages();
        Boolean postOk = false;

        // if space is null, it means that this post should be the very first post by the user
        // so the check should be passed.
        if (allocation == null) {
            // First POST, no need to check the allocation
            postOk = true;
        } else {
            // get user's quota information
            if (quota == null) {
                quota = new UserFinalQuota();
            }

            // get user's usage status
            usage = allocation.getUsage();

            // get limit
            limit = quota.getApplicationLimits();

            // check if there is the correct limit values is there, otherwise give default values
            if (limit.getDatasets() == 0) {
                limit = AllocationUtils.setDefalutLimit(limit);
            }

            // check if the user's dataset number is within the allocation
            if (usage.getHazards() < limit.getHazards()) {
                postOk = true;
            }
        }

        return postOk;
    }

    /***
     * This method receives a username and to see if the user's number of hazard dataset is within the allocation.
     *
     * @param allocationRepository IUserAllocationsRepository injected by controller
     * @param quotaRepository IUserFinalQuotaRepository injected by controller
     * @param username string representation of username
     * @return postOk boolean if the user can post more datasets
     */
    public static Boolean canCreateHazardDataset(IUserAllocationsRepository allocationRepository,
                                                 IUserFinalQuotaRepository quotaRepository,
                                                 String username) {
        // check allocation first
        UserAllocations allocation = allocationRepository.getAllocationByUsername(username);   // get default allocation
        UserFinalQuota quota = quotaRepository.getQuotaByUsername(username);
        UserUsages limit = new UserUsages();
        UserUsages usage = new UserUsages();
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
            usage = allocation.getUsage();

            // get user's limit
            limit = quota.getApplicationLimits();

            // check if there is the correct limit values is there, otherwise give default values
            if (limit.getDatasets() == 0) {
                limit = AllocationUtils.setDefalutLimit(limit);
            }

            // check if the user's dataset number is within the allocation
            if (usage.getHazardDatasets() < limit.getHazardDatasets()) {
                postOk = true;
            }
        }

        return postOk;
    }

    /***
     * This method receives a username and to see if the user's number of hazard dataset is within the allocation.
     *
     * @param allocationRepository IUserAllocationsRepository injected by controller
     * @param quotaRepository IUserFinalQuotaRepository injected by controller
     * @param username string representation of username
     * @param isHazard boolean for indicating if the dataset is the regular dataset or hazard dataset
     * @return postOk boolean if the user can post more datasets
     */
    public static Boolean canAttachFile(IUserAllocationsRepository allocationRepository,
                                                 IUserFinalQuotaRepository quotaRepository,
                                                 String username, Boolean isHazard) {
        // check allocation first
        UserAllocations allocation = allocationRepository.getAllocationByUsername(username);   // get default allocation
        UserFinalQuota quota = quotaRepository.getQuotaByUsername(username);
        UserUsages limit = new UserUsages();
        UserUsages usage = new UserUsages();
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
            usage = allocation.getUsage();

            // get user's limit
            limit = quota.getApplicationLimits();

            // check if there is the correct limit values is there, otherwise give default values
            if (limit.getDatasets() == 0) {
                limit = AllocationUtils.setDefalutLimit(limit);
            }

            // check if the user's dataset number is within the allocation
            if (isHazard) {
                if (usage.getHazardDatasetSize() < limit.getHazardDatasetSize()) {
                    postOk = true;
                }
            } else {
                if (usage.getDatasetSize() < limit.getDatasetSize()) {
                    postOk = true;
                }
            }
        }

        return postOk;
    }

    /***
     * Add one more hazard number in the user allocation
     *
     * @param allocation a UserAllocations object
     * @return allocation a UserAllocations object
     */
    public static UserAllocations addNumHazard(UserAllocations allocation) {
        UserUsages usage = allocation.getUsage();
        usage.setHazards(usage.getHazards() + 1);
        allocation.setUsage(usage);

        return allocation;
    }

    /***
     * Add one more dataset number in the user allocation
     *
     * @param allocation a UserAllocations object
     * @return allocation a UserAllocations object
     */
    public static UserAllocations addNumDataset(UserAllocations allocation) {
        UserUsages usage = allocation.getUsage();
        usage.setDatasets(usage.getDatasets() + 1);
        allocation.setUsage(usage);

        return allocation;
    }

    /***
     * Add one more hazard dataset number in the user allocation
     *
     * @param allocation a UserAllocations object
     * @return allocation a UserAllocations object
     */
    public static UserAllocations addNumHazardDataset(UserAllocations allocation) {
        UserUsages usage = allocation.getUsage();
        usage.setHazardDatasets(usage.getHazardDatasets() + 1);
        allocation.setUsage(usage);

        return allocation;
    }

    /***
     * Remove one dataset number in the user allocation
     *
     * @param allocation a UserAllocations object
     * @return allocation a UserAllocations object
     */
    public static UserAllocations removeNumDataset(UserAllocations allocation) {
        UserUsages usage = allocation.getUsage();
        usage.setDatasets(usage.getDatasets() - 1);
        allocation.setUsage(usage);

        return allocation;
    }

    /***
     * Remove one hazard number in the user allocation
     *
     * @param allocation a UserAllocations object
     * @return allocation a UserAllocations object
     */
    public static UserAllocations removeNumHazard(UserAllocations allocation) {
        UserUsages usage = allocation.getUsage();
        usage.setHazards(usage.getHazards() - 1);
        allocation.setUsage(usage);

        return allocation;
    }

    /***
     * Remove one hazard dataset number in the user allocation
     *
     * @param allocation a UserAllocations object
     * @return allocation a UserAllocations object
     */
    public static UserAllocations removeNumHazardDataset(UserAllocations allocation) {
        UserUsages usage = allocation.getUsage();
        usage.setHazardDatasets(usage.getHazardDatasets() - 1);
        allocation.setUsage(usage);

        return allocation;
    }

    /***
     * Reduce one number of the hazard from the user allocation and update
     *
     * @param allocationsRepository
     * @param username
     * @return
     */
    public static void reduceNumHazard(IUserAllocationsRepository allocationsRepository, String username) {
        // reduce the number of hazard from the space
        UserAllocations allocation = allocationsRepository.getAllocationByUsername(username);
        if (allocation != null) {
            // remove one dataset in the usage
            allocation = AllocationUtils.removeNumHazard(allocation);
            UserAllocations updatedAllocation = allocationsRepository.addAllocation(allocation);
            if (updatedAllocation == null) {
                throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "There was an unexpected error when trying to remove " +
                    "the hazard number to user's allocation.");
            }
        }
    }

    /***
     * Reduce one number of the dataset from the user allocation and update
     *
     * @param allocationsRepository
     * @param username
     * @return
     */
    public static void reduceNumDataset(IUserAllocationsRepository allocationsRepository, String username) {
        // reduce the number of hazard from the user allocation
        UserAllocations allocation = allocationsRepository.getAllocationByUsername(username);
        if (allocation != null) {
            // remove one dataset in the usage
            allocation = AllocationUtils.removeNumDataset(allocation);
            UserAllocations updatedAllocation = allocationsRepository.addAllocation(allocation);
            if (updatedAllocation == null) {
                throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "There was an unexpected error when trying to removing " +
                    "the dataset number to user's allocation.");
            }
        }
    }

    /***
     * Reduce one number of the hazard dataset from the user allocation and update
     *
     * @param allocationsRepository
     * @param username
     * @return
     */
    public static void reduceNumHazardDataset(IUserAllocationsRepository allocationsRepository, String username) {
        // reduce the number of hazard from the space
        UserAllocations allocation = allocationsRepository.getAllocationByUsername(username);
        if (allocation != null) {
            // remove one dataset in the usage
            allocation = AllocationUtils.removeNumHazardDataset(allocation);
            UserAllocations updatedAllocation = allocationsRepository.addAllocation(allocation);
            if (updatedAllocation == null) {
                throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "There was an unexpected error when trying to removing " +
                    "the hazard dataset number to user's allocation.");
            }
        }
    }

    /***
     * increase the number of hazard in the user allocation
     *
     * @param allocationsRepository
     * @param username
     */
    public static void increaseNumHazards(IUserAllocationsRepository allocationsRepository, String username) {
        UserAllocations allocation = allocationsRepository.getAllocationByUsername(username);
        if (allocation != null) {
            allocation = AllocationUtils.addNumHazard(allocation);

            UserAllocations updatedAllocation = allocationsRepository.addAllocation(allocation);
            if (updatedAllocation == null) {
                throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "There was an unexpected error when trying to add " +
                    "the hazard to user's allocation.");
            }
        }
    }

    /***
     * increase the number of dataset in the user allocation
     *
     * @param allocation
     * @param allocationsRepository
     */
    public static void increaseNumDataset(UserAllocations allocation, IUserAllocationsRepository allocationsRepository) {
        if (allocation != null) {
            allocation = AllocationUtils.addNumDataset(allocation);

            UserAllocations updatedAllocation = allocationsRepository.addAllocation(allocation);
            if (updatedAllocation == null) {
                throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "There was an unexpected error when trying to add " +
                    "the dataset to user's allocation.");
            }
        }
    }

    /***
     * increase the number of dataset in the user allocation
     *
     * @param allocation
     * @param allocationsRepository
     */
    public static void increaseNumHazardDataset(UserAllocations allocation, IUserAllocationsRepository allocationsRepository) {
        if (allocation != null) {
            allocation = AllocationUtils.addNumHazardDataset(allocation);

            UserAllocations updatedAllocation = allocationsRepository.addAllocation(allocation);
            if (updatedAllocation == null) {
                throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "There was an unexpected error when trying to add " +
                    "the hazard dataset to user's allocation.");
            }
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

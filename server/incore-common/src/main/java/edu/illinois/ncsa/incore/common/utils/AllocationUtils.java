package edu.illinois.ncsa.incore.common.utils;

import edu.illinois.ncsa.incore.common.auth.Authorizer;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.common.models.Allocation;
import edu.illinois.ncsa.incore.common.models.Space;
import edu.illinois.ncsa.incore.common.models.SpaceUsage;
import edu.illinois.ncsa.incore.common.dao.IAllocationRepository;
import edu.illinois.ncsa.incore.common.dao.ISpaceRepository;
import edu.illinois.ncsa.incore.common.dao.MongoSpaceDBRepository;
import edu.illinois.ncsa.incore.common.dao.MongoAllocationDBRepository;
import org.apache.log4j.Logger;


import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;

public class AllocationUtils {

    /***
     * This method receives a username and to see if the user's number of dataset is within the allocation.
     * @param allocationRepository IAllocationRepository injected by controller
     * @param spaceRepository ISpaceRepository injected by controller
     * @param username string representation of username
     * @return postOk boolean if the user can post more datasets
     */
    public static Boolean checkNumDataset(IAllocationRepository allocationRepository,
                                         ISpaceRepository spaceRepository,
                                         String username) {
        // check allocation first
        Space space = spaceRepository.getSpaceByName(username);
        Allocation allocation = new Allocation();   // get default allocation
        SpaceUsage usage = new SpaceUsage();
        Boolean postOk = false;

        // if space is null, it means that this post should be the very first post by the user
        // so the check should be passed.
        if (space == null) {
            // First POST, no need to check the allocation
            postOk = true;
        } else {
            String spaceId = space.getId();
            usage = space.getUsage();
            // check if there is special allocation for the user
//                spaceId = "5a284f09c7d30d13bc0819a3";
            allocation = allocationRepository.getAllocationBySpaceId(spaceId);
            if (allocation == null) {
                // use default allocation
                allocation = new Allocation();
            }

            // get user's usage status
            usage = space.getUsage();

            // check if the user's dataset number is within the allocation
            if (usage.getDatasets() < allocation.getDatasets()) {
                postOk = true;
            }
        }

        return postOk;
    }

    /***
     * This method receives a username and to see if the user's number of hazard is within the allocation.
     * @param allocationRepository IAllocationRepository injected by controller
     * @param spaceRepository ISpaceRepository injected by controller
     * @param username string representation of username
     * @return postOk boolean if the user can post more datasets
     */
    public static Boolean checkNumHazard(IAllocationRepository allocationRepository,
                                          ISpaceRepository spaceRepository,
                                          String username) {
        // check allocation first
        Space space = spaceRepository.getSpaceByName(username);
        Allocation allocation = new Allocation();   // get default allocation
        SpaceUsage usage = new SpaceUsage();
        Boolean postOk = false;

        // if space is null, it means that this post should be the very first post by the user
        // so the check should be passed.
        if (space == null) {
            // First POST, no need to check the allocation
            postOk = true;
        } else {
            String spaceId = space.getId();
            usage = space.getUsage();
            // check if there is special allocation for the user
//                spaceId = "5a284f09c7d30d13bc0819a3";
            allocation = allocationRepository.getAllocationBySpaceId(spaceId);
            if (allocation == null) {
                // use default allocation
                allocation = new Allocation();
            }

            // get user's usage status
            usage = space.getUsage();

            // check if the user's dataset number is within the allocation
            if (usage.getHazards() < allocation.getHazards()) {
                postOk = true;
            }
        }

        return postOk;
    }

    /***
     * Add one more hazard number in the space
     *
     * @param space a Space object
     * @return space a Space object
     */
    public static Space addNumHazard(Space space) {
        SpaceUsage usage = space.getUsage();
        usage.setHazards(usage.getHazards() + 1);
        space.setUsage(usage);

        return space;
    }

    /***
     * Add one more dataset number in the space
     *
     * @param space a Space object
     * @return space a Space object
     */
    public static Space addNumDataset(Space space) {
        SpaceUsage usage = space.getUsage();
        usage.setDatasets(usage.getDatasets() + 1);
        space.setUsage(usage);

        return space;
    }

    /***
     * Remove one dataset number in the space
     *
     * @param space a Space object
     * @return space a Space object
     */
    public static Space removeNumDataset(Space space) {
        SpaceUsage usage = space.getUsage();
        usage.setDatasets(usage.getDatasets() - 1);
        space.setUsage(usage);

        return space;
    }

    /***
     * Remove one hazard number in the space
     *
     * @param space a Space object
     * @return space a Space object
     */
    public static Space removeNumHazard(Space space) {
        SpaceUsage usage = space.getUsage();
        usage.setHazards(usage.getHazards() - 1);
        space.setUsage(usage);

        return space;
    }

    /***
     * Reduce one number of the hazard from the space and update
     *
     * @param spaceRepository
     * @param username
     * @return
     */
    public static void reduceNumHazard(ISpaceRepository spaceRepository, String username) {
        // reduce the number of hazard from the space
        Space space = spaceRepository.getSpaceByName(username);
        if (space != null) {
            // remove one dataset in the usage
            space = AllocationUtils.removeNumHazard(space);
            Space updated_space = spaceRepository.addSpace(space);
            if (updated_space == null) {
                throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "There was an unexpected error when trying to remove " +
                    "the hazard number to user's space.");
            }
        }
    }

    /***
     * Reduce one number of the dataset from the space and update
     *
     * @param spaceRepository
     * @param username
     * @return
     */
    public static void reduceNumDataset(ISpaceRepository spaceRepository, String username) {
        // reduce the number of hazard from the space
        Space space = spaceRepository.getSpaceByName(username);
        if (space != null) {
            // remove one dataset in the usage
            space = AllocationUtils.removeNumDataset(space);
            Space updated_space = spaceRepository.addSpace(space);
            if (updated_space == null) {
                throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "There was an unexpected error when trying to removing " +
                    "the dataset number to user's space.");
            }
        }
    }

    /***
     * increase the number of hazard in the space
     *
     * @param spaceRepository
     * @param username
     */
    public static void increaseNumHazards(ISpaceRepository spaceRepository, String username) {
        Space space = spaceRepository.getSpaceByName(username);
        if (space != null) {
            space = AllocationUtils.addNumHazard(space);

            Space updated_space = spaceRepository.addSpace(space);
            if (updated_space == null) {
                throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "There was an unexpected error when trying to add " +
                    "the dataset to user's space.");
            }
        }
    }

    /***
     * increase the number of dataset in the space
     *
     * @param spaceRepository
     * @param username
     */
    public static void increaseNumDataset(ISpaceRepository spaceRepository, String username) {
        Space space = spaceRepository.getSpaceByName(username);
        if (space != null) {
            space = AllocationUtils.addNumHazard(space);

            Space updated_space = spaceRepository.addSpace(space);
            if (updated_space == null) {
                throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "There was an unexpected error when trying to add " +
                    "the dataset to user's space.");
            }
        }
    }
}

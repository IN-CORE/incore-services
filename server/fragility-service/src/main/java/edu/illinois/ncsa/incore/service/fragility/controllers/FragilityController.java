/*
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 */

package edu.illinois.ncsa.incore.service.fragility.controllers;

import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.auth.Privileges;
import edu.illinois.ncsa.incore.service.fragility.daos.IFragilityDAO;
import edu.illinois.ncsa.incore.service.fragility.models.FragilitySet;
import io.swagger.annotations.*;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Api(value="fragilities", authorizations = {})
@Path("fragilities")
public class FragilityController {
    private static final Logger logger = Logger.getLogger(FragilityController.class);

    @Inject
    private IFragilityDAO fragilityDAO;

    @Inject
    private IAuthorizer authorizer;

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public List<FragilitySet> getFragilities(@HeaderParam("X-Credential-Username") String username,
                                             @QueryParam("demand") String demandType, @QueryParam("hazard") String hazardType,
                                             @QueryParam("inventory") String inventoryType, @QueryParam("author") String author,
                                             @QueryParam("legacy_id") String legacyId, @QueryParam("skip") int offset,
                                             @QueryParam("creator") String creator,
                                             @DefaultValue("100") @QueryParam("limit") int limit) {

        Map<String, String> queryMap = new HashMap<>();

        if (legacyId != null) {
            queryMap.put("legacyId", legacyId);
        }

        if (demandType != null) {
            queryMap.put("demandType", demandType);
        }

        if (hazardType != null) {
            queryMap.put("hazardType", hazardType);
        }

        if (inventoryType != null) {
            queryMap.put("inventoryType", inventoryType);
        }

        if (creator != null) {
            queryMap.put("creator", creator);
        }

        if (author != null) {

        }

        List<FragilitySet> fragilitySets;

        if (queryMap.isEmpty()) {
            // return top 100
            fragilitySets = this.fragilityDAO.getCachedFragilities()
                .stream()
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
        } else {
            // return query
            fragilitySets = this.fragilityDAO.queryFragilities(queryMap, offset, limit);
        }

        return fragilitySets.stream()
            .filter(b -> authorizer.canRead(username, b.getPrivileges()))
            .collect(Collectors.toList());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON})
    public FragilitySet uploadFragilitySet(@HeaderParam("X-Credential-Username") String username, FragilitySet fragilitySet) {
        fragilitySet.setPrivileges(Privileges.newWithSingleOwner(username));
        fragilitySet.setCreator(username);
        this.fragilityDAO.saveFragility(fragilitySet);
        return fragilitySet;
    }

    @GET
    @Path("{fragilityId}")
    @Produces({MediaType.APPLICATION_JSON})
    public FragilitySet getFragilityById(@HeaderParam("X-Credential-Username") String username, @PathParam("fragilityId") String id) {
        Optional<FragilitySet> fragilitySet = this.fragilityDAO.getFragilitySetById(id);

        if (fragilitySet.isPresent()) {
            FragilitySet frag = fragilitySet.get();
            if (authorizer.canRead(username, frag.getPrivileges())) {
                return frag;
            }
            throw new ForbiddenException();
        } else {
            throw new NotFoundException();
        }
    }

    @GET
    @Path("/search")
    @Produces({MediaType.APPLICATION_JSON})
    public List<FragilitySet> findFragilities(@HeaderParam("X-Credential-Username") String username, @QueryParam("text") String text) {
        List<FragilitySet> sets = this.fragilityDAO.searchFragilities(text);

        if (sets == null || sets.size() == 0) {
            throw new NotFoundException();
        } else {
            return sets.stream()
                .filter(b -> authorizer.canRead(username, b.getPrivileges()))
                .collect(Collectors.toList());
        }
    }
}

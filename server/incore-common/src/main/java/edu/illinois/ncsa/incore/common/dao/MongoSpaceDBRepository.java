/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Omar Elabd (NCSA) - initial API and implementation
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *******************************************************************************/

package edu.illinois.ncsa.incore.common.dao;

import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.DiscriminatorFunction;
import dev.morphia.mapping.MapperOptions;
import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filters;
import edu.illinois.ncsa.incore.common.models.Space;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;


public class MongoSpaceDBRepository implements ISpaceRepository {
    private final String SPACE_FIELD_METADATA_NAME = "metadata.name";
    private final String databaseName;
    private final MongoClientURI mongoClientURI;
    private Datastore dataStore;

    private final Logger logger = Logger.getLogger(MongoSpaceDBRepository.class);

    public MongoSpaceDBRepository(MongoClientURI mongoClientURI) {
        this.mongoClientURI = mongoClientURI;
        this.databaseName = mongoClientURI.getDatabase();
    }

    @Override
    public void initialize() {
        this.initializeDataStore();
    }

    public List<Space> getAllSpaces() {
        return this.dataStore.find(Space.class).iterator().toList();
    }

    public Space getSpaceById(String id) {
        return this.dataStore.find(Space.class).filter(Filters.eq("_id", new ObjectId(id))).first();
    }

    public Space getSpaceByName(String name) {
        Query<Space> spaceQuery = this.dataStore.find(Space.class);
        Space foundSpace = spaceQuery.filter(Filters.eq(SPACE_FIELD_METADATA_NAME, name)).first();

        return foundSpace;
    }

    public Space getOrphanSpace() {
        Space orphans = getSpaceByName("orphans");
        if (orphans == null) {
            logger.error("orphans space not found in the Space DB. This can cause data inconsistency issues.");
        }
        return orphans;
    }

    //TODO: Rename to saveSpace since it updates an existing too
    public Space addSpace(Space space) {
        String id = (this.dataStore.save(space)).getId();
        return getSpaceById(id);
    }

    public Space addToOrphansSpace(String memberId) {
        Space orphans = getOrphanSpace();
        if (orphans != null) {
            orphans.addMember(memberId);
            return addSpace(orphans);
        }
        return null;
    }

    public Space deleteSpace(String id) {
        Query<Space> spaceQuery = this.dataStore.find(Space.class).filter(Filters.eq("_id", new ObjectId(id)));
        return spaceQuery.findAndDelete();
    }

    private void initializeDataStore() {
        Datastore morphiaStore = Morphia.createDatastore(MongoClients.create(mongoClientURI.getURI()), databaseName,
            MapperOptions
                .builder()
                .discriminator(DiscriminatorFunction.className())
                .discriminatorKey("className")
                .build()
        );
        morphiaStore.getMapper().map(Space.class);
        morphiaStore.ensureIndexes();
        this.dataStore = morphiaStore;
    }

    public List<String> getSpaceNamesOfMember(String memberId) {
        List<Space> allSpaces = getAllSpaces();
        List<String> spacesWithMember = new ArrayList<>();
        for (Space space : allSpaces) {
            if (space.hasMember(memberId)) {
                spacesWithMember.add(space.getName());
            }
        }

        return spacesWithMember;
    }

}

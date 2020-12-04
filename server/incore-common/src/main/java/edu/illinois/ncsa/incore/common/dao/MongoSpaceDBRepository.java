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

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import edu.illinois.ncsa.incore.common.models.Space;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.query.Query;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MongoSpaceDBRepository implements ISpaceRepository {
    private final String SPACE_FIELD_METADATA_NAME = "metadata.name";
    private String databaseName;
    private MongoClientURI mongoClientURI;
    private Datastore dataStore;

    private Logger logger = Logger.getLogger(MongoSpaceDBRepository.class);

    public MongoSpaceDBRepository(MongoClientURI mongoClientURI) {
        this.mongoClientURI = mongoClientURI;
        this.databaseName = mongoClientURI.getDatabase();
    }

    @Override
    public void initialize() {
        this.initializeDataStore();
    }

    public List<Space> getAllSpaces() {
        return this.dataStore.createQuery(Space.class).find().toList();
    }

    public Space getSpaceById(String id) {
        return this.dataStore.createQuery(Space.class)
            .field("_id").equal(new ObjectId(id)).find().tryNext();
    }

    public Space getSpaceByName(String name) {
        Query<Space> spaceQuery = this.dataStore.createQuery(Space.class);
        spaceQuery.field(SPACE_FIELD_METADATA_NAME).equal(name);
        Space foundSpace = spaceQuery.find().tryNext();

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
        String id = (this.dataStore.save(space)).getId().toString();
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
        Query<Space> spaceQuery = this.dataStore.createQuery(Space.class);
        spaceQuery.field("_id").equal(new ObjectId(id));
        return this.dataStore.findAndDelete(spaceQuery);
    }

    private void initializeDataStore() {
        MongoClient client = new MongoClient(mongoClientURI);
        Set<Class> classesToMap = new HashSet<>();
        classesToMap.add(Space.class);
        Morphia morphia = new Morphia(classesToMap);
        Datastore morphiaStore = morphia.createDatastore(client, databaseName);
        morphiaStore.ensureIndexes();
        this.dataStore = morphiaStore;
    }

}

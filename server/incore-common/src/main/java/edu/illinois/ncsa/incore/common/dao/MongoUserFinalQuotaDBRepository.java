/*******************************************************************************
 * Copyright (c) 2021University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
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
import edu.illinois.ncsa.incore.common.models.UserFinalQuota;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import java.util.List;


public class MongoUserFinalQuotaDBRepository implements IUserFinalQuotaRepository {
    private final String QUOTA_FIELD_USERNAME = "username";
    private final String databaseName;
    private final MongoClientURI mongoClientURI;
    private Datastore dataStore;

    private final Logger logger = Logger.getLogger(MongoUserFinalQuotaDBRepository.class);

    public MongoUserFinalQuotaDBRepository(MongoClientURI mongoClientURI) {
        this.mongoClientURI = mongoClientURI;
        this.databaseName = mongoClientURI.getDatabase();
    }

    @Override
    public void initialize() {
        this.initializeDataStore();
    }

    public List<UserFinalQuota> getAllQuotas() {
        return this.dataStore.find(UserFinalQuota.class).iterator().toList();
    }

    public UserFinalQuota getQuotaById(String id) {
        return this.dataStore.find(UserFinalQuota.class).filter(Filters.eq("_id", new ObjectId(id))).first();
    }

    public UserFinalQuota getQuotaByUsername(String username) {
        Query<UserFinalQuota> quotaQuery = this.dataStore.find(UserFinalQuota.class);
        UserFinalQuota foundUserQuota = null;
        try {
            foundUserQuota = quotaQuery.filter(Filters.eq(QUOTA_FIELD_USERNAME, username)).first();
        } catch (IllegalArgumentException e) {
            logger.error("the user doesn't have UserFinalQuota");
        } catch (dev.morphia.mapping.MappingException e) {
            logger.error("the user doesn't have UserFinalQuota");
        }
        return foundUserQuota;
    }

    private void initializeDataStore() {
        Datastore morphiaStore = Morphia.createDatastore(MongoClients.create(mongoClientURI.getURI()), databaseName,
            MapperOptions
                .builder()
                .discriminator(DiscriminatorFunction.className())
                .discriminatorKey("className")
                .build()
        );
        morphiaStore.getMapper().map(UserFinalQuota.class);
        morphiaStore.ensureIndexes();
        this.dataStore = morphiaStore;
    }

}

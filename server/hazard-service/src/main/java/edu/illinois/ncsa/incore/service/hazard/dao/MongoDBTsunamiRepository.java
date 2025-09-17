/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.dao;

import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.DiscriminatorFunction;
import dev.morphia.mapping.MapperOptions;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filters;
import edu.illinois.ncsa.incore.service.hazard.models.tsunami.Tsunami;
import edu.illinois.ncsa.incore.service.hazard.models.tsunami.TsunamiDataset;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MongoDBTsunamiRepository extends MongoDAO implements ITsunamiRepository {

    public MongoDBTsunamiRepository(MongoClientURI mongoClientURI) {
        super(mongoClientURI);
    }

    @Override
    public void initialize() {
        this.initializeDataStore();
    }

    private void initializeDataStore() {
        Datastore morphiaStore = Morphia.createDatastore(MongoClients.create(mongoClientURI.getURI()),
            mongoClientURI.getDatabase(),
            MapperOptions
                .builder()
                .discriminator(DiscriminatorFunction.className())
                .discriminatorKey("className")
                .build()
        );
        morphiaStore.getMapper().map(TsunamiDataset.class);
        morphiaStore.ensureIndexes();
        this.dataStore = morphiaStore;
    }

    @Override
    public Tsunami getTsunamiById(String id) {
        if (!ObjectId.isValid(id)) {
            return null;
        }

        Tsunami tsunami = this.dataStore.find(TsunamiDataset.class)
            .filter(Filters.eq("_id", new ObjectId(id))).first();
        // TODO this will need to be updated if there are model based tsunamis

        return tsunami;
    }

    @Override
    public Tsunami addTsunami(Tsunami tsunami) {
        String id = this.dataStore.save(tsunami).getId();
        return getTsunamiById(id);
    }

    @Override
    public Tsunami deleteTsunamiById(String id) {
        Tsunami tsunami = this.dataStore.find(TsunamiDataset.class)
            .filter(Filters.eq("_id", new ObjectId(id))).first();
        if (tsunami != null) {
            Query<TsunamiDataset> query = this.dataStore.find(TsunamiDataset.class);
            return query.filter(Filters.eq("_id", new ObjectId(id))).findAndDelete();
        }
        return null;
    }

    @Override
    public List<Tsunami> getTsunamis() {
        List<Tsunami> tsunamis = new LinkedList<>();
        List<TsunamiDataset> tsunamiDatasets = this.dataStore.find(TsunamiDataset.class).iterator().toList();
        tsunamis.addAll(tsunamiDatasets);
        // TODO this will need to be updated if there are model based tsunamis

        return tsunamis;
    }

    @Override
    public List<Tsunami> searchTsunamis(String text) {
        Query<TsunamiDataset> query = this.dataStore.find(TsunamiDataset.class).filter(
            Filters.or(
                Filters.regex("name").pattern(text).caseInsensitive(),
                Filters.regex("description").pattern(text).caseInsensitive()
            ));
        List<TsunamiDataset> tsunamiDatasets = query.iterator().toList();

        List<Tsunami> tsunamis = new ArrayList<>();
        tsunamis.addAll(tsunamiDatasets);

        return tsunamis;
    }

    @Override
    public List<Tsunami> getTsunamisByCreator(String creator) {
        Query<TsunamiDataset> query = this.dataStore.find(TsunamiDataset.class).filter(
            Filters.regex("creator").pattern(creator).caseInsensitive()
        );
        List<TsunamiDataset> tsunamiDatasets = query.iterator().toList();

        List<Tsunami> tsunamis = new ArrayList<>();
        tsunamis.addAll(tsunamiDatasets);

        return tsunamis;
    }

    @Override
    public int getTsunamisCountByCreator(String creator) {
        int count = (int) (this.dataStore.find(TsunamiDataset.class).filter(Filters.regex("creator").
            pattern(creator).caseInsensitive()).count());

        return count;
    }
}

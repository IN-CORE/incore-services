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
import edu.illinois.ncsa.incore.service.hazard.models.tsunami.Tsunami;
import edu.illinois.ncsa.incore.service.hazard.models.tsunami.TsunamiDataset;
import org.bson.types.ObjectId;
import dev.morphia.query.Query;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MongoDBTsunamiRepository extends MongoDAO implements ITsunamiRepository {

    public MongoDBTsunamiRepository(MongoClientURI mongoClientURI) {
        super(mongoClientURI);
    }

    @Override
    public void initialize() {
        super.initializeDataStore(Tsunami.class);
    }

    @Override
    public Tsunami getTsunamiById(String id) {
        if (!ObjectId.isValid(id)) {
            return null;
        }

        Tsunami tsunami = this.dataStore.find(TsunamiDataset.class)
            .field("_id").equal(new ObjectId(id)).tryNext();
        // TODO this will need to be updated if there are model based tsunamis

        return tsunami;
    }

    @Override
    public Tsunami addTsunami(Tsunami tsunami) {
        String id = this.dataStore.save(tsunami).getId().toString();
        return getTsunamiById(id);
    }

    @Override
    public Tsunami deleteTsunamiById(String id) {
        Tsunami tsunami = this.dataStore.find(TsunamiDataset.class)
            .field("_id").equal(new ObjectId(id)).tryNext();
        if (tsunami != null) {
            Query<TsunamiDataset> query = this.dataStore.find(TsunamiDataset.class);
            query.field("_id").equal(new ObjectId(id));
            return this.dataStore.findAndDelete(query);
        }
        return null;
    }

    @Override
    public List<Tsunami> getTsunamis() {
        List<Tsunami> tsunamis = new LinkedList<>();
        List<TsunamiDataset> tsunamiDatasets = this.dataStore.find(TsunamiDataset.class).toList();
        tsunamis.addAll(tsunamiDatasets);
        // TODO this will need to be updated if there are model based tsunamis

        return tsunamis;
    }

    @Override
    public List<Tsunami> searchTsunamis(String text) {
        Query<TsunamiDataset> query = this.dataStore.find(TsunamiDataset.class);

        query.or(query.criteria("name").containsIgnoreCase(text),
            query.criteria("description").containsIgnoreCase(text));

        List<TsunamiDataset> tsunamiDatasets = query.toList();

        List<Tsunami> tsunamis = new ArrayList<>();
        tsunamis.addAll(tsunamiDatasets);

        return tsunamis;
    }
}

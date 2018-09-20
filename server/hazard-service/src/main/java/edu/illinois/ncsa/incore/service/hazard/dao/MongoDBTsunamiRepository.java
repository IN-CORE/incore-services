/*******************************************************************************
 * Copyright (c) 2018 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.dao;

import com.mongodb.MongoClientURI;
import edu.illinois.ncsa.incore.service.hazard.models.tsunami.Tsunami;
import edu.illinois.ncsa.incore.service.hazard.models.tsunami.TsunamiDataset;
import org.bson.types.ObjectId;

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
        Tsunami tsunami = this.dataStore.get(TsunamiDataset.class, new ObjectId(id));
        // TODO this will need to be updated if there are model based tsunamis

        return tsunami;
    }

    @Override
    public Tsunami addTsunami(Tsunami tsunami) {
        String id = this.dataStore.save(tsunami).getId().toString();
        return getTsunamiById(id);
    }

    @Override
    public List<Tsunami> getTsunamis() {
        List<Tsunami> tsunamis = new LinkedList<>();
        List<TsunamiDataset> tsunamiDatasets = this.dataStore.createQuery(TsunamiDataset.class).asList();
        tsunamis.addAll(tsunamiDatasets);
        // TODO this will need to be updated if there are model based tsunamis

        return tsunamis;
    }
}

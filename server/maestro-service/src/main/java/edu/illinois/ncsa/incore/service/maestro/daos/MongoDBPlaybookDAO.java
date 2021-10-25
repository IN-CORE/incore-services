/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Indira Gutierrez (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.maestro.daos;

import com.mongodb.MongoClientURI;
import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filters;
import edu.illinois.ncsa.incore.service.maestro.models.Playbook;
import org.bson.types.ObjectId;

import java.util.List;


public class MongoDBPlaybookDAO extends MongoDAO implements IPlaybookDAO {
    public MongoDBPlaybookDAO(MongoClientURI mongoClientURI) {
        super(mongoClientURI);
    }

    @Override
    public void initialize() {
        super.initializeDataStore(Playbook.class);
    }

    @Override
    public List<Playbook> getAllPlaybooks() {
        return this.dataStore.find(Playbook.class).iterator().toList();
    }

    @Override
    public Playbook getPlaybookById(String id) {
        if (!ObjectId.isValid(id)) {
            return null;
        }

        Playbook playbook = this.dataStore.find(Playbook.class)
            .filter(Filters.eq("_id", new ObjectId(id))).first();

        return playbook;
    }

    @Override
    public Playbook addPlaybook(Playbook playbook) {
        if (playbook == null) {
            throw new IllegalArgumentException();
        } else {
            return this.dataStore.save(playbook);
        }
    }

    @Override
    public Playbook removePlaybook(String playbookId) {
        if (!ObjectId.isValid(playbookId)) {
            return null;
        }

        Query<Playbook> query = this.dataStore.find(Playbook.class)
            .filter(Filters.eq("_id", new ObjectId(playbookId)));
        return query.findAndDelete();
    }

}

/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Omar Elabd
 */

package edu.illinois.ncsa.incore.service.dfr3.daos;

import com.mongodb.MongoClientURI;
import edu.illinois.ncsa.incore.service.dfr3.models.MappingSet;
import edu.illinois.ncsa.incore.service.dfr3.models.FragilityMappingSet;
import edu.illinois.ncsa.incore.service.dfr3.models.RestorationMappingSet;
import org.bson.types.ObjectId;
import org.mongodb.morphia.query.Query;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MongoDBMappingDAO extends MongoDAO implements IMappingDAO {

    public MongoDBMappingDAO(MongoClientURI mongoClientURI) {
        super(mongoClientURI);
    }

    @Override
    public void initialize() { super.initializeDataStore(MappingSet.class);
    }

    @Override
    public List<MappingSet> getMappingSets(String type) {
        List<MappingSet> mappingSets = new LinkedList<>();
        if (type.equals("fragility")){
            List<FragilityMappingSet> fragilityMappingSets = this.dataStore.createQuery(FragilityMappingSet.class).asList();
            mappingSets.addAll(fragilityMappingSets);
        }
        else if (type.equals("restoration")){
            List<RestorationMappingSet> restorationMappingSets = this.dataStore.createQuery(RestorationMappingSet.class).asList();
            mappingSets.addAll(restorationMappingSets);
        }
        return mappingSets;
    }

    @Override
    public Optional<MappingSet> getMappingSetById(String id, String type) {
        if (!ObjectId.isValid(id)) {
            return Optional.empty();
        }

        MappingSet mappingSet = null;
        if (type.equals("fragility")){
            mappingSet = this.dataStore.get(FragilityMappingSet.class, new ObjectId(id));

        }
        else if (type.equals("restoration")){
            mappingSet = this.dataStore.get(RestorationMappingSet.class, new ObjectId(id));
        }

        if (mappingSet == null) {
            return Optional.empty();
        } else {
            return Optional.of(mappingSet);
        }
    }

    @Override
    public String saveMappingSet(MappingSet mappingSet) {
        if (mappingSet == null) {
            throw new IllegalArgumentException();
        } else {
            String id = this.dataStore.save(mappingSet).getId().toString();
            return id;
        }
    }

    @Override
    public List<MappingSet> queryMappingSets(Map<String, String> queryMap, String type) {

        List<MappingSet> mappingSets = new LinkedList<MappingSet>();

        if (type.equals("fragility")) {
            Query<FragilityMappingSet> query = this.dataStore.createQuery(FragilityMappingSet.class);
            for (Map.Entry<String, String> queryEntry : queryMap.entrySet()) {
                query.filter(queryEntry.getKey(), queryEntry.getValue());
            }
            List<FragilityMappingSet> fragilityMappingSets = query.asList();
            mappingSets.addAll(fragilityMappingSets);
        }
        else if (type.equals("restoration")) {
            Query<RestorationMappingSet> query = this.dataStore.createQuery(RestorationMappingSet.class);
            for (Map.Entry<String, String> queryEntry : queryMap.entrySet()) {
                query.filter(queryEntry.getKey(), queryEntry.getValue());
            }
            List<RestorationMappingSet> restorationMappingSets = query.asList();
            mappingSets.addAll(restorationMappingSets);
        }

        return mappingSets;
    }
}

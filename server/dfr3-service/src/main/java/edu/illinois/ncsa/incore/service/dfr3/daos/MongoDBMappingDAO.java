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
import edu.illinois.ncsa.incore.service.dfr3.models.Mapping;
import edu.illinois.ncsa.incore.service.dfr3.models.MappingSet;
import org.bson.types.ObjectId;
import dev.morphia.query.Query;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MongoDBMappingDAO extends MongoDAO implements IMappingDAO {

    public MongoDBMappingDAO(MongoClientURI mongoClientURI) {
        super(mongoClientURI);
    }

    @Override
    public void initialize() {
        super.initializeDataStore(MappingSet.class);
    }

    @Override
    public List<MappingSet> getMappingSets() {
        return this.dataStore.createQuery(MappingSet.class).find().toList();
    }

    @Override
    public Optional<MappingSet> getMappingSetById(String id) {
        if (!ObjectId.isValid(id)) {
            return Optional.empty();
        }

        MappingSet mappingSet = null;
        mappingSet = this.dataStore.createQuery(MappingSet.class).field("_id").equal(new ObjectId(id)).find().tryNext();

        if (mappingSet == null) {
            return Optional.empty();
        } else {
            return Optional.of(mappingSet);
        }
    }

    @Override
    public MappingSet deleteMappingSetById(String id) {
        MappingSet mappingSet = this.dataStore.createQuery(MappingSet.class)
            .field("_id").equal(new ObjectId(id)).find().tryNext();

        if (mappingSet == null) {
            return null;
        } else {
            Query<MappingSet> query = this.dataStore.createQuery(MappingSet.class);
            query.field("_id").equal(new ObjectId(id));
            return this.dataStore.findAndDelete(query);
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
    public List<MappingSet> queryMappingSets(Map<String, String> queryMap) {

        Query<MappingSet> query = this.dataStore.createQuery(MappingSet.class);
        for (Map.Entry<String, String> queryEntry : queryMap.entrySet()) {
            query.filter(queryEntry.getKey(), queryEntry.getValue());
        }
        return query.find().toList();
    }

    @Override
    public List<MappingSet> searchMappings(String text, String mappingType) {
        Query<MappingSet> query = this.dataStore.createQuery(MappingSet.class);

        if(mappingType != null && !mappingType.trim().equals("")){
                    query.criteria("mappingType").equalIgnoreCase(mappingType).or(
                        query.criteria("name").containsIgnoreCase(text),
                        query.criteria("hazardType").containsIgnoreCase(text),
                        query.criteria("inventoryType").containsIgnoreCase(text));
        } else {
            query.or(query.criteria("name").containsIgnoreCase(text),
                query.criteria("hazardType").containsIgnoreCase(text),
                query.criteria("inventoryType").containsIgnoreCase(text));
        }

        List<MappingSet> sets = query.find().toList();

        return sets;
    }

    @Override
    public Boolean isCurvePresentInMappings(String id) {
        if (!ObjectId.isValid(id)) {
            return false;
        }

        List<MappingSet> mappingSets = this.dataStore.createQuery(MappingSet.class).find().toList();
        for(MappingSet map: mappingSets){
            List<Mapping> mappings =  map.getMappings();
            for(Mapping mapping: mappings){
                Map<String, String> entry = mapping.getEntry();
                if(entry.containsValue(id)){
                    return true;
                }
            }
        }
        return false;
    }
}

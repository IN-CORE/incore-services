/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 */

package edu.illinois.ncsa.incore.service.dfr3.daos;

import com.mongodb.MongoClientURI;
import dev.morphia.query.experimental.filters.Filters;
import dev.morphia.query.experimental.filters.RegexFilter;
import edu.illinois.ncsa.incore.service.dfr3.models.ConditionalStandardFragilityCurve;
import edu.illinois.ncsa.incore.service.dfr3.models.FragilityCurve;
import edu.illinois.ncsa.incore.service.dfr3.models.FragilitySet;
import org.bson.types.ObjectId;
import dev.morphia.query.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MongoDBFragilityDAO extends MongoDAO implements IFragilityDAO {

    public MongoDBFragilityDAO(MongoClientURI mongoClientURI) {
        super(mongoClientURI);
    }

    @Override
    public void initialize() {
        super.initializeDataStore(FragilitySet.class);
    }

    @Override
    public List<FragilitySet> getFragilities() {
        return this.dataStore.find(FragilitySet.class).iterator().toList();
    }

    @Override
    public String saveFragility(FragilitySet fragilitySet) {
        if (fragilitySet == null) {
            throw new IllegalArgumentException();
        } else {
            // the save method mutates the fragilitySet object with an document id
            // check if conditional fragility curves alpha and beta has the same shape before saving
            List<FragilityCurve> fragilityCurves = fragilitySet.getFragilityCurves();
            fragilityCurves.forEach(curve -> {
                if (curve instanceof ConditionalStandardFragilityCurve) {
                    double[] alpha = ((ConditionalStandardFragilityCurve) curve).getAlpha();
                    double[] beta = ((ConditionalStandardFragilityCurve) curve).getBeta();
                    if (alpha.length != beta.length) {
                        throw new IllegalArgumentException("Alpha and Beta must have the same length/shape.");
                    }
                }
            });

            String id = this.dataStore.save(fragilitySet).getId().toString();

            return id;
        }

    }

    @Override
    public Optional<FragilitySet> getFragilitySetById(String id) {
        if (!ObjectId.isValid(id)) {
            return Optional.empty();
        }

        FragilitySet fragilitySet = this.dataStore.find(FragilitySet.class)
            .filter(Filters.eq("_id", new ObjectId(id))).first();

        if (fragilitySet == null) {
            return Optional.empty();
        } else {
            return Optional.of(fragilitySet);
        }
    }

    @Override
    public FragilitySet deleteFragilitySetById(String id) {
        FragilitySet fragilitySet = this.dataStore.find(FragilitySet.class)
            .filter(Filters.eq("_id", new ObjectId(id))).first();

        if (fragilitySet == null) {
            return null;
        } else {
            Query<FragilitySet> query = this.dataStore.find(FragilitySet.class)
                .filter(Filters.eq("_id", new ObjectId(id)));
            return query.findAndDelete();
        }
    }

    @Override
    public List<FragilitySet> searchFragilities(String text) {
        Query<FragilitySet> query = this.dataStore.find(FragilitySet.class).filter(
            Filters.or(
                Filters.regex("demandType").pattern(text).caseInsensitive(),
                Filters.regex("legacyId").pattern(text).caseInsensitive(),
                Filters.regex("hazardType").pattern(text).caseInsensitive(),
                Filters.regex("inventoryType").pattern(text).caseInsensitive(),
                Filters.regex("description").pattern(text).caseInsensitive(),
                Filters.regex("authors").pattern(text).caseInsensitive()));
        List<FragilitySet> sets = query.iterator().toList();

        return sets;
    }

    @Override
    public List<FragilitySet> queryFragilities(String attributeType, String attributeValue) {
        List<FragilitySet> sets = this.dataStore.find(FragilitySet.class)
            .filter(Filters.eq(attributeType, attributeValue)).iterator().toList();

        return sets;
    }

    @Override
    public List<FragilitySet> queryFragilities(Map<String, String> queryMap) {
        Query<FragilitySet> query = this.dataStore.find(FragilitySet.class);

        for (Map.Entry<String, String> queryEntry : queryMap.entrySet()) {
            query.filter(Filters.eq(queryEntry.getKey(), queryEntry.getValue()));
        }

        List<FragilitySet> sets = query.iterator().toList();

        return sets;
    }

    @Override
    public List<FragilitySet> queryFragilityAuthor(String author) {
        // TODO need to make sure this works
        List<FragilitySet> sets = this.dataStore.find(FragilitySet.class).filter(Filters.regex("authors")
            .pattern(author).caseInsensitive()).iterator().toList();

        return sets;
    }
}

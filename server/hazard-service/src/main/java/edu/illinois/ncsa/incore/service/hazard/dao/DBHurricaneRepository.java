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

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.Hurricane;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.ScenarioTornado;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class DBHurricaneRepository implements IHurricaneRepository {
    private String hostUri;
    private String databaseName;
    private int port;
    private MongoClientURI mongoClientURI;

    private JSONObject jsonParams;

    public DBHurricaneRepository() {
        this.port = 27017;
        this.hostUri = "localhost";
        this.databaseName = "hazarddb";
    }

    public DBHurricaneRepository(String hostUri, String databaseName, int port) {
        this.databaseName = databaseName;
        this.hostUri = hostUri;
        this.port = port;
    }



//    @Override
//    public void initialize() {
//        this.initializeDataStore();
//    }

    @Override
    public Hurricane getHurricaneByModel(String model) {
        JSONParser parser = new JSONParser();
        Hurricane hurricane = new Hurricane();
        JSONObject jsonParams = new JSONObject();
        hurricane.setHurricaneModel(model);

        try {
            String path = "/Users/vnarah2/incore-data/Hurricane wind field/mat-to-json/Models/";
            String fileName = model+".json";
            Object obj = parser.parse(new FileReader(path+fileName));

            jsonParams =  (JSONObject) obj;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        hurricane.setHurricaneParameters(jsonParams);

        return hurricane;

        //return this.dataStore.get(ScenarioTornado.class, new ObjectId(id));
    }


}

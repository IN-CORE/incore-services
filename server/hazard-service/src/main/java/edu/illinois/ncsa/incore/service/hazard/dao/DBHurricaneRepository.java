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

import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.service.hazard.models.hurricaneWindfields.HistoricHurricane;

import java.net.URL;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class DBHurricaneRepository {

    public static final Logger log = Logger.getLogger(DBHurricaneRepository.class);

    //@Override
    public HistoricHurricane getHurricaneByModel(String model) {
        JSONParser parser = new JSONParser();
        HistoricHurricane hurricane = new HistoricHurricane();
        JSONObject jsonParams = new JSONObject();
        hurricane.setHurricaneModel(model);

        try {

            String fileName = model+".json";
            URL modelURL = this.getClass().getClassLoader().getResource("/hazard/hurricane/models/" + fileName);
            Object obj = parser.parse(new FileReader(modelURL.getFile()));

            jsonParams =  (JSONObject) obj;

        } catch (FileNotFoundException e) {
            log.debug("File Not Found.", e)
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Model not found in the database");
        } catch (IOException e) {
            log.debug("IO Exception.", e)
        } catch (ParseException e) {
            log.debug("Parse Exception.", e)
        }

        hurricane.setHurricaneParameters(jsonParams);

        return hurricane;
    }


}

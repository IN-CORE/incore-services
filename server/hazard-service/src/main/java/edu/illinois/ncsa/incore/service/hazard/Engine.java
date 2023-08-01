/*******************************************************************************
 * Copyright (c) 2020 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard;

import edu.illinois.ncsa.incore.common.HazardConstants;
import edu.illinois.ncsa.incore.service.hazard.dao.IEarthquakeRepository;
import edu.illinois.ncsa.incore.service.hazard.models.eq.EarthquakeModel;
import edu.illinois.ncsa.incore.service.hazard.utils.ServiceUtil;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Engine {
    private static final Logger log = Logger.getLogger(Engine.class);

    private final Map<String, Object> serviceRepositories = new HashMap<>();
    // In the future, we should make multiple worker threads
    private final WorkerThread engineThread = new WorkerThread();
    // TODO we should persist the queue somewhere
    private final List<Job> queue = new ArrayList<>();

    public Engine() {
        engineThread.setName("EngineThread");
        engineThread.start();
    }

    public void addServiceRepository(String service, Object repository) {
        serviceRepositories.put(service, repository);
    }

    public void interrupt() {
        engineThread.interrupt();
    }

    public void addJob(Job job) {
        synchronized (queue) {
            queue.add(job);
        }
    }

    class WorkerThread extends Thread {
        public void run() {
            List<Job> dequeue = new ArrayList<Job>();
            while (true) {
                // little sleep
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    log.debug("Thread interrupted", e);
                }

                for (Job job : queue) {
                    if (job.getExecutionId() == null) {
                        // submit to datawolf to create dataset
                        // TODO - consider associating workflow executions by user, currently we associate with a generic incore-dw user
                        // TODO - create user if they don't exist
                        String username = System.getenv("DATAWOLF_USER");
                        String executionId = ServiceUtil.submitCreateEarthquakeJob("a5e77b9c-5d8a-4052-b1bb-9b260a6c5102", username,
                            "create eq", "Create earthquake dataset", job.getEqJson());
                        job.setExecutionId(executionId);
                    } else {
                        // check on job status
                        Map<String, String> jobStatus = ServiceUtil.getWorkflowJobStatus(job.getExecutionId());
                        Iterator<String> iterator = jobStatus.keySet().iterator();

                        // Assumption that all workflows are single step
                        String key = iterator.next();
                        String status = jobStatus.get(key);
                        job.setState(Job.State.valueOf(status));
                        log.debug("job id = " + job.getExecutionId() + ", state is " + job.getState());
                        if (status.equalsIgnoreCase("FINISHED")) {
                            dequeue.add(job);
                            storeResults(job);
                        } else if (status.equalsIgnoreCase("ABORTED") || status.equalsIgnoreCase("FAILED")) {
                            // remove from queue
                            dequeue.add(job);
                            // notify failure
                            log.debug("Create earthquake job failed for " + job.getObjectId());
                        }
                    }
                }

                for (Job job : dequeue) {
                    queue.remove(job);
                }
                dequeue.clear();
            }
        }
    }

    private void storeResults(Job job) {
        if (job.getService() == "earthquake") {
            storeEarthquakeResults(job);
        } else {
            log.warn("Storing jobs for " + job.getService() + " is not yet implemented");
        }
    }

    private void storeEarthquakeResults(Job job) {
        String workflowOutputKey = "dec2447e-19b0-4b31-fe5e-35a42cf13809";
        List<String> datasetList = new ArrayList<String>();
        datasetList.add(workflowOutputKey);
        Map<String, String> datasetOutputs = ServiceUtil.getWorkflowOutputs(job.getExecutionId(), datasetList);
        List<File> datasetFiles = ServiceUtil.getWorkflowDatasetFiles(datasetOutputs.get(workflowOutputKey));

        File hazardFile = datasetFiles.get(0);

        IEarthquakeRepository repository = (IEarthquakeRepository) serviceRepositories.get("earthquake");
        String earthquakeId = job.getObjectId();

        EarthquakeModel earthquake = (EarthquakeModel) repository.getEarthquakeById(earthquakeId);
        String demandType = earthquake.getRasterDataset().getDemandType();
        String username = earthquake.getCreator();
        String description = "Earthquake visualization";
        String userGroups = "{\"groups\": [\"incore_user\"]}";
        try {
            String datasetId = ServiceUtil.createRasterDataset(hazardFile, demandType + " hazard", username, userGroups,
                description, HazardConstants.DETERMINISTIC_EARTHQUAKE_HAZARD_SCHEMA);
            earthquake.getRasterDataset().setDatasetId(datasetId);

            repository.addEarthquake(earthquake);
            log.debug("eq id is = " + earthquakeId);
        } catch (IOException e) {
            log.error("Could not store earthquake dataset", e);
        }

    }

}

/*******************************************************************************
 * Copyright (c) 2024 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *******************************************************************************/

package edu.illinois.ncsa.incore.service.project.dao;

import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.DiscriminatorFunction;
import dev.morphia.mapping.MapperOptions;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filters;
import dev.morphia.query.updates.UpdateOperators;
import edu.illinois.ncsa.incore.service.project.models.Project;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;


public class MongoProjectDBRepository implements IProjectRepository {
    private final String PROJECT_DATABASE_NAME = "projectdb";
    private final String PROJECT_COLLECTION_NAME = "Project";
    private final String PROJECT_FIELD_NAME = "name";
    private final String PROJECT_FIELD_DESCRIPTION = "description";
    private final String PROJECT_FIELD_CREATOR = "creator";
    private final String PROJECT_FIELD_OWNER = "owner";
    private final String PROJECT_FIELD_REGION = "region";
    private String hostUri;
    private final String databaseName;
    private int port;
    private MongoClientURI mongoClientURI;
    private Datastore dataStore;

    public MongoProjectDBRepository() {
        this.port = 27017;
        this.hostUri = "localhost";
        this.databaseName = PROJECT_DATABASE_NAME;
    }

    public MongoProjectDBRepository(String hostUri, String databaseName, int port) {
        this.databaseName = databaseName;
        this.hostUri = hostUri;
        this.port = port;
    }

    public MongoProjectDBRepository(MongoClientURI mongoClientURI) {
        this.mongoClientURI = mongoClientURI;
        this.databaseName = mongoClientURI.getDatabase();
    }

    @Override
    public void initialize() {
        this.initializeDataStore();
    }

    @Override
    public List<Project> getAllProjects() {
        return this.dataStore.find(Project.class).iterator().toList();
    }

    @Override
    public List<Project> queryAllProjects(Map<String, String> queryMap) {
        Query<Project> query = this.dataStore.find(Project.class);

        for (Map.Entry<String, String> queryEntry : queryMap.entrySet()) {
            query.filter(Filters.eq(queryEntry.getKey(), queryEntry.getValue()));
        }

        return query.iterator().toList();
    }

    @Override
    public Project getProjectById(String id) {
        if (!ObjectId.isValid(id)) {
            return null;
        }
        return this.dataStore.find(Project.class).filter(Filters.eq("_id", new ObjectId(id))).first();
    }

    @Override
    public List<Project> getProjectsByName(String name) {
        Query<Project> projectQuery = this.dataStore.find(Project.class)
                .filter(Filters.regex(PROJECT_FIELD_NAME).pattern(name).caseInsensitive());
        return projectQuery.iterator().toList();
    }

    @Override
    public List<Project> getProjectsByCreator(String creator) {
        Query<Project> projectQuery = this.dataStore.find(Project.class).filter(Filters.and(
                Filters.regex(PROJECT_FIELD_CREATOR).pattern(creator).caseInsensitive()
            ));
        return projectQuery.iterator().toList();
    }

    @Override
    public List<Project> getProjectsByOwner(String owner) {
        Query<Project> projectQuery = this.dataStore.find(Project.class).filter(Filters.and(
            Filters.regex(PROJECT_FIELD_OWNER).pattern(owner).caseInsensitive()
        ));
        return projectQuery.iterator().toList();
    }

    @Override
    public List<Project> getProjectsByRegion(String region) {
        Query<Project> projectQuery = this.dataStore.find(Project.class).filter(Filters.and(
            Filters.regex(PROJECT_FIELD_REGION).pattern(region).caseInsensitive()
            ));
        return projectQuery.iterator().toList();
    }

    @Override
    public List<Project> searchProjects(String text) {
        Query<Project> query = this.dataStore.find(Project.class).filter(
            Filters.or(
                Filters.regex(PROJECT_FIELD_NAME).pattern(text).caseInsensitive(),
                Filters.regex(PROJECT_FIELD_DESCRIPTION).pattern(text).caseInsensitive(),
                Filters.regex(PROJECT_FIELD_CREATOR).pattern(text).caseInsensitive(),
                Filters.regex(PROJECT_FIELD_OWNER).pattern(text).caseInsensitive(),
                Filters.regex(PROJECT_FIELD_REGION).pattern(text).caseInsensitive()
            ));
        return query.iterator().toList();
    }

    @Override
    public Project updateProject(String projectId, Project newProject) {
        // Create a query to find the project by ID
        Query<Project> query = this.dataStore.find(Project.class)
            .filter(Filters.eq("_id", new ObjectId(projectId)));

        // Retrieve the existing project
        Project existingProject = query.first();
        if (existingProject == null) {
            throw new IllegalArgumentException("Project with ID " + projectId + " does not exist.");
        }

        // Perform the update operation using chained UpdateOperators
        query.update(
            newProject.getName() != null && !newProject.getName().isEmpty() ? UpdateOperators.set("name", newProject.getName()) : UpdateOperators.set("name", existingProject.getName()),
            newProject.getDescription() != null ? UpdateOperators.set("description", newProject.getDescription()) : UpdateOperators.set("description", existingProject.getDescription()),
            newProject.getDate() != null ? UpdateOperators.set("date", newProject.getDate()) : UpdateOperators.set("date", existingProject.getDate()),
            newProject.getCreator() != null ? UpdateOperators.set("creator", newProject.getCreator()) : UpdateOperators.set("creator", existingProject.getCreator()),
            newProject.getOwner() != null ? UpdateOperators.set("owner", newProject.getOwner()) : UpdateOperators.set("owner", existingProject.getOwner()),
            newProject.getRegion() != null ? UpdateOperators.set("region", newProject.getRegion()) : UpdateOperators.set("region", existingProject.getRegion()),
            newProject.getHazards() != null ? UpdateOperators.set("hazards", newProject.getHazards()) : UpdateOperators.set("hazards", existingProject.getHazards()),
            newProject.getDfr3Mappings() != null ? UpdateOperators.set("dfr3Mappings", newProject.getDfr3Mappings()) : UpdateOperators.set("dfr3Mappings", existingProject.getDfr3Mappings()),
            newProject.getDatasets() != null ? UpdateOperators.set("datasets", newProject.getDatasets()) : UpdateOperators.set("datasets", existingProject.getDatasets()),
            newProject.getWorkflows() != null ? UpdateOperators.set("workflows", newProject.getWorkflows()) : UpdateOperators.set("workflows", existingProject.getWorkflows()),
            newProject.getVisualizations() != null ? UpdateOperators.set("visualizations", newProject.getVisualizations()) : UpdateOperators.set("visualizations", existingProject.getVisualizations())
        ).execute();

        // Retrieve and return the updated project
        return this.dataStore.find(Project.class)
            .filter(Filters.eq("_id", new ObjectId(projectId)))
            .first();
    }

    public Project addProject(Project project) {
        String id = this.dataStore.save(project).getId();
        return getProjectById(id);
    }

    public Project deleteProject(String id) {
        Query<Project> query = this.dataStore.find(Project.class).filter(Filters.eq("_id", new ObjectId(id)));
        return query.findAndDelete();
    }

    @Override
    public Datastore getDataStore() {
        return this.dataStore;
    }

    private void initializeDataStore() {
        // You can call MongoClients.create() without any parameters to connect to a MongoDB instance running on
        // localhost on port 27017
        Datastore morphiaStore = Morphia.createDatastore(MongoClients.create(mongoClientURI.getURI()), databaseName,
            MapperOptions
                .builder()
                .discriminator(DiscriminatorFunction.className())
                .discriminatorKey("className")
                .build()
        );
        morphiaStore.getMapper().map(Project.class);
        morphiaStore.ensureIndexes();
        this.dataStore = morphiaStore;
    }
}

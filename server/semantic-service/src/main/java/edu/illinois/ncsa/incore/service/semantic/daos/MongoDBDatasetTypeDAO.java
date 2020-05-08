package edu.illinois.ncsa.incore.service.semantic.daos;
import com.mongodb.MongoClientURI;

import java.util.*;

import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.*;


public class MongoDBDatasetTypeDAO extends MongoDAO implements IDatasetTypeDAO {

    private List<Document> datasetTypeList;

    public MongoDBDatasetTypeDAO(MongoClientURI mongoClientURI) {
        super(mongoClientURI);
    }

    @Override
    public void initialize() {
        super.initializeDataStore();
        this.loadDatasetTypes();
    }

    @Override
    public List<Document> getDatasetTypes() {
        this.loadDatasetTypes();
        return this.datasetTypeList;
    }

    private void loadDatasetTypes() {
        this.datasetTypeList = (List<Document>) this.dataStoreDatasetType.find().into(new ArrayList<Document>());

    }

    @Override
    public Optional<List<Document>> getDatasetTypeByUri(String uri, String version) {
        List<Document> matchedDatasetTypeList;

        // version can be all, latest or specific version
        if (version.equals("all") || version.equals("latest")) {
            // due to latest and all need to be restricted by space
            // check latest later
            matchedDatasetTypeList = (List<Document>) this.dataStoreDatasetType
                .find(eq("url", uri)).into(new ArrayList<Document>());
        } else {
            matchedDatasetTypeList = (List<Document>) this.dataStoreDatasetType.find(and(eq("url", uri),
                eq("openvocab:versionnumber", version))).into(new ArrayList<Document>());
        }

        if (matchedDatasetTypeList.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(matchedDatasetTypeList);
        }

    }

    @Override
    public Optional<List<Document>> searchDatasetType(String datasetTypeName){
        List<Document> datasetTypeList = (List<Document>) this.dataStoreDatasetType.find().into(new ArrayList<Document>());
        List<Document> matchDatasetTypeList = new ArrayList<Document>();

        for (Document datsetType : datasetTypeList){
            String title = datsetType.get("dc:title").toString();
            if (title.toLowerCase().contains(datasetTypeName.toLowerCase())){
                matchDatasetTypeList.add(datsetType);
            }
        }

        if (matchDatasetTypeList.isEmpty()){
            return Optional.empty();
        } else {
            return Optional.of(matchDatasetTypeList);
        }
    }

    private Boolean checkNewDatasetType(Document newDatasetType){

        if (newDatasetType.get("@context") != null
            && newDatasetType.get("dc:license") != null
            && newDatasetType.get("dc:title") != null
            && newDatasetType.get("dc:description") != null
            && newDatasetType.get("url") != null
            && newDatasetType.get("openvocab:versionnumber") != null
            && newDatasetType.get("tableSchema") != null){

            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public String postDatasetType(Document newDatasetType) {
        if (newDatasetType != null && checkNewDatasetType(newDatasetType)) {
            // insert new dataset type
            this.dataStoreDatasetType.insertOne(newDatasetType);

            return newDatasetType.getObjectId("_id").toString();
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String deleteDatasetType(String id) {
        this.dataStoreDatasetType.findOneAndDelete(eq("_id", new ObjectId(id)));
        return id;
    }


}

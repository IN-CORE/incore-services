package edu.illinois.ncsa.incore.service.data.controllers;

import edu.illinois.ncsa.incore.service.data.dao.IRepository;
import edu.illinois.ncsa.incore.service.data.model.MvzLoader;
import edu.illinois.ncsa.incore.service.data.model.datawolf.domain.Dataset;
import edu.illinois.ncsa.incore.service.data.model.datawolf.domain.FileDescriptor;
import edu.illinois.ncsa.incore.service.data.model.datawolf.domain.impl.FileStorageDisk;
import edu.illinois.ncsa.incore.service.data.model.mvz.MvzDataset;
import edu.illinois.ncsa.incore.service.data.utils.ControllerFileUtils;
import edu.illinois.ncsa.incore.service.data.utils.ControllerJsonUtils;
import edu.illinois.ncsa.incore.service.data.utils.ControllerMongoUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ywkim on 7/26/2017.
 */

@Path("")
public class DataController {
    private static final String MONGO_URL = "mongodb://localhost:27017"; //$NON-NLS-1$
    private static final String MONGO_DB_NAME = "repoDB";    //$NON-NLS-1$
    private static final String DATA_REPO_FOLDER = "C:\\Users\\ywkim\\Downloads\\Rest";

    @Inject
    private IRepository repository;

    private Logger logger = Logger.getLogger(DataController.class);

    // test with:
    //http://localhost:8080/data/api/metadata/Shelby_County_RES31224702005658
    //http://localhost:8080/data/api/metadata/Memphis_Electric_Power_Facility_with_Topology_for_INA1213389330789
    //http://localhost:8080/data/api/metadata/HAZUS_Table_13.8_Collapse_Rates1209053226524
    //http://localhost:8080/data/api/metadata/Building_Disruption_Cost1168019087905

    /**
     * get metadata json from earthquake server using dataset id
     *
     * @param datasetId id of the dataset artifact
     * @return metadata mvzdataset object
     */
    @GET
    @Path("/metadata/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public MvzDataset getMetadataFromRepo(@PathParam("id") String datasetId) {
        String typeId = ControllerFileUtils.findTypeIdByDatasetId(datasetId, ControllerFileUtils.EXTENSION_META);
        MvzDataset mvzDataset = new MvzDataset();
        if (typeId.equals("")) {
            // create error mvz obj
            mvzDataset.setName("Error: no dataset existed");    //$NON-NLS-1$
        } else {
            String combinedId = typeId + "/" + datasetId;
            mvzDataset = MvzLoader.createMvzDatasetFromMetadata(combinedId);
        }
        return mvzDataset;
    }

    // test with
    //http://localhost:8080/data/api/datasets/59cd1d4763f94025803cee5c

    /**
     * Query dataset from database by given id
     *
     * @param datasetId dataset id for querying the dataset content from datase
     * @return dataset object
     */
    @GET
    @Path("/datasets/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Dataset getDatasetFromRepo(@PathParam("id") String datasetId) {
        return repository.getDatasetById(datasetId);
    }

    // test with
    //http://localhost:8080/data/api/collection/Dataset

    /**
     * create list of dataset in the database
     *
     * @return list of dataset
     */
    @GET
    @Path("/datasets")
    @Produces(MediaType.TEXT_HTML)
    public List<Dataset> getDatasetList() {
        return repository.getAllDatasets();
    }

    // test with
    //http://localhost:8080/data/api/collection/edu.illinois.ncsa.ergo.eq.buildings.schemas.buildingInventoryVer5.v1.0

    /**
     * see the metadata json of the dataset. data can be downloaded by clicking location
     *
     * @param collId collection id
     * @return list of document name
     */
    @GET
    @Path("/collection/{id}")
    @Produces(MediaType.TEXT_HTML)
    public List<String> getListInCollection(@PathParam("id") String collId) {
        return ControllerMongoUtils.getDocListByCollId(MONGO_URL, MONGO_DB_NAME, collId);
    }

//    // http//localhost:8080/data/api/ingest-result
//
//    /**
//     * ingest dataset into mongodb and file server
//     * @param inJson input json string
//     * @return output dataset object
//     */
//    @POST
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    @Path("/ingest-result")
//    public Dataset ingestResult(String inJson) {
//        // check if the input json string is valid
//        boolean isJsonValid = ControllerJsonUtils.isJSONValid(inJson);
//
//        // create DataWolf POJO object
//        Dataset dataset = new Dataset();
//        if (isJsonValid) {
//            String type = ControllerJsonUtils.extractValueFromJsonString(ControllerFileUtils.DATASET_TYPE, inJson);
//            String sourceDataset = ControllerJsonUtils.extractValueFromJsonString(ControllerFileUtils.DATASET_SOURCE_DATASET, inJson);
//            String format = ControllerJsonUtils.extractValueFromJsonString(ControllerFileUtils.DATASET_FORMAT, inJson);
//            List<String> spaces = ControllerJsonUtils.extractValueListFromJsonString(ControllerFileUtils.DATASET_SPACES, inJson);
//            dataset.setType(type);
//            dataset.setSourceDataset(sourceDataset);
//            dataset.setFormat(format);
//            dataset.setSpaces(spaces);
//        }
//
//        return repository.addDataset(dataset);
//    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/ingest-result")
    public Dataset uploadFile(
            @FormDataParam("file") InputStream is,
            @FormDataParam("file") FormDataContentDisposition fileDetail,
            @FormDataParam("description") String inDescJson) {

        String uploadedFileLocation = DATA_REPO_FOLDER;
        FileDescriptor fd = new FileDescriptor();
        FileStorageDisk fsd = new FileStorageDisk();

        fsd.setFolder(uploadedFileLocation);

        try {
            fd = fsd.storeFile(fileDetail.getName(), is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String storedUrl = fd.getDataURL();

        boolean isJsonValid = ControllerJsonUtils.isJSONValid(inDescJson);

        // create DataWolf POJO object
        Dataset dataset = new Dataset();
        if (isJsonValid) {
            String type = ControllerJsonUtils.extractValueFromJsonString(ControllerFileUtils.DATASET_TYPE, inDescJson);
            String sourceDataset = ControllerJsonUtils.extractValueFromJsonString(ControllerFileUtils.DATASET_SOURCE_DATASET, inDescJson);
            String format = ControllerJsonUtils.extractValueFromJsonString(ControllerFileUtils.DATASET_FORMAT, inDescJson);
            List<String> spaces = ControllerJsonUtils.extractValueListFromJsonString(ControllerFileUtils.DATASET_SPACES, inDescJson);
            dataset.setType(type);
            dataset.setSourceDataset(sourceDataset);
            dataset.setFormat(format);
            dataset.setSpaces(spaces);
            dataset.setStoredUrl(storedUrl);
        }

        return repository.addDataset(dataset);
    }

    // http

    // list all the metadatas in the repository information as json
    // zipped dataset can be downloaded when the location get clicked
    @GET
    @Path("/datasets/earthquake")
    @Produces(MediaType.APPLICATION_JSON)
    // http://localhost:8080/data/api/datasets
    public List<MvzDataset> getDirectoryListJson() {
        // create the POJO object;
        List<String> resHref = ControllerFileUtils.getDirectoryContent(ControllerFileUtils.REPO_PROP_URL, "");
        List<MvzDataset> mvzDatasets = new ArrayList<>();

        for (String tmpUrl : resHref) {
            String metaDirUrl = ControllerFileUtils.REPO_PROP_URL + tmpUrl;
            List<String> metaHref = ControllerFileUtils.getDirectoryContent(metaDirUrl, "");
            for (String metaFileName : metaHref) {
                String fileExtStr = FilenameUtils.getExtension(metaFileName);
                // get only the mvz file
                if (fileExtStr.equals(ControllerFileUtils.EXTENSION_META)) {
                    String combinedId = tmpUrl + "/" + metaFileName;
                    MvzDataset mvzDataset = new MvzDataset();
                    mvzDataset = MvzLoader.createMvzDatasetFromMetadata(combinedId);
                    mvzDatasets.add(mvzDataset);
                }
            }
        }

        return mvzDatasets;
    }

    // get the geojson from mongodb
    //http://localhost:8080/data/api/datasets/Shelby_County_RES31224702005658/mongo
    //http://localhost:8080/data/api/datasets/Memphis_Electric_Power_Facility_with_Topology_for_INA1213389330789/mongo
    //http://localhost:8080/data/api/datasets/HAZUS_Table_13.8_Collapse_Rates1209053226524/mongo
    //http://localhost:8080/data/api/datasets/Building_Disruption_Cost1168019087905/mongo
    @GET
    @Path("/datasets/{datasetId}/mongo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGeoJsonFromMongo(@PathParam("datasetId") String datasetId) {
        String outJson = ControllerJsonUtils.getJsonByDatasetIdFromMongo(datasetId, MONGO_URL, MONGO_DB_NAME);
        return Response.ok(outJson).status(Response.Status.OK).build();
    }

    // insert all dataset to mongodb
    // http://localhost:8080/data/api/datasets/ingestmongo
    @GET
    @Path("/datasets/ingestmongo")
    @Produces(MediaType.APPLICATION_JSON)
    public String ingestAllToMongo() {
        // list all the directory
        List<String> resHref = ControllerFileUtils.getDirectoryContent(ControllerFileUtils.REPO_PROP_URL, "");
        resHref.remove("properties");

        for (String tmpUrl : resHref) {
            //String dataDirUrl = REPO_PROP_URL + tmpUrl; // use follow to use metadata
            String dataDirUrl = ControllerFileUtils.REPO_DS_URL + tmpUrl; // use follow for actual dataset

            List<String> dataHref = ControllerFileUtils.getDirectoryContent(dataDirUrl, "");
            for (String dataFileName : dataHref) {
                String combinedId = dataDirUrl + "/" + dataFileName + "/converted/";    //$NON-NLS-1$
                List<String> fileNames = ControllerFileUtils.getDirectoryContent(combinedId, "");
                for (String fileName : fileNames) {
                    // skip if the file name is converted
                    if (!fileName.equals("converted")) {
                        String fileExtStr = FilenameUtils.getExtension(fileName);
                        // check out the file extension and decide to ingest
                        if (fileExtStr.equals(ControllerFileUtils.EXTENSION_SHP)) {
                            System.out.println("Ingesting " + tmpUrl + "/" + dataFileName + " to database.");   //$NON-NLS-1$ //$NON-NLS-2$
                            ControllerMongoUtils.ingestShpfileToMongo(tmpUrl, dataFileName, MONGO_URL, MONGO_DB_NAME, ControllerFileUtils.REPO_DS_URL);
                        } else if (fileExtStr.equals(ControllerFileUtils.EXTENSION_CSV)) {
                            System.out.println("Ingesting " + tmpUrl + "/" + dataFileName + " to database.");   //$NON-NLS-1$ //$NON-NLS-2$
                            ControllerMongoUtils.ingestCsvToMongo(ControllerFileUtils.EXTENSION_CSV, tmpUrl, dataFileName, MONGO_URL, MONGO_DB_NAME, ControllerFileUtils.REPO_DS_URL, ControllerFileUtils.SERVER_URL_PREFIX);
                        } else {
                            System.out.println("other file format " + fileExtStr);  //$NON-NLS-1$
                        }
                    }
                }
            }
        }

        return "done";
    }

    // test with the following line
    // http://localhost:8080/data/api/datasets/query?type=edu.illinois.ncsa.ergo.eq.buildings.schemas.buildingInventoryVer5.v1.0
    @GET
    @Path("datasets/query")
    @Produces(MediaType.APPLICATION_JSON)
    //public List<MvzDataset> getJsonObjTest(@QueryParam("type") String inTypeId) {
    //public List<MvzDataset> getJsonObjTest(@QueryParam("type") String inTypeId , @HeaderParam("X-Credential-Username") String username) {
    public List<MvzDataset> getJsonObjTest(@QueryParam("type") String inTypeId, @HeaderParam("HTTP_USER_AGENT") String username, @Context HttpHeaders headers) {
        String userAgent = headers.getRequestHeader("user-agent").get(0);
        System.out.println(username);
        String propUrl = ControllerFileUtils.REPO_PROP_URL + inTypeId;
        File metadata = null;

        List<String> resourceUrls = ControllerFileUtils.getDirectoryContent(propUrl, inTypeId);
        List<MvzDataset> mvzDatasets = new ArrayList<MvzDataset>();

        for (String rUrl : resourceUrls) {
            MvzDataset mvzDataset = new MvzDataset();
            mvzDataset = MvzLoader.createMvzDatasetFromMetadata(rUrl);
            mvzDatasets.add(mvzDataset);
        }

        return mvzDatasets;
    }

    //list all the metadata belonged to type id. data can be downloaded by clicking location
    //metadata converted as POJO object
    // http://localhost:8080/data/api/datasets/test?type=edu.illinois.ncsa.ergo.eq.buildings.schemas.buildingInventoryVer5.v1.0
    @GET
    @Path("/datasets/test")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDatasetByIdTest(@QueryParam("type") String typeId) {
        String propUrl = ControllerFileUtils.REPO_PROP_URL + typeId;
        File metadata = null;

        List<String> resourceUrls = ControllerFileUtils.getDirectoryContent(propUrl, typeId);
        String outJsonStr = "[\n";
        String combinedId = "";

        for (String rUrl : resourceUrls) {
            outJsonStr = outJsonStr;

            try {
                metadata = ControllerFileUtils.loadMetadataFromRepository(rUrl);
                outJsonStr = outJsonStr + MvzLoader.formatMetadataAsJson(metadata, rUrl, ControllerFileUtils.SERVER_URL_PREFIX) + ",\n";
            } catch (IOException e) {
                e.printStackTrace();
                ;
                String err = "{\"error:\" + \"" + e.getLocalizedMessage() + "\"}";
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        }
        outJsonStr = outJsonStr.substring(0, outJsonStr.length() - 2);
        outJsonStr = outJsonStr + "\n]";

        return Response.ok(outJsonStr).status(Response.Status.OK).build();
    }

    // directory listing
    @GET
    @Path("/datasets/list")     // this should be changed later for the appropriate line
    @Produces(MediaType.TEXT_HTML)
    // http://localhost:8080/data/api/datasets/list
    public String getDirectoryList() {
        try {
            return (ControllerFileUtils.loadDirectoryList());
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error:\" + \"" + e.getLocalizedMessage() + "\"}";
        }
    }

    // create geoJson of shapefile dataset
    //http://localhost:8080/data/api/datasets/edu.illinois.ncsa.ergo.eq.buildings.schemas.buildingInventoryVer5.v1.0/Shelby_County_RES31224702005658/geojson
    //http://localhost:8080/data/api/datasets/edu.illinois.ncsa.ergo.eq.lifeline.schemas.powerFacilityTopo.v1.0/Memphis_Electric_Power_Facility_with_Topology_for_INA1213389330789/geojson
    @GET
    @Path("/datasets/{typeid}/{datasetId}/geojson")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDatasetByTypeId(@PathParam("typeid") String typeId, @PathParam("datasetId") String datasetId) {
        File dataset = null;
        String combinedId = typeId + "/" + datasetId + "/converted/";   //$NON-NLS-1$
        String fileName = "";
        try {
            fileName = ControllerFileUtils.loadFileNameFromRepository(combinedId, ControllerFileUtils.EXTENSION_SHP, ControllerFileUtils.REPO_DS_URL);
            if (fileName.length() > 0) {
                dataset = new File(fileName);
                String outJson = ControllerJsonUtils.formatDatasetAsGeoJson(dataset);
                return Response.ok(outJson).status(Response.Status.OK).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (IOException e) {
            e.printStackTrace();
            String err = "{\"error:\" + \"" + e.getLocalizedMessage() + "\"}";
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    // create geoJson of shapefile dataset
    //http://localhost:8080/data/api/datasets/Shelby_County_RES31224702005658/earthquake
    //http://localhost:8080/data/api/datasets/Memphis_Electric_Power_Facility_with_Topology_for_INA1213389330789/earthquake
    //http://localhost:8080/data/api/datasets/HAZUS_Table_13.8_Collapse_Rates1209053226524/earthquake
    //http://localhost:8080/data/api/datasets/Building_Disruption_Cost1168019087905/earthquake
    @GET
    @Path("/datasets/{datasetId}/earthquake")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDatasetByDatasetId(@PathParam("datasetId") String datasetId) {
        String outJson = ControllerJsonUtils.getJsonByDatasetId(datasetId);
        return Response.ok(outJson).status(Response.Status.OK).build();
    }


    //list the dataset belonged to type
    //http://localhost:8080/data/api/datasets/edu.illinois.ncsa.ergo.eq.buildings.schemas.buildingInventoryVer5.v1.0
    @GET
    @Path("/datasets/type/{typeId}")
    @Produces(MediaType.TEXT_HTML)
    public String getDirectoryListWithId(@PathParam("typeId") String typeId) {
        try {
            return (ControllerFileUtils.loadDirectoryList(typeId));
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error:\" + \"" + e.getLocalizedMessage() + "\"}";    //$NON-NLS-1$
        }
    }

    // see the metadata json of the dataset. data can be downloaded by clicking location
    //http://localhost:8080/data/api/datasets/edu.illinois.ncsa.ergo.eq.buildings.schemas.buildingInventoryVer5.v1.0/Shelby_County_RES31224702005658
    @GET
    @Path("/datasets/{typeId}/{datasetId}")
    @Produces(MediaType.APPLICATION_JSON)
    public MvzDataset getMetadataById(@PathParam("typeId") String typeId, @PathParam("datasetId") String datasetId) {
        String combinedId = typeId + "/" + datasetId;
        MvzDataset mvzDataset = MvzLoader.createMvzDatasetFromMetadata(combinedId);

        return mvzDataset;
    }

    // download zipped dataset file
    // http://localhost:8080/data/api/datasets/edu.illinois.ncsa.ergo.eq.buildings.schemas.buildingInventoryVer5.v1.0/Shelby_County_RES31224702005658/files
    @GET
    @Path("/datasets/{typeId}/{datasetId}/files")
    @Produces(MediaType.TEXT_PLAIN)
    public File getShapefileById(@PathParam("typeId") String typeId, @PathParam("datasetId") String datasetId) {
        File dataset = null;

        String combinedId = typeId + "/" + datasetId + "/converted/";   //$NON-NLS-1$
        try {
            dataset = ControllerFileUtils.loadZipdataFromRepository(combinedId);
            return dataset;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ingest metadata into mongodb
    //http://localhost:8080/data/api/datasets//edu.illinois.ncsa.ergo.eq.buildings.decisionsupport.schemas.buildingCollapseRateTable.v1.0/ingest
    //http://localhost:8080/data/api/datasets/edu.illinois.ncsa.ergo.eq.buildings.schemas.buildingInventoryVer5.v1.0/ingest
    @GET
    @Path("/datasets/{typeId}/ingest")
    @Produces(MediaType.TEXT_PLAIN)
    public String ingestMetadataToMongo(@PathParam("typeId") String typeId) {
        // get the metadata file name list from the type directory
        String propUrl = ControllerFileUtils.REPO_PROP_URL + typeId;
        File metadata = null;

        List<String> resourceUrls = ControllerFileUtils.getDirectoryContent(propUrl, typeId);

        for (String tmpUrl : resourceUrls) {
            String metaDirUrl = ControllerFileUtils.REPO_PROP_URL + tmpUrl;
            List<String> metaHref = ControllerFileUtils.getDirectoryContent(metaDirUrl, "");
            for (String metaFileName : metaHref) {
                String fileExtStr = FilenameUtils.getExtension(metaFileName);
                String fileName = FilenameUtils.getBaseName(metaFileName);
                // get only the mvz file
                if (fileExtStr.equals(ControllerFileUtils.EXTENSION_META)) {
                    ControllerMongoUtils.ingestMetaToMongo(ControllerFileUtils.EXTENSION_CSV, typeId, fileName, MONGO_URL, MONGO_DB_NAME, ControllerFileUtils.REPO_PROP_URL, ControllerFileUtils.SERVER_URL_PREFIX);
                }
            }
        }
        return "Metadata ingested successfully";    //$NON-NLS-1$
    }

    //http://localhost:8080/data/api/datasets/edu.illinois.ncsa.ergo.eq.buildings.schemas.buildingInventoryVer5.v1.0/Shelby_County_RES31224702005658/ingest
    //http://localhost:8080/data/api/datasets/edu.illinois.ncsa.ergo.eq.lifeline.schemas.powerFacilityTopo.v1.0/Memphis_Electric_Power_Facility_with_Topology_for_INA1213389330789/ingest
    //http://localhost:8080/data/api/datasets/edu.illinois.ncsa.ergo.eq.buildings.decisionsupport.schemas.buildingCollapseRateTable.v1.0/HAZUS_Table_13.8_Collapse_Rates1209053226524/ingest
    //http://localhost:8080/data/api/datasets/edu.illinois.ncsa.ergo.eq.buildings.decisionsupport.schemas.buildingDisruptionCost.v1.0/Building_Disruption_Cost1168019087905/ingest
    @GET
    @Path("/datasets/{typeId}/{datasetId}/ingest")
    @Produces(MediaType.TEXT_PLAIN)
    public String ingestDatasetToMongo(@PathParam("typeId") String typeId, @PathParam("datasetId") String datasetId) {
        // check if it is shapefile or csv
        String combinedId = typeId + "/" + datasetId + "/converted/";   //$NON-NLS-1$
        int fileType = ControllerFileUtils.checkDataFormatFromRepository(combinedId, ControllerFileUtils.REPO_DS_URL);

        if (fileType >= ControllerFileUtils.TYPE_NUMBER_MULTI) {
            logger.error("There are multiple file formats in the directory.");  //$NON-NLS-1$
            return "There are multiple file formats in the directory."; //$NON-NLS-1$
        }

        if (fileType == ControllerFileUtils.TYPE_NUMBER_SHP) {    // ingest shapefile into mongodb
            if (ControllerMongoUtils.ingestShpfileToMongo(typeId, datasetId, MONGO_URL, MONGO_DB_NAME, ControllerFileUtils.REPO_DS_URL)) {
                return "Data ingested successfully";    //$NON-NLS-1$
            } else {
                return "There was a problem ingesting the data";    //$NON-NLS-1$
            }
        } else if (fileType == ControllerFileUtils.TYPE_NUMBER_CSV) { // ingest table into mongodb
            if (ControllerMongoUtils.ingestCsvToMongo(ControllerFileUtils.EXTENSION_CSV, typeId, datasetId, MONGO_URL, MONGO_DB_NAME, ControllerFileUtils.REPO_DS_URL, ControllerFileUtils.SERVER_URL_PREFIX)) {
                return "Data ingested successfully.";   //$NON-NLS-1$
            } else {
                return "There was a problem ingesting the data";    //$NON-NLS-1$
            }
        }
        return "The given file type was unknow. The ingestion terminated."; //$NON-NLS-1$
    }


}

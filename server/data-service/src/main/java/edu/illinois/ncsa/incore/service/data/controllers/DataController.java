package edu.illinois.ncsa.incore.service.data.controllers;

import edu.illinois.ncsa.incore.service.data.controllers.utils.ControllerFileUtils;
import edu.illinois.ncsa.incore.service.data.controllers.utils.ControllerJsonUtils;
import edu.illinois.ncsa.incore.service.data.controllers.utils.ControllerMongoUtils;
import edu.illinois.ncsa.incore.service.data.dao.IRepository;
import edu.illinois.ncsa.incore.service.data.geotools.GeotoolsUtils;
import edu.illinois.ncsa.incore.service.data.model.MvzLoader;
import edu.illinois.ncsa.incore.service.data.model.Space;
import edu.illinois.ncsa.incore.service.data.model.datawolf.domain.Dataset;
import edu.illinois.ncsa.incore.service.data.model.datawolf.domain.FileDescriptor;
import edu.illinois.ncsa.incore.service.data.model.datawolf.domain.impl.FileStorageDisk;
import edu.illinois.ncsa.incore.service.data.model.mvz.MvzDataset;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by ywkim on 7/26/2017.
 */

@Path("datasets")
public class DataController {
    private static final String MONGO_URL = "mongodb://localhost:27017"; //$NON-NLS-1$
    private static final String MONGO_DB_NAME = "repoDB";    //$NON-NLS-1$
    private static final String DATA_REPO_FOLDER = "C:\\Users\\ywkim\\Downloads\\Rest"; //$NON-NLS-1$
    private static final String POST_PARAMENTER_NAME = "name";  //$NON-NLS-1$
    private static final String POST_PARAMENTER_FILE = "file";  //$NON-NLS-1$
    private static final String POST_PARAMENTER_META = "metadata";  //$NON-NLS-1$
    private static final String POST_PARAMETER_DATASET_ID = "datasetId";    //$NON-NLS-1$

    @Inject
    private IRepository repository;

    private Logger logger = Logger.getLogger(DataController.class);

    // test with:
    //http://localhost:8080/data/api/datasets/metadata/Shelby_County_RES31224702005658
    //http://localhost:8080/data/api/datasets/metadata/Memphis_Electric_Power_Facility_with_Topology_for_INA1213389330789
    //http://localhost:8080/data/api/datasets/metadata/HAZUS_Table_13.8_Collapse_Rates1209053226524
    //http://localhost:8080/data/api/datasets/metadata/Building_Disruption_Cost1168019087905

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
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Dataset getDatasetFromRepo(@PathParam("id") String datasetId) {
        return repository.getDatasetById(datasetId);
    }

    //http://localhost:8080/data/api/datasets/joinshptable/59dfb53468f4742898a40267
    @GET
    @Path("/joinshptable/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Dataset getJoinedShapefile(@PathParam("id") String datasetId) throws IOException, URISyntaxException {
        Dataset dataset = repository.getDatasetById(datasetId);
        List<FileDescriptor> csvFDs = dataset.getFileDescriptors();
        File csvFile = null;
        for (int i=0;i<csvFDs.size();i++) {
            FileDescriptor csvFd = csvFDs.get(i);
            String csvLoc = csvFd.getDataURL();
            csvFile = new File(new URI(csvLoc));
        }

        Dataset sourceDataset = repository.getDatasetById(dataset.getSourceDataset());
        List<FileDescriptor> sourceFDs = sourceDataset.getFileDescriptors();
        String sourceType = sourceDataset.getType();
        List<File> shpfiles = new ArrayList<File>();
        boolean IsShpfile = false;

        if (!(sourceType.equalsIgnoreCase("shapefile"))) {
            for (int i = 0; i < sourceFDs.size(); i++) {
                FileDescriptor sfd = sourceFDs.get(i);
                String shpLoc = sfd.getDataURL();
                File shpFile = new File(new URI(shpLoc));
                //get file, if the file is in remote, use http downloader
                String fileExt = FilenameUtils.getExtension(shpLoc);
                if (fileExt.equalsIgnoreCase(ControllerFileUtils.EXTENSION_SHP)){
                    GeotoolsUtils.JoinTableShapefile(shpFile, csvFile);
                }
            }
        }
        return dataset;
    }

    //http://localhost:8080/data/api/datasets/list-datasets
    /**
     * create list of dataset in the database
     *
     * @return list of dataset
     */
    @GET
    @Path("/list-datasets")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Dataset> getDatasetList() {
        return repository.getAllDatasets();
    }

    //http://localhost:8080/data/api/datasets/list-datasets
    /**
     * create list of spaces in the database
     *
     * @return list of dataset
     */
    @GET
    @Path("/list-spaces")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Space> getSpaceList() {
        return repository.getAllSpaces();
    }

    // test with
    //http://localhost:8080/data/api/datasets/collection/edu.illinois.ncsa.ergo.eq.buildings.schemas.buildingInventoryVer5.v1.0

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

//    // http//localhost:8080/data/api/datasets/ingest-result
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

    //  http//localhost:8080/data/api/datasets/ingest-multi-files
    //    {datasetId: "59e0e8ed68f4740274f39733"}
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/ingest-multi-files")
    public Dataset uplaodFiles(FormDataMultiPart inputs) {

        int bodyPartSize = inputs.getBodyParts().size();
        String objIdStr = "";   //$NON-NLS-1$
        String inJson = ""; //$NON-NLS-1$
        String paramName = "";  //$NON-NLS-1$
        Dataset dataset = null;
        boolean isJsonValid = false;

        for (int i = 0; i < bodyPartSize; i++) {
            paramName = inputs.getBodyParts().get(i).getContentDisposition().getParameters().get(POST_PARAMENTER_NAME);
            if (paramName.equals(POST_PARAMENTER_META)) {
                inJson = (String) inputs.getFields(POST_PARAMENTER_META).get(0).getValueAs(String.class);
                isJsonValid = ControllerJsonUtils.isJSONValid(inJson);
                if (isJsonValid) {
                    objIdStr = ControllerJsonUtils.extractValueFromJsonString("datasetId", inJson);
                    dataset = repository.getDatasetById(objIdStr);
                } else {
                    return dataset;
                }
            }
        }

        for (int i = 0; i < bodyPartSize; i++) {
            paramName = inputs.getBodyParts().get(i).getContentDisposition().getParameters().get(POST_PARAMENTER_NAME);
            if (paramName.equals(POST_PARAMENTER_FILE)) {
                String fileName = inputs.getBodyParts().get(i).getContentDisposition().getFileName();
                InputStream is = (InputStream) inputs.getFields(POST_PARAMENTER_FILE).get(0).getValueAs(InputStream.class);
                FileDescriptor fd = new FileDescriptor();
                FileStorageDisk fsDisk = new FileStorageDisk();

                fsDisk.setFolder(DATA_REPO_FOLDER);
                try {
                    fd = fsDisk.storeFile(fileName, is);
                    fd.setFilename(fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dataset.addFileDescriptor(fd);
            }
        }
        repository.addDataset(dataset);

        return dataset;
    }

    // http//localhost:8080/data/api/datasets/ingest-dataset
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/ingest-dataset")
    public Dataset ingestDataset(@FormDataParam("dataset") String inDatasetJson) {

        // example input json
        //{ schema: "buildingInventory", type: "http://localhost:8080/semantics/edu.illinois.ncsa.ergo.eq.buildings.schemas.buildingInventoryVer5.v1.0", sourceDataset: "", format: "shapefile", spaces: ["ywkim", "ergo"] }
        //{ schema: "buildingDamage", type: "http://localhost:8080/semantics/edu.illinois.ncsa.ergo.eq.buildings.schemas.buildingDamage.v1.0", sourceDataset: "59e0eb7d68f4742a342d9738", format: "csv", spaces: ["ywkim", "ergo"] }
        boolean isJsonValid = ControllerJsonUtils.isJSONValid(inDatasetJson);

        // create DataWolf POJO object
        Dataset dataset = new Dataset();
        if (isJsonValid) {
            String type = ControllerJsonUtils.extractValueFromJsonString(ControllerFileUtils.DATASET_TYPE, inDatasetJson);
            String sourceDataset = ControllerJsonUtils.extractValueFromJsonString(ControllerFileUtils.DATASET_SOURCE_DATASET, inDatasetJson);
            String format = ControllerJsonUtils.extractValueFromJsonString(ControllerFileUtils.DATASET_FORMAT, inDatasetJson);
            String fileName = ControllerJsonUtils.extractValueFromJsonString(ControllerFileUtils.DATASET_FILE_NAME, inDatasetJson);
            List<String> spaces = ControllerJsonUtils.extractValueListFromJsonString(ControllerFileUtils.DATASET_SPACES, inDatasetJson);
            dataset.setType(type);
            dataset.setSourceDataset(sourceDataset);
            dataset.setFormat(format);
            dataset.setSpaces(spaces);

            dataset = repository.addDataset(dataset);

            String id = dataset.getId();

            // insert/update space
            for (String spaceName : spaces) {
                Space foundSpace = repository.getSpaceByName(spaceName);
                if (foundSpace == null) {   // new space: insert the data
                    Space space = new Space();
                    space.setName(spaceName);
                    List<String> datasetIds = new ArrayList<String>();
                    datasetIds.add(id);
                    space.setDatasetIds(datasetIds);
                    repository.addSpace(space);
                } else {    // the space with space name exists
                    // get dataset ids
                    List<String> datasetIds = foundSpace.getDatasetIds();
                    boolean isIdExists = false;
                    for (String datasetId : datasetIds) {
                        if (datasetId.equals(id)) {
                            isIdExists = true;
                        }
                    }
                    if (!isIdExists) {
                        foundSpace.addId(id);
                        // this will update it since the objectId is identical
                        repository.addSpace(foundSpace);
                    }
                }
            }
        }

        return dataset;
    }

    // http//localhost:8080/data/api/datasets/ingest-result
    // example input json
    //{ datasetId: "59dfb20a68f4742898e0e1e4" }

    /**
     * @param is
     * @param fileDetail
     * @param inDescJson
     * @return
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/ingest-result")
    public Dataset uploadFile(
            @FormDataParam("file") InputStream is,
            @FormDataParam("file") FormDataContentDisposition fileDetail,
            @FormDataParam("description") String inDescJson) {

        FileDescriptor fd = new FileDescriptor();
        FileStorageDisk fsDisk = new FileStorageDisk();

        fsDisk.setFolder(DATA_REPO_FOLDER);

        try {
            fd = fsDisk.storeFile(fileDetail.getName(), is);
            fd.setFilename(fileDetail.getFileName());
        } catch (IOException e) {
            e.printStackTrace();
        }

        String objIdStr = ControllerJsonUtils.extractValueFromJsonString(POST_PARAMETER_DATASET_ID, inDescJson);
        Dataset dataset = repository.getDatasetById(objIdStr);
        dataset.addFileDescriptor(fd);
        repository.addDataset(dataset);

        return dataset;
    }

    // http

    // list all the metadatas in the repository information as json
    // zipped dataset can be downloaded when the location get clicked
    @GET
    @Path("/earthquake")
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
    @Path("/{datasetId}/mongo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGeoJsonFromMongo(@PathParam("datasetId") String datasetId) {
        String outJson = ControllerJsonUtils.getJsonByDatasetIdFromMongo(datasetId, MONGO_URL, MONGO_DB_NAME);
        return Response.ok(outJson).status(Response.Status.OK).build();
    }

    // insert all dataset to mongodb
    // http://localhost:8080/data/api/datasets/ingestmongo
    @GET
    @Path("/ingestmongo")
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
    @Path("query")
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
    @Path("/test")
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
    @Path("/list-html")     // this should be changed later for the appropriate line
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
    @Path("/{typeid}/{datasetId}/geojson")
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
    @Path("/{datasetId}/earthquake")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDatasetByDatasetId(@PathParam("datasetId") String datasetId) {
        String outJson = ControllerJsonUtils.getJsonByDatasetId(datasetId);
        return Response.ok(outJson).status(Response.Status.OK).build();
    }


    //list the dataset belonged to type
    //http://localhost:8080/data/api/datasets/edu.illinois.ncsa.ergo.eq.buildings.schemas.buildingInventoryVer5.v1.0
    @GET
    @Path("/type/{typeId}")
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
    @Path("/{typeId}/{datasetId}")
    @Produces(MediaType.APPLICATION_JSON)
    public MvzDataset getMetadataById(@PathParam("typeId") String typeId, @PathParam("datasetId") String datasetId) {
        String combinedId = typeId + "/" + datasetId;
        MvzDataset mvzDataset = MvzLoader.createMvzDatasetFromMetadata(combinedId);

        return mvzDataset;
    }

    // download zipped dataset file
    // http://localhost:8080/data/api/datasets/edu.illinois.ncsa.ergo.eq.buildings.schemas.buildingInventoryVer5.v1.0/Shelby_County_RES31224702005658/files
    @GET
    @Path("/{typeId}/{datasetId}/files")
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
    @Path("/{typeId}/ingest")
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
    @Path("/{typeId}/{datasetId}/ingest")
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

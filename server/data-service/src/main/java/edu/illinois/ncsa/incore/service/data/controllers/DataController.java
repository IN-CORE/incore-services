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
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
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
    private static final String UPDATE_OBJECT_NAME = "property name";  //$NON-NLS-1$
    private static final String UPDATE_OBJECT_VALUE = "property value";  //$NON-NLS-1$
    private static final String FILE_ZIP_EXTENSION = "zip"; //$NON-NLS-1$
    private static final String FILE_SHAPFILE_NAME = "shapefile";   //$NON-NLS-1$

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

    //http://localhost:8080/data/api/datasets/list-datasets
    /**
     * create list of dataset in the database
     *
     * @return list of dataset
     */
    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Dataset> getDatasetList() {
        return repository.getAllDatasets();
    }

    //http://localhost:8080/data/api/datasets/59e5098168f47426547409f3/files
    /**
     * download zip file of the files belonged to the dataset based on filedescriptor
     * @param datasetId id of the Dataset in mongodb
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    @GET
    @Path("/{id}/files")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFileByDataset(@PathParam("id") String datasetId) throws IOException, URISyntaxException{
        Dataset dataset = repository.getDatasetById(datasetId);
        List<FileDescriptor> fds = dataset.getFileDescriptors();
        List<File> fileList = new ArrayList<File>();
        String absolutePath = "";   //$NON-NLS-1$
        String filePath = "";   //$NON-NLS-1$
        String fileBaseName = "";   //$NON-NLS-1$
        String fileName = "";   //$NON-NLS-1$
        File outFile = null;

        if (fds.size() > 0) {
            File tmpFile = new File(fds.get(0).getDataURL());
            absolutePath = tmpFile.getPath();
            filePath = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator));
            fileBaseName = FilenameUtils.getBaseName(tmpFile.getName());

            List<String> fileNameList = new ArrayList<String>();
            for (FileDescriptor fd: fds) {
                String dataUrl = fd.getDataURL();
                fileList.add(new File(new URI(dataUrl)));
                fileNameList.add(FilenameUtils.getName(dataUrl));
            }

            // create temp dir and copy files to temp dir
            String tempDir = Files.createTempDirectory(ControllerFileUtils.DATA_TEMP_DIR_PREFIX).toString();
            // copiedFileList below is not used but the method is needed to copy files
            List<File> copieFileList = GeotoolsUtils.copyFilesToTmpDir(fileList, tempDir);

            outFile =  ControllerFileUtils.createZipFile(fileNameList, tempDir, fileBaseName);
            fileName = fileBaseName + "." + FILE_ZIP_EXTENSION;
        }
        return Response.ok(outFile, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition", "attachment; filename=\"" + fileName + "\"" ).build();
    }

    //http://localhost:8080/data/api/datasets/59e5098168f47426547409f3/filedescriptors/59e50a2568f4742654e59629/files

    /**
     * Download a file attached to a given FileDescriptor
     *
     * @param datasetId Dataset id
     * @param inFdId    FileDescriptor id in the Dataset
     * @return
     * @throws URISyntaxException
     */
    @GET
    @Path("/{id}/filedescriptors/{fdid}/files")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFileByFileDescriptor(@PathParam("id") String datasetId, @PathParam("fdid") String inFdId) throws URISyntaxException{
        File outFile =  null;
        Dataset dataset = repository.getDatasetById(datasetId);
        List<FileDescriptor> fds = dataset.getFileDescriptors();
        String dataUrl = ""; //$NON-NLS-1$
        String fdId = "";   //$NON-NLS-1$
        String fileName = "";   //$NON-NLS-1$

        for (FileDescriptor fd: fds) {
            fdId = fd.getId();
            if (fdId.equals(inFdId)) {
                dataUrl = fd.getDataURL();
                fileName = fd.getFilename();
            }
        }

        if (!dataUrl.equals("")) {  //$NON-NLS-1$
            outFile = new File(new URI(dataUrl));
            outFile.renameTo(new File(outFile.getParentFile(), fileName));
        }

        return Response.ok(outFile, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition", "attachment; filename=\"" + fileName + "\"" ).build();
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

    //http://localhost:8080/data/api/datasets/joinshptable/59e509ca68f4742654e59621
    @GET
    @Path("/joinshptable/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getJoinedShapefile(@PathParam("id") String datasetId) throws IOException, URISyntaxException {
        Dataset dataset = repository.getDatasetById(datasetId);
        List<FileDescriptor> csvFDs = dataset.getFileDescriptors();
        File csvFile = null;
        String outFileName = "";    //$NON-NLS-1$
        for (int i=0;i<csvFDs.size();i++) {
            FileDescriptor csvFd = csvFDs.get(i);
            String csvLoc = csvFd.getDataURL();
            csvFile = new File(new URI(csvLoc));
        }

        Dataset sourceDataset = repository.getDatasetById(dataset.getSourceDataset());
        List<FileDescriptor> sourceFDs = sourceDataset.getFileDescriptors();
        String sourceType = sourceDataset.getType();
        List<File> shpfiles = new ArrayList<File>();
        File zipFile = null;
        boolean isShpfile = false;

        if (!(sourceType.equalsIgnoreCase(FILE_SHAPFILE_NAME))) {
            for (int i = 0; i < sourceFDs.size(); i++) {
                FileDescriptor sfd = sourceFDs.get(i);
                String shpLoc = sfd.getDataURL();
                File shpFile = new File(new URI(shpLoc));
                shpfiles.add(shpFile);
                //get file, if the file is in remote, use http downloader
                String fileExt = FilenameUtils.getExtension(shpLoc);
                if (fileExt.equalsIgnoreCase(ControllerFileUtils.EXTENSION_SHP)){
                    isShpfile = true;
                }
            }
        }
        if (isShpfile) {
            zipFile = GeotoolsUtils.JoinTableShapefile(shpfiles, csvFile);
            outFileName = FilenameUtils.getBaseName(zipFile.getName()) + "." + FILE_ZIP_EXTENSION;
        }

        return Response.ok(zipFile, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition", "attachment; filename=\"" + outFileName + "\"" ).build();
    }

//    {datasetId: "59e0ec0c68f4742a340411d2", property name: "sourceDataset", property value: "59e0eb7d68f4742a342d9738"}
    /**
     * Update dataset property using given json input
     * @param inDatasetJson
     * @return
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/update")
    public Object updateObject(@FormDataParam("update") String inDatasetJson) {
        boolean isJsonValid = ControllerJsonUtils.isJSONValid(inDatasetJson);
        Dataset dataset = null;

        if (isJsonValid) {
            String datasetId = ControllerJsonUtils.extractValueFromJsonString(POST_PARAMETER_DATASET_ID, inDatasetJson);
            String propName = ControllerJsonUtils.extractValueFromJsonString(UPDATE_OBJECT_NAME, inDatasetJson);
            String propVal = ControllerJsonUtils.extractValueFromJsonString(UPDATE_OBJECT_VALUE, inDatasetJson);
            dataset = repository.updateDataset(datasetId, propName, propVal);
        }

        return dataset;
    }

    //  http//localhost:8080/data/api/datasets/ingest-multi-files
    //    {datasetId: "59e5046668f47426549b606e"}
    /**
     * Upload multiple files and create FileDescriptors and attach those to dataset
     * @param inputs    post paraemters including file and metadata
     * @return
     */
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

        int j = 0;
        for (int i = 0; i < bodyPartSize; i++) {
            paramName = inputs.getBodyParts().get(i).getContentDisposition().getParameters().get(POST_PARAMENTER_NAME);
            if (paramName.equals(POST_PARAMENTER_FILE)) {
                String fileName = inputs.getBodyParts().get(i).getContentDisposition().getFileName();
                InputStream is = (InputStream) inputs.getFields(POST_PARAMENTER_FILE).get(j).getValueAs(InputStream.class);
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
                j++;
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
        //{ schema: "buildingInventory", type: "http://localhost:8080/semantics/edu.illinois.ncsa.ergo.eq.schemas.buildingInventoryVer4.v1.0", title: "Shelby_County_Essential_Facilities", sourceDataset: "", format: "shapefile", spaces: ["ywkim", "ergo"] }
        //{ schema: "buildingDamage", type: "http://localhost:8080/semantics/edu.illinois.ncsa.ergo.eq.schemas.buildingDamageVer4.v1.0", title: "shelby building damage", sourceDataset: "59e5098168f47426547409f3", format: "csv", spaces: ["ywkim", "ergo"] }
        boolean isJsonValid = ControllerJsonUtils.isJSONValid(inDatasetJson);
        String title = "";
        String type = "";
        String sourceDataset = "";
        String format = "";
        String fileName = "";
        List<String> spaces = null;

        // create DataWolf POJO object
        Dataset dataset = new Dataset();
        if (isJsonValid) {
            title = ControllerJsonUtils.extractValueFromJsonString(ControllerFileUtils.DATASET_TITLE, inDatasetJson);
            type = ControllerJsonUtils.extractValueFromJsonString(ControllerFileUtils.DATASET_TYPE, inDatasetJson);
            sourceDataset = ControllerJsonUtils.extractValueFromJsonString(ControllerFileUtils.DATASET_SOURCE_DATASET, inDatasetJson);
            format = ControllerJsonUtils.extractValueFromJsonString(ControllerFileUtils.DATASET_FORMAT, inDatasetJson);
            fileName = ControllerJsonUtils.extractValueFromJsonString(ControllerFileUtils.DATASET_FILE_NAME, inDatasetJson);
            spaces = ControllerJsonUtils.extractValueListFromJsonString(ControllerFileUtils.DATASET_SPACES, inDatasetJson);
            dataset.setTitle(title);
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
                        foundSpace.addDatasetId(id);
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
}

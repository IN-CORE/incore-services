/*
 * ******************************************************************************
 *   Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 *   This program and the accompanying materials are made available under the
 *   terms of the BSD-3-Clause which accompanies this distribution,
 *   and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *  ******************************************************************************
 */

package edu.illinois.ncsa.incore.service.data.controllers;

import edu.illinois.ncsa.incore.common.config.Config;
import edu.illinois.ncsa.incore.service.data.dao.HttpDownloader;
import edu.illinois.ncsa.incore.service.data.dao.IRepository;
import edu.illinois.ncsa.incore.service.data.geoserver.GeoserverUtils;
import edu.illinois.ncsa.incore.service.data.geotools.GeotoolsUtils;
import edu.illinois.ncsa.incore.service.data.model.MvzLoader;
import edu.illinois.ncsa.incore.service.data.model.Space;
import edu.illinois.ncsa.incore.service.data.model.datawolf.domain.Dataset;
import edu.illinois.ncsa.incore.service.data.model.datawolf.domain.FileDescriptor;
import edu.illinois.ncsa.incore.service.data.model.datawolf.domain.impl.FileStorageDisk;
import edu.illinois.ncsa.incore.service.data.model.mvz.MvzDataset;
import edu.illinois.ncsa.incore.service.data.utils.FileUtils;
import edu.illinois.ncsa.incore.service.data.utils.JsonUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ywkim on 7/26/2017.
 */

@Path("datasets")
public class DataController {
    private static final String DATA_REPO_FOLDER = Config.getConfigProperties().getProperty("data.repo.data.dir");  //$NON-NLS-1$
    private static final String POST_PARAMENTER_NAME = "name";  //$NON-NLS-1$
    private static final String POST_PARAMENTER_FILE = "file";  //$NON-NLS-1$
    private static final String POST_PARAMENTER_META = "parentdataset";  //$NON-NLS-1$
    private static final String POST_PARAMETER_DATASET_ID = "datasetId";    //$NON-NLS-1$
    private static final String UPDATE_OBJECT_NAME = "property name";  //$NON-NLS-1$
    private static final String UPDATE_OBJECT_VALUE = "property value";  //$NON-NLS-1$
    private static final String WEBDAV_SPACE_NAME = "earthquake";   //$NON-NLS-1$
    private Logger logger = Logger.getLogger(DataController.class);

    @Inject
    private IRepository repository;

    //http://localhost:8080/data/api/datasets/59cd1d4763f94025803cee5c
    /**
     * Returns a list of datasets in the Dataset collection
     *
     * @param datasetId dataset id for querying the dataset content from datase
     * @return dataset object
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Dataset getDatasetFromRepo(String username, @PathParam("id") String datasetId) {
        Dataset dataset = repository.getDatasetById(datasetId);
        if (dataset == null) {
            throw new NotFoundException("There is no Dataset with given id in the repository.");
        }

        return dataset;
    }

    //http://localhost:8080/data/api/datasets?type=edu.illinois.ncsa.ergo.eq.buildings.schemas&title=shelby
    /**
     * query dataset by using either title or type or both
     * @param typeStr
     * @param titleStr
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Dataset> getDatasets(@QueryParam("type") String typeStr, @QueryParam("title") String titleStr) {
        List<Dataset> datasets = null;
        if (typeStr != null && titleStr == null) {  // query only for the type
            datasets = repository.getDatasetByType(typeStr);
        } else if (typeStr == null && titleStr != null) {   // query only for the title
            datasets = repository.getDatasetByTitle(titleStr);
        } else if (typeStr != null && titleStr != null) {   // query for both type and title
            datasets = repository.getDatasetByTypeAndTitle(typeStr, titleStr);
        } else {
            datasets = repository.getAllDatasets();
        }

        return datasets;
    }

    //http://localhost:8080/data/api/datasets/list-datasets
    /**
     * create list of spaces in the database
     * @return list of dataset
     */
    @GET
    @Path("/list-spaces")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Space> getSpaceList() {
        List<Space> spaces = repository.getAllSpaces();
        if (spaces == null) {
            throw new NotFoundException("There is no Space in the repository.");
        }
        return spaces;
    }

    //http://localhost:8080/data/api/datasets/59e5098168f47426547409f3/files
    /**
     * Returns a zip file that contains all the files attached to a dataset specified by {id} using FileDescriptor in the dataset
     * @param datasetId id of the Dataset in mongodb
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    @GET
    @Path("/{id}/files")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFileByDataset(@PathParam("id") String datasetId) throws IOException, URISyntaxException {
        File outFile = FileUtils.loadFileFromService(datasetId, repository, false, "");
        String fileName = outFile.getName();

        if (outFile != null) {
            return Response.ok(outFile, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition", "attachment; filename=\"" + fileName + "\"").build(); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            return Response.status(404).build();
        }
    }

    //http://localhost:8080/data/api/datasets/files/59f775fce1b2b8080c37aa60/file
    /**
     * Returns a file that is attached to a FileDescriptor specified by {fdid} in a dataset
     * @param id    FileDescriptor id in the Dataset
     * @return
     * @throws URISyntaxException
     */
    @GET
    @Path("/files/{file-id}/file")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFileByFileDescriptor(@PathParam("file-id") String id) throws URISyntaxException {
        File outFile = null;
        Dataset dataset = repository.getDatasetByFileDescriptorId(id);

        List<FileDescriptor> fds = dataset.getFileDescriptors();
        String dataUrl = ""; //$NON-NLS-1$
        String fdId = "";   //$NON-NLS-1$
        String fileName = "";   //$NON-NLS-1$

        for (FileDescriptor fd : fds) {
            fdId = fd.getId();
            if (fdId.equals(id)) {
                dataUrl = fd.getDataURL();
                fileName = fd.getFilename();
            }
        }

        if (!dataUrl.equals("")) {  //$NON-NLS-1$
            outFile = new File(new URI(dataUrl));
            outFile.renameTo(new File(outFile.getParentFile(), fileName));
        }

        if (outFile != null) {
            return Response.ok(outFile, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition", "attachment; filename=\"" + fileName + "\"").build(); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            return Response.status(404).build();
        }
    }

    //http://localhost:8080/data/api/datasets/joinshptable/59e509ca68f4742654e59621
    /**
     * Returns a zip file of shapefile after joinig analysis result table dataset specified by {id} using result dataset's source dataset shapefile
     * @param datasetId input result dataset id
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    @GET
    @Path("/joinshptable/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getJoinedShapefile(@PathParam("id") String datasetId) throws IOException, URISyntaxException {
        Dataset dataset = repository.getDatasetById(datasetId);
        if (dataset == null) {
            throw new NotFoundException("There is no Dataset with given id in the repository.");    //$NON-NLS-1$
        }

        File zipFile = FileUtils.joinShpTable(dataset, repository, false);
        String outFileName = FilenameUtils.getBaseName(zipFile.getName()) + "." + FileUtils.FILE_ZIP_EXTENSION;

        if (zipFile != null) {
            return Response.ok(zipFile, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition", "attachment; filename=\"" + outFileName + "\"").build();  //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            return Response.status(404).build();
        }
    }

    // http//localhost:8080/data/api/datasets/ingest-dataset
    /** ingest dataset object using json
     * @param inDatasetJson
     * @return
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/ingest-dataset")
    public Dataset ingestDataset(@FormDataParam("dataset") String inDatasetJson) {
        // example input json
        //
        //{ schema: "buildingDamage", type: "http://localhost:8080/semantics/edu.illinois.ncsa.ergo.eq.schemas.buildingDamageVer4.v1.0", title: "shelby building damage", sourceDataset: "59e5098168f47426547409f3", format: "csv", spaces: ["ywkim", "ergo"] }
        boolean isJsonValid = JsonUtils.isJSONValid(inDatasetJson);
        String title = "";
        String type = "";
        String sourceDataset = "";
        String format = "";
        String fileName = "";
        List<String> spaces = null;

        // create DataWolf POJO object
        Dataset dataset = new Dataset();
        if (isJsonValid) {
            title = JsonUtils.extractValueFromJsonString(FileUtils.DATASET_TITLE, inDatasetJson);
            type = JsonUtils.extractValueFromJsonString(FileUtils.DATASET_TYPE, inDatasetJson);
            sourceDataset = JsonUtils.extractValueFromJsonString(FileUtils.DATASET_SOURCE_DATASET, inDatasetJson);
            format = JsonUtils.extractValueFromJsonString(FileUtils.DATASET_FORMAT, inDatasetJson);
            fileName = JsonUtils.extractValueFromJsonString(FileUtils.DATASET_FILE_NAME, inDatasetJson);
            spaces = JsonUtils.extractValueListFromJsonString(FileUtils.DATASET_SPACES, inDatasetJson);
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

    //  http//localhost:8080/data/api/datasets/upload-files
    //    {datasetId: "59e5046668f47426549b606e"}
    /**
     * upload file(s) to attach to a dataset by FileDescriptor
     * @param inputs post paraemters including file and metadata
     * @return
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/upload-files")
    public Dataset uplaodFiles(FormDataMultiPart inputs) throws IOException, URISyntaxException {
        int bodyPartSize = inputs.getBodyParts().size();
        String objIdStr = "";   //$NON-NLS-1$
        String inJson = ""; //$NON-NLS-1$
        String paramName = "";  //$NON-NLS-1$
        Dataset dataset = null;
        boolean isJsonValid = false;
        boolean isGeoserver = false;
        boolean isAsc = false;
        boolean isShp = false;
        boolean isTif = false;
        boolean isJoin = false;

        for (int i = 0; i < bodyPartSize; i++) {
            paramName = inputs.getBodyParts().get(i).getContentDisposition().getParameters().get(POST_PARAMENTER_NAME);
            if (paramName.equals(POST_PARAMENTER_META)) {
                inJson = (String) inputs.getFields(POST_PARAMENTER_META).get(0).getValueAs(String.class);
                isJsonValid = JsonUtils.isJSONValid(inJson);
                if (isJsonValid) {
                    objIdStr = JsonUtils.extractValueFromJsonString("datasetId", inJson);
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
                String fileExt = FilenameUtils.getExtension(fileName);
                if (fileExt.equalsIgnoreCase("shp") || fileExt.equalsIgnoreCase("asc") ||   //$NON-NLS-1$ //$NON-NLS-2$
                        fileExt.equalsIgnoreCase("tif")) {  //$NON-NLS-1
                    isGeoserver = true;
                    if (fileExt.equalsIgnoreCase("asc")) {  //$NON-NLS-1$
                        isAsc = true;
                    } else if (fileExt.equalsIgnoreCase("tif")) {   //$NON-NLS-1$
                        isTif = true;
                    } else if (fileExt.equalsIgnoreCase("shp")) {   //$NON-NLS-1$
                        isShp = true;
                    }
                }
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

        // check if there is a source dataset, if so it will be joined to source dataset
        String type = dataset.getFormat();
        String sourceDataset = dataset.getSourceDataset();
        // join it if it is a table dataset with source dataset existed
        if (sourceDataset.length() > 0 && type.equalsIgnoreCase("table")) {
            isJoin = true;
            isGeoserver = true;
        }

        if (isGeoserver) {
            if (isJoin) {
                File zipFile = FileUtils.joinShpTable(dataset, repository, true);
                boolean published = GeoserverUtils.uploadShpZipToGeoserver(dataset.getId(), zipFile);
            } else {
                boolean published = GeoserverUtils.datasetUploadToGeoserver(dataset, repository, isShp, isTif, isAsc);
            }
        }
        return dataset;
    }

    // {property name: "sourceDataset", property value: "59e0eb7d68f4742a342d9738"}
    /**
     * file(s) to upload to attach to a dataset by FileDescriptor
     * @param inDatasetJson
     * @return
     */
    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/update")
    public Object updateObject(@PathParam("id") String datasetId, @FormDataParam("update") String inDatasetJson) {
        boolean isJsonValid = JsonUtils.isJSONValid(inDatasetJson);
        Dataset dataset = null;

        if (isJsonValid) {
            String propName = JsonUtils.extractValueFromJsonString(UPDATE_OBJECT_NAME, inDatasetJson);
            String propVal = JsonUtils.extractValueFromJsonString(UPDATE_OBJECT_VALUE, inDatasetJson);
            dataset = repository.updateDataset(datasetId, propName, propVal);
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

        String objIdStr = JsonUtils.extractValueFromJsonString(POST_PARAMETER_DATASET_ID, inDescJson);
        Dataset dataset = repository.getDatasetById(objIdStr);
        dataset.addFileDescriptor(fd);
        repository.addDataset(dataset);

        return dataset;
    }

    // http://localhost:8080/data/api/datasets/dump
    /**
     * Dump all datasets in earthquake server to database as Datasets
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    @GET
    @Path("/dump")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Dataset> getDatasetFromWebdav() throws IOException, URISyntaxException {
        List<Dataset> datasets = new ArrayList<Dataset>();
        List<String> resHref = FileUtils.getDirectoryContent(FileUtils.REPO_DS_URL, "");
        String spaceName = WEBDAV_SPACE_NAME;
        String typeId = null;
        String datasetId = null;
        String mvzFileNameLoc = null;
        String dsIdFileDir = null;
        String mvzUrl = null;
        String dsFileUrl = null;
        List<String> downloadFileUrls = null;
        List<File> delFiles = new ArrayList<File>();
        String datasetTitle = null;
        String datasetFormat = null;
        String datasetType = null;
        List<String> datasetIds = null;

        // this tmpUrl should be the file type
        for (String tmpUrl : resHref) {
            // get mvz file
            String mvzDirUrl = FileUtils.REPO_PROP_URL + tmpUrl;
            List<String> mvzHref = FileUtils.getDirectoryContent(mvzDirUrl, "");
            for (String mvzFileName : mvzHref) {
                String mvzFileExtStr = FilenameUtils.getExtension(mvzFileName);
                if (mvzFileExtStr.equals(FileUtils.EXTENSION_META)) {
                    typeId = tmpUrl;
                    datasetId = FilenameUtils.getBaseName(mvzFileName);
                    if (datasetId.equalsIgnoreCase("Shelby_County%2C_TN_Boundary1212593366993")) {  //$NON-NLS-1$
                        System.out.println("check");
                        datasetId = "Shelby_County,_TN_Boundary1212593366993";  //$NON-NLS-1$
                        mvzFileName = "Shelby_County,_TN_Boundary1212593366993.mvz";    //$NON-NLS-1$
                    }
                    // ceate MvzDataset
                    mvzFileNameLoc = tmpUrl + "/" + mvzFileName;
                    mvzUrl = FileUtils.REPO_PROP_URL + mvzFileNameLoc;
                    String tempMetaDir = Files.createTempDirectory(FileUtils.DATA_TEMP_DIR_PREFIX).toString();
                    HttpDownloader.downloadFile(mvzUrl, tempMetaDir);
                    File metadata = new File(tempMetaDir + File.separator + mvzFileName);
                    MvzDataset mvzDataset = MvzLoader.setMvzDatasetFromMetadata(metadata, mvzFileNameLoc);

                    datasetTitle = mvzDataset.getName();
                    datasetFormat = mvzDataset.getDataFormat();
                    datasetType = mvzDataset.getTypeId();

                    downloadFileUrls = new ArrayList<String>();
                    downloadFileUrls.add(mvzUrl);

                    dsIdFileDir = FileUtils.REPO_DS_URL + typeId + "/" + datasetId + "/converted/";   //$NON-NLS-1$ //$NON-NLS-2$
                    List<String> idDirFileContent = FileUtils.getDirectoryContent(dsIdFileDir, "");

                    // construct the list of files to download
                    for (String tmpFileName : idDirFileContent) {
                        String tmpFileExt = FilenameUtils.getExtension(tmpFileName);
                        if (tmpFileExt.length() > 0) {
                            dsFileUrl = dsIdFileDir + tmpFileName;
                            downloadFileUrls.add(dsFileUrl);
                        }
                    }

                    // create dataset
                    Dataset dataset = new Dataset();
                    dataset.setTitle(datasetTitle);
                    dataset.setType(datasetType);
                    dataset.setFormat(datasetFormat);
                    List<String> spaces = new ArrayList<String>();
                    spaces.add(spaceName);
                    dataset.setSpaces(spaces);

                    if (downloadFileUrls != null && downloadFileUrls.size() > 0) {
                        delFiles = new ArrayList<File>();
                        String tempDir = Files.createTempDirectory(FileUtils.DATA_TEMP_DIR_PREFIX).toString();
                        // download files
                        for (String downUrl : downloadFileUrls) {
                            HttpDownloader.downloadFile(downUrl, tempDir);
                            URI uri = new URI(downUrl);
                            String[] segments = uri.getPath().split("/");   //$NON-NLS-1$
                            String downFileName = segments[segments.length - 1];
                            File downFile = new File(tempDir + File.separator + downFileName);
                            delFiles.add(downFile);

                            FileInputStream fis = new FileInputStream(downFile);
                            FileDescriptor fd = new FileDescriptor();
                            FileStorageDisk fsDisk = new FileStorageDisk();

                            fsDisk.setFolder(DATA_REPO_FOLDER);
                            try {
                                fd = fsDisk.storeFile(downFileName, fis);
                                fd.setFilename(downFileName);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            dataset.addFileDescriptor(fd);
                        }
                        FileUtils.deleteTmpDir(delFiles);
                    }
                    dataset = repository.addDataset(dataset);
                    datasets.add(dataset);
                    datasetIds = new ArrayList<String>();
                    datasetIds.add(dataset.getId());
                    Space foundSpace = repository.getSpaceByName(spaceName);
                    if (foundSpace == null) {   // new space: insert the data
                        Space space = new Space();
                        space.setName(spaceName);
                        space.setDatasetIds(datasetIds);
                        repository.addSpace(space);
                    } else {    // the space with space name exists
                        // get dataset ids
                        List<String> dsIdsinSpace = foundSpace.getDatasetIds();
                        for (String dsIdInSpace : dsIdsinSpace) {
                            datasetIds.add(dsIdInSpace);
                        }
                        foundSpace.setDatasetIds(datasetIds);
                        repository.addSpace(foundSpace);
                    }
                    System.out.println(dataset);
                    System.out.println(dataset.getFileDescriptors());
                }
            }
        }

        if (datasets == null) {
            throw new NotFoundException("There is no Space in the repository.");    //$NON-NLS-1$
        }

        return datasets;
    }

    // http://localhost:8080/data/api/datasets/dump
    /**
     * Dump metadata files in earthquake server to database creating MvzDataset
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    @GET
    @Path("/dump/metadata")
    @Produces(MediaType.APPLICATION_JSON)
    public List<MvzDataset> dumpMetadataFromWebdab() throws IOException, URISyntaxException {
        List<String> resHref = FileUtils.getDirectoryContent(FileUtils.REPO_PROP_URL, "");
        List<MvzDataset> mvzDatasets = new ArrayList<>();

        for (String tmpUrl : resHref) {
            String metaDirUrl = FileUtils.REPO_PROP_URL + tmpUrl;
            List<String> metaHref = FileUtils.getDirectoryContent(metaDirUrl, "");
            for (String metaFileName : metaHref) {
                String fileExtStr = FilenameUtils.getExtension(metaFileName);
                // get only the mvz file
                if (fileExtStr.equals(FileUtils.EXTENSION_META)) {
                    String combinedId = tmpUrl + "/" + metaFileName;    //$NON-NLS-1$
                    MvzDataset mvzDataset = new MvzDataset();
                    try {
                        File metadata = FileUtils.loadMetadataFromRepository(combinedId);
                        String fileName = metadata.getName();
                        FileInputStream fis = new FileInputStream(metadata);
                        FileDescriptor fd = new FileDescriptor();
                        FileStorageDisk fsDisk = new FileStorageDisk();

                        fsDisk.setFolder(DATA_REPO_FOLDER);
                        try {
                            fd = fsDisk.storeFile(fileName, fis);
                            fd.setFilename(fileName);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mvzDataset = MvzLoader.setMvzDatasetFromMetadata(metadata, combinedId);
                        mvzDataset.addFileDescriptor(fd);

                    } catch (IOException e) {
                        e.printStackTrace();
                        String err = "{\"error:\" + \"" + e.getLocalizedMessage() + "\"}";  //$NON-NLS-1$
                    }
//                    mvzDataset = MvzLoader.createMvzDatasetFromMetadata(combinedId);
                    mvzDataset = repository.addMvzDataset(mvzDataset);
                    mvzDatasets.add(mvzDataset);
                }
            }
        }

        if (mvzDatasets == null) {
            throw new NotFoundException("There is no Space in the repository.");    //$NON-NLS-1$
        }
        return mvzDatasets;
    }
}

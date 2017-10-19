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

import edu.illinois.ncsa.incore.service.data.utils.FileUtils;
import edu.illinois.ncsa.incore.service.data.utils.JsonUtils;
import edu.illinois.ncsa.incore.service.data.dao.IRepository;
import edu.illinois.ncsa.incore.service.data.geotools.GeotoolsUtils;
import edu.illinois.ncsa.incore.service.data.model.Space;
import edu.illinois.ncsa.incore.service.data.model.datawolf.domain.Dataset;
import edu.illinois.ncsa.incore.service.data.model.datawolf.domain.FileDescriptor;
import edu.illinois.ncsa.incore.service.data.model.datawolf.domain.impl.FileStorageDisk;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ywkim on 7/26/2017.
 */

@Path("datasets")
public class DataController {
    private static final String DATA_REPO_FOLDER = "C:\\Users\\ywkim\\Downloads\\Rest"; //$NON-NLS-1$
    private static final String POST_PARAMENTER_NAME = "name";  //$NON-NLS-1$
    private static final String POST_PARAMENTER_FILE = "file";  //$NON-NLS-1$
    private static final String POST_PARAMENTER_META = "parentdataset";  //$NON-NLS-1$
    private static final String POST_PARAMETER_DATASET_ID = "datasetId";    //$NON-NLS-1$
    private static final String UPDATE_OBJECT_NAME = "property name";  //$NON-NLS-1$
    private static final String UPDATE_OBJECT_VALUE = "property value";  //$NON-NLS-1$
    private static final String FILE_ZIP_EXTENSION = "zip"; //$NON-NLS-1$
    private static final String FILE_SHAPFILE_NAME = "shapefile";   //$NON-NLS-1$

    @Inject
    private IRepository repository;

    private Logger logger = Logger.getLogger(DataController.class);

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
    public Dataset getDatasetFromRepo(@PathParam("id") String datasetId) {
        Dataset dataset = repository.getDatasetById(datasetId);
        if (dataset == null) {
            throw new NotFoundException("There is no Dataset with given id in the repository.");
        }

        return dataset;
    }

    //http://localhost:8080/data/api/datasets/list-datasets
    /**
     * Returns a list of spaces in the Space collection
     *
     * @return list of dataset
     */
    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Dataset> getDatasetList() {
        List<Dataset> datasets = repository.getAllDatasets();
        if (datasets == null) {
            throw new NotFoundException("There is no Dataset in the repository.");
        }

        return datasets;
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
        List<Space> spaces = repository.getAllSpaces();
        if (spaces == null) {
            throw new NotFoundException("There is no Space in the repository.");
        }
        return spaces;
    }

    //http://localhost:8080/data/api/datasets/59e5098168f47426547409f3/files
    /**
     * Returns a zip file that contains all the files attached to a dataset specified by {id} using FileDescriptor in the dataset
     *
     * @param datasetId id of the Dataset in mongodb
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    @GET
    @Path("/{id}/files")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFileByDataset(@PathParam("id") String datasetId) throws IOException, URISyntaxException {
        Dataset dataset = repository.getDatasetById(datasetId);
        if (dataset == null) {
            throw new NotFoundException("There is no Dataset with given id in the repository.");
        }
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
            for (FileDescriptor fd : fds) {
                String dataUrl = fd.getDataURL();
                fileList.add(new File(new URI(dataUrl)));
                fileNameList.add(FilenameUtils.getName(dataUrl));
            }

            // create temp dir and copy files to temp dir
            String tempDir = Files.createTempDirectory(FileUtils.DATA_TEMP_DIR_PREFIX).toString();
            // copiedFileList below is not used but the method is needed to copy files
            List<File> copieFileList = GeotoolsUtils.copyFilesToTmpDir(fileList, tempDir);

            outFile = FileUtils.createZipFile(fileNameList, tempDir, fileBaseName);
            fileName = fileBaseName + "." + FILE_ZIP_EXTENSION;
        }

        if (outFile != null) {
            return Response.ok(outFile, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition", "attachment; filename=\"" + fileName + "\"").build();
        } else {
            return Response.status(404).build();
        }
    }

    //http://localhost:8080/data/api/datasets/59e5098168f47426547409f3/filedescriptors/59e50a2568f4742654e59629/files
    /**
     * Returns a file that is attached to a FileDescriptor specified by {fdid} in a dataset specified by {id}
     *
     * @param datasetId Dataset id
     * @param inFdId    FileDescriptor id in the Dataset
     * @return
     * @throws URISyntaxException
     */
    @GET
    @Path("/{id}/filedescriptors/{fdid}/files")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFileByFileDescriptor(@PathParam("id") String datasetId, @PathParam("fdid") String inFdId) throws URISyntaxException {
        File outFile = null;
        Dataset dataset = repository.getDatasetById(datasetId);
        if (dataset == null) {
            throw new NotFoundException("There is no Dataset with given id in the repository.");
        }
        List<FileDescriptor> fds = dataset.getFileDescriptors();
        String dataUrl = ""; //$NON-NLS-1$
        String fdId = "";   //$NON-NLS-1$
        String fileName = "";   //$NON-NLS-1$

        for (FileDescriptor fd : fds) {
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

        if (outFile != null) {
            return Response.ok(outFile, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition", "attachment; filename=\"" + fileName + "\"").build();
        } else {
            return Response.status(404).build();
        }
    }

    //http://localhost:8080/data/api/datasets/joinshptable/59e509ca68f4742654e59621

    /**
     * Returns a zip file of shapefile after joinig analysis result table dataset specified by {id} using result dataset's source dataset shapefile
     *
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
            throw new NotFoundException("There is no Dataset with given id in the repository.");
        }
        List<FileDescriptor> csvFDs = dataset.getFileDescriptors();
        File csvFile = null;
        String outFileName = "";    //$NON-NLS-1$
        for (int i = 0; i < csvFDs.size(); i++) {
            FileDescriptor csvFd = csvFDs.get(i);
            String csvLoc = csvFd.getDataURL();
            csvFile = new File(new URI(csvLoc));
        }

        Dataset sourceDataset = repository.getDatasetById(dataset.getSourceDataset());
        if (sourceDataset == null) {
            throw new NotFoundException("There is no Dataset with given id in the repository.");
        }
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
                if (fileExt.equalsIgnoreCase(FileUtils.EXTENSION_SHP)) {
                    isShpfile = true;
                }
            }
        }
        if (isShpfile) {
            zipFile = GeotoolsUtils.JoinTableShapefile(shpfiles, csvFile);
            outFileName = FilenameUtils.getBaseName(zipFile.getName()) + "." + FILE_ZIP_EXTENSION;
        }

        if (zipFile != null) {
            return Response.ok(zipFile, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition", "attachment; filename=\"" + outFileName + "\"").build();
        } else {
            return Response.status(404).build();
        }
    }

    // http//localhost:8080/data/api/datasets/ingest-dataset
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

    //  http//localhost:8080/data/api/datasets/ingest-multi-files
    //    {datasetId: "59e5046668f47426549b606e"}
    /**
     * upload file(s) to attach to a dataset by FileDescriptor
     *
     * @param inputs post paraemters including file and metadata
     * @return
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/upload-files")
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

    // {datasetId: "59e0ec0c68f4742a340411d2", property name: "sourceDataset", property value: "59e0eb7d68f4742a342d9738"}
    /**
     * file(s) to upload to attach to a dataset by FileDescriptor
     *
     * @param inDatasetJson
     * @return
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/update")
    public Object updateObject(@FormDataParam("update") String inDatasetJson) {
        boolean isJsonValid = JsonUtils.isJSONValid(inDatasetJson);
        Dataset dataset = null;

        if (isJsonValid) {
            String datasetId = JsonUtils.extractValueFromJsonString(POST_PARAMETER_DATASET_ID, inDatasetJson);
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
}

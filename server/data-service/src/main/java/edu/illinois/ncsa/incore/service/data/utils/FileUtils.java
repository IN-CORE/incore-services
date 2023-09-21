/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 ********************************************************************************/
package edu.illinois.ncsa.incore.service.data.utils;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.service.data.dao.HttpDownloader;
import edu.illinois.ncsa.incore.service.data.dao.IRepository;
import edu.illinois.ncsa.incore.service.data.models.Dataset;
import edu.illinois.ncsa.incore.service.data.models.FileDescriptor;
import edu.illinois.ncsa.incore.service.data.models.MvzLoader;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import jakarta.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by ywkim on 6/8/2017.
 */
public class FileUtils {
    public static final String REPO_SERVER_URL = System.getenv("DATA_REPO_WEBDAV_SERVER_URL");
    public static final String REPO_PROP_DIR = System.getenv("DATA_REPO_WEBDAV_PROP_DIR");
    public static final String REPO_DS_DIR = System.getenv("DATA_REPO_WEBDAV_DS_DIR");
    public static final String REPO_PROP_URL = REPO_SERVER_URL + REPO_PROP_DIR;
    public static final String REPO_DS_URL = REPO_SERVER_URL + REPO_DS_DIR;
    public static final String DATA_TEMP_DIR_PREFIX = "data_repo_";
    public static final String[] EXTENSIONS_SHAPEFILES = new String[]{"dbf", "prj", "shp", "shx"};
    public static final String EXTENSION_SHP = "shp";
    public static final String EXTENSION_META = "mvz";
    public static final String EXTENSION_CSV = "csv";
    public static final String EXTENSION_ZIP = "zip";
    public static final String EXTENSION_PRJ = "prj";
    public static final String EXTENSION_GEOPACKAGE = "gpkg"; // file extension of geopackage
    public static final int INDENT_SPACE = 4;
    public static final int TYPE_NUMBER_SHP = 1;
    public static final int TYPE_NUMBER_CSV = 2;
    public static final int TYPE_NUMBER_META = 3;
    public static final int TYPE_NUMBER_MULTI = 10;
    public static final String DATASET_TITLE = "title";
    public static final String DATASET_TYPE = "dataType";
    public static final String DATASET_SOURCE_DATASET = "sourceDataset";
    public static final String DATASET_FORMAT = "format";
    public static final String DATASET_DESCRIPTION = "description";
    public static final String DATASET_FILE_NAME = "fileName";
    public static final String FORMAT_SHAPEFILE = "shapefile";
    public static final String FORMAT_NETWORK = "shp-network";
    public static final String NETWORK_COMPONENT = "networkDataset";
    public static final String NETWORK_LINK = "link";
    public static final String NETWORK_NODE = "node";
    public static final String NETWORK_GRAPH = "graph";
    public static final Logger logger = Logger.getLogger(FileUtils.class);
    private static final String DATA_REPO_FOLDER = System.getenv("DATA_REPO_DATA_DIR");

    /**
     * delete temporary directory
     *
     * @param inFile
     */
    public static void deleteTmpDir(File inFile) {
        //remove temp dir
        String tempDir = inFile.getParent();
        File dirFile = new File(tempDir);
        ArrayList<File> files = new ArrayList<File>(Arrays.asList(dirFile.listFiles()));
        deleteTmpDir(files);
    }

    /**
     * delete temporary directory created for temporary file processing
     *
     * @param metadataFile
     * @param fileExt
     */
    public static void deleteTmpDir(File metadataFile, String fileExt) {
        String fileName = metadataFile.getAbsolutePath();
        String filePath = fileName.substring(0, fileName.lastIndexOf(File.separator));
        int extLoc = metadataFile.getName().indexOf(".");
        String extName = metadataFile.getName().substring(extLoc);
        String fileNameWithNoExt = FilenameUtils.removeExtension(fileName);

        String delFileName = fileNameWithNoExt + "." + fileExt;
        File delFile = new File(delFileName);
        deleteFiles(delFile, delFileName);

        File delDir = new File(filePath);
        deleteFiles(delDir, filePath);
    }

    /**
     * delete temporary directory created for temporary file processing
     *
     * @param delFiles
     */
    public static void deleteTmpDir(List<File> delFiles) {
        File delDir = null;
        String filePath = null;
        for (File delFile : delFiles) {
            deleteFiles(delFile);
            delDir = new File(delFile.getParent());
            filePath = delFile.getParent();
        }
        deleteFiles(delDir, filePath);
    }

    /**
     * delete temporary directory created for temporary file processing
     *
     * @param shapefile
     * @param fileExts
     */
    public static void deleteTmpDir(File shapefile, String[] fileExts) {
        String fileName = shapefile.getAbsolutePath();
        String filePath = fileName.substring(0, fileName.lastIndexOf(File.separator));
        int extLoc = shapefile.getName().indexOf(".");
        String extName = shapefile.getName().substring(extLoc);
        String fileNameWithNoExt = FilenameUtils.removeExtension(fileName);

        for (String extension : fileExts) {
            String delFileName = fileNameWithNoExt + "." + extension;
            File delFile = new File(delFileName);
            deleteFiles(delFile, delFileName);
        }
        File delDir = new File(filePath);
        deleteFiles(delDir, filePath);
    }

    /**
     * delete temporary file created for temporary file processing
     *
     * @param delFile
     */
    public static void deleteFiles(File delFile) {
        String delFileName = delFile.getName();
        deleteFiles(delFile, delFileName);
    }

    /**
     * delete temporary files created for temporary file processing
     *
     * @param delFile
     * @param delFileName
     */
    public static void deleteFiles(File delFile, String delFileName) {
        try {
            if (delFile.delete()) {
                logger.debug("file or directory deleted: " + delFileName);
            } else {
                logger.error("file or directory did not deleted: " + delFileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * load file name from the data repository
     *
     * @param inId
     * @param extStr
     * @param repoUrl
     * @return
     * @throws IOException
     */
    public static String loadFileNameFromRepository(String inId, String extStr, String repoUrl) throws IOException {
        String urlPart = inId.replace("$", "/");
        String datasetUrl = repoUrl + urlPart;
        return loadFileNameFromRepository(datasetUrl, extStr);
    }

    /**
     * load file name from the data repository
     *
     * @param datasetUrl
     * @param extStr
     * @return
     * @throws IOException
     */
    public static String loadFileNameFromRepository(String datasetUrl, String extStr) throws IOException {
        List<String> fileList = createFileListFromUrl(datasetUrl);

        String outfileStr = "";
        for (int i = 0; i < fileList.size(); i++) {
            String fileExt = FilenameUtils.getExtension(fileList.get(i));
            if (fileExt.equals(extStr)) {
                outfileStr = fileList.get(i);
            }
        }

        String outfileName = "";
        if (outfileStr.length() > 0) {
            // get the base name of the shapefile
            String[] shapefileNames = outfileStr.split("." + extStr);
            String baseName = shapefileNames[0];
            String tempDir = Files.createTempDirectory(DATA_TEMP_DIR_PREFIX).toString();
            if (extStr.equals(EXTENSION_SHP)) {
                for (String extension : EXTENSIONS_SHAPEFILES) {
                    HttpDownloader.downloadFile(datasetUrl + baseName + "." + extension, tempDir);
                }
            } else {
                HttpDownloader.downloadFile(datasetUrl + baseName + "." + extStr, tempDir);
            }
            outfileName = tempDir + File.separator + baseName + "." + extStr;
        }

        return outfileName;
    }

    /**
     * find type id by using the dataset id
     *
     * @param datasetId
     * @param fileExt
     * @return
     */
    public static String findTypeIdByDatasetId(String datasetId, String fileExt) {
        List<String> typeHref = new LinkedList<String>();
        // if it is a mvz file
        if (fileExt.equals(EXTENSION_META)) {
            typeHref = getDirectoryContent(REPO_PROP_URL, "");
            for (String tmpTypeName : typeHref) {
                List<String> tmpMetaFileList = getDirectoryContent(REPO_PROP_URL + "/" + tmpTypeName, "");
                for (String metaFileName : tmpMetaFileList) {
                    if (FilenameUtils.getBaseName(metaFileName).equals(datasetId)) {
                        return tmpTypeName;
                    }
                }
            }
            // if it is other file
        } else {
            typeHref = getDirectoryContent(REPO_DS_URL, "");
            for (String tmpTypeName : typeHref) {
                String fileDirUrl = REPO_DS_URL + tmpTypeName + "/" + datasetId + "/converted/";
                List<String> fileHref = getDirectoryContent(fileDirUrl, "");
                if (fileHref.size() > 1) {
                    return tmpTypeName;
                }
            }
        }
        return "";
    }

    /**
     * create a list of files in from the url
     *
     * @param inUrl
     * @return
     */
    public static List<String> createFileListFromUrl(String inUrl) {
        String realUrl = getRealUrl(inUrl);
        List<String> linkList = getDirList(realUrl);

        return linkList;
    }

    /**
     * create a list of directory content from given url
     *
     * @param inUrl
     * @return
     */
    public static List<String> getDirList(String inUrl) {
        List<String> linkList = new LinkedList<String>();
        org.jsoup.nodes.Document doc = null;
        try {
            doc = Jsoup.connect(inUrl).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Elements links = doc.select("a");
        String linkAtr = "";

        for (int i = 0; i < links.size(); i++) {
            linkAtr = links.get(i).attr("href");
            if (linkAtr.length() > 3) {
                linkList.add(linkAtr);
            }
        }

        return linkList;
    }

    /**
     * create a url of the webdav that embed the folder 'converted' in the correct position
     *
     * @param inUrl
     * @return
     */
    public static String getRealUrl(String inUrl) {
        String[] strs = inUrl.split("/converted/");
        String urlPrefix = strs[0];
        String realUrl = urlPrefix + "/converted/";

        return realUrl;
    }

    /**
     * check what kind of file format is in the repository web site.
     *
     * @param inId
     * @param repoUrl
     * @return
     */
    public static int checkDataFormatFromRepository(String inId, String repoUrl) {
        int typeNumber = 0;    // 1: shp, 2: csv, 3: mvz
        boolean isMultiType = false;

        String urlPart = inId.replace("$", "/");
        String datasetUrl = repoUrl + urlPart;
        List<String> fileList = createFileListFromUrl(datasetUrl);

        for (int i = 0; i < fileList.size(); i++) {
            String fileExt = FilenameUtils.getExtension(fileList.get(i));
            if (fileExt.equals(EXTENSION_SHP)) {
                if (typeNumber > 0) {
                    isMultiType = true;
                }
                typeNumber = TYPE_NUMBER_SHP;
            }
            if (fileExt.equals(EXTENSION_CSV)) {
                if (typeNumber > 0) {
                    isMultiType = true;
                }
                typeNumber = TYPE_NUMBER_CSV;
            }
            if (fileExt.equals(EXTENSION_META)) {
                if (typeNumber > 0) {
                    isMultiType = true;
                }
                typeNumber = TYPE_NUMBER_META;
            }
        }

        // if there are multiple type of file extensions return high number
        if (isMultiType) {
            typeNumber = TYPE_NUMBER_MULTI;
        }
        return typeNumber;
    }

    /**
     * create an html of the directory list
     *
     * @return
     */
    public static String loadDirectoryList() {
        String outHtml = createListHtml("");
        return outHtml;
    }

    /**
     * create directory list from the dataset id
     *
     * @param inId
     * @return
     */
    public static String loadDirectoryList(String inId) {
        String outHtml = createListHtml(inId);
        return outHtml;
    }

    /**
     * create a html of the directory list
     *
     * @param inId
     * @return
     */
    private static String createListHtml(String inId) {
        String outHtml = "<HTML><BODY>";
        String tmpRepoUrl = "";
        if (inId.length() > 0) {
            tmpRepoUrl = REPO_DS_URL + inId;
        } else {
            tmpRepoUrl = REPO_PROP_URL;
        }

        List<String> resHref = getDirectoryContent(tmpRepoUrl, inId);

        for (String tmpUrl : resHref) {
            // get only the last elemente after back slashes
            if (inId.length() > 0) {
                String[] linkUrls = tmpUrl.split("/");
                outHtml = outHtml + "<a href =\"" + tmpUrl + "\">" + linkUrls[linkUrls.length - 1] + "</a>";
            } else {
                outHtml = outHtml + "<a href =\"" + tmpUrl + "\">" + tmpUrl + "</a>";
            }
            outHtml = outHtml + "</BR>";
        }

        outHtml = outHtml + "</BODY></HTML>";
        return outHtml;
    }

    /**
     * create a list of directory content from gibven url and dataset id
     *
     * @param inUrl
     * @param inId
     * @return
     */
    public static List<String> getDirectoryContent(String inUrl, String inId) {
        List<String> outList = new LinkedList<String>();
        Sardine sardine = SardineFactory.begin();
        try {
            List<DavResource> resources = sardine.list(inUrl);

            for (DavResource res : resources) {
                String[] tmpUrls = res.getHref().toString().split("/");
                String tmpUrl = "";
                if (inId.length() > 0) {
                    tmpUrl = tmpUrls[tmpUrls.length - 2] + "/" + tmpUrls[tmpUrls.length - 1];
                } else {
                    tmpUrl = tmpUrls[tmpUrls.length - 1];
                }

                if (!tmpUrls[tmpUrls.length - 1].equals(inId)) {
                    outList.add(tmpUrl);
                }
            }
            Collections.sort(outList, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outList;
    }

    /**
     * load file from the data repository service using dataset id
     *
     * @param dataset
     * @param repository
     * @param isGeoserver
     * @param inExt
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public static File loadFileFromService(Dataset dataset, IRepository repository, boolean isGeoserver, String inExt) throws IOException
        , URISyntaxException {
        String datasetId = dataset.getId();
        List<FileDescriptor> fds = dataset.getFileDescriptors();
        List<File> fileList = new ArrayList<File>();
        String fileBaseName = "";
        File outFile = null;

        if (fds.size() > 0) {
            File tmpFile = new File(FilenameUtils.concat(DATA_REPO_FOLDER, fds.get(0).getDataURL()));
            fileBaseName = FilenameUtils.getBaseName(tmpFile.getName());

            List<String> fileNameList = new LinkedList<String>();
            for (FileDescriptor fd : fds) {
                // do not put the mvz file
                String dataUrl = FilenameUtils.concat(DATA_REPO_FOLDER, fd.getDataURL());
                String ext = FilenameUtils.getExtension(dataUrl);
                if (!ext.equalsIgnoreCase(EXTENSION_META)) {
                    fileList.add(new File(dataUrl));
                    fileNameList.add(FilenameUtils.getName(dataUrl));
                }
            }

            // create temp dir and copy files to temp dir
            String tempDir = Files.createTempDirectory(DATA_TEMP_DIR_PREFIX).toString();
            // copiedFileList below is not used but the method is needed to copy files
            List<File> copieFileList = null;
            copieFileList = GeotoolsUtils.performCopyFiles(fileList, tempDir, datasetId, isGeoserver, inExt);

            if (isGeoserver) {
                // this is basically a renaming of the files to have dataset id as their name
                if (inExt.equalsIgnoreCase("tif")) {
                    for (File file : copieFileList) {
                        String fileExt = FilenameUtils.getExtension(file.getName());
                        if (fileExt.equalsIgnoreCase(inExt)) {
                            String newFileName = file.getParent() + File.separator + datasetId + ".tif";
                            File newfile = new File(newFileName);
                            file.renameTo(newfile);
                            outFile = file;
                        }
                    }
                } else if (inExt.equalsIgnoreCase("asc")) {
                    for (File file : copieFileList) {
                        String fileExt = FilenameUtils.getExtension(file.getName());
                        if (fileExt.equalsIgnoreCase(inExt)) {
                            String newFileName = file.getParent() + File.separator + datasetId + ".asc";
                            File newfile = new File(newFileName);
                            file.renameTo(newfile);
                            outFile = file;
                        }
                    }
                } else {
                    fileBaseName = datasetId;
                    fileNameList = new LinkedList<String>();
                    for (File file : copieFileList) {
                        fileNameList.add(file.getName());
                    }
                    outFile = FileUtils.createZipFile(fileNameList, tempDir, fileBaseName);
                }
            } else {
                outFile = FileUtils.createZipFile(fileNameList, tempDir, fileBaseName);
            }
        }
        return outFile;
    }

    public static File[] loadNetworkFileFromService(Dataset dataset, IRepository repository, boolean isGeoserver, String inExt) throws IOException, URISyntaxException {
        File[] outFiles = new File[2];
        String datasetId = dataset.getId();
        List<FileDescriptor> fds = dataset.getFileDescriptors();
        List<File> fileList = new ArrayList<File>();
        List<File> linkFileList = new ArrayList<File>();
        List<File> nodeFileList = new ArrayList<File>();
        String fileBaseName = "";
        String linkFileBaseName = "";
        String nodeFileBaseName = "";
        File outFile = null;

        if (fds.size() > 0) {
            File tmpFile = new File(FilenameUtils.concat(DATA_REPO_FOLDER, fds.get(0).getDataURL()));
            fileBaseName = FilenameUtils.getBaseName(tmpFile.getName());

            List<String> fileNameList = new LinkedList<String>();
            List<String> linkFileNameList = new LinkedList<String>();
            List<String> nodeFileNameList = new LinkedList<String>();

            for (FileDescriptor fd : fds) {
                // do not put the mvz file
                String dataUrl = FilenameUtils.concat(DATA_REPO_FOLDER, fd.getDataURL());
                String ext = FilenameUtils.getExtension(dataUrl);
                if (!ext.equalsIgnoreCase(EXTENSION_META)) {
                    fileList.add(new File(dataUrl));
                    fileNameList.add(FilenameUtils.getName(dataUrl));
                }
            }

            // create temp dir and copy files to temp dir
            String tempDir = Files.createTempDirectory(DATA_TEMP_DIR_PREFIX).toString();
            // copiedFileList below is not used but the method is needed to copy files
            List<File> copieFileList = null;
            copieFileList = GeotoolsUtils.performCopyNetworkFiles(dataset, fileList, tempDir, datasetId, isGeoserver, inExt);

            String linkName = FilenameUtils.removeExtension(dataset.getNetworkDataset().getLink().getFileName());
            String nodeName = FilenameUtils.removeExtension(dataset.getNetworkDataset().getNode().getFileName());

            if (isGeoserver) {
                // this is basically a renaming of the files to have dataset id as their name
                // create link file output zip file
                linkName = datasetId + "_link";
                nodeName = datasetId + "_node";
            }

            linkFileNameList = new LinkedList<String>();
            nodeFileNameList = new LinkedList<String>();

            for (File file : copieFileList) {
                String tmpName = FilenameUtils.removeExtension(file.getName());
                if (tmpName.equalsIgnoreCase(linkName)) {
                    linkFileNameList.add(file.getName());
                } else if (tmpName.equalsIgnoreCase(nodeName)) {
                    nodeFileNameList.add(file.getName());
                }
            }
            outFiles[0] = FileUtils.createZipFile(linkFileNameList, tempDir, linkName);
            outFiles[1] = FileUtils.createZipFile(nodeFileNameList, tempDir, nodeName);
//            } else {
//                linkFileNameList = new LinkedList<String>();
//                nodeFileNameList = new LinkedList<String>();
//
//                for (File file : copieFileList) {
//                    String tmpName = FilenameUtils.removeExtension(file.getName());
//                    if (tmpName.equalsIgnoreCase(linkName)) {
//                        linkFileNameList.add(file.getName());
//                    } else if (tmpName.equalsIgnoreCase(nodeName)) {
//                        linkFileNameList.add(file.getName());
//                    }
//                }
//                outFiles[0] = FileUtils.createZipFile(linkFileNameList, tempDir, linkName);
//                outFiles[1] = FileUtils.createZipFile(nodeFileNameList, tempDir, nodeName);
//            }
        }
        return outFiles;
    }

    /**
     * change the extension string from the file name string
     *
     * @param inFileName
     * @param inExt
     * @return
     */
    public static String changeFileNameExtension(String inFileName, String inExt) {
        int pos = inFileName.lastIndexOf(".");
        if (pos > 0) {
            inFileName = inFileName.substring(0, pos);
        }
        String outName = inFileName + "." + inExt;

        return outName;
    }

    /**
     * laod metadata from the data repository by using dataset id
     *
     * @param inId
     * @return
     * @throws IOException
     */
    public static File loadMetadataFromRepository(String inId) throws IOException {
        String urlPart = inId.replace("$", "/");
        String[] urlStrs = urlPart.split("/converted/");       // split the url using the folder name "converted"
        String metadataUrl = REPO_PROP_URL + urlStrs[0];
        // what if there is a dot in the basename? avoid use getBasename
        //String baseName = FilenameUtils.getBaseName(metadataUrl);
        String[] baseNameStrs = urlStrs[0].split("/");
        String baseName = baseNameStrs[baseNameStrs.length - 1];
        String tempDir = Files.createTempDirectory(DATA_TEMP_DIR_PREFIX).toString();

        // check if metadataUrl ends with EXTENSION_META that is .mvz
        String tmpEndStr = metadataUrl.substring(metadataUrl.lastIndexOf('.') + 1);
        if (!tmpEndStr.equals(EXTENSION_META)) {
            HttpDownloader.downloadFile(metadataUrl + "." + EXTENSION_META, tempDir);
        } else {
            // remove mvz extension from the basename
            baseNameStrs = baseName.split("." + EXTENSION_META);
            baseName = baseNameStrs[0];
            HttpDownloader.downloadFile(metadataUrl, tempDir);
        }

        String metadataFile = tempDir + File.separator + baseName + "." + EXTENSION_META;

        return new File(metadataFile);
    }

    /**
     * create a zip file of the dataset file content from the data service by using dataset id
     *
     * @param inId
     * @return
     * @throws IOException
     */
    public static File loadZipdataFromRepository(String inId) throws IOException {
        String urlPart = inId.replace("$", "/");
        String fileDatasetUrl = REPO_DS_URL + urlPart;
        String baseName = FilenameUtils.getBaseName(fileDatasetUrl);
        String tempDir = Files.createTempDirectory(DATA_TEMP_DIR_PREFIX).toString();
        String realUrl = getRealUrl(fileDatasetUrl);
        List<String> fileList = createFileListFromUrl(fileDatasetUrl);

        for (int i = 0; i < fileList.size(); i++) {
            HttpDownloader.downloadFile(realUrl + fileList.get(i), tempDir);
        }

        File zipFile = createZipFile(fileList, tempDir, baseName);

        return zipFile;
    }

    /**
     * join csv table and shapefile and create a geopackage file
     *
     * @param dataset
     * @param repository
     * @return
     * @throws Exception
     * @throws URISyntaxException
     * @throws IOException
     */
    public static File joinShpTable(Dataset dataset, IRepository repository, boolean isRename) throws IncoreHTTPException, IOException {
        List<FileDescriptor> csvFDs = dataset.getFileDescriptors();
        File csvFile = null;
        File geoPkgFile = null;

        for (int i = 0; i < csvFDs.size(); i++) {
            FileDescriptor csvFd = csvFDs.get(i);
            String csvLoc = FilenameUtils.concat(DATA_REPO_FOLDER, csvFd.getDataURL());
            csvFile = new File(csvLoc);
        }

        Dataset sourceDataset = repository.getDatasetById(dataset.getSourceDataset());

        // if source dataset doesn't exist, throw exception
        // else perform join
        if (sourceDataset == null) {
            String errorMsg = "There is no source Dataset with given id in the repository: " + dataset.getId();
            logger.error(errorMsg);
            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, errorMsg);
        }

        List<FileDescriptor> sourceFDs = sourceDataset.getFileDescriptors();
        List<File> shpfiles = new ArrayList<File>();
        boolean isShpfile = false;

        // check whether shp file exists or not
        // build list of files for shapefiles
        for (int i = 0; i < sourceFDs.size(); i++) {
            FileDescriptor sfd = sourceFDs.get(i);
            String shpLoc = FilenameUtils.concat(DATA_REPO_FOLDER, sfd.getDataURL());
            File shpFile = new File(shpLoc);
            shpfiles.add(shpFile);
            //get file, if the file is in remote, use http downloader
            String fileExt = FilenameUtils.getExtension(shpLoc);
            if (fileExt.equalsIgnoreCase(FileUtils.EXTENSION_SHP)) {
                isShpfile = true;
            }
        }

        if (!isShpfile) {
            String errorMsg = "The source dataset is not a shapefile: " + dataset.getId();
            logger.error(errorMsg);
            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, errorMsg);

        }

        geoPkgFile = GeotoolsUtils.joinTableShapefile(dataset, shpfiles, csvFile, isRename);

        return geoPkgFile;
    }

    /**
     * craeate a zip file from given file list
     *
     * @param fileList
     * @param tempDir
     * @param baseName
     * @return
     * @throws IOException
     */
    public static File createZipFile(List<String> fileList, String tempDir, String baseName) throws IOException {
        String zipfile = tempDir + File.separator + baseName + ".zip";

        // create zip file
        byte[] buffer = new byte[1024];
        FileOutputStream fileOS = new FileOutputStream(zipfile);
        ZipOutputStream zipOS = new ZipOutputStream(fileOS);
        for (int i = 0; i < fileList.size(); i++) {
            ZipEntry zEntry = new ZipEntry(fileList.get(i));
            zipOS.putNextEntry(zEntry);

            FileInputStream in = new FileInputStream(tempDir + File.separator + fileList.get(i));
            int index;
            while ((index = in.read(buffer)) > 0) {
                zipOS.write(buffer, 0, index);
            }
            in.close();
        }

        zipOS.closeEntry();
        zipOS.close();
        System.out.println("zip file has been created");

        return new File(zipfile);
    }

    /**
     * switch dbf files in the repository
     *
     * @param inFile
     * @param shpfiles
     * @throws IOException
     */
    public static void switchDbfFile(File inFile, List<File> shpfiles) throws IOException {
        String inShpName = FilenameUtils.removeExtension(inFile.getAbsolutePath()) + ".shp";
        String inShxName = FilenameUtils.removeExtension(inFile.getAbsolutePath()) + ".shx";
        String inDbfName = FilenameUtils.removeExtension(inFile.getAbsolutePath()) + ".dbf";
        String inPrjName = FilenameUtils.removeExtension(inFile.getAbsolutePath()) + ".prj";
        File inShpFile = new File(inShpName);
        File inShxFIle = new File(inShxName);
        File inDbfFile = new File(inDbfName);
        File inPrjFile = new File(inPrjName);
        File outShpFile = null;
        File outShxFile = null;
        File outDbfFile = null;
        File outPrjFIle = null;
        for (File tmpFile : shpfiles) {
            String extStr = FilenameUtils.getExtension(tmpFile.getName());
            if (extStr.equalsIgnoreCase("shp")) {
                outShpFile = tmpFile;
                org.apache.commons.io.FileUtils.copyFile(inShpFile, outShpFile);
            }
            if (extStr.equalsIgnoreCase("shx")) {
                outShxFile = tmpFile;
                org.apache.commons.io.FileUtils.copyFile(inShxFIle, outShxFile);
            }
            if (extStr.equalsIgnoreCase("dbf")) {
                outDbfFile = tmpFile;
                org.apache.commons.io.FileUtils.copyFile(inDbfFile, outDbfFile);
            }
            if (extStr.equalsIgnoreCase("prj")) {
                outPrjFIle = tmpFile;
                org.apache.commons.io.FileUtils.copyFile(inPrjFile, outPrjFIle);
            }
        }
    }

    /**
     * get directory list in the root directory and crate one big json file using mvz files located under each directory
     *
     * @return
     */
    private String loadDirectoryListJsonString() {
        String outStr = "[\n";
        String tmpRepoUrl = REPO_PROP_URL;

        List<String> resHref = getDirectoryContent(tmpRepoUrl, "");

        for (String tmpUrl : resHref) {
            String metaDirUrl = REPO_PROP_URL + tmpUrl;
            List<String> metaHref = getDirectoryContent(metaDirUrl, "");
            for (String metaFileName : metaHref) {
                String fileExtStr = FilenameUtils.getExtension(metaFileName);
                // get only the mvz file
                if (fileExtStr.equals(EXTENSION_META)) {
                    String combinedId = tmpUrl + "/" + metaFileName;
                    File metadataFile = null;
                    try {
                        metadataFile = loadMetadataFromRepository(combinedId);
                        String jsonStr = MvzLoader.formatMetadataAsJson(metadataFile, combinedId);
                        outStr = outStr + jsonStr + ",\n";
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        outStr = outStr.substring(0, outStr.length() - 2);
        outStr = outStr + "\n]";

        return outStr;
    }

    // Note that this method does not consider geopkg datasets which store csv files in the data folder but creates a
    // geopkg file on geoserver. Consider renaming this method and/or overloading the method with "format" field so
    // it can also handle geopkg datasets.
    public static boolean fileUseGeoserver(String examinedFile, boolean geoserverEnabled) {
        boolean useGeoserver = false;
        if (geoserverEnabled) {
            String fileExt = FilenameUtils.getExtension(examinedFile);
            if (fileExt.equalsIgnoreCase("shp") || fileExt.equalsIgnoreCase("asc")
                || fileExt.equalsIgnoreCase("tif") || fileExt.equalsIgnoreCase("zip")) {
                useGeoserver = true;
            }
        }

        return useGeoserver;
    }

    public static void removeFilesFromFileDescriptor(List fdList) {

        for (int i = 0; i < fdList.size(); i++) {
            FileDescriptor fd = (FileDescriptor) fdList.get(i);
            String delFileName = FilenameUtils.concat(DATA_REPO_FOLDER, fd.getDataURL());
            Path parent = Paths.get(delFileName).getParent();
            File delFile = new File(delFileName);
            File delParent = new File(parent.toString());
            deleteFiles(delFile);
            deleteFiles(delParent);
        }
    }
}

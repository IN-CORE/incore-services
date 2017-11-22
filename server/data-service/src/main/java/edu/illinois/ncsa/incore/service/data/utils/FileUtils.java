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

package edu.illinois.ncsa.incore.service.data.utils;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import edu.illinois.ncsa.incore.common.config.Config;
import edu.illinois.ncsa.incore.service.data.dao.HttpDownloader;
import edu.illinois.ncsa.incore.service.data.dao.IRepository;
import edu.illinois.ncsa.incore.service.data.geotools.GeotoolsUtils;
import edu.illinois.ncsa.incore.service.data.models.MvzLoader;
import edu.illinois.ncsa.incore.service.data.models.Dataset;
import edu.illinois.ncsa.incore.service.data.models.FileDescriptor;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import javax.ws.rs.NotFoundException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by ywkim on 6/8/2017.
 */
public class FileUtils {
    public static final String REPO_SERVER_URL = Config.getConfigProperties().getProperty("data.repo.webdav.server.url");   //$NON-NLS-1$
    public static final String REPO_PROP_DIR = Config.getConfigProperties().getProperty("data.repo.webdav.prop.dir");   //$NON-NLS-1$
    public static final String REPO_DS_DIR = Config.getConfigProperties().getProperty("data.repo.webdav.ds.dir");   //$NON-NLS-1$//$NON-NLS-1$
    public static final String REPO_PROP_URL = REPO_SERVER_URL + REPO_PROP_DIR;
    public static final String REPO_DS_URL = REPO_SERVER_URL + REPO_DS_DIR;
    public static final String DATA_TEMP_DIR_PREFIX = "data_repo_";    //$NON-NLS-1$
    public static final String[] EXTENSIONS_SHAPEFILES = new String[]{"dbf", "prj", "shp", "shx"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    public static final String EXTENSION_SHP = "shp";   //$NON-NLS-1$
    public static final String EXTENSION_META = "mvz";  //$NON-NLS-1$
    public static final String EXTENSION_CSV = "csv";    //$NON-NLS-1$
    public static final String EXTENSION_XML = "xml";    //$NON-NLS-1$
    public static final String EXTENSION_ASC = "asc";    //$NON-NLS-1$
    public static final int INDENT_SPACE = 4;
    public static final int TYPE_NUMBER_SHP = 1;
    public static final int TYPE_NUMBER_CSV = 2;
    public static final int TYPE_NUMBER_META = 3;
    public static final int TYPE_NUMBER_MULTI = 10;
    public static final String DATASET_TITLE = "title";   //$NON-NLS-1$
    public static final String DATASET_TYPE = "type";   //$NON-NLS-1$
    public static final String DATASET_SOURCE_DATASET = "sourceDataset";    //$NON-NLS-1$
    public static final String DATASET_FORMAT = "format";   //$NON-NLS-1$
    public static final String DATASET_SPACES = "spaces";    //$NON-NLS-1$
    public static final String DATASET_FILE_NAME = "fileName";    //$NON-NLS-1$
    public static final String FILE_ZIP_EXTENSION = "zip"; //$NON-NLS-1$
    public static final String FORMAT_SHAPEFILE = "shapefile";   //$NON-NLS-1$

    public static final Logger logger = Logger.getLogger(FileUtils.class);


    /**
     * delete temporary directory
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
     * @param metadataFile
     * @param fileExt
     */
    public static void deleteTmpDir(File metadataFile, String fileExt) {
        String fileName = metadataFile.getAbsolutePath();
        String filePath = fileName.substring(0, fileName.lastIndexOf(metadataFile.separator));
        int extLoc = metadataFile.getName().indexOf(".");   //$NON-NLS-1$
        String extName = metadataFile.getName().substring(extLoc);
        String fileNameWithNoExt = FilenameUtils.removeExtension(fileName);

        String delFileName = fileNameWithNoExt + "." + fileExt; //$NON-NLS-1$
        File delFile = new File(delFileName);
        deleteFiles(delFile, delFileName);

        File delDir = new File(filePath);
        deleteFiles(delDir, filePath);
    }

    /**
     * delete temporary directory created for temporary file processing
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
     * @param shapefile
     * @param fileExts
     */
    public static void deleteTmpDir(File shapefile, String[] fileExts) {
        String fileName = shapefile.getAbsolutePath();
        String filePath = fileName.substring(0, fileName.lastIndexOf(shapefile.separator));
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
     * @param delFile
     */
    public static void deleteFiles(File delFile) {
        String delFileName = delFile.getName();
        deleteFiles(delFile, delFileName);
    }

    /**
     * delete temporary files created for temporary file processing
     * @param delFile
     * @param delFileName
     */
    public static void deleteFiles(File delFile, String delFileName) {
        try {
            if (delFile.delete()) {
                logger.debug("file or directory deleted: " + delFileName);  //$NON-NLS-1$
            } else {
                logger.error("file or directory did not deleted: " + delFileName);  //$NON-NLS-1$
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * load file name from the data repository
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
     * @param datasetUrl
     * @param extStr
     * @return
     * @throws IOException
     */
    public static String loadFileNameFromRepository(String datasetUrl, String extStr) throws IOException {
        List<String> fileList = createFileListFromUrl(datasetUrl);

        String outfileStr = ""; //$NON-NLS-1$
        for (int i = 0; i < fileList.size(); i++) {
            String fileExt = FilenameUtils.getExtension(fileList.get(i));
            if (fileExt.equals(extStr)) {
                outfileStr = fileList.get(i);
            }
        }

        String outfileName = "";    //$NON-NLS-1$
        if (outfileStr.length() > 0) {
            // get the base name of the shapefile
            String shapefileNames[] = outfileStr.split("." + extStr);
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
                String fileDirUrl = REPO_DS_URL + tmpTypeName + "/" + datasetId + "/converted/";    //$NON-NLS-1$ //$NON-NLS-2$
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
        Elements links = doc.select("a");   //$NON-NLS-1$
        String linkAtr = "";

        for (int i = 0; i < links.size(); i++) {
            linkAtr = links.get(i).attr("href");    //$NON-NLS-1$
            if (linkAtr.length() > 3) {
                linkList.add(linkAtr);
            }
        }

        return linkList;
    }

    /**
     * create a url of the webdav that embed the folder 'converted' in the correct position
     * @param inUrl
     * @return
     */
    public static String getRealUrl(String inUrl) {
        String strs[] = inUrl.split("/converted/");
        String urlPrefix = strs[0];
        String realUrl = urlPrefix + "/converted/"; //$NON-NLS-1$

        return realUrl;
    }

    /**
     * check what kind of file format is in the repository web site.
     * @param inId
     * @param repoUrl
     * @return
     */
    public static int checkDataFormatFromRepository(String inId, String repoUrl) {
        int typeNumber = 0;    // 1: shp, 2: csv, 3: mvz
        boolean isMultiType = false;

        String urlPart = inId.replace("$", "/");    //$NON-NLS-1$ //$NON-NLS-2$
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
     * @return
     */
    public static String loadDirectoryList() {
        String outHtml = createListHtml("");
        return outHtml;
    }

    /**
     * create directory list from the dataset id
     * @param inId
     * @return
     */
    public static String loadDirectoryList(String inId) {
        String outHtml = createListHtml(inId);
        return outHtml;
    }

    /**
     * create a html of the directory list
     * @param inId
     * @return
     */
    private static String createListHtml(String inId) {
        String outHtml = "<HTML><BODY>";    //$NON-NLS-1$
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
                outHtml = outHtml + "<a href =\"" + tmpUrl + "\">" + linkUrls[linkUrls.length - 1] + "</a>";    //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                outHtml = outHtml + "<a href =\"" + tmpUrl + "\">" + tmpUrl + "</a>";   //$NON-NLS-1$ //$NON-NLS-2$
            }
            outHtml = outHtml + "</BR>";    //$NON-NLS-1$
        }

        outHtml = outHtml + "</BODY></HTML>";   //$NON-NLS-1$
        return outHtml;
    }

    /**
     * create a list of directory content from gibven url and dataset id
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
                String[] tmpUrls = res.getHref().toString().split("/"); //$NON-NLS-1$
                String tmpUrl = "";
                if (inId.length() > 0) {
                    tmpUrl = tmpUrls[tmpUrls.length - 2] + "/" + tmpUrls[tmpUrls.length - 1];   //$NON-NLS-1$
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
     * @param datasetId
     * @param repository
     * @param isGeoserver
     * @param inExt
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public static File loadFileFromService(String datasetId, IRepository repository, boolean isGeoserver, String inExt) throws IOException, URISyntaxException {
        Dataset dataset = repository.getDatasetById(datasetId);
        if (dataset == null) {
            throw new NotFoundException("There is no Dataset with given id in the repository.");
        }
        List<FileDescriptor> fds = dataset.getFileDescriptors();
        List<File> fileList = new ArrayList<File>();
        String fileBaseName = "";   //$NON-NLS-1$
        File outFile = null;

        if (fds.size() > 0) {
            File tmpFile = new File(fds.get(0).getDataURL());
            fileBaseName = FilenameUtils.getBaseName(tmpFile.getName());

            List<String> fileNameList = new LinkedList<String>();
            for (FileDescriptor fd : fds) {
                String dataUrl = fd.getDataURL();
                fileList.add(new File(new URI(dataUrl)));
                fileNameList.add(FilenameUtils.getName(dataUrl));
            }

            // create temp dir and copy files to temp dir
            String tempDir = Files.createTempDirectory(DATA_TEMP_DIR_PREFIX).toString();
            // copiedFileList below is not used but the method is needed to copy files
            List<File> copieFileList = GeotoolsUtils.copyFilesToTmpDir(fileList, tempDir, datasetId, isGeoserver, inExt);

            if (isGeoserver) {
                // this is basically a renaming of the files to have dataset id as their name
                if (inExt.equalsIgnoreCase("tif")) {  //$NON-NLS-1$
                    for (File file : copieFileList) {
                        String fileExt = FilenameUtils.getExtension(file.getName());
                        if (fileExt.equalsIgnoreCase(inExt)) {
                            String newFileName = file.getParent() + File.separator + datasetId + ".tif";    //$NON-NLS-1$
                            File newfile = new File(newFileName);
                            file.renameTo(newfile);
                            outFile = file;
                        }
                    }
                } else if (inExt.equalsIgnoreCase("asc")) {  //$NON-NLS-1$
                    for (File file : copieFileList) {
                        String fileExt = FilenameUtils.getExtension(file.getName());
                        if (fileExt.equalsIgnoreCase(inExt)) {
                            String newFileName = file.getParent() + File.separator + datasetId + ".asc";    //$NON-NLS-1$
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

    /**
     * change the extension string from the file name string
     * @param inFileName
     * @param inExt
     * @return
     */
    public static String changeFileNameExtension(String inFileName, String inExt) {
        int pos = inFileName.lastIndexOf("."); //$NON-NLS-1$
        if (pos > 0) {
            inFileName = inFileName.substring(0, pos);
        }
        String outName = inFileName + "." + inExt;   //$NON-NLS-1$

        return outName;
    }

    /**
     * laod metadata from the data repository by using dataset id
     * @param inId
     * @return
     * @throws IOException
     */
    public static File loadMetadataFromRepository(String inId) throws IOException {
        String urlPart = inId.replace("$", "/");    //$NON-NLS-1$ //$NON-NLS-2$
        String[] urlStrs = urlPart.split("/converted/");    //$NON-NLS-1$ // split the url using the folder name "converted"
        String metadataUrl = REPO_PROP_URL + urlStrs[0];
        // what if there is a dot in the basename? avoid use getBasename
        //String baseName = FilenameUtils.getBaseName(metadataUrl);
        String baseNameStrs[] = urlStrs[0].split("/");  //$NON-NLS-1$
        String baseName = baseNameStrs[baseNameStrs.length - 1];
        String tempDir = Files.createTempDirectory(DATA_TEMP_DIR_PREFIX).toString();    //$NON-NLS-1$

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
     * @param inId
     * @return
     * @throws IOException
     */
    public static File loadZipdataFromRepository(String inId) throws IOException {
        String urlPart = inId.replace("$", "/");    //$NON-NLS-1$ //$NON-NLS-2$
        String fileDatasetUrl = REPO_DS_URL + urlPart;
        String baseName = FilenameUtils.getBaseName(fileDatasetUrl);
        String tempDir = Files.createTempDirectory(DATA_TEMP_DIR_PREFIX).toString();    //$NON-NLS-1$
        String realUrl = getRealUrl(fileDatasetUrl);
        List<String> fileList = createFileListFromUrl(fileDatasetUrl);

        for (int i = 0; i < fileList.size(); i++) {
            HttpDownloader.downloadFile(realUrl + fileList.get(i), tempDir);
        }

        File zipFile = createZipFile(fileList, tempDir, baseName);

        return zipFile;
    }

    /**
     * join csv table and shapefile and create a zip file
     * @param dataset
     * @param repository
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public static File joinShpTable(Dataset dataset, IRepository repository, boolean isRename) throws URISyntaxException, IOException {
        List<FileDescriptor> csvFDs = dataset.getFileDescriptors();
        File csvFile = null;

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

        if (!(sourceType.equalsIgnoreCase(FORMAT_SHAPEFILE))) {
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
            zipFile = GeotoolsUtils.joinTableShapefile(dataset, shpfiles, csvFile, isRename);
        }

        return zipFile;
    }

    /**
     * craeate a zip file from given file list
     * @param fileList
     * @param tempDir
     * @param baseName
     * @return
     * @throws IOException
     */
    public static File createZipFile(List<String> fileList, String tempDir, String baseName) throws IOException {
        String zipfile = tempDir + File.separator + baseName + ".zip";  //$NON-NLS-1$

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
        System.out.println("zip file has been created");    //$NON-NLS-1$

        return new File(zipfile);
    }

    /**
     * get directory list in the root directory and crate one big json file using mvz files located under each directory
     * @return
     */
    private String loadDirectoryListJsonString() {
        String outStr = "[\n";  //$NON-NLS-1$
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
        outStr = outStr + "\n]";    //$NON-NLS-1$

        return outStr;
    }
}

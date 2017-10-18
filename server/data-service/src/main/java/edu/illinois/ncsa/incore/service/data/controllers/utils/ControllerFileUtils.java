package edu.illinois.ncsa.incore.service.data.controllers.utils;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.util.JSON;
import edu.illinois.ncsa.incore.service.data.dao.HttpDownloader;
import edu.illinois.ncsa.incore.service.data.model.MvzLoader;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.mongodb.client.model.Sorts.descending;

/**
 * Created by ywkim on 6/8/2017.
 */
public class ControllerFileUtils {
    public static final String REPO_SERVER_URL = "https://earthquake.ncsa.illinois.edu/";   //$NON-NLS-1$
    public static final String REPO_PROP_DIR = "ergo-repo/properties/"; //$NON-NLS-1$
    public static final String REPO_DS_DIR = "ergo-repo/datasets/"; //$NON-NLS-1$
    public static final String REPO_PROP_URL = REPO_SERVER_URL + REPO_PROP_DIR;
    public static final String REPO_DS_URL = REPO_SERVER_URL + REPO_DS_DIR;
    public static final String SERVER_URL_PREFIX = "http://localhost:8080/repo/api/datasets/";  //$NON-NLS-1$
    public static final String DATA_TEMP_DIR_PREFIX = "data_repo_" ;    //$NON-NLS-1$
    public static final String[] EXTENSIONS_SHAPEFILES = new String[]{"dbf", "prj", "shp", "shx"};
    public static final String EXTENSION_SHP = "shp";   //$NON-NLS-1$
    public static final String EXTENSION_META = "mvz";  //$NON-NLS-1$
    public static final String EXTENSION_CSV ="csv";    //$NON-NLS-1$
    public static final int INDENT_SPACE = 4;
    public static final int TYPE_NUMBER_SHP = 1;
    public static final int TYPE_NUMBER_CSV = 2;
    public static final int TYPE_NUMBER_META = 3;
    public static final int TYPE_NUMBER_MULTI = 10;
    public static final String DATASET_TITLE = "title";   //$NON-NLS-1$
    public static final String DATASET_TYPE = "type";   //$NON-NLS-1$
    public static final String DATASET_SOURCE_DATASET = "sourceDataset";    //$NON-NLS-1$
    public static final String DATASET_FORMAT = "format";   //$NON-NLS-1$
    public static final String DATASET_SPACES ="spaces";    //$NON-NLS-1$
    public static final String DATASET_FILE_NAME = "fileName";    //$NON-NLS-1$

    public static final Logger logger = Logger.getLogger(ControllerFileUtils.class);


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

    public static void deleteFiles(File delFile, String delFileName){
        try {
            if (delFile.delete()) {
                logger.debug("file or directory deleted: " + delFileName);  //$NON-NLS-1$
            } else {
                logger.error("file or directory did not deleted: " + delFileName);  //$NON-NLS-1$
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // load shapefile data from repository web site
    public static String loadFileNameFromRepository(String inId, String extStr, String repoUrl) throws IOException {
        String urlPart = inId.replace("$", "/");
        String datasetUrl = repoUrl + urlPart;
        List<String> fileList = createFileListFromUrl(datasetUrl);

        String outfileStr = ""; //$NON-NLS-1$
        for (int i=0; i < fileList.size();i++) {
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
            String tempDir = Files.createTempDirectory(DATA_TEMP_DIR_PREFIX).toString();    //$NON-NLS-1$
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

    public static String findTypeIdByDatasetId(String datasetId, String fileExt) {
        List<String> typeHref = new LinkedList<String>();
        // if it is a mvz file
        if (fileExt.equals(EXTENSION_META)) {
            typeHref = getDirectoryContent(REPO_PROP_URL, "");
            for (String tmpTypeName: typeHref) {
                List<String> tmpMetaFileList = getDirectoryContent(REPO_PROP_URL + "/" + tmpTypeName, "");
                for (String metaFileName: tmpMetaFileList) {
                    if (FilenameUtils.getBaseName(metaFileName).equals(datasetId)) {
                        return tmpTypeName;
                    }
                }
            }
            // if it is other file
        } else {
            typeHref = getDirectoryContent(REPO_DS_URL, "");
            for (String tmpTypeName: typeHref) {
                String fileDirUrl = REPO_DS_URL + tmpTypeName + "/" + datasetId + "/converted/";    //$NON-NLS-1$
                List<String> fileHref = getDirectoryContent(fileDirUrl, "");
                if (fileHref.size() > 1) {
                    return tmpTypeName;
                }
            }
        }
        return "";
    }

    public static List<String> createFileListFromUrl(String inUrl) {
        String realUrl = getRealUrl(inUrl);
        List<String> linkList = getDirList(realUrl);

        return linkList;
    }

    public static List<String> getDirList(String inUrl){
        List<String> linkList = new LinkedList<String>();
        org.jsoup.nodes.Document doc = null;
        try {
            doc = Jsoup.connect(inUrl).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Elements links = doc.select("a");   //$NON-NLS-1$
        String linkAtr = "";

        for (int i=0;i < links.size();i++){
            linkAtr = links.get(i).attr("href");    //$NON-NLS-1$
            if (linkAtr.length() > 3) {
                linkList.add(linkAtr);
            }
        }

        return linkList;
    }

    public static String getRealUrl(String inUrl) {
        String strs[] = inUrl.split("/converted/");
        String urlPrefix = strs[0];
        String realUrl = urlPrefix + "/converted/"; //$NON-NLS-1$

        return realUrl;
    }


    public void insertGeoJsonToMongoTest(String typeId, String datasetId, String inJson, String mongoUrl, String geoDbName) {
        //crete mongodb connection
        MongoClientURI mongoUri = new MongoClientURI(mongoUrl);
        MongoClient mongoClient = new MongoClient(mongoUri);
        MongoDatabase database = mongoClient.getDatabase(geoDbName);

        // check if collection exists, otherwise create one with a type id
        MongoIterable<String> collNames = database.listCollectionNames();
        boolean isCollExist = false;
        for (String collName: collNames) {
            if (collName.equalsIgnoreCase(typeId)) {
                isCollExist = true;
            }
        }

        // create collection
        if (isCollExist) {
            logger.debug("Collection already exists");  //$NON-NLS-1$
        } else {
            database.createCollection(typeId, new CreateCollectionOptions().capped(false));
        }

        MongoCollection<org.bson.Document> geoJsonColl = database.getCollection(typeId);
        BasicDBObject document = (BasicDBObject) JSON.parse(inJson);
        document.put("_id", datasetId); //$NON-NLS-1$
        BasicDBObject query = new BasicDBObject();
        query.put("_id", datasetId);    //$NON-NLS-1$
        FindIterable existDoc = geoJsonColl.find(document);
        //MongoIterable<Document> iterable = geoJsonColl.find({_id: datasetId), {_id:1}).limit(1);
        if (existDoc.first() == null) {
            geoJsonColl.insertOne(new org.bson.Document(query));
        } else {
            logger.debug("Document already exists");    //$NON-NLS-1$
        }

//        org.bson.Document myDoc = geoJsonColl.find(query).first();
//        String user; // the user name
//        String database; // the name of the database in which the user is defined
//        char[] password; // the password as a character array
//        MongoCredential credential = MongoCredential.createCredential(user, database, password);
//        MongoClientOptions options = MongoClientOptions.builder().sslEnabled(true).build();
//        MongoClient mongoClient = new MongoClient(new ServerAddress("host1", 27017), Arrays.asList(credential), options);
    }


    // check what kind of file format is in the repository web site.
    public static int checkDataFormatFromRepository(String inId, String repoUrl) {
        int typeNumber  = 0;    // 1: shp, 2: csv, 3: mvz
        boolean isMultiType = false;

        String urlPart = inId.replace("$", "/");
        String datasetUrl = repoUrl + urlPart;
        List<String> fileList = createFileListFromUrl(datasetUrl);

        for (int i=0; i < fileList.size();i++) {
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

    // get directory list in the root directory and crate one big json file using mvz files located under each directory
    private String loadDirectoryListJsonString(){
        String outStr = "[\n";
        String tmpRepoUrl = REPO_PROP_URL;

        List<String> resHref = getDirectoryContent(tmpRepoUrl, "");

        for (String tmpUrl: resHref) {
            String metaDirUrl = REPO_PROP_URL + tmpUrl;
            List<String> metaHref = getDirectoryContent(metaDirUrl, "");
            for (String metaFileName: metaHref) {
                String fileExtStr = FilenameUtils.getExtension(metaFileName);
                // get only the mvz file
                if (fileExtStr.equals(EXTENSION_META)) {
                    String combinedId = tmpUrl + "/" + metaFileName;
                    File metadataFile = null;
                    try {
                        metadataFile = loadMetadataFromRepository(combinedId);
                        String jsonStr = MvzLoader.formatMetadataAsJson(metadataFile, combinedId, SERVER_URL_PREFIX);
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

    public static String loadDirectoryList() {
        String outHtml = createListHtml("");
        return outHtml;
    }

    public static String loadDirectoryList(String inId) {
        String outHtml = createListHtml(inId);
        return outHtml;
    }

    private static String createListHtml(String inId){

        String outHtml = "<HTML><BODY>";    //$NON-NLS-1$
        String tmpRepoUrl = "";
        if (inId.length() > 0) {
            tmpRepoUrl = REPO_DS_URL + inId;
        } else {
            tmpRepoUrl = REPO_PROP_URL;
        }

        List<String> resHref = getDirectoryContent(tmpRepoUrl, inId);

        for (String tmpUrl: resHref) {
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

    public static List<String> getDirectoryContent(String inUrl, String inId){
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

                if (!tmpUrls[tmpUrls.length -1].equals(inId)) {
                    outList.add(tmpUrl);
                }
            }
            Collections.sort(outList, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outList;
    }

    public static File loadMetadataFromRepository(String inId) throws IOException {
        String urlPart = inId.replace("$", "/");
        String[] urlStrs = urlPart.split("/converted/");    // split the url using the folder name "converted"
        String metadataUrl = REPO_PROP_URL + urlStrs[0];
        // what if there is a dot in the basename? avoid use getBasename
        //String baseName = FilenameUtils.getBaseName(metadataUrl);
        String baseNameStrs[] = urlStrs[0].split("/");
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

    public static File loadZipdataFromRepository(String inId) throws IOException {
        String urlPart = inId.replace("$", "/");
        String fileDatasetUrl = REPO_DS_URL + urlPart;
        String baseName = FilenameUtils.getBaseName(fileDatasetUrl);
        String tempDir = Files.createTempDirectory(DATA_TEMP_DIR_PREFIX).toString();    //$NON-NLS-1$
        String realUrl = getRealUrl(fileDatasetUrl);
        List<String> fileList = createFileListFromUrl(fileDatasetUrl);

        for (int i=0; i < fileList.size();i++) {
            HttpDownloader.downloadFile(realUrl + fileList.get(i), tempDir);
        }

        File zipFile = createZipFile(fileList, tempDir, baseName);

        return zipFile;
    }

    public static File createZipFile(List<String> fileList, String tempDir, String baseName) throws IOException {
        String zipfile = tempDir + File.separator + baseName + ".zip";  //$NON-NLS-1$

        // create zip file
        byte[] buffer = new byte[1024];
        FileOutputStream fileOS = new FileOutputStream(zipfile);
        ZipOutputStream zipOS = new ZipOutputStream(fileOS);
        for (int i=0; i < fileList.size();i++) {
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
}

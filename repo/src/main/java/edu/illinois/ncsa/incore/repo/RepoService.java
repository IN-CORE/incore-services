package edu.illinois.ncsa.incore.repo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.jmx.Agent;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geojson.feature.FeatureJSON;

import java.io.*;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


@Path("")
public class RepoService {
    public static final String REPO_SERVER_URL = "https://earthquake.ncsa.illinois.edu/";
    public static final String REPO_PROP_DIR = "ergo-repo/properties/";
    public static final String REPO_DS_DIR = "ergo-repo/datasets/";
    public static final String REPO_PROP_URL = REPO_SERVER_URL + REPO_PROP_DIR;
    public static final String REPO_DS_URL = REPO_SERVER_URL + REPO_DS_DIR;
    public static final String SERVER_URL_PREFIX = "http://localhost:8080/repo/api/datasets/";
    public static final String[] EXTENSIONS_SHAPEFILE = new String[]{"dbf", "prj", "shp", "shx"};
    public static final String EXTENSION_META = "mvz";
    public static final int INDENT_SPACE = 4;
    public static final String TAG_PROPERTIES_GIS = "gis-dataset-properties";
    public static final String TAG_PROPERTIES_MAP = "mapped-dataset-properties";
    public static final String TAG_PROPERTIES_FILE = "file-dataset-properties";
    public static final String TAG_PROPERTIES_RASTER = "raster-dataset-properties";
    public static final String TAG_PROPERTIES_SCENARIO = "dataset-properties";
    public static final String TAG_LOCATION ="location";
    public static final String TAG_DATASET_ID = "dataset-id";

    @GET
    @Path("/datasets")
    @Produces(MediaType.APPLICATION_JSON)
    // test this with       http://localhost:8080/repo/api/datasets
    public Response getDirectoryListJson(){
        String dirStr = loadDirectoryListJsonString();
//        return(dirStr);
//        return new JsonResultDataset(dirStr);
        return Response.ok(dirStr).status(Response.Status.OK).build();
    }

    @GET
    @Path("/datasets/query")
    @Produces(MediaType.APPLICATION_JSON)
    // test this with
    // http://localhost:8080/repo/api/datasets/query?type=edu.illinois.ncsa.ergo.eq.buildings.schemas.buildingInventoryVer5.v1.0
    public Response getDatasetById(@QueryParam("type") String typeId) {
        String propUrl = REPO_PROP_URL + typeId;
        File metadata = null;

        List<String> resourceUrls = getDirectoryContent(propUrl, typeId);
        String outJsonStr = "[\n";
        String combinedId = "";

        for (String rUrl: resourceUrls) {
            outJsonStr = outJsonStr;

            try {
                metadata = loadMetadataFromRepository(rUrl);
                outJsonStr = outJsonStr + formatMetadataAsJson(metadata, rUrl) +",\n";
            } catch (IOException e) {
                e.printStackTrace();;
                String err = "{\"error:\" + \"" + e.getLocalizedMessage() + "\"}";
//                return (new JsonResultDataset(err));
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        }
        outJsonStr = outJsonStr.substring(0, outJsonStr.length() - 2);
        outJsonStr = outJsonStr + "\n]";

//        return new JsonResultDataset(outJsonStr);
        return Response.ok(outJsonStr).status(Response.Status.OK).build();
    }

    @GET
    @Path("/datasets/list")     // this should be changed later for the appropriate line
    @Produces(MediaType.TEXT_HTML)
    // test this with       http://localhost:8080/repo/api/datasets/list
    public String getDirectoryList() {
        try {
            return (loadDirectoryList());
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error:\" + \"" + e.getLocalizedMessage() + "\"}";
        }
    }

    @GET
    @Path("/datasets/{typeid}/{datasetId}/geojson")
//    @Produces("application/vnd.geo+json")
    @Produces(MediaType.APPLICATION_JSON)
    //http://localhost:8080/repo/api/datasets/edu.illinois.ncsa.ergo.eq.buildings.schemas.buildingInventoryVer5.v1.0/Shelby_County_RES31224702005658/geojson
    public Response getDatasetById(@PathParam("typeid") String typeId , @PathParam("datasetId") String datasetId ) {
        File dataset = null;

        String combinedId = typeId + "/" + datasetId + "/converted/";

        try{
            dataset = loadDataFromRepository(combinedId);
            String outJson = formatDatasetAsGeoJson(dataset);
//            return new JsonResultDataset(outJson).jsonStr;
            return Response.ok(outJson).status(Response.Status.OK).build();
//            return outJson;
        }catch (IOException e) {
            e.printStackTrace();
            String err = "{\"error:\" + \"" + e.getLocalizedMessage() + "\"}";
//            return new JsonResultDataset(err);
            return Response.status(Response.Status.NOT_FOUND).build();
//            return err;
        }
    }

    @GET
    @Path("/datasets/{typeId}")
    @Produces(MediaType.TEXT_HTML)
    public String getDirectoryListWithId(@PathParam("typeId") String typeId) {
        try {
            return (loadDirectoryList(typeId));
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error:\" + \"" + e.getLocalizedMessage() + "\"}";
        }
    }

    @GET
    @Path("/datasets/{typeId}/{datasetId}")
    @Produces(MediaType.APPLICATION_JSON)
//    http://localhost:8080/repo/api/datasets/edu.illinois.ncsa.ergo.eq.buildings.schemas.buildingInventoryVer5.v1.0/Shelby_County_RES31224702005658
    public Response getMetadataById(@PathParam("typeId") String typeId, @PathParam("datasetId") String datasetId) {
        File metadata = null;
        String combinedId = typeId + "/" + datasetId;

        try {
            metadata = loadMetadataFromRepository(combinedId);
            String outJson = formatMetadataAsJson(metadata, combinedId);
//            return new JsonResultDataset(outJson);
            return Response.ok(outJson).status(Response.Status.OK).build();
        } catch (IOException e) {
            e.printStackTrace();;
            String err =  "{\"error:\" + \"" + e.getLocalizedMessage() + "\"}";
//            return new JsonResultDataset(err);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/datasets/{typeId}/{datasetId}/files")
    @Produces(MediaType.TEXT_PLAIN)
    // http://localhost:8080/repo/api/datasets/edu.illinois.ncsa.ergo.eq.buildings.schemas.buildingInventoryVer5.v1.0/Shelby_County_RES31224702005658/files
    public File getShapefileById(@PathParam("typeId") String typeId, @PathParam("datasetId") String datasetId) {
        File dataset = null;

        String combinedId = typeId + "/" + datasetId + "/converted/";
        try{
            dataset = loadZipdataFromRepository(combinedId);
            return dataset;
        }catch (IOException e) {
            e.printStackTrace();
//            return "{\"error:\" + \"" + e.getLocalizedMessage() + "\"}";
            return null;
        }
    }

    private String formatDatasetAsGeoJson(File shapefile) throws IOException {
        //TODO: this should return the data in geoJSON format
        String geoJsonStr;

        shapefile.setReadOnly();

        ShapefileDataStore store = new ShapefileDataStore(shapefile.toURI().toURL());
        SimpleFeatureSource source = store.getFeatureSource();
        SimpleFeatureCollection featureCollection = source.getFeatures();
        FeatureJSON fjson = new FeatureJSON();

        try (StringWriter writer = new StringWriter()) {
            fjson.writeFeatureCollection(featureCollection, writer);
            geoJsonStr = writer.toString();
        }

        RepoUtils.deleteTmpDir(shapefile, EXTENSIONS_SHAPEFILE);

        return geoJsonStr;
    }

    private String formatMetadataAsJson(File metadataFile, String inId) throws IOException {
        // convert from UTF-16 to UTF-8
        String xmlString = "";
        metadataFile.setReadOnly();
        Reader metadataReader = new InputStreamReader(new FileInputStream(metadataFile), "UTF-16");
        char metaCharBuffer[] = new char[2048];
        int len;
        while ((len = metadataReader.read(metaCharBuffer, 0, metaCharBuffer.length)) != -1) {
            xmlString = xmlString + new String(metaCharBuffer, 0, len);
        }
        metadataReader.close();
        RepoUtils.deleteTmpDir(metadataFile, EXTENSION_META);

        // remove metadata file extestion from inId if there is any
        String tmpEndStr = inId.substring(inId.lastIndexOf('.') + 1);
        if (tmpEndStr.equals(EXTENSION_META)) {
            inId = inId.substring(0, inId.length() - 4);
        }

        try {
            JSONObject metaJsonObj = XML.toJSONObject(xmlString);
            JSONObject locObj = null;
            if (metaJsonObj.has(TAG_PROPERTIES_GIS)) {
                locObj = metaJsonObj.getJSONObject(TAG_PROPERTIES_GIS).getJSONObject(TAG_DATASET_ID);
            }
            if (metaJsonObj.has(TAG_PROPERTIES_MAP)) {
                locObj = metaJsonObj.getJSONObject(TAG_PROPERTIES_MAP).getJSONObject(TAG_DATASET_ID);
            }
            if (metaJsonObj.has(TAG_PROPERTIES_FILE)) {
                locObj = metaJsonObj.getJSONObject(TAG_PROPERTIES_FILE).getJSONObject(TAG_DATASET_ID);
            }
            if (metaJsonObj.has(TAG_PROPERTIES_RASTER)) {
                locObj = metaJsonObj.getJSONObject(TAG_PROPERTIES_RASTER).getJSONObject(TAG_DATASET_ID);
            }
            if (metaJsonObj.has(TAG_PROPERTIES_SCENARIO)) {
                locObj = metaJsonObj.getJSONObject(TAG_PROPERTIES_SCENARIO).getJSONObject(TAG_DATASET_ID);
            }

            String newUrl = SERVER_URL_PREFIX + inId + "/files";
            locObj.put(TAG_LOCATION, newUrl);
            String jsonString = metaJsonObj.toString(INDENT_SPACE);
            return jsonString;
        } catch (JSONException e) {
            e.printStackTrace();
            return "{\"error:\" + \"" + e.getLocalizedMessage() + "\"}";
        }
    }

    private File loadDataFromRepository(String inId) throws IOException {
        String urlPart = inId.replace("$", "/");
        String shapefileDatasetUrl = REPO_DS_URL + urlPart;
//        String[] urlStrs = urlPart.split("/converted/");

        List<String> fileList = createFileListFromUrl(shapefileDatasetUrl);
        String shapefileStr = "";
        for (int i=0; i < fileList.size();i++) {
            String fileExt = FilenameUtils.getExtension(fileList.get(i));
            if (fileExt.equals("shp")) {
                shapefileStr = fileList.get(i);
            }
        }
        // get the base name of the shapefile
        String shapefileNames[] = shapefileStr.split(".shp");
        String baseName = shapefileNames[0];
        String tempDir = Files.createTempDirectory("repo_download_").toString();
        for (String extension : EXTENSIONS_SHAPEFILE) {
            HttpDownloader.downloadFile(shapefileDatasetUrl + baseName + "." + extension, tempDir);
        }
        String shapefile = tempDir + File.separator + baseName + ".shp";

        return new File(shapefile);
    }

    private File loadZipdataFromRepository(String inId) throws IOException {
        String urlPart = inId.replace("$", "/");
        String fileDatasetUrl = REPO_DS_URL + urlPart;
        String baseName = FilenameUtils.getBaseName(fileDatasetUrl);
        String tempDir = Files.createTempDirectory("repo_download_").toString();
        String realUrl = getRealUrl(fileDatasetUrl);
        List<String> fileList = createFileListFromUrl(fileDatasetUrl);

        for (int i=0; i < fileList.size();i++) {
            HttpDownloader.downloadFile(realUrl + fileList.get(i), tempDir);
        }

        String zipfile = tempDir + File.separator + baseName + ".zip";

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
        System.out.println("zip file has been created");
        return new File(zipfile);
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
                        String jsonStr = formatMetadataAsJson(metadataFile, combinedId);
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

    private String loadDirectoryList() {
        String outHtml = createListHtml("");
        return outHtml;
    }

    private String loadDirectoryList(String inId) {
        String outHtml = createListHtml(inId);
        return outHtml;
    }

    private String createListHtml(String inId){

        String outHtml = "<HTML><BODY>";
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
                outHtml = outHtml + "<a href =\"" + tmpUrl + "\">" + linkUrls[linkUrls.length - 1] + "</a>";
            } else {
                outHtml = outHtml + "<a href =\"" + tmpUrl + "\">" + tmpUrl + "</a>";
            }
            outHtml = outHtml + "</BR>";
        }

        outHtml = outHtml + "</BODY></HTML>";
        return outHtml;
    }

    private List<String> getDirectoryContent(String inUrl, String inId){
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

    private List<String> createFileListFromUrl(String inUrl) {
        String realUrl = getRealUrl(inUrl);
        List<String> linkList = getDirList(realUrl);

//        URL dirUrl = null;
//        URLConnection dirUC = null;
//        String inLine = "";
//        String outLine = "";
//        try {
//            dirUrl = new URL(REPO_URL_PREFIX + "properties");
//            dirUC = dirUrl.openConnection();
//            InputStreamReader isr = new InputStreamReader(dirUC.getInputStream());
//            BufferedReader buffReader = new BufferedReader(isr);
//
//            while((inLine = buffReader.readLine()) != null){
//                linkList.add(inLine);
//                outLine = outLine + inLine;
//            }
//
//            Document doc = Jsoup.parse(outLine);
//            Document doc = Jsoup.connect(String.valueOf(dirUrl)).get();
//            Elements links = doc.select("a");
//            String linkAtr = "";
//
//            for (int i=0;i < links.size();i++){
//                linkList.add(links.get(i).attr("href"));
//            }
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        return linkList;
    }

    private List<String> getDirList(String inUrl){
        List<String> linkList = new LinkedList<String> ();
        Document doc = null;
        try {
            doc = Jsoup.connect(inUrl).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Elements links = doc.select("a");
        String linkAtr = "";

        for (int i=0;i < links.size();i++){
            linkAtr = links.get(i).attr("href");
            if (linkAtr.length() > 3) {
                linkList.add(linkAtr);
            }
        }

        return linkList;
    }

    private String getRealUrl(String inUrl) {
        String strs[] = inUrl.split("/converted/");
        String urlPrefix = strs[0];
        String realUrl = urlPrefix + "/converted/";

        return realUrl;
    }

    private File loadMetadataFromRepository(String inId) throws IOException {
        String urlPart = inId.replace("$", "/");
        String[] urlStrs = urlPart.split("/converted/");    // split the url using the folder name "converted"
        String metadataUrl = REPO_PROP_URL + urlStrs[0];
        // what if there is a dot in the basename? avoid use getBasename
//        String baseName = FilenameUtils.getBaseName(metadataUrl);
        String baseNameStrs[] = urlStrs[0].split("/");
        String baseName = baseNameStrs[baseNameStrs.length - 1];
        String tempDir = Files.createTempDirectory("repo_download_").toString();

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

}
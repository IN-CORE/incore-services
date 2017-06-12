package edu.illinois.ncsa.incore.repo;
import com.sun.net.httpserver.HttpServer;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import org.apache.commons.io.FilenameUtils;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geojson.feature.FeatureJSON;

import java.io.*;
import java.nio.file.Files;

import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

@Path("/datasets")
public class RepoService {
    public static final String[] EXTENSIONS_TO_GRAB = new String[]{"dbf", "prj", "shp", "shx"};
    public static final String EXTENSION_META = "mvz";
    public static final int INDENT_SPACE = 4;

    // The Java method will process HTTP GET requests like the following:
    //http://localhost:8080/repo/api/datasets/edu.illinois.ncsa.ergo.eq.buildings.schemas.buildingInventoryVer5.v1.0$Shelby_County_RES31224702005658$converted$all_bldgs_ver5_WGS1984
    @GET
    @Path("{datasetId}/files")
    @Produces(MediaType.APPLICATION_JSON)

    public String getDatasetById(@PathParam("datasetId") String id ) {
        File dataset = null;

        try{
            dataset = loadDataFromRepository(id);
            return formatDatasetAsGeoJson(dataset);
        }catch (IOException e) {
            e.printStackTrace();
            return "{\"error:\" + \"" + e.getLocalizedMessage() + "\"}";
        }
    }

    @GET
    @Path("{datasetId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getMetadataById(@PathParam("datasetId") String id) {
        File metadata = null;

        try {
            metadata = loadMetadataFromRepository(id);
            return(formatMetadataAsJson(metadata));
        } catch (IOException e) {
            e.printStackTrace();;
            return "{\"error:\" + \"" + e.getLocalizedMessage() + "\"}";
        }
    }

    private String formatDatasetAsGeoJson(File shapefile) throws IOException {
        //TODO: this should return the data in geoJSON format
        String geoJson;

        shapefile.setReadOnly();

        ShapefileDataStore store = new ShapefileDataStore(shapefile.toURI().toURL());
        SimpleFeatureSource source = store.getFeatureSource();
        SimpleFeatureCollection featureCollection = source.getFeatures();
        FeatureJSON fjson = new FeatureJSON();

        try (StringWriter writer = new StringWriter()) {
            fjson.writeFeatureCollection(featureCollection, writer);
            geoJson = writer.toString();
        }

        RepoUtils.deleteTmpDir(shapefile, EXTENSIONS_TO_GRAB);

        return geoJson;
    }

    private String formatMetadataAsJson(File metadataFile) throws IOException {
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

        try {
            JSONObject metaJsonObj = XML.toJSONObject(xmlString);
            String jsonString = metaJsonObj.toString(INDENT_SPACE);
            return jsonString;
        } catch (JSONException e) {
            e.printStackTrace();
            return "{\"error:\" + \"" + e.getLocalizedMessage() + "\"}";
        }
    }

    private File loadDataFromRepository(String id) throws IOException {
        String urlPart = id.replace("$", "/");
        String shapefileDatasetUrl = "https://earthquake.ncsa.illinois.edu/ergo-repo/datasets/" + urlPart;
        String baseName = FilenameUtils.getBaseName(shapefileDatasetUrl);

        String tempDir = Files.createTempDirectory("repo_download_").toString();
        for (String extension : EXTENSIONS_TO_GRAB) {
            HttpDownloader.downloadFile(shapefileDatasetUrl + "." + extension, tempDir);
        }
        //ok, now the files should be here with the shapefile
        String shapefile = tempDir + File.separator + baseName + ".shp";

        return new File(shapefile);
    }

    private File loadMetadataFromRepository(String id) throws IOException {
        String urlPart = id.replace("$", "/");
        String[] urlStrs = urlPart.split("/converted/");    // split the url using the folder name "converted"
        String metadataUrl = "https://earthquake.ncsa.illinois.edu/ergo-repo/properties/" + urlStrs[0];
        String baseName = FilenameUtils.getBaseName(metadataUrl);
        String tempDir = Files.createTempDirectory("repo_download_").toString();

        HttpDownloader.downloadFile(metadataUrl + "." + EXTENSION_META, tempDir);

        String metadataFile = tempDir + File.separator + baseName + "." + EXTENSION_META;

        return new File(metadataFile);
    }

}

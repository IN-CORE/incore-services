package edu.illinois.ncsa.incore.repo;

import org.apache.commons.io.FilenameUtils;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geojson.feature.FeatureJSON;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.nio.file.Files;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

@Path("/properties")
public class RepoMetadataService {
    public static final String METADATA_EXTENSION = "mvz";
    public static int INDENT_SPACE = 4;

    // The Java method will process HTTP GET requests like the following:
    //http://localhost:8080/repo/api/properties/edu.illinois.ncsa.ergo.eq.buildings.schemas.buildingInventoryVer5.v1.0$Shelby_County_RES31224702005658
    @GET
    @Path("{datasetId}")
    @Produces(MediaType.APPLICATION_JSON)

    public String getMetadataById(@PathParam("datasetId") String id) {
        File metadata = null;

        try {
            metadata = loadMetadataFromRepository(id);
            return(formatAsJson(metadata));
        } catch (IOException e) {
            e.printStackTrace();;
            return "{\"error:\" + \"" + e.getLocalizedMessage() + "\"}";
        }
    }

    private String formatAsJson(File metadataFile) throws IOException {
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
        RepoUtils.deleteTmpDir(metadataFile, METADATA_EXTENSION);


        try {
            JSONObject metaJsonObj = XML.toJSONObject(xmlString);
            String jsonString = metaJsonObj.toString(INDENT_SPACE);
            return jsonString;
        } catch (JSONException e) {
            e.printStackTrace();
            return "{\"error:\" + \"" + e.getLocalizedMessage() + "\"}";
        }
    }

    private File loadMetadataFromRepository(String id) throws IOException {
        String urlPart = id.replace("$", "/");
        String metadataUrl = "https://earthquake.ncsa.illinois.edu/ergo-repo/properties/" + urlPart;
        String baseName = FilenameUtils.getBaseName(metadataUrl);
        String tempDir = Files.createTempDirectory("repo_download_").toString();

        HttpDownloader.downloadFile(metadataUrl + "." + METADATA_EXTENSION, tempDir);

        String metadataFile = tempDir + File.separator + baseName + "." + METADATA_EXTENSION;

        return new File(metadataFile);
    }
}

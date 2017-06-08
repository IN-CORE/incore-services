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
import java.net.URLDecoder;
import java.net.URLEncoder;

@Path("/properties")
public class RepoMetadataService {
    public static final String METADATA_EXTENSION = "mvz";

    // The Java method will process HTTP GET requests like the following:
    //http://localhost:8080/repo/api/properties/edu.illinois.ncsa.ergo.eq.buildings.schemas.buildingInventoryVer5.v1.0$Shelby_County_RES31224702005658
    @GET
    @Path("{datasetId}")
    @Produces(MediaType.APPLICATION_JSON)

    public String getMetadataById(@PathParam("datasetId") String id) {
        File metadata = null;
        try {
            metadata = loadMetadataFromRepository(id);
            String utf16String = formatAsString(metadata);
//            return convertUtf16to8(utf16String);
            return utf16String;
        } catch (IOException e) {
            e.printStackTrace();;
            return "{\"error:\" + \"" + e.getLocalizedMessage() + "\"}";
        }
    }

    public String convertUtf16to8(String inStr){
        try {
            // Convert from UTF-8 to UTF-8
//            byte[] str16Bytes = inStr.getBytes("UTF-16");
//            String str16 = new String(str16Bytes, "UTF-16");

            // Convert from UTF-8 to Unicode
//            String byte16Str = new String(str16Bytes,  "UTF-16");
//            byte[] out8Byte = str16.getBytes("UTF-8");
//            String outStr = new String(out8Byte, "UTF-8");
            String tmpString = URLEncoder.encode(inStr, "UTF-16");
            System.out.println(tmpString);
            String outStr = URLDecoder.decode(inStr, "UTF-8");
            System.out.println(outStr);
            return outStr;
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    private String formatAsString(File metadataFile) throws IOException {
        //TODO: this should return the data in geoJSON format
        String outString;

        StringBuilder metadataSB = new StringBuilder();
        try {
            BufferedReader metadataReader = new BufferedReader(new FileReader(metadataFile));
            String metadataLine;

            while ((metadataLine = metadataReader.readLine()) != null) {
                metadataSB.append(convertUtf16to8(metadataLine));
            }
            outString = metadataSB.toString();
        } catch (IOException e) {
            e.printStackTrace();;
            outString = "{\"error:\" + \"" + e.getLocalizedMessage() + "\"}";
        }

        deleteTmpDir(metadataFile);

        return outString;
    }

    private void deleteTmpDir(File metadataFile) {
        String fileName = metadataFile.getAbsolutePath();
        System.out.println(fileName);
        String filePath = fileName.substring(0, fileName.lastIndexOf(metadataFile.separator));
        int extLoc = metadataFile.getName().indexOf(".");
        String extName = metadataFile.getName().substring(extLoc);
        String fileNameWithNoExt = FilenameUtils.removeExtension(fileName);

        String delFileName = fileNameWithNoExt + "." + METADATA_EXTENSION;
        File delFile = new File(delFileName);
        try {
            if (delFile.delete()) {
                System.out.println("file deleted: " + delFileName);
            } else {
                System.out.println("file did not deleted: " + delFileName);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        File delDir = new File(filePath);
        try {
            if (delDir.delete()) {
                System.out.println("Directory deleted: " + filePath);
            } else {
                System.out.println("Directory not deleted: " + filePath);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private File loadMetadataFromRepository(String id) throws IOException {
        String urlPart = id.replace("$", "/");
        System.out.println(urlPart);
        String metadataUrl = "https://earthquake.ncsa.illinois.edu/ergo-repo/properties/" + urlPart;
        System.out.println(metadataUrl);
        String baseName = FilenameUtils.getBaseName(metadataUrl);
        String tempDir = Files.createTempDirectory("repo_download_").toString();

        HttpDownloader.downloadFile(metadataUrl + "." + METADATA_EXTENSION, tempDir);

        String metadataFile = tempDir + File.separator + baseName + "." + METADATA_EXTENSION;

        return new File(metadataFile);
    }
}

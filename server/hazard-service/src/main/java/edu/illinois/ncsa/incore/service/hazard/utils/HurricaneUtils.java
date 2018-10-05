package edu.illinois.ncsa.incore.service.hazard.utils;

import edu.illinois.ncsa.incore.service.hazard.geotools.GeotoolsUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Created by ywkim on 10/4/2018.
 */
public class HurricaneUtils {
    private static final Logger logger = Logger.getLogger(HurricaneUtils.class);
    public static final String TEMP_DIR_PREFIX = "temp_hurricane_";

    // gdal_grid command should be changed based on the system
    public static final String cmdGdalGrid = "cmd /c \"C:\\Program Files\\GDAL\\gdal_grid\" "; // for windows

    public static final String cmdZField = "-zfield velocity ";
    //public static final String cmdLayer = "-l hurricane5 ";
    public static final String cmdAlgo = "-a invdist:power=2.0:smothing=0.0:radius1=0.0:radius2=0.0:angle=0.0:max_points=0:min_points=0:nodata=0.0 ";
    public static final String cmdSize = "-outsize 3000 3000 ";
    public static final String cmdType = "-of GTiff ";

    public static LinkedList convertDoubleJsonArrayToList(JSONArray inArray){
        LinkedList<Double> jsonList = new LinkedList<Double>();
        for (int i=0;i<inArray.size();i++){
            jsonList.add((double) inArray.get(i));
        }

        return jsonList;
    }

    public static void CreateHurricanePointShapefile(List<Double> lats, List<Double> lons, List vals, String inFile) throws MismatchedDimensionException, NoSuchAuthorityCodeException, FactoryException, TransformException, IOException, SchemaException {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        DefaultGeographicCRS crs = DefaultGeographicCRS.WGS84;

        AttributeTypeBuilder attBuilder = new AttributeTypeBuilder();
        attBuilder.setName("the_geom"); //$NON-NLS-1$
        attBuilder.setBinding(Point.class);
        attBuilder.crs(crs);

        GeometryType geomType = attBuilder.buildGeometryType();
        GeometryDescriptor geomDesc = attBuilder.buildDescriptor("the_geom", geomType); //$NON-NLS-1$

        builder.setName("Point"); //$NON-NLS-1$
        builder.add(geomDesc);
        builder.add("velocity", Integer.class); //$NON-NLS-1$

        //build the type
        final SimpleFeatureType schema = builder.buildFeatureType();
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(schema);

        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        DefaultFeatureCollection dfc = new DefaultFeatureCollection();
        for (int i=0; i<lats.size();i++){
            for (int j=0;j<lons.size();j++) {
                LinkedList<Double> tmpList = (LinkedList<Double>) vals.get(i);
                Point point = geometryFactory.createPoint(new Coordinate(lons.get(j), lats.get(i)));
                featureBuilder.add(point);
                featureBuilder.add(tmpList.get(j));
                SimpleFeature feature = featureBuilder.buildFeature(null);
                dfc.add(feature);
            }
        }

        File outfile = new File(inFile);

        GeotoolsUtils.outToFile(outfile, schema, dfc);

        System.out.println(inFile + " has been created.");
    }

    public static void processHurricaneFromJson(String inJsonPath) throws MismatchedDimensionException, NoSuchAuthorityCodeException, FactoryException, TransformException, SchemaException {
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader(inJsonPath))
        {
            //Read JSON file
            JSONObject hurricaneObj = (JSONObject)jsonParser.parse(reader);
            Long resolution = (Long) hurricaneObj.get("resolution");
            String resolutionUnits = (String) hurricaneObj.get("resolutionUnits");
            String landfallLocation = (String) hurricaneObj.get("landfallLocation");
            JSONArray times = (JSONArray) hurricaneObj.get("times");
            JSONArray centers = (JSONArray) hurricaneObj.get("centers");
            JSONArray hurricaneSims = (JSONArray) hurricaneObj.get("hurricaneSimulations");
            JSONObject sampleHurricane = (JSONObject) hurricaneSims.get(0);
            JSONArray gridLats = (JSONArray) sampleHurricane.get("gridLats");
            JSONArray gridLongs = (JSONArray) sampleHurricane.get("gridLongs");
            JSONArray surfaceVeloc = (JSONArray) sampleHurricane.get("surfaceVelocityAbs");

            // create velocity list
            LinkedList<LinkedList> velList = new LinkedList<LinkedList>();
            for (int i=0;i<surfaceVeloc.size();i++) {
                JSONArray tmpArray = (JSONArray) surfaceVeloc.get(i);
                LinkedList<Double> tmpList = new LinkedList<Double>();
                for(int j=0;j<tmpArray.size();j++){
                    tmpList.add((double)tmpArray.get(j));
                }
                velList.add(tmpList);
            }

            // create temp dir and copy files to temp dir
            String tempDir = Files.createTempDirectory(TEMP_DIR_PREFIX).toString();
            logger.debug("Temporay directory " + tempDir + " has been created.");
            String outShp = tempDir + "/hurricane5.shp";
            String outTif = tempDir + "/hurricane5.tif";


            List<Double> latList = convertDoubleJsonArrayToList(gridLats);
            List<Double> lonList = convertDoubleJsonArrayToList(gridLongs);

            CreateHurricanePointShapefile(latList, lonList, velList, outShp);

            String cmdStr = cmdGdalGrid + cmdZField + cmdAlgo + cmdType + outShp + " " + outTif;

            logger.debug(cmdStr);
            Process p = Runtime.getRuntime().exec(cmdStr);
            String s = null;

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            // read the output from the command
            logger.debug("standard output of the command:\n");
            while ((s = stdInput.readLine()) != null) {
                logger.debug(s);
            }

            // read any errors from the attempted command
            System.out.println("standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                logger.error(s);
            }

            System.exit(0);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, MismatchedDimensionException, NoSuchAuthorityCodeException, FactoryException, TransformException, SchemaException {
        String inJsonPath = "C:/Users/ywkim/Documents/NIST/Hurricane/hurricanes_RealValue_21by21.json";
        processHurricaneFromJson(inJsonPath);

    }
}

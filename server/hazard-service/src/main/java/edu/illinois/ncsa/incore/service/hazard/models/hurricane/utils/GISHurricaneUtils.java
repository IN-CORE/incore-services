/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.hurricane.utils;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.util.GeometricShapeFactory;
import edu.illinois.ncsa.incore.common.config.Config;
import edu.illinois.ncsa.incore.service.hazard.geotools.GeotoolsUtils;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.HurricaneSimulationDataset;
import edu.illinois.ncsa.incore.service.hazard.utils.ServiceUtil;
import edu.illinois.ncsa.incore.service.hazard.utils.GISUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.geotools.data.collection.SpatialIndexFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.process.vector.ClipProcess;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.spatial.BBOX;
import org.opengis.geometry.BoundingBox;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;

import javax.ws.rs.NotFoundException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.*;

/**
 * Created by ywkim on 10/4/2018.
 * <p>
 * This creates the geotiff from the given hurricane JSON
 * This process needs GDAL to be installed in the running platform machine.
 * TODO the variable cmdGdalGrid should be modified based on the platform machine and system
 * It first creates the point shapefile with the velocity values
 * Then, created the geotiff using the IDW method of the points' location and velocity values
 * The cell size is being calculated by the number of points and given resolution and set to 100m as default
 * The resolution part might be enhanced by user's input, maybe later?
 */
public class GISHurricaneUtils {
    public static final String TEMP_DIR_PREFIX = "temp_hurricane_";
    // todo gdal_grid command should be changed based on the system
//    public static final String cmdGdalGrid = "cmd /c \"C:\\Program Files\\GDAL\\gdal_grid\" "; // for windows
    public static final String cmdGdalGrid = "gdal_grid ";  // for mac/linux
    public static final String cmdZField = "-zfield velocity ";
    public static final String cmdAlgo = "-a invdist:power=2.0:smoothing=0.0:radius1=0.0:radius2=0.0:angle=0.0:max_points=0:min_points=0:nodata=0.0 ";
    public static final String cmdType = "-of GTiff ";
    // hurrican shapefile field name
    public static final String HURRICANE_FLD_SIM = "simulation";
    public static final String HURRICANE_FLD_VELOCITY = "velocity";
    public static final List<Integer> CIRCULAR_ZONE_DISTS = Arrays.asList(10000, 50000, 100000, 300000); //meters from coast
    private static final Logger logger = Logger.getLogger(GISHurricaneUtils.class);
    private static final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
    // file path for land polygon - dissolved
    public static String dslvPolygon = "tm_north_america_dislvd.shp";
    // file path for country boundary polygon - separated
    public static String sprPolygon = "tm_north_america_country.shp";
    public static String resourcePath = "/hazard/hurricane/shapefiles/";
    public static SimpleFeatureCollection continentFeatures;
    public static SimpleFeatureCollection countriesFeatures;
    public static SpatialIndexFeatureCollection continentFeatureIndex;
    public static DefaultGeographicCRS crs = DefaultGeographicCRS.WGS84;
    public static double searchDistLimit = 0;
    public static double minSearchDist = 0;
    public static String usaCords = "usa.txt";
    public static String mexicoCords = "mexico.txt";
    public static String cubaCords = "cuba.txt";
    public static String jamaicaCords = "jamaica.txt";
    public static Polygon usaPolygon;
    public static Polygon mexicoPolygon;
    public static Polygon cubaPolygon;
    public static Polygon jamaicaPolygon;
    private static DefaultFeatureCollection allDfc = new DefaultFeatureCollection();
    private static SimpleFeatureType allSchema = null;

    //public static GeodeticCalculator geodeticCalculator;

    //Is it a good idea to load them in static context? Affecting Performance when loading per request (each loop)
    static {
        try {
            continentFeatures = GeotoolsUtils.GetSimpleFeatureCollectionFromPath(
                GeotoolsUtils.class.getResource(resourcePath + dslvPolygon).getPath());
            countriesFeatures = GeotoolsUtils.GetSimpleFeatureCollectionFromPath(GeotoolsUtils.class.getResource(
                resourcePath + sprPolygon).getPath());
            continentFeatureIndex = new SpatialIndexFeatureCollection(continentFeatures.getSchema());
            continentFeatureIndex.addAll(continentFeatures);
            searchDistLimit = continentFeatureIndex.getBounds().getSpan(0);
            minSearchDist = searchDistLimit + 1.0e-6;

            usaPolygon = GeotoolsUtils.getPolygonFromFile(GeotoolsUtils.class.getResource(
                resourcePath + usaCords).getPath());
            mexicoPolygon = GeotoolsUtils.getPolygonFromFile(GeotoolsUtils.class.getResource(
                resourcePath + mexicoCords).getPath());
            cubaPolygon = GeotoolsUtils.getPolygonFromFile(GeotoolsUtils.class.getResource(
                resourcePath + cubaCords).getPath());
            jamaicaPolygon = GeotoolsUtils.getPolygonFromFile(GeotoolsUtils.class.getResource(
                resourcePath + jamaicaCords).getPath());

        } catch (IOException e) {
            throw new NotFoundException("Shapefile Not found. Static init failed");
        }
    }

    public static LinkedList convertDoubleJsonArrayToList(JSONArray inArray) {
        LinkedList<Double> jsonList = new LinkedList<Double>();
        for (int i = 0; i < inArray.size(); i++) {
            jsonList.add((double) inArray.get(i));
        }

        return jsonList;
    }

    /**
     * This creates the point shapefile from latitude and longitude lists and put the value list as its attribute
     *
     * @param lats
     * @param lons
     * @param vals
     * @param inFile
     * @throws MismatchedDimensionException
     * @throws NoSuchAuthorityCodeException
     * @throws FactoryException
     * @throws TransformException
     * @throws IOException
     * @throws SchemaException
     */
    public static void CreateHurricanePointShapefile(List<Double> lats, List<Double> lons, List vals, int simNum, String inFile) throws MismatchedDimensionException, IOException {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        DefaultGeographicCRS crs = DefaultGeographicCRS.WGS84;

        AttributeTypeBuilder attBuilder = new AttributeTypeBuilder();
        attBuilder.setName("the_geom");
        attBuilder.setBinding(Point.class);
        attBuilder.crs(crs);

        GeometryType geomType = attBuilder.buildGeometryType();
        GeometryDescriptor geomDesc = attBuilder.buildDescriptor("the_geom", geomType);

        builder.setName("Point");
        builder.add(geomDesc);
        builder.add(HURRICANE_FLD_VELOCITY, Double.class);
        builder.add(HURRICANE_FLD_SIM, Integer.class);

        //build the type
        final SimpleFeatureType schema = builder.buildFeatureType();
        allSchema = schema;
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(schema);

        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        DefaultFeatureCollection dfc = new DefaultFeatureCollection();
        for (int i = 0; i < lats.size(); i++) {
            for (int j = 0; j < lons.size(); j++) {
                LinkedList<Double> tmpList = (LinkedList<Double>) vals.get(i);
                Point point = geometryFactory.createPoint(new Coordinate(lons.get(j), lats.get(i)));
                featureBuilder.add(point);
                featureBuilder.add(tmpList.get(j));
                featureBuilder.add(simNum);
                SimpleFeature feature = featureBuilder.buildFeature(null);
                dfc.add(feature);
                allDfc.add(feature);
            }
        }

        File outfile = new File(inFile);

        GeotoolsUtils.outToFile(outfile, schema, dfc);
        dfc.clear();

        logger.debug(inFile + " has been created.");
    }

    /**
     * Main method for creating hurricane GeoTiff from JSON file
     *
     * @param strJson
     * @param rasterResolution in km
     * @throws MismatchedDimensionException
     * @throws NoSuchAuthorityCodeException
     * @throws FactoryException
     * @throws TransformException
     * @throws SchemaException
     */
    //
    public static List<HurricaneSimulationDataset> processHurricaneFromJson(String strJson, double rasterResolution,
                                                                            String username) throws MismatchedDimensionException {
        // TODO the rasterResolution input in here is KM, so if the input json unit is not km, this should be changed
        // TODO also, since this is geographic projection, the resolution is not perfectly matching with the given value
        // TODO however, since the geotiff is for the visualization purpose only, this should be fine
        // TODO if this is for the actual velocity value calculation, this also should be changed.
        JSONParser jsonParser = new JSONParser();
        List<HurricaneSimulationDataset> hsDatasets = new ArrayList<>();
        try {

            JSONObject hurricaneObj = (JSONObject) jsonParser.parse(strJson);
            Long resolution = (Long) hurricaneObj.get("resolution");
            JSONArray hurricaneSims = (JSONArray) hurricaneObj.get("hurricaneSimulations");

            // calculate the cell size.
            // in here it is set to 100m resolution
            // TODO it assumes that unit is km, if it is different, the new lines should be added
            int numGridLatPoint = ((JSONArray) ((JSONObject) hurricaneSims.get(0)).get("gridLats")).size();
            int numGridLonPoint = ((JSONArray) ((JSONObject) hurricaneSims.get(0)).get("gridLongs")).size();
            double totalXLength = resolution * (numGridLonPoint - 1);   // total length in km
            double totalYLength = resolution * (numGridLatPoint - 1);   // total length in km
            int numCellsX = (int) (totalXLength / rasterResolution);
            int numCellsY = (int) (totalYLength / rasterResolution);
            int totCells = numCellsX * numCellsY;
            logger.debug("Number of the cell will be " + totCells);
            String tempDir = Files.createTempDirectory(TEMP_DIR_PREFIX).toString();
            System.out.println(tempDir);
            logger.debug("Temporay directory " + tempDir + " has been created.");

            for (int k = 0; k < hurricaneSims.size(); k++) {
                JSONObject tempHurricane = (JSONObject) hurricaneSims.get(k);
                JSONArray gridLats = (JSONArray) tempHurricane.get("gridLats");
                JSONArray gridLongs = (JSONArray) tempHurricane.get("gridLongs");
                JSONArray surfaceVeloc = (JSONArray) tempHurricane.get("surfaceVelocityAbs");

                // create velocity list
                LinkedList<LinkedList> velList = new LinkedList<LinkedList>();
                for (int i = 0; i < surfaceVeloc.size(); i++) {
                    JSONArray tmpArray = (JSONArray) surfaceVeloc.get(i);
                    LinkedList<Double> tmpList = new LinkedList<Double>();
                    for (int j = 0; j < tmpArray.size(); j++) {
                        tmpList.add((double) tmpArray.get(j));
                    }
                    velList.add(tmpList);
                }

                String tifName = "hurricane" + k;
                String outShp = tempDir + "/hurricane" + k + ".shp";
                String outTif = tempDir + "/hurricane" + k + ".tif";

                List<Double> latList = convertDoubleJsonArrayToList(gridLats);
                List<Double> lonList = convertDoubleJsonArrayToList(gridLongs);

                // create point shapefile
                CreateHurricanePointShapefile(latList, lonList, velList, k, outShp);

                // create geotiff from given shapefile
                String cmdSize = "-outsize " + numCellsX + " " + numCellsY + " ";
                String layerCmd = "-l " + tifName + " ";
                String cmdStr = "";
                if (numCellsX > 1) {
                    cmdStr = cmdGdalGrid + cmdZField + cmdAlgo + cmdType + layerCmd + cmdSize + outShp + " " + outTif;
                } else {
                    cmdStr = cmdGdalGrid + cmdZField + cmdAlgo + cmdType + layerCmd + outShp + " " + outTif;
                }

                logger.debug(cmdStr);
                performProcess(cmdStr);

                //TODO Get the creator - pass a param?
                HurricaneSimulationDataset simDataset = HurricaneUtil.createHurricaneDataSetFromFile(outTif, "Hurricane Grid Snapshot",
                    username, "Created by Hurricane Windfield Simulation Service", "HurricaneDataset",
                    (String) tempHurricane.get("absTime"));
                hsDatasets.add(simDataset);
            }

            // create big output file
            String outFilePath = tempDir + "/hurricane_all.shp";
            File outAllFile = new File(outFilePath);
            GeotoolsUtils.outToFile(outAllFile, allSchema, allDfc);
            allDfc.clear();

            List<String> outFilePaths = Arrays.asList(tempDir + "/hurricane_all.shp", tempDir + "/hurricane_all.shx",
                tempDir + "/hurricane_all.dbf", tempDir + "/hurricane_all.fix", tempDir + "/hurricane_all.prj");

            HurricaneSimulationDataset simDatasetAll = HurricaneUtil.createHurricaneDataSetFromFiles(outFilePaths, "Hurricane Full Snapshot",
                username, "Created by Hurricane Windfield Simulation Service", "HurricaneDataset",
                "full time range");
            hsDatasets.add(simDatasetAll);

            // remove temp file
            deleteTmpDir(new File(tempDir));

            //TODO add a method to remove the temp directory
        } catch (FileNotFoundException e) {
            throw new NotFoundException("Input file not found");
        } catch (IOException e) {
            throw new NotFoundException("Error creating the Raster");
        } catch (ParseException e) {
            throw new NotFoundException("Error parsing the json input");
        }
        return hsDatasets;
    }

    /**
     * perform system process outside the java
     *
     * @param cmdStr
     * @throws IOException
     */
    public static void performProcess(String cmdStr) throws IOException {
        Process p = Runtime.getRuntime().exec(cmdStr);
        String s = null;

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        // read the output from the command
        logger.debug("standard output of the command:\n");
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }

        // read any errors from the attempted command
        logger.debug("standard error of the command (if any):\n");
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }
    }

    /**
     * calculate hurricane velocity of given point
     *
     * @param datasetId
     * @param username
     * @param lat
     * @param lon
     * @return
     * @throws IOException
     */
    public static double CalcVelocityFromPoint(String datasetId, String username, double lat, double lon) throws IOException {
        // copy the shapefile from the repository to the temp directory and cache them
        // if there is no cache, create the cache folder and use it
        // to collect all the shapefile components in a single directory
        // so geotools can read the shapefile
        double searchLimitDistVariable = 0.5;
        File hurricaneCacheDir = new File(Config.getConfigProperties().getProperty("data.repo.data.dir") + File.separator + "cache_data" + File.separator + "hurricane" + File.separator + "shapefile" + File.separator + datasetId);
        String shpFilePath = null;
        if (hurricaneCacheDir.exists()) {
            String[] shapefileList = hurricaneCacheDir.list();
            for (String file : shapefileList) {
                File inFile = new File(file);
                String fileName = FilenameUtils.getName(inFile.getName());
                String fileExt = FilenameUtils.getExtension(inFile.getName());
                if (fileExt.equalsIgnoreCase("shp")) {
                    shpFilePath = hurricaneCacheDir + File.separator + inFile.getPath();
                }
            }
        } else {
            org.json.JSONObject datasetJson = ServiceUtil.getDatasetJsonFromDataService(datasetId, username);
            List fileList = ServiceUtil.getFileDescriptorFileList(datasetJson);
            shpFilePath = ServiceUtil.collectShapfileInFolder(fileList, hurricaneCacheDir);
        }

        SimpleFeatureCollection inFeatures = GeotoolsUtils.GetSimpleFeatureCollectionFromPath(shpFilePath);
        DefaultFeatureCollection outFC = new DefaultFeatureCollection();

        if (inFeatures != null) {
            Coordinate coord = new Coordinate(lon, lat);
            Point startPoint = geometryFactory.createPoint(coord);
            DefaultGeographicCRS crs = DefaultGeographicCRS.WGS84;

            // check if the point is in the boulding box
            boolean isPointIn = GISUtil.IsPointInPolygonBySFC(inFeatures, startPoint);
            if (isPointIn == false) {
                logger.debug("Point is out of bounds");
                return 0.0;
            }

            SimpleFeatureIterator inFeatureIter = inFeatures.features();
            List<SimpleFeature> inFeatureList = new ArrayList<SimpleFeature>();
            int ifIndex = 0;
            try {
                while (inFeatureIter.hasNext()) {
                    inFeatureList.add((SimpleFeature) inFeatureIter.next());
                    ifIndex++;
                    if (ifIndex == 2) {
                        break;
                    }
                }
            } finally {
                inFeatureIter.close();
            }

            SimpleFeature firstFeature = inFeatureList.get(0);
            SimpleFeature secondFeature = inFeatureList.get(1);

            double firstX = ((Point) firstFeature.getDefaultGeometry()).getCoordinate().x;
            double secondX = ((Point) secondFeature.getDefaultGeometry()).getCoordinate().x;


            // TODO calculate limit distance
//            final double searchDistLimitByBoundingBox = featureIndex.getBounds().getSpan(0) / 10;
            double searchDistLimit = (Math.abs(firstX - secondX)) * searchLimitDistVariable;
            Coordinate pCoord = startPoint.getCoordinate();
            ReferencedEnvelope refEnv = new ReferencedEnvelope(new Envelope(pCoord),
                inFeatures.getSchema().getCoordinateReferenceSystem());

            // sort features by the distance. the closest comes first
            SimpleFeature[] sfArr = createClipper(refEnv, searchDistLimit, inFeatures);

            if (sfArr.length == 0) {
                logger.debug("Point is out of bounds");
                return 0.0;
            }

            // get the simulation number with the highest value in closest N points
            // 12 should cover most of simulation box in around the point
            // create closest n points
            int n = 12;

            // check if sfArr has more than needed number of point which is n
            while(sfArr.length < n) {
                searchLimitDistVariable += 1;
                searchDistLimit = (Math.abs(firstX - secondX)) * searchLimitDistVariable;
                sfArr = createClipper(refEnv, searchDistLimit, inFeatures);
            }
            sfArr = sortByDistance(sfArr, pCoord);
            int simNum = getHighestSimulationNumber(sfArr.clone(), n);

            // grab closet 3 point with the simulation given simulation number
            List<SimpleFeature> simPtList = new LinkedList<SimpleFeature>();
            int testN = 3;
            for (int i = 0; i < sfArr.length; i++) {
                if ((int) sfArr[i].getAttribute("simulation") == simNum) {
                    simPtList.add(sfArr[i]);
                    if (simPtList.size() == testN) {
                        break;
                    }
                }
            }

            // check if it is in a same line which means that the point is not in the simulation.
            boolean isLinedUp = isPointOnSameLine(simPtList);

            // if the points are lined up, select other simulation
            if (isLinedUp) {
                simNum = selectAnotherSimNum(simNum, sfArr);
            }

            // select point with only simNum
            List<SimpleFeature> selectedSimPts = selectSimNumPoints(sfArr, simNum);

            // if selectedSimPts is less than 4 points, this should be redone
            if (selectedSimPts.size() < 4) {
                while (selectedSimPts.size() < 4) {
                    searchLimitDistVariable += 1;
                    searchDistLimit = (Math.abs(firstX - secondX)) * searchLimitDistVariable;
                    sfArr = createClipper(refEnv, searchDistLimit, inFeatures);
                    selectedSimPts = selectSimNumPoints(sfArr, simNum);
                }
                // reselect after distance sorting for more accurate result
                sfArr = sortByDistance(sfArr, pCoord);
                selectedSimPts = selectSimNumPoints(sfArr, simNum);
            }
            // create 4 points that consists rectangular shape
            List<SimpleFeature> rectangularPtList = createRectangularPoints(selectedSimPts);

//			// create output shapefile of 4 points for debugging purpose
//			for (int i=0; i<rectangularPtList.size();i++){
//				outFC.add(rectangularPtList.get(i));
//			}
//			outToFile(new File("C:\\Users\\ywkim\\Documents\\NIST\\Hurricane\\out_small.shp"), schema, outFC);

            // give enough distance for the minimum distance to start
            double minDist = searchDistLimit + 1.0e-6;

            return interpolateFromFourPoints(rectangularPtList, lat, lon, minDist);
        } else {
            return 0.0;
        }
    }

    public static SimpleFeature[] createClipper(ReferencedEnvelope refEnv, double searchDistLimit, SimpleFeatureCollection inFeatures) {
        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
        refEnv.expandBy(searchDistLimit);
        BBOX bbox = ff.bbox(ff.property(inFeatures.getSchema().getGeometryDescriptor().getName()), (BoundingBox) refEnv);

        // make sfc clip
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        Coordinate[] coords  =
            new Coordinate[] {new Coordinate(refEnv.getMinX(), refEnv.getMinY()), new Coordinate(refEnv.getMaxX(), refEnv.getMinY()),
                new Coordinate(refEnv.getMaxX(), refEnv.getMaxY()), new Coordinate(refEnv.getMinX(), refEnv.getMaxY()), new Coordinate(refEnv.getMinX(), refEnv.getMinY()) };
        LinearRing ring = geometryFactory.createLinearRing( coords );
        LinearRing holes[] = null; // use LinearRing[] to represent holes
        Polygon clip_zone = geometryFactory.createPolygon(ring, holes );

        ClipProcess cp = new ClipProcess();
        SimpleFeatureCollection sfc = cp.execute(inFeatures, clip_zone, false);

        Coordinate minDistPoint = null;
        SimpleFeatureIterator sfi = sfc.features();

        // create output feature array.
        SimpleFeature[] sfArr = new SimpleFeature[sfc.size()];
        int sfIndex = 0;
        try {
            while (sfi.hasNext()) {
                sfArr[sfIndex] = (SimpleFeature) sfi.next();
                sfIndex++;
            }
        } finally {
            sfi.close();
        }

        return sfArr;
    }

    public static void deleteTmpDir(File tempDir) {
        String[] fileList = tempDir.list();
        for (String file : fileList) {
            File currentFile = new File(tempDir.getPath(), file);
            currentFile.delete();
        }
        tempDir.delete();
    }

    /**
     * create rectangular points from the hurricane point shapefile for interpolation
     *
     * @param simList
     * @return
     */
    public static List<SimpleFeature> createRectangularPoints(List<SimpleFeature> simList) {
        List<SimpleFeature> outPtList = new LinkedList<SimpleFeature>();

        // select first three points
        Coordinate pt1 = ((Point) simList.get(0).getDefaultGeometry()).getCoordinate();
        Coordinate pt2 = ((Point) simList.get(1).getDefaultGeometry()).getCoordinate();
        Coordinate pt3 = ((Point) simList.get(2).getDefaultGeometry()).getCoordinate();

        List<Double> xList = new LinkedList<Double>();
        List<Double> yList = new LinkedList<Double>();

        xList.add(pt1.x);
        xList.add(pt2.x);
        xList.add(pt3.x);
        yList.add(pt1.y);
        yList.add(pt2.y);
        yList.add(pt3.y);

        double xMin = Collections.min(xList);
        double xMax = Collections.max(xList);
        double yMin = Collections.min(yList);
        double yMax = Collections.max(yList);
        int breakIndex = 0;

        for (int i = 0; i < simList.size(); i++) {
            double tmpX = (((Point) simList.get(i).getDefaultGeometry()).getCoordinate()).x;
            double tmpY = (((Point) simList.get(i).getDefaultGeometry()).getCoordinate()).y;

            if (tmpX == xMin && tmpY == yMin) {
                outPtList.add(simList.get(i));
                breakIndex++;
            }
            if (tmpX == xMin && tmpY == yMax) {
                outPtList.add(simList.get(i));
                breakIndex++;
            }
            if (tmpX == xMax && tmpY == yMin) {
                outPtList.add(simList.get(i));
                breakIndex++;
            }
            if (tmpX == xMax && tmpY == yMax) {
                outPtList.add(simList.get(i));
                breakIndex++;
            }
            if (breakIndex == 4) {
                break;
            }
        }

        return outPtList;

    }

    /**
     * select points with only given simulation number
     *
     * @param sfArr
     * @param simNum
     * @return
     */
    public static List<SimpleFeature> selectSimNumPoints(SimpleFeature[] sfArr, int simNum) {

        List<SimpleFeature> sfList = new LinkedList<SimpleFeature>();
        for (int i = 0; i < sfArr.length; i++) {
            if ((int) sfArr[i].getAttribute("simulation") == simNum) {
                sfList.add(sfArr[i]);
            }
        }

        return sfList;
    }

    /**
     * select newer simulation number when selected simulation number doesn't work
     * when the given point located outside the simulation
     *
     * @param simNum
     * @param sfArr
     * @return
     */
    public static int selectAnotherSimNum(int simNum, SimpleFeature[] sfArr)  {
        // the simnum that should be selected should be either simnum + 1 or simnum - 1
        List<SimpleFeature> sfList = new LinkedList<SimpleFeature>();

        // remove given simulation number from the list
        for (int i = 0; i < sfArr.length; i++) {
            if ((int) sfArr[i].getAttribute("simulation") != simNum) {
                sfList.add(sfArr[i]);
            }
        }

        SimpleFeature[] newSfArr = new SimpleFeature[sfList.size()];

        //create output shapefile of the selection for the debugging purpose
        DefaultFeatureCollection outFC = new DefaultFeatureCollection();
        for (int i = 0; i < sfList.size(); i++) {
            newSfArr[i] = sfList.get(i);
        }

        // get new simulation number with highest value
        simNum = getHighestSimulationNumber(newSfArr, newSfArr.length);

        return simNum;
    }

    /**
     * sort given selected features based on the distance from the given point
     *
     * @param sfArr
     * @param pCoord
     * @return
     */
    public static SimpleFeature[] sortByDistance(SimpleFeature[] sfArr, Coordinate pCoord) {
        int n = sfArr.length;
        for (int i = 0; i < n; i++) {
            for (int j = 1; j < (n - 1); j++) {
                SimpleFeature sf_j = sfArr[j];
                SimpleFeature sf_j_1 = sfArr[j - 1];
                Coordinate sf_j_point = ((Point) sf_j.getDefaultGeometry()).getCoordinate();
                Coordinate sf_j_1_point = ((Point) sf_j_1.getDefaultGeometry()).getCoordinate();
                double distance_j = sf_j_point.distance(pCoord);
                double distance_j_1 = sf_j_1_point.distance(pCoord);

                if (distance_j_1 > distance_j) {
                    SimpleFeature temp = sfArr[j - 1];
                    sfArr[j - 1] = sfArr[j];
                    sfArr[j] = temp;
                }
            }
        }

        return sfArr;
    }

    /**
     * get the simulation number of the highest hurrication value
     *
     * @param sfArr
     * @param n
     * @return
     */
    public static int getHighestSimulationNumber(SimpleFeature[] sfArr, int n) {
        double highestValue = 0.0;
        int simNum = 0;

        for (int i = 0; i < n; i++) {
            double tempValue = (double) sfArr[i].getAttribute(HURRICANE_FLD_VELOCITY);
            if (tempValue > highestValue) {
                highestValue = tempValue;
                simNum = (int) sfArr[i].getAttribute(HURRICANE_FLD_SIM);
            }
        }

        return simNum;
    }

    /**
     * check if the selected three points are in the same line, which is an error status
     *
     * @param simPtList
     * @return
     */
    public static boolean isPointOnSameLine(List<SimpleFeature> simPtList) {
        boolean isLinedUp = false;
        // collect all x, y coordinage values
        List<Double> xList = new ArrayList<Double>();
        List<Double> yList = new ArrayList<Double>();

        for (int i = 0; i < simPtList.size(); i++) {
            xList.add((((Point) simPtList.get(i).getDefaultGeometry()).getCoordinate()).x);
            yList.add((((Point) simPtList.get(i).getDefaultGeometry()).getCoordinate()).y);
        }

        // remove all duplicates in the x and y list
        List<Double> xListWithoutDuplicates = xList.stream().distinct().collect(Collectors.toList());
        List<Double> yListWithoutDuplicates = yList.stream().distinct().collect(Collectors.toList());

        if (xListWithoutDuplicates.size() == 1 || yListWithoutDuplicates.size() == 1) {
            isLinedUp = true;
        }

        return isLinedUp;
    }

    /**
     * find minmum and maxium x and y value
     *
     * @param inList
     * @return
     */
    public static double[] findMaxMin(List<SimpleFeature> inList) {
        // collect all x, y coordinage values
        List<Double> xList = new ArrayList<Double>();
        List<Double> yList = new ArrayList<Double>();

        for (int i = 0; i < inList.size(); i++) {
            xList.add((((Point) inList.get(i).getDefaultGeometry()).getCoordinate()).x);
            yList.add((((Point) inList.get(i).getDefaultGeometry()).getCoordinate()).y);
        }

        double[] outArr = new double[4];
        outArr[0] = Collections.min(xList);
        outArr[1] = Collections.max(xList);
        outArr[2] = Collections.min(yList);
        outArr[3] = Collections.max(yList);

        return outArr;
    }

    /**
     * calculate interpolation value from give four points
     *
     * @param inList
     * @param lat
     * @param lon
     * @param minDist
     * @return
     * @throws IOException
     */
    public static double interpolateFromFourPoints(List<SimpleFeature> inList, double lat, double lon, double minDist) throws IOException {
        double[] minMaxArr = findMaxMin(inList);

        double xMin = minMaxArr[0];
        double xMax = minMaxArr[1];
        double yMin = minMaxArr[2];
        double yMax = minMaxArr[3];

        // arrange the point with ll, ul, lr, and ur
        // it is always in different shape, so just decide
        // start from the point with the least x values (will be Q11)
        SimpleFeature q11Sf = null;
        SimpleFeature q12Sf = null;
        SimpleFeature q21Sf = null;
        SimpleFeature q22Sf = null;

        for (int i = 0; i < inList.size(); i++) {
            double tmpX = (((Point) inList.get(i).getDefaultGeometry()).getCoordinate()).x;
            double tmpY = (((Point) inList.get(i).getDefaultGeometry()).getCoordinate()).y;

            if (tmpX == xMin && tmpY == yMin) {
                q11Sf = inList.get(i);
            }
            if (tmpX == xMin && tmpY == yMax) {
                q12Sf = inList.get(i);
            }
            if (tmpX == xMax && tmpY == yMin) {
                q21Sf = inList.get(i);
            }
            if (tmpX == xMax && tmpY == yMax) {
                q22Sf = inList.get(i);
            }
        }

        List<SimpleFeature> qList = new LinkedList<SimpleFeature>();
        qList.add(q11Sf);
        qList.add(q12Sf);
        qList.add(q22Sf);
        qList.add(q21Sf);

        // create polygon using sfList4 for testing
//		createSurroundingPolygonShapefile(new File("C:\\Users\\ywkim\\Documents\\NIST\\Hurricane\\out_poly.shp"), qList);

        double x1 = ((Point) q11Sf.getDefaultGeometry()).getCoordinate().x;
        double x2 = ((Point) q21Sf.getDefaultGeometry()).getCoordinate().x;
        double y1 = ((Point) q11Sf.getDefaultGeometry()).getCoordinate().y;
        double y2 = ((Point) q12Sf.getDefaultGeometry()).getCoordinate().y;
        double x = lon;
        double y = lat;

        double q11val = (double) q11Sf.getAttribute("velocity");
        double q12val = (double) q12Sf.getAttribute("velocity");
        double q21val = (double) q21Sf.getAttribute("velocity");
        double q22val = (double) q22Sf.getAttribute("velocity");

        // interpolation
        // from https://en.wikipedia.org/wiki/Bilinear_interpolation
        // x-direction interpolation
        double f_x_y1 = ((x2 - x) / (x2 - x1)) * q11val + ((x - x1) / (x2 - x1)) * q21val;
        double f_x_y2 = ((x2 - x) / (x2 - x1)) * q12val + ((x - x1) / (x2 - x1)) * q22val;
        // y direction interpolation
        double f_x_y = ((y2 - y) / (y2 - y1)) * f_x_y1 + ((y - y1) / (y2 - y1)) * f_x_y2;

        return f_x_y;
    }

    public static String getCountryFromNAPolygons(double lat, double lon) {
        Point currPoint = geometryFactory.createPoint(new Coordinate(lon, lat));

        if (GISHurricaneUtils.usaPolygon.contains(currPoint)) {
            return "usa";
        } else if (GISHurricaneUtils.mexicoPolygon.contains(currPoint)) {
            return "mexico";
        } else if (GISHurricaneUtils.cubaPolygon.contains(currPoint)) {
            return "cuba";
        } else if (GISHurricaneUtils.jamaicaPolygon.contains(currPoint)) {
            return "jamaica";
        }

        return "";
    }

    //TODO: This function needs to be revisited to match PI's implementation. Cleanup is needed once done
    public static List<Polygon> createCircles(double cLat, double cLong, double tangentDist) {
        List<Polygon> circles = new ArrayList<>();

        for (int dist : CIRCULAR_ZONE_DISTS) {
            GeometricShapeFactory gsf = new GeometricShapeFactory();
            gsf.setNumPoints(101); //PI is making 101 point circle
            gsf.setCentre(new Coordinate(cLong, cLat));
            double diameterKm = 2 * tangentDist + dist;
            //gsf.setSize(diameterKm);
            //Polygon geometry = gsf.createCircle();

            gsf.setWidth(diameterKm / 111.320d);
            // Length in meters of 1° of longitude = 40075 km * cos( latitude ) / 360
            gsf.setHeight(diameterKm / (40075.000 * Math.cos(Math.toRadians(cLat)) / 360));

            //TODO: This needs to be changed to create a perfect circle, but how?
            Polygon geometry = gsf.createEllipse();


            circles.add(geometry);
        }
        return circles;
    }

    public static List<Double> createRegionRadii(double tangentDist) {
        List<Double> regionRadii = new ArrayList<>();

        for (int dist : CIRCULAR_ZONE_DISTS) {
            regionRadii.add(dist + tangentDist);
        }
        return regionRadii;
    }

    /**
     * Gets distance between two points on earth
     *
     * @param p1
     * @param p2
     * @param method
     * @return
     */
    public static double getGeogDistance(Point p1, Point p2, String method) {
        double distance = 0;

        double lamda1 = p1.getX() * Math.PI / 180;
        double lamda2 = p2.getX() * Math.PI / 180;
        double phi1 = p1.getY() * Math.PI / 180;
        double phi2 = p2.getY() * Math.PI / 180;

        double deltaPhi = phi1 - phi2;
        double deltaLamda = lamda1 - lamda2;

        if (method == "hwind") {
            double a = pow(sin(deltaPhi / 2), 2);
            double c = 2 * atan2(sqrt(a), sqrt(1 - a));

            double deltaY = HurricaneUtil.R_EARTH * c * signum(deltaPhi);

            double a1 = pow(cos(HurricaneUtil.PARA / 180 * PI), 2) * pow(sin(deltaLamda / 2), 2);

            double c1 = 2 * atan2(sqrt(a1), sqrt(1 - a1));

            double deltaX = HurricaneUtil.R_EARTH * c1 * signum(deltaLamda);

            distance = sqrt(pow(deltaY, 2) + pow(deltaX, 2));
        }
        return distance;
    }
}

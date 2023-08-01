package edu.illinois.ncsa.incore.service.data.utils;

import it.geosolutions.geoserver.rest.GeoServerRESTManager;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.decoder.*;
import it.geosolutions.geoserver.rest.decoder.utils.NameLinkElem;
import it.geosolutions.geoserver.rest.encoder.GSResourceEncoder;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static java.lang.System.getenv;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

// https://github.com/geosolutions-it/geoserver-manager/blob/master/src/test/java/it/geosolutions/geoserver/rest/GeoserverRESTTest.java

public class GeoserverTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(GeoserverTest.class);

    @Rule
    public TestName _testName = new TestName();

    public static final String DEFAULT_WS = "test";

    public static final String RESTURL;

    public static final String RESTUSER;

    public static final String RESTPW;

    // geoserver target version
    public static final String GS_VERSION;

    public static URL URL;

    public static GeoServerRESTManager manager;

    public static GeoServerRESTReader reader;

    public static GeoServerRESTPublisher publisher;

    private static boolean enabled = false;

    private static Boolean existgs = null;


    static {
        RESTURL = "http://localhost:8080/geoserver";
        RESTUSER = "admin";
        RESTPW = "geoserver";
        GS_VERSION = "2.22";

        // These tests will destroy data, so let's make sure we do want to run them
        enabled = true;
        if (!enabled)
            LOGGER.warn("Tests are disabled. Please read the documentation to enable them.");

        try {
            URL = new URL(RESTURL);
            manager = new GeoServerRESTManager(URL, RESTUSER, RESTPW);
            reader = manager.getReader();
            publisher = manager.getPublisher();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public static void testGetDatastores() {

        RESTWorkspaceList wslist = reader.getWorkspaces();
        assertNotNull(wslist);
//        assertEquals(7, wslist.size()); // value in default gs installation

        LOGGER.debug("Workspaces: " + wslist.size());
        int dsnum = 0;
        for (RESTWorkspaceList.RESTShortWorkspace ws : wslist) {
            LOGGER.debug("Getting DSlist for WS " + ws.getName() + "..." );
            RESTDataStoreList result = reader.getDatastores(ws.getName());
            assertNotNull(result);
            dsnum += result.size();
            for (NameLinkElem ds : result) {
                assertNotNull(ds.getName());
                LOGGER.debug(ds.getName() + " " );
                RESTDataStore datastore = reader.getDatastore(ws.getName(), ds.getName());
                assertNotNull(datastore);
                assertEquals(ds.getName(), datastore.getName());
                assertEquals(ws.getName(), datastore.getWorkspaceName());
            }
            LOGGER.debug("");
        }
        LOGGER.debug("");
        LOGGER.debug("Datastores:" + dsnum); // value in default gs installation
        System.out.println("Datastores:" + dsnum);
//        assertEquals(4, dsnum); // value in default gs installation

    }

    public static void testGetWorkspaces() {
        RESTWorkspaceList wslist = reader.getWorkspaces();
        assertNotNull(wslist);
//        assertEquals(7, wslist.size()); // value in default gs installation

        LOGGER.debug("Workspaces:" + wslist.size());
        LOGGER.debug("Workspaces:");
        for (RESTWorkspaceList.RESTShortWorkspace ws : wslist) {
            LOGGER.debug(ws.getName() + " ");
        }
        LOGGER.debug("");

        assertEquals(wslist.size(), reader.getWorkspaceNames().size());
    }

    public static void testGetWorkspaceNames() {
        List<String> names = reader.getWorkspaceNames();
        assertNotNull(names);
//        assertEquals(7, names.size()); // value in default gs installation

        LOGGER.debug("Workspaces:" + names.size());
        LOGGER.debug("Workspaces:");
        for (String name : names) {
            LOGGER.debug(name + " ");
            System.out.println(name + " ");
        }
        LOGGER.debug("");
    }

    public static void testCoverageStores() {
        List<String> workspaces = reader.getWorkspaceNames();
        for (String workspace : workspaces) {
            List<String> stores = reader.getCoverageStores(workspace).getNames();

            for (String storename : stores) {
                // RESTCoverageStore store = reader.getCoverageStore(workspace, storename);

                System.out.println(storename);
            }
        }
        LOGGER.debug("");
    }

    public static void insertExternalShape() throws FileNotFoundException, IOException {

//        publisher.createWorkspace(DEFAULT_WS);
        publisher.publishStyle(new File("C:\\rest\\test\\default_point.sld"));
        String store_name = "teststore";


        File zipFile = new File("C:\\rest\\test\\guid_yes.zip");

        boolean published = publisher.publishShp(DEFAULT_WS, "anyname", "cities", zipFile, "EPSG:4326", "default_point");
        assertTrue("publish() failed", published);

        //test delete
        boolean ok = publisher.unpublishFeatureType(DEFAULT_WS, "anyname", "cities");
        assertTrue("Unpublish() failed", ok);
        // remove also datastore
        boolean dsRemoved = publisher.removeDatastore(DEFAULT_WS, "anyname");
        assertTrue("removeDatastore() failed", dsRemoved);
    }


    public static void insertExternalGeotiff() throws FileNotFoundException, IOException {
        String storeName = "testRESTStoreGeotiff";
        String layerName = "resttestdem";

//        publisher.createWorkspace(DEFAULT_WS);
//        publisher.publishStyle(new File(new ClassPathResource("testdata").getFile(),"raster.sld"));

//        File geotiff = new ClassPathResource("testdata/resttestdem.tif").getFile();
        File geotiff = new File("C:\\rest\\test\\2009_30m_cdls_small_wgs84.tif");
        boolean pc = publisher.publishExternalGeoTIFF(DEFAULT_WS, storeName, geotiff, layerName,"EPSG:4326", GSResourceEncoder.ProjectionPolicy.FORCE_DECLARED,"raster");

        assertTrue(pc);
    }

    private static void deleteCoverage(String layerName) {
        RESTLayer layer = reader.getLayer(layerName);
        RESTCoverage coverage = reader.getCoverage(layer);
        RESTCoverageStore coverageStore = reader.getCoverageStore(coverage);

        LOGGER.warn("Deleting Coverage " + coverageStore.getWorkspaceName() + " : "
            + coverageStore.getName() + " / " + coverage.getName());

        boolean removed = publisher.unpublishCoverage(coverageStore.getWorkspaceName(),
            coverageStore.getName(), coverage.getName());
        assertTrue("Coverage not deleted " + coverageStore.getWorkspaceName() + " : "
            + coverageStore.getName() + " / " + coverage.getName(), removed);
    }

    private static void deleteCoverageStores(String workspace, String storename) {
        LOGGER.warn("Deleting CoverageStore " + workspace + " : " + storename);
        boolean removed = publisher.removeCoverageStore(workspace, storename, false, GeoServerRESTPublisher.Purge.METADATA);
        assertTrue("CoverageStore not removed " + workspace + " : " + storename, removed);
    }

    protected boolean existsLayer(String layername) {
        return reader.getLayer(layername) != null;
    }

    public static void main(String[] args) throws IOException {
        System.out.println("test");
//        testGetDatastores();
//        testGetWorkspaces();
//        testGetWorkspaceNames();
//        insertExternalGeotiff();
        insertExternalShape();
//        deleteCoverage("resttestdem1");
//        testCoverageStores();
//        deleteCoverageStores("test", "testRESTStoreGeotiff");
    }
}

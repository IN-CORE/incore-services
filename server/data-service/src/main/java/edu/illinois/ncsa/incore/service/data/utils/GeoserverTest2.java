package edu.illinois.ncsa.incore.service.data.utils;

import org.geoserver.openapi.model.catalog.CoverageInfo;
import org.geoserver.openapi.model.catalog.CoverageStoreInfo;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.SecureRandom;
import java.util.List;

import org.geoserver.openapi.model.catalog.CoverageInfo;
import org.geoserver.openapi.model.catalog.CoverageStoreInfo;
import org.geoserver.openapi.model.catalog.CoverageStoreInfo.TypeEnum;
import org.geoserver.openapi.model.catalog.ProjectionPolicy;
import org.geoserver.openapi.v1.model.CoverageStoreResponse;
import org.geoserver.openapi.v1.model.Layer;
import org.geoserver.openapi.v1.model.WorkspaceSummary;
import org.geoserver.restconfig.client.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GeoserverTest2 {
    final String apiUrl = "http://localhost:8080/geoserver/rest";
    final String publicUrl = "http://localhost:8080/geoserver";

//    final URL geoserverApiURL = new URL(apiUrl);
//    final URL geoserverPublicURL = new URL(publicUrl);

    final String adminName = "admin";
    final String adminPassword = "geoserver";
    public GeoServerClient client = new GeoServerClient(apiUrl).setBasicAuth(this.adminName, this.adminPassword);

    private SecureRandom rnd = new SecureRandom();
//
//    // two workspaces
//    private WorkspaceSummary ws1, ws2;
//
//    private URI sfdemURI;
//
//    private WorkspacesClient workspaces;
//    private CoverageStoresClient coverageStores;
//    private CoveragesClient coverages;
//
//    private CoverageStoreResponse sfdemStore;
//
//    this.workspaces.create(wsname1);
//    this.workspaces.create(wsname2);
//    this.ws1 = workspaces.findByName(wsname1).get();
//    this.ws2 = workspaces.findByName(wsname2).get();
//
//    this.sfdemURI = support.getSFDemGeoTiff();
//
//    this.sfdemStore =
//    this.coverageStores.create(wsname1, "sfdem", "Test geotiff", TypeEnum.GEOTIFF, sfdemURI.toString());

    public static void createCoverage(GeoServerClient client, CoveragesClient coverages) {
        CoverageInfo createBody =
            new CoverageInfo() //
                .nativeCoverageName("test_coverage") //
                .name("PublishedName") //
                .store(new CoverageStoreInfo().name("testcoverage"));
        CoverageInfo created = coverages.create("test", createBody);
        System.out.println("test");
//
//        CoverageInfo created = this.coverages.create(ws1.getName(), createBody);
//        assertNotNull(created);
    }

    public static void testCreateGeoTiffCoverageStore(GeoServerClient client, CoverageStoresClient coverages) {
        URI testUri = URI.create("file:data/test/sfdem.tif");
        String uri = testUri.toString();
        String wsname = "test";
        CoverageStoreResponse created = coverages.create(wsname, "geotiffStore", "test geotiff based data store", TypeEnum.GEOTIFF, uri);
        assertNotNull(created);
    }

    public static void testFindByWorkspace(GeoServerClient client, CoverageStoresClient coverages) {
        List<Link> stores = coverages.findByWorkspace("sf");
        assertEquals(1, stores.size());
        Link storeLink = stores.get(0);
        assertEquals("sfdem", storeLink.getName());

        stores = coverages.findByWorkspace("topp");
        assertEquals(0, stores.size());
    }

    public static void main(String[] args) throws IOException {
        String apiUrl = "http://localhost:8080/geoserver/rest";
        String publicUrl = "http://localhost:8080/geoserver";

//    final URL geoserverApiURL = new URL(apiUrl);
//    final URL geoserverPublicURL = new URL(publicUrl);

        String adminName = "admin";
        String adminPassword = "geoserver";
        GeoServerClient client = new GeoServerClient(apiUrl).setBasicAuth(adminName, adminPassword);
        WorkspacesClient workspaces = client.workspaces();
        CoverageStoresClient coverageStores = client.coverageStores();
        CoveragesClient coverages = client.coverages();

//        createCoverage(client, coverages);
//        testCreateGeoTiffCoverageStore(client, coverageStores);
        testFindByWorkspace(client, coverageStores);
        System.out.println("test");
    }
}

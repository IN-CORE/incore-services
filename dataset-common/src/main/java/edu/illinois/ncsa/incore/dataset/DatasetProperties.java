package edu.illinois.ncsa.incore.dataset;

/**
 * Metadata/properties to describe a dataset
 */
public class DatasetProperties {

    public String friendlyName;
    public String datasetId;  //in ergo 1 the id was a complex object. Not sure that's necessary?
    public String dataFormat; //ie what factory/reader/writer to use. Shapefile? csv?
    public String schema;     //in ergo 1 schema was a single string. We might have to adapt this for v2

    public String dataURI;

}

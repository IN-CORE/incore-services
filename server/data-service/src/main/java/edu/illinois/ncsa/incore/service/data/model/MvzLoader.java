/*
 * ******************************************************************************
 *   Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 *   This program and the accompanying materials are made available under the
 *   terms of the BSD-3-Clause which accompanies this distribution,
 *   and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *  ******************************************************************************
 */

package edu.illinois.ncsa.incore.service.data.model;

import edu.illinois.ncsa.incore.service.data.utils.FileUtils;
import edu.illinois.ncsa.incore.service.data.model.mvz.*;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by ywkim on 9/27/2017.
 */
public class MvzLoader {
    public static final String TAG_PROPERTIES_GIS = "gis-dataset-properties";   //$NON-NLS-1$
    public static final String TAG_PROPERTIES_MAP = "mapped-dataset-properties";    //$NON-NLS-1$
    public static final String TAG_PROPERTIES_FILE = "file-dataset-properties"; //$NON-NLS-1$
    public static final String TAG_PROPERTIES_RASTER = "raster-dataset-properties"; //$NON-NLS-1$
    public static final String TAG_PROPERTIES_SCENARIO = "dataset-properties";  //$NON-NLS-1$
    public static final String TAG_NAME = "name";   //$NON-NLS-1$
    public static final String TAG_VERSION = "version"; //$NON-NLS-1$
    public static final String TAG_DATA_FORMAT = "data-format"; //$NON-NLS-1$
    public static final String TAG_TYPE_ID = "type-id"; //$NON-NLS-1$
    public static final String TAG_FEATURE_TYPE_NAME = "feature-type-name"; //$NON-NLS-1$
    public static final String TAG_CONVERTED_FEATURE_TYPE_NAME = "converted-feature-type-name"; //$NON-NLS-1$
    public static final String TAG_GEOMETRY_TYPE = "geometry-type"; //$NON-NLS-1$
    public static final String TAG_LOCATION ="location";    //$NON-NLS-1$
    public static final String TAG_DESCRIPTION = "desription";  //$NON-NLS-1$
    public static final String TAG_DATASET_ID = "dataset-id";   //$NON-NLS-1$
    public static final String TAG_MAEVIZ_MAPPING = "maeviz-mapping";   //$NON-NLS-1$
    public static final String TAG_SCHEMA = "schema";   //$NON-NLS-1$
    public static final String TAG_MAPPING = "mapping"; //$NON-NLS-1$
    public static final String TAG_FROM = "from";   //$NON-NLS-1$
    public static final String TAG_TO = "to";   //$NON-NLS-1$
    public static final String TAG_METADATA = "metadata";   //$NON-NLS-1$
    public static final String TAG_TABLE_METADATA = "table-metadata";   //$NON-NLS-1$
    public static final String TAG_COLUMN_METADATA = "column-metadata"; //$NON-NLS-1$
    public static final String TAG_FRIENDLY_NAME = "friendly-name"; //$NON-NLS-1$
    public static final String TAG_FIELD_LENGTH = "field-length";   //$NON-NLS-1$
    public static final String TAG_UNIT = "unit";   //$NON-NLS-1$
    public static final String TAG_COLUMN_ID = "column-id"; //$NON-NLS-1$
    public static final String TAG_SIGFIGS = "sig-figs";    //$NON-NLS-1$
    public static final String TAG_UNIT_TYPE = "unit-type"; //$NON-NLS-1$
    public static final String TAG_IS_NUMERIC = "is-numeric";   //$NON-NLS-1$
    public static final String TAG_IS_RESULT = "is-result"; //$NON-NLS-1$
    public static final String TAG_PROPERTIES = ""; //$NON-NLS-1$

    public static final Logger logger = Logger.getLogger(MvzLoader.class);

    public static MvzDataset createMvzDatasetFromMetadata(String inUrl){
        MvzDataset mvzDataset = new MvzDataset();
        try {
            File metadata = FileUtils.loadMetadataFromRepository(inUrl);
            mvzDataset = setMvzDatasetFromMetadata(metadata, inUrl);

        } catch (IOException e) {
            e.printStackTrace();;
            String err = "{\"error:\" + \"" + e.getLocalizedMessage() + "\"}";  //$NON-NLS-1$
        }

        return mvzDataset;
    }

    public static MvzDataset setMvzDatasetFromMetadata(File metadataFile, String rUrl) throws IOException {
        String xmlString = "";
        metadataFile.setReadOnly();
        Reader metadataReader = new InputStreamReader(new FileInputStream(metadataFile), "UTF-16");
        char metaCharBuffer[] = new char[2048];
        int len;
        while ((len = metadataReader.read(metaCharBuffer, 0, metaCharBuffer.length)) != -1) {
            xmlString = xmlString + new String(metaCharBuffer, 0, len);
        }
        metadataReader.close();
        FileUtils.deleteTmpDir(metadataFile, FileUtils.EXTENSION_META);

        // remove metadata file extestion from inId if there is any
        String tmpEndStr = rUrl.substring(rUrl.lastIndexOf('.') + 1);
        if (tmpEndStr.equals(FileUtils.EXTENSION_META)) {
            rUrl = rUrl.substring(0, rUrl.length() - 4);
        }

        MvzDataset mvzDataset = setMvzDatasetFromJson(rUrl, xmlString, true);

        return mvzDataset;
    }

    private static MvzDataset setMvzDatasetFromJson(String rUrl, String inJson, boolean isXml) {
        MvzDataset mvzDataset = new MvzDataset();
        String datasetPropertyName = "";    //$NON-NLS-1$
        String name = "";   //$NON-NLS-1$
        String version = "";    //$NON-NLS-1$
        String dataFormat = ""; //$NON-NLS-1$
        String typeId = ""; //$NON-NLS-1$
        String featureTypeName = "";    //$NON-NLS-1$
        String convertedFeatureTypeName = "";   //$NON-NLS-1$
        String geometryType = "";   //$NON-NLS-1$
        String location = "";   //$NON-NLS-1$
        String description = "";    //$NON-NLS-1$
        //String schema = "";
        //String from = "";
        //String to = "";
        //boolean isMaevizMapping = false;
        //boolean isMetadata = false;

        try {
            JSONObject inJsonObj = new JSONObject();
            if (isXml) {
                inJsonObj = XML.toJSONObject(inJson);
            } else {
                inJsonObj = new JSONObject(inJson);
            }
            JSONObject metaInfoObj = null;
            JSONObject locObj = null;
            if (inJsonObj.has(TAG_PROPERTIES_GIS)) {
                metaInfoObj = inJsonObj.getJSONObject(TAG_PROPERTIES_GIS);
                locObj = inJsonObj.getJSONObject(TAG_PROPERTIES_GIS).getJSONObject(TAG_DATASET_ID);
                featureTypeName = metaInfoObj.get(TAG_FEATURE_TYPE_NAME).toString();
                convertedFeatureTypeName = metaInfoObj.get(TAG_CONVERTED_FEATURE_TYPE_NAME).toString();
                geometryType = metaInfoObj.get(TAG_GEOMETRY_TYPE).toString();
                datasetPropertyName = TAG_PROPERTIES_GIS;
                mvzDataset.setFeaturetypeName(featureTypeName);
                mvzDataset.setConvertedFeatureTypeName(convertedFeatureTypeName);
                mvzDataset.setGeometryType(geometryType);
            }
            if (inJsonObj.has(TAG_PROPERTIES_MAP)) {
                metaInfoObj = inJsonObj.getJSONObject(TAG_PROPERTIES_MAP);
                locObj = inJsonObj.getJSONObject(TAG_PROPERTIES_MAP).getJSONObject(TAG_DATASET_ID);
                datasetPropertyName = TAG_PROPERTIES_MAP;
            }
            if (inJsonObj.has(TAG_PROPERTIES_FILE)) {
                metaInfoObj = inJsonObj.getJSONObject(TAG_PROPERTIES_FILE);
                locObj = inJsonObj.getJSONObject(TAG_PROPERTIES_FILE).getJSONObject(TAG_DATASET_ID);
                datasetPropertyName = TAG_PROPERTIES_FILE;
            }
            if (inJsonObj.has(TAG_PROPERTIES_RASTER)) {
                metaInfoObj = inJsonObj.getJSONObject(TAG_PROPERTIES_RASTER);
                locObj = inJsonObj.getJSONObject(TAG_PROPERTIES_RASTER).getJSONObject(TAG_DATASET_ID);
                datasetPropertyName = TAG_PROPERTIES_RASTER;
            }
            if (inJsonObj.has(TAG_PROPERTIES_SCENARIO)) {
                metaInfoObj = inJsonObj.getJSONObject(TAG_PROPERTIES_SCENARIO);
                locObj = inJsonObj.getJSONObject(TAG_PROPERTIES_SCENARIO).getJSONObject(TAG_DATASET_ID);
                datasetPropertyName = TAG_PROPERTIES_SCENARIO;
            }

            if (metaInfoObj.has(TAG_NAME)) {
                name = metaInfoObj.get(TAG_NAME).toString();
            }
            if (metaInfoObj.has(TAG_VERSION)) {
                version = metaInfoObj.get(TAG_VERSION).toString();
            }
            if (metaInfoObj.has(TAG_DATA_FORMAT)) {
                dataFormat = metaInfoObj.get(TAG_DATA_FORMAT).toString();
            }
            if (metaInfoObj.has(TAG_TYPE_ID)) {
                typeId = metaInfoObj.get(TAG_TYPE_ID).toString();
            }
            if (metaInfoObj.has(TAG_LOCATION)) {
                location = locObj.get(TAG_LOCATION).toString();
            }
            if (metaInfoObj.has(TAG_DESCRIPTION)) {
                description = locObj.get(TAG_DESCRIPTION).toString();
            }

            mvzDataset.setDatasetPropertyName(datasetPropertyName);
            mvzDataset.setName(name);
            mvzDataset.setVersion(version);
            mvzDataset.setDataFormat(dataFormat);
            mvzDataset.setTypeId(typeId);
            mvzDataset.datasetId.setDescription(description);

            String newUrl = FileUtils.SERVER_URL_PREFIX + rUrl + "/files";    //$NON-NLS-1$
            mvzDataset.datasetId.setLocation(newUrl);

            // check maeviz-mapping object and set
            if (metaInfoObj != null) {
                if (metaInfoObj.has(TAG_MAEVIZ_MAPPING)) {
                    List<Mapping> mappings = new LinkedList<Mapping>();
                    mvzDataset.maevizMapping.setSchema(metaInfoObj.getJSONObject(TAG_MAEVIZ_MAPPING).get(TAG_SCHEMA).toString());
                    if (metaInfoObj.getJSONObject(TAG_MAEVIZ_MAPPING).has(TAG_MAPPING)) {
                        if (metaInfoObj.getJSONObject(TAG_MAEVIZ_MAPPING).get(TAG_MAPPING) instanceof JSONObject) {
                            JSONObject mappingJsonObj = (JSONObject) metaInfoObj.getJSONObject(TAG_MAEVIZ_MAPPING).get(TAG_MAPPING);
                            Mapping m = new Mapping();
                            if (mappingJsonObj.has(TAG_FROM)) {
                                m.setFrom(mappingJsonObj.get(TAG_FROM).toString());
                            }
                            if (mappingJsonObj.has(TAG_TO)) {
                                m.setTo(mappingJsonObj.get(TAG_TO).toString());
                            }
                            mappings.add(m);
                            mvzDataset.maevizMapping.setMapping(mappings);
                        } else if (metaInfoObj.getJSONObject(TAG_MAEVIZ_MAPPING).get(TAG_MAPPING) instanceof JSONArray) {
                            JSONArray mappingJsonArray = (JSONArray) metaInfoObj.getJSONObject(TAG_MAEVIZ_MAPPING).get(TAG_MAPPING);
                            for (int i = 0; i < mappingJsonArray.length(); i++) {
                                JSONObject mappingJsonObj = (JSONObject) mappingJsonArray.get(i);
                                Mapping m = new Mapping();
                                if (mappingJsonObj.has(TAG_FROM)) {
                                    m.setFrom(mappingJsonObj.get(TAG_FROM).toString());
                                }
                                if (mappingJsonObj.has(TAG_TO)) {
                                    m.setTo(mappingJsonObj.get(TAG_TO).toString());
                                }
                                mappings.add(m);
                            }
                            mvzDataset.maevizMapping.setMapping(mappings);
                        }
                    }
                }

                // check metadata object and set
                if (metaInfoObj.has(TAG_METADATA)) {
                    List<ColumnMetadata> columnMetadatas = new LinkedList<ColumnMetadata>();
                    if (metaInfoObj.getJSONObject(TAG_METADATA).has(TAG_TABLE_METADATA)) {
                        if (!(metaInfoObj.getJSONObject(TAG_METADATA).get(TAG_TABLE_METADATA) instanceof String)) {
                            if (((JSONObject) (metaInfoObj.getJSONObject(TAG_METADATA).get(TAG_TABLE_METADATA))).has(TAG_COLUMN_METADATA)) {
                                if (((JSONObject) metaInfoObj.getJSONObject(TAG_METADATA).get(TAG_TABLE_METADATA)).get(TAG_COLUMN_METADATA) instanceof JSONObject) {
                                    JSONObject columnMetadataObj = (JSONObject) ((JSONObject) metaInfoObj.getJSONObject(TAG_METADATA).get(TAG_TABLE_METADATA)).get(TAG_COLUMN_METADATA);
                                    Metadata metadata = new Metadata();
                                    ColumnMetadata columnMetadata = new ColumnMetadata();
                                    if (columnMetadataObj.has(TAG_FRIENDLY_NAME)) {
                                        columnMetadata.setFriendlyName(columnMetadataObj.get(TAG_FRIENDLY_NAME).toString());
                                    }
                                    if (columnMetadataObj.has(TAG_FIELD_LENGTH)) {
                                        columnMetadata.setFieldLength(Integer.parseInt(columnMetadataObj.get(TAG_FIELD_LENGTH).toString()));
                                    }
                                    if (columnMetadataObj.has(TAG_UNIT)) {
                                        columnMetadata.setUnit(columnMetadataObj.get(TAG_UNIT).toString());
                                    }
                                    if (columnMetadataObj.has(TAG_COLUMN_ID)) {
                                        columnMetadata.setColumnId(columnMetadataObj.get(TAG_COLUMN_ID).toString());
                                    }
                                    if (columnMetadataObj.has(TAG_FIELD_LENGTH)) {
                                        columnMetadata.setSigFigs(Integer.parseInt(columnMetadataObj.get(TAG_FIELD_LENGTH).toString()));
                                    }
                                    if (columnMetadataObj.has(TAG_UNIT_TYPE)) {
                                        columnMetadata.setUnitType(columnMetadataObj.get(TAG_UNIT_TYPE).toString());
                                    }
                                    if (columnMetadataObj.has(TAG_IS_NUMERIC)) {
                                        columnMetadata.setIsNumeric((boolean) columnMetadataObj.get(TAG_IS_NUMERIC));
                                    }
                                    if (columnMetadataObj.has(TAG_IS_RESULT)) {
                                        columnMetadata.setIsResult((boolean) columnMetadataObj.get(TAG_IS_RESULT));
                                    }
                                    columnMetadatas.add(columnMetadata);
                                    metadata.tableMetadata.setColumnMetadata(columnMetadatas);
                                    mvzDataset.setMetadata(metadata);
                                } else if (((JSONObject) metaInfoObj.getJSONObject(TAG_METADATA).get(TAG_TABLE_METADATA)).get(TAG_COLUMN_METADATA) instanceof JSONArray) {
                                    JSONArray columnMetadataArray = (JSONArray) ((JSONObject) metaInfoObj.getJSONObject(TAG_METADATA).get(TAG_TABLE_METADATA)).get(TAG_COLUMN_METADATA);
                                    Metadata metadata = new Metadata();
                                    TableMetadata tableMetadata = new TableMetadata();
                                    for (int i = 0; i < columnMetadataArray.length(); i++) {
                                        ColumnMetadata columnMetadata = new ColumnMetadata();
                                        JSONObject columnMetadataObj = (JSONObject) columnMetadataArray.get(i);
                                        if (columnMetadataObj.has(TAG_FRIENDLY_NAME)) {
                                            columnMetadata.setFriendlyName(columnMetadataObj.get(TAG_FRIENDLY_NAME).toString());
                                        }
                                        if (columnMetadataObj.has(TAG_FIELD_LENGTH)) {
                                            columnMetadata.setFieldLength(Integer.parseInt(columnMetadataObj.get(TAG_FIELD_LENGTH).toString()));
                                        }
                                        if (columnMetadataObj.has(TAG_UNIT)) {
                                            columnMetadata.setUnit(columnMetadataObj.get(TAG_UNIT).toString());
                                        }
                                        if (columnMetadataObj.has(TAG_COLUMN_ID)) {
                                            columnMetadata.setColumnId(columnMetadataObj.get(TAG_COLUMN_ID).toString());
                                        }
                                        if (columnMetadataObj.has(TAG_FIELD_LENGTH)) {
                                            columnMetadata.setSigFigs(Integer.parseInt(columnMetadataObj.get(TAG_FIELD_LENGTH).toString()));
                                        }
                                        if (columnMetadataObj.has(TAG_UNIT_TYPE)) {
                                            columnMetadata.setUnitType(columnMetadataObj.get(TAG_UNIT_TYPE).toString());
                                        }
                                        if (columnMetadataObj.has(TAG_IS_NUMERIC)) {
                                            columnMetadata.setIsNumeric((boolean) columnMetadataObj.get(TAG_IS_NUMERIC));
                                        }
                                        if (columnMetadataObj.has(TAG_IS_RESULT)) {
                                            columnMetadata.setIsResult((boolean) columnMetadataObj.get(TAG_IS_RESULT));
                                        }
                                        columnMetadatas.add(columnMetadata);
                                    }
                                    metadata.tableMetadata.setColumnMetadata(columnMetadatas);
                                    mvzDataset.setMetadata(metadata);
                                }
                            }
                        }
                    }
                }
            }
            //String jsonString = metaJsonObj.toString(FileUtils.INDENT_SPACE);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mvzDataset;
    }

    public static String formatMetadataAsJson(File metadataFile, String inId, String serverUrlPrefix) throws IOException {
        // convert from UTF-16 to UTF-8
        String xmlString = "";  //$NON-NLS-1$
        metadataFile.setReadOnly();
        Reader metadataReader = new InputStreamReader(new FileInputStream(metadataFile), "UTF-16");
        char metaCharBuffer[] = new char[2048];
        int len;
        while ((len = metadataReader.read(metaCharBuffer, 0, metaCharBuffer.length)) != -1) {
            xmlString = xmlString + new String(metaCharBuffer, 0, len);
        }
        metadataReader.close();
        FileUtils.deleteTmpDir(metadataFile, FileUtils.EXTENSION_META);

        // remove metadata file extestion from inId if there is any
        String tmpEndStr = inId.substring(inId.lastIndexOf('.') + 1);
        if (tmpEndStr.equals(FileUtils.EXTENSION_META)) {
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

            String newUrl = serverUrlPrefix + inId + "/files";  //$NON-NLS-1$
            locObj.put(TAG_LOCATION, newUrl);
            String jsonString = metaJsonObj.toString(FileUtils.INDENT_SPACE);
            return jsonString;
        } catch (JSONException ex) {
            logger.error(ex);
            return "{\"error:\" + \"" + ex.getLocalizedMessage() + "\"}";   //$NON-NLS-1$
        }
    }

    private Mapping setMapping(JSONObject mappingJsonObj) {
        Mapping m = new Mapping();
        m.setFrom(mappingJsonObj.get(TAG_FROM).toString());
        m.setTo(mappingJsonObj.get(TAG_FROM).toString());

        return m;
    }
}

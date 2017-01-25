package edu.illinois.ncsa.incore.services.fragilitymapping;

import ncsa.tools.common.exceptions.DeserializationException;
import ncsa.tools.common.util.XmlUtils;
import org.jamel.dbf.DbfReader;
import org.jamel.dbf.structure.DbfField;
import org.jamel.dbf.structure.DbfRow;

import javax.ws.rs.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Map;

@Path("fragility")
public class FragilityMappingController {
    @GET
    @Produces("text/plain")
    public String getTest() {
        return "hello";
    }

}

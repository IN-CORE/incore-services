
package edu.illinois.ncsa.incore.semantic.units.dimensions;

import edu.illinois.ncsa.incore.semantic.units.dimension.BaseDimension;
import edu.illinois.ncsa.incore.semantic.units.dimension.Dimension;
import edu.illinois.ncsa.incore.semantic.units.instances.Dimensions;
import edu.illinois.ncsa.incore.semantic.units.io.parser.DimensionParser;
import edu.illinois.ncsa.incore.semantic.units.io.parser.RDFParser;
import org.junit.jupiter.api.Test;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RDFParserTests {
    ClassLoader loader = this.getClass().getClassLoader();

    @Test
    public void testSerialize_BaseDimension() throws IOException {
        // arrange
        File file = new File(loader.getResource("dimensions/length.ttl").getFile());
        String rdf = FileUtils.readFileToString(file, "UTF-8");
        BaseDimension expected = Dimensions.length;

        // act
        // Dimension actual = RDFParser.parseDimensions(rdf).get(0);

        // assert
        // assertEquals(expected, actual);
    }
}

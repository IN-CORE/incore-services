
package edu.illinois.ncsa.incore.semantic.units.serializer;

import edu.illinois.ncsa.incore.semantic.units.io.serializer.JsonSerializer;
import org.junit.jupiter.api.Test;

import static edu.illinois.ncsa.incore.semantic.units.instances.SIDerivedUnits.squareMetre;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonSerializerTests {
    ClassLoader loader = this.getClass().getClassLoader();

    @Test
    public void test() {
        JsonSerializer serializer = new JsonSerializer();
        String value = serializer.serialize(squareMetre);

        assertTrue(true);
    }
}

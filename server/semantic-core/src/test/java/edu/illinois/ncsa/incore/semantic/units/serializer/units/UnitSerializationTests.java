
package edu.illinois.ncsa.incore.semantic.units.serializer.units;

import edu.illinois.ncsa.incore.semantic.units.model.PrefixedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import static edu.illinois.ncsa.incore.semantic.units.instances.Prefixes.centi;
import static edu.illinois.ncsa.incore.semantic.units.instances.Prefixes.kilo;
import static edu.illinois.ncsa.incore.semantic.units.instances.SIDerivedUnits.*;
import static edu.illinois.ncsa.incore.semantic.units.instances.SIUnits.metre;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnitSerializationTests {
    ClassLoader loader = this.getClass().getClassLoader();

    @Test
    @DisplayName("Serialize Prefixed Unit - centimetre")
    public void testSerializePrefixedUnit() throws IOException {
        File file = new File(loader.getResource("units/centimetre.ttl").getFile());
        String actual = FileUtils.readFileToString(file, "UTF-8");

        Unit unit = new PrefixedUnit(centi, metre);
        String expected = unit.serialize();

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Serialize Prefixed Coherent Derived Unit - kilojoule")
    public void testSerializePrefixedCoherentDerivedUnit() throws IOException {
        File file = new File(loader.getResource("units/kilojoule.ttl").getFile());
        String actual = FileUtils.readFileToString(file, "UTF-8");

        Unit unit = new PrefixedUnit(kilo, joule);
        String expected = unit.serialize();

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Serialize Prefixable Unit - metre")
    public void testSerializePrefixableUnit() throws IOException {
        File file = new File(loader.getResource("units/metre.ttl").getFile());
        String actual = FileUtils.readFileToString(file, "UTF-8");

        String expected = metre.serialize();

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Serialize Named Derived Unit - volt")
    public void testSerializeNamedDerived() throws IOException {
        File file = new File(loader.getResource("units/volt.ttl").getFile());
        String actual = FileUtils.readFileToString(file, "UTF-8");

        String expected = volt.serialize();

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Serialize Product Derived Unit - newton metre second")
    public void testSerializeProductDerived() throws IOException {
        File file = new File(loader.getResource("units/newton_metre_second.ttl").getFile());
        String actual = FileUtils.readFileToString(file, "UTF-8");

        String expected = newtonMetreSecond.serialize();

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Serialize Dimensionless Unit - radian")
    public void testSerializeDimensionless() throws IOException {
        File file = new File(loader.getResource("units/radian.ttl").getFile());
        String actual = FileUtils.readFileToString(file, "UTF-8");

        String expected = radian.serialize();

        assertEquals(expected, actual);
    }
}


package edu.illinois.ncsa.incore.semantic.units.equivalence;

import org.junit.jupiter.api.Test;

import static edu.illinois.ncsa.incore.semantic.units.instances.SIDerivedUnits.joule;
import static edu.illinois.ncsa.incore.semantic.units.instances.SIDerivedUnits.squareMetre;
import static edu.illinois.ncsa.incore.semantic.units.instances.SIDerivedUnits.watt;
import static edu.illinois.ncsa.incore.semantic.units.instances.SIUnits.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class InequivalenceTests {
    @Test
    public void testInequivalence_Power() {
        assertFalse(metre.equivalentTo(squareMetre));
    }

    @Test
    public void testInequivalence_NamedUnits() {
        assertFalse(joule.equivalentTo(watt.getDerivation()));
    }

    @Test
    public void testInequivalence_Prefix() {
        assertFalse(centimetre.equivalentTo(metre));
        assertFalse(kilogram.equivalentTo(gram));
    }

    @Test
    public void testInequivalence_DerivedPrefix() {
        // assertFalse();
    }

}

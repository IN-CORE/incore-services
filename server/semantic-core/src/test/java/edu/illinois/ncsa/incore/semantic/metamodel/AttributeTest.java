package edu.illinois.ncsa.incore.semantic.metamodel;

import edu.illinois.ncsa.incore.semantic.metamodel.attributes.EnumerableAttribute;
import edu.illinois.ncsa.incore.semantic.metamodel.common.Enumeration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class AttributeTest {
    @Test
    public void test() {
        EnumerableAttribute vacancy = new EnumerableAttribute<Integer>("VACANCY", "Type of Vacancy", Arrays.asList(
                new Enumeration<Integer>(0, "N/A"),
                new Enumeration<Integer>(1, "For rent (H0050002)")
        ));

        //
        //        isRequired = false;
        //        isNullable = true;
        //        UnknownValues = "N/A", "NULL";
        //        isUnique = false;

        // A. Representing the User's Schema as a Model
        // B. Adding a relationship between the User's Schema and The Concept
        // C. Representing the Concept as a Model

        // We also want to state that ever user's schema is potentially a usable concept


        // so how does this match the concept of Housing Vacancy

        // Generic Concept
        // Concept
        // -> Enumeration

        // Enumerable Mapping
    }
}

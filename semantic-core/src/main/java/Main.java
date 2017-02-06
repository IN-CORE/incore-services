import Semantic.Model.Attributes.Attribute;
import Semantic.Model.Attributes.EnumerationAttribute;
import Semantic.Model.Dataset.Dataset;
import Semantic.Units.Instances.SIDerivedUnits;
import Semantic.Units.Instances.SIUnits;
import Semantic.Units.Model.Derived.DerivedUnit;
import Semantic.Units.Model.Derived.DivisionDerivedUnit;
import Semantic.Units.Quantity;
import Semantic.Units.Model.Derived.ProductDerivedUnit;
import Semantic.Units.Model.Normalization;
import Semantic.Units.Model.Unit;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        Dataset test = new Dataset();
        // Category
        // Associated Metadata

        List<Attribute> attributes = new ArrayList<>();


        DerivedUnit m2 = new ProductDerivedUnit(SIUnits.metre, SIUnits.metre);
        Normalization m2Normalization = m2.getNormalForm();

        DerivedUnit j_m3 = SIDerivedUnits.joulePerCubicMetre;
        Normalization normalized = j_m3.getNormalForm();

        Unit ww = new DivisionDerivedUnit(SIDerivedUnits.joulePerCubicMetre, SIUnits.second);
        Quantity quantity = new Quantity(10000, SIUnits.gram);
        Quantity convertedQuantity = quantity.convertTo(SIUnits.kilogram);
    }
}

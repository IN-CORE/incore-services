import SemanticModels.Units.Instances.SIDerivedUnits;
import SemanticModels.Units.Instances.SIUnits;
import SemanticModels.Units.Model.Derived.DerivedUnit;
import SemanticModels.Units.Model.Derived.DivisionDerivedUnit;
import SemanticModels.Units.Quantity;
import SemanticModels.Units.Model.Derived.ProductDerivedUnit;
import SemanticModels.Units.Model.Normalization;
import SemanticModels.Units.Model.Unit;

public class Main {

    public static void main(String[] args) {
        DerivedUnit m2 = new ProductDerivedUnit(SIUnits.metre, SIUnits.metre);
        Normalization m2Normalization = m2.getNormalForm();

        DerivedUnit j_m3 = SIDerivedUnits.joulePerCubicMetre;
        Normalization normalized = j_m3.getNormalForm();

        Unit ww = new DivisionDerivedUnit(SIDerivedUnits.joulePerCubicMetre, SIUnits.second);
        Quantity quantity = new Quantity(10000, SIUnits.gram);
        Quantity convertedQuantity = quantity.convertTo(SIUnits.kilogram);
    }
}

package Models.Inventory.Building.GEM.v2.ConstructionDate;

import Models.Inventory.Building.GEM.v2.Common.Qualifier;

import java.util.Arrays;
import java.util.List;

public class YearBuiltQual extends Qualifier {
    public static YearBuiltQual Y99 = new YearBuiltQual("Y99", "Unknown date of construction/retrofit");
    public static YearBuiltQual YEX = new YearBuiltQual("YEX", "Exactly");
    public static YearBuiltQual YBET = new YearBuiltQual("YBET", "Between");
    public static YearBuiltQual YPRE = new YearBuiltQual("YPRE", "Pre");
    public static YearBuiltQual YAPP = new YearBuiltQual("YAPP", "Approximately");

    private YearBuiltQual(String code, String description, String shortDescription) {
        super(code, description, shortDescription);
    }

    private YearBuiltQual(String code, String description) {
        super(code, description);
    }

    private static List<YearBuiltQual> qualifierMembers = Arrays.asList(Y99, YEX, YBET, YPRE, YAPP);

    @Override
    protected List<YearBuiltQual> getQualifierMembers() {
        return qualifierMembers;
    }
}
package Models.Inventory.Building.GEM.v2.LLRSMaterial;

import Models.Inventory.Building.GEM.v2.Common.GemAttribute;
import Models.Inventory.Building.GEM.v2.Common.Utils.ParseUtils;
import com.sun.deploy.util.StringUtils;

import java.util.List;

public class Material extends GemAttribute {
    private MaterialType type = MaterialType.MAT99;
    private MaterialTechnology technology;
    private SteelConnectionType steelConnectionType;

    public Material() {

    }

    public Material deserialize(String taxonomyString) {
        List<String> taxonomyTokens = ParseUtils.splitAttribute(taxonomyString);

        MaterialType type = MaterialType.valueOf();

    }
}

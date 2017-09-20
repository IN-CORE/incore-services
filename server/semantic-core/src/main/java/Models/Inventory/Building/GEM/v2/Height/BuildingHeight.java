package Models.Inventory.Building.GEM.v2.Height;

import Models.Inventory.Building.GEM.v2.Common.GemAtomSerializable;
import Models.Inventory.Building.GEM.v2.Common.GemAttribute;
import Models.Inventory.Building.GEM.v2.Height.HeightTypes.Height;
import Models.Inventory.Building.GEM.v2.Height.HeightTypes.UnknownHeight;
import Models.Inventory.Building.GEM.v2.Height.Slope.Slope;
import Models.Inventory.Building.GEM.v2.Height.Slope.UnknownSlope;
import com.sun.deploy.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BuildingHeight extends GemAttribute {
    public Height<Integer> aboveGroundStoreys = new UnknownHeight<>(HeightQualifier.H99); // STORY_AG
    public Height<Integer> belowGroundStoreys = new UnknownHeight<>(HeightQualifier.HB99); // STORY_BG
    public Height<Double> groundFloorAboveGrade = new UnknownHeight<>(HeightQualifier.HF99); // HT_HR_GF
    public Slope groundSlope = new UnknownSlope(); // SLOPE

    public BuildingHeight() {

    }

    @Override
    public GemAtomSerializable[] getAttributeMembers() {
        return new GemAtomSerializable[]{aboveGroundStoreys, belowGroundStoreys, groundFloorAboveGrade, groundSlope};
    }

    @Override
    public GemAttribute deserialize(String taxonomyString) {
        List<String> taxonomyTokens = Arrays.asList(StringUtils.splitString(taxonomyString, "+"));



    }
}

package Models.Inventory.Building.GEM.v2.Roof;

import Models.Inventory.Building.GEM.v2.Common.GemAttribute;

import java.util.Arrays;

public class Roof extends GemAttribute {
    public RoofShape shape = RoofShape.RSH99;
    public RoofCoverMaterial coverMaterial = RoofCoverMaterial.RMT99;
    public RoofSystemMaterial systemMaterial = RoofSystemMaterial.R99;
    public RoofSystemType systemMaterialType = null;
    public RoofConnection connections = RoofConnection.RTD99;

    public Roof() {
        // default of all unknown
    }

    public Roof(RoofShape shape) {
        this.shape = shape;
    }

    @Override
    public String getTaxonomyStringShort() {
        return shape.getCode() ;
    }

    @Override
    public String getTaxonomyStringFull() {
        return null;
    }
}

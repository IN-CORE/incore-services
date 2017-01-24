package Models.Inventory.Building.GEM.v2;

import Models.Inventory.Building.GEM.v2.BuildingPlan.PlanShape;
import Models.Inventory.Building.GEM.v2.BuildingPosition.Position;
import Models.Inventory.Building.GEM.v2.ExteriorWalls.ExteriorWalls;
import Models.Inventory.Building.GEM.v2.Foundation.FoundationSystem;

public class GEMBuildingTaxonomy {

    // the easy ones
    public PlanShape planShape = PlanShape.PLF99;
    public Position position = Position.BP99;
    public ExteriorWalls exteriorWalls = ExteriorWalls.EW99;
    public FoundationSystem foundationSystem = FoundationSystem.FOS99;

    public static GEMBuildingTaxonomy deserialize(String taxonomyString) {
        planShape = PlaneShape.valueOf();
    }

    public String getTaxonomyStringShort() {

    }

    public String getTaxonomyStringFull() {
        // JOIN +
    }

    public String getTaxonomyStringOmitUnknown() {
        // ends with 99
    }

}

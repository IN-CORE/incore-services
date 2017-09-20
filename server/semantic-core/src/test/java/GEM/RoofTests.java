package GEM;

import Models.Inventory.Building.GEM.v2.Occupancy.Occupancy;
import Models.Inventory.Building.GEM.v2.Occupancy.OccupancyDetail;
import Models.Inventory.Building.GEM.v2.Occupancy.OccupancyType;
import Models.Inventory.Building.GEM.v2.Roof.Roof;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RoofTests {
    @Test
    public void serializationTest() {
        // arrange
        Roof roof = new Roof();

        // act
        String taxonomyString = occupancy.getShortTaxonomyString();

        // assert
        assertEquals(taxonomyString, "RES+RES4");
    }

    @Test
    public void deserializationTest() {
        // arrange
        String taxonomyString = "RES+RES4";

        // act
        Occupancy occupancy = Occupancy.deserialize(taxonomyString);

        // assert
        assertEquals(occupancy.occupancyType, OccupancyType.RES);
        assertEquals(occupancy.occupancyDetail, OccupancyDetail.RES4);
    }
}

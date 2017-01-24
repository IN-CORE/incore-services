package GEM;

import Models.Inventory.Building.GEM.v2.Occupancy.Occupancy;
import Models.Inventory.Building.GEM.v2.Occupancy.OccupancyDetail;
import Models.Inventory.Building.GEM.v2.Occupancy.OccupancyType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OccupancyTests {
    @Test
    public void serializationTest() {
        // arrange
        Occupancy occupancy = new Occupancy(OccupancyType.RES, OccupancyDetail.RES4);

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

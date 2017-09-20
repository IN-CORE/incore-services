package Models.Inventory.Building.GEM.v2.Occupancy;

import Models.Inventory.Building.GEM.v2.Common.GemAttribute;
import Models.Inventory.Building.GEM.v2.Common.Utils.ParseUtils;

import java.util.List;

public class Occupancy extends GemAttribute {
    public OccupancyType occupancyType = OccupancyType.OC99;
    public OccupancyDetail occupancyDetail = null;

    public Occupancy() {
    }

    public Occupancy(OccupancyType occupancyType) {
        this.occupancyType = occupancyType;

        if (occupancyType != OccupancyType.OC99 && occupancyType != OccupancyType.OCO) {
            this.occupancyDetail = OccupancyDetail.valueOf(this.occupancyType.getCode() + "99" );
        }
    }

    public Occupancy(OccupancyType occupancyType, OccupancyDetail occupancyDetail) {
        this.occupancyType = occupancyType;
        this.occupancyDetail = occupancyDetail;
    }

    @Override
    public String getTaxonomyStringShort() {
        if (occupancyType.getCode().endsWith("99" )) {
            return "";
        } else if (occupancyType.getCode().endsWith("99" )) {
            return occupancyType.getCode();
        } else {
            return occupancyType.getCode() + "+" + occupancyDetail.getCode();
        }
    }

    @Override
    public String getTaxonomyStringFull() {
        if (occupancyDetail != null) {
            return occupancyType.getCode() + "+" + occupancyDetail.getCode();
        } else {
            return occupancyType.getCode();
        }
    }

    public static Occupancy deserialize(String taxonomyString) {
        List<String> taxonomyTokens = ParseUtils.splitAttribute(taxonomyString);


        if (taxonomyTokens.size() > 0) {
            OccupancyType type = OccupancyType.valueOf(taxonomyTokens.get(0));

            if (taxonomyTokens.size() > 1) {
                OccupancyDetail detail = OccupancyDetail.valueOf(taxonomyTokens.get(1));

                return new Occupancy(type, detail);
            } else {
                return new Occupancy(type);
            }
        } else {
            return new Occupancy();
        }
    }
}

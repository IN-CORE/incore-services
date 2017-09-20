package Models.Inventory.Building.GEM.v2.Occupancy;

import Models.Inventory.Building.GEM.v2.Common.Qualifier;

public enum OccupancyDetail implements Qualifier {
    RES99 ("RES99", "Residential, unknown type"),
    RES1 ("RES1", "Single dwelling", "This includes various dwelling sizes, from a small home to a castle"),
    RES2 ("RES2", "Multi-unit, unknown type"),
    RES2A ("RES2A", "2 Units (duplex)"),
    RES2B ("RES2B", "3-4 Units"),
    RES2C ("RES2C", "5-9 Units"),
    RES2D ("RES2D", "10-19 Units"),
    RES2E ("RES2E", "20-49 Units"),
    RES2F ("RES2F", "50+ Units"),
    RES3 ("RES3", "Temporary lodging"),
    RES4 ("RES4", "Institutional housing"),
    RES5 ("RES5", "Mobile home"),
    COM99 ("COM99", "Commercial and public, unknown type"),
    COM1 ("COM1", "Retail trade"),
    COM2 ("COM2", "Wholesale trade and storage (warehouse)"),
    COM3 ("COM3", "Offices, professional/technical services"),
    COM4 ("COM4", "Hospital/medical clinic"),
    COM5 ("COM5", "Entertainment", "Restaurants, bars, cafes"),
    COM6 ("COM6", "Public building"),
    COM7 ("COM7", "Covered parking garage"),
    COM8 ("COM8", "Bus station"),
    COM9 ("COM9", "Railway station"),
    COM10 ("COM10", "Airport"),
    COM11 ("COM11", "Recreation and leisure", "Smaller sport facilities, leisure centres"),
    MIX99 ("MIX99", "Mixed, unknown type"),
    MIX1 ("MIX1", "Mostly residential and commercial"),
    MIX2 ("MIX2", "Mostly commercial and residential"),
    MIX3 ("MIX3", "Mostly commercial and industrial"),
    MIX4 ("MIX4", "Mostly residential and industrial"),
    MIX5 ("MIX5", "Mostly industrial and commercial"),
    MIX6 ("MIX6", "Mostly industrial and residential"),
    IND99 ("IND99", "Industrial, unknown type"),
    IND1 ("IND1", "Heavy industrial"),
    IND2 ("IND2", "Light industrial"),
    AGR99 ("AGR99", "Agriculture, unknown type"),
    AGR1 ("AGR1", "Produce storage", "It includes grain storage, and also hay, silage, fruit, vegetables, etc."),
    AGR2 ("AGR2", "Animal shelter", "Example: shelter for cows during the winter, but it may not necessarily have to do with the rearing."),
    AGR3 ("AGR3", "Agricultural processing", "This includes abatoirs"),
    ASS99 ("ASS99", "Assembly, unknown type"),
    ASS1 ("ASS1", "Religious gathering"),
    ASS2 ("ASS2", "Arena"),
    ASS3 ("ASS3", "Cinema or concert hall"),
    ASS4 ("ASS4", "Other gatherings", "Clubs, societies, political parties, function centres, etc."),
    GOV99 ("GOV99", "Government, unknown type"),
    GOV1 ("GOV1", "Government, general services"),
    GOV2 ("GOV2", "Government, emergency response"),
    EDU99 ("EDU99", "Education, unknown type"),
    EDU1 ("EDU1", "Pre-school facility"),
    EDU2 ("EDU2", "School"),
    EDU3 ("EDU3", "College/university, offices and/or classrooms"),
    EDU4 ("EDU4", "College/university, research facilities/labs");

    private String code;
    private String description;
    private String longDescription;

    private OccupancyDetail(String code, String description) {
        this.code = code;
        this.description = description;
        this.longDescription = description;
    }

    private OccupancyDetail(String code, String description, String longDescription) {
        this.code = code;
        this.description = description;
        this.longDescription = longDescription;
    }

    public String getCode() {
        return this.code;
    }

    public String getDescription() {
        return this.description;
    }

    public String getLongDescription() {
        return this.longDescription;
    }
}
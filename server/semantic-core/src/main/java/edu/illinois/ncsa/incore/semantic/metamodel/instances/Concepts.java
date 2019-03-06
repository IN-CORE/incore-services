/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.metamodel.instances;

import edu.illinois.ncsa.incore.semantic.metamodel.common.Enumeration;
import edu.illinois.ncsa.incore.semantic.metamodel.common.reference.PdfWebReference;
import edu.illinois.ncsa.incore.semantic.metamodel.common.reference.WebReference;
import edu.illinois.ncsa.incore.semantic.metamodel.concepts.*;
import edu.illinois.ncsa.incore.semantic.units.instances.Dimensions;

import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static edu.illinois.ncsa.incore.semantic.units.instances.SIDerivedUnits.squareMetre;
import static edu.illinois.ncsa.incore.semantic.units.instances.USCustomaryUnits.squareFoot;

public final class Concepts {
    public final static GeneralConcept latitude = new GeneralConcept();
    public final static GeneralConcept width = new GeneralConcept();

    public final static ValueConcept<String> parcelId = new ValueConcept<>();
    public final static ValueConcept<String> buildingId = new ValueConcept<>();
    public final static MeasurementConcept buildingArea = new MeasurementConcept();
    public final static MeasurementConcept buildingAreaGround = new MeasurementConcept();
    public final static ValueConcept<Integer> aboveGroundStories = new ValueConcept<>();
    public final static ValueConcept<Integer> belowGroundStories = new ValueConcept<>();
    public final static ValueConcept<Integer> yearConstructed = new ValueConcept<>();
    public final static ValueConcept<Integer> numberOfStories = new ValueConcept<>();
    public final static EnumerableConcept<String> structureType = new EnumerableConcept<>();
    public final static EnumerableConcept<String> basementType = new EnumerableConcept<>();
    public final static EnumerableConcept<String> occupancyType = new EnumerableConcept<>();
    public final static EnumerableConcept<String> buildingDesignType = new EnumerableConcept<>();
    public final static MonetaryConcept appraisedBuildingValue = new MonetaryConcept();
    public final static MonetaryConcept replacementCost = new MonetaryConcept();
    public final static MonetaryConcept accelerationSensitiveCost = new MonetaryConcept();
    public final static MonetaryConcept displacementSensitiveCost = new MonetaryConcept();
    public final static MonetaryConcept contentValue = new MonetaryConcept();
    public final static MonetaryConcept structuralCost = new MonetaryConcept();

    private Concepts() {}

    static {
        // General Concepts
        latitude.resourceName = "latitude";
        latitude.description = "Latitude is an angle (defined below) which ranges from 0° at the Equator to 90° (North or South) at the poles.";
        // Validation 0 - 90

        // decimal degrees, angles, degree minute second

        // Instead of two derived concepts latitude_dd and latitude_deg, just say it's a dimensional with angle?
        // Latitude Decimal Degrees
        // Latitude Angular

        // --------------------------------

        // Percentage (Ratio) / proportion / Normalized to range [0,1]
        // Percentage (Percentage)

        //
        width.resourceName = "width";
        width.description = "...";
        // Validation > 0

        // Latitude
        // DimensionConcept lat

        // Parcel ID
        parcelId.resourceName = "parcel_id";
        parcelId.resourceNameShort = "parcel_id";
        parcelId.namespace = "building";
        parcelId.reference = new WebReference("https://opensource.ncsa.illinois.edu/confluence/display/ERGO/Building%20Data");
        parcelId.description = "Parcel Identifier";
        parcelId.aliases = Arrays.asList("par_id", "parcel_id", "parid");
        parcelId.typicallyNullable = false;
        parcelId.typicallyUnique = true;

        // Building ID
        buildingId.resourceName = "building_id";
        buildingId.resourceNameShort = "bldg_id";
        buildingId.namespace = "building";
        buildingId.aliases = Arrays.asList("building_identifer", "bld_id");
        buildingId.reference = new WebReference("https://opensource.ncsa.illinois.edu/confluence/display/ERGO/Building%20Data");
        buildingId.description = "Identifier for each building record";
        buildingId.typicallyNullable = false;
        buildingId.typicallyUnique = true;

        // Building Area
        buildingArea.resourceName = "building_area";
        buildingArea.resourceNameShort = "bldg_area";
        buildingArea.namespace = "building";
        buildingArea.reference = new WebReference("https://opensource.ncsa.illinois.edu/confluence/display/ERGO/Building%20Data");
        buildingArea.dimension = Dimensions.area;
        buildingArea.commonUnits = Arrays.asList(squareFoot, squareMetre);
        buildingArea.aliases = Arrays.asList("sq_foot", "square_feet", "square_foot", "squareFoot", "sq_feet");
        buildingArea.description = "Building Area";
        buildingArea.typicallyUnique = false;
        buildingArea.typicallyNullable = true;

        // Ground-level Area of the Building
        buildingAreaGround.resourceName = "building_area_ground";
        buildingAreaGround.resourceNameShort = "grnd_area";
        buildingAreaGround.aliases = Arrays.asList("gsq_feet", "gsq_foot", "gr_bldg_area");
        buildingAreaGround.namespace = "building";
        buildingAreaGround.reference = new WebReference("https://opensource.ncsa.illinois.edu/confluence/display/ERGO/Building%20Data");
        buildingAreaGround.dimension = Dimensions.area;
        buildingAreaGround.commonUnits = Arrays.asList(squareFoot, squareMetre);
        buildingAreaGround.description = "Ground-level Area of the Building";

        // No Stories
        numberOfStories.resourceName = "no_stories";
        numberOfStories.resourceNameShort = "no_stories";
        numberOfStories.namespace = "building";
        numberOfStories.reference = new WebReference("https://opensource.ncsa.illinois.edu/confluence/display/ERGO/Building%20Data");
        numberOfStories.aliases = Arrays.asList("no_stories", "num_stories");
        numberOfStories.description = "Number of Stories";

        // No Stories
        aboveGroundStories.resourceName = "above_ground_stories";
        aboveGroundStories.resourceNameShort = "ag_stories";
        aboveGroundStories.namespace = "building";
        aboveGroundStories.reference = new WebReference("https://opensource.ncsa.illinois.edu/confluence/display/ERGO/Building%20Data");
        aboveGroundStories.aliases = Arrays.asList("a_stories", "astories", "no_a_stories");
        aboveGroundStories.description = "Number of above ground stories";

        // Below Ground Stories
        belowGroundStories.resourceName = "below_ground_stories";
        belowGroundStories.resourceNameShort = "bg_stories";
        belowGroundStories.namespace = "building";
        belowGroundStories.reference = new WebReference("https://opensource.ncsa.illinois.edu/confluence/display/ERGO/Building%20Data");
        belowGroundStories.aliases = Arrays.asList("b_stories", "bstories", "no_b_stories", "no_bg_stories");
        belowGroundStories.description = "Number of below ground stories";

        // Year Built
        yearConstructed.resourceName = "year_constructed";
        yearConstructed.resourceNameShort = "yr_constr";
        yearConstructed.namespace = "building";
        yearConstructed.reference = new WebReference("https://opensource.ncsa.illinois.edu/confluence/display/ERGO/Building%20Data");
        yearConstructed.aliases = Arrays.asList("year_built", "year_blt");
        yearConstructed.description = "Year that the building was constructed";

        // Structure Type
        structureType.resourceName = "structure_type";
        structureType.resourceNameShort = "struct_typ";
        structureType.namespace = "hazus/building";
        structureType.reference = new PdfWebReference("https://www.fema.gov/media-library-data/20130726-1820-25045-6286/hzmh2_1_eq_tm.pdf", 45);
        structureType.description = "Building Structure Type";
        structureType.aliases = Arrays.asList("struct_type", "struct_typ", "structure_type");
        structureType.enumerations = Arrays.asList(
                new Enumeration<>("W1", "Wood, Light Frame (\u2264 5,000 sq. ft.)"),
                new Enumeration<>("W2", "Wood, Commercial and Industrial (> 5,000 sq. ft.)"),
                new Enumeration<>("S1L", "Steel Moment Frame"),
                new Enumeration<>("S1M", "Steel Moment Frame"),
                new Enumeration<>("S1H", "Steel Moment Frame"),
                new Enumeration<>("S2L", "Steel Braced Frame"),
                new Enumeration<>("S2M", "Steel Braced Frame"),
                new Enumeration<>("S2H", "Steel Braced Frame"),
                new Enumeration<>("S3", "Steel Light Frame"),
                new Enumeration<>("S4L", "Steel Frame with Cast-in-Place Concrete Shear Walls"),
                new Enumeration<>("S4M", "Steel Frame with Cast-in-Place Concrete Shear Walls"),
                new Enumeration<>("S4H", "Steel Frame with Cast-in-Place Concrete Shear Walls"),
                new Enumeration<>("S5L", "Steel Frame with Unreinforced Masonry Infill Walls"),
                new Enumeration<>("S5M", "Steel Frame with Unreinforced Masonry Infill Walls"),
                new Enumeration<>("S5H", "Steel Frame with Unreinforced Masonry Infill Walls"),
                new Enumeration<>("C1L", "Concrete Moment Frame"),
                new Enumeration<>("C1M", "Concrete Moment Frame"),
                new Enumeration<>("C1H", "Concrete Moment Frame"),
                new Enumeration<>("C2L", "Concrete Shear Walls"),
                new Enumeration<>("C2M", "Concrete Shear Walls"),
                new Enumeration<>("C2H", "Concrete Shear Walls"),
                new Enumeration<>("C3L", "Concrete Frame with Unreinforced Masonry Infill Walls"),
                new Enumeration<>("C3M", "Concrete Frame with Unreinforced Masonry Infill Walls"),
                new Enumeration<>("C3H", "Concrete Frame with Unreinforced Masonry Infill Walls"),
                new Enumeration<>("PC1", "Precast Concrete Tilt-Up Walls"),
                new Enumeration<>("PC2L", "Precast Concrete Frames with Concrete Shear Walls"),
                new Enumeration<>("PC2M", "Precast Concrete Frames with Concrete Shear Walls"),
                new Enumeration<>("PC2H", "Precast Concrete Frames with Concrete Shear Walls"),
                new Enumeration<>("RM1L", "Reinforced Masonry Bearing Walls with Wood or Metal Deck Diaphragms"),
                new Enumeration<>("RM1M", "Reinforced Masonry Bearing Walls with Wood or Metal Deck Diaphragms"),
                new Enumeration<>("RM2L", "Reinforced Masonry Bearing Walls with Precast Concrete Diaphragms"),
                new Enumeration<>("URML", "Unreinforced Masonry Bearing Walls"),
                new Enumeration<>("URMM", "Unreinforced Masonry Bearing Walls"),
                new Enumeration<>("MH", "Mobile Homes")
        );

        // Occupancy Type
        occupancyType.resourceName = "occupancy_type";
        occupancyType.resourceNameShort = "occ_type";
        occupancyType.namespace = "hazus/building";
        occupancyType.aliases = Arrays.asList("occu_type", "occ_typ", "occupancy");
        occupancyType.reference = new PdfWebReference("https://www.fema.gov/media-library-data/20130726-1820-25045-6286/hzmh2_1_eq_tm.pdf", 46);
        occupancyType.description = "HAZUS MR-3 Building Occupancy Categories";
        occupancyType.enumerations = Arrays.asList(
                new Enumeration<>("RES1", "Residential - Single Family Dwelling (e.g. House)"),
                new Enumeration<>("RES2", "Residential - Mobile Home"),
                new Enumeration<>("RES3", "Residential - Multi-Family Dwelling (e.g. Apartment/Condominium)"),
                new Enumeration<>("RES4", "Residential - Temporary Lodging (e.g. Hotel/Motel)"),
                new Enumeration<>("RES5", "Residential - Institutional Dormitory (e.g. Group Housing (military, college), Jails)"),
                new Enumeration<>("RES6", "Residential - Nursing Home"),
                new Enumeration<>("COM1", "Commercial - Retail Trade (e.g. Store)"),
                new Enumeration<>("COM2", "Commercial - Wholesale Trade (e.g. Warehouse)"),
                new Enumeration<>("COM3", "Commercial - Personal and Repair Services (e.g. Service Station/Shop)"),
                new Enumeration<>("COM4", "Commercial - Professional/Technical Services (e.g. Offices)"),
                new Enumeration<>("COM5", "Commercial - Banks"),
                new Enumeration<>("COM6", "Commercial - Hospitals"),
                new Enumeration<>("COM7", "Commercial - Medical Office/Clinic"),
                new Enumeration<>("COM8", "Commercial - Entertainment & Recreation (e.g. Restaurants/Bars)"),
                new Enumeration<>("COM9", "Commercial - Theaters"),
                new Enumeration<>("COM10", "Commercial - Parking (e.g. Garages)"),
                new Enumeration<>("IND1", "Industrial - Heavy (e.g. Factory)"),
                new Enumeration<>("IND2", "Industrial - Light (e.g. Factory)"),
                new Enumeration<>("IND3", "Industrial - Food/Drugs/Chemical (e.g. Factory)"),
                new Enumeration<>("IND4", "Industrial - Metals/Minerals Processing (e.g. Factory)"),
                new Enumeration<>("IND5", "Industrial - High Technology (e.g. Factory)"),
                new Enumeration<>("IND6", "Industrial - Construction (e.g. Office)"),
                new Enumeration<>("AGR1", "Agriculture"),
                new Enumeration<>("GOV1", "Government - General Services (e.g. Office)"),
                new Enumeration<>("GOV2", "Government - Emergency Response (e.g. Police/Fire Station/EOC)"),
                new Enumeration<>("EDU1", "Education - Grade Schools"),
                new Enumeration<>("EDU2", "Education - Colleges/Universities")
        );

        // Foundation Type
        basementType.resourceName = "basement_type";
        basementType.resourceNameShort = "basemt_typ";
        basementType.namespace = "building";
        basementType.reference = new WebReference("https://opensource.ncsa.illinois.edu/confluence/display/ERGO/Building%20Data");
        basementType.aliases = Arrays.asList("bsmt_type", "bsmt_typ");
        basementType.description = "Specifies the type of basement for the building";
        basementType.enumerations = Arrays.asList(
                new Enumeration<>("COMMERCIAL BSMT", "Commercial Basement"),
                new Enumeration<>("CRAWL=0-24%", "Crawlspace Basement"),
                new Enumeration<>("PART=25-75%", "Part basement"),
                new Enumeration<>("FULL>=75%", "Full basement"),
                new Enumeration<>("SLAB", "Slab Foundation"),
                new Enumeration<>("NONE", "No basement")
        );

        // Appraised Building Value
        appraisedBuildingValue.resourceName = "appr_value";
        appraisedBuildingValue.resourceNameShort = "appr_value";
        appraisedBuildingValue.namespace = "building";
        appraisedBuildingValue.reference = new WebReference("https://opensource.ncsa.illinois.edu/confluence/display/ERGO/Building%20Data");
        appraisedBuildingValue.aliases = Arrays.asList("appr_bldg", "appraised_bldg", "appr_val");
        appraisedBuildingValue.description = "Appraised value for the building";
        appraisedBuildingValue.commonCurrencies = Arrays.asList(Currency.getInstance("USD"), Currency.getInstance("CAD"));

        replacementCost.resourceName = "replacement_cost";
        replacementCost.resourceNameShort = "repl_cost";
        replacementCost.description = "Replacement cost for the building";
        replacementCost.namespace = "building";
        replacementCost.reference = new PdfWebReference("https://www.fema.gov/media-library-data/20130726-1820-25045-6286/hzmh2_1_eq_tm.pdf", 59);
        replacementCost.aliases = Arrays.asList("repl_cst");
        replacementCost.commonCurrencies = Arrays.asList(Currency.getInstance("USD"), Currency.getInstance("CAD"));

        accelerationSensitiveCost.resourceName = "acceleration_sensitive_nonstructural_replacement_cost";
        accelerationSensitiveCost.resourceNameShort = "as_repl_cst";
        accelerationSensitiveCost.description = "Acceleration-sensitive Non-structural Component of the Replacement cost for the building";
        accelerationSensitiveCost.namespace = "building";
        accelerationSensitiveCost.reference = new PdfWebReference("https://www.fema.gov/media-library-data/20130726-1820-25045-6286/hzmh2_1_eq_tm.pdf", 59);
        accelerationSensitiveCost.aliases = Arrays.asList("cnsa", "acc_repl_cst", "nstra_cst");
        accelerationSensitiveCost.commonCurrencies = Arrays.asList(Currency.getInstance("USD"), Currency.getInstance("CAD"));

        displacementSensitiveCost.resourceName = "drift_sensitive_nonstructural_replacement_cost";
        displacementSensitiveCost.resourceNameShort = "ds_repl_cst";
        displacementSensitiveCost.description = "Drift-sensitive Non-structural Component of the Replacement cost for the building";
        displacementSensitiveCost.namespace = "building";
        displacementSensitiveCost.reference = new PdfWebReference("https://www.fema.gov/media-library-data/20130726-1820-25045-6286/hzmh2_1_eq_tm.pdf", 59);
        displacementSensitiveCost.aliases = Arrays.asList("cnsd", "nstrd_cst");
        displacementSensitiveCost.commonCurrencies = Arrays.asList(Currency.getInstance("USD"), Currency.getInstance("CAD"));

        contentValue.resourceName = "content_replacement_cost";
        contentValue.resourceNameShort = "cont_cost";
        contentValue.description = "Replacement value for contents in the building";
        contentValue.namespace = "building";
        contentValue.reference = new PdfWebReference("https://www.fema.gov/media-library-data/20130726-1820-25045-6286/hzmh2_1_eq_tm.pdf", 70);
        contentValue.aliases = Arrays.asList("crv", "content_replacement_value", "con_repl_val", "cont_val");
        contentValue.commonCurrencies = Arrays.asList(Currency.getInstance("USD"), Currency.getInstance("CAD"));

        structuralCost.resourceName = "structural_replacement_cost";
        structuralCost.resourceNameShort = "struct_cst";
        structuralCost.description = "Replacement cost for the Structural Component of the building";
        structuralCost.namespace = "building";
        structuralCost.reference = new PdfWebReference("https://www.fema.gov/media-library-data/20130726-1820-25045-6286/hzmh2_1_eq_tm.pdf", 59);
        structuralCost.aliases = Arrays.asList("src", "srv", "structural_replacement_value", "str_val", "str_cst");
        structuralCost.commonCurrencies = Arrays.asList(Currency.getInstance("USD"), Currency.getInstance("CAD"));

        buildingDesignType.resourceName = "building_design_type";
        buildingDesignType.resourceNameShort = "bldg_dsgn";
        buildingDesignType.description = "HAZUS MR-3 Building Design Code";
        buildingDesignType.namespace = "hazus/building";
        buildingDesignType.aliases = Arrays.asList("dsgn_level", "design_level", "dsgn_lvl", "design_lvl", "bld_typ", "build_type", "bldg_type", "dgn_lvl");
        buildingDesignType.reference = new PdfWebReference("https://www.fema.gov/media-library-data/20130726-1820-25045-6286/hzmh2_1_eq_tm.pdf", 168);
        buildingDesignType.namespace = "building";
        buildingDesignType.enumerations = Arrays.asList(
            new Enumeration<>("High - Code", "High Strength, High Ductility"),
            new Enumeration<>("Moderate - Code", "Moderate Strength, Moderate Ductility"),
            new Enumeration<>("Low - Code", "Low Strength, Low Ductility"),
            new Enumeration<>("Pre - Code", "Minimal Strength, Minimal Ductility")
        );
    }

    public static List<Concept> All = Arrays.asList(buildingArea, numberOfStories, structureType, appraisedBuildingValue, basementType,
                                                    aboveGroundStories, belowGroundStories, buildingAreaGround, occupancyType, yearConstructed,
                                                    parcelId, buildingId, replacementCost, contentValue, accelerationSensitiveCost,
                                                    displacementSensitiveCost, buildingDesignType, structuralCost);

    public static Optional<Concept> getByName(String name) {
        return All.stream()
                  .filter(concept -> concept.getResourceName().equals(name) || concept.getResourceNameShort().equals(name))
                  .findFirst();
    }

    public static Optional<Concept> getByNameOrAlias(String name) {
        Optional<Concept> result = getByName(name);

        if (result.isPresent()) {
            return result;
        } else {
            return All.stream()
                      .filter(concept -> concept.getAliases().contains(name))
                      .findFirst();
        }
    }
}

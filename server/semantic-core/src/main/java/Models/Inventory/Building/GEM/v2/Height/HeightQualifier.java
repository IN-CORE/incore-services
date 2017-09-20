package Models.Inventory.Building.GEM.v2.Height;

import Models.Inventory.Building.GEM.v2.Common.Qualifier;

public class HeightQualifier extends Qualifier {
    public static HeightQualifier H99 = new HeightQualifier("H99", "Unknown"); // Above Ground
    public static HeightQualifier HB99 = new HeightQualifier("HB99", "Unknown"); // Below Ground
    public static HeightQualifier HF99 = new HeightQualifier("HF99", "Unknown"); // Height of Ground Floor level above grade

    public static HeightQualifier HBET = new HeightQualifier("HBET", "Between"); // Above Ground
    public static HeightQualifier HBBET = new HeightQualifier("HBBET", "Between"); // Below Ground
    public static HeightQualifier HFBET = new HeightQualifier("HFBET", "Between"); // Height of Ground Floor level above grade

    public static HeightQualifier HEX = new HeightQualifier("HEX", "Exactly"); // Above Ground
    public static HeightQualifier HBEX = new HeightQualifier("HEX", "Exactly");  // Below Ground
    public static HeightQualifier HFEX = new HeightQualifier("HEX", "Exactly"); // Height of Ground Floor level above grade

    public static HeightQualifier HAPP = new HeightQualifier("HAPP", "Approximately"); // Above Ground
    public static HeightQualifier HBAPP = new HeightQualifier("HBAPP", "Approximately"); // Below Ground
    public static HeightQualifier HFAPP = new HeightQualifier("HFAPP", "Approximately"); // Height of Ground Floor level above grade

    private HeightQualifier(String code, String description, String shortDescription) {
        super(code, description, shortDescription);
    }

    private HeightQualifier(String code, String description) {
        super(code, description);
    }
}

package SemanticModels.Units.Utils;

public class StringRepresentationUtil {
    public static String getPowerRepresentationUnicode(String strInteger) {
        String superScript = strInteger;

        superScript = superScript.replaceAll("\\+" , "\u207A" );
        superScript = superScript.replaceAll("-" , "\u207B" );
        superScript = superScript.replaceAll("0" , "\u2070" );
        superScript = superScript.replaceAll("1" , "\u00B9" );
        superScript = superScript.replaceAll("2" , "\u00B2" );
        superScript = superScript.replaceAll("3" , "\u00B3" );
        superScript = superScript.replaceAll("4" , "\u2074" );
        superScript = superScript.replaceAll("5" , "\u2075" );
        superScript = superScript.replaceAll("6" , "\u2076" );
        superScript = superScript.replaceAll("7" , "\u2077" );
        superScript = superScript.replaceAll("8" , "\u2078" );
        superScript = superScript.replaceAll("9" , "\u2079" );

        return superScript;
    }

    public static String getPowerRepresentationUnicode(int integer) {
        String strValue = Integer.toString(integer);
        return getPowerRepresentationUnicode(strValue);
    }

    // NIST Guide to the SI Chapter 9.6
    public static String getRaisedPowerName(String name, int power) {
        switch (power) {
            case 1:
                return name;
            case 2:
                return "square " + name;
            case 3:
                return "cubic " + name;
            case 4:
                return "quartic " + name;
            case 5:
                return "quintic " + name;
            case 6:
                return "sextic " + name;
            case 7:
                return "heptic " + name;
            case 8:
                return "octic " + name;
            default:
                return name + " to the power " + Integer.toString(power);
        }
    }

    // NIST Guide to the SI Chapter 9.6
    public static String getRaisedPowerPlural(String pluralName, int power) {
        switch (power) {
            case 1:
                return pluralName;
            case 2:
                return pluralName + " squared";
            case 3:
                return pluralName + " cubed";
            default:
                return pluralName + " to the power " + Integer.toString(power);
        }
    }
}

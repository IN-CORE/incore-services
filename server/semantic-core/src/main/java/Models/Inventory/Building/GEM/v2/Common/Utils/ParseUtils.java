package Models.Inventory.Building.GEM.v2.Common.Utils;

import com.sun.deploy.util.StringUtils;

import java.util.Arrays;
import java.util.List;

public class ParseUtils {
    public static List<String> splitAttribute(String taxonomyString) {
        List<String> taxonomyTokens = Arrays.asList(StringUtils.splitString(taxonomyString, "+"));

        return taxonomyTokens;
    }
}

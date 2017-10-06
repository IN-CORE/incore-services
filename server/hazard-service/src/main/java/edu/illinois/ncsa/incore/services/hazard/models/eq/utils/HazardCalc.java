package edu.illinois.ncsa.incore.services.hazard.models.eq.utils;

import edu.illinois.ncsa.incore.services.hazard.models.eq.ScenarioEarthquake;
import edu.illinois.ncsa.incore.services.hazard.models.eq.Site;
import edu.illinois.ncsa.incore.services.hazard.models.eq.attenuations.AtkinsonBoore1995;
import edu.illinois.ncsa.incore.services.hazard.models.eq.attenuations.BaseAttenuation;
import edu.illinois.ncsa.incore.services.hazard.models.eq.site.NEHRPSiteAmplification;
import edu.illinois.ncsa.incore.services.hazard.models.eq.site.SiteAmplification;
import edu.illinois.ncsa.incore.services.hazard.models.eq.types.SeismicHazardResult;

import java.util.Iterator;
import java.util.Map;

public class HazardCalc {

    public static SeismicHazardResult getGroundMotionAtSite(ScenarioEarthquake earthquake, Map<BaseAttenuation, Double> attenuations, Site site, String hazardType, String demand, int spectrumOverride, boolean amplifyHazard) throws Exception {
        Iterator<BaseAttenuation> iterator = attenuations.keySet().iterator();

        double hazardValue = 0.0;
        String closestHazardPeriod = hazardType;
        while (iterator.hasNext()) {
            BaseAttenuation model = iterator.next();
            double weight = attenuations.get(model);
            SeismicHazardResult matchedResult = model.getValueClosestMatch(hazardType, site);
            hazardValue += (Math.log(matchedResult.getHazardValue()) * weight);

            closestHazardPeriod = matchedResult.getPeriod();
        }

        hazardValue = Math.exp(hazardValue);

        // TODO check if site class dataset is defined for amplifying hazard
        int siteClass = HazardUtil.getSiteClassAsInt(earthquake.getDefaultSiteClass());

        SiteAmplification siteAmplification = null;
        if (amplifyHazard) {
            System.out.println("amplifying hazard");
            // TODO need to add check for if VS already accounted for soil type

            // TODO add check for Rix Fernandez, no need to amplify

            // TODO Add support for other amplification methods besides NEHRP

            if (earthquake.getSiteAmplification().equalsIgnoreCase("NEHRP")) {
                siteAmplification = new NEHRPSiteAmplification();

                if (closestHazardPeriod.equalsIgnoreCase(HazardUtil.PGV)) {
                    double pga = getGroundMotionAtSite(earthquake, attenuations, site, "PGA", demand, spectrumOverride, false).getHazardValue();
                    hazardValue *= siteAmplification.getSiteAmplification(site, pga, siteClass, closestHazardPeriod);
                } else {
                    // Note, hazard value input should be PGA if amplifying PGV hazard because NEHRP uses PGA coefficients for amplifying PGV
                    // and the range for interpretation is in units of g
                    hazardValue *= siteAmplification.getSiteAmplification(site, hazardValue, siteClass, hazardType);
                }
            }

        }

        return new SeismicHazardResult(hazardValue, closestHazardPeriod, demand);
    }
}

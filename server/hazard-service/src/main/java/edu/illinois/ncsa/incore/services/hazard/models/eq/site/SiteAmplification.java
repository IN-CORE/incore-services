package edu.illinois.ncsa.incore.services.hazard.models.eq.site;

import edu.illinois.ncsa.incore.services.hazard.models.eq.Site;

public abstract class SiteAmplification {

    public abstract double getSiteAmplification(Site site, double hazardValue, int siteClass, String period);

}



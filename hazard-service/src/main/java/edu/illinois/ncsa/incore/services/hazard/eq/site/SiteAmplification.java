package edu.illinois.ncsa.incore.services.hazard.eq.site;

import edu.illinois.ncsa.incore.services.hazard.eq.Site;

public abstract class SiteAmplification {

    public abstract double getSiteAmplification(Site site, double hazardValue, int siteClass, String period);

}



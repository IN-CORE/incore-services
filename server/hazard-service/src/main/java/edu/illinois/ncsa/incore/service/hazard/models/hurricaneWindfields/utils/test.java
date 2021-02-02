package edu.illinois.ncsa.incore.service.hazard.models.hurricaneWindfields.utils;

import org.geotools.referencing.crs.DefaultGeographicCRS;

public class test
{
    public static void main( String[] args )
    {
        System.out.println( "Start!" );
        test();
    }

    public static final void test()
    {
        DefaultGeographicCRS crs = DefaultGeographicCRS.WGS84;
        System.out.println(crs);
    }
}

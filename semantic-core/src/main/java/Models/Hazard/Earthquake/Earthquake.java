package BeanModels.Hazard.Earthquake;

import BeanModels.Hazard.Earthquake.Attenuation.Attenuation;
import BeanModels.Hazard.Hazard;

public class Earthquake extends Hazard {
    public Point epicenter;
    public double magnitude;
    public Attenuation attenuation;
}

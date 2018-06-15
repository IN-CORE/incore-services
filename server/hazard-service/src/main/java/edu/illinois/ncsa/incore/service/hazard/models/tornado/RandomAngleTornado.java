package edu.illinois.ncsa.incore.service.hazard.models.tornado;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.utils.TornadoUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomAngleTornado extends Tornado {

    @Override
    public boolean requiresEndPoint(){
        return true;
    }

    @Override
    public void createTornado(TornadoParameters tornadoParameters){
        // generate random
        int randomSeed = tornadoParameters.getRandomSeed();
        Random randomAngleGenerator = new Random(randomSeed);

        String efRating = tornadoParameters.getEfRating();

        double meanWidth = TornadoUtils.computeMeanWidth(efRating);
        tornadoWidths = new ArrayList<Double>();
        tornadoWidths.add(meanWidth);

        double meanDistance = TornadoUtils.computeMeanLength(efRating);
        double meanAngle = TornadoUtils.computeMeanAngle(efRating);
        double stdDevAngle = TornadoUtils.computeAngleStandardDeviation(efRating);

        double randomAngle = 0;

        for (int i = 0; i < tornadoParameters.getNumSimulations(); i++){
            // Get a random angle following the normal distribution
            randomAngle = (randomAngleGenerator.nextGaussian() * stdDevAngle) + meanAngle;
            double normalizedAngle = (randomAngle + 360) % 360;

            Coordinate startPtCoordinate = new Coordinate(tornadoParameters.getStartLongitude(),
                tornadoParameters.getStartLatitude());
            Coordinate endPtCoordinate = TornadoUtils.calculateDestination(startPtCoordinate, normalizedAngle, meanDistance);

            this.efBoxes.add(this.computeTornadoEFBoxWidths(startPtCoordinate, endPtCoordinate, meanWidth, efRating));

            // put the caclulated endpoint back to tornadoParameters
            List<Double> endLongitude = new ArrayList<Double>();
            List<Double> endLatitude = new ArrayList<Double>();
            endLongitude.add(endPtCoordinate.x);
            endLatitude.add(endPtCoordinate.y);

            tornadoParameters.setEndLongitude(endLongitude);
            tornadoParameters.setEndLatitude(endLatitude);
        }
    }

}

package frc.robot.lib;

import static org.wpilib.units.Units.Inches;
import static org.wpilib.units.Units.RPM;

import java.util.Set;

import org.wpilib.math.interpolation.Interpolator;
import org.wpilib.units.measure.Distance;
import frc.robot.Constants.JoeLookupTableConstants;
import frc.robot.Constants.JoeLookupTableConstants.LookupTablePoint;

public class JoeLookupTable {
    /** @param distance distance away from hub 
     * @return the corresponding ShotData object (time and angle) from linearly interpolating the best 2 joeLookupTable points
    */
    public static LookupTablePoint getLookupTablePoint(Distance distance){
        
        Distance bestFitDistance = Inches.of(Double.MAX_VALUE);
        Distance secondBestFitDistance = Inches.of(Double.MAX_VALUE);

        //get keys from table (A Java Map)
        Set<Distance> keys = JoeLookupTableConstants.joeLookupTable.keySet();

        //loop through each key
        for(Distance currentDistance : keys){
            //find best fit and second best fit distances
            if(currentDistance.minus(distance).abs(Inches) < bestFitDistance.minus(distance).abs(Inches)){

                secondBestFitDistance = bestFitDistance;
                bestFitDistance = currentDistance;
            }
            else if(currentDistance.minus(distance).abs(Inches) < secondBestFitDistance.minus(distance).abs(Inches)){

                secondBestFitDistance = currentDistance;
            }
        }
        //get avs as doubles from 2 closest points
        LookupTablePoint bestFitPoint = JoeLookupTableConstants.joeLookupTable.get(bestFitDistance);
        LookupTablePoint secondBestFitPoint = JoeLookupTableConstants.joeLookupTable.get(secondBestFitDistance);

        double bestFitAV = bestFitPoint.angularVelocity().in(RPM);
        double secondBestFitAV = secondBestFitPoint.angularVelocity().in(RPM);

        double bestFitEfficiency = bestFitPoint.efficiency();
        double secondBestFitEfficiency = secondBestFitPoint.efficiency();

        //get the interpolation point
        double linearInterpolation = 
            distance.minus(bestFitDistance).abs(Inches) / 
            (bestFitDistance.minus(distance).abs(Inches) + secondBestFitDistance.minus(distance).abs(Inches));

        //return a new ShotData object with the interpolated time and angle.
        return new LookupTablePoint(
            RPM.of(Interpolator.forDouble().interpolate(bestFitAV, secondBestFitAV, linearInterpolation)),
            Interpolator.forDouble().interpolate(bestFitEfficiency, secondBestFitEfficiency, linearInterpolation));
    }
}
package frc.lib;

import static org.wpilib.units.Units.Meters;
import static org.wpilib.units.Units.Radians;
import static org.wpilib.units.Units.RadiansPerSecond;

import org.wpilib.driverstation.Alliance;
import org.wpilib.driverstation.MatchState;
import org.wpilib.framework.RobotBase;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.math.geometry.Transform2d;
import org.wpilib.math.geometry.Translation2d;
import org.wpilib.math.geometry.Translation3d;
import org.wpilib.math.util.Units;
import org.wpilib.smartdashboard.SmartDashboard;
import org.wpilib.system.Timer;
import org.wpilib.units.measure.Angle;
import org.wpilib.units.measure.AngularVelocity;

import frc.robot.Constants.FieldConstants;
import frc.robot.Constants.JoeLookupTableConstants.LookupTablePoint;
import frc.robot.Constants.ShooterConstants;
import frc.robot.lib.JoeLookupTable;
import frc.robot.subsystems.Drivetrain;
import frc.robot.subsystems.Shooter;

public class AutoAimCalculator {

    private final Drivetrain drivetrain;
    private final Shooter shooter;

    private Pose2d adjustedRobotPose, targetPose;
    private Angle desiredHoodAngle, desiredRobotAngle;
    private AngularVelocity desiredShooterAngularVelocity;
    private double efficiency = 1;
    private final double height = FieldConstants.hubZ - Units.inchesToMeters(20);

    //sim
    double lastShootTime = Timer.getTimestamp();

    public AutoAimCalculator(Drivetrain drivetrain, Shooter shooter){
        this.drivetrain = drivetrain;
        this.shooter = shooter;
        adjustedRobotPose = drivetrain.getState().Pose;
        targetPose = FieldConstants.hubLocations.get(MatchState.getAlliance().orElse(Alliance.BLUE));
        //calculate once when constructed to initialize desiredHoodAngle, desiredRobotAngle, and desiredShooterAngularVelocity
        calculate();
    }

    public void calculate(){
        desiredHoodAngle = getDesiredHoodPitch(shooter.getFuelSpeedWithCustomEfficiency(efficiency));

        for (int i = 0; i < 2; i++) {
            // get desired angular velocity and efficiency from lookup table
            LookupTablePoint lookupTablePoint = JoeLookupTable.getLookupTablePoint(Meters.of(getDistanceFromHub()));
            desiredShooterAngularVelocity = lookupTablePoint.angularVelocity();
            efficiency = lookupTablePoint.efficiency();

            double shooterLaunchVelocity = shooter.getFuelSpeedWithCustomEfficiency(efficiency);

            // calculate TOF(used for calculating adjusted robot pose)
            double timeOfFlight = getTimeOfFlight(desiredHoodAngle,
                    shooterLaunchVelocity);
            if (RobotBase.isSimulation()) {
                timeOfFlight = getTimeOfFlight(desiredHoodAngle, getSimLaunchVelocity(desiredShooterAngularVelocity));
            }
            // calculate the distance traveled by the robot during the time of flight
            Transform2d adjustedRobotPoseTransform = new Transform2d(
                    //drivetrain.getState().Velocity.vx * timeOfFlight,
                    drivetrain.getState().Velocity.vx * timeOfFlight,
                    drivetrain.getState().Velocity.vy * timeOfFlight,
                    new Rotation2d());
            // add the distance traveled during TOF to current robot pose to get the
            // adjusted robot pose
            // this will be used for shooting while moving adjustment
            adjustedRobotPose = drivetrain.getState().Pose.plus(adjustedRobotPoseTransform);

            // recalculate desired hood angle with new adjustedPose (converges)
            desiredHoodAngle = getDesiredHoodPitch(shooterLaunchVelocity);

            // // send adjusted robot pose to advantageScope(for sim testing)
            // adjustedRobotPosePublisher.set(adjustedRobotPose);
        }

        // get desired angular velocity and efficiency from lookup table
        LookupTablePoint lookupTablePoint = JoeLookupTable.getLookupTablePoint(Meters.of(getDistanceFromHub()));
        desiredShooterAngularVelocity = lookupTablePoint.angularVelocity();
        efficiency = lookupTablePoint.efficiency();

        desiredRobotAngle = targetPose.getTranslation().minus(adjustedRobotPose.getTranslation()).getAngle().getMeasure();
    }

    public Angle getDesiredRobotAngle() {
        return desiredRobotAngle;
    }

    public Angle getDesiredHoodAngle() {
        return desiredHoodAngle;
    }

    public AngularVelocity getDesiredShooterAngularVelocity(){
        return desiredShooterAngularVelocity;
    }

    /**
     * @return the desired pitch for the hood based on the adjusted robot position
     */
    private Angle getDesiredHoodPitch(double launchVelocity) {
        // distance from robot to target
        Translation2d robotTranslation = adjustedRobotPose.getTranslation();

        double distance = robotTranslation.getDistance(targetPose.getTranslation());

        if (RobotBase.isSimulation()) {
            launchVelocity = getSimLaunchVelocity(
                JoeLookupTable.getLookupTablePoint(Meters.of(adjustedRobotPose.getTranslation().getDistance(targetPose.getTranslation()))).angularVelocity());
        }
        
        double desiredPitch = Math.atan(
            (Math.pow(launchVelocity, 2)
                + Math.sqrt(Math.pow(launchVelocity, 4)
                    - Math.pow(FieldConstants.g * distance, 2)
                        - 2 * FieldConstants.g * height * Math.pow(launchVelocity, 2)))
            / (FieldConstants.g * distance)
        );

        if (Double.isNaN(desiredPitch)) {
            // equation can only return angles from 45-90 deg (in radians of course),
            // anything lower than that will be NaN
            // the minimum possible hood angle on the physical shooter is 45, so no
            // additional calculation is needed, just set to 45
            desiredPitch = Units.degreesToRadians(45);
            // autoAimStatus = AutoAimStatus.OUTOFRANGE;
        }

        if (desiredPitch > ShooterConstants.maxPitch.in(Radians)) {

            desiredPitch = ShooterConstants.maxPitch.in(Radians);
        }
        
        SmartDashboard.putNumber("autoaim desired pitch", Units.radiansToDegrees(desiredPitch));
        return Radians.of(desiredPitch);
    }
    

    /**
     * 
     * @param hoodPitch      what angle the ball is shot at from the horizontal
     * @param launchVelocity the velocity the ball will be shot at in m/s
     * @return the time of flight to the hub in seconds
     */
    private double getTimeOfFlight(Angle hoodPitch, double launchVelocity) {
        // initial y component of launch velocity
        double vy = launchVelocity * Math.sin(hoodPitch.in(Radians));
        // the calculation is based on delta y = vy * TOF - (1/2)g * TOF^2 (where g is a
        // positive constant)
        // the delta y for TOF would be the height
        // the equation then becomes 0 = -(1/2)g * TOF^2 + vy * TOF - height -> 0 =
        // (1/2)g * TOF^2 - vy * TOF + height
        // then use quadratic formula and always add the radical to get the 2nd time the
        // fuel is at the target height (so that it is on the way down)
        double radical = Math.sqrt(Math.pow(vy, 2) - 2 * FieldConstants.g * height);
        if (Double.isNaN(radical)) {
            return 0;
        }
        double numerator = vy + radical;
        double time = numerator / FieldConstants.g;
        SmartDashboard.putNumber("time of flight", time);
        return time;
    }

    /** Units: meters */
    private double getDistanceFromHub() {
        return adjustedRobotPose.getTranslation().getDistance(targetPose.getTranslation());
    }

    //sim
    public void simulate(){
        double robotRelativeBallVelocityHorizontal = getSimLaunchVelocity(desiredShooterAngularVelocity)
                * Math.cos(desiredHoodAngle.in(Radians));
        double robotRelativeBallVelocityVertical = getSimLaunchVelocity(desiredShooterAngularVelocity)
                * Math.sin(desiredHoodAngle.in(Radians));

        // AdvantageScope fuel simulation
        // get the current robot yaw angle
        double robotYaw = drivetrain.getState().Pose.getRotation().getRadians();

        // calculate field relative initial fuel velocities
        double vx = robotRelativeBallVelocityHorizontal * Math.cos(desiredRobotAngle.in(Radians))
                + drivetrain.getState().Velocity.vx * Math.cos(robotYaw)
                - drivetrain.getState().Velocity.vy * Math.sin(robotYaw);

        double vy = robotRelativeBallVelocityHorizontal * Math.sin(desiredRobotAngle.in(Radians))
                + drivetrain.getState().Velocity.vx * Math.sin(robotYaw)
                + drivetrain.getState().Velocity.vy * Math.cos(robotYaw);

        double vz = robotRelativeBallVelocityVertical;

        //MathSharedStore.getTimestamp() - lastShoot <= 1.0 / 10.0
        if (Timer.getTimestamp() - lastShootTime > 0.1){
            sim_shootFuel(vx, vy, vz);
            lastShootTime = Timer.getTimestamp();
        }
    }

    /**
     * AdvantageScope fuel shooting simulation
     * 
     * @param vx initial field relative fuel velocity x component
     * @param vy initial field relative fuel velocity y component
     * @param vz initial field relative fuel velocity z component (FuelSimulation
     *           class will simulate gravity)
     */
    private void sim_shootFuel(double vx, double vy, double vz) {
        if (!RobotBase.isSimulation()) {
            return;
        }
        FuelSimulation.getInstance().shootFuel(
            new Translation3d(drivetrain.getState().Pose.getTranslation().getX(),
                    drivetrain.getState().Pose.getTranslation().getY(), 0),
            new Translation3d(vx, vy, vz),
            new Translation3d(0, 0, 0));
    }

    private static double getSimLaunchVelocity(AngularVelocity desiredMotorVelocity) {
        double shooterOmega = desiredMotorVelocity.in(RadiansPerSecond) * ShooterConstants.ratio;

        double wheelTangentialSpeed = shooterOmega * ShooterConstants.kShooterWheelRadius.in(Meters);
        double rollerTangentialSpeed = shooterOmega * ShooterConstants.kShooterRollerRadius.in(Meters);

        return ShooterConstants.kShooterEfficiency * (wheelTangentialSpeed + rollerTangentialSpeed) / 2;
    }

}

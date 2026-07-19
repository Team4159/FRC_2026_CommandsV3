package frc.robot.mechanisms;

import static org.wpilib.units.Units.Degrees;

import java.util.function.Supplier;

import org.wpilib.command3.Command;
import org.wpilib.command3.Coroutine;
import org.wpilib.command3.Mechanism;
import org.wpilib.math.util.Units;
import org.wpilib.units.measure.Angle;

import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.controls.MotionMagicVoltage;
import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.hardware.TalonFX;

import frc.robot.Constants.HoodConstants;

public class Hood extends Mechanism{

    private final TalonFX hoodMotor;
    //the CANCoder on the hood
    private final CANcoder hoodCanCoder;

    //Motion Magic® is a CTRE control request that is a profiled PID controller that takes a max velocity, acceleration, and jerk into account for smoother motion control, works very well for pivots
    private final MotionMagicVoltage hoodMotionMagic;

    //the current manual angle setpoint in degrees
    private double manualAngle = 5;

    public Hood(){
        hoodMotor = new TalonFX(HoodConstants.HoodId, CANBus.systemcore(1));
        hoodCanCoder = new CANcoder(HoodConstants.kHoodEncoderID, CANBus.systemcore(1));

        //apply the configs
        hoodCanCoder.getConfigurator().apply(HoodConstants.canCoderConfig);
        hoodMotor.getConfigurator().apply(HoodConstants.hoodConfig);

        hoodMotionMagic = new MotionMagicVoltage(0);
    }

        /** @param desiredAngle set the deisred angle of the hood*/
    public void adjustHood(Angle desiredAngle) {
        //set the hood motor control to the motion magic with a desired position that is the desired angle
        hoodMotor.setControl(hoodMotionMagic.withPosition(desiredAngle));    
    }

    /** @param trajectoryAngle the desired launch angle of the fuel
     * adjusts the hood such to achieve the desired fuel launch angle
     */
    public void adjustTrajectoryAngle(Angle trajectoryAngle) {
        //adjusthood is in terms of shooter angle where the angle of the shooter COM with respect to the horizontal is 0, to get this from trajectory angle must get the complement of the trajectory angle
        //subtract the hood offset which is the angle between the hood COM and the final hood roller
        adjustHood(Degrees.of(90).minus(trajectoryAngle).minus(HoodConstants.kHoodAngleOffset));
    }
    
    /** sets the desired angle of the hood to the resting angle(fits under the trench) */
    public void restHood() {
        adjustHood(HoodConstants.kRestingAngle);
    }

    /** @return true if the hood is at the right pitch within tolerance, false otherwise */
    public boolean isAtPitch() {
        //5 degrees of tolerance should be a constant instead
        return hoodMotor.getClosedLoopReference().isNear(hoodMotor.getPosition().getValueAsDouble(), Units.degreesToRotations(5));
    }

    /** @param adjustment how much to adjust by in degrees */
    public void manualHood(double adjustment){
        //make sure the hood setpoint stays within its bounded range
        double targetAngle = Math.max(5, Math.min(manualAngle + adjustment, 45));
        //store the setpoint in the manualAngle member variable
        manualAngle = targetAngle;
        //adjust the hood to the manual setpoint
        adjustHood(Degrees.of(targetAngle));
    }

    public Command ManualHoodUp(){
        return run(coroutine -> {
            manualHood(-5);
            coroutine.park();
        })
        .named(getName() + "manualHoodPitchUp");
    }

    public Command ManualHoodDown(){
        return run(coroutine -> {
            manualHood(5);
            coroutine.park();
        })
        .named(getName() + "manualHoodPitchDown");
    }

    public Command changeTrajectoryAngle(Angle targetAngle){
        return run(coroutine -> {
            adjustTrajectoryAngle(targetAngle);
            coroutine.park();
        })
        .named(getName() + " changeTrajectoryAngle: " + targetAngle.in(Degrees));
    }

    public Command changeTrajectoryAngle(Supplier<Angle> targetAngleSupplier){
        return runRepeatedly(() -> {
            adjustTrajectoryAngle(targetAngleSupplier.get());
        })
        .named(getName() + " changeTrajectoryAngle: supplier");
    }

    @Override
    public Command idle(){
        return run(coroutine -> {
            restHood();
            coroutine.park();
        })
        .withPriority(Command.LOWEST_PRIORITY)
        .named(getName() + "[IDLE]");
    }
}

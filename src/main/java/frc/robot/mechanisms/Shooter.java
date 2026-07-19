package frc.robot.mechanisms;

import static org.wpilib.units.Units.Amps;
import static org.wpilib.units.Units.Degrees;
import static org.wpilib.units.Units.Meters;
import static org.wpilib.units.Units.RPM;
import static org.wpilib.units.Units.RadiansPerSecond;
import static org.wpilib.units.Units.RotationsPerSecond;

import java.util.function.Supplier;

import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;

import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.MotorOutputConfigs;
import com.ctre.phoenix6.controls.MotionMagicVoltage;
import com.ctre.phoenix6.controls.VelocityVoltage;
import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;

import org.wpilib.math.util.Units;
import org.wpilib.units.measure.Angle;
import org.wpilib.units.measure.AngularVelocity;
import frc.robot.Constants;
import frc.robot.Constants.ShooterConstants;

public class Shooter extends Mechanism{
    //all TalonFX motors on the shooter (hood and feeder are X44, shooter motors are X60 but in code all TalonFX motors (Falcon, Kraken x44 and x60) all behave the same)
    private final TalonFX 
        leftBottomShooterMotor, 
        leftTopShooterMotor, 
        rightTopShooterMotor, 
        rightBottomShooterMotor;
    //this was cooked for some reason never got a chance to figure out why so instead we just set each motor individually instead of using the leader/follower system
    //private final TalonFX leaderShooterMotor;

    //Phoenix control requests
    //this allows for control systems to be run on the motor controllers (less work for roborio) because CTRE is actually good at this (unlike REV, for REV motors just use a WPILIB PID)

    //velocity voltage is just a regular voltage based PID for a velocity setpoint
    private final VelocityVoltage shooterVelocityVoltage;

    public Shooter() {
        //initialize motors and CANCoder using the CANIDs in constants
        leftBottomShooterMotor = new TalonFX(ShooterConstants.ShooterIDLeftBottom, CANBus.systemcore(1));
        leftTopShooterMotor = new TalonFX(ShooterConstants.ShooterIDLeftTop, CANBus.systemcore(1));
        rightTopShooterMotor = new TalonFX(ShooterConstants.ShooterIDRightTop, CANBus.systemcore(1));
        rightBottomShooterMotor = new TalonFX(ShooterConstants.ShooterIDRightBottom, CANBus.systemcore(1));

        //leaderShooterMotor = leftBottomShooterMotor;
        
        //these should no longer be necessary with the other configs applied after this but i (Trevor Choy) left it in because i was worried about breaking something at comp lol
        leftBottomShooterMotor.getConfigurator().apply(new MotorOutputConfigs().withInverted(InvertedValue.Clockwise_Positive));
        leftTopShooterMotor.getConfigurator().apply(new MotorOutputConfigs().withInverted(InvertedValue.Clockwise_Positive));
        
        leftBottomShooterMotor.getConfigurator().apply(ShooterConstants.leftShooterMotorsConfig);
        leftTopShooterMotor.getConfigurator().apply(ShooterConstants.leftShooterMotorsConfig);
        rightTopShooterMotor.getConfigurator().apply(ShooterConstants.rightShooterMotorsConfig);
        rightBottomShooterMotor.getConfigurator().apply(ShooterConstants.rightShooterMotorsConfig);

        //initialize the control requests(the setpoints are changed later and are currently meaningless)
        shooterVelocityVoltage = new VelocityVoltage(0);

        //set the hood to the resting position
        //when making commands for the shooter the hood should always be set back to resting position when done so the robot can go under the trench
        //restHood();
        //set speed of the shooter wheels ot the resting velocity (makes it take less time to spin up and shoot, didn't cause any brownouts at Contra Costa but could use some more testing)
        setSpeed(Constants.ShooterConstants.restingAngularVelocity);

        // leftTopShooterMotor.setControl(new StrictFollower(leaderShooterMotor.getDeviceID()));
        // rightTopShooterMotor.setControl(new StrictFollower(leaderShooterMotor.getDeviceID()));
        // rightBottomShooterMotor.setControl(new StrictFollower(leaderShooterMotor.getDeviceID()));
    }
    /** @param deisredAngularVelocity the desired angular velocity of the motors */
    public void setSpeed(AngularVelocity desiredAngularVelocity) {
        //set the velocity target of the velocity voltage to the desired angular velocity
        shooterVelocityVoltage.withVelocity(desiredAngularVelocity.in(RotationsPerSecond));
        //leaderShooterMotor.setControl(shooterVelocityVoltage);
        //set the control of the motors to the velocityVoltage
        leftBottomShooterMotor.setControl(shooterVelocityVoltage.withVelocity(desiredAngularVelocity));
        leftTopShooterMotor.setControl(shooterVelocityVoltage.withVelocity(desiredAngularVelocity));
        rightTopShooterMotor.setControl(shooterVelocityVoltage.withVelocity(desiredAngularVelocity));
        rightBottomShooterMotor.setControl(shooterVelocityVoltage.withVelocity(desiredAngularVelocity));
    }

    /** @return the estimated initial speed of the ball after being shot from the shooter in m/s*/
    public double getFuelSpeed(){
        return getFuelSpeedWithCustomEfficiency(ShooterConstants.kShooterEfficiency);
    }

    /** @param efficiency the percentage of velocity transferred from the wheels to the fuel SUPPOSED to be from (0-1) but sometimes cooked things happen and you get 1.05 as your efficiency 😭 likely because of forgetting the hood roller gear ratio
     * @return the estimated initial speed of the ball after being shot from the shooter in m/s*/
    public double getFuelSpeedWithCustomEfficiency(double efficiency){
        
        //angular velocity of the motors
        double motorOmega = getShooterMotorVelocity().in(RadiansPerSecond);

        //angular velocity of the flywheels
        double shooterOmega = motorOmega * ShooterConstants.ratio;

        //tangential speed of the flywheel wheels
        double wheelTangentialSpeed = shooterOmega * ShooterConstants.kShooterWheelRadius.in(Meters);

        //tangential speed of the roller hood wheels (I just realized I forgot to account for the gear ratio lol 😭)
        double rollerTangentialSpeed = shooterOmega * ShooterConstants.kShooterRollerRadius.in(Meters);

        //return an average of the two velocities times the efficiency to get the estimated fuel velocity
        return efficiency * (wheelTangentialSpeed + rollerTangentialSpeed)/2;
    }

    /** @return the average angular velocity of the shooter motors measured from all 4 shooter motors*/
    public AngularVelocity getShooterMotorVelocity(){
            return  
            RadiansPerSecond.of((leftBottomShooterMotor.getVelocity().getValue().in(RadiansPerSecond)
          + leftTopShooterMotor.getVelocity().getValue().in(RadiansPerSecond)
          + rightTopShooterMotor.getVelocity().getValue().in(RadiansPerSecond)
          + rightBottomShooterMotor.getVelocity().getValue().in(RadiansPerSecond))/4);
    }

    /** @return true if the shooter motors are at the target velocity (within tolerance), false otherwise*/
    public boolean isAtSpeed(){
        return leftBottomShooterMotor.getClosedLoopReference().isNear(getShooterMotorVelocity().in(RotationsPerSecond), ShooterConstants.kShooterVelocityTolerance.in(RotationsPerSecond));
    }

    // @Override
    // public void periodic(){
    //     //just a bunch of smartdashboard logging used for tuning
    //     SmartDashboard.putNumber("hood position", Units.rotationsToDegrees(hoodMotor.getPosition().getValueAsDouble()));
    //     SmartDashboard.putNumber("hood target position", hoodMotionMagic.Position);
    //     SmartDashboard.putNumber("manual hood target", manualAngle);
    //     SmartDashboard.putNumber("shooter velocity", leftBottomShooterMotor.getVelocity().getValue().in(RPM));

    //     SmartDashboard.putNumber("bottom left shooter motor current", leftBottomShooterMotor.getSupplyCurrent().getValue().in(Amps));
    //     SmartDashboard.putNumber("top left shooter motor current", leftTopShooterMotor.getSupplyCurrent().getValue().in(Amps));
    //     SmartDashboard.putNumber("bottom right shooter motor current", rightBottomShooterMotor.getSupplyCurrent().getValue().in(Amps));
    //     SmartDashboard.putNumber("top right shooter motor current", rightTopShooterMotor.getSupplyCurrent().getValue().in(Amps));
    // }

    /** stop all shooter motors */
    public void stopShooter(){
        leftBottomShooterMotor.stopMotor();
        leftTopShooterMotor.stopMotor();
        rightTopShooterMotor.stopMotor();
        rightBottomShooterMotor.stopMotor();
    }

    /** A command to run the shooter motors at a given velocity */
    public Command changeVelocity(AngularVelocity velocity){
        return run(coroutine -> {
            //set the shooter target speed to the desired angular velocity
            //"Shooter.this" is not needed to use setSpeed, it can be accessed directly due to the command being a nested class of the Shooter subsystem
            setSpeed(velocity);
            coroutine.park();
        })
        .named("shooter ChangeVelocity: " + velocity.in(RPM));
    }

    public Command changeVelocity(Supplier<AngularVelocity> velocitySupplier){
        return runRepeatedly(() -> {
            setSpeed(velocitySupplier.get());
        })
        .named("shooter ChangeVelocity: supplier");
    }

    @Override
    public Command idle(){
        return run(coroutine -> {
            setSpeed(ShooterConstants.restingAngularVelocity);
            coroutine.park();
        })
        .withPriority(Command.LOWEST_PRIORITY)
        .named(getName() + "[IDLE]");
    }
}
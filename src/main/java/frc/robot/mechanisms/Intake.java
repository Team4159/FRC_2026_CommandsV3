package frc.robot.mechanisms;

import static org.wpilib.units.Units.Amps;
import static org.wpilib.units.Units.Degrees;
import static org.wpilib.units.Units.Rotations;

import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.MotionMagicConfigs;
import com.ctre.phoenix6.controls.MotionMagicVoltage;
import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.hardware.TalonFX;

import org.wpilib.units.measure.Angle;
import org.wpilib.system.Timer;
import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;

import frc.robot.Constants.IntakeConstants;
import frc.robot.Constants.IntakeConstants.IntakeState;

public class Intake extends Mechanism {
    private final TalonFX locationMotor;
    private final TalonFX spinMotor;
    private final CANcoder canCoder;

    private double rollerPercentage;

    private final MotionMagicVoltage intakeMotionMagicVoltage;
    // private final VelocityVoltage intakeVelocityVoltage;

    public Intake() {
        locationMotor = new TalonFX(IntakeConstants.kAngleId, CANBus.systemcore(1));
        spinMotor = new TalonFX(IntakeConstants.kIntakeSpinId, CANBus.systemcore(1));
        canCoder = new CANcoder(IntakeConstants.kAngleEncoderId, CANBus.systemcore(1));

        canCoder.getConfigurator().apply(IntakeConstants.canCoderConfig);
        locationMotor.getConfigurator().apply(IntakeConstants.angleConfig);
        setMotionMagic(IntakeConstants.kFastMotionMagicConfig);

        intakeMotionMagicVoltage = new MotionMagicVoltage(0);
        setLocation(IntakeState.UP_OFF.rotationLocation);
        // intakeVelocityVoltage = new VelocityVoltage(0);

        CurrentLimitsConfigs currentLimits = new CurrentLimitsConfigs().withSupplyCurrentLimit(Amps.of(20))
                .withSupplyCurrentLimitEnable(true);
        spinMotor.getConfigurator().apply(currentLimits);

        rollerPercentage = 0;
    }

    public void setMotionMagic(MotionMagicConfigs motionMagicConfigs) {
        locationMotor.getConfigurator().apply(IntakeConstants.angleConfig.withMotionMagic(motionMagicConfigs));
    }

    public void setSpinSpeed(double speed) {
        spinMotor.setThrottle(speed);
    }

    public void setLocation(Angle angle) {
        locationMotor.setControl(intakeMotionMagicVoltage.withPosition(angle));
    }

    private Angle getPivotAngle() {
        return Rotations.of(locationMotor.getPosition().getValueAsDouble());
    }

    // @Override
    // public void periodic() {
    //     SmartDashboard.putNumber("intake angle", getPivotAngle().in(Degrees));
    //     SmartDashboard.putNumber("intake pid error",
    //             Units.rotationsToDegrees(locationMotor.getClosedLoopError().getValueAsDouble()));

    //     if(getPivotAngle().in(Degrees) < 30){
    //         setSpinSpeed(rollerPercentage);
    //     }
    //     else{
    //         setSpinSpeed(0);
    //     }
    // }

    public Command changeState(IntakeState state){
        return run(coroutine -> {
            rollerPercentage = state.spinSpeed;
            setLocation(state.rotationLocation);
            coroutine.park();
        }).whenCanceled(() -> {
            setSpinSpeed(IntakeState.STOP.spinSpeed);
            rollerPercentage = 0;
        }).named("intake ChangeState: " + state.name());
    }

    // public class ChangeStates extends Command {
    //     private IntakeState state;

    //     public ChangeStates(IntakeState state) {
    //         this.state = state;
    //         addRequirements(Intake.this);
    //     }

    //     @Override
    //     public void initialize() {
    //         rollerPercentage = state.spinSpeed;
    //         setLocation(state.rotationLocation);
    //     }

    //     @Override
    //     public void end(boolean interrupt) {
    //         setSpinSpeed(IntakeState.STOP.spinSpeed);
    //         rollerPercentage = 0;
    //     }
    // }

    public Command compressIntake(){
        return run(coroutine -> {
            setMotionMagic(IntakeConstants.kSlowMotionMagicConfig);
            setLocation(IntakeState.UP_OFF.rotationLocation);
            coroutine.park();
        }).whenCanceled(() -> {
            setMotionMagic(IntakeConstants.kFastMotionMagicConfig);
        }).named("intake compressIntake");
    }

    // public class CompressIntake extends Command {
    //     public CompressIntake() {
    //         addRequirements(Intake.this);
    //     }

    //     @Override
    //     public void initialize() {
    //         setMotionMagic(IntakeConstants.kSlowMotionMagicConfig);
    //         setLocation(IntakeState.UP_OFF.rotationLocation);
    //     }

    //     @Override
    //     public void end(boolean interrupted) {
    //         setMotionMagic(IntakeConstants.kFastMotionMagicConfig);
    //     }
    // }

    public Command bounceIntake(){
        return runRepeatedly(() -> {
            bounceIntakeInternal();
        }).whenCanceled(() -> {
            setSpinSpeed(IntakeState.STOP.spinSpeed);
            rollerPercentage = 0;
        }).named("intake bounceIntake");
    }

    IntakeState bounceState = IntakeState.BOUNCE_UP;
    Timer bounceTimer = new Timer();

    private void bounceIntakeInternal(){
        boolean alternate = isNear(bounceState) || bounceTimer.get() > 1;
        if (bounceState == IntakeState.DOWN_OFF && alternate) {
            setLocation(IntakeState.BOUNCE_UP.rotationLocation);
            bounceTimer.reset();
        }
        if (bounceState == IntakeState.BOUNCE_UP && alternate) {
            setLocation(IntakeState.DOWN_OFF.rotationLocation);
            bounceTimer.reset();
        }
    }

    public boolean isNear(IntakeState state) {
        return getPivotAngle().isNear(state.rotationLocation, Degrees.of(10));
    }

    @Override
    public Command idle() {
        return run(coroutine -> {
            setLocation(IntakeState.DOWN_OFF.rotationLocation);
            setSpinSpeed(IntakeState.DOWN_OFF.spinSpeed);
            coroutine.park();
        }).withPriority(Command.LOWEST_PRIORITY)
        .named(getName() + "[IDLE]");
    }

    // public class BounceIntake extends Command {
    //     private double lastStateChange;
    //     private IntakeState state;
    //     private Timer timer;

    //     public BounceIntake() {
    //         addRequirements(Intake.this);
    //         timer = new Timer();
    //     }

    //     @Override
    //     public void initialize() {
    //         changeState(IntakeState.BOUNCE_UP);
    //         timer.reset();
    //     }

    //     @Override
    //     public void execute() {
    //         boolean alternate = isNear(state) || timer.get() > 1;
    //         if (state == IntakeState.DOWN_OFF && alternate) {
    //             setLocation(IntakeState.BOUNCE_UP.rotationLocation);
    //             timer.reset();
    //         }
    //         if (state == IntakeState.BOUNCE_UP && alternate) {
    //             setLocation(IntakeState.DOWN_OFF.rotationLocation);
    //             timer.reset();
    //         }
    //     }

    //     @Override
    //     public void end(boolean interrupted) {
    //         setLocation(IntakeState.BOUNCE_UP.rotationLocation);
    //     }

    //     private double getTime() {
    //         return MathSharedStore.getTimestamp();
    //     }

    //     private void changeState(IntakeState state) {
    //         this.state = state;
    //         lastStateChange = getTime();
    //         applyState();
    //     }

    //     private void applyState() {
    //         setLocation(state.rotationLocation);
    //     }

    //     private boolean isNear(IntakeState state) {
    //         return getPivotAngle().isNear(state.rotationLocation, Degrees.of(10));
    //     }
    // }
}

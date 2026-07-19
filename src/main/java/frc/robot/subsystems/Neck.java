package frc.robot.subsystems;

import static org.wpilib.units.Units.Amps;

import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;

import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.hardware.TalonFX;

import frc.robot.Constants.NeckConstants;
import frc.robot.Constants.NeckConstants.NeckState;

public class Neck extends Mechanism{
    private final TalonFX neckMotor;

    public Neck(){
        neckMotor = new TalonFX(NeckConstants.kNeckID, CANBus.systemcore(1));

        //the neck motor does not need much of a config so it just has its current limit config created and applied here
        CurrentLimitsConfigs currentLimits = new CurrentLimitsConfigs().withSupplyCurrentLimit(Amps.of(20)).withSupplyCurrentLimitEnable(true);
        neckMotor.getConfigurator().apply(currentLimits);
    }

    /** @param speed the percentage (-1-1) of how much power is sent to the neck motor*/
    public void setNeckSpeed(double speed){
        neckMotor.setThrottle(speed);
    }

    /** stops the neck */
    public void stopNeck(){
        neckMotor.setThrottle(0);
    }

    public Command changeState(NeckState state){
        return run(coroutine -> {
            setNeckSpeed(state.percentage);
            System.out.println("neck changestate");
            coroutine.park();
        })
        .named("shooter ChangeFeederState: " + state.name());
    }

    @Override
    public Command idle(){
        return run(coroutine -> {
            stopNeck();
            coroutine.park();
        })
        .withPriority(Command.LOWEST_PRIORITY)
        .named(getName() + "[IDLE]");
    }

}

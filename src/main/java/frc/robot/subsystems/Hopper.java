package frc.robot.subsystems;

import static org.wpilib.units.Units.Amps;

import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;

import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.hardware.TalonFX;

import frc.robot.Constants.HopperConstants;
import frc.robot.Constants.HopperConstants.HopperState;

public class Hopper extends Mechanism{
    private TalonFX hopperMotor;

    public Hopper(){
        hopperMotor = new TalonFX(HopperConstants.HopperId, CANBus.systemcore(1));
        CurrentLimitsConfigs currentLimits = new CurrentLimitsConfigs().withSupplyCurrentLimit(Amps.of(20)).withSupplyCurrentLimitEnable(true);
        hopperMotor.getConfigurator().apply(currentLimits);
    }
    
    public void setHopperSpeed(double speed){
        hopperMotor.setThrottle(speed);
    }

    public void stopHopper(){
        hopperMotor.setThrottle(0);
    }

    public Command changeState(HopperState state){
        return run(coroutine -> 
            {
                setHopperSpeed(state.percentage);
                coroutine.park();
            }
        ).named("hopper ChangeState: " + state.name());
    }

    @Override
    public Command idle() {
        return run(coroutine -> {
            stopHopper();
            coroutine.park();
        }).withPriority(Command.LOWEST_PRIORITY)
        .named(getName() + "[IDLE]");
    }

    // public class ChangeState extends Command{
    //     private HopperState hopperState;
        
    //     public ChangeState(HopperState hopperState) {
    //         this.hopperState = hopperState;
    //         // addRequirements(Hopper.this);
    //     }

    //     @Override
    //     public void initialize(){
    //         Hopper.this.setHopperSpeed(hopperState.percentage);
    //     }

    //     @Override
    //     public void end(boolean interrupt){
    //         Hopper.this.stopHopper();
    //     }
    // }
}
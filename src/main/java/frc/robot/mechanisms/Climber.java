package frc.robot.mechanisms;

import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.hardware.TalonFX;

import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;

import frc.robot.Constants;
import frc.robot.Constants.ClimberConstants.ClimberState;

public class Climber extends Mechanism{
    private TalonFX climbMotorOne;
    //private TalonFX climbMotorTwo;
    
    //private final VelocityVoltage climberVelocityVoltage;

    public Climber(){
        // Slot0Configs climberConfigOne = new Slot0Configs();
        // climberConfigOne.kP = Constants.ClimberConstants.kP;
        // climberConfigOne.kI = Constants.ClimberConstants.kI;
        // climberConfigOne.kD = Constants.ClimberConstants.kD;

        // Slot0Configs climberConfigTwo = new Slot0Configs();
        // climberConfigTwo.kP = Constants.ClimberConstants.kP;
        // climberConfigTwo.kI = Constants.ClimberConstants.kI;
        // climberConfigTwo.kD = Constants.ClimberConstants.kD;

        climbMotorOne = new TalonFX(Constants.ClimberConstants.idClimberOne, CANBus.systemcore(1));
        // climbMotorTwo = new TalonFX(Constants.ClimberConstants.idClimberTwo);

        // climbMotorOne.getConfigurator().apply(climberConfigOne);
        // climbMotorTwo.getConfigurator().apply(climberConfigTwo);

        // climberVelocityVoltage = new VelocityVoltage(0);
    }
    

    public void setClimberSpeed(double climberSpeed){
        climbMotorOne.setThrottle(climberSpeed);
    }

    public void stopClimber(){
        climbMotorOne.stopMotor();
    }

    public Command changeState(ClimberState climberState){
        return run(coroutine -> {
            setClimberSpeed(climberState.percentage);
        })
        .named("changeClimberState");
    }

    @Override
    public Command idle() {
        return run(coroutine -> {
            stopClimber();
            coroutine.park();
        }).withPriority(Command.LOWEST_PRIORITY)
        .named(getName() + "[IDLE]");
    }

    // public class ChangeState extends Command{
    //     private ClimberState climberState;
        
    //     public ChangeState(ClimberState climberState){
    //         this.climberState = climberState;
    //         addRequirements(Climber.this);
    //     }

    //     @Override
    //     public void initialize(){
    //         Climber.this.setClimberSpeed(climberState.percentage);
    //     }

    //     @Override
    //     public void end(boolean interrupt){
    //         Climber.this.stopClimber();
    //     }
    // }
}

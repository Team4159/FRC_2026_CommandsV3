package frc.robot.commands;

import java.util.Optional;

import org.wpilib.framework.RobotBase;
import org.wpilib.smartdashboard.SmartDashboard;
import org.wpilib.command3.Command;
import org.wpilib.command3.button.CommandGamepad;

import frc.lib.AutoAimCalculator;
import frc.robot.Constants.HopperConstants.HopperState;
import frc.robot.Constants.LEDConstants.LEDStatus;
import frc.robot.Constants.NeckConstants.NeckState;
import frc.robot.mechanisms.Drivetrain;
import frc.robot.mechanisms.Hood;
import frc.robot.mechanisms.Hopper;
import frc.robot.mechanisms.Intake;
import frc.robot.mechanisms.LEDs;
import frc.robot.mechanisms.Neck;
import frc.robot.mechanisms.Shooter;

public class AutoAim {
    public static Command cmd(
        Drivetrain drivetrain,
        Shooter shooter,
        Hood hood,
        Neck neck,
        Hopper hopper,
        Intake intake,
        LEDs leds,
        boolean autonomousMode,
        Optional<CommandGamepad> feedbackController
    ){
        return Command.noRequirements(
            coroutine -> {
                boolean startFeeding = false;
                AutoAimCalculator autoAimCalculator = new AutoAimCalculator(drivetrain, shooter);

                coroutine.fork(leds.changeLED(LEDStatus.YELLOW_BLINK));
                coroutine.fork(intake.bounceIntake());
                coroutine.fork(drivetrain.rotateSwerve(autoAimCalculator::getDesiredRobotAngle));
                coroutine.fork(shooter.changeVelocity(autoAimCalculator::getDesiredShooterAngularVelocity));
                coroutine.fork(hood.changeTrajectoryAngle(autoAimCalculator::getDesiredHoodAngle));
                while(true){
                    autoAimCalculator.calculate();

                    //SmartDashboard logging:
                    SmartDashboard.putBoolean("isAtPitch", hood.isAtPitch());
                    SmartDashboard.putBoolean("isatspeed", shooter.isAtSpeed());

                    if(!startFeeding && hood.isAtPitch() && shooter.isAtSpeed()){
                        coroutine.fork(neck.changeState(NeckState.FEED));
                        coroutine.fork(hopper.changeState(HopperState.FEED));
                        coroutine.fork(leds.changeLED(LEDStatus.GREEN_BLINK));
                        startFeeding = true;
                    }

                    if(RobotBase.isSimulation())
                        autoAimCalculator.simulate();

                    //prevent infinite loop
                    coroutine.yield();
                }
            }
        )
        // .whenCanceled(() -> end())
        .named("AutoAim");
    }
}
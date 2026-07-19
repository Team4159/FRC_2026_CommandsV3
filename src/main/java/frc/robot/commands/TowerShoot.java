package frc.robot.commands;

import org.wpilib.command3.Command;
import org.wpilib.smartdashboard.SmartDashboard;

import frc.robot.Constants.HoodConstants;
import frc.robot.Constants.ShooterConstants;
import frc.robot.Constants.HopperConstants.HopperState;
import frc.robot.Constants.NeckConstants.NeckState;
import frc.robot.subsystems.Hood;
import frc.robot.subsystems.Hopper;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Neck;
import frc.robot.subsystems.Shooter;

public class TowerShoot{
    public static Command cmd(Shooter shooter, Hood hood, Neck neck, Intake intake, Hopper hopper){

        return Command.noRequirements(
            coroutine -> {
                //fork the coroutine to run shooter, hood, and intake
                coroutine.fork(shooter.changeVelocity(ShooterConstants.towerAngularVelocity));
                coroutine.fork(hood.changeTrajectoryAngle(HoodConstants.towerHoodPitch));
                coroutine.fork(intake.bounceIntake());
                while (true) {
                    //log isAtPitch and isAtSpeed on smartdashboard
                    SmartDashboard.putBoolean("isAtPitch", hood.isAtPitch());
                    SmartDashboard.putBoolean("isatspeed", shooter.isAtSpeed());

                    //check if conditions for feeding are met
                    if(hood.isAtPitch() && shooter.isAtSpeed()){
                        //fork the coroutine to run neck and hopper
                        coroutine.fork(neck.changeState(NeckState.FEED));
                        coroutine.fork(hopper.changeState(HopperState.FEED));
                        //park the coroutine so it doesn't continually fork neck and hopper
                        coroutine.park();
                    }
                    //yield coroutine to prevent infinite loop
                    coroutine.yield();
                }
            }
        )
        //no need for .whenCanceled because the children (the forks) are automatically cancelled causing the mechanisms to return to idle state
        .named("HubShoot");
    }
}
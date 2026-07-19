// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import java.lang.classfile.ClassFile.Option;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.wpilib.command3.Command;
import org.wpilib.command3.ParallelGroup;
import org.wpilib.command3.ParallelGroupBuilder;
import org.wpilib.command3.Trigger;
import org.wpilib.command3.button.CommandGamepad;
import org.wpilib.command3.button.RobotModeTriggers;
// import choreo.auto.AutoFactory;
import org.wpilib.driverstation.DriverStation;
import org.wpilib.driverstation.RobotState;
import org.wpilib.driverstation.GenericHID.RumbleType;
import org.wpilib.framework.RobotBase;

import frc.lib.FluentTrigger;
import frc.robot.Constants.OperatorConstants;
import frc.robot.Constants.ShooterConstants;
import frc.robot.Constants.OperatorConstants.DriveFlag;
import frc.robot.Constants.OperatorConstants.DriveMode;
import frc.robot.Constants.HopperConstants.HopperState;
import frc.robot.Constants.IntakeConstants.IntakeState;
import frc.robot.Constants.NeckConstants.NeckState;
import frc.robot.commands.AutoAim;
import frc.robot.commands.HubShoot;
import frc.robot.commands.TowerShoot;
import frc.robot.mechanisms.Drivetrain;
import frc.robot.mechanisms.Hood;
import frc.robot.mechanisms.Hopper;
import frc.robot.mechanisms.Intake;
import frc.robot.mechanisms.LEDs;
import frc.robot.mechanisms.Neck;
import frc.robot.mechanisms.PhotonVision;
import frc.robot.mechanisms.Shooter;

public class RobotContainer {
        private final CommandGamepad primaryController = new CommandGamepad(
                        OperatorConstants.kPrimaryControllerPort);
        private final CommandGamepad secondaryController = new CommandGamepad(
                        OperatorConstants.kSecondaryControllerPort);
        private final Trigger primaryZeroTrigger = primaryController.back();
        private final Trigger primaryIntakeAssistTrigger = primaryController.rightBumper();
        // private final Trigger primaryRadialModeTrigger = primaryController.y();
        private final Trigger primaryRobotManualAlignModeTrigger = primaryController.leftBumper();
        private final Trigger primaryRobotRelativeTrigger = primaryController.leftTrigger(0.1);
        private final Trigger primarySlowModeTrigger = primaryController.rightTrigger();
        private final Trigger primaryDriveAssistTrigger = primaryController.start();
        private final Trigger primaryLeftClimbAlignTrigger = primaryController.povLeft();
        private final Trigger primaryAutoAimTrigger = primaryController.westFace();
        private final Trigger primaryAutoLobTrigger = primaryController.southFace();
        private final Trigger primaryRightClimbAlignTrigger = primaryController.povRight();

        // Secondary Triggers
        private final Trigger feedHopperTrigger = secondaryController.rightBumper();
        private final Trigger reverseFeederHopperTrigger = secondaryController.rightTrigger(0.1);

        private final Trigger manualHoodPitchUpTrigger = secondaryController.povUp();
        private final Trigger manualHoodPitchDownTrigger = secondaryController.povDown();

        private final Trigger intakeTrigger = secondaryController.leftBumper();
        private final Trigger outtakeTrigger = secondaryController.leftTrigger(0.1);
        private final Trigger compressIntakeTrigger = secondaryController.southFace();
        private final Trigger bounceIntakeTrigger = secondaryController.eastFace();

        // private final Trigger raiseClimbTrigger = secondaryController.povLeft();
        // private final Trigger lowerClimbTrigger = secondaryController.povRight();

        private final Trigger shootTrigger = secondaryController.povRight();

        private final Trigger hubShootTrigger = secondaryController.westFace();
        private final Trigger towerShootTrigger = secondaryController.northFace();

        // Subsystems
        private final Intake intake = new Intake();
        private final Shooter shooter = new Shooter();
        private final Neck neck = new Neck();
        private final Hood hood = new Hood();
        private final Hopper hopper = new Hopper();
        private final LEDs leds = new LEDs();
        private final Drivetrain drivetrain = new Drivetrain(primaryController);
        // @SuppressWarnings("unused")
        // periodic function inside photon vision class used to send vision data
        private final PhotonVision photonVision = new PhotonVision(drivetrain);

        /* Path follower */
        // private final AutoFactory autoFactory;
        // private final ConfigurableAuto configurableAuto;

        private final Telemetry logger = new Telemetry(Constants.DrivetrainConstants.kMaxTranslationSpeed);

        public RobotContainer() {
                // set auto command for drivetrain
                // drivetrain.setAutonomousAutoAimCommand(
                //                 new AutoAim(drivetrain, shooter, hopper, intake, leds, true, Optional.empty()));
                // Choreo Auto
                // autoFactory = drivetrain.createAutoFactory();

                // configurableAuto = new ConfigurableAuto(autoFactory, drivetrain, shooter, intake, hopper, leds);

                //rumble on collision
                // drivetrain.crashTrigger.onTrue(Commands
                //                 .runOnce(() -> HIDRumble.rumble(primaryController.getHID(),
                //                                 new RumbleRequest(RumbleType.RIGHT_RUMBLE, 1, 0.5, 1))));

                //call the function that configures the robot bindings
                configureBindings();
        }

        private void configureBindings() {
                photonVision.setDefaultCommand(photonVision.defaultCMD());
                // Note that X is defined as forward according to WPILib convention,
                // and Y is defined as to the left according to WPILib convention.
                drivetrain.setDefaultCommand(drivetrain.drive(DriveMode.TELEOP,
                                primaryRobotRelativeTrigger::getAsBoolean));
                // Idle while the robot is disabled. This ensures the configured
                // neutral mode is applied to the drive motors while disabled.
                RobotModeTriggers.disabled().whileTrue(
                                drivetrain.drive(DriveMode.IDLE));

                // test mode
                primaryController.northFace()
                .whileTrue(drivetrain.drive(DriveMode.BRAKE));
        
                primaryController.eastFace().and(RobotState::isUtility).whileTrue(drivetrain.drive(DriveMode.POINT));

                // teleop mode
                primarySlowModeTrigger.and(RobotState::isTeleop)
                                .whileTrue(drivetrain.driveFlagToggler(DriveFlag.SLOW_MODE));
                primaryDriveAssistTrigger.and(RobotState::isTeleop).onTrue(
                                Command.noRequirements(
                                        (coroutine) -> {
                                                // HIDRumble.rumble(primaryController.getHID(),
                                                //         new RumbleRequest(RumbleType.LEFT_RUMBLE, 0.5, 0.25));
                                                drivetrain.setDriveFlagValue(DriveFlag.DRIVE_ASSIST,
                                                        !drivetrain.getDriveFlagValue(DriveFlag.DRIVE_ASSIST));
                                        }).named("primaryDriveAssistCommand"));
                primaryRobotManualAlignModeTrigger.and(RobotState::isTeleop)
                                .whileTrue(drivetrain.driveFlagToggler(DriveFlag.MANUAL_ALIGN));
                primaryIntakeAssistTrigger.and(RobotState::isTeleop)
                                .whileTrue(drivetrain.driveFlagToggler(DriveFlag.INTAKE_ASSIST));
                // FluentTrigger.build()
                //                 // .bind(1, primaryLeftClimbAlignTrigger.and(RobotState::isTeleop),
                //                 //                 new AutoAlign(drivetrain, TowerAlignGoal.LEFT))
                //                 // .bind(1, primaryRightClimbAlignTrigger.and(RobotState::isTeleop),
                //                 //                 new AutoAlign(drivetrain, TowerAlignGoal.RIGHT))
                //                 .bind(0, primaryAutoAimTrigger.and(RobotState::isTeleop),
                //                                 new AutoAim(drivetrain, shooter, hopper, intake, leds, false,
                //                                                 Optional.of(primaryController)).cmd())
                //                 .bind(0, primaryAutoLobTrigger.and(RobotState::isTeleop),
                //                                 new AutoLob(drivetrain, shooter, hopper, intake, leds, false).cmd());
                // primaryAutoAimTrigger.whileTrue(new AutoAim(drivetrain, shooter, hood, neck, hopper, intake, leds, false, Optional.of(primaryController)).cmd());
                primaryAutoAimTrigger.whileTrue(AutoAim.cmd(drivetrain, shooter, hood, neck, hopper, intake, leds, false, Optional.of(primaryController)));
                // primaryAutoLobTrigger.whileTrue(new AutoLob(drivetrain, shooter, hopper, intake, leds, false).cmd());

                // Reset the field-centric heading on left bumper press.
                primaryZeroTrigger.onTrue(Command.noRequirements(
                        (coroutine) -> {
                                // HIDRumble.rumble(primaryController.getHID(),
                                //         new RumbleRequest(RumbleType.LEFT_RUMBLE, 0.5, 0.25));
                                drivetrain.seedFieldCentric();
                        }
                ).named("primaryZeroTrigger"));

                drivetrain.registerTelemetry(logger::telemeterize);

                feedHopperTrigger.whileTrue(new ParallelGroupBuilder()
                        .requiring(
                                neck.changeState(NeckState.FEED),
                                hopper.changeState(HopperState.FEED))
                                .named("feedHopperTrigger"));

                reverseFeederHopperTrigger.whileTrue(new ParallelGroupBuilder()
                        .requiring(
                                neck.changeState(NeckState.UNSTUCKNECK),
                                hopper.changeState(HopperState.REVERSE))
                                .named("reverseFeederHopperTrigger"));
                
                // // intake
                intakeTrigger.whileTrue(new ParallelGroupBuilder()
                        .requiring(
                                intake.changeState(IntakeState.DOWN_ON),
                                hopper.changeState(HopperState.FEED))
                                .named("intake"));

                // intakeTrigger.whileTrue(
                //         intake.changeState(IntakeState.DOWN_ON)
                // );
                                
                outtakeTrigger.whileTrue(new ParallelGroupBuilder()
                        .requiring(
                                intake.changeState(IntakeState.DOWN_REV),
                                hopper.changeState((HopperState.REVERSE)),
                                neck.changeState(NeckState.UNSTUCKNECK))
                                .named("outtake"));

                compressIntakeTrigger.whileTrue(intake.changeState(IntakeState.UP_OFF));
                bounceIntakeTrigger.whileTrue(intake.bounceIntake());

                // raiseClimbTrigger.whileTrue(climber.new ChangeState(ClimberState.CLIMB));
                // lowerClimbTrigger.whileTrue(climber.new ChangeState(ClimberState.DOWN));

                manualHoodPitchDownTrigger.onTrue(hood.ManualHoodDown());
                manualHoodPitchUpTrigger.onTrue(hood.ManualHoodUp());

                shootTrigger.whileTrue(shooter.changeVelocity(ShooterConstants.shooterAngularVelocity));

                hubShootTrigger.whileTrue(HubShoot.cmd(shooter, hood, neck, intake, hopper));
                towerShootTrigger.whileTrue(TowerShoot.cmd(shooter, hood, neck, intake, hopper));
        }

        public Command getAutonomousCommand() {
                /* Run the routine selected from the auto chooser 
                 * cmd() is used to get the Choreo AutoRoutine object as a WPILIB Command object
                */
               return Command.requiring(new HashSet<>()).executing(coroutine -> {
                        System.out.println("no auto lol");
                        coroutine.park();
               }).named("auto");
                // return configurableAuto.getRoutine().cmd();
        }
}
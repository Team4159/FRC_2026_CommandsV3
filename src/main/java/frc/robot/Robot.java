// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import java.util.ArrayList;

import org.littletonrobotics.junction.LoggedRobot;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.networktables.NT4Publisher;

import org.wpilib.framework.RobotBase;
import org.wpilib.networktables.NetworkTableInstance;
import org.wpilib.networktables.ProtobufPublisher;
import org.wpilib.networktables.ProtobufSubscriber;
import org.wpilib.networktables.ProtobufTopic;
import org.wpilib.smartdashboard.SmartDashboard;
import org.wpilib.system.DataLogManager;
import org.wpilib.command3.Command;
import org.wpilib.command3.Scheduler;
import org.wpilib.command3.SchedulerEvent.Completed;
import org.wpilib.command3.SchedulerEvent.Mounted;
import org.wpilib.command3.SchedulerEvent.Scheduled;
import org.wpilib.command3.SchedulerEvent.Yielded;
import org.wpilib.command3.proto.SchedulerProto;
import org.wpilib.driverstation.DriverStation;

import frc.lib.FuelSimulation;

/**
 * The methods in this class are called automatically corresponding to each mode, as described in
 * the TimedRobot documentation. If you change the name of this class or the package after creating
 * this project, you must also update the Main.java file in the project.
 */
public class Robot extends LoggedRobot {
  private Command m_autonomousCommand;

  private final RobotContainer m_robotContainer;

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  public Robot() {
    // Instantiate our RobotContainer.  This will perform all our button bindings, and put our
    // autonomous chooser on the dashboard.
    m_robotContainer = new RobotContainer();
    
    // Start recording to data log
    DataLogManager.start();

    // Record DS control and joystick data.
    // Change to `false` to not record joystick data.
    DriverStation.startDataLog(DataLogManager.getLog(), true);

    if (RobotBase.isSimulation()) {
      Logger.addDataReceiver(new NT4Publisher());
    }
    Logger.start();
  }

  /**
   * This function is called every 20 ms, no matter the mode. Use this for items like diagnostics
   * that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
    // Runs the Scheduler.  This is responsible for polling buttons, adding newly-scheduled
    // commands, running already-scheduled commands, removing finished or interrupted commands,
    // and running subsystem periodic() methods.  This must be called from the robot's periodic
    // block in order for anything in the Command-based framework to work.
    Scheduler.getDefault().run();

    var runningCommands = Scheduler.getDefault().getRunningCommands();
    ArrayList<String> runningCommandsList = new ArrayList<>();
    runningCommands.stream().forEach(
      command -> {
        runningCommandsList.add(command.name());
      }
    );
    SmartDashboard.putStringArray("running commands", runningCommandsList.toArray(String[]::new));

    // String[] runningCommandsStrings = new String[runningCommands.size()];
    // for(int i = 0; i < runningCommands.size(); i++){
    //   runningCommandsStrings[i] = runningCommands.
    // }
    // SmartDashboard.putStringArray("running commands", runningCommands.toArray(runningCommandsStrings));

    // var queuedCommands = Scheduler.getDefault().getRunningCommands();
    // String[] queuedCommandsStrings = new String[runningCommands.size()];
    // SmartDashboard.putStringArray("queued commands", queuedCommands.toArray(queuedCommandsStrings));

    //SmartDashboard.putData(Scheduler.getDefault().proto.);

    // ProtobufTopic<Scheduler> topic = NetworkTableInstance.getDefault()
    //   .getProtobufTopic("MySubTable/MyProtoTopic", Scheduler.proto);

    // // Publisher
    // ProtobufPublisher<Scheduler> publisher = topic.publish();
    // publisher.set(Scheduler.getDefault());

    // ProtobufTopic<Scheduler> topic = NetworkTableInstance.getDefault()
    //   .getProtobufTopic("MySubTable/MyProtoTopic", Scheduler.proto);

    // // Publisher
    // ProtobufPublisher<Scheduler> publisher = topic.publish();
    // publisher.set(Scheduler.getDefault());

    // Scheduler.getDefault().addEventListener(event -> {
    //   switch (event) {
    //     case Scheduled(var cmd, var time) ->
    //     println("Scheduled " + cmd.name() + " at " + time);
    //     case Mounted(var cmd, var time) ->
    //     println("Resumed " + cmd.name() + " at " + time);
    //     case Yielded(var cmd, var time) ->
    //     println("Paused " + cmd.name() + " at " + time);
    //     case Completed(var cmd, var time) ->
    //     println("Finished " + cmd.name() + " at " + time);
    //   }
    // });
  }

  /** This function is called once each time the robot enters Disabled mode. */
  @Override
  public void disabledInit() {}

  @Override
  public void disabledPeriodic() {}

  /** This autonomous runs the autonomous command selected by your {@link RobotContainer} class. */
  @Override
  public void autonomousInit() {
    m_autonomousCommand = m_robotContainer.getAutonomousCommand();

    // schedule the autonomous command (example)
    if (m_autonomousCommand != null) {
      Scheduler.getDefault().schedule(m_autonomousCommand);
    }
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {}

  @Override
  public void teleopInit() {
    // This makes sure that the autonomous stops running when
    // teleop starts running. If you want the autonomous to
    // continue until interrupted by another command, remove
    // this line or comment it out.
    if (m_autonomousCommand != null) {
      Scheduler.getDefault().cancel(m_autonomousCommand);
    }
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {}

  @Override
  public void utilityInit() {
    // Cancels all running commands at the start of test mode.
    Scheduler.getDefault().cancelAll();
  }

  /** This function is called periodically during test mode. */
  @Override
  public void utilityPeriodic() {}

  /** This function is called once when the robot is first started up. */
  @Override
  public void simulationInit() {}

  /** This function is called periodically whilst in simulation. */
  @Override
  public void simulationPeriodic() {
    FuelSimulation.getInstance().update();
  }
}

// // Copyright (c) FIRST and other WPILib contributors.
// // Open Source Software; you can modify and/or share it under the terms of
// // the WPILib BSD license file in the root directory of this project.

// package frc.lib;

// import org.wpilib.command2.InstantCommand;
// import org.wpilib.command2.Subsystem;

// /**
//  * A Command that runs instantly; it will initialize, execute once, and end on the same iteration of
//  * the scheduler. Users can either pass in a Runnable and a set of requirements, or else subclass
//  * this command if desired.
//  *
//  * <p>This class is provided by the NewCommands VendorDep
//  */
// public class InstantCommandRunWhenDisabled extends InstantCommand {
//   /**
//    * Creates a new InstantCommand that runs the given Runnable with the given requirements.
//    *
//    * @param toRun the Runnable to run
//    * @param requirements the subsystems required by this command
//    */
//   public InstantCommandRunWhenDisabled(Runnable toRun, Subsystem... requirements) {
//     super(toRun, requirements);
//   }

//   @Override
//   public boolean runsWhenDisabled() {
//     return true;
//   }
// }

// package frc.robot.commands;

// import frc.robot.Constants.ShooterConstants;

// import org.wpilib.command3.Command;

// import frc.robot.Constants.FeederConstants.FeederState;
// import frc.robot.Constants.HopperConstants.HopperState;
// import frc.robot.subsystems.Hopper;
// import frc.robot.subsystems.Shooter;

// public class AutoShoot{
//     private final Shooter shooter;
//     private final Hopper hopper;

//     public AutoShoot(Shooter shooter, Hopper hopper){
//         this.shooter = shooter;
//         this.hopper = hopper;
//     }

//     public void execute(){
//         if(shooter.isAtSpeed()){
//             shooter.setFeederSpeed(FeederState.FEED.percentage);
//             hopper.setHopperSpeed(HopperState.FEED.percentage);
//         }
//         else{
//             shooter.setFeederSpeed(FeederState.STOP.percentage);
//             hopper.setHopperSpeed(HopperState.STOP.percentage);
//         }
//     }

//     public void end(){
//         shooter.setFeederSpeed(FeederState.STOP.percentage);
//         hopper.setHopperSpeed(HopperState.STOP.percentage);
//     }

//     public Command cmd(){
//         return Command.requiring(shooter)
//         .executing(coroutine ->{
//             shooter.setSpeed(ShooterConstants.shooterAngularVelocity);
//             while(true){
//                 execute();
//                 coroutine.yield();
//             }
//         })
//         .whenCanceled(() -> {
//             end();
//         })
//         .named("AutoShoot");
//     }
// }

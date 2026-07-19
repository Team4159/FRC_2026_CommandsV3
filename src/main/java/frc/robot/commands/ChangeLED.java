// package frc.robot.commands;

// import static org.wpilib.units.Units.Meters;
// import static org.wpilib.units.Units.MetersPerSecond;
// import static org.wpilib.units.Units.Seconds;

// import org.wpilib.units.measure.Distance;
// import org.wpilib.hardware.led.LEDPattern;
// import org.wpilib.util.Color;
// import org.wpilib.command2.Command;
// import frc.robot.subsystems.LEDs;

// public class ChangeLED extends Command {
    
//     private LEDPattern pattern;

//     private final LEDs led;
//     private static final Distance ledSpacing = Meters.of(1.0 / 120.0);
    
//     public static enum LEDStatus { 
//         RAINBOW(LEDPattern.rainbow(255,64)),
//         RAINBOW_SCROLL(RAINBOW.pattern.scrollAtAbsoluteSpeed(MetersPerSecond.of(1), ledSpacing)),

//         RED_SOLID(LEDPattern.solid(Color.kRed)),
//         YELLOW_SOLID(LEDPattern.solid(Color.kYellow)),
//         BLUE_SOLID(LEDPattern.solid(Color.kBlue)),
//         GREEN_SOLID(LEDPattern.solid(Color.kGreen)),

//         RED_BLINK(RED_SOLID.getPattern().blink(Seconds.of(0.25), Seconds.of(0.25))),
//         YELLOW_BLINK(YELLOW_SOLID.getPattern().blink(Seconds.of(0.25), Seconds.of(0.25))),
//         GREEN_BLINK(GREEN_SOLID.getPattern().blink(Seconds.of(0.25), Seconds.of(0.25)));

//         private LEDPattern pattern;

//         LEDStatus(LEDPattern c_InPattern) {
//             this.pattern = c_InPattern;
//         }

//         public LEDPattern getPattern() {
//             return pattern;
//         }
//     }

//     public ChangeLED(LEDStatus ledStatus, LEDs led) {
//         this.pattern = ledStatus.getPattern();
//         this.led = led;

//         addRequirements(led);
//     }
    
//     @Override
//     public void execute() {
//         led.setBuffer(pattern);
//     }
    
// }

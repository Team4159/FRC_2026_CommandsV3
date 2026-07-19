package frc.robot.subsystems;

import org.wpilib.hardware.led.AddressableLED;
import org.wpilib.hardware.led.AddressableLEDBuffer;
import org.wpilib.hardware.led.LEDPattern;
import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;

import frc.robot.Constants.LEDConstants.LEDStatus;

public class LEDs extends Mechanism {
    // private static final int kPort = 8;
    // private static final int kLength = 60;

    // private static final AddressableLED led = new AddressableLED(kPort); //i would prefer to not make it static and define it in the constructor but ethan thinks this is better.
    // private final AddressableLEDBuffer ledBuffer;


    public LEDs () {
        
        // ledBuffer = new AddressableLEDBuffer(kLength);

        // led.setLength(ledBuffer.getLength());

        // led.setData(ledBuffer);
        // // led.start();
    }

    public void setBuffer(LEDPattern pattern) {
        // pattern.applyTo(ledBuffer);
        // led.setData(ledBuffer);
    }

    public Command changeLED(LEDStatus ledStatus){
        return run(coroutine -> {
            // LEDs.this.setBuffer(ledStatus.getPattern());
            coroutine.park();
        }).named("LED ChangeStatus");
    }

    // public class ChangeLED extends Command {
        
    //     private LEDPattern pattern;
        
    //     public ChangeLED(LEDStatus ledStatus) {
    //         this.pattern = ledStatus.getPattern();

    //         addRequirements(LEDs.this);
    //     }
        
    //     @Override
    //     public void execute() {
    //         LEDs.this.setBuffer(pattern);
    //     }
        
    // }

    public Command changeLEDStatusSupplier(LEDStatusSupplier ledStatusSupplier){
        return runRepeatedly(() -> {
            // LEDs.this.setBuffer(ledStatusSupplier.getStatus().getPattern());
        }).named("LED ChangeStatus");
    }

    // public class ChangeLEDStatusSupplier extends Command{

    //     private LEDStatusSupplier ledStatusSupplier;

    //     public ChangeLEDStatusSupplier(LEDStatusSupplier ledStatusSupplier){
    //         this.ledStatusSupplier = ledStatusSupplier;
    //         addRequirements(LEDs.this);
    //     }
        
    //     @Override
    //     public void execute() {
    //         LEDs.this.setBuffer(ledStatusSupplier.getStatus().getPattern());
    //     }
    // }

    @FunctionalInterface
    public interface LEDStatusSupplier{
        public LEDStatus getStatus();
    }

}

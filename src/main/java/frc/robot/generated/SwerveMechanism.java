package frc.robot.generated;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.linalg.Matrix;
import org.wpilib.math.numbers.N1;
import org.wpilib.math.numbers.N3;

import com.ctre.phoenix6.swerve.SwerveRequest;
import com.ctre.phoenix6.swerve.jni.SwerveJNI;
import com.ctre.phoenix6.swerve.SwerveDrivetrain.SwerveDriveState;

public class SwerveMechanism extends Mechanism{
    private final CommandSwerveDrivetrain swerve;

    public SwerveMechanism(CommandSwerveDrivetrain swerve){
        this.swerve = swerve;
    }

    public CommandSwerveDrivetrain getSwerve(){
        return this.swerve;
    }

    public SwerveDriveState getState(){
        return swerve.getState();
    }

    public void setControl(SwerveRequest request) {
        swerve.setControl(request);
    }

    public void addVisionMeasurement(
            Pose2d visionRobotPoseMeters,
            double timestampSeconds,
            Matrix<N3, N1> visionMeasurementStdDevs)
    {
        swerve.addVisionMeasurement(visionRobotPoseMeters, timestampSeconds, visionMeasurementStdDevs);
    }

    public void registerTelemetry(Consumer<SwerveDriveState> telemetryFunction) {
        swerve.registerTelemetry(telemetryFunction);
    }

    public void seedFieldCentric(){
        swerve.seedFieldCentric();
    }

    /**
     * Returns a command that applies the specified control request to this swerve drivetrain.
     *
     * @param request Function returning the request to apply
     * @return Command to run
     */
    public Command applyRequest(Supplier<SwerveRequest> request) {
        return run(
            (coroutine) -> {
                swerve.setControl(request.get());
                coroutine.park();
            }).named("SwerveMechanism applyRequest");
    }
}
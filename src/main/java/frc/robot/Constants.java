// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import static org.wpilib.units.Units.*;

import java.util.Map;
import java.util.Set;

import com.ctre.phoenix6.configs.CANcoderConfiguration;
import com.ctre.phoenix6.configs.MotionMagicConfigs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.signals.FeedbackSensorSourceValue;
import com.ctre.phoenix6.signals.GravityTypeValue;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.ctre.phoenix6.signals.SensorDirectionValue;
import com.ctre.phoenix6.swerve.utility.PhoenixPIDController;
// import com.therekrab.autopilot.APConstraints;
// import com.therekrab.autopilot.APProfile;
// import com.therekrab.autopilot.APTarget;
// import com.therekrab.autopilot.Autopilot;

import org.wpilib.math.linalg.Matrix;
import org.wpilib.math.linalg.VecBuilder;
import org.wpilib.math.controller.ProfiledPIDController;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.math.geometry.Rotation3d;
import org.wpilib.math.geometry.Transform2d;
import org.wpilib.math.geometry.Transform3d;
import org.wpilib.math.geometry.Translation2d;
import org.wpilib.math.numbers.N1;
import org.wpilib.math.numbers.N3;
import org.wpilib.math.trajectory.TrapezoidProfile;
import org.wpilib.math.util.Units;
import org.wpilib.units.measure.Angle;
import org.wpilib.units.measure.AngularVelocity;
import org.wpilib.units.measure.Distance;
import org.wpilib.driverstation.Alliance;
// import org.wpilib.driverstation.DriverStation.Alliance;
import frc.robot.generated.TunerConstants;
import org.wpilib.hardware.led.LEDPattern;
import org.wpilib.util.Color;

/**
 * The Constants class provides a convenient place for teams to hold robot-wide
 * numerical or boolean
 * constants. This class should not be used for any other purpose. All constants
 * should be declared
 * globally (i.e. public static). Do not put anything functional in this class.
 *
 * <p>
 * It is advised to statically import this class (or one of its inner classes)
 * wherever the
 * constants are needed, to reduce verbosity.
 */
public final class Constants {

    public static class ClimberConstants {
        public static final double kI = 0;
        public static final double kD = 0;
        public static final double kP = 0;
        public static final int idClimberOne = 15;
        // public static final int idClimberTwo = 10;

        public static enum ClimberState {
            CLIMB(0.25), STOP(0), DOWN(-0.25);

            public double percentage;

            private ClimberState(double speed) {
                percentage = speed;
            }
        }
    }

    public static class HopperConstants {
        public static final int HopperId = 30;
        public static final Distance kHopperExtent = Inches.of(12.0);

        public static enum HopperState {
            FEED(1), REVERSE(-1), STOP(0);

            public double percentage;

            private HopperState(double speed) {
                percentage = speed;
            }
        }
    }

    public static class NeckConstants {
        public static final int kNeckID = 20; // idk if this port is used yet plz check

        public static enum NeckState {
            
            FEED(1),
            UNSTUCKNECK(-1),
            STOP(0);

            public double percentage;

            private NeckState(double speed) {
                percentage = speed;
            }
        }
    }

    public static class IntakeConstants {
        public static final double kAngleI = 1;
        public static final double kAngleD = 0;
        public static final double kAngleP = 40;
        public static final double kAngleG = 0.07;

        // motion magic
        public static final double kFastCruiseVelocity = 200;
        public static final double kFastAcceleration = 500;
        public static final double kFastJerk = 1600;

        public static final double kSlowCruiseVelocity = 1;
        public static final double kSlowAcceleration = 5;
        public static final double kSlowJerk = 1600;

        public static final int kAngleEncoderId = 1;
        public static final int kAngleId = 6; // youre welcome Faye
        public static final int kIntakeSpinId = 7;

        public static final Angle kEncoderOffset = Degrees.of(0);
        //now 25 and 2 because encoder is on the jackshaft now.
        public static final double kMotorToSensorRatio = 25;
        public static final double kSensorToMechanismRatio = 2;

        // motor configs
        public static final CANcoderConfiguration canCoderConfig = new CANcoderConfiguration() {
            {
                MagnetSensor.withAbsoluteSensorDiscontinuityPoint(Rotations.of(0.9));
                MagnetSensor.SensorDirection = SensorDirectionValue.CounterClockwise_Positive;
                MagnetSensor.withMagnetOffset(IntakeConstants.kEncoderOffset);
            }
        };

        public static final TalonFXConfiguration angleConfig = new TalonFXConfiguration() {
            {
                Slot0.kP = IntakeConstants.kAngleP;
                Slot0.kI = IntakeConstants.kAngleI;
                Slot0.kD = IntakeConstants.kAngleD;
                Slot0.kG = IntakeConstants.kAngleG;
                Slot0.withGravityType(GravityTypeValue.Arm_Cosine);
                // abs encoder
                Feedback.FeedbackRemoteSensorID = IntakeConstants.kAngleEncoderId;
                Feedback.FeedbackSensorSource = FeedbackSensorSourceValue.RemoteCANcoder;
                Feedback.SensorToMechanismRatio = IntakeConstants.kSensorToMechanismRatio;
                Feedback.RotorToSensorRatio = IntakeConstants.kMotorToSensorRatio;

                CurrentLimits.SupplyCurrentLimitEnable = true;
                CurrentLimits.SupplyCurrentLimit = 20;
            }
        };

        // motion magic
        public static final MotionMagicConfigs kFastMotionMagicConfig = new MotionMagicConfigs() {
            {
                MotionMagicCruiseVelocity = IntakeConstants.kFastCruiseVelocity; // Target cruise velocity of 80 rps
                MotionMagicAcceleration = IntakeConstants.kFastAcceleration; // Target acceleration of 160 rps/s (0.5
                                                                             // seconds)
                MotionMagicJerk = IntakeConstants.kFastJerk; // Target jerk of 1600 rps/s/s (0.1 seconds)
            }
        };

        public static final MotionMagicConfigs kSlowMotionMagicConfig = new MotionMagicConfigs() {
            {
                MotionMagicCruiseVelocity = IntakeConstants.kSlowCruiseVelocity;
                MotionMagicAcceleration = IntakeConstants.kSlowAcceleration;
                MotionMagicJerk = IntakeConstants.kSlowJerk;
            }
        };

        // public static final double kLocationGearRatio = 1.0 / 2.0;
        public static final double kSpinGearRatio = 1.0 / 5.0;

        /** Units: rad/s */
        public static final double kCompressRate = 1;
        public static final double kCompressP = 1;
        public static final double kCompressI = 0;
        public static final double kCompressD = 0;
        public static final ProfiledPIDController compressPID = new ProfiledPIDController(
                kCompressP,
                kCompressI,
                kCompressD,
                new TrapezoidProfile.Constraints(kCompressRate, 1));

        public static enum IntakeState {
            DOWN_ON(Degrees.of(-9), 1),
            DOWN_OFF(Degrees.of(-9), 0),
            DOWN_REV(Degrees.of(-9), -1),
            UP_OFF(Degrees.of(120), 0),
            BOUNCE_UP(Degrees.of(60), 0),
            STOP(Degrees.of(120), 0);

            public final Angle rotationLocation;
            public final double spinSpeed;

            private IntakeState(Angle location, double speed) {
                rotationLocation = location;
                spinSpeed = speed;
            }
        }
    }

    public static class OperatorConstants {
        public static final int kPrimaryControllerPort = 0;
        public static final int kSecondaryControllerPort = 1;

        // controller joystick constants
        public static final double kPrimaryTranslationDeadband = 0.1;
        public static final double kPrimaryRotationDeadband = 0.1;
        public static final double kPrimaryTranslationExponent = 2.0;
        public static final double kPrimaryRotationExponent = 2.0;
        public static final double kPrimaryTranslationRadius = 0.99;
        public static final double kPrimaryRotationRadius = 0.99;

        public static enum DriveMode {
            TELEOP,
            BRAKE,
            POINT,
            IDLE,
        }

        public static enum DriveFlag {
            SLOW_MODE,
            DRIVE_ASSIST,
            AUTO_BRAKE,
            INTAKE_ASSIST,
            MANUAL_ALIGN,
        }

        // drive assist constants
        public static final Distance kTrenchAssistPassPositionTolerance = Meters.of(0.45);
        public static final double kTrenchAssistApproachInputTolerance = 0.2;
        public static final Distance kTrenchAssistAlignPositionInnerTolerance = Meters.of(0.05);
        public static final Distance kTrenchAssistAlignPositionOuterTolerance = Meters.of(0.15);
        public static final double kTrenchAssistAlignStrength = 0.8;
        public static final double kTrenchAssistAlignInfluence = 0.2;
        public static final Distance kTrenchAssistFrontProtrusionExtent = Inches.of(10.0);

        // drive mode constants
        public static final Angle kPrimaryAutoBrakeReachedDesiredAngleTolerance = Degrees.of(5);

        public static final double kPrimarySlowModeTranslationFactor = 0.25;
        public static final double kPrimarySlowModeRotationFactor = 1;

        public static final double kPrimaryIntakeRotationInputDeadzone = 0.2;

        public static final double kPrimaryRadialModeDeadband = 0.2;

        public static final double kPrimaryAlignModeDeadband = 0.65;
        public static final double kPrimaryAlignModeSpeedTranslationFactor = 0.2;
        public static final double kPrimaryAlignModeSpeedRotationFactor = 0.1;
    }

    public static class DrivetrainConstants {
        public static final Distance kChassisSizeX = Inches.of(27.0);
        public static final Distance kChassisSizeY = Inches.of(27.0);

        public static final Distance kBumperSizeX = Inches.of(35.0);
        public static final Distance kBumperSizeY = Inches.of(35.0);

        public static final double kMaxTranslationSpeed = 1.0 * TunerConstants.kSpeedAt12Volts.in(MetersPerSecond);
        public static final double kMaxRotationSpeed = RotationsPerSecond.of(0.75).in(RadiansPerSecond);
        
        public static final double kPointKP = 5;
        public static final double kPointKI = 0.0;
        public static final double kPointKD = 0.0;
        public static final double kPointFeedForward = 0.0;

        public static final double kAimKP = 6;
        public static final double kAimKI = 0.1;
        public static final double kAimKD = 0.0;
        public static final double kAimFeedForward = 0.0;


        public static final Angle AutoAimTolerance = Degrees.of(10);
        public static final double kAutoAimInputMultiplier = 1;

        public static final PhoenixPIDController AutoAimRotationController = new PhoenixPIDController(kAimKP, kAimKI,
                kAimKD);
        static {
            AutoAimRotationController.enableContinuousInput(-Math.PI, Math.PI);
        }

    }

    public static class HoodConstants {
        //Hood Constants
        //CAN IDs
        public static final int HoodId = 8;
        public static final int kHoodEncoderID = 2;

                
        /** the angle between the center of the shooter and the part it launches from */
        public static final Angle kHoodAngleOffset = Degrees.of(7.6743605);         

        public static final Angle kRestingAngle = Degrees.of(-5.8019605);
        //PID
        public static final double kP = 150;

        public static final double kI = 25;

        public static final double kD = 0;

        public static final double kG = 0.03;

        public static final double kS = 5;

        //encoder offsets, ratios, and MotionMagic® constraints
        public static final Angle kEncoderOffset = Degrees.of(-248);

        public static final double kSensorToMechanismRatio = 34 / 16;

        public static final double kMotorToSensorRatio = 125;

        public static final double kCruiseVelocity = 40;

        public static final double kAcceleration = 80;

        public static final double kJerk = 1600;

        
        public static final Angle hubHoodPitch = Degrees.of(75);
        public static final Angle towerHoodPitch = Degrees.of(70);

                // hood cancoder config
        public static final CANcoderConfiguration canCoderConfig = new CANcoderConfiguration() {
            {
                MagnetSensor.withAbsoluteSensorDiscontinuityPoint(Rotations.of(0.5));

                MagnetSensor.SensorDirection = SensorDirectionValue.Clockwise_Positive;
                
                MagnetSensor.withMagnetOffset(HoodConstants.kEncoderOffset);
            }
        };

        // hood motion magic config
        public static final MotionMagicConfigs kHoodMotionMagicConfig = new MotionMagicConfigs() {
            {
                MotionMagicCruiseVelocity = HoodConstants.kCruiseVelocity;

                MotionMagicAcceleration = HoodConstants.kAcceleration;

                MotionMagicJerk = HoodConstants.kJerk;
            }
        };
        // hood motor conifg
        public static final TalonFXConfiguration hoodConfig = new TalonFXConfiguration() {
            {
                Slot0.kP = HoodConstants.kP;
                Slot0.kI = HoodConstants.kI;
                Slot0.kD = HoodConstants.kD;
                Slot0.kG = HoodConstants.kG;
                Slot0.withGravityType(GravityTypeValue.Arm_Cosine);
                MotorOutput.Inverted = InvertedValue.Clockwise_Positive;
                MotorOutput.NeutralMode = NeutralModeValue.Brake;

                CurrentLimits.SupplyCurrentLimitEnable = true;
                CurrentLimits.SupplyCurrentLimit = 20;
                // abs encoder
                Feedback.FeedbackRemoteSensorID = HoodConstants.kHoodEncoderID;
                Feedback.FeedbackSensorSource = FeedbackSensorSourceValue.RemoteCANcoder;
                Feedback.SensorToMechanismRatio = HoodConstants.kSensorToMechanismRatio;
                Feedback.RotorToSensorRatio = HoodConstants.kMotorToSensorRatio;
                MotionMagic = kHoodMotionMagicConfig;
            }
        };
    }

    public static class ShooterConstants {
        // Shooter Motor Config and PID
        // public static final double kP = 35;
        // public static final double kI = 10;
        public static final double kP = 5;
        public static final double kI = 0;
        public static final double kD = 0;
        public static final double kS = 0;
        //remove if doesnt work
        public static final double kV = 0.25;
        
        public static final double kA = 2.14;

        public static final double kCurrentLimit = 25;
        public static final double kRampRate = 0.2;

        public static final int ShooterIDLeftBottom = 9;
        public static final int ShooterIDLeftTop = 10;
        public static final int ShooterIDRightTop = 12;
        public static final int ShooterIDRightBottom = 11;

        // shooter motors config
        public static final TalonFXConfiguration rightShooterMotorsConfig = new TalonFXConfiguration() {
            {
                Slot0.kP = ShooterConstants.kP;
                Slot0.kI = ShooterConstants.kI;
                Slot0.kD = ShooterConstants.kD;
                Slot0.kS = ShooterConstants.kS;
                Slot0.kV = ShooterConstants.kV;
                Slot0.kA = ShooterConstants.kA;
                CurrentLimits.SupplyCurrentLimitEnable = true;
                CurrentLimits.SupplyCurrentLimit = kCurrentLimit;
                MotorOutput.Inverted = InvertedValue.CounterClockwise_Positive;
                MotorOutput.NeutralMode = NeutralModeValue.Coast;
                ClosedLoopRamps.VoltageClosedLoopRampPeriod=kRampRate;
            }
        };

        public static final TalonFXConfiguration leftShooterMotorsConfig = new TalonFXConfiguration() {
            {
                Slot0.kP = ShooterConstants.kP;
                Slot0.kI = ShooterConstants.kI;
                Slot0.kD = ShooterConstants.kD;
                Slot0.kS = ShooterConstants.kS;
                Slot0.kV = ShooterConstants.kV;
                Slot0.kA = ShooterConstants.kA;
                CurrentLimits.SupplyCurrentLimitEnable = true;
                CurrentLimits.SupplyCurrentLimit = kCurrentLimit;
                MotorOutput.Inverted = InvertedValue.Clockwise_Positive;
                MotorOutput.NeutralMode = NeutralModeValue.Coast;
                ClosedLoopRamps.VoltageClosedLoopRampPeriod=kRampRate;
            }
        };

        public static final AngularVelocity shooterAngularVelocity = RPM.of(2000);
        public static final AngularVelocity lobAngularVelocity = RPM.of(2000);
        public static final AngularVelocity restingAngularVelocity = RPM.of(1000);
        public static final AngularVelocity hubAngularVelocity = RPM.of(2500);
        public static final AngularVelocity towerAngularVelocity = RPM.of(3000);

        public static final double backwardsTime = 0.05;

        // Old equation stuff
        /** units: m/s */
        public static final double launchVelocity = Units.feetToMeters(29);// convert from ft/s to m/s
        public static final double ratio = 1;
        public static final double shootHeight = Units.inchesToMeters(40);

        public static final AngularVelocity kShooterVelocityTolerance = RPM.of(50);
        public static final Angle maxPitch = Degrees.of(85);

        public static final Distance kShooterWheelRadius = Inches.of(2);
        public static final Distance kShooterRollerRadius = Inches.of(0.75);

        public static final double kShooterEfficiency = 0.80;

        // robot relative shooter offset
        // TODO implement in the calculation
        public static final Transform2d shooterOffset = new Transform2d(0, 0, new Rotation2d());

        public static enum AutoAimStatus {
            SHOOT(LEDConstants.LEDStatus.GREEN_BLINK),
            OUTOFRANGE(LEDConstants.LEDStatus.RED_BLINK),
            WAITING(LEDConstants.LEDStatus.YELLOW_BLINK);

            public LEDConstants.LEDStatus ledStatus;

            private AutoAimStatus(LEDConstants.LEDStatus ledStatus) {
                this.ledStatus = ledStatus;
            }
        }
    }

    public static class PhotonVisionConstants {

        // TODO: tune stddev values
        public static final Matrix<N3, N1> kSingleTagStdDevs = VecBuilder.fill(1, 1, 4);
        public static final Matrix<N3, N1> kMultiTagStdDevs = VecBuilder.fill(0.25, 0.25, 1);

        public final static Transform3d leftShooterCamTransform = new Transform3d(
                Units.inchesToMeters(-1.2887),
                Units.inchesToMeters(8.8466),
                Units.inchesToMeters(21.1190),
                new Rotation3d(
                        0,
                        Units.degreesToRadians(-30),
                        Units.degreesToRadians(-5)));
        public final static Transform3d rightShooterCamTransform = new Transform3d(
                Units.inchesToMeters(-1.2887),
                Units.inchesToMeters(-8.8466),
                Units.inchesToMeters(21.1190),
                new Rotation3d(
                        0,
                        Units.degreesToRadians(-30),
                        Units.degreesToRadians(5)));
    }

    public static class FieldConstants {
        public static final Map<Alliance, Pose2d> hubLocations = Map.of(
                Alliance.BLUE, new Pose2d(Units.inchesToMeters(182.11), Units.inchesToMeters(158.84), new Rotation2d()),
                Alliance.RED,
                new Pose2d(Units.inchesToMeters(651.22 - 182.11), Units.inchesToMeters(158.84), new Rotation2d()));

        public static final Set<Pose2d> blueLobPositions = Set.of(
                new Pose2d(2.5, 2.1, new Rotation2d()),
                new Pose2d(2.5, 5.6, new Rotation2d()));

        public static final Set<Pose2d> redLobPositions = Set.of(
                new Pose2d(14.1, 2.1, new Rotation2d()),
                new Pose2d(14.1, 5.6, new Rotation2d()));

        public static final Map<Alliance, Set<Pose2d>> lobLocations = Map.of(
                Alliance.BLUE,
                blueLobPositions,
                Alliance.RED,
                redLobPositions);

        public static enum TrenchZone {
            BLUE_LEFT(Inches.of(182.11), Inches.of(317.69 - 24.97)),
            BLUE_RIGHT(Inches.of(182.11), Inches.of(24.97)),
            RED_LEFT(Inches.of(651.22 - 182.11), Inches.of(24.97)),
            RED_RIGHT(Inches.of(651.22 - 182.11), Inches.of(317.69 - 24.97));

            public final Distance x, y;

            private TrenchZone(Distance x, Distance y) {
                this.x = x;
                this.y = y;
            }
        }

        public static Distance kFieldWidth = Inches.of(651.22);
        public static Distance kFieldHeight = Inches.of(317.69);

        public static Distance kAllianceWidth = Inches.of(156.61);
        public static Distance kAllianceHeight = kFieldHeight;

        public static Distance kTrenchX = Inches.of(182.11);
        public static Distance kTrenchZoneWidth = Inches.of(140.0);
        public static Distance kTrenchZoneHeight = Inches.of(49.96);

        public static Distance kTowerX = Inches.of(41.755);
        public static Distance kTowerY = Inches.of(147.47);
        public static Distance kTowerWidth = Inches.of(35.2);

        public static Distance kTrenchZoneYBuffer = DrivetrainConstants.kBumperSizeY.div(4);

        public static enum FieldZone {
            FIELD(
                    new Translation2d(kFieldWidth.div(2), kFieldHeight.div(2)),
                    new Translation2d(kFieldWidth, kFieldHeight)),
            TRENCH_BLUE_LEFT(
                    new Translation2d(kTrenchX,
                            kFieldHeight.minus(kTrenchZoneHeight.div(2)).minus(kTrenchZoneYBuffer.div(2))),
                    new Translation2d(kTrenchX, kFieldHeight.minus(kTrenchZoneHeight.div(2))),
                    new Translation2d(kTrenchZoneWidth, kTrenchZoneHeight.plus(kTrenchZoneYBuffer))),
            TRENCH_BLUE_RIGHT(
                    new Translation2d(kTrenchX, kTrenchZoneHeight.div(2).plus(kTrenchZoneYBuffer.div(2))),
                    new Translation2d(kTrenchX, kTrenchZoneHeight.div(2)),
                    new Translation2d(kTrenchZoneWidth, kTrenchZoneHeight.plus(kTrenchZoneYBuffer))),
            TRENCH_RED_LEFT(
                    new Translation2d(kFieldWidth.minus(kTrenchX),
                            kTrenchZoneHeight.div(2).plus(kTrenchZoneYBuffer.div(2))),
                    new Translation2d(kFieldWidth.minus(kTrenchX), kTrenchZoneHeight.div(2)),
                    new Translation2d(kTrenchZoneWidth, kTrenchZoneHeight.plus(kTrenchZoneYBuffer))),
            TRENCH_RED_RIGHT(
                    new Translation2d(kFieldWidth.minus(kTrenchX),
                            kFieldHeight.minus(kTrenchZoneHeight.div(2)).minus(kTrenchZoneYBuffer.div(2))),
                    new Translation2d(kFieldWidth.minus(kTrenchX), kFieldHeight.minus(kTrenchZoneHeight.div(2))),
                    new Translation2d(kTrenchZoneWidth, kTrenchZoneHeight.plus(kTrenchZoneYBuffer)));

            public final Translation2d center, focus, size;

            private FieldZone(Translation2d center, Translation2d focus, Translation2d size) {
                this.center = center;
                this.focus = focus;
                this.size = size;
            }

            private FieldZone(Translation2d center, Translation2d size) {
                this(center, center, size);
            }
        }

        public static final FieldZone[] kTrenchZones = new FieldZone[] {
                FieldZone.TRENCH_BLUE_LEFT,
                FieldZone.TRENCH_BLUE_RIGHT,
                FieldZone.TRENCH_RED_LEFT,
                FieldZone.TRENCH_RED_RIGHT
        };

        /** Units:m/s^2 */
        public static final double g = 9.80;

        public static final double hubZ = Units.inchesToMeters(56.4);
    }

    public static class AutoConstants {
        /** units: seconds */
        public static final double ShootTime = 4;

        // public static final APConstraints kAutopilotConstraints = new APConstraints()
        //         .withAcceleration(7.0)
        //         .withJerk(3.5);
        // public static final APProfile kAutopilotAlignProfile = new APProfile(kAutopilotConstraints)
        //         .withErrorXY(Centimeters.of(2.0))
        //         .withErrorTheta(Degrees.of(1))
        //         .withBeelineRadius(Centimeters.of(5.0));
        // public static final Autopilot kAutopilotAlignController = new Autopilot(kAutopilotAlignProfile);
        // public static final APProfile kAutopilotCruiseProfile = new APProfile(kAutopilotConstraints)
        //         .withErrorXY(Centimeters.of(20.0))
        //         .withErrorTheta(Degrees.of(15.0))
        //         .withBeelineRadius(Centimeters.of(25.0));
        // public static final Autopilot kAutopilotCruiseController = new Autopilot(kAutopilotCruiseProfile);
    }

    public static class AlignConstants {
        // public static enum TowerAlignGoal {
        //     LEFT(
        //             new APTarget(new Pose2d(FieldConstants.kTowerX,
        //                     FieldConstants.kTowerY.plus(FieldConstants.kTowerWidth.div(2))
        //                             .plus(DrivetrainConstants.kBumperSizeX.div(2)),
        //                     Rotation2d.kZero)).withVelocity(0).withoutEntryAngle()),
        //     RIGHT(
        //             new APTarget(new Pose2d(FieldConstants.kTowerX,
        //                     FieldConstants.kTowerY.minus(FieldConstants.kTowerWidth.div(2))
        //                             .minus(DrivetrainConstants.kBumperSizeX.div(2)),
        //                     Rotation2d.k180deg)).withVelocity(0).withEntryAngle(Rotation2d.k180deg)),
        //     MIDDLE_FRONT(
        //             new APTarget(new Pose2d(
        //                     FieldConstants.kTowerX.plus(DrivetrainConstants.kBumperSizeX.div(2)).plus(Inches.of(6)),
        //                     FieldConstants.kTowerY, Rotation2d.k180deg)).withVelocity(0).withoutEntryAngle(),
        //             new APTarget(new Pose2d(FieldConstants.kTowerX, FieldConstants.kTowerY, Rotation2d.k180deg))
        //                     .withVelocity(0).withoutEntryAngle()),
        //     MIDDLE_BACK(
        //             new APTarget(new Pose2d(
        //                     FieldConstants.kTowerX.minus(DrivetrainConstants.kBumperSizeX.div(2))
        //                             .minus(Inches.of(6)),
        //                     FieldConstants.kTowerY, Rotation2d.kZero)).withVelocity(0).withoutEntryAngle(),
        //             new APTarget(new Pose2d(FieldConstants.kTowerX, FieldConstants.kTowerY, Rotation2d.kZero))
        //                     .withVelocity(0).withoutEntryAngle());

        //     public final APTarget[] targets;

        //     private TowerAlignGoal(APTarget... targets) {
        //         this.targets = targets;
        //     }
        // }
    }

    public static final class LEDConstants {

        private static final Distance ledSpacing = Meters.of(1.0 / 120.0);

        public static enum LEDStatus {
            RAINBOW(LEDPattern.rainbow(255, 64)),
            RAINBOW_SCROLL(RAINBOW.pattern.scrollAtAbsoluteVelocity(MetersPerSecond.of(1), ledSpacing)),

            RED_SOLID(LEDPattern.solid(Color.RED)),
            YELLOW_SOLID(LEDPattern.solid(Color.YELLOW)),
            BLUE_SOLID(LEDPattern.solid(Color.BLUE)),
            GREEN_SOLID(LEDPattern.solid(Color.GREEN)),

            RED_BLINK(RED_SOLID.getPattern().blink(Seconds.of(0.25), Seconds.of(0.25))),
            YELLOW_BLINK(YELLOW_SOLID.getPattern().blink(Seconds.of(0.25), Seconds.of(0.25))),
            GREEN_BLINK(GREEN_SOLID.getPattern().blink(Seconds.of(0.25), Seconds.of(0.25)));

            private LEDPattern pattern;

            LEDStatus(LEDPattern c_InPattern) {
                this.pattern = c_InPattern;
            }

            public LEDPattern getPattern() {
                return pattern;
            }
        }
    }

    public static final class JoeLookupTableConstants {

        public static record LookupTablePoint(AngularVelocity angularVelocity, double efficiency) {};

        public static final Distance kMaxDistance = Meters.of(4.5);

        // stores desired velocity based on position
        public static final Map<Distance, LookupTablePoint> joeLookupTable = Map.ofEntries(
            
            Map.entry(Meters.of(1),   new LookupTablePoint(RPM.of(1800), 1.1)),
            Map.entry(Meters.of(1.5), new LookupTablePoint(RPM.of(1900), 1.05)),
            Map.entry(Meters.of(2),   new LookupTablePoint(RPM.of(2000), 1.025)),
            Map.entry(Meters.of(2.5), new LookupTablePoint(RPM.of(2100), 1)),
            Map.entry(Meters.of(3),   new LookupTablePoint(RPM.of(2200), 1)),
            Map.entry(Meters.of(3.5), new LookupTablePoint(RPM.of(2300), 0.97)),
            Map.entry(Meters.of(4),   new LookupTablePoint(RPM.of(2500), 0.90)),
            Map.entry(Meters.of(4.5), new LookupTablePoint(RPM.of(2700), 0.85))
        );
    }
}

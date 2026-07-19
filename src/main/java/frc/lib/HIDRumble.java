// package frc.lib;

// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.Iterator;
// import java.util.Map;

// import org.wpilib.math.util.MathUtil;
// import org.wpilib.math.util.Units;
// import org.wpilib.driverstation.DriverStation;
// import org.wpilib.driverstation.GenericHID;
// import org.wpilib.driverstation.RobotState;
// import org.wpilib.system.Timer;
// import org.wpilib.driverstation.GenericHID.RumbleType;
// /**
//  * rudimentary library for rumble feedback on controllers and HIDs
//  */
// public class HIDRumble {
//     private static final double kDefaultRequestDuration = Units.millisecondsToSeconds(50);
//     private static final int kDefaultRequestPriority = 0;
//     private static final boolean kRumblePersistWhileDisabled = false;

//     private static boolean rumbleEnabled = true;

//     private static final HashMap<GenericHID, RumbleManager> rumbleManagerMap = new HashMap<>();

//     @SuppressWarnings("unused")
//     private static final HIDRumble instance = new HIDRumble();

//     private HIDRumble() {
//         new SubsystemBase() {
//             @Override
//             public void periodic() {
//                 // update all rumble managers
//                 for (Map.Entry<GenericHID, RumbleManager> rumbleManagerEntry : rumbleManagerMap.entrySet()) {
//                     RumbleManager rumbleManager = rumbleManagerEntry.getValue();
//                     rumbleManager.update();
//                 }
//             }
//         };
//     }

//     public static void rumble(GenericHID hid, RumbleRequest rumbleRequest) {
//         RumbleManager existingRumbleManager = rumbleManagerMap.get(hid);
//         RumbleManager rumbleManager = (existingRumbleManager != null) ? existingRumbleManager : new RumbleManager(hid);
//         rumbleManager.request(rumbleRequest);
//     }

//     public static void rumble(CommandGenericHID commandHid, RumbleRequest rumbleRequest) {
//         rumble(commandHid.getHID(), rumbleRequest);
//     }

//     public static void enable(boolean enabled) {
//         rumbleEnabled = enabled;
//     }

//     private static class RumbleManager {
//         private final ArrayList<RumbleRequest> rumbleRequestList = new ArrayList<>();
//         private int highestPriorityRequestIndex = 0;

//         private final GenericHID hid;

//         public RumbleManager(GenericHID hid) {
//             this.hid = hid;
//             HIDRumble.rumbleManagerMap.put(hid, this);
//         }

//         public void request(RumbleRequest rumbleRequest) {
//             rumbleRequestList.add(rumbleRequest);
//             if (rumbleRequest.priority > highestPriorityRequestIndex) {
//                 highestPriorityRequestIndex = rumbleRequest.priority;
//             }
//         }

//         public void update() {
//             boolean robotEnabled = RobotState.isEnabled();

//             if (!robotEnabled && !kRumblePersistWhileDisabled) {
//                 rumbleRequestList.clear();
//                 highestPriorityRequestIndex = 0;
//             } else {
//                 Iterator<RumbleRequest> removeIterator = rumbleRequestList.iterator();
//                 boolean removedHighestPriorityRequest = false;
//                 while (removeIterator.hasNext()) {
//                     RumbleRequest rumbleRequest = removeIterator.next();
//                     if (rumbleRequest.isExpired()) {
//                         removeIterator.remove();
//                         if (rumbleRequest.priority == highestPriorityRequestIndex) {
//                             removedHighestPriorityRequest = true;
//                         }
//                     }
//                 }
//                 if (removedHighestPriorityRequest) {
//                     updateHighestPriorityRequestIndex();
//                 }
//             }

//             if (rumbleEnabled && rumbleRequestList.size() > 0) {
//                 setRumbleFromRequest(getLatestHighestPriorityRequest());
//             } else {
//                 hid.setRumble(RumbleType.LEFT_RUMBLE, 0);
//             }
//         }

//         private void setRumbleFromRequest(RumbleRequest rumbleRequest) {
//             double leftStrength = 0, rightStrength = 0;
//             switch (rumbleRequest.rumbleType) {
//                 case LEFT_RUMBLE:
//                     leftStrength = rumbleRequest.strength;
//                     break;
//                 case RIGHT_RUMBLE:
//                     rightStrength = rumbleRequest.strength;
//                     break;
//                 // case BOTH:
//                 //     leftStrength = rumbleRequest.strength;
//                 //     rightStrength = rumbleRequest.strength;
//                 //     break;
//             }
//             hid.setRumble(RumbleType.LEFT_RUMBLE, leftStrength);
//             hid.setRumble(RumbleType.RIGHT_RUMBLE, rightStrength);
//         }

//         private void updateHighestPriorityRequestIndex() {
//             highestPriorityRequestIndex = 0;
//             for (RumbleRequest rumbleRequest : rumbleRequestList) {
//                 if (rumbleRequest.priority > highestPriorityRequestIndex) {
//                     highestPriorityRequestIndex = rumbleRequest.priority;
//                 }
//             }
//         }

//         private RumbleRequest getLatestHighestPriorityRequest() {
//             for (int i = rumbleRequestList.size() - 1; i >= 0; i--) {
//                 RumbleRequest rumbleRequest = rumbleRequestList.get(i);
//                 if (rumbleRequest.priority == highestPriorityRequestIndex) {
//                     return rumbleRequest;
//                 }
//             }
//             return null;
//         }
//     }

//     public static class RumbleRequest {
//         public final double start, duration, strength;
//         public final RumbleType rumbleType;
//         public final int priority;

//         public RumbleRequest(RumbleType rumbleType, double strength, double duration, int priority) {
//             this.start = Timer.getTimestamp();
//             this.rumbleType = rumbleType;
//             this.strength = Math.clamp(strength, 0, 1);
//             this.duration = Math.max(0, duration);
//             this.priority = priority;
//         }

//         public RumbleRequest(RumbleType rumbleType, double strength) {
//             this(rumbleType, strength, kDefaultRequestDuration, kDefaultRequestPriority);
//         }

//         public RumbleRequest(double strength) {
//             this(RumbleType.LEFT_RUMBLE, strength, kDefaultRequestDuration, kDefaultRequestPriority);
//         }

//         public RumbleRequest(RumbleType rumbleType, double strength, int priority) {
//             this(rumbleType, strength, kDefaultRequestDuration, priority);
//         }

//         public RumbleRequest(RumbleType rumbleType, double strength, double duration) {
//             this(rumbleType, strength, duration, kDefaultRequestPriority);
//         }

//         public RumbleRequest(double strength, int priority) {
//             this(RumbleType.LEFT_RUMBLE, strength, kDefaultRequestDuration, priority);
//         }

//         public boolean isExpired() {
//             return Timer.getTimestamp() - start > duration;
//         }
//     }
// }

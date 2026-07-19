package frc.lib;

import java.util.Optional;

import org.wpilib.driverstation.Alliance;
import org.wpilib.driverstation.DriverStation;
// import org.wpilib.driverstation.DriverStation.Alliance;
import org.wpilib.driverstation.MatchState;

public class HubTracker {

    public static boolean isHubActive(Alliance alliance) {
        Optional<Alliance> activeHub = getActiveHub();
        if (!activeHub.isEmpty()) {
            return alliance.equals(activeHub.get());
        }
        return false;
    }

    public static Optional<Alliance> getActiveHub() {
        double matchTime = MatchState.getMatchTime();
        Optional<Alliance> autoWinnerOptional = getAutoWinner();
        if (matchTime > 130 || matchTime <= 30 || autoWinnerOptional.isEmpty()) {
            return Optional.empty();
        }

        Alliance autoWinner = autoWinnerOptional.get();
        Alliance autoLoser = autoWinner.equals(Alliance.BLUE) ? Alliance.RED : Alliance.BLUE;
        if (matchTime > 105) {
            return Optional.of(autoLoser);
        } else if (matchTime > 80) {
            return Optional.of(autoWinner);
        } else if (matchTime > 55) {
            return Optional.of(autoLoser);
        } else if (matchTime > 30) {
            return Optional.of(autoWinner);
        }

        return Optional.empty();
    }

    public static Optional<Double> getTimeUntilNextActiveHub() {
        if (getAutoWinner().isEmpty()) {
            return Optional.empty();
        }

        double matchTime = MatchState.getMatchTime();

        if (matchTime > 105) {
            return Optional.of(matchTime - 105);
        } else if (matchTime > 80) {
            return Optional.of(matchTime - 80);
        } else if (matchTime > 55) {
            return Optional.of(matchTime - 55);
        } else if (matchTime > 30) {
            return Optional.of(matchTime - 30);
        }

        return Optional.empty();
    }

    public static Optional<Alliance> getAutoWinner() {
        String gameData = MatchState.getGameData().orElse("");
        return switch (gameData.length() > 0 ? gameData.charAt(0) : ' ') {
            case 'B' -> Optional.of(Alliance.BLUE);
            case 'R' -> Optional.of(Alliance.RED);
            default -> Optional.empty();
        };
    }

    private HubTracker() {
    }
}

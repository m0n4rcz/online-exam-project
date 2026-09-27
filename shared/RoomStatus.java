package shared;

/**
 * Standard statuses for exam rooms.
 * Meets requirement: "View a list of test rooms and the status of each room (not started, ongoing, finished)"
 */
public final class RoomStatus {

    private RoomStatus() {}

    /** Room is in lobby, waiting for participants; test has not started */
    public static final String NOT_STARTED = "NOT_STARTED";

    /** Test is actively in progress */
    public static final String ONGOING = "ONGOING";

    /** Test has concluded and room is finished */
    public static final String FINISHED = "FINISHED";

    public static boolean isJoinable(String status) {
        return NOT_STARTED.equalsIgnoreCase(status);
    }
}

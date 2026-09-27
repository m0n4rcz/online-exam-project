package shared.dtos;

import java.io.Serializable;

/**
 * Server's response to a join room request.
 * Rubric requirement: "Joining a test room"
 */
public class JoinRoomResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private RoomDetailsDTO roomDetails;

    public JoinRoomResponse() {}

    public JoinRoomResponse(boolean success, String message, RoomDetailsDTO roomDetails) {
        this.success = success;
        this.message = message;
        this.roomDetails = roomDetails;
    }

    public static JoinRoomResponse ok(RoomDetailsDTO roomDetails) {
        return new JoinRoomResponse(true, "Successfully joined room", roomDetails);
    }

    public static JoinRoomResponse fail(String message) {
        return new JoinRoomResponse(false, message, null);
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public RoomDetailsDTO getRoomDetails() { return roomDetails; }
    public void setRoomDetails(RoomDetailsDTO roomDetails) { this.roomDetails = roomDetails; }
}

package shared.dtos;

import java.io.Serializable;

/**
 * Server response after creating a room.
 * Rubric requirement: "Creating test rooms"
 */
public class CreateRoomResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private String roomCode;
    private RoomInfoDTO roomInfo;

    public CreateRoomResponse() {}

    public CreateRoomResponse(boolean success, String message, String roomCode, RoomInfoDTO roomInfo) {
        this.success = success;
        this.message = message;
        this.roomCode = roomCode;
        this.roomInfo = roomInfo;
    }

    public static CreateRoomResponse ok(String roomCode, RoomInfoDTO roomInfo) {
        return new CreateRoomResponse(true, "Room created successfully", roomCode, roomInfo);
    }

    public static CreateRoomResponse fail(String message) {
        return new CreateRoomResponse(false, message, null, null);
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }

    public RoomInfoDTO getRoomInfo() { return roomInfo; }
    public void setRoomInfo(RoomInfoDTO roomInfo) { this.roomInfo = roomInfo; }
}

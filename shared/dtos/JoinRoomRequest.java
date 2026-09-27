package shared.dtos;

import java.io.Serializable;

/**
 * Client request to join an existing test room.
 * Rubric requirement: "Joining a test room: 2 points"
 * "Join a test room if the test has not started yet."
 */
public class JoinRoomRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String roomCode;
    private String roomPassword; // Optional, required only if room is password protected

    public JoinRoomRequest() {}

    public JoinRoomRequest(String roomCode) {
        this.roomCode = roomCode;
    }

    public JoinRoomRequest(String roomCode, String roomPassword) {
        this.roomCode = roomCode;
        this.roomPassword = roomPassword;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getRoomPassword() {
        return roomPassword;
    }

    public void setRoomPassword(String roomPassword) {
        this.roomPassword = roomPassword;
    }
}

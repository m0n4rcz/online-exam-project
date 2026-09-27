package shared.dtos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Broadcast event sent by server to all clients in a room when the lobby state changes.
 */
public class RoomUpdateEventDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String roomCode;
    private String roomStatus;
    private String message;
    private List<RoomParticipantDTO> participants = new ArrayList<>();

    public RoomUpdateEventDTO() {}

    public RoomUpdateEventDTO(String roomCode, String roomStatus, String message, List<RoomParticipantDTO> participants) {
        this.roomCode = roomCode;
        this.roomStatus = roomStatus;
        this.message = message;
        this.participants = participants != null ? participants : new ArrayList<>();
    }

    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }

    public String getRoomStatus() { return roomStatus; }
    public void setRoomStatus(String roomStatus) { this.roomStatus = roomStatus; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<RoomParticipantDTO> getParticipants() { return participants; }
    public void setParticipants(List<RoomParticipantDTO> participants) {
        this.participants = participants != null ? participants : new ArrayList<>();
    }
}

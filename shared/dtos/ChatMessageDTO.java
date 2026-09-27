package shared.dtos;

import java.io.Serializable;

/**
 * Chat message sent in room lobby.
 */
public class ChatMessageDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String roomCode;
    private String senderUsername;
    private String senderDisplayName;
    private String message;
    private long timestamp;

    public ChatMessageDTO() {
        this.timestamp = System.currentTimeMillis();
    }

    public ChatMessageDTO(String roomCode, String senderUsername, String senderDisplayName, String message) {
        this.roomCode = roomCode;
        this.senderUsername = senderUsername;
        this.senderDisplayName = senderDisplayName;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }

    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

    public String getSenderDisplayName() { return senderDisplayName; }
    public void setSenderDisplayName(String senderDisplayName) { this.senderDisplayName = senderDisplayName; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return String.format("[%s]: %s", senderDisplayName != null ? senderDisplayName : senderUsername, message);
    }
}

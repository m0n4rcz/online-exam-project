package shared.dtos;

import java.io.Serializable;

/**
 * Client request to reclaim a dropped session and resume exam in progress.
 */
public class ReconnectRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String token;
    private String username;
    private String lastRoomCode;

    public ReconnectRequestDTO() {}

    public ReconnectRequestDTO(String token, String username, String lastRoomCode) {
        this.token = token;
        this.username = username;
        this.lastRoomCode = lastRoomCode;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getLastRoomCode() { return lastRoomCode; }
    public void setLastRoomCode(String lastRoomCode) { this.lastRoomCode = lastRoomCode; }
}

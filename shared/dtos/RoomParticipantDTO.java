package shared.dtos;

import java.io.Serializable;

/**
 * Represents a participant in an exam room lobby or active test.
 */
public class RoomParticipantDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String displayName;
    private boolean isHost;
    private boolean isReady;
    private boolean hasSubmitted;
    private double score;           // Populated after finish
    private int correctCount;       // Populated after finish
    private long joinedAt;

    public RoomParticipantDTO() {}

    public RoomParticipantDTO(String username, String displayName, boolean isHost, boolean isReady) {
        this.username = username;
        this.displayName = displayName;
        this.isHost = isHost;
        this.isReady = isReady;
        this.hasSubmitted = false;
        this.joinedAt = System.currentTimeMillis();
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public boolean isHost() { return isHost; }
    public void setHost(boolean host) { isHost = host; }

    public boolean isReady() { return isReady; }
    public void setReady(boolean ready) { isReady = ready; }

    public boolean isHasSubmitted() { return hasSubmitted; }
    public void setHasSubmitted(boolean hasSubmitted) { this.hasSubmitted = hasSubmitted; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public int getCorrectCount() { return correctCount; }
    public void setCorrectCount(int correctCount) { this.correctCount = correctCount; }

    public long getJoinedAt() { return joinedAt; }
    public void setJoinedAt(long joinedAt) { this.joinedAt = joinedAt; }
}

package shared.dtos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Detailed test results and leaderboard for a completed test room.
 * Rubric requirement: "Viewing test results of completed rooms: 1 point"
 * "View test results for completed rooms."
 */
public class CompletedRoomResultDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String roomCode;
    private String roomName;
    private String hostUsername;
    private String topic;
    private String difficulty;
    private int totalQuestions;
    private int durationMinutes;
    private long completedAt;
    private List<ParticipantResultDTO> leaderboard = new ArrayList<>();

    public CompletedRoomResultDTO() {}

    public CompletedRoomResultDTO(String roomCode, String roomName, String hostUsername,
                                  String topic, String difficulty, int totalQuestions,
                                  int durationMinutes, long completedAt,
                                  List<ParticipantResultDTO> leaderboard) {
        this.roomCode = roomCode;
        this.roomName = roomName;
        this.hostUsername = hostUsername;
        this.topic = topic;
        this.difficulty = difficulty;
        this.totalQuestions = totalQuestions;
        this.durationMinutes = durationMinutes;
        this.completedAt = completedAt;
        this.leaderboard = leaderboard != null ? leaderboard : new ArrayList<>();
    }

    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getHostUsername() { return hostUsername; }
    public void setHostUsername(String hostUsername) { this.hostUsername = hostUsername; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public long getCompletedAt() { return completedAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }

    public List<ParticipantResultDTO> getLeaderboard() { return leaderboard; }
    public void setLeaderboard(List<ParticipantResultDTO> leaderboard) {
        this.leaderboard = leaderboard != null ? leaderboard : new ArrayList<>();
    }
}

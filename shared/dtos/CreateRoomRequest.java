package shared.dtos;

import shared.QuestionCategory;
import shared.QuestionDifficulty;
import java.io.Serializable;

/**
 * Client request to create a new test room.
 * Rubric requirement: "Creating test rooms: 2 points"
 * "Users can create a new test room, set the number of questions, and define the test duration."
 * "Classify questions by difficulty and allow users to customize the type of questions included in the test."
 */
public class CreateRoomRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String roomName;
    private int questionCount;
    private int durationMinutes;
    private String topic;        // Category filter (e.g. QuestionCategory.ALL or specific topic)
    private String difficulty;   // Difficulty filter (e.g. QuestionDifficulty.ALL, EASY, etc.)
    private int maxParticipants;
    private String roomPassword; // Optional password for private room

    public CreateRoomRequest() {
        this.questionCount = 10;
        this.durationMinutes = 15;
        this.topic = QuestionCategory.ALL;
        this.difficulty = QuestionDifficulty.ALL;
        this.maxParticipants = 30;
    }

    public CreateRoomRequest(String roomName, int questionCount, int durationMinutes,
                             String topic, String difficulty, int maxParticipants, String roomPassword) {
        this.roomName = roomName;
        this.questionCount = questionCount > 0 ? questionCount : 10;
        this.durationMinutes = durationMinutes > 0 ? durationMinutes : 15;
        this.topic = topic != null ? topic : QuestionCategory.ALL;
        this.difficulty = difficulty != null ? difficulty : QuestionDifficulty.ALL;
        this.maxParticipants = maxParticipants > 0 ? maxParticipants : 30;
        this.roomPassword = roomPassword;
    }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public int getQuestionCount() { return questionCount; }
    public void setQuestionCount(int questionCount) { this.questionCount = questionCount; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public int getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }

    public String getRoomPassword() { return roomPassword; }
    public void setRoomPassword(String roomPassword) { this.roomPassword = roomPassword; }
}

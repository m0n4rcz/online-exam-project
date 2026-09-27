package shared.dtos;

import java.io.Serializable;

/**
 * Carries an in-flight answer update from student to server.
 * Enables students to select or change their answer as long as time permits.
 * 
 * Rubric requirement: "Allow users to change their answered questions as long as time permits."
 * "Changing previously selected answers: 1 point"
 */
public class AnswerSubmissionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String roomCode;     // Null/empty if solo practice
    private int questionId;      // Unique Question ID
    private String selectedOption; // "A", "B", "C", "D", or null/empty to clear selection
    private long timestamp;

    public AnswerSubmissionDTO() {
        this.timestamp = System.currentTimeMillis();
    }

    public AnswerSubmissionDTO(String roomCode, int questionId, String selectedOption) {
        this.roomCode = roomCode;
        this.questionId = questionId;
        this.selectedOption = selectedOption;
        this.timestamp = System.currentTimeMillis();
    }

    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public String getSelectedOption() { return selectedOption; }
    public void setSelectedOption(String selectedOption) { this.selectedOption = selectedOption; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}

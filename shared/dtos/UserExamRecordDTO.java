package shared.dtos;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Historical record of an exam completed by a student (Practice or Multiplayer Room).
 * 
 * Rubric requirement: "Store information about tests that users have taken and display it upon request."
 * "Storing test information + graphical statistics: 2 points"
 */
public class UserExamRecordDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private long recordId;
    private String username;
    private String testType;            // "PRACTICE" or "ROOM"
    private String title;               // e.g. "Java Core Exam" or "Multiplayer Room ABC123"
    private String roomCode;            // Null if solo practice
    private String topic;
    private String difficulty;
    private double score;               // e.g. 8.5 / 10.0
    private int correctCount;
    private int totalQuestions;
    private int timeSpentSeconds;
    private long takenAt;
    private String takenAtFormatted;

    public UserExamRecordDTO() {
        this.takenAt = System.currentTimeMillis();
        this.takenAtFormatted = formatDate(this.takenAt);
    }

    public UserExamRecordDTO(long recordId, String username, String testType, String title,
                             String roomCode, String topic, String difficulty, double score,
                             int correctCount, int totalQuestions, int timeSpentSeconds, long takenAt) {
        this.recordId = recordId;
        this.username = username;
        this.testType = testType;
        this.title = title;
        this.roomCode = roomCode;
        this.topic = topic;
        this.difficulty = difficulty;
        this.score = score;
        this.correctCount = correctCount;
        this.totalQuestions = totalQuestions;
        this.timeSpentSeconds = timeSpentSeconds;
        this.takenAt = takenAt;
        this.takenAtFormatted = formatDate(takenAt);
    }

    private static String formatDate(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(new Date(time));
    }

    public double getAccuracyPercentage() {
        if (totalQuestions <= 0) return 0.0;
        return (correctCount * 100.0) / totalQuestions;
    }

    public long getRecordId() { return recordId; }
    public void setRecordId(long recordId) { this.recordId = recordId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getTestType() { return testType; }
    public void setTestType(String testType) { this.testType = testType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public int getCorrectCount() { return correctCount; }
    public void setCorrectCount(int correctCount) { this.correctCount = correctCount; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getTimeSpentSeconds() { return timeSpentSeconds; }
    public void setTimeSpentSeconds(int timeSpentSeconds) { this.timeSpentSeconds = timeSpentSeconds; }

    public long getTakenAt() { return takenAt; }
    public void setTakenAt(long takenAt) { 
        this.takenAt = takenAt;
        this.takenAtFormatted = formatDate(takenAt);
    }

    public String getTakenAtFormatted() { return takenAtFormatted; }
    public void setTakenAtFormatted(String takenAtFormatted) { this.takenAtFormatted = takenAtFormatted; }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s) - Score: %.1f (%d/%d) on %s",
                testType, title, topic, score, correctCount, totalQuestions, takenAtFormatted);
    }
}

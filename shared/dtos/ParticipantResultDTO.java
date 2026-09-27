package shared.dtos;

import java.io.Serializable;

/**
 * Result of a single participant in a completed room.
 */
public class ParticipantResultDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int rank;
    private String username;
    private String displayName;
    private double score;
    private int correctCount;
    private int totalQuestions;
    private boolean submittedEarly;
    private int timeSpentSeconds;

    public ParticipantResultDTO() {}

    public ParticipantResultDTO(int rank, String username, String displayName, double score,
                                int correctCount, int totalQuestions, boolean submittedEarly, int timeSpentSeconds) {
        this.rank = rank;
        this.username = username;
        this.displayName = displayName;
        this.score = score;
        this.correctCount = correctCount;
        this.totalQuestions = totalQuestions;
        this.submittedEarly = submittedEarly;
        this.timeSpentSeconds = timeSpentSeconds;
    }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public int getCorrectCount() { return correctCount; }
    public void setCorrectCount(int correctCount) { this.correctCount = correctCount; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public boolean isSubmittedEarly() { return submittedEarly; }
    public void setSubmittedEarly(boolean submittedEarly) { this.submittedEarly = submittedEarly; }

    public int getTimeSpentSeconds() { return timeSpentSeconds; }
    public void setTimeSpentSeconds(int timeSpentSeconds) { this.timeSpentSeconds = timeSpentSeconds; }

    @Override
    public String toString() {
        return String.format("#%d %s - Score: %.1f (%d/%d correct)", rank, displayName, score, correctCount, totalQuestions);
    }
}

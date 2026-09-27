package shared.dtos;

import java.io.Serializable;

/**
 * Entry in the global or topic leaderboard.
 */
public class LeaderboardEntryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int rank;
    private String username;
    private String displayName;
    private int totalExams;
    private double averageScore;
    private double highestScore;
    private double accuracyPercentage;

    public LeaderboardEntryDTO() {}

    public LeaderboardEntryDTO(int rank, String username, String displayName, int totalExams,
                               double averageScore, double highestScore, double accuracyPercentage) {
        this.rank = rank;
        this.username = username;
        this.displayName = displayName;
        this.totalExams = totalExams;
        this.averageScore = averageScore;
        this.highestScore = highestScore;
        this.accuracyPercentage = accuracyPercentage;
    }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public int getTotalExams() { return totalExams; }
    public void setTotalExams(int totalExams) { this.totalExams = totalExams; }

    public double getAverageScore() { return averageScore; }
    public void setAverageScore(double averageScore) { this.averageScore = averageScore; }

    public double getHighestScore() { return highestScore; }
    public void setHighestScore(double highestScore) { this.highestScore = highestScore; }

    public double getAccuracyPercentage() { return accuracyPercentage; }
    public void setAccuracyPercentage(double accuracyPercentage) { this.accuracyPercentage = accuracyPercentage; }

    @Override
    public String toString() {
        return String.format("#%d %s - Avg: %.1f, High: %.1f (%d exams)", rank, displayName, averageScore, highestScore, totalExams);
    }
}

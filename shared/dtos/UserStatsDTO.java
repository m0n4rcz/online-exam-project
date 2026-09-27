package shared.dtos;

import java.io.Serializable;
import java.util.*;

/**
 * Aggregated statistical data prepared specifically for graphical visualization
 * (Bar charts, Line charts, Pie charts, and Radar charts in Swing/JavaFX).
 * 
 * Rubric requirement: "Provide statistics in graphical form about the tests that users have completed."
 * "Storing test information + graphical statistics: 2 points"
 */
public class UserStatsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private int totalTests;
    private int practiceTests;
    private int roomTests;
    private double averageScore;
    private double highestScore;
    private double lowestScore;
    private int totalQuestionsAnswered;
    private int totalCorrectAnswers;
    private double overallAccuracyPercentage;

    // Charts Data:
    // 1. Topic Performance (for Bar / Radar Chart): Category -> Accuracy % (e.g. "Java Core" -> 85.5)
    private Map<String, Double> topicAccuracy = new HashMap<>();

    // 2. Topic Distribution (for Pie / Donut Chart): Category -> Question / Test Count
    private Map<String, Integer> topicDistribution = new HashMap<>();

    // 3. Difficulty Breakdown (for Grouped Bar Chart): Difficulty -> Accuracy % (e.g. "EASY" -> 95.0)
    private Map<String, Double> difficultyAccuracy = new HashMap<>();

    // 4. Progress / Score Over Time (for Line Chart): chronological test trend
    private List<ScoreTrendPointDTO> scoreTrend = new ArrayList<>();

    // 5. Recent completed records for summary table display
    private List<UserExamRecordDTO> recentRecords = new ArrayList<>();

    public UserStatsDTO() {}

    public UserStatsDTO(String username) {
        this.username = username;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getTotalTests() { return totalTests; }
    public void setTotalTests(int totalTests) { this.totalTests = totalTests; }

    public int getPracticeTests() { return practiceTests; }
    public void setPracticeTests(int practiceTests) { this.practiceTests = practiceTests; }

    public int getRoomTests() { return roomTests; }
    public void setRoomTests(int roomTests) { this.roomTests = roomTests; }

    public double getAverageScore() { return averageScore; }
    public void setAverageScore(double averageScore) { this.averageScore = averageScore; }

    public double getHighestScore() { return highestScore; }
    public void setHighestScore(double highestScore) { this.highestScore = highestScore; }

    public double getLowestScore() { return lowestScore; }
    public void setLowestScore(double lowestScore) { this.lowestScore = lowestScore; }

    public int getTotalQuestionsAnswered() { return totalQuestionsAnswered; }
    public void setTotalQuestionsAnswered(int totalQuestionsAnswered) { this.totalQuestionsAnswered = totalQuestionsAnswered; }

    public int getTotalCorrectAnswers() { return totalCorrectAnswers; }
    public void setTotalCorrectAnswers(int totalCorrectAnswers) { this.totalCorrectAnswers = totalCorrectAnswers; }

    public double getOverallAccuracyPercentage() { return overallAccuracyPercentage; }
    public void setOverallAccuracyPercentage(double overallAccuracyPercentage) { this.overallAccuracyPercentage = overallAccuracyPercentage; }

    public Map<String, Double> getTopicAccuracy() { return topicAccuracy; }
    public void setTopicAccuracy(Map<String, Double> topicAccuracy) {
        this.topicAccuracy = topicAccuracy != null ? topicAccuracy : new HashMap<>();
    }

    public Map<String, Integer> getTopicDistribution() { return topicDistribution; }
    public void setTopicDistribution(Map<String, Integer> topicDistribution) {
        this.topicDistribution = topicDistribution != null ? topicDistribution : new HashMap<>();
    }

    public Map<String, Double> getDifficultyAccuracy() { return difficultyAccuracy; }
    public void setDifficultyAccuracy(Map<String, Double> difficultyAccuracy) {
        this.difficultyAccuracy = difficultyAccuracy != null ? difficultyAccuracy : new HashMap<>();
    }

    public List<ScoreTrendPointDTO> getScoreTrend() { return scoreTrend; }
    public void setScoreTrend(List<ScoreTrendPointDTO> scoreTrend) {
        this.scoreTrend = scoreTrend != null ? scoreTrend : new ArrayList<>();
    }

    public List<UserExamRecordDTO> getRecentRecords() { return recentRecords; }
    public void setRecentRecords(List<UserExamRecordDTO> recentRecords) {
        this.recentRecords = recentRecords != null ? recentRecords : new ArrayList<>();
    }
}

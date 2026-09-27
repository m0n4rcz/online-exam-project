package shared.dtos;

import java.io.Serializable;

/**
 * Data point for score progression and trendline graphing.
 */
public class ScoreTrendPointDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int testIndex;      // 1, 2, 3...
    private String label;       // e.g. "Test #1" or "2026-09-20"
    private double score;       // Score value (0.0 - 10.0)
    private double accuracy;    // Accuracy percentage (0.0 - 100.0)

    public ScoreTrendPointDTO() {}

    public ScoreTrendPointDTO(int testIndex, String label, double score, double accuracy) {
        this.testIndex = testIndex;
        this.label = label;
        this.score = score;
        this.accuracy = accuracy;
    }

    public int getTestIndex() { return testIndex; }
    public void setTestIndex(int testIndex) { this.testIndex = testIndex; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public double getAccuracy() { return accuracy; }
    public void setAccuracy(double accuracy) { this.accuracy = accuracy; }
}

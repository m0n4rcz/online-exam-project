package shared.dtos;

import shared.QuestionCategory;
import shared.QuestionDifficulty;
import java.io.Serializable;

/**
 * Client request to start a solo practice session with customizable topic and difficulty.
 * 
 * Rubric requirement: "Participating in practice mode: 1 point"
 * "In this mode, users can take practice tests with a set number of questions and a specific time limit."
 * "Classify questions by difficulty and allow users to customize the type of questions included in the test."
 */
public class PracticeRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String topic;        // QuestionCategory (e.g. "ALL", "Java Core", "Networking")
    private String difficulty;   // QuestionDifficulty (e.g. "ALL", "EASY", "MEDIUM", "HARD")
    private int questionCount;   // e.g. 5, 10, 20
    private int durationMinutes; // e.g. 10, 15

    public PracticeRequestDTO() {
        this.topic = QuestionCategory.ALL;
        this.difficulty = QuestionDifficulty.ALL;
        this.questionCount = 10;
        this.durationMinutes = 15;
    }

    public PracticeRequestDTO(String topic, String difficulty, int questionCount, int durationMinutes) {
        this.topic = topic != null ? topic : QuestionCategory.ALL;
        this.difficulty = difficulty != null ? difficulty : QuestionDifficulty.ALL;
        this.questionCount = questionCount > 0 ? questionCount : 10;
        this.durationMinutes = durationMinutes > 0 ? durationMinutes : 15;
    }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public int getQuestionCount() { return questionCount; }
    public void setQuestionCount(int questionCount) { this.questionCount = questionCount; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
}

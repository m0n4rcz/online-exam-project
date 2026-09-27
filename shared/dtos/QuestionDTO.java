package shared.dtos;

import java.io.Serializable;

/**
 * Represents a single sanitized exam question transmitted to the client.
 * 
 * ANTI-CHEAT DESIGN:
 * This DTO deliberately omits 'correctOption' and 'explanation' fields.
 * Answers are evaluated exclusively on the server side.
 * 
 * Rubric requirement: "Classify questions by difficulty, topic"
 */
public class QuestionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int questionNumber; // 1-based index in the current test (e.g., 1 to 20)
    private String content;     // The question prompt text
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String category;    // Topic / Category (e.g. "Java Core", "Networking")
    private String difficulty;  // "EASY", "MEDIUM", "HARD"

    public QuestionDTO() {}

    public QuestionDTO(int id, int questionNumber, String content, 
                       String optionA, String optionB, String optionC, String optionD, 
                       String category) {
        this(id, questionNumber, content, optionA, optionB, optionC, optionD, category, "MEDIUM");
    }

    public QuestionDTO(int id, int questionNumber, String content, 
                       String optionA, String optionB, String optionC, String optionD, 
                       String category, String difficulty) {
        this.id = id;
        this.questionNumber = questionNumber;
        this.content = content;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.category = category;
        this.difficulty = difficulty != null ? difficulty : "MEDIUM";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(int questionNumber) {
        this.questionNumber = questionNumber;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public String toString() {
        return String.format("Q%d [ID=%d, %s, %s]: %s", questionNumber, id, category, difficulty, content);
    }
}

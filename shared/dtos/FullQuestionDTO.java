package shared.dtos;

import java.io.Serializable;

/**
 * Full question representation used for question bank management, administrative editing,
 * and server-side evaluation.
 * 
 * Rubric requirement: "Classifying questions by difficulty, topic: 1-3 points"
 */
public class FullQuestionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String content;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctOption;   // "A", "B", "C", or "D"
    private String explanation;
    private String category;        // Topic / Category
    private String difficulty;      // "EASY", "MEDIUM", "HARD"

    public FullQuestionDTO() {}

    public FullQuestionDTO(int id, String content, String optionA, String optionB,
                           String optionC, String optionD, String correctOption,
                           String explanation, String category, String difficulty) {
        this.id = id;
        this.content = content;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = correctOption;
        this.explanation = explanation;
        this.category = category;
        this.difficulty = difficulty;
    }

    /**
     * Converts to sanitized QuestionDTO (omits correctOption and explanation)
     * for safe transmission to students during exams.
     */
    public QuestionDTO toSanitizedDTO(int questionNumber) {
        return new QuestionDTO(id, questionNumber, content, optionA, optionB, optionC, optionD, category, difficulty);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }

    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }

    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }

    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }

    public String getCorrectOption() { return correctOption; }
    public void setCorrectOption(String correctOption) { this.correctOption = correctOption; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
}

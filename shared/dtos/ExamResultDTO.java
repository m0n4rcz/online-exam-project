package shared.dtos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Server -> Client: Final graded results sent after submission or time expiration.
 * 
 * Rubric requirement: "After the test time ends, the server notifies users of the number of correct answers."
 * "Submitting and scoring the test: 2 points"
 */
public class ExamResultDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    public static class QuestionReview implements Serializable {
        private static final long serialVersionUID = 1L;

        private int questionId;
        private int questionNumber;
        private String prompt;
        private String studentChoice;
        private String correctChoice;
        private boolean isCorrect;
        private String explanation;

        public QuestionReview() {}

        public QuestionReview(int questionId, int questionNumber, String prompt, 
                              String studentChoice, String correctChoice, 
                              boolean isCorrect, String explanation) {
            this.questionId = questionId;
            this.questionNumber = questionNumber;
            this.prompt = prompt;
            this.studentChoice = studentChoice;
            this.correctChoice = correctChoice;
            this.isCorrect = isCorrect;
            this.explanation = explanation;
        }

        public int getQuestionId() { return questionId; }
        public void setQuestionId(int questionId) { this.questionId = questionId; }

        public int getQuestionNumber() { return questionNumber; }
        public void setQuestionNumber(int questionNumber) { this.questionNumber = questionNumber; }

        public String getPrompt() { return prompt; }
        public void setPrompt(String prompt) { this.prompt = prompt; }

        public String getStudentChoice() { return studentChoice; }
        public void setStudentChoice(String studentChoice) { this.studentChoice = studentChoice; }

        public String getCorrectChoice() { return correctChoice; }
        public void setCorrectChoice(String correctChoice) { this.correctChoice = correctChoice; }

        public boolean isCorrect() { return isCorrect; }
        public void setCorrect(boolean correct) { isCorrect = correct; }

        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
    }

    private String studentUsername;
    private double score;           // e.g. 8.5 / 10.0
    private int correctCount;       // Number of correct answers (rubric requirement)
    private int totalQuestions;
    private int rank;               // Rank within the room (1st, 2nd, etc. - 0 if practice)
    private int totalParticipants; // 1 if practice
    private int timeSpentSeconds;
    private boolean isPractice;
    private List<QuestionReview> reviews = new ArrayList<>();

    public ExamResultDTO() {}

    public ExamResultDTO(String studentUsername, double score, int correctCount, 
                         int totalQuestions, int rank, int totalParticipants, 
                         List<QuestionReview> reviews) {
        this(studentUsername, score, correctCount, totalQuestions, rank, totalParticipants, 0, false, reviews);
    }

    public ExamResultDTO(String studentUsername, double score, int correctCount, 
                         int totalQuestions, int rank, int totalParticipants,
                         int timeSpentSeconds, boolean isPractice,
                         List<QuestionReview> reviews) {
        this.studentUsername = studentUsername;
        this.score = score;
        this.correctCount = correctCount;
        this.totalQuestions = totalQuestions;
        this.rank = rank;
        this.totalParticipants = totalParticipants;
        this.timeSpentSeconds = timeSpentSeconds;
        this.isPractice = isPractice;
        this.reviews = reviews != null ? reviews : new ArrayList<>();
    }

    public double getAccuracyPercentage() {
        if (totalQuestions <= 0) return 0.0;
        return (correctCount * 100.0) / totalQuestions;
    }

    public String getStudentUsername() { return studentUsername; }
    public void setStudentUsername(String studentUsername) { this.studentUsername = studentUsername; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public int getCorrectCount() { return correctCount; }
    public void setCorrectCount(int correctCount) { this.correctCount = correctCount; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public int getTotalParticipants() { return totalParticipants; }
    public void setTotalParticipants(int totalParticipants) { this.totalParticipants = totalParticipants; }

    public int getTimeSpentSeconds() { return timeSpentSeconds; }
    public void setTimeSpentSeconds(int timeSpentSeconds) { this.timeSpentSeconds = timeSpentSeconds; }

    public boolean isPractice() { return isPractice; }
    public void setPractice(boolean practice) { isPractice = practice; }

    public List<QuestionReview> getReviews() { return reviews; }
    public void setReviews(List<QuestionReview> reviews) { this.reviews = reviews; }

    @Override
    public String toString() {
        return String.format("Result for %s: %d/%d Correct (Score: %.1f/10.0, Rank: #%d/%d)",
                studentUsername, correctCount, totalQuestions, score, rank, totalParticipants);
    }
}

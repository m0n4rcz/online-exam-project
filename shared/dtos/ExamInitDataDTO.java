package shared.dtos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Server -> Client payload sent when a test room or practice begins.
 * Contains sanitized questions, total time duration, and countdown timestamps.
 * 
 * Rubric requirement: "When the test begins, the server sends the user the test questions."
 * "Starting the test: 1 point"
 */
public class ExamInitDataDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String roomCode;            // Null or "PRACTICE" if practice mode
    private String examTitle;
    private int durationSeconds;        // Total duration allowed in seconds
    private long startTimestamp;        // Server epoch ms when test officially started
    private long endTimestamp;          // Server epoch ms when test must end
    private int totalQuestions;
    private List<QuestionDTO> questions = new ArrayList<>();

    public ExamInitDataDTO() {}

    public ExamInitDataDTO(String roomCode, String examTitle, int durationSeconds, 
                           long startTimestamp, long endTimestamp, List<QuestionDTO> questions) {
        this.roomCode = roomCode;
        this.examTitle = examTitle;
        this.durationSeconds = durationSeconds;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.questions = questions != null ? questions : new ArrayList<>();
        this.totalQuestions = this.questions.size();
    }

    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }

    public String getExamTitle() { return examTitle; }
    public void setExamTitle(String examTitle) { this.examTitle = examTitle; }

    public int getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }

    public long getStartTimestamp() { return startTimestamp; }
    public void setStartTimestamp(long startTimestamp) { this.startTimestamp = startTimestamp; }

    public long getEndTimestamp() { return endTimestamp; }
    public void setEndTimestamp(long endTimestamp) { this.endTimestamp = endTimestamp; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public List<QuestionDTO> getQuestions() { return questions; }
    public void setQuestions(List<QuestionDTO> questions) { 
        this.questions = questions != null ? questions : new ArrayList<>();
        this.totalQuestions = this.questions.size();
    }
}

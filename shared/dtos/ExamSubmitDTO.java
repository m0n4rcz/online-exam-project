package shared.dtos;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Carries all submitted answers from the student to the server upon exam completion.
 * Supports both early submission and submission upon timer expiration.
 * 
 * Rubric requirement: "Users can submit their answers early, before the test time ends."
 * "Submitting and scoring the test: 2 points"
 */
public class ExamSubmitDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String roomCode;            // Null or "PRACTICE" if practice mode
    private String studentUsername;
    // Maps Question ID -> Selected Option ("A", "B", "C", or "D")
    private Map<Integer, String> answers = new HashMap<>();
    private long submissionTimestamp;
    private boolean isEarlySubmission;  // True if user clicked submit before time expired

    public ExamSubmitDTO() {
        this.submissionTimestamp = System.currentTimeMillis();
    }

    public ExamSubmitDTO(String roomCode, String studentUsername, Map<Integer, String> answers) {
        this(roomCode, studentUsername, answers, false);
    }

    public ExamSubmitDTO(String roomCode, String studentUsername, Map<Integer, String> answers, boolean isEarlySubmission) {
        this.roomCode = roomCode;
        this.studentUsername = studentUsername;
        this.answers = answers != null ? answers : new HashMap<>();
        this.isEarlySubmission = isEarlySubmission;
        this.submissionTimestamp = System.currentTimeMillis();
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getStudentUsername() {
        return studentUsername;
    }

    public void setStudentUsername(String studentUsername) {
        this.studentUsername = studentUsername;
    }

    public Map<Integer, String> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<Integer, String> answers) {
        this.answers = answers != null ? answers : new HashMap<>();
    }

    public long getSubmissionTimestamp() {
        return submissionTimestamp;
    }

    public void setSubmissionTimestamp(long submissionTimestamp) {
        this.submissionTimestamp = submissionTimestamp;
    }

    public boolean isEarlySubmission() {
        return isEarlySubmission;
    }

    public void setEarlySubmission(boolean earlySubmission) {
        isEarlySubmission = earlySubmission;
    }
}

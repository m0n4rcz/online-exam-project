package shared.dtos;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Server response restoring active exam state after reconnection.
 */
public class ReconnectResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private boolean inRoom;
    private boolean inExam;
    private int remainingSeconds;
    private RoomDetailsDTO roomDetails;
    private ExamInitDataDTO examData;
    // Maps Question ID -> Previously selected option ("A", "B", etc.)
    private Map<Integer, String> currentAnswers = new HashMap<>();

    public ReconnectResponseDTO() {}

    public ReconnectResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isInRoom() { return inRoom; }
    public void setInRoom(boolean inRoom) { this.inRoom = inRoom; }

    public boolean isInExam() { return inExam; }
    public void setInExam(boolean inExam) { this.inExam = inExam; }

    public int getRemainingSeconds() { return remainingSeconds; }
    public void setRemainingSeconds(int remainingSeconds) { this.remainingSeconds = remainingSeconds; }

    public RoomDetailsDTO getRoomDetails() { return roomDetails; }
    public void setRoomDetails(RoomDetailsDTO roomDetails) { this.roomDetails = roomDetails; }

    public ExamInitDataDTO getExamData() { return examData; }
    public void setExamData(ExamInitDataDTO examData) { this.examData = examData; }

    public Map<Integer, String> getCurrentAnswers() { return currentAnswers; }
    public void setCurrentAnswers(Map<Integer, String> currentAnswers) {
        this.currentAnswers = currentAnswers != null ? currentAnswers : new HashMap<>();
    }
}

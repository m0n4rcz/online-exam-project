package shared.dtos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Detailed information about a room including all connected participants.
 */
public class RoomDetailsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private RoomInfoDTO roomInfo;
    private List<RoomParticipantDTO> participants = new ArrayList<>();

    public RoomDetailsDTO() {}

    public RoomDetailsDTO(RoomInfoDTO roomInfo, List<RoomParticipantDTO> participants) {
        this.roomInfo = roomInfo;
        this.participants = participants != null ? participants : new ArrayList<>();
    }

    public RoomInfoDTO getRoomInfo() {
        return roomInfo;
    }

    public void setRoomInfo(RoomInfoDTO roomInfo) {
        this.roomInfo = roomInfo;
    }

    public List<RoomParticipantDTO> getParticipants() {
        return participants;
    }

    public void setParticipants(List<RoomParticipantDTO> participants) {
        this.participants = participants != null ? participants : new ArrayList<>();
    }
}

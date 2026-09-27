package shared.dtos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Server response providing the list of active/existing test rooms.
 * Rubric requirement: "Viewing the list of test rooms: 1 point"
 */
public class RoomListResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<RoomInfoDTO> rooms = new ArrayList<>();

    public RoomListResponse() {}

    public RoomListResponse(List<RoomInfoDTO> rooms) {
        this.rooms = rooms != null ? rooms : new ArrayList<>();
    }

    public List<RoomInfoDTO> getRooms() {
        return rooms;
    }

    public void setRooms(List<RoomInfoDTO> rooms) {
        this.rooms = rooms != null ? rooms : new ArrayList<>();
    }
}

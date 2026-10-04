package server.room;

import shared.RoomStatus;
import shared.dtos.CreateRoomRequest;
import shared.dtos.RoomInfoDTO;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all active multiplayer test rooms and user lobby assignments.
 */
public class RoomManager {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final Map<String, ExamRoom> rooms = new ConcurrentHashMap<>();
    private final Map<String, String> userRooms = new ConcurrentHashMap<>(); // username (lowercase) -> roomCode

    /**
     * Creates a new multiplayer exam room.
     */
    public ExamRoom createRoom(CreateRoomRequest req, String hostUsername, String hostDisplayName) {
        String roomCode = generateUniqueRoomCode();
        String title = (req.getRoomName() != null && !req.getRoomName().trim().isEmpty())
                ? req.getRoomName().trim()
                : "Exam Room " + roomCode;

        ExamRoom room = new ExamRoom(
                roomCode,
                title,
                hostUsername,
                hostDisplayName,
                req.getTopic(),
                req.getDifficulty(),
                req.getQuestionCount(),
                req.getDurationMinutes(),
                req.getMaxParticipants(),
                req.getRoomPassword()
        );

        rooms.put(roomCode.toUpperCase(), room);
        userRooms.put(hostUsername.toLowerCase(), roomCode.toUpperCase());
        return room;
    }

    /**
     * Fetches an exam room by code.
     */
    public ExamRoom getRoom(String roomCode) {
        if (roomCode == null) return null;
        return rooms.get(roomCode.trim().toUpperCase());
    }

    /**
     * Returns a summary list of all available rooms.
     */
    public List<RoomInfoDTO> getRoomList() {
        List<RoomInfoDTO> list = new ArrayList<>();
        for (ExamRoom r : rooms.values()) {
            list.add(r.toRoomInfoDTO());
        }
        // Newest rooms first
        list.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
        return list;
    }

    /**
     * Associates a user with a room upon successful join.
     */
    public void bindUserToRoom(String username, String roomCode) {
        userRooms.put(username.toLowerCase(), roomCode.toUpperCase());
    }

    /**
     * Removes user from room tracking.
     */
    public void unbindUserFromRoom(String username) {
        userRooms.remove(username.toLowerCase());
    }

    /**
     * Gets the room code a user is currently participating in.
     */
    public String getUserRoomCode(String username) {
        if (username == null) return null;
        return userRooms.get(username.toLowerCase());
    }

    /**
     * Removes a completed or empty room.
     */
    public void removeRoom(String roomCode) {
        if (roomCode == null) return;
        ExamRoom r = rooms.remove(roomCode.toUpperCase());
        if (r != null) {
            for (String u : r.getParticipants().keySet()) {
                userRooms.remove(u.toLowerCase());
            }
        }
    }

    /**
     * Handles unexpected player disconnects.
     */
    public void handleDisconnect(String username) {
        if (username == null) return;
        String code = userRooms.remove(username.toLowerCase());
        if (code != null) {
            ExamRoom room = rooms.get(code);
            if (room != null) {
                // If room is NOT_STARTED and host left, assign new host or delete
                if (RoomStatus.NOT_STARTED.equals(room.getStatus())) {
                    room.removeParticipant(username);
                    if (room.getParticipants().isEmpty()) {
                        rooms.remove(code);
                    }
                }
            }
        }
    }

    private String generateUniqueRoomCode() {
        for (int attempt = 0; attempt < 100; attempt++) {
            StringBuilder sb = new StringBuilder("ROOM");
            for (int i = 0; i < 3; i++) {
                sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
            }
            String code = sb.toString();
            if (!rooms.containsKey(code)) {
                return code;
            }
        }
        return "R" + System.currentTimeMillis() % 100000;
    }
}

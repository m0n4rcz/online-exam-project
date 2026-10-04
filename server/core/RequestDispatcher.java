package server.core;

import server.db.dao.ActivityLogDAO;
import server.db.dao.ExamRecordDAO;
import server.db.dao.QuestionDAO;
import server.db.dao.UserDAO;
import server.room.ExamRoom;
import server.room.RoomManager;
import shared.OpCodes;
import shared.Packet;
import shared.RoomStatus;
import shared.UserRole;
import shared.dtos.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dispatches application-level OpCodes received from clients to their corresponding
 * database DAO operations, multiplayer room coordinator, and business logic handlers.
 */
public class RequestDispatcher {

    private static final Logger LOGGER = Logger.getLogger(RequestDispatcher.class.getName());

    private final UserDAO userDAO;
    private final QuestionDAO questionDAO;
    private final ExamRecordDAO examRecordDAO;
    private final ActivityLogDAO logDAO;
    private final SessionManager sessionManager;
    private final RoomManager roomManager;

    public RequestDispatcher(UserDAO userDAO, QuestionDAO questionDAO, ExamRecordDAO examRecordDAO,
                             ActivityLogDAO logDAO, SessionManager sessionManager, RoomManager roomManager) {
        this.userDAO = userDAO;
        this.questionDAO = questionDAO;
        this.examRecordDAO = examRecordDAO;
        this.logDAO = logDAO;
        this.sessionManager = sessionManager;
        this.roomManager = roomManager;
    }

    /**
     * Dispatches an incoming packet and returns the response packet (or null if no direct response needed).
     */
    public Packet dispatch(ClientSession session, Packet req) {
        short op = req.getOpCode();
        LOGGER.info(String.format("Dispatching %s from %s", OpCodes.getName(op), session));

        try {
            return switch (op) {
                // System & Heartbeat
                case OpCodes.PING               -> handlePing(session, req);

                // Authentication & Account
                case OpCodes.LOGIN_REQ          -> handleLogin(session, req);
                case OpCodes.REGISTER_REQ       -> handleRegister(session, req);
                case OpCodes.LOGOUT_REQ         -> handleLogout(session, req);
                case OpCodes.UPDATE_PROFILE_REQ -> handleUpdateProfile(session, req);
                case OpCodes.SESSION_CHECK_REQ  -> handleSessionCheck(session, req);

                // Room & Lobby Coordination
                case OpCodes.CREATE_ROOM_REQ    -> handleCreateRoom(session, req);
                case OpCodes.JOIN_ROOM_REQ      -> handleJoinRoom(session, req);
                case OpCodes.TOGGLE_READY_REQ   -> handleToggleReady(session, req);
                case OpCodes.START_ROOM_REQ     -> handleStartRoom(session, req);
                case OpCodes.LEAVE_ROOM_REQ     -> handleLeaveRoom(session, req);
                case OpCodes.HOST_END_EXAM_REQ  -> handleHostEndExam(session, req);
                case OpCodes.ROOM_CHAT_REQ      -> handleRoomChat(session, req);
                case OpCodes.ROOM_LIST_REQ      -> handleRoomList(session, req);
                case OpCodes.COMPLETED_ROOM_RESULT_REQ -> handleCompletedRoomResult(session, req);

                // Multiplayer Exam Engine
                case OpCodes.SUBMIT_ANSWER_REQ  -> handleSubmitAnswer(session, req);
                case OpCodes.SUBMIT_EXAM_REQ    -> handleSubmitExam(session, req);

                // Solo Practice Mode
                case OpCodes.PRACTICE_REQ       -> handlePracticeRequest(session, req);
                case OpCodes.PRACTICE_SUBMIT_REQ-> handlePracticeSubmit(session, req);

                // History, Graphical Stats & Leaderboard
                case OpCodes.USER_HISTORY_REQ   -> handleUserHistory(session, req);
                case OpCodes.USER_STATS_REQ     -> handleUserStats(session, req);
                case OpCodes.LEADERBOARD_REQ    -> handleLeaderboard(session, req);

                // Question Bank & Audit Logging (Admin/Teacher)
                case OpCodes.GET_LOGS_REQ       -> handleGetLogs(session, req);
                case OpCodes.QUESTION_LIST_REQ  -> handleQuestionList(session, req);
                case OpCodes.QUESTION_ADD_REQ   -> handleQuestionAdd(session, req);
                case OpCodes.QUESTION_UPDATE_REQ-> handleQuestionUpdate(session, req);
                case OpCodes.QUESTION_DELETE_REQ-> handleQuestionDelete(session, req);

                default -> Packet.error(op, OpCodes.STATUS_BAD_REQUEST, "Unknown or unhandled OpCode: " + OpCodes.getName(op));
            };
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error handling " + OpCodes.getName(op) + ": " + e.getMessage(), e);
            return Packet.error(op, OpCodes.STATUS_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    // ==========================================
    // AUTHENTICATION HANDLERS
    // ==========================================

    private Packet handleLogin(ClientSession session, Packet req) {
        LoginRequest loginReq = req.getPayloadAs(LoginRequest.class);
        if (loginReq == null || loginReq.getUsername() == null || loginReq.getPassword() == null) {
            return Packet.error(OpCodes.LOGIN_RES, OpCodes.STATUS_BAD_REQUEST, LoginResponse.fail("Missing credentials"));
        }

        UserProfileDTO profile = userDAO.login(loginReq.getUsername(), loginReq.getPassword());
        if (profile == null) {
            logDAO.log(loginReq.getUsername(), "LOGIN_FAILED", "Invalid password or username", session.getRemoteAddress());
            return Packet.error(OpCodes.LOGIN_RES, OpCodes.STATUS_UNAUTHORIZED, LoginResponse.fail("Invalid username or password"));
        }

        sessionManager.bindUser(session, profile.getUsername(), profile.getDisplayName(), profile.getRole());
        logDAO.log(profile.getUsername(), "LOGIN", "Successful login", session.getRemoteAddress());

        LoginResponse res = LoginResponse.ok(session.getToken(), profile.getUsername(), profile.getDisplayName(), profile.getRole());
        return Packet.ok(OpCodes.LOGIN_RES, res);
    }

    private Packet handleRegister(ClientSession session, Packet req) {
        RegisterRequest regReq = req.getPayloadAs(RegisterRequest.class);
        if (regReq == null || regReq.getUsername() == null || regReq.getPassword() == null) {
            return Packet.error(OpCodes.REGISTER_RES, OpCodes.STATUS_BAD_REQUEST, RegisterResponse.fail("Missing registration data"));
        }

        try {
            UserProfileDTO created = userDAO.register(
                    regReq.getUsername(), regReq.getPassword(), regReq.getDisplayName(), regReq.getEmail(), UserRole.STUDENT);
            logDAO.log(created.getUsername(), "REGISTER", "Account created successfully", session.getRemoteAddress());
            return Packet.ok(OpCodes.REGISTER_RES, RegisterResponse.ok(created.getUsername()));
        } catch (IllegalStateException e) {
            return Packet.error(OpCodes.REGISTER_RES, OpCodes.STATUS_CONFLICT, RegisterResponse.fail("Username already exists"));
        } catch (Exception e) {
            return Packet.error(OpCodes.REGISTER_RES, OpCodes.STATUS_BAD_REQUEST, RegisterResponse.fail("Registration error: " + e.getMessage()));
        }
    }

    private Packet handleLogout(ClientSession session, Packet req) {
        if (session.isAuthenticated()) {
            logDAO.log(session.getUsername(), "LOGOUT", "User logged out", session.getRemoteAddress());
            roomManager.handleDisconnect(session.getUsername());
            sessionManager.unbindUser(session.getUsername());
            session.clearAuthentication();
        }
        return Packet.ok(OpCodes.LOGOUT_RES, "Logged out successfully");
    }

    private Packet handleUpdateProfile(ClientSession session, Packet req) {
        if (!session.isAuthenticated()) {
            return Packet.error(OpCodes.UPDATE_PROFILE_RES, OpCodes.STATUS_UNAUTHORIZED, "Please login first");
        }

        UserProfileDTO profile = req.getPayloadAs(UserProfileDTO.class);
        if (profile == null) {
            return Packet.error(OpCodes.UPDATE_PROFILE_RES, OpCodes.STATUS_BAD_REQUEST, "Invalid profile payload");
        }

        boolean ok = userDAO.updateProfile(session.getUsername(), profile.getDisplayName(), profile.getEmail());
        if (ok) {
            session.setDisplayName(profile.getDisplayName());
            logDAO.log(session.getUsername(), "UPDATE_PROFILE", "Updated profile details", session.getRemoteAddress());
            return Packet.ok(OpCodes.UPDATE_PROFILE_RES, userDAO.findByUsername(session.getUsername()));
        } else {
            return Packet.error(OpCodes.UPDATE_PROFILE_RES, OpCodes.STATUS_SERVER_ERROR, "Failed to update profile");
        }
    }

    private Packet handleSessionCheck(ClientSession session, Packet req) {
        if (session.isAuthenticated()) {
            UserProfileDTO profile = userDAO.findByUsername(session.getUsername());
            return Packet.ok(OpCodes.SESSION_CHECK_RES, profile);
        } else {
            return Packet.error(OpCodes.SESSION_CHECK_RES, OpCodes.STATUS_UNAUTHORIZED, "Session expired or not logged in");
        }
    }

    // ==========================================
    // MULTIPLAYER ROOM & LOBBY COORDINATION
    // ==========================================

    private Packet handleCreateRoom(ClientSession session, Packet req) {
        if (!session.isAuthenticated()) {
            return Packet.error(OpCodes.CREATE_ROOM_RES, OpCodes.STATUS_UNAUTHORIZED, CreateRoomResponse.fail("Must login to create room"));
        }

        CreateRoomRequest createReq = req.getPayloadAs(CreateRoomRequest.class);
        if (createReq == null) {
            createReq = new CreateRoomRequest();
        }

        ExamRoom room = roomManager.createRoom(createReq, session.getUsername(), session.getDisplayName());
        session.setCurrentRoomCode(room.getRoomCode());

        logDAO.log(session.getUsername(), "CREATE_ROOM", "Created room " + room.getRoomCode(), session.getRemoteAddress());

        CreateRoomResponse res = CreateRoomResponse.ok(room.getRoomCode(), room.toRoomInfoDTO());
        return Packet.ok(OpCodes.CREATE_ROOM_RES, res);
    }

    private Packet handleJoinRoom(ClientSession session, Packet req) {
        if (!session.isAuthenticated()) {
            return Packet.error(OpCodes.JOIN_ROOM_RES, OpCodes.STATUS_UNAUTHORIZED, JoinRoomResponse.fail("Must login to join room"));
        }

        JoinRoomRequest joinReq = req.getPayloadAs(JoinRoomRequest.class);
        if (joinReq == null || joinReq.getRoomCode() == null) {
            return Packet.error(OpCodes.JOIN_ROOM_RES, OpCodes.STATUS_BAD_REQUEST, JoinRoomResponse.fail("Room code required"));
        }

        ExamRoom room = roomManager.getRoom(joinReq.getRoomCode());
        if (room == null) {
            return Packet.error(OpCodes.JOIN_ROOM_RES, OpCodes.STATUS_NOT_FOUND, JoinRoomResponse.fail("Room does not exist"));
        }

        if (!RoomStatus.isJoinable(room.getStatus())) {
            return Packet.error(OpCodes.JOIN_ROOM_RES, OpCodes.STATUS_ROOM_ALREADY_STARTED, JoinRoomResponse.fail("Test already started or finished"));
        }

        if (!room.checkPassword(joinReq.getRoomPassword())) {
            return Packet.error(OpCodes.JOIN_ROOM_RES, OpCodes.STATUS_FORBIDDEN, JoinRoomResponse.fail("Incorrect room password"));
        }

        boolean joined = room.addParticipant(session.getUsername(), session.getDisplayName());
        if (!joined) {
            return Packet.error(OpCodes.JOIN_ROOM_RES, OpCodes.STATUS_ROOM_FULL, JoinRoomResponse.fail("Room is full"));
        }

        roomManager.bindUserToRoom(session.getUsername(), room.getRoomCode());
        session.setCurrentRoomCode(room.getRoomCode());

        // Broadcast updated participant list to other room members
        room.broadcastToRoomExcept(sessionManager, session.getUsername(), Packet.ok(OpCodes.EVENT_ROOM_UPDATE, 
                room.toRoomUpdateEventDTO(session.getDisplayName() + " joined the room")));

        logDAO.log(session.getUsername(), "JOIN_ROOM", "Joined room " + room.getRoomCode(), session.getRemoteAddress());

        return Packet.ok(OpCodes.JOIN_ROOM_RES, JoinRoomResponse.ok(room.toRoomDetailsDTO()));
    }

    private Packet handleToggleReady(ClientSession session, Packet req) {
        String code = session.getCurrentRoomCode();
        ExamRoom room = roomManager.getRoom(code);
        if (room == null) {
            return Packet.error(OpCodes.TOGGLE_READY_RES, OpCodes.STATUS_NOT_FOUND, "Not currently in any room");
        }

        boolean ok = room.toggleReady(session.getUsername());
        if (ok) {
            room.broadcastToRoomExcept(sessionManager, session.getUsername(), Packet.ok(OpCodes.EVENT_ROOM_UPDATE, 
                    room.toRoomUpdateEventDTO("Ready status updated")));
            return Packet.ok(OpCodes.TOGGLE_READY_RES, room.toRoomDetailsDTO());
        }
        return Packet.error(OpCodes.TOGGLE_READY_RES, OpCodes.STATUS_BAD_REQUEST, "Cannot toggle ready status");
    }

    private Packet handleStartRoom(ClientSession session, Packet req) {
        String code = session.getCurrentRoomCode();
        ExamRoom room = roomManager.getRoom(code);
        if (room == null) {
            return Packet.error(OpCodes.START_ROOM_RES, OpCodes.STATUS_NOT_FOUND, "Not in a room");
        }

        if (!room.getHostUsername().equalsIgnoreCase(session.getUsername())) {
            return Packet.error(OpCodes.START_ROOM_RES, OpCodes.STATUS_NOT_HOST, "Only the host can start the exam");
        }

        boolean started = room.startExam(questionDAO, sessionManager, examRecordDAO);
        if (started) {
            logDAO.log(session.getUsername(), "START_ROOM", "Started exam in room " + code, session.getRemoteAddress());
            return Packet.ok(OpCodes.START_ROOM_RES, room.toRoomInfoDTO());
        } else {
            return Packet.error(OpCodes.START_ROOM_RES, OpCodes.STATUS_CONFLICT, "Room cannot be started");
        }
    }

    private Packet handleLeaveRoom(ClientSession session, Packet req) {
        String code = session.getCurrentRoomCode();
        if (code != null) {
            ExamRoom room = roomManager.getRoom(code);
            if (room != null) {
                room.removeParticipant(session.getUsername());
                roomManager.unbindUserFromRoom(session.getUsername());
                session.setCurrentRoomCode(null);

                room.broadcastToRoomExcept(sessionManager, session.getUsername(), Packet.ok(OpCodes.EVENT_ROOM_UPDATE, 
                        room.toRoomUpdateEventDTO(session.getDisplayName() + " left the room")));
            }
        }
        return Packet.ok(OpCodes.LEAVE_ROOM_RES, "Left room");
    }

    private Packet handleHostEndExam(ClientSession session, Packet req) {
        String code = session.getCurrentRoomCode();
        ExamRoom room = roomManager.getRoom(code);
        if (room == null) return Packet.error(OpCodes.HOST_END_EXAM_RES, OpCodes.STATUS_NOT_FOUND, "No room found");

        if (!room.getHostUsername().equalsIgnoreCase(session.getUsername())) {
            return Packet.error(OpCodes.HOST_END_EXAM_RES, OpCodes.STATUS_NOT_HOST, "Only host can end exam");
        }

        room.endExam(sessionManager, examRecordDAO);
        return Packet.ok(OpCodes.HOST_END_EXAM_RES, "Exam ended by host");
    }

    private Packet handleRoomChat(ClientSession session, Packet req) {
        ChatMessageDTO chat = req.getPayloadAs(ChatMessageDTO.class);
        if (chat == null || chat.getRoomCode() == null) {
            return Packet.error(OpCodes.ROOM_CHAT_REQ, OpCodes.STATUS_BAD_REQUEST, "Missing chat payload");
        }

        ExamRoom room = roomManager.getRoom(chat.getRoomCode());
        if (room != null) {
            chat.setSenderUsername(session.getUsername());
            chat.setSenderDisplayName(session.getDisplayName());
            room.addChatMessage(chat);
            room.broadcastToRoomExcept(sessionManager, session.getUsername(), Packet.ok(OpCodes.EVENT_ROOM_CHAT, chat));
            return Packet.ok(OpCodes.ROOM_CHAT_REQ, "Sent");
        }
        return Packet.error(OpCodes.ROOM_CHAT_REQ, OpCodes.STATUS_NOT_FOUND, "Room not found");
    }

    private Packet handleRoomList(ClientSession session, Packet req) {
        List<RoomInfoDTO> list = roomManager.getRoomList();
        return Packet.ok(OpCodes.ROOM_LIST_RES, new RoomListResponse(list));
    }

    private Packet handleCompletedRoomResult(ClientSession session, Packet req) {
        String roomCode = req.getPayloadAsString().replace("\"", "").trim();
        List<UserExamRecordDTO> results = examRecordDAO.getRoomResults(roomCode);
        return Packet.ok(OpCodes.COMPLETED_ROOM_RESULT_RES, results);
    }

    // ==========================================
    // MULTIPLAYER EXAM ENGINE (ANSWERS & SCORING)
    // ==========================================

    private Packet handleSubmitAnswer(ClientSession session, Packet req) {
        AnswerSubmissionDTO ans = req.getPayloadAs(AnswerSubmissionDTO.class);
        if (ans == null) return Packet.error(OpCodes.SUBMIT_ANSWER_RES, OpCodes.STATUS_BAD_REQUEST, "Missing answer data");

        String code = ans.getRoomCode() != null ? ans.getRoomCode() : session.getCurrentRoomCode();
        if (code != null) {
            ExamRoom room = roomManager.getRoom(code);
            if (room != null) {
                room.updateInFlightAnswer(session.getUsername(), ans.getQuestionId(), ans.getSelectedOption());
                return Packet.ok(OpCodes.SUBMIT_ANSWER_RES, "Answer saved");
            }
        }
        return Packet.error(OpCodes.SUBMIT_ANSWER_RES, OpCodes.STATUS_NOT_FOUND, "Room not active");
    }

    private Packet handleSubmitExam(ClientSession session, Packet req) {
        ExamSubmitDTO submitDTO = req.getPayloadAs(ExamSubmitDTO.class);
        if (submitDTO == null) return Packet.error(OpCodes.SUBMIT_EXAM_RES, OpCodes.STATUS_BAD_REQUEST, "Missing submit payload");

        String code = submitDTO.getRoomCode() != null ? submitDTO.getRoomCode() : session.getCurrentRoomCode();

        // If Solo Practice
        if (code == null || code.equalsIgnoreCase("PRACTICE")) {
            return handlePracticeSubmit(session, req);
        }

        // If Multiplayer Room
        ExamRoom room = roomManager.getRoom(code);
        if (room != null) {
            ExamResultDTO result = room.submitParticipantExam(
                    session.getUsername(), submitDTO.getAnswers(), submitDTO.isEarlySubmission(), sessionManager, examRecordDAO);

            logDAO.log(session.getUsername(), "SUBMIT_EXAM", 
                    String.format("Submitted room %s with score: %.1f", code, result != null ? result.getScore() : 0.0), 
                    session.getRemoteAddress());

            return Packet.ok(OpCodes.SUBMIT_EXAM_RES, result);
        }

        return Packet.error(OpCodes.SUBMIT_EXAM_RES, OpCodes.STATUS_NOT_FOUND, "Room not found");
    }

    // ==========================================
    // PRACTICE MODE (SOLO)
    // ==========================================

    private Packet handlePracticeRequest(ClientSession session, Packet req) {
        PracticeRequestDTO practiceReq = req.getPayloadAs(PracticeRequestDTO.class);
        if (practiceReq == null) {
            practiceReq = new PracticeRequestDTO();
        }

        List<FullQuestionDTO> fullQuestions = questionDAO.getRandomQuestions(
                practiceReq.getTopic(), practiceReq.getDifficulty(), practiceReq.getQuestionCount());

        List<QuestionDTO> sanitizedQuestions = new ArrayList<>();
        int qNum = 1;
        for (FullQuestionDTO fq : fullQuestions) {
            sanitizedQuestions.add(fq.toSanitizedDTO(qNum++));
        }

        int durationSec = practiceReq.getDurationMinutes() * 60;
        PracticeResponseDTO responseDTO = new PracticeResponseDTO(
                practiceReq.getTopic(), practiceReq.getDifficulty(), durationSec, sanitizedQuestions);

        logDAO.log(session.getUsername(), "START_PRACTICE", 
                "Topic: " + practiceReq.getTopic() + ", Questions: " + sanitizedQuestions.size(), session.getRemoteAddress());

        return Packet.ok(OpCodes.PRACTICE_RES, responseDTO);
    }

    private Packet handlePracticeSubmit(ClientSession session, Packet req) {
        ExamSubmitDTO submitDTO = req.getPayloadAs(ExamSubmitDTO.class);
        if (submitDTO == null) {
            return Packet.error(OpCodes.PRACTICE_RESULT_RES, OpCodes.STATUS_BAD_REQUEST, "Missing answers submission payload");
        }

        String username = session.isAuthenticated() ? session.getUsername() : submitDTO.getStudentUsername();
        if (username == null || username.trim().isEmpty()) {
            username = "student01";
        }

        Map<Integer, String> studentAnswers = submitDTO.getAnswers() != null ? submitDTO.getAnswers() : Collections.emptyMap();
        List<ExamResultDTO.QuestionReview> reviews = new ArrayList<>();
        int correctCount = 0;
        int totalQuestions = studentAnswers.size();
        int qNum = 1;

        String topic = "General IT";
        String difficulty = "MEDIUM";

        for (Map.Entry<Integer, String> entry : studentAnswers.entrySet()) {
            int qId = entry.getKey();
            String studentChoice = entry.getValue();

            FullQuestionDTO q = questionDAO.getQuestionById(qId);
            if (q != null) {
                topic = q.getCategory();
                difficulty = q.getDifficulty();
                boolean isCorr = q.getCorrectOption() != null && q.getCorrectOption().equalsIgnoreCase(studentChoice);
                if (isCorr) correctCount++;

                reviews.add(new ExamResultDTO.QuestionReview(
                        qId, qNum++, q.getContent(), studentChoice, q.getCorrectOption(), isCorr, q.getExplanation()));
            }
        }

        if (totalQuestions == 0) totalQuestions = 1;
        double score = Math.round(((correctCount * 10.0) / totalQuestions) * 10.0) / 10.0;

        // Save exam record
        UserExamRecordDTO record = new UserExamRecordDTO(
                0, username, "PRACTICE", "Solo Practice (" + topic + ")", null,
                topic, difficulty, score, correctCount, totalQuestions, 0, System.currentTimeMillis());
        examRecordDAO.saveRecord(record);

        logDAO.log(username, "SUBMIT_PRACTICE", 
                String.format("Practice finished. Score: %.1f (%d/%d)", score, correctCount, totalQuestions), 
                session.getRemoteAddress());

        ExamResultDTO resultDTO = new ExamResultDTO(
                username, score, correctCount, totalQuestions, 1, 1, 0, true, reviews);

        return Packet.ok(OpCodes.PRACTICE_RESULT_RES, resultDTO);
    }

    // ==========================================
    // EXAM HISTORY, STATS & LEADERBOARD
    // ==========================================

    private Packet handleUserHistory(ClientSession session, Packet req) {
        String targetUser = session.isAuthenticated() ? session.getUsername() : req.getPayloadAsString();
        if (targetUser == null || targetUser.trim().isEmpty()) {
            return Packet.error(OpCodes.USER_HISTORY_RES, OpCodes.STATUS_BAD_REQUEST, "User must be authenticated");
        }

        List<UserExamRecordDTO> history = examRecordDAO.getHistoryByUsername(targetUser.trim(), 50);
        return Packet.ok(OpCodes.USER_HISTORY_RES, history);
    }

    private Packet handleUserStats(ClientSession session, Packet req) {
        String targetUser = session.isAuthenticated() ? session.getUsername() : req.getPayloadAsString();
        if (targetUser == null || targetUser.trim().isEmpty()) {
            targetUser = "student01";
        }

        UserStatsDTO stats = examRecordDAO.getUserStats(targetUser.trim());
        return Packet.ok(OpCodes.USER_STATS_RES, stats);
    }

    private Packet handleLeaderboard(ClientSession session, Packet req) {
        List<LeaderboardEntryDTO> leaderboard = examRecordDAO.getLeaderboard(20);
        return Packet.ok(OpCodes.LEADERBOARD_RES, leaderboard);
    }

    // ==========================================
    // ACTIVITY LOGS & QUESTION BANK (ADMIN / TEACHER)
    // ==========================================

    private Packet handleGetLogs(ClientSession session, Packet req) {
        List<ActivityLogDTO> logs = logDAO.getRecentLogs(100);
        return Packet.ok(OpCodes.GET_LOGS_RES, logs);
    }

    private Packet handleQuestionList(ClientSession session, Packet req) {
        List<FullQuestionDTO> questions = questionDAO.getAllQuestions();
        return Packet.ok(OpCodes.QUESTION_LIST_RES, questions);
    }

    private Packet handleQuestionAdd(ClientSession session, Packet req) {
        FullQuestionDTO q = req.getPayloadAs(FullQuestionDTO.class);
        if (q == null) {
            return Packet.error(OpCodes.QUESTION_ADD_RES, OpCodes.STATUS_BAD_REQUEST, "Invalid question payload");
        }
        int id = questionDAO.addQuestion(q);
        logDAO.log(session.getUsername(), "ADD_QUESTION", "Added question id " + id, session.getRemoteAddress());
        return Packet.ok(OpCodes.QUESTION_ADD_RES, Map.of("id", id, "message", "Question added successfully"));
    }

    private Packet handleQuestionUpdate(ClientSession session, Packet req) {
        FullQuestionDTO q = req.getPayloadAs(FullQuestionDTO.class);
        if (q == null) {
            return Packet.error(OpCodes.QUESTION_UPDATE_RES, OpCodes.STATUS_BAD_REQUEST, "Invalid question payload");
        }
        boolean ok = questionDAO.updateQuestion(q);
        logDAO.log(session.getUsername(), "UPDATE_QUESTION", "Updated question id " + q.getId(), session.getRemoteAddress());
        return ok ? Packet.ok(OpCodes.QUESTION_UPDATE_RES, "Question updated successfully")
                  : Packet.error(OpCodes.QUESTION_UPDATE_RES, OpCodes.STATUS_NOT_FOUND, "Question not found");
    }

    private Packet handleQuestionDelete(ClientSession session, Packet req) {
        int id;
        try {
            id = Integer.parseInt(req.getPayloadAsString().replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return Packet.error(OpCodes.QUESTION_DELETE_RES, OpCodes.STATUS_BAD_REQUEST, "Invalid question id");
        }

        boolean ok = questionDAO.deleteQuestion(id);
        logDAO.log(session.getUsername(), "DELETE_QUESTION", "Deleted question id " + id, session.getRemoteAddress());
        return ok ? Packet.ok(OpCodes.QUESTION_DELETE_RES, "Question deleted successfully")
                  : Packet.error(OpCodes.QUESTION_DELETE_RES, OpCodes.STATUS_NOT_FOUND, "Question not found");
    }

    private Packet handlePing(ClientSession session, Packet req) {
        return Packet.ok(OpCodes.PONG, "PONG");
    }
}

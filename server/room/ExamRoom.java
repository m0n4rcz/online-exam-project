package server.room;

import server.core.SessionManager;
import server.db.dao.ExamRecordDAO;
import server.db.dao.QuestionDAO;
import shared.OpCodes;
import shared.Packet;
import shared.RoomStatus;
import shared.dtos.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Encapsulates the complete real-time state, participant coordination,
 * countdown timer, in-flight answers, and scoring engine for an active multiplayer test room.
 */
public class ExamRoom {

    private static final Logger LOGGER = Logger.getLogger(ExamRoom.class.getName());

    private final String roomCode;
    private final String roomName;
    private final String hostUsername;
    private final String hostDisplayName;
    private final String topic;
    private final String difficulty;
    private final int questionCount;
    private final int durationMinutes;
    private final int maxParticipants;
    private final String roomPassword;
    private final long createdAt;

    private volatile String status = RoomStatus.NOT_STARTED;
    private volatile long startTimestamp = 0;
    private volatile long endTimestamp = 0;

    private final Map<String, RoomParticipantDTO> participants = new ConcurrentHashMap<>();
    private final Map<String, Map<Integer, String>> inFlightAnswers = new ConcurrentHashMap<>();
    private final Map<String, Long> submissionTimes = new ConcurrentHashMap<>();
    private final List<ChatMessageDTO> chatMessages = new CopyOnWriteArrayList<>();

    private List<FullQuestionDTO> fullQuestions = new ArrayList<>();
    private List<QuestionDTO> sanitizedQuestions = new ArrayList<>();

    private ScheduledExecutorService timerScheduler;
    private ScheduledFuture<?> tickTask;
    private final AtomicInteger secondsRemaining = new AtomicInteger(0);

    public ExamRoom(String roomCode, String roomName, String hostUsername, String hostDisplayName,
                    String topic, String difficulty, int questionCount, int durationMinutes,
                    int maxParticipants, String roomPassword) {
        this.roomCode = roomCode;
        this.roomName = roomName;
        this.hostUsername = hostUsername;
        this.hostDisplayName = hostDisplayName;
        this.topic = topic;
        this.difficulty = difficulty;
        this.questionCount = questionCount;
        this.durationMinutes = durationMinutes;
        this.maxParticipants = maxParticipants;
        this.roomPassword = roomPassword;
        this.createdAt = System.currentTimeMillis();

        // Automatically register host
        RoomParticipantDTO hostParticipant = new RoomParticipantDTO(hostUsername, hostDisplayName, true, true);
        participants.put(hostUsername.toLowerCase(), hostParticipant);
    }

    // ==========================================
    // LOBBY & PARTICIPANT COORDINATION
    // ==========================================

    public synchronized boolean addParticipant(String username, String displayName) {
        if (!RoomStatus.isJoinable(status)) return false;
        if (maxParticipants > 0 && participants.size() >= maxParticipants) return false;

        String key = username.toLowerCase();
        if (!participants.containsKey(key)) {
            RoomParticipantDTO p = new RoomParticipantDTO(username, displayName, false, false);
            participants.put(key, p);
            return true;
        }
        return true; // Already joined
    }

    public synchronized boolean removeParticipant(String username) {
        String key = username.toLowerCase();
        RoomParticipantDTO removed = participants.remove(key);
        inFlightAnswers.remove(key);
        return removed != null;
    }

    public synchronized boolean toggleReady(String username) {
        String key = username.toLowerCase();
        RoomParticipantDTO p = participants.get(key);
        if (p != null && !p.isHost()) {
            p.setReady(!p.isReady());
            return true;
        }
        return false;
    }

    public void addChatMessage(ChatMessageDTO msg) {
        chatMessages.add(msg);
    }

    public boolean checkPassword(String password) {
        if (roomPassword == null || roomPassword.trim().isEmpty()) return true;
        return roomPassword.equals(password);
    }

    // ==========================================
    // EXAM LIFECYCLE (START / TICK / SUBMIT / END)
    // ==========================================

    public synchronized boolean startExam(QuestionDAO questionDAO, SessionManager sessionManager, ExamRecordDAO examRecordDAO) {
        if (!RoomStatus.NOT_STARTED.equals(status)) {
            return false;
        }

        // 1. Fetch questions from QuestionDAO
        this.fullQuestions = questionDAO.getRandomQuestions(topic, difficulty, questionCount);
        this.sanitizedQuestions = new ArrayList<>();
        int qNum = 1;
        for (FullQuestionDTO fq : fullQuestions) {
            sanitizedQuestions.add(fq.toSanitizedDTO(qNum++));
        }

        // 2. Set timing
        int totalSeconds = durationMinutes * 60;
        this.secondsRemaining.set(totalSeconds);
        this.startTimestamp = System.currentTimeMillis();
        this.endTimestamp = this.startTimestamp + (totalSeconds * 1000L);
        this.status = RoomStatus.ONGOING;

        // 3. Initialize in-flight answers container for each participant
        for (String u : participants.keySet()) {
            inFlightAnswers.put(u, new ConcurrentHashMap<>());
        }

        // 4. Broadcast EVENT_ROOM_START and EXAM_INIT_DATA to all participants
        broadcastToRoom(sessionManager, Packet.ok(OpCodes.EVENT_ROOM_START, 
                Map.of("roomCode", roomCode, "message", "Exam is starting now!")));

        ExamInitDataDTO initData = new ExamInitDataDTO(
                roomCode, roomName, totalSeconds, startTimestamp, endTimestamp, sanitizedQuestions);
        broadcastToRoom(sessionManager, Packet.ok(OpCodes.EXAM_INIT_DATA, initData));

        // 5. Start 1-second countdown ticker
        this.timerScheduler = Executors.newSingleThreadScheduledExecutor();
        this.tickTask = timerScheduler.scheduleAtFixedRate(() -> {
            try {
                int sec = secondsRemaining.decrementAndGet();
                if (sec >= 0) {
                    Map<String, Object> tickPayload = Map.of("roomCode", roomCode, "secondsRemaining", sec);
                    broadcastToRoom(sessionManager, Packet.ok(OpCodes.EVENT_EXAM_TICK, tickPayload));
                }

                if (sec <= 0) {
                    endExam(sessionManager, examRecordDAO);
                }
            } catch (Throwable t) {
                LOGGER.log(Level.SEVERE, "Error in room countdown tick: " + t.getMessage(), t);
            }
        }, 1, 1, TimeUnit.SECONDS);

        LOGGER.info(String.format("Exam started for room %s with %d questions, %d seconds", roomCode, sanitizedQuestions.size(), totalSeconds));
        return true;
    }

    public void updateInFlightAnswer(String username, int questionId, String selectedOption) {
        if (!RoomStatus.ONGOING.equals(status)) return;
        String key = username.toLowerCase();
        Map<Integer, String> userAns = inFlightAnswers.computeIfAbsent(key, k -> new ConcurrentHashMap<>());
        if (selectedOption != null && !selectedOption.trim().isEmpty()) {
            userAns.put(questionId, selectedOption.trim().toUpperCase());
        } else {
            userAns.remove(questionId);
        }
    }

    public synchronized ExamResultDTO submitParticipantExam(String username, Map<Integer, String> finalAnswers,
                                                            boolean isEarlySubmission, SessionManager sessionManager,
                                                            ExamRecordDAO examRecordDAO) {
        String key = username.toLowerCase();
        RoomParticipantDTO p = participants.get(key);
        if (p == null) return null;

        // Store final answers
        if (finalAnswers != null) {
            inFlightAnswers.put(key, new ConcurrentHashMap<>(finalAnswers));
        }
        p.setHasSubmitted(true);
        submissionTimes.put(key, System.currentTimeMillis());

        // Notify other participants of early submission
        Map<String, Object> submittedNotice = Map.of(
                "roomCode", roomCode,
                "username", p.getUsername(),
                "displayName", p.getDisplayName(),
                "isEarlySubmission", isEarlySubmission
        );
        broadcastToRoomExcept(sessionManager, username, Packet.ok(OpCodes.EVENT_PARTICIPANT_SUBMITTED, submittedNotice));

        // Check if ALL participants have submitted
        boolean allSubmitted = true;
        for (RoomParticipantDTO part : participants.values()) {
            if (!part.isHasSubmitted()) {
                allSubmitted = false;
                break;
            }
        }

        if (allSubmitted && RoomStatus.ONGOING.equals(status)) {
            LOGGER.info("All participants submitted early. Ending room " + roomCode);
            endExam(sessionManager, examRecordDAO);
        }

        // Return current evaluation
        return evaluateUser(p, examRecordDAO);
    }

    public synchronized void endExam(SessionManager sessionManager, ExamRecordDAO examRecordDAO) {
        if (RoomStatus.FINISHED.equals(status)) return;
        this.status = RoomStatus.FINISHED;

        // Cancel countdown timer
        if (tickTask != null) {
            tickTask.cancel(false);
        }
        if (timerScheduler != null && !timerScheduler.isShutdown()) {
            timerScheduler.shutdown();
        }

        LOGGER.info("Grading and finalizing room " + roomCode + " results...");

        // 1. Evaluate all participants
        List<ParticipantResultDTO> leaderboard = new ArrayList<>();
        Map<String, ExamResultDTO> individualResults = new HashMap<>();

        for (RoomParticipantDTO p : participants.values()) {
            p.setHasSubmitted(true);
            ExamResultDTO eval = evaluateUser(p, examRecordDAO);
            individualResults.put(p.getUsername().toLowerCase(), eval);
        }

        // 2. Sort leaderboard: Score DESC, Time Spent ASC
        List<RoomParticipantDTO> sortedList = new ArrayList<>(participants.values());
        sortedList.sort((a, b) -> {
            int scoreCmp = Double.compare(b.getScore(), a.getScore());
            if (scoreCmp != 0) return scoreCmp;
            long tA = submissionTimes.getOrDefault(a.getUsername().toLowerCase(), endTimestamp);
            long tB = submissionTimes.getOrDefault(b.getUsername().toLowerCase(), endTimestamp);
            return Long.compare(tA, tB);
        });

        int rank = 1;
        for (RoomParticipantDTO p : sortedList) {
            long subTime = submissionTimes.getOrDefault(p.getUsername().toLowerCase(), endTimestamp);
            int timeSpent = (int) Math.max(1, (subTime - startTimestamp) / 1000L);
            leaderboard.add(new ParticipantResultDTO(
                    rank, p.getUsername(), p.getDisplayName(), p.getScore(),
                    p.getCorrectCount(), sanitizedQuestions.size(), true, timeSpent));

            ExamResultDTO userRes = individualResults.get(p.getUsername().toLowerCase());
            if (userRes != null) {
                userRes.setRank(rank);
                userRes.setTotalParticipants(sortedList.size());
            }
            rank++;
        }

        CompletedRoomResultDTO roomResult = new CompletedRoomResultDTO(
                roomCode, roomName, hostUsername, topic, difficulty,
                sanitizedQuestions.size(), durationMinutes, System.currentTimeMillis(), leaderboard);

        // 3. Send individual detailed results & broadcast room results
        for (RoomParticipantDTO p : sortedList) {
            ExamResultDTO userRes = individualResults.get(p.getUsername().toLowerCase());
            if (userRes != null) {
                sessionManager.broadcastToUsers(List.of(p.getUsername()), Packet.ok(OpCodes.EXAM_RESULT_RES, userRes));
            }
        }

        broadcastToRoom(sessionManager, Packet.ok(OpCodes.COMPLETED_ROOM_RESULT_RES, roomResult));
        broadcastToRoom(sessionManager, Packet.ok(OpCodes.EVENT_EXAM_ENDED, Map.of("roomCode", roomCode, "message", "Exam finished")));
    }

    private ExamResultDTO evaluateUser(RoomParticipantDTO p, ExamRecordDAO examRecordDAO) {
        String key = p.getUsername().toLowerCase();
        Map<Integer, String> userAns = inFlightAnswers.getOrDefault(key, Collections.emptyMap());

        int correctCount = 0;
        int totalQuestions = sanitizedQuestions.size() > 0 ? sanitizedQuestions.size() : 1;
        List<ExamResultDTO.QuestionReview> reviews = new ArrayList<>();
        int qNum = 1;

        for (FullQuestionDTO fq : fullQuestions) {
            String studentChoice = userAns.get(fq.getId());
            boolean isCorr = fq.getCorrectOption() != null && fq.getCorrectOption().equalsIgnoreCase(studentChoice);
            if (isCorr) correctCount++;

            reviews.add(new ExamResultDTO.QuestionReview(
                    fq.getId(), qNum++, fq.getContent(), studentChoice, fq.getCorrectOption(), isCorr, fq.getExplanation()));
        }

        double score = Math.round(((correctCount * 10.0) / totalQuestions) * 10.0) / 10.0;
        p.setScore(score);
        p.setCorrectCount(correctCount);

        long subTime = submissionTimes.getOrDefault(key, System.currentTimeMillis());
        int timeSpent = (int) Math.max(1, (subTime - (startTimestamp > 0 ? startTimestamp : subTime)) / 1000L);

        // Persist to exam_records table in SQLite
        UserExamRecordDTO dbRecord = new UserExamRecordDTO(
                0, p.getUsername(), "ROOM", roomName, roomCode,
                topic, difficulty, score, correctCount, totalQuestions, timeSpent, subTime);
        examRecordDAO.saveRecord(dbRecord);

        return new ExamResultDTO(
                p.getUsername(), score, correctCount, totalQuestions, 1, participants.size(), timeSpent, false, reviews);
    }

    // ==========================================
    // BROADCAST & DTO HELPERS
    // ==========================================

    public void broadcastToRoom(SessionManager sessionManager, Packet packet) {
        sessionManager.broadcastToUsers(participants.keySet(), packet);
    }

    public void broadcastToRoomExcept(SessionManager sessionManager, String excludeUsername, Packet packet) {
        Set<String> targets = new HashSet<>(participants.keySet());
        if (excludeUsername != null) {
            targets.remove(excludeUsername.toLowerCase());
        }
        sessionManager.broadcastToUsers(targets, packet);
    }

    public RoomInfoDTO toRoomInfoDTO() {
        return new RoomInfoDTO(
                roomCode, roomName, hostUsername, hostDisplayName, status,
                participants.size(), maxParticipants, questionCount, durationMinutes,
                topic, difficulty, roomPassword != null && !roomPassword.trim().isEmpty(), createdAt);
    }

    public RoomDetailsDTO toRoomDetailsDTO() {
        return new RoomDetailsDTO(toRoomInfoDTO(), new ArrayList<>(participants.values()));
    }

    public RoomUpdateEventDTO toRoomUpdateEventDTO(String message) {
        return new RoomUpdateEventDTO(roomCode, status, message, new ArrayList<>(participants.values()));
    }

    public String getRoomCode() { return roomCode; }
    public String getRoomName() { return roomName; }
    public String getHostUsername() { return hostUsername; }
    public String getStatus() { return status; }
    public Map<String, RoomParticipantDTO> getParticipants() { return participants; }
    public int getQuestionCount() { return questionCount; }
    public int getDurationMinutes() { return durationMinutes; }
    public int getSecondsRemaining() { return secondsRemaining.get(); }
}

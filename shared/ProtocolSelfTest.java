package shared;

import shared.dtos.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Comprehensive test harness verifying the protocol framing, stream handling,
 * anti-fragmentation defense, and DTO JSON serialization across all rubric categories.
 */
public class ProtocolSelfTest {

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("  RUNNING ONLINE EXAM SYSTEM PROTOCOL & DTO TEST SUITE");
        System.out.println("===============================================================\n");

        int testsPassed = 0;
        int totalTests = 0;

        // -------------------------------------------------------------
        // TEST 1: Basic Packet Encoding & Decoding
        // -------------------------------------------------------------
        totalTests++;
        System.out.print("[TEST 1] Binary Frame Encoding & Decoding... ");
        try {
            LoginRequest loginReq = new LoginRequest("student01", "P@ssword123");
            Packet packetOut = Packet.ok(OpCodes.LOGIN_REQ, loginReq);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PacketCodec.writePacket(baos, packetOut);
            byte[] rawBytes = baos.toByteArray();

            // Assert 12-byte header + payload
            assert rawBytes.length == Packet.HEADER_SIZE + packetOut.getLength();

            ByteArrayInputStream bais = new ByteArrayInputStream(rawBytes);
            Packet packetIn = PacketCodec.readPacket(bais);

            assert packetIn != null;
            assert packetIn.getMagic() == Packet.MAGIC_NUMBER;
            assert packetIn.getOpCode() == OpCodes.LOGIN_REQ;
            assert packetIn.getStatus() == OpCodes.STATUS_OK;

            LoginRequest parsed = packetIn.getPayloadAs(LoginRequest.class);
            assert "student01".equals(parsed.getUsername());
            assert "P@ssword123".equals(parsed.getPassword());

            System.out.println("PASSED");
            testsPassed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            t.printStackTrace();
        }

        // -------------------------------------------------------------
        // TEST 2: Sticky Packets Defense (Multiple frames concatenated in buffer)
        // -------------------------------------------------------------
        totalTests++;
        System.out.print("[TEST 2] Sticky Packet Handling (Multiple frames in one stream)... ");
        try {
            Packet p1 = Packet.ok(OpCodes.PING, "ping1");
            Packet p2 = Packet.ok(OpCodes.PONG, "pong2");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PacketCodec.writePacket(baos, p1);
            PacketCodec.writePacket(baos, p2);

            byte[] multiFrame = baos.toByteArray();
            ByteBuffer buf = ByteBuffer.wrap(multiFrame);

            Packet d1 = PacketCodec.decodeFromBuffer(buf);
            Packet d2 = PacketCodec.decodeFromBuffer(buf);

            assert d1 != null && d1.getOpCode() == OpCodes.PING;
            assert "ping1".equals(d1.getPayloadAsString());
            assert d2 != null && d2.getOpCode() == OpCodes.PONG;
            assert "pong2".equals(d2.getPayloadAsString());

            System.out.println("PASSED");
            testsPassed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            t.printStackTrace();
        }

        // -------------------------------------------------------------
        // TEST 3: Packet Fragmentation Defense (Partial Header/Payload)
        // -------------------------------------------------------------
        totalTests++;
        System.out.print("[TEST 3] Packet Fragmentation Defense (Partial reads)... ");
        try {
            Packet p = Packet.ok(OpCodes.ROOM_LIST_REQ, "{\"filter\":\"ACTIVE\"}");
            byte[] fullBytes = PacketCodec.encode(p);

            // Buffer with only 8 bytes (incomplete header)
            ByteBuffer partialBuf = ByteBuffer.allocate(fullBytes.length);
            partialBuf.put(fullBytes, 0, 8);
            partialBuf.flip();

            Packet partialResult = PacketCodec.decodeFromBuffer(partialBuf);
            assert partialResult == null : "Decoder should return null when header is incomplete";

            // Supply remaining bytes
            partialBuf.compact();
            partialBuf.put(fullBytes, 8, fullBytes.length - 8);
            partialBuf.flip();

            Packet completeResult = PacketCodec.decodeFromBuffer(partialBuf);
            assert completeResult != null : "Decoder should succeed once full packet is available";
            assert completeResult.getOpCode() == OpCodes.ROOM_LIST_REQ;

            System.out.println("PASSED");
            testsPassed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            t.printStackTrace();
        }

        // -------------------------------------------------------------
        // TEST 4: Room Creation & Room Status List DTO
        // -------------------------------------------------------------
        totalTests++;
        System.out.print("[TEST 4] Room Creation & Room List DTO Serialization... ");
        try {
            CreateRoomRequest createReq = new CreateRoomRequest(
                    "Network Exam 2026", 20, 30, QuestionCategory.NETWORKING, QuestionDifficulty.HARD, 25, null);
            String json = JsonUtil.toJson(createReq);
            CreateRoomRequest roundtrip = JsonUtil.fromJson(json, CreateRoomRequest.class);

            assert "Network Exam 2026".equals(roundtrip.getRoomName());
            assert roundtrip.getQuestionCount() == 20;
            assert roundtrip.getDurationMinutes() == 30;
            assert QuestionCategory.NETWORKING.equals(roundtrip.getTopic());
            assert QuestionDifficulty.HARD.equals(roundtrip.getDifficulty());

            RoomInfoDTO room1 = new RoomInfoDTO("NET101", "Networking Midterm", "prof_smith", "Prof. Smith",
                    RoomStatus.NOT_STARTED, 5, 20, 15, 20, "Networking", "MEDIUM", false, System.currentTimeMillis());
            RoomInfoDTO room2 = new RoomInfoDTO("JAV202", "Java Final", "prof_john", "Prof. John",
                    RoomStatus.ONGOING, 18, 20, 25, 45, "Java Core", "HARD", true, System.currentTimeMillis());

            RoomListResponse listRes = new RoomListResponse(List.of(room1, room2));
            String listJson = JsonUtil.toJson(listRes);
            RoomListResponse parsedList = JsonUtil.fromJson(listJson, RoomListResponse.class);

            assert parsedList.getRooms().size() == 2;
            assert RoomStatus.NOT_STARTED.equals(parsedList.getRooms().get(0).getStatus());
            assert RoomStatus.ONGOING.equals(parsedList.getRooms().get(1).getStatus());

            System.out.println("PASSED");
            testsPassed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            t.printStackTrace();
        }

        // -------------------------------------------------------------
        // TEST 5: Question Customization by Difficulty & Topic
        // -------------------------------------------------------------
        totalTests++;
        System.out.print("[TEST 5] Question DTO with Topic & Difficulty Classification... ");
        try {
            QuestionDTO q = new QuestionDTO(101, 1,
                    "Which protocol provides reliable, connection-oriented byte stream transmission?",
                    "UDP", "TCP", "ICMP", "IP",
                    QuestionCategory.NETWORKING, QuestionDifficulty.EASY);

            String qJson = JsonUtil.toJson(q);
            QuestionDTO qParsed = JsonUtil.fromJson(qJson, QuestionDTO.class);

            assert qParsed.getId() == 101;
            assert QuestionCategory.NETWORKING.equals(qParsed.getCategory());
            assert QuestionDifficulty.EASY.equals(qParsed.getDifficulty());
            assert "TCP".equals(qParsed.getOptionB());

            System.out.println("PASSED");
            testsPassed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            t.printStackTrace();
        }

        // -------------------------------------------------------------
        // TEST 6: Practice Mode Request & Response
        // -------------------------------------------------------------
        totalTests++;
        System.out.print("[TEST 6] Practice Mode DTOs... ");
        try {
            PracticeRequestDTO practiceReq = new PracticeRequestDTO(
                    QuestionCategory.JAVA_CORE, QuestionDifficulty.MEDIUM, 15, 20);

            String pReqJson = JsonUtil.toJson(practiceReq);
            PracticeRequestDTO pReqRoundtrip = JsonUtil.fromJson(pReqJson, PracticeRequestDTO.class);

            assert QuestionCategory.JAVA_CORE.equals(pReqRoundtrip.getTopic());
            assert QuestionDifficulty.MEDIUM.equals(pReqRoundtrip.getDifficulty());
            assert pReqRoundtrip.getQuestionCount() == 15;

            System.out.println("PASSED");
            testsPassed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            t.printStackTrace();
        }

        // -------------------------------------------------------------
        // TEST 7: Changing Answers In-Flight & Early Exam Submit
        // -------------------------------------------------------------
        totalTests++;
        System.out.print("[TEST 7] In-Flight Answer Change & Early Submission... ");
        try {
            AnswerSubmissionDTO answerChange = new AnswerSubmissionDTO("ROOM99", 101, "B");
            String ansJson = JsonUtil.toJson(answerChange);
            AnswerSubmissionDTO ansParsed = JsonUtil.fromJson(ansJson, AnswerSubmissionDTO.class);
            assert "B".equals(ansParsed.getSelectedOption());
            assert ansParsed.getQuestionId() == 101;

            Map<Integer, String> studentAnswers = new HashMap<>();
            studentAnswers.put(101, "B");
            studentAnswers.put(102, "C");
            studentAnswers.put(103, "A");

            ExamSubmitDTO submitDTO = new ExamSubmitDTO("ROOM99", "student01", studentAnswers, true);
            String submitJson = JsonUtil.toJson(submitDTO);
            ExamSubmitDTO submitParsed = JsonUtil.fromJson(submitJson, ExamSubmitDTO.class);

            assert submitParsed.isEarlySubmission();
            assert "B".equals(submitParsed.getAnswers().get(101));
            assert submitParsed.getAnswers().size() == 3;

            System.out.println("PASSED");
            testsPassed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            t.printStackTrace();
        }

        // -------------------------------------------------------------
        // TEST 8: Exam Results & Number of Correct Answers
        // -------------------------------------------------------------
        totalTests++;
        System.out.print("[TEST 8] Graded Result & Correct Count Notification... ");
        try {
            ExamResultDTO.QuestionReview r1 = new ExamResultDTO.QuestionReview(
                    101, 1, "What is TCP?", "B", "B", true, "TCP is Transmission Control Protocol.");
            ExamResultDTO.QuestionReview r2 = new ExamResultDTO.QuestionReview(
                    102, 2, "What is UDP?", "A", "C", false, "UDP is User Datagram Protocol.");

            ExamResultDTO result = new ExamResultDTO("student01", 8.0, 16, 20, 2, 10, 750, false, List.of(r1, r2));
            String resultJson = JsonUtil.toJson(result);
            ExamResultDTO resultParsed = JsonUtil.fromJson(resultJson, ExamResultDTO.class);

            assert resultParsed.getCorrectCount() == 16;
            assert resultParsed.getTotalQuestions() == 20;
            assert resultParsed.getScore() == 8.0;
            assert resultParsed.getRank() == 2;
            assert resultParsed.getReviews().size() == 2;
            assert resultParsed.getReviews().get(0).isCorrect();
            assert !resultParsed.getReviews().get(1).isCorrect();

            System.out.println("PASSED");
            testsPassed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            t.printStackTrace();
        }

        // -------------------------------------------------------------
        // TEST 9: Completed Room Results & Leaderboard
        // -------------------------------------------------------------
        totalTests++;
        System.out.print("[TEST 9] Completed Room Result & Leaderboard... ");
        try {
            ParticipantResultDTO p1 = new ParticipantResultDTO(1, "alice", "Alice Wonder", 9.5, 19, 20, true, 540);
            ParticipantResultDTO p2 = new ParticipantResultDTO(2, "bob", "Bob Builder", 8.0, 16, 20, false, 900);

            CompletedRoomResultDTO roomResult = new CompletedRoomResultDTO(
                    "ROOM99", "Midterm Exam", "prof_smith", "Networking", "MEDIUM", 20, 15,
                    System.currentTimeMillis(), List.of(p1, p2));

            String crJson = JsonUtil.toJson(roomResult);
            CompletedRoomResultDTO crParsed = JsonUtil.fromJson(crJson, CompletedRoomResultDTO.class);

            assert "ROOM99".equals(crParsed.getRoomCode());
            assert crParsed.getLeaderboard().size() == 2;
            assert crParsed.getLeaderboard().get(0).getRank() == 1;
            assert "alice".equals(crParsed.getLeaderboard().get(0).getUsername());

            System.out.println("PASSED");
            testsPassed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            t.printStackTrace();
        }

        // -------------------------------------------------------------
        // TEST 10: Stored Test History & Graphical Statistics DTO
        // -------------------------------------------------------------
        totalTests++;
        System.out.print("[TEST 10] Stored Exam History & Graphical Statistics DTO... ");
        try {
            UserStatsDTO stats = new UserStatsDTO("student01");
            stats.setTotalTests(12);
            stats.setAverageScore(8.2);
            stats.setHighestScore(10.0);
            stats.setOverallAccuracyPercentage(82.0);

            Map<String, Double> topicAcc = new HashMap<>();
            topicAcc.put("Java Core", 90.0);
            topicAcc.put("Networking", 75.0);
            stats.setTopicAccuracy(topicAcc);

            List<ScoreTrendPointDTO> trend = new ArrayList<>();
            trend.add(new ScoreTrendPointDTO(1, "Test 1", 7.0, 70.0));
            trend.add(new ScoreTrendPointDTO(2, "Test 2", 8.5, 85.0));
            trend.add(new ScoreTrendPointDTO(3, "Test 3", 9.0, 90.0));
            stats.setScoreTrend(trend);

            String statsJson = JsonUtil.toJson(stats);
            UserStatsDTO statsParsed = JsonUtil.fromJson(statsJson, UserStatsDTO.class);

            assert statsParsed.getTotalTests() == 12;
            assert statsParsed.getScoreTrend().size() == 3;
            assert statsParsed.getScoreTrend().get(2).getScore() == 9.0;
            assert statsParsed.getTopicAccuracy().get("Java Core") == 90.0;

            System.out.println("PASSED");
            testsPassed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            t.printStackTrace();
        }

        // -------------------------------------------------------------
        // TEST 11: Activity Logging DTO
        // -------------------------------------------------------------
        totalTests++;
        System.out.print("[TEST 11] Activity Logging DTO... ");
        try {
            ActivityLogDTO log = new ActivityLogDTO(1, "student01", "SUBMIT_EXAM", "Submitted room ROOM99 with score 8.5", "127.0.0.1");
            String logJson = JsonUtil.toJson(log);
            ActivityLogDTO logParsed = JsonUtil.fromJson(logJson, ActivityLogDTO.class);

            assert "SUBMIT_EXAM".equals(logParsed.getAction());
            assert "student01".equals(logParsed.getUsername());
            assert logParsed.getFormattedTime() != null;

            System.out.println("PASSED");
            testsPassed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            t.printStackTrace();
        }

        // -------------------------------------------------------------
        // TEST SUMMARY
        // -------------------------------------------------------------
        System.out.println("\n===============================================================");
        System.out.printf("  TEST RESULTS: %d/%d TESTS PASSED\n", testsPassed, totalTests);
        if (testsPassed == totalTests) {
            System.out.println("  >>> ALL PROTOCOL & DTO CHECKS SUCCEEDED! EXCELLENT! <<<");
        } else {
            System.err.println("  >>> SOME TESTS FAILED! CHECK STACK TRACE ABOVE! <<<");
        }
        System.out.println("===============================================================\n");
    }
}

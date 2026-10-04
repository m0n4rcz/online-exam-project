package server.db.dao;

import server.db.DatabaseManager;
import shared.dtos.LeaderboardEntryDTO;
import shared.dtos.ScoreTrendPointDTO;
import shared.dtos.UserExamRecordDTO;
import shared.dtos.UserStatsDTO;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Data Access Object for storing exam results, retrieving history,
 * calculating graphical statistics (UserStatsDTO), and ranking leaderboards.
 */
public class ExamRecordDAO {

    /**
     * Saves an exam completion record (Practice or Multiplayer Room) and returns the generated record ID.
     */
    public long saveRecord(UserExamRecordDTO record) {
        if (record == null) throw new IllegalArgumentException("Record cannot be null");

        String sql = """
            INSERT INTO exam_records (username, test_type, title, room_code, topic, difficulty,
                                     score, correct_count, total_questions, time_spent_seconds, taken_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
        """;

        long takenAt = record.getTakenAt() > 0 ? record.getTakenAt() : System.currentTimeMillis();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, record.getUsername() != null ? record.getUsername().trim() : "anonymous");
            ps.setString(2, record.getTestType() != null ? record.getTestType().trim() : "PRACTICE");
            ps.setString(3, record.getTitle() != null ? record.getTitle().trim() : "Exam");
            ps.setString(4, record.getRoomCode());
            ps.setString(5, record.getTopic() != null ? record.getTopic().trim() : "ALL");
            ps.setString(6, record.getDifficulty() != null ? record.getDifficulty().trim() : "MEDIUM");
            ps.setDouble(7, Math.round(record.getScore() * 100.0) / 100.0);
            ps.setInt(8, record.getCorrectCount());
            ps.setInt(9, record.getTotalQuestions());
            ps.setInt(10, record.getTimeSpentSeconds());
            ps.setLong(11, takenAt);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long recordId = rs.getLong(1);
                        record.setRecordId(recordId);
                        return recordId;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Save exam record error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * Retrieves test history for a specific user ordered chronologically (newest first).
     */
    public List<UserExamRecordDTO> getHistoryByUsername(String username, int limit) {
        if (username == null || username.trim().isEmpty()) {
            return Collections.emptyList();
        }
        if (limit <= 0) limit = 50;

        List<UserExamRecordDTO> list = new ArrayList<>();
        String sql = """
            SELECT id, username, test_type, title, room_code, topic, difficulty,
                   score, correct_count, total_questions, time_spent_seconds, taken_at
            FROM exam_records
            WHERE username = ? COLLATE NOCASE
            ORDER BY taken_at DESC
            LIMIT ?;
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToRecord(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Get exam history error: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Aggregates comprehensive graphical statistics for a user.
     * Computes accuracy percentages by topic and difficulty, score progression trend,
     * and high/low averages.
     */
    public UserStatsDTO getUserStats(String username) {
        if (username == null || username.trim().isEmpty()) {
            return new UserStatsDTO("unknown");
        }

        UserStatsDTO stats = new UserStatsDTO(username.trim());

        // 1. Overall Aggregation
        String overallSql = """
            SELECT 
                COUNT(*) AS total_tests,
                SUM(CASE WHEN test_type = 'PRACTICE' THEN 1 ELSE 0 END) AS practice_tests,
                SUM(CASE WHEN test_type = 'ROOM' THEN 1 ELSE 0 END) AS room_tests,
                AVG(score) AS avg_score,
                MAX(score) AS max_score,
                MIN(score) AS min_score,
                SUM(total_questions) AS total_questions_sum,
                SUM(correct_count) AS total_correct_sum
            FROM exam_records
            WHERE username = ? COLLATE NOCASE;
        """;

        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(overallSql)) {
                ps.setString(1, username.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt("total_tests") > 0) {
                        int total = rs.getInt("total_tests");
                        stats.setTotalTests(total);
                        stats.setPracticeTests(rs.getInt("practice_tests"));
                        stats.setRoomTests(rs.getInt("room_tests"));
                        stats.setAverageScore(round1Dec(rs.getDouble("avg_score")));
                        stats.setHighestScore(round1Dec(rs.getDouble("max_score")));
                        stats.setLowestScore(round1Dec(rs.getDouble("min_score")));

                        int totalQuestions = rs.getInt("total_questions_sum");
                        int totalCorrect = rs.getInt("total_correct_sum");
                        stats.setTotalQuestionsAnswered(totalQuestions);
                        stats.setTotalCorrectAnswers(totalCorrect);

                        if (totalQuestions > 0) {
                            stats.setOverallAccuracyPercentage(round1Dec((totalCorrect * 100.0) / totalQuestions));
                        }
                    }
                }
            }

            // If user has never taken an exam, return empty stats
            if (stats.getTotalTests() == 0) {
                return stats;
            }

            // 2. Topic Performance & Distribution
            String topicSql = """
                SELECT topic, 
                       COUNT(*) AS test_count,
                       SUM(correct_count) AS topic_correct,
                       SUM(total_questions) AS topic_total
                FROM exam_records
                WHERE username = ? COLLATE NOCASE
                GROUP BY topic;
            """;
            try (PreparedStatement ps = conn.prepareStatement(topicSql)) {
                ps.setString(1, username.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    Map<String, Double> topicAcc = new HashMap<>();
                    Map<String, Integer> topicDist = new HashMap<>();
                    while (rs.next()) {
                        String topic = rs.getString("topic");
                        int count = rs.getInt("test_count");
                        int correct = rs.getInt("topic_correct");
                        int total = rs.getInt("topic_total");

                        topicDist.put(topic, count);
                        double acc = total > 0 ? (correct * 100.0) / total : 0.0;
                        topicAcc.put(topic, round1Dec(acc));
                    }
                    stats.setTopicAccuracy(topicAcc);
                    stats.setTopicDistribution(topicDist);
                }
            }

            // 3. Difficulty Breakdown
            String diffSql = """
                SELECT difficulty,
                       SUM(correct_count) AS diff_correct,
                       SUM(total_questions) AS diff_total
                FROM exam_records
                WHERE username = ? COLLATE NOCASE
                GROUP BY difficulty;
            """;
            try (PreparedStatement ps = conn.prepareStatement(diffSql)) {
                ps.setString(1, username.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    Map<String, Double> diffAcc = new HashMap<>();
                    while (rs.next()) {
                        String diff = rs.getString("difficulty");
                        int correct = rs.getInt("diff_correct");
                        int total = rs.getInt("diff_total");
                        double acc = total > 0 ? (correct * 100.0) / total : 0.0;
                        diffAcc.put(diff, round1Dec(acc));
                    }
                    stats.setDifficultyAccuracy(diffAcc);
                }
            }

            // 4. Score Progression Trend (Last 20 exams in chronological order for Line Chart)
            String trendSql = """
                SELECT score, correct_count, total_questions, taken_at
                FROM (
                    SELECT score, correct_count, total_questions, taken_at
                    FROM exam_records
                    WHERE username = ? COLLATE NOCASE
                    ORDER BY taken_at DESC
                    LIMIT 20
                )
                ORDER BY taken_at ASC;
            """;
            try (PreparedStatement ps = conn.prepareStatement(trendSql)) {
                ps.setString(1, username.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    List<ScoreTrendPointDTO> trend = new ArrayList<>();
                    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
                    int idx = 1;
                    while (rs.next()) {
                        double score = round1Dec(rs.getDouble("score"));
                        int corr = rs.getInt("correct_count");
                        int tot = rs.getInt("total_questions");
                        double acc = tot > 0 ? round1Dec((corr * 100.0) / tot) : 0.0;
                        long takenAt = rs.getLong("taken_at");
                        String label = "Test " + idx + " (" + sdf.format(new Date(takenAt)) + ")";

                        trend.add(new ScoreTrendPointDTO(idx, label, score, acc));
                        idx++;
                    }
                    stats.setScoreTrend(trend);
                }
            }

            // 5. Recent records for summary table (last 10)
            stats.setRecentRecords(getHistoryByUsername(username, 10));

        } catch (SQLException e) {
            throw new RuntimeException("Calculate user stats error: " + e.getMessage(), e);
        }

        return stats;
    }

    /**
     * Computes the global leaderboard across all users.
     */
    public List<LeaderboardEntryDTO> getLeaderboard(int limit) {
        if (limit <= 0) limit = 20;

        List<LeaderboardEntryDTO> list = new ArrayList<>();
        String sql = """
            SELECT 
                u.username,
                u.display_name,
                COUNT(e.id) AS total_exams,
                AVG(e.score) AS avg_score,
                MAX(e.score) AS high_score,
                SUM(e.correct_count) AS total_corr,
                SUM(e.total_questions) AS total_ques
            FROM users u
            JOIN exam_records e ON u.username = e.username
            GROUP BY u.username, u.display_name
            ORDER BY high_score DESC, avg_score DESC
            LIMIT ?;
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    int totalExams = rs.getInt("total_exams");
                    double avg = round1Dec(rs.getDouble("avg_score"));
                    double high = round1Dec(rs.getDouble("high_score"));
                    int corr = rs.getInt("total_corr");
                    int ques = rs.getInt("total_ques");
                    double acc = ques > 0 ? round1Dec((corr * 100.0) / ques) : 0.0;

                    list.add(new LeaderboardEntryDTO(
                            rank++,
                            rs.getString("username"),
                            rs.getString("display_name"),
                            totalExams,
                            avg,
                            high,
                            acc
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Get leaderboard error: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Gets all participant scores for a completed multiplayer room.
     */
    public List<UserExamRecordDTO> getRoomResults(String roomCode) {
        if (roomCode == null || roomCode.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<UserExamRecordDTO> list = new ArrayList<>();
        String sql = """
            SELECT id, username, test_type, title, room_code, topic, difficulty,
                   score, correct_count, total_questions, time_spent_seconds, taken_at
            FROM exam_records
            WHERE room_code = ?
            ORDER BY score DESC, time_spent_seconds ASC;
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, roomCode.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToRecord(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Get room results error: " + e.getMessage(), e);
        }
        return list;
    }

    private UserExamRecordDTO mapRowToRecord(ResultSet rs) throws SQLException {
        return new UserExamRecordDTO(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("test_type"),
                rs.getString("title"),
                rs.getString("room_code"),
                rs.getString("topic"),
                rs.getString("difficulty"),
                rs.getDouble("score"),
                rs.getInt("correct_count"),
                rs.getInt("total_questions"),
                rs.getInt("time_spent_seconds"),
                rs.getLong("taken_at")
        );
    }

    private static double round1Dec(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}

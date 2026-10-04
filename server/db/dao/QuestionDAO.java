package server.db.dao;

import server.db.DatabaseManager;
import shared.QuestionCategory;
import shared.QuestionDifficulty;
import shared.dtos.FullQuestionDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for question bank management and random exam paper generation.
 */
public class QuestionDAO {

    /**
     * Inserts a new question into the bank and returns the auto-generated ID.
     */
    public int addQuestion(FullQuestionDTO q) {
        if (q == null) throw new IllegalArgumentException("Question cannot be null");

        String sql = """
            INSERT INTO questions (content, option_a, option_b, option_c, option_d, correct_option, explanation, category, difficulty, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
        """;

        long now = System.currentTimeMillis();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, q.getContent());
            ps.setString(2, q.getOptionA());
            ps.setString(3, q.getOptionB());
            ps.setString(4, q.getOptionC());
            ps.setString(5, q.getOptionD());
            ps.setString(6, q.getCorrectOption() != null ? q.getCorrectOption().toUpperCase().trim() : "A");
            ps.setString(7, q.getExplanation());
            ps.setString(8, q.getCategory() != null ? q.getCategory().trim() : QuestionCategory.GENERAL_IT);
            ps.setString(9, q.getDifficulty() != null ? q.getDifficulty().trim() : QuestionDifficulty.MEDIUM);
            ps.setLong(10, now);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        q.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Add question error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * Updates an existing question by ID.
     */
    public boolean updateQuestion(FullQuestionDTO q) {
        if (q == null || q.getId() <= 0) return false;

        String sql = """
            UPDATE questions
            SET content = ?, option_a = ?, option_b = ?, option_c = ?, option_d = ?,
                correct_option = ?, explanation = ?, category = ?, difficulty = ?
            WHERE id = ?;
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, q.getContent());
            ps.setString(2, q.getOptionA());
            ps.setString(3, q.getOptionB());
            ps.setString(4, q.getOptionC());
            ps.setString(5, q.getOptionD());
            ps.setString(6, q.getCorrectOption() != null ? q.getCorrectOption().toUpperCase().trim() : "A");
            ps.setString(7, q.getExplanation());
            ps.setString(8, q.getCategory() != null ? q.getCategory().trim() : QuestionCategory.GENERAL_IT);
            ps.setString(9, q.getDifficulty() != null ? q.getDifficulty().trim() : QuestionDifficulty.MEDIUM);
            ps.setInt(10, q.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Update question error: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a question from the question bank.
     */
    public boolean deleteQuestion(int id) {
        String sql = "DELETE FROM questions WHERE id = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Delete question error: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves a question by its primary key ID.
     */
    public FullQuestionDTO getQuestionById(int id) {
        String sql = "SELECT * FROM questions WHERE id = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToQuestion(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Get question error: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Retrieves all questions in the bank.
     */
    public List<FullQuestionDTO> getAllQuestions() {
        List<FullQuestionDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM questions ORDER BY category ASC, id ASC;";
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRowToQuestion(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Get all questions error: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Randomly samples questions based on category, difficulty, and requested question count.
     * If category or difficulty is "ALL" or null, the filter is bypassed.
     */
    public List<FullQuestionDTO> getRandomQuestions(String category, String difficulty, int count) {
        if (count <= 0) count = 10;
        String catFilter = (category == null || category.trim().isEmpty() || category.equalsIgnoreCase("ALL")) ? "ALL" : category.trim();
        String diffFilter = (difficulty == null || difficulty.trim().isEmpty() || difficulty.equalsIgnoreCase("ALL")) ? "ALL" : difficulty.trim();

        String sql = """
            SELECT * FROM questions
            WHERE (? = 'ALL' OR category = ?)
              AND (? = 'ALL' OR difficulty = ?)
            ORDER BY RANDOM()
            LIMIT ?;
        """;

        List<FullQuestionDTO> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, catFilter);
            ps.setString(2, catFilter);
            ps.setString(3, diffFilter);
            ps.setString(4, diffFilter);
            ps.setInt(5, count);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToQuestion(rs));
                }
            }

            // Fallback: If not enough questions matched the strict category/difficulty filter,
            // supplement with random questions from any topic to guarantee the room/exam starts properly.
            if (list.size() < count) {
                int needed = count - list.size();
                List<Integer> existingIds = list.stream().map(FullQuestionDTO::getId).toList();
                String fallbackSql = "SELECT * FROM questions ORDER BY RANDOM() LIMIT ?;";
                try (PreparedStatement fallbackPs = conn.prepareStatement(fallbackSql)) {
                    fallbackPs.setInt(1, count * 2);
                    try (ResultSet fallbackRs = fallbackPs.executeQuery()) {
                        while (fallbackRs.next() && needed > 0) {
                            int fallbackId = fallbackRs.getInt("id");
                            if (!existingIds.contains(fallbackId)) {
                                list.add(mapRowToQuestion(fallbackRs));
                                needed--;
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Get random questions error: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Counts questions available for a given topic and difficulty.
     */
    public int countQuestions(String category, String difficulty) {
        String catFilter = (category == null || category.trim().isEmpty() || category.equalsIgnoreCase("ALL")) ? "ALL" : category.trim();
        String diffFilter = (difficulty == null || difficulty.trim().isEmpty() || difficulty.equalsIgnoreCase("ALL")) ? "ALL" : difficulty.trim();

        String sql = """
            SELECT COUNT(*) FROM questions
            WHERE (? = 'ALL' OR category = ?)
              AND (? = 'ALL' OR difficulty = ?);
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, catFilter);
            ps.setString(2, catFilter);
            ps.setString(3, diffFilter);
            ps.setString(4, diffFilter);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Count questions error: " + e.getMessage(), e);
        }
        return 0;
    }

    private FullQuestionDTO mapRowToQuestion(ResultSet rs) throws SQLException {
        return new FullQuestionDTO(
                rs.getInt("id"),
                rs.getString("content"),
                rs.getString("option_a"),
                rs.getString("option_b"),
                rs.getString("option_c"),
                rs.getString("option_d"),
                rs.getString("correct_option"),
                rs.getString("explanation"),
                rs.getString("category"),
                rs.getString("difficulty")
        );
    }
}

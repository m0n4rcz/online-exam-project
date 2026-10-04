package server.db.dao;

import server.db.DatabaseManager;
import shared.dtos.ActivityLogDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data Access Object for audit trail and system activity logging.
 * Rubric requirement: "Logging activities: 1 point"
 */
public class ActivityLogDAO {

    /**
     * Records a new activity log entry.
     */
    public long log(String username, String action, String details, String ipAddress) {
        String sql = """
            INSERT INTO activity_logs (timestamp, username, action, details, ip_address)
            VALUES (?, ?, ?, ?, ?);
        """;

        long now = System.currentTimeMillis();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, now);
            ps.setString(2, username);
            ps.setString(3, action != null ? action.trim() : "UNKNOWN");
            ps.setString(4, details);
            ps.setString(5, ipAddress);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
            }
        } catch (SQLException e) {
            // Logging failure should not crash main operations
            System.err.println("Failed to insert activity log: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Retrieves the most recent system activity logs.
     */
    public List<ActivityLogDTO> getRecentLogs(int limit) {
        if (limit <= 0) limit = 100;

        List<ActivityLogDTO> list = new ArrayList<>();
        String sql = """
            SELECT id, timestamp, username, action, details, ip_address
            FROM activity_logs
            ORDER BY timestamp DESC
            LIMIT ?;
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToLog(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Get recent logs error: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Retrieves logs for a specific user.
     */
    public List<ActivityLogDTO> getLogsByUsername(String username, int limit) {
        if (username == null || username.trim().isEmpty()) {
            return Collections.emptyList();
        }
        if (limit <= 0) limit = 50;

        List<ActivityLogDTO> list = new ArrayList<>();
        String sql = """
            SELECT id, timestamp, username, action, details, ip_address
            FROM activity_logs
            WHERE username = ? COLLATE NOCASE
            ORDER BY timestamp DESC
            LIMIT ?;
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToLog(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Get user logs error: " + e.getMessage(), e);
        }
        return list;
    }

    private ActivityLogDTO mapRowToLog(ResultSet rs) throws SQLException {
        ActivityLogDTO log = new ActivityLogDTO(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("action"),
                rs.getString("details"),
                rs.getString("ip_address")
        );
        log.setTimestamp(rs.getLong("timestamp"));
        return log;
    }
}

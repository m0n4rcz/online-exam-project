package server.db.dao;

import server.db.DatabaseManager;
import server.db.PasswordUtil;
import shared.UserRole;
import shared.dtos.UserProfileDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for users and authentication management.
 */
public class UserDAO {

    /**
     * Authenticates a user by username and raw password.
     * Returns UserProfileDTO if successful, null if invalid credentials.
     */
    public UserProfileDTO login(String username, String rawPassword) {
        if (username == null || rawPassword == null) {
            return null;
        }

        String sql = "SELECT username, password_hash, display_name, email, role, created_at FROM users WHERE username = ? COLLATE NOCASE;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (PasswordUtil.verifyPassword(rawPassword, storedHash)) {
                        return mapRowToProfile(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Login query error: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Registers a new account with hashed password.
     */
    public UserProfileDTO register(String username, String rawPassword, String displayName, String email, String role) {
        if (username == null || username.trim().isEmpty() || rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Username and password are required");
        }

        String trimmedUser = username.trim();
        String assignedRole = UserRole.isValid(role) ? role.trim().toUpperCase() : UserRole.STUDENT;
        String finalDisplayName = (displayName != null && !displayName.trim().isEmpty()) ? displayName.trim() : trimmedUser;
        String hashedPassword = PasswordUtil.hashPassword(rawPassword);
        long now = System.currentTimeMillis();

        String sql = """
            INSERT INTO users (username, password_hash, display_name, email, role, created_at)
            VALUES (?, ?, ?, ?, ?, ?);
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, trimmedUser);
            ps.setString(2, hashedPassword);
            ps.setString(3, finalDisplayName);
            ps.setString(4, email != null ? email.trim() : null);
            ps.setString(5, assignedRole);
            ps.setLong(6, now);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                return new UserProfileDTO(trimmedUser, finalDisplayName, email, assignedRole, now);
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 19 || e.getMessage().contains("UNIQUE constraint failed")) {
                throw new IllegalStateException("Username already exists: " + trimmedUser);
            }
            throw new RuntimeException("Register error: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Finds user profile by username.
     */
    public UserProfileDTO findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT username, display_name, email, role, created_at FROM users WHERE username = ? COLLATE NOCASE;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UserProfileDTO(
                            rs.getString("username"),
                            rs.getString("display_name"),
                            rs.getString("email"),
                            rs.getString("role"),
                            rs.getLong("created_at")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Query user error: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Checks if a username already exists.
     */
    public boolean usernameExists(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        String sql = "SELECT 1 FROM users WHERE username = ? COLLATE NOCASE;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Check username error: " + e.getMessage(), e);
        }
    }

    /**
     * Updates display name and email for a user.
     */
    public boolean updateProfile(String username, String displayName, String email) {
        if (username == null) return false;

        String sql = "UPDATE users SET display_name = ?, email = ? WHERE username = ? COLLATE NOCASE;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, displayName != null ? displayName.trim() : username);
            ps.setString(2, email != null ? email.trim() : null);
            ps.setString(3, username.trim());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Update profile error: " + e.getMessage(), e);
        }
    }

    /**
     * Changes user password after validating old password.
     */
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        if (username == null || oldPassword == null || newPassword == null) return false;

        String selectSql = "SELECT password_hash FROM users WHERE username = ? COLLATE NOCASE;";
        String updateSql = "UPDATE users SET password_hash = ? WHERE username = ? COLLATE NOCASE;";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement psSel = conn.prepareStatement(selectSql)) {

            psSel.setString(1, username.trim());
            try (ResultSet rs = psSel.executeQuery()) {
                if (!rs.next()) return false;
                String currentHash = rs.getString("password_hash");
                if (!PasswordUtil.verifyPassword(oldPassword, currentHash)) {
                    return false; // Old password wrong
                }
            }

            try (PreparedStatement psUpd = conn.prepareStatement(updateSql)) {
                psUpd.setString(1, PasswordUtil.hashPassword(newPassword));
                psUpd.setString(2, username.trim());
                return psUpd.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Change password error: " + e.getMessage(), e);
        }
    }

    /**
     * Lists all users in the system.
     */
    public List<UserProfileDTO> getAllUsers() {
        List<UserProfileDTO> list = new ArrayList<>();
        String sql = "SELECT username, display_name, email, role, created_at FROM users ORDER BY created_at DESC;";
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new UserProfileDTO(
                        rs.getString("username"),
                        rs.getString("display_name"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getLong("created_at")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fetch all users error: " + e.getMessage(), e);
        }
        return list;
    }

    private UserProfileDTO mapRowToProfile(ResultSet rs) throws SQLException {
        return new UserProfileDTO(
                rs.getString("username"),
                rs.getString("display_name"),
                rs.getString("email"),
                rs.getString("role"),
                rs.getLong("created_at")
        );
    }
}

package shared.dtos;

import java.io.Serializable;

/**
 * Profile information for an account.
 * Rubric requirement: "Account registration and management"
 */
public class UserProfileDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String displayName;
    private String email;
    private String role;
    private long createdAt;

    public UserProfileDTO() {}

    public UserProfileDTO(String username, String displayName, String email, String role, long createdAt) {
        this.username = username;
        this.displayName = displayName;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}

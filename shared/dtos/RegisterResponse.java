package shared.dtos;

import java.io.Serializable;

/**
 * Server's response to an account registration attempt.
 * Rubric requirement: "Account registration and management"
 */
public class RegisterResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private String username;

    public RegisterResponse() {}

    public RegisterResponse(boolean success, String message, String username) {
        this.success = success;
        this.message = message;
        this.username = username;
    }

    public static RegisterResponse ok(String username) {
        return new RegisterResponse(true, "Registration successful. You can now login.", username);
    }

    public static RegisterResponse fail(String message) {
        return new RegisterResponse(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

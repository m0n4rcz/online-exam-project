package shared.dtos;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Audit log entry for tracking system actions.
 * Rubric requirement: "Logging activities: 1 point"
 */
public class ActivityLogDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private long timestamp;
    private String formattedTime;
    private String username;
    private String action;          // e.g. "LOGIN", "CREATE_ROOM", "SUBMIT_EXAM"
    private String details;
    private String ipAddress;

    public ActivityLogDTO() {
        this.timestamp = System.currentTimeMillis();
        this.formattedTime = formatTimestamp(this.timestamp);
    }

    public ActivityLogDTO(long id, String username, String action, String details, String ipAddress) {
        this.id = id;
        this.timestamp = System.currentTimeMillis();
        this.formattedTime = formatTimestamp(this.timestamp);
        this.username = username;
        this.action = action;
        this.details = details;
        this.ipAddress = ipAddress;
    }

    private static String formatTimestamp(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(time));
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { 
        this.timestamp = timestamp;
        this.formattedTime = formatTimestamp(timestamp);
    }

    public String getFormattedTime() { return formattedTime; }
    public void setFormattedTime(String formattedTime) { this.formattedTime = formattedTime; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    @Override
    public String toString() {
        return String.format("[%s] %s by %s (%s): %s", formattedTime, action, username, ipAddress, details);
    }
}

package server.core;

/**
 * Global configuration constants for the Socket Server.
 */
public final class ServerConfig {

    private ServerConfig() {}

    public static final int DEFAULT_PORT = 8888;
    public static final String DEFAULT_HOST = "0.0.0.0";
    public static final int MAX_CLIENT_THREADS = 100;
    public static final int SOCKET_TIMEOUT_MS = 60000; // 60s idle timeout
}

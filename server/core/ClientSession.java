package server.core;

import shared.Packet;
import shared.PacketCodec;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents an active connected client socket and their authenticated state.
 */
public class ClientSession {

    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);

    private final long sessionId;
    private final Socket socket;
    private final InputStream in;
    private final OutputStream out;
    private final String remoteAddress;
    private final long connectedAt;

    private volatile String username;
    private volatile String displayName;
    private volatile String role;
    private volatile String token;
    private volatile String currentRoomCode;

    public ClientSession(Socket socket) throws IOException {
        this.sessionId = ID_GENERATOR.getAndIncrement();
        this.socket = socket;
        this.in = socket.getInputStream();
        this.out = new BufferedOutputStream(socket.getOutputStream());
        this.remoteAddress = socket.getRemoteSocketAddress() != null 
                ? socket.getRemoteSocketAddress().toString() 
                : "unknown";
        this.connectedAt = System.currentTimeMillis();
    }

    /**
     * Sends a packet to this client synchronously and safely across threads.
     */
    public synchronized void sendPacket(Packet packet) throws IOException {
        if (socket.isClosed()) {
            throw new IOException("Cannot send packet: socket is closed for session " + sessionId);
        }
        PacketCodec.writePacket(out, packet);
    }

    /**
     * Authenticates this session with user credentials.
     */
    public void authenticate(String username, String displayName, String role) {
        this.username = username;
        this.displayName = displayName;
        this.role = role;
        this.token = UUID.randomUUID().toString();
    }

    public void clearAuthentication() {
        this.username = null;
        this.displayName = null;
        this.role = null;
        this.token = null;
        this.currentRoomCode = null;
    }

    public boolean isAuthenticated() {
        return username != null && !username.trim().isEmpty();
    }

    public void close() {
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {}
    }

    public long getSessionId() { return sessionId; }
    public Socket getSocket() { return socket; }
    public InputStream getInputStream() { return in; }
    public String getRemoteAddress() { return remoteAddress; }
    public long getConnectedAt() { return connectedAt; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getCurrentRoomCode() { return currentRoomCode; }
    public void setCurrentRoomCode(String currentRoomCode) { this.currentRoomCode = currentRoomCode; }

    @Override
    public String toString() {
        return String.format("ClientSession[id=%d, user=%s, ip=%s]", sessionId, username, remoteAddress);
    }
}

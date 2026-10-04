package server.core;

import shared.Packet;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages all active connected client sessions and handles thread-safe messaging/broadcasting.
 */
public class SessionManager {

    private static final Logger LOGGER = Logger.getLogger(SessionManager.class.getName());

    private final Set<ClientSession> allSessions = ConcurrentHashMap.newKeySet();
    private final Map<String, ClientSession> userSessions = new ConcurrentHashMap<>();

    public void registerSession(ClientSession session) {
        allSessions.add(session);
        LOGGER.info("Registered session: " + session);
    }

    public void removeSession(ClientSession session) {
        allSessions.remove(session);
        if (session.getUsername() != null) {
            userSessions.remove(session.getUsername().toLowerCase());
        }
        LOGGER.info("Removed session: " + session);
    }

    /**
     * Binds an authenticated username to a session, kicking out any existing stale session.
     */
    public void bindUser(ClientSession session, String username, String displayName, String role) {
        String key = username.toLowerCase();
        ClientSession old = userSessions.put(key, session);
        if (old != null && old != session) {
            LOGGER.warning("Duplicate login detected for " + username + ". Disconnecting previous session.");
            old.close();
            allSessions.remove(old);
        }
        session.authenticate(username, displayName, role);
    }

    public void unbindUser(String username) {
        if (username != null) {
            userSessions.remove(username.toLowerCase());
        }
    }

    public ClientSession getSessionByUsername(String username) {
        if (username == null) return null;
        return userSessions.get(username.toLowerCase());
    }

    public boolean isUserOnline(String username) {
        if (username == null) return false;
        ClientSession s = userSessions.get(username.toLowerCase());
        return s != null && !s.getSocket().isClosed();
    }

    /**
     * Broadcasts a packet to all connected clients.
     */
    public void broadcastToAll(Packet packet) {
        for (ClientSession session : allSessions) {
            try {
                session.sendPacket(packet);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to broadcast to " + session, e);
            }
        }
    }

    /**
     * Broadcasts a packet to a specific collection of usernames.
     */
    public void broadcastToUsers(Collection<String> usernames, Packet packet) {
        if (usernames == null || usernames.isEmpty()) return;
        for (String u : usernames) {
            ClientSession s = getSessionByUsername(u);
            if (s != null) {
                try {
                    s.sendPacket(packet);
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Failed to send packet to " + u, e);
                }
            }
        }
    }

    public int getActiveSessionCount() {
        return allSessions.size();
    }

    public int getAuthenticatedUserCount() {
        return userSessions.size();
    }
}

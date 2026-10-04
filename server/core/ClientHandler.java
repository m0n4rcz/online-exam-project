package server.core;

import shared.Packet;
import shared.PacketCodec;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles communication for a single client connection in a dedicated thread.
 * Reads incoming packets, dispatches them, and writes responses.
 */
public class ClientHandler implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());

    private final ClientSession session;
    private final RequestDispatcher dispatcher;
    private final SessionManager sessionManager;
    private volatile boolean running = true;

    public ClientHandler(ClientSession session, RequestDispatcher dispatcher, SessionManager sessionManager) {
        this.session = session;
        this.dispatcher = dispatcher;
        this.sessionManager = sessionManager;
    }

    @Override
    public void run() {
        LOGGER.info("Client connected: " + session);
        sessionManager.registerSession(session);

        try {
            while (running && !session.getSocket().isClosed()) {
                Packet req;
                try {
                    req = PacketCodec.readPacket(session.getInputStream());
                } catch (EOFException | SocketException e) {
                    // Client disconnected or connection was reset
                    break;
                }

                if (req == null) {
                    break;
                }

                // Dispatch to business logic
                Packet res = dispatcher.dispatch(session, req);
                if (res != null) {
                    session.sendPacket(res);
                }
            }
        } catch (IOException e) {
            if (running && !session.getSocket().isClosed()) {
                LOGGER.log(Level.WARNING, "I/O error with client " + session + ": " + e.getMessage());
            }
        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE, "Unexpected error in ClientHandler for " + session, t);
        } finally {
            close();
        }
    }

    public void stop() {
        running = false;
        close();
    }

    private void close() {
        sessionManager.removeSession(session);
        session.close();
        LOGGER.info("Client disconnected: " + session);
    }
}

package server.core;

import server.db.DatabaseManager;
import server.db.dao.ActivityLogDAO;
import server.db.dao.ExamRecordDAO;
import server.db.dao.QuestionDAO;
import server.db.dao.UserDAO;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main TCP Socket Server engine for the Online Exam System.
 * Initializes Database, manages client connection pooling, and handles lifecycle.
 */
public class ServerCore {

    private static final Logger LOGGER = Logger.getLogger(ServerCore.class.getName());

    private final int port;
    private final String dbUrl;

    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private volatile boolean running = false;

    private SessionManager sessionManager;
    private server.room.RoomManager roomManager;
    private RequestDispatcher requestDispatcher;

    public ServerCore() {
        this(ServerConfig.DEFAULT_PORT, DatabaseManager.DEFAULT_DB_URL);
    }

    public ServerCore(int port) {
        this(port, DatabaseManager.DEFAULT_DB_URL);
    }

    public ServerCore(int port, String dbUrl) {
        this.port = port;
        this.dbUrl = dbUrl;
    }

    /**
     * Starts the TCP Server and blocks on the accept loop.
     */
    public void start() throws IOException {
        // 1. Initialize SQLite Database & DAOs
        LOGGER.info("Initializing Database...");
        DatabaseManager.initialize(dbUrl);

        UserDAO userDAO = new UserDAO();
        QuestionDAO questionDAO = new QuestionDAO();
        ExamRecordDAO examRecordDAO = new ExamRecordDAO();
        ActivityLogDAO logDAO = new ActivityLogDAO();

        this.sessionManager = new SessionManager();
        this.roomManager = new server.room.RoomManager();
        this.requestDispatcher = new RequestDispatcher(userDAO, questionDAO, examRecordDAO, logDAO, sessionManager, roomManager);

        // 2. Open ServerSocket
        this.serverSocket = new ServerSocket(port);
        this.threadPool = Executors.newCachedThreadPool();
        this.running = true;

        printBanner();
        LOGGER.info(String.format("Server started successfully on port %d! Waiting for client connections...", port));

        // 3. Accept Loop
        try {
            while (running && !serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                clientSocket.setTcpNoDelay(true); // Disable Nagle's algorithm for low-latency packets

                ClientSession session = new ClientSession(clientSocket);
                ClientHandler handler = new ClientHandler(session, requestDispatcher, sessionManager);
                threadPool.submit(handler);
            }
        } catch (IOException e) {
            if (running) {
                LOGGER.log(Level.SEVERE, "ServerSocket encountered an error: " + e.getMessage(), e);
            }
        } finally {
            stop();
        }
    }

    /**
     * Gracefully stops the server, closing all sockets and releasing database resources.
     */
    public synchronized void stop() {
        if (!running) return;
        running = false;
        LOGGER.info("Shutting down Online Exam Server...");

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {}

        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(3, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
            }
        }

        LOGGER.info("Online Exam Server stopped cleanly.");
    }

    public boolean isRunning() {
        return running;
    }

    public int getPort() {
        return port;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public server.room.RoomManager getRoomManager() {
        return roomManager;
    }

    public RequestDispatcher getRequestDispatcher() {
        return requestDispatcher;
    }

    private void printBanner() {
        System.out.println("===============================================================");
        System.out.println("     ONLINE EXAM SYSTEM - MULTIPLAYER TCP SOCKET SERVER        ");
        System.out.println("===============================================================");
        System.out.printf("  Listening Port  : %d\n", port);
        System.out.printf("  Database Engine : SQLite (%s)\n", dbUrl);
        System.out.printf("  Protocol Magic  : 0x4558414D ('EXAM')\n");
        System.out.printf("  Header Size     : 12-byte binary framing\n");
        System.out.println("===============================================================\n");
    }

    public static void main(String[] args) {
        int port = ServerConfig.DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        ServerCore server = new ServerCore(port);

        // Register JVM Shutdown Hook
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

        try {
            server.start();
        } catch (Exception e) {
            System.err.println("Fatal: Server could not be started: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

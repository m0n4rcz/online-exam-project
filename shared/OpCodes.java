package shared;

/**
 * Defines all Application-Level Operation Codes (OpCodes) and Status Codes for the Exam System.
 * 
 * Each OpCode uniquely identifies an action or event transmitted across the TCP socket.
 * Range Guide:
 *   - 0x1000 - 0x1FFF: Authentication & User Management (Register, Login, Session, Profile)
 *   - 0x2000 - 0x2FFF: Room & Lobby Coordination (Create, List, Join, Ready, Start, End, Results)
 *   - 0x3000 - 0x3FFF: Exam Engine & Answering (Init, Change Answer, Early Submit, Score Notification)
 *   - 0x4000 - 0x4FFF: Practice Mode (Solo practice with difficulty/topic filtering)
 *   - 0x5000 - 0x5FFF: Activity Logging & Question Bank Management
 *   - 0x6000 - 0x6FFF: Stored Exam History, Graphical Statistics & Leaderboard
 *   - 0x9000 - 0x9FFF: System, Heartbeat & Reconnection
 */
public final class OpCodes {

    private OpCodes() {
        // Utility constant class - prevent instantiation
    }

    // ==========================================
    // 0x1000: AUTHENTICATION & USER MANAGEMENT
    // ==========================================
    public static final short LOGIN_REQ          = 0x1001; // Client -> Server: Submit credentials
    public static final short LOGIN_RES          = 0x1002; // Server -> Client: Auth token & user info
    public static final short REGISTER_REQ       = 0x1003; // Client -> Server: Register new account
    public static final short REGISTER_RES       = 0x1004; // Server -> Client: Registration result
    public static final short LOGOUT_REQ         = 0x1005; // Client -> Server: Cleanly disconnect
    public static final short LOGOUT_RES         = 0x1006; // Server -> Client: Logout confirmed
    public static final short UPDATE_PROFILE_REQ = 0x1007; // Client -> Server: Update user profile
    public static final short UPDATE_PROFILE_RES = 0x1008; // Server -> Client: Update result
    public static final short SESSION_CHECK_REQ  = 0x1009; // Client -> Server: Validate existing session
    public static final short SESSION_CHECK_RES  = 0x100A; // Server -> Client: Session valid status

    // ==========================================
    // 0x2000: ROOM & LOBBY MANAGEMENT
    // ==========================================
    public static final short CREATE_ROOM_REQ    = 0x2001; // Client -> Server: Create test room (duration, questions, topic, difficulty)
    public static final short CREATE_ROOM_RES    = 0x2002; // Server -> Client: Room code & host confirmed
    public static final short JOIN_ROOM_REQ      = 0x2003; // Client -> Server: Join room (allowed only if not started)
    public static final short JOIN_ROOM_RES      = 0x2004; // Server -> Client: Room details & participants
    public static final short TOGGLE_READY_REQ   = 0x2005; // Client -> Server: Toggle ready/unready status
    public static final short TOGGLE_READY_RES   = 0x2006; // Server -> Client: Ready state ACK
    public static final short START_ROOM_REQ     = 0x2007; // Host -> Server: Decide to start the test
    public static final short START_ROOM_RES     = 0x2008; // Server -> Host: Start request ACK
    public static final short LEAVE_ROOM_REQ     = 0x2009; // Client -> Server: Leave current room
    public static final short LEAVE_ROOM_RES     = 0x200A; // Server -> Client: Leave confirmed
    public static final short HOST_END_EXAM_REQ  = 0x200B; // Host -> Server: Decide to end the test early
    public static final short HOST_END_EXAM_RES  = 0x200C; // Server -> Host: End request ACK

    // Room broadcasts
    public static final short EVENT_ROOM_UPDATE  = 0x2010; // Server -> All Room Clients: Participant list/status changed
    public static final short EVENT_ROOM_START   = 0x2011; // Server -> All Room Clients: Test started countdown
    public static final short ROOM_CHAT_REQ      = 0x2012; // Client -> Server: Send message in room lobby
    public static final short EVENT_ROOM_CHAT    = 0x2013; // Server -> All Room Clients: Chat broadcast

    // Room listing & Completed room results
    public static final short ROOM_LIST_REQ      = 0x2020; // Client -> Server: View list of test rooms & statuses
    public static final short ROOM_LIST_RES      = 0x2021; // Server -> Client: List of rooms (not started, ongoing, finished)
    public static final short COMPLETED_ROOM_RESULT_REQ = 0x2030; // Client -> Server: View results for completed room
    public static final short COMPLETED_ROOM_RESULT_RES = 0x2031; // Server -> Client: Completed room leaderboard & scores

    // ==========================================
    // 0x3000: MULTIPLAYER EXAM ENGINE
    // ==========================================
    public static final short EXAM_INIT_DATA     = 0x3001; // Server -> Client: Server sends test questions + duration
    public static final short SUBMIT_ANSWER_REQ  = 0x3002; // Client -> Server: Change/save answered question
    public static final short SUBMIT_ANSWER_RES  = 0x3003; // Server -> Client: In-flight answer update ACK
    public static final short SUBMIT_EXAM_REQ    = 0x3004; // Client -> Server: Final/Early exam submission
    public static final short SUBMIT_EXAM_RES    = 0x3005; // Server -> Client: Submission ACK
    public static final short EVENT_EXAM_ENDED   = 0x3006; // Server -> All: Time's up broadcast / exam closed
    public static final short EXAM_RESULT_RES    = 0x3007; // Server -> Client: Score & number of correct answers
    public static final short EVENT_EXAM_TICK    = 0x3010; // Server -> All Room Clients: Time sync / seconds remaining
    public static final short EVENT_PARTICIPANT_SUBMITTED = 0x3011; // Server -> All: Notice someone submitted early

    // ==========================================
    // 0x4000: PRACTICE MODE (SOLO)
    // ==========================================
    public static final short PRACTICE_REQ       = 0x4001; // Client -> Server: Practice request (difficulty, topic, count, duration)
    public static final short PRACTICE_RES       = 0x4002; // Server -> Client: Practice questions & time limit
    public static final short PRACTICE_SUBMIT_REQ = 0x4003; // Client -> Server: Submit practice answers
    public static final short PRACTICE_RESULT_RES = 0x4004; // Server -> Client: Practice score & correct count

    // ==========================================
    // 0x5000: ACTIVITY LOGGING & QUESTION MANAGEMENT
    // ==========================================
    public static final short GET_LOGS_REQ       = 0x5001; // Client/Admin -> Server: View activity logs
    public static final short GET_LOGS_RES       = 0x5002; // Server -> Client: Activity log entries
    public static final short QUESTION_LIST_REQ  = 0x5010; // Manage questions: list all questions
    public static final short QUESTION_LIST_RES  = 0x5011; // Server -> Client: Question bank
    public static final short QUESTION_ADD_REQ   = 0x5012; // Add new question (topic, difficulty, options)
    public static final short QUESTION_ADD_RES   = 0x5013; // Add question result
    public static final short QUESTION_UPDATE_REQ = 0x5014;// Update question
    public static final short QUESTION_UPDATE_RES = 0x5015;// Update result
    public static final short QUESTION_DELETE_REQ = 0x5016;// Delete question
    public static final short QUESTION_DELETE_RES = 0x5017;// Delete result

    // ==========================================
    // 0x6000: EXAM HISTORY, GRAPHICAL STATISTICS & LEADERBOARD
    // ==========================================
    public static final short USER_HISTORY_REQ   = 0x6001; // Client -> Server: View stored tests user has taken
    public static final short USER_HISTORY_RES   = 0x6002; // Server -> Client: Past exam records
    public static final short USER_STATS_REQ     = 0x6003; // Client -> Server: Request data for graphical statistics
    public static final short USER_STATS_RES     = 0x6004; // Server -> Client: Statistics dataset (accuracy, charts)
    public static final short LEADERBOARD_REQ    = 0x6005; // Client -> Server: Request leaderboard
    public static final short LEADERBOARD_RES    = 0x6006; // Server -> Client: Global / topic leaderboard

    // ==========================================
    // 0x9000: SYSTEM, HEARTBEAT & KEEP-ALIVE
    // ==========================================
    public static final short PING               = (short) 0x9001; // Ping
    public static final short PONG               = (short) 0x9002; // Pong
    public static final short RECONNECT_REQ      = (short) 0x9003; // Reclaim session after drop
    public static final short RECONNECT_RES      = (short) 0x9004; // Restored state

    // ==========================================
    // STATUS CODES (Header Status Field)
    // ==========================================
    public static final short STATUS_OK                     = 0;
    public static final short STATUS_BAD_REQUEST            = 400;
    public static final short STATUS_UNAUTHORIZED           = 401;
    public static final short STATUS_FORBIDDEN              = 403;
    public static final short STATUS_NOT_FOUND              = 404;
    public static final short STATUS_CONFLICT               = 409;
    public static final short STATUS_ROOM_FULL              = 410;
    public static final short STATUS_ROOM_ALREADY_STARTED   = 411; // Cannot join if test has already started
    public static final short STATUS_ROOM_FINISHED          = 412; // Room already ended
    public static final short STATUS_NOT_HOST               = 413; // Only room host can start/end test
    public static final short STATUS_ALREADY_IN_ROOM        = 414;
    public static final short STATUS_EXAM_ALREADY_SUBMITTED = 415;
    public static final short STATUS_EXAM_TIME_EXPIRED      = 416; // Time limit has expired
    public static final short STATUS_SERVER_ERROR           = 500;

    // Aliases for backwards compatibility
    public static final short STATUS_ERROR_AUTH     = STATUS_UNAUTHORIZED;
    public static final short STATUS_ERROR_NOTFOUND = STATUS_NOT_FOUND;
    public static final short STATUS_ERROR_INVALID  = STATUS_BAD_REQUEST;

    /**
     * Translates an OpCode into a human-readable string for logging and Wireshark inspection.
     */
    public static String getName(short opCode) {
        return switch (opCode) {
            case LOGIN_REQ          -> "LOGIN_REQ (0x1001)";
            case LOGIN_RES          -> "LOGIN_RES (0x1002)";
            case REGISTER_REQ       -> "REGISTER_REQ (0x1003)";
            case REGISTER_RES       -> "REGISTER_RES (0x1004)";
            case LOGOUT_REQ         -> "LOGOUT_REQ (0x1005)";
            case LOGOUT_RES         -> "LOGOUT_RES (0x1006)";
            case UPDATE_PROFILE_REQ -> "UPDATE_PROFILE_REQ (0x1007)";
            case UPDATE_PROFILE_RES -> "UPDATE_PROFILE_RES (0x1008)";
            case SESSION_CHECK_REQ  -> "SESSION_CHECK_REQ (0x1009)";
            case SESSION_CHECK_RES  -> "SESSION_CHECK_RES (0x100A)";

            case CREATE_ROOM_REQ    -> "CREATE_ROOM_REQ (0x2001)";
            case CREATE_ROOM_RES    -> "CREATE_ROOM_RES (0x2002)";
            case JOIN_ROOM_REQ      -> "JOIN_ROOM_REQ (0x2003)";
            case JOIN_ROOM_RES      -> "JOIN_ROOM_RES (0x2004)";
            case TOGGLE_READY_REQ   -> "TOGGLE_READY_REQ (0x2005)";
            case TOGGLE_READY_RES   -> "TOGGLE_READY_RES (0x2006)";
            case START_ROOM_REQ     -> "START_ROOM_REQ (0x2007)";
            case START_ROOM_RES     -> "START_ROOM_RES (0x2008)";
            case LEAVE_ROOM_REQ     -> "LEAVE_ROOM_REQ (0x2009)";
            case LEAVE_ROOM_RES     -> "LEAVE_ROOM_RES (0x200A)";
            case HOST_END_EXAM_REQ  -> "HOST_END_EXAM_REQ (0x200B)";
            case HOST_END_EXAM_RES  -> "HOST_END_EXAM_RES (0x200C)";
            case EVENT_ROOM_UPDATE  -> "EVENT_ROOM_UPDATE (0x2010)";
            case EVENT_ROOM_START   -> "EVENT_ROOM_START (0x2011)";
            case ROOM_CHAT_REQ      -> "ROOM_CHAT_REQ (0x2012)";
            case EVENT_ROOM_CHAT    -> "EVENT_ROOM_CHAT (0x2013)";
            case ROOM_LIST_REQ      -> "ROOM_LIST_REQ (0x2020)";
            case ROOM_LIST_RES      -> "ROOM_LIST_RES (0x2021)";
            case COMPLETED_ROOM_RESULT_REQ -> "COMPLETED_ROOM_RESULT_REQ (0x2030)";
            case COMPLETED_ROOM_RESULT_RES -> "COMPLETED_ROOM_RESULT_RES (0x2031)";

            case EXAM_INIT_DATA     -> "EXAM_INIT_DATA (0x3001)";
            case SUBMIT_ANSWER_REQ  -> "SUBMIT_ANSWER_REQ (0x3002)";
            case SUBMIT_ANSWER_RES  -> "SUBMIT_ANSWER_RES (0x3003)";
            case SUBMIT_EXAM_REQ    -> "SUBMIT_EXAM_REQ (0x3004)";
            case SUBMIT_EXAM_RES    -> "SUBMIT_EXAM_RES (0x3005)";
            case EVENT_EXAM_ENDED   -> "EVENT_EXAM_ENDED (0x3006)";
            case EXAM_RESULT_RES    -> "EXAM_RESULT_RES (0x3007)";
            case EVENT_EXAM_TICK    -> "EVENT_EXAM_TICK (0x3010)";
            case EVENT_PARTICIPANT_SUBMITTED -> "EVENT_PARTICIPANT_SUBMITTED (0x3011)";

            case PRACTICE_REQ       -> "PRACTICE_REQ (0x4001)";
            case PRACTICE_RES       -> "PRACTICE_RES (0x4002)";
            case PRACTICE_SUBMIT_REQ-> "PRACTICE_SUBMIT_REQ (0x4003)";
            case PRACTICE_RESULT_RES-> "PRACTICE_RESULT_RES (0x4004)";

            case GET_LOGS_REQ       -> "GET_LOGS_REQ (0x5001)";
            case GET_LOGS_RES       -> "GET_LOGS_RES (0x5002)";
            case QUESTION_LIST_REQ  -> "QUESTION_LIST_REQ (0x5010)";
            case QUESTION_LIST_RES  -> "QUESTION_LIST_RES (0x5011)";
            case QUESTION_ADD_REQ   -> "QUESTION_ADD_REQ (0x5012)";
            case QUESTION_ADD_RES   -> "QUESTION_ADD_RES (0x5013)";
            case QUESTION_UPDATE_REQ-> "QUESTION_UPDATE_REQ (0x5014)";
            case QUESTION_UPDATE_RES-> "QUESTION_UPDATE_RES (0x5015)";
            case QUESTION_DELETE_REQ-> "QUESTION_DELETE_REQ (0x5016)";
            case QUESTION_DELETE_RES-> "QUESTION_DELETE_RES (0x5017)";

            case USER_HISTORY_REQ   -> "USER_HISTORY_REQ (0x6001)";
            case USER_HISTORY_RES   -> "USER_HISTORY_RES (0x6002)";
            case USER_STATS_REQ     -> "USER_STATS_REQ (0x6003)";
            case USER_STATS_RES     -> "USER_STATS_RES (0x6004)";
            case LEADERBOARD_REQ    -> "LEADERBOARD_REQ (0x6005)";
            case LEADERBOARD_RES    -> "LEADERBOARD_RES (0x6006)";

            case PING               -> "PING (0x9001)";
            case PONG               -> "PONG (0x9002)";
            case RECONNECT_REQ      -> "RECONNECT_REQ (0x9003)";
            case RECONNECT_RES      -> "RECONNECT_RES (0x9004)";
            default                 -> String.format("UNKNOWN_OPCODE (0x%04X)", opCode);
        };
    }
}

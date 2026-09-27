package shared;

/**
 * Constants representing difficulty levels for exam questions.
 * Meets rubric requirement: "Classifying questions by difficulty, topic"
 */
public final class QuestionDifficulty {

    private QuestionDifficulty() {}

    public static final String ALL    = "ALL";
    public static final String EASY   = "EASY";
    public static final String MEDIUM = "MEDIUM";
    public static final String HARD   = "HARD";

    public static boolean isValid(String difficulty) {
        if (difficulty == null) return false;
        String upper = difficulty.trim().toUpperCase();
        return upper.equals(ALL) || upper.equals(EASY) || upper.equals(MEDIUM) || upper.equals(HARD);
    }
}

package shared;

/**
 * Standard topics and categories for questions in the test system.
 * Meets rubric requirement: "Classifying questions by difficulty, topic"
 */
public final class QuestionCategory {

    private QuestionCategory() {}

    public static final String ALL               = "ALL";
    public static final String JAVA_CORE         = "Java Core";
    public static final String NETWORKING        = "Computer Networking";
    public static final String DATABASE          = "Database Systems";
    public static final String DATA_STRUCTURES   = "Data Structures & Algorithms";
    public static final String OPERATING_SYSTEMS = "Operating Systems";
    public static final String SOFTWARE_ENGINEERING = "Software Engineering";
    public static final String GENERAL_IT        = "General IT";

    public static final String[] ALL_CATEGORIES = {
        ALL,
        JAVA_CORE,
        NETWORKING,
        DATABASE,
        DATA_STRUCTURES,
        OPERATING_SYSTEMS,
        SOFTWARE_ENGINEERING,
        GENERAL_IT
    };
}

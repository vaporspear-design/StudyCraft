package io.github.dennisochulor.flashcards.study.question;

public enum QuestionType {

    TEXT("text", "Typed Answer"),
    NUMERIC("numeric", "Numeric Answer"),
    SELF_MARK("self_mark", "Self Mark");

    private final String id;
    private final String displayName;

    QuestionType(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }
}
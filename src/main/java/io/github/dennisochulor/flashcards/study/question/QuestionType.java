package io.github.dennisochulor.flashcards.study.question;

import java.util.Arrays;
import java.util.Optional;

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

    public static Optional<QuestionType> fromId(String id) {
        return Arrays.stream(values())
                .filter(type -> type.id.equalsIgnoreCase(id))
                .findFirst();
    }
}
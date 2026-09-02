package io.github.dennisochulor.flashcards.study;

import java.util.Arrays;
import java.util.Optional;

public enum Subject {

    ENGLISH("english", "English"),
    IRISH("irish", "Irish"),
    MATHS("maths", "Maths"),
    MUSIC("music", "Music"),
    ART_HISTORY("art_history", "Art History"),
    PHYSICS("physics", "Physics"),
    APPLIED_MATHS("applied_maths", "Applied Maths");

    private final String id;
    private final String displayName;

    Subject(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public static Optional<Subject> fromId(String id) {
        return Arrays.stream(values())
                .filter(subject -> subject.id.equalsIgnoreCase(id))
                .findFirst();
    }
}
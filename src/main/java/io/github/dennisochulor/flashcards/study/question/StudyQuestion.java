package io.github.dennisochulor.flashcards.study.question;

import io.github.dennisochulor.flashcards.study.Subject;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record StudyQuestion(
        String id,
        Subject subject,
        String topic,
        QuestionType type,
        String question,
        @Nullable String imageName,
        String answer,
        List<String> alternativeAnswers,
        double numericTolerance,
        int difficulty
) {

    public StudyQuestion {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Question ID cannot be blank.");
        }

        if (subject == null) {
            throw new IllegalArgumentException("Subject cannot be null.");
        }

        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("Topic cannot be blank.");
        }

        if (type == null) {
            throw new IllegalArgumentException("Question type cannot be null.");
        }

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question text cannot be blank.");
        }

        if (answer == null || answer.isBlank()) {
            throw new IllegalArgumentException("Answer cannot be blank.");
        }

        if (alternativeAnswers == null) {
            alternativeAnswers = List.of();
        } else {
            alternativeAnswers = List.copyOf(alternativeAnswers);
        }

        if (numericTolerance < 0) {
            throw new IllegalArgumentException(
                    "Numeric tolerance cannot be negative."
            );
        }

        if (difficulty < 1 || difficulty > 5) {
            throw new IllegalArgumentException(
                    "Difficulty must be between 1 and 5."
            );
        }
    }

    public StudyQuestion(
            String id,
            Subject subject,
            String topic,
            QuestionType type,
            String question,
            String answer
    ) {
        this(
                id,
                subject,
                topic,
                type,
                question,
                null,
                answer,
                List.of(),
                0.0,
                3
        );
    }
}
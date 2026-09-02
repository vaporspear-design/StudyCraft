package io.github.dennisochulor.flashcards.study.progress;

public record QuestionProgress(
        String questionId,
        int timesAsked,
        int timesCorrect,
        int timesWrong,
        int consecutiveCorrect,
        int mastery,
        long lastAskedEpochMillis,
        long nextReviewEpochMillis
) {

    public QuestionProgress {

        if (questionId == null || questionId.isBlank()) {
            throw new IllegalArgumentException(
                    "Question ID cannot be blank."
            );
        }

        if (timesAsked < 0) {
            throw new IllegalArgumentException(
                    "Times asked cannot be negative."
            );
        }

        if (timesCorrect < 0) {
            throw new IllegalArgumentException(
                    "Times correct cannot be negative."
            );
        }

        if (timesWrong < 0) {
            throw new IllegalArgumentException(
                    "Times wrong cannot be negative."
            );
        }

        if (consecutiveCorrect < 0) {
            throw new IllegalArgumentException(
                    "Consecutive correct cannot be negative."
            );
        }

        if (mastery < 0 || mastery > 5) {
            throw new IllegalArgumentException(
                    "Mastery must be between 0 and 5."
            );
        }

        if (lastAskedEpochMillis < 0) {
            throw new IllegalArgumentException(
                    "Last asked time cannot be negative."
            );
        }

        if (nextReviewEpochMillis < 0) {
            throw new IllegalArgumentException(
                    "Next review time cannot be negative."
            );
        }

        if (timesCorrect + timesWrong > timesAsked) {
            throw new IllegalArgumentException(
                    "Correct and wrong answers cannot exceed times asked."
            );
        }
    }

    public static QuestionProgress newQuestion(String questionId) {
        return new QuestionProgress(
                questionId,
                0,
                0,
                0,
                0,
                0,
                0L,
                0L
        );
    }

    public double accuracy() {
        if (timesAsked == 0) {
            return 0.0;
        }

        return (double) timesCorrect / timesAsked;
    }

    public boolean hasBeenAsked() {
        return timesAsked > 0;
    }

    public boolean isDue(long currentTimeMillis) {
        return nextReviewEpochMillis == 0
                || currentTimeMillis >= nextReviewEpochMillis;
    }
}
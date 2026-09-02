package io.github.dennisochulor.flashcards.study.progress;

import java.time.Duration;

public final class ProgressUpdater {

    private static final Duration WRONG_REVIEW_DELAY =
            Duration.ofMinutes(10);

    private ProgressUpdater() {
    }

    public static QuestionProgress recordCorrect(
            QuestionProgress current,
            long currentTimeMillis
    ) {

        validateInput(current, currentTimeMillis);

        int newMastery = Math.min(
                5,
                current.mastery() + 1
        );

        int newConsecutiveCorrect =
                current.consecutiveCorrect() + 1;

        long nextReviewTime =
                currentTimeMillis
                        + reviewDelayForMastery(newMastery).toMillis();

        return new QuestionProgress(
                current.questionId(),
                current.timesAsked() + 1,
                current.timesCorrect() + 1,
                current.timesWrong(),
                newConsecutiveCorrect,
                newMastery,
                currentTimeMillis,
                nextReviewTime
        );
    }

    public static QuestionProgress recordWrong(
            QuestionProgress current,
            long currentTimeMillis
    ) {

        validateInput(current, currentTimeMillis);

        int newMastery = Math.max(
                0,
                current.mastery() - 1
        );

        long nextReviewTime =
                currentTimeMillis
                        + WRONG_REVIEW_DELAY.toMillis();

        return new QuestionProgress(
                current.questionId(),
                current.timesAsked() + 1,
                current.timesCorrect(),
                current.timesWrong() + 1,
                0,
                newMastery,
                currentTimeMillis,
                nextReviewTime
        );
    }

    private static Duration reviewDelayForMastery(int mastery) {
        return switch (mastery) {
            case 0 -> Duration.ofMinutes(10);
            case 1 -> Duration.ofMinutes(30);
            case 2 -> Duration.ofHours(4);
            case 3 -> Duration.ofDays(1);
            case 4 -> Duration.ofDays(3);
            case 5 -> Duration.ofDays(14);

            default -> throw new IllegalArgumentException(
                    "Mastery must be between 0 and 5."
            );
        };
    }

    private static void validateInput(
            QuestionProgress current,
            long currentTimeMillis
    ) {

        if (current == null) {
            throw new IllegalArgumentException(
                    "Question progress cannot be null."
            );
        }

        if (currentTimeMillis < 0) {
            throw new IllegalArgumentException(
                    "Current time cannot be negative."
            );
        }
    }
}
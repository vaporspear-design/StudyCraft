package io.github.dennisochulor.flashcards.study.scheduler;

import io.github.dennisochulor.flashcards.study.progress.ProgressManager;
import io.github.dennisochulor.flashcards.study.progress.QuestionProgress;
import io.github.dennisochulor.flashcards.study.question.StudyQuestion;
import io.github.dennisochulor.flashcards.study.repository.StudyQuestionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class StudyScheduler {

    private StudyScheduler() {
    }

    public static StudyQuestion nextQuestion() {

        return nextQuestion(
                System.currentTimeMillis()
        );
    }

    public static StudyQuestion nextQuestion(
            long currentTimeMillis
    ) {

        List<StudyQuestion> questions =
                StudyQuestionRepository.getAll();

        if (questions.isEmpty()) {
            return null;
        }

        List<WeightedQuestion> candidates =
                new ArrayList<>();

        boolean hasDueOrUnseen =
                false;

        for (StudyQuestion question :
                questions) {

            QuestionProgress progress =
                    ProgressManager.get(
                            question.id()
                    );

            if (progress == null
                    || progress.isDue(
                    currentTimeMillis
            )) {

                hasDueOrUnseen =
                        true;

                break;
            }
        }

        for (StudyQuestion question :
                questions) {

            QuestionProgress progress =
                    ProgressManager.get(
                            question.id()
                    );

            boolean dueOrUnseen =
                    progress == null
                            || progress.isDue(
                            currentTimeMillis
                    );

            /*
             * If anything is actually due,
             * don't waste time on cards that
             * aren't due yet.
             */
            if (hasDueOrUnseen
                    && !dueOrUnseen) {

                continue;
            }

            int weight =
                    calculateWeight(
                            progress,
                            currentTimeMillis
                    );

            candidates.add(
                    new WeightedQuestion(
                            question,
                            weight
                    )
            );
        }

        if (candidates.isEmpty()) {

            return questions.get(
                    ThreadLocalRandom.current()
                            .nextInt(
                                    questions.size()
                            )
            );
        }

        return weightedRandom(
                candidates
        );
    }

    private static int calculateWeight(
            QuestionProgress progress,
            long currentTimeMillis
    ) {

        /*
         * Brand-new questions should appear
         * fairly often.
         */
        if (progress == null) {
            return 50;
        }

        int weight =
                10;

        /*
         * Low mastery gets a strong priority.
         */
        weight +=
                (5 - progress.mastery())
                        * 15;

        /*
         * Questions you previously got wrong
         * gain some extra weight.
         */
        weight +=
                Math.min(
                        progress.timesWrong()
                                * 4,
                        40
                );

        /*
         * Due questions gain another boost.
         */
        if (progress.isDue(
                currentTimeMillis
        )) {

            weight += 40;
        }

        /*
         * Repeated correct answers reduce
         * frequency slightly.
         */
        weight -=
                Math.min(
                        progress.consecutiveCorrect()
                                * 3,
                        15
                );

        return Math.max(
                1,
                weight
        );
    }

    private static StudyQuestion weightedRandom(
            List<WeightedQuestion> candidates
    ) {

        int totalWeight =
                candidates.stream()
                        .mapToInt(
                                WeightedQuestion::weight
                        )
                        .sum();

        int selected =
                ThreadLocalRandom.current()
                        .nextInt(
                                totalWeight
                        );

        int running =
                0;

        for (WeightedQuestion candidate :
                candidates) {

            running +=
                    candidate.weight();

            if (selected < running) {

                return candidate.question();
            }
        }

        return candidates.getLast()
                .question();
    }

    private record WeightedQuestion(
            StudyQuestion question,
            int weight
    ) {
    }
}
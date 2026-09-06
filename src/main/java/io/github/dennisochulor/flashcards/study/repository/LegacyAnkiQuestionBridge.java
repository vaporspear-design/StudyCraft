package io.github.dennisochulor.flashcards.study.repository;

import io.github.dennisochulor.flashcards.FileManager;
import io.github.dennisochulor.flashcards.Flashcards;
import io.github.dennisochulor.flashcards.questions.Question;
import io.github.dennisochulor.flashcards.study.Subject;
import io.github.dennisochulor.flashcards.study.question.QuestionType;
import io.github.dennisochulor.flashcards.study.question.StudyQuestion;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class LegacyAnkiQuestionBridge {

    private LegacyAnkiQuestionBridge() {
    }

    public static List<StudyQuestion>
    loadIrishVocabularyQuestions() {

        Map<String, List<Question>> legacyCategories =
                FileManager.getQuestions();

        List<StudyQuestion> result =
                new ArrayList<>();

        for (
                Map.Entry<String, List<Question>> entry
                : legacyCategories.entrySet()
        ) {

            String category =
                    entry.getKey();

            if (!isIrishAnkiCategory(category)) {
                continue;
            }

            for (Question legacyQuestion :
                    entry.getValue()) {

                result.add(
                        convertIrishVocabularyQuestion(
                                legacyQuestion
                        )
                );
            }
        }

        Flashcards.LOGGER.info(
                "Bridged {} Irish Anki vocabulary questions into StudyCraft.",
                result.size()
        );

        return List.copyOf(result);
    }

    private static boolean isIrishAnkiCategory(
            String category
    ) {

        String normalized =
                category
                        .toLowerCase(Locale.ROOT)
                        .replace('_', ' ')
                        .replace('-', ' ');

        boolean isAnki =
                normalized.contains("anki");

        boolean isIrish =
                normalized.contains("irish")
                        || normalized.contains(
                        "gaeilge"
                );

        return isAnki && isIrish;
    }

    private static StudyQuestion
    convertIrishVocabularyQuestion(
            Question legacyQuestion
    ) {

        String questionId =
                "anki_irish_vocab_"
                        + stableHash(
                        legacyQuestion.question()
                                + "\u0000"
                                + legacyQuestion.answer()
                );

        return new StudyQuestion(
                questionId,
                Subject.IRISH,
                "Vocabulary",
                QuestionType.TEXT,
                legacyQuestion.question(),
                legacyQuestion.imageName(),
                legacyQuestion.answer(),
                List.of(),
                0.0,
                1
        );
    }

    private static String stableHash(
            String value
    ) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            value.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            /*
             * First 8 bytes = 16 hexadecimal characters.
             * More than sufficient for stable question IDs.
             */
            return HexFormat.of()
                    .formatHex(
                            hash,
                            0,
                            8
                    );

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "SHA-256 is unavailable.",
                    e
            );
        }
    }
}
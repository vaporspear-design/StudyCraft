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
import java.util.Optional;

public final class LegacyAnkiQuestionBridge {

    private LegacyAnkiQuestionBridge() {
    }

    public static List<StudyQuestion> loadQuestions() {

        Map<String, List<Question>> categories =
                FileManager.getQuestions();

        List<StudyQuestion> result =
                new ArrayList<>();

        for (Map.Entry<String, List<Question>> entry
                : categories.entrySet()) {

            String category =
                    entry.getKey();

            Optional<CategoryInfo> categoryInfo =
                    parseCategory(category);

            if (categoryInfo.isEmpty()) {
                continue;
            }

            CategoryInfo info =
                    categoryInfo.get();

            for (Question legacyQuestion
                    : entry.getValue()) {

                result.add(
                        convertQuestion(
                                legacyQuestion,
                                info
                        )
                );
            }
        }

        Flashcards.LOGGER.info(
                "Bridged {} Anki questions into StudyCraft.",
                result.size()
        );

        return List.copyOf(result);
    }

    private static Optional<CategoryInfo> parseCategory(
            String category
    ) {

        String lower =
                category.toLowerCase(Locale.ROOT);

        if (!lower.startsWith("anki")) {
            return Optional.empty();
        }

        String remainder =
                category.substring(4)
                        .replaceFirst(
                                "^[-_ ]+",
                                ""
                        );

        if (remainder.isBlank()) {
            return Optional.empty();
        }

        String[] parts =
                remainder.split(
                        "[-_]",
                        2
                );

        String subjectToken =
                normalizeToken(
                        parts[0]
                );

        Optional<Subject> subject =
                subjectFromToken(
                        subjectToken
                );

        if (subject.isEmpty()) {

            Flashcards.LOGGER.warn(
                    "Could not determine StudyCraft subject from Anki category '{}'.",
                    category
            );

            return Optional.empty();
        }

        String topic =
                parts.length >= 2
                        ? makeDisplayTopic(parts[1])
                        : "General";

        return Optional.of(
                new CategoryInfo(
                        subject.get(),
                        topic
                )
        );
    }

    private static Optional<Subject> subjectFromToken(
            String token
    ) {

        return switch (token) {

            case "english" ->
                    Optional.of(
                            Subject.ENGLISH
                    );

            case "irish",
                 "gaeilge" ->
                    Optional.of(
                            Subject.IRISH
                    );

            case "maths",
                 "math",
                 "mathematics" ->
                    Optional.of(
                            Subject.MATHS
                    );

            case "music" ->
                    Optional.of(
                            Subject.MUSIC
                    );

            case "art",
                 "arthistory" ->
                    Optional.of(
                            Subject.ART_HISTORY
                    );

            case "physics" ->
                    Optional.of(
                            Subject.PHYSICS
                    );

            case "appliedmaths",
                 "appliedmath",
                 "appliedmathematics" ->
                    Optional.of(
                            Subject.APPLIED_MATHS
                    );

            default ->
                    Optional.empty();
        };
    }

    private static StudyQuestion convertQuestion(
            Question legacyQuestion,
            CategoryInfo info
    ) {

        String idSource =
                info.subject().id()
                        + "\u0000"
                        + info.topic()
                        + "\u0000"
                        + legacyQuestion.question()
                        + "\u0000"
                        + legacyQuestion.answer();

        String questionId =
                "anki_"
                        + info.subject().id()
                        + "_"
                        + stableHash(
                        idSource
                );

        return new StudyQuestion(
                questionId,
                info.subject(),
                info.topic(),
                QuestionType.TEXT,
                legacyQuestion.question(),
                legacyQuestion.imageName(),
                legacyQuestion.answer(),
                List.of(),
                0.0,
                1
        );
    }

    private static String normalizeToken(
            String value
    ) {

        return value
                .toLowerCase(Locale.ROOT)
                .replaceAll(
                        "[^a-z]",
                        ""
                );
    }

    private static String makeDisplayTopic(
            String value
    ) {

        String cleaned =
                value
                        .replace('_', ' ')
                        .replace('-', ' ')
                        .trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        );

        if (cleaned.isBlank()) {
            return "General";
        }

        StringBuilder result =
                new StringBuilder();

        boolean capitalizeNext =
                true;

        for (char c :
                cleaned.toCharArray()) {

            if (Character.isWhitespace(c)) {

                result.append(c);

                capitalizeNext =
                        true;

            } else if (capitalizeNext) {

                result.append(
                        Character.toUpperCase(c)
                );

                capitalizeNext =
                        false;

            } else {

                result.append(c);
            }
        }

        return result.toString();
    }

    private static String stableHash(
            String value
    ) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] bytes =
                    digest.digest(
                            value.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return HexFormat.of()
                    .formatHex(
                            bytes,
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

    private record CategoryInfo(
            Subject subject,
            String topic
    ) {
    }
}
package io.github.dennisochulor.flashcards.study.question;

import java.text.Normalizer;
import java.util.Locale;

public final class AnswerValidator {

    private static final double FLOATING_POINT_EPSILON = 0.000000001;

    private AnswerValidator() {
    }

    public static boolean isCorrect(
            StudyQuestion question,
            String userAnswer
    ) {

        if (question == null) {
            throw new IllegalArgumentException(
                    "Question cannot be null."
            );
        }

        if (userAnswer == null) {
            return false;
        }

        return switch (question.type()) {
            case TEXT ->
                    validateText(question, userAnswer);

            case NUMERIC ->
                    validateNumeric(question, userAnswer);

            case SELF_MARK ->
                    throw new IllegalArgumentException(
                            "Self-mark questions cannot be automatically validated."
                    );
        };
    }

    public static boolean requiresManualMarking(
            StudyQuestion question
    ) {

        if (question == null) {
            throw new IllegalArgumentException(
                    "Question cannot be null."
            );
        }

        return question.type() == QuestionType.SELF_MARK;
    }

    private static boolean validateText(
            StudyQuestion question,
            String userAnswer
    ) {

        if (matchesText(
                userAnswer,
                question.answer()
        )) {
            return true;
        }

        for (String alternative :
                question.alternativeAnswers()) {

            if (matchesText(
                    userAnswer,
                    alternative
            )) {
                return true;
            }
        }

        return false;
    }

    private static boolean matchesText(
            String userAnswer,
            String expectedAnswer
    ) {

        String normalizedUser =
                normalizeText(userAnswer);

        String normalizedExpected =
                normalizeText(expectedAnswer);

        if (normalizedUser.equals(normalizedExpected)) {
            return true;
        }

        if (looksFormulaLike(normalizedUser)
                || looksFormulaLike(normalizedExpected)) {

            return removeWhitespace(normalizedUser)
                    .equals(
                            removeWhitespace(normalizedExpected)
                    );
        }

        return false;
    }

    private static boolean validateNumeric(
            StudyQuestion question,
            String userAnswer
    ) {

        Double userValue =
                parseNumber(userAnswer);

        if (userValue == null) {
            return false;
        }

        if (matchesNumericAnswer(
                userValue,
                question.answer(),
                question.numericTolerance()
        )) {
            return true;
        }

        for (String alternative :
                question.alternativeAnswers()) {

            if (matchesNumericAnswer(
                    userValue,
                    alternative,
                    question.numericTolerance()
            )) {
                return true;
            }
        }

        return false;
    }

    private static boolean matchesNumericAnswer(
            double userValue,
            String expectedAnswer,
            double tolerance
    ) {

        Double expectedValue =
                parseNumber(expectedAnswer);

        if (expectedValue == null) {
            return false;
        }

        double difference =
                Math.abs(userValue - expectedValue);

        return difference
                <= tolerance + FLOATING_POINT_EPSILON;
    }

    private static String normalizeText(String value) {

        String normalized =
                Normalizer.normalize(
                        value,
                        Normalizer.Form.NFKC
                );

        normalized = normalized
                .replace('\u2018', '\'')
                .replace('\u2019', '\'')
                .replace('\u201C', '"')
                .replace('\u201D', '"')
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");

        normalized =
                normalized.replaceAll("[.!?]+$", "");

        return normalized.trim();
    }

    private static String removeWhitespace(String value) {
        return value.replaceAll("\\s+", "");
    }

    private static boolean looksFormulaLike(String value) {

        if (value.chars().anyMatch(Character::isDigit)) {
            return true;
        }

        return value.matches(
                ".*[=+\\-*/^()].*"
        );
    }

    private static Double parseNumber(String value) {

        if (value == null) {
            return null;
        }

        String normalized =
                Normalizer.normalize(
                                value,
                                Normalizer.Form.NFKC
                        )
                        .trim()
                        .replace('\u2212', '-')
                        .replaceAll("\\s+", "");

        if (normalized.contains(",")
                && !normalized.contains(".")) {

            normalized =
                    normalized.replace(',', '.');
        }

        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            return null;
        }
    }


}
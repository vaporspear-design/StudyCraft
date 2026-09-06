package io.github.dennisochulor.flashcards.study.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.dennisochulor.flashcards.Flashcards;
import io.github.dennisochulor.flashcards.study.Subject;
import io.github.dennisochulor.flashcards.study.question.QuestionType;
import io.github.dennisochulor.flashcards.study.question.StudyQuestion;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class StudyQuestionRepository {

    private static final Gson GSON =
            new GsonBuilder().create();

    private static final Map<String, StudyQuestion> questionsById =
            new LinkedHashMap<>();

    private static Path questionsDirectory;

    private StudyQuestionRepository() {
    }

    public static void init() {
        Path gameDirectory =
                Minecraft.getInstance().gameDirectory.toPath();

        questionsDirectory =
                gameDirectory
                        .resolve("config")
                        .resolve("flashcards")
                        .resolve("study-questions");

        try {
            Files.createDirectories(questionsDirectory);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Failed to create StudyCraft question directory.",
                    e
            );
        }

        reload();

        Flashcards.LOGGER.info(
                "Loaded {} StudyCraft questions.",
                questionsById.size()
        );
    }

    public static void reload() {

        ensureInitialised();

        questionsById.clear();

        /*
         * Native StudyCraft JSON.
         */
        try (Stream<Path> files =
                     Files.list(questionsDirectory)) {

            List<Path> questionFiles =
                    files.filter(Files::isRegularFile)
                            .filter(path ->
                                    path.getFileName()
                                            .toString()
                                            .toLowerCase()
                                            .endsWith(".json")
                            )
                            .sorted()
                            .toList();

            for (Path file : questionFiles) {
                loadFile(file);
            }

        } catch (IOException e) {

            throw new UncheckedIOException(
                    "Failed to load StudyCraft questions.",
                    e
            );
        }

        /*
         * Imported Anki Irish vocabulary.
         */
        List<StudyQuestion> bridgedQuestions =
                LegacyAnkiQuestionBridge
                        .loadQuestions();

        for (StudyQuestion question :
                bridgedQuestions) {

            StudyQuestion previous =
                    questionsById.putIfAbsent(
                            question.id(),
                            question
                    );

            if (previous != null) {

                throw new IllegalArgumentException(
                        "Duplicate StudyCraft question ID: "
                                + question.id()
                );
            }
        }
    }

    public static StudyQuestion getById(String questionId) {
        ensureInitialised();
        return questionsById.get(questionId);
    }

    public static List<StudyQuestion> getAll() {
        ensureInitialised();

        return List.copyOf(
                new ArrayList<>(questionsById.values())
        );
    }

    public static int size() {
        ensureInitialised();
        return questionsById.size();
    }

    private static void loadFile(Path file) {

        try {
            String json = Files.readString(
                    file,
                    StandardCharsets.UTF_8
            );

            StudyQuestionData[] entries =
                    GSON.fromJson(
                            json,
                            StudyQuestionData[].class
                    );

            if (entries == null) {
                return;
            }

            for (StudyQuestionData entry : entries) {

                StudyQuestion question =
                        convert(entry, file);

                StudyQuestion previous =
                        questionsById.putIfAbsent(
                                question.id(),
                                question
                        );

                if (previous != null) {
                    throw new IllegalArgumentException(
                            "Duplicate StudyCraft question ID: "
                                    + question.id()
                    );
                }
            }

        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Failed to read question file: " + file,
                    e
            );
        }
    }

    private static StudyQuestion convert(
            StudyQuestionData data,
            Path sourceFile
    ) {

        Subject subject =
                Subject.fromId(data.subject())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Unknown subject '"
                                                + data.subject()
                                                + "' in "
                                                + sourceFile.getFileName()
                                )
                        );

        QuestionType type =
                QuestionType.fromId(data.type())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Unknown question type '"
                                                + data.type()
                                                + "' in "
                                                + sourceFile.getFileName()
                                )
                        );

        return new StudyQuestion(
                data.id(),
                subject,
                data.topic(),
                type,
                data.question(),
                data.imageName(),
                data.answer(),
                data.alternativeAnswers(),
                data.numericTolerance(),
                data.difficulty()
        );
    }

    private static void ensureInitialised() {
        if (questionsDirectory == null) {
            throw new IllegalStateException(
                    "StudyQuestionRepository has not been initialised."
            );
        }
    }

    private record StudyQuestionData(
            String id,
            String subject,
            String topic,
            String type,
            String question,
            @Nullable String imageName,
            String answer,
            List<String> alternativeAnswers,
            double numericTolerance,
            int difficulty
    ) {
    }
}
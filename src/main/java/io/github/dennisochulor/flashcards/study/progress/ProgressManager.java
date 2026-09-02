package io.github.dennisochulor.flashcards.study.progress;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public final class ProgressManager {

    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    private static final Type PROGRESS_MAP_TYPE =
            new TypeToken<Map<String, QuestionProgress>>() {
            }.getType();

    private static final Map<String, QuestionProgress> progress =
            new HashMap<>();

    private static Path progressFile;

    private ProgressManager() {
    }

    public static void init() {
        Path gameDirectory =
                Minecraft.getInstance().gameDirectory.toPath();

        Path configDirectory =
                gameDirectory.resolve("config")
                        .resolve("flashcards");

        progressFile =
                configDirectory.resolve("study-progress.json");

        try {
            Files.createDirectories(configDirectory);

            if (Files.notExists(progressFile)) {
                save();
            } else {
                load();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Failed to initialise StudyCraft progress.",
                    e
            );
        }
    }

    public static QuestionProgress getOrCreate(String questionId) {
        ensureInitialised();

        return progress.computeIfAbsent(
                questionId,
                QuestionProgress::newQuestion
        );
    }

    public static QuestionProgress get(String questionId) {
        ensureInitialised();
        return progress.get(questionId);
    }

    public static Map<String, QuestionProgress> getAll() {
        ensureInitialised();

        return Collections.unmodifiableMap(
                new HashMap<>(progress)
        );
    }

    public static QuestionProgress recordCorrect(
            String questionId,
            long currentTimeMillis
    ) {
        ensureInitialised();

        QuestionProgress current =
                getOrCreate(questionId);

        QuestionProgress updated =
                ProgressUpdater.recordCorrect(
                        current,
                        currentTimeMillis
                );

        progress.put(questionId, updated);
        save();

        return updated;
    }

    public static QuestionProgress recordWrong(
            String questionId,
            long currentTimeMillis
    ) {
        ensureInitialised();

        QuestionProgress current =
                getOrCreate(questionId);

        QuestionProgress updated =
                ProgressUpdater.recordWrong(
                        current,
                        currentTimeMillis
                );

        progress.put(questionId, updated);
        save();

        return updated;
    }

    private static void load() {
        try {
            String json = Files.readString(
                    progressFile,
                    StandardCharsets.UTF_8
            );

            Map<String, QuestionProgress> loaded =
                    GSON.fromJson(json, PROGRESS_MAP_TYPE);

            progress.clear();

            if (loaded != null) {
                progress.putAll(loaded);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Failed to load StudyCraft progress.",
                    e
            );
        }
    }

    private static void save() {
        ensureProgressFilePathExists();

        Map<String, QuestionProgress> sortedProgress =
                new TreeMap<>(progress);

        String json =
                GSON.toJson(
                        sortedProgress,
                        PROGRESS_MAP_TYPE
                );

        try {
            Files.writeString(
                    progressFile,
                    json,
                    StandardCharsets.UTF_8
            );
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Failed to save StudyCraft progress.",
                    e
            );
        }
    }

    private static void ensureInitialised() {
        if (progressFile == null) {
            throw new IllegalStateException(
                    "ProgressManager has not been initialised."
            );
        }
    }

    private static void ensureProgressFilePathExists() {
        if (progressFile == null) {
            throw new IllegalStateException(
                    "ProgressManager has not been initialised."
            );
        }
    }
}
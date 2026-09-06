package io.github.dennisochulor.flashcards;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import io.github.dennisochulor.flashcards.config.ModConfig;
import io.github.dennisochulor.flashcards.questions.Question;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class FileManager {

    private FileManager() {
    }

    public static final ImageUtils.ImagePackage ADD_IMAGE_IMAGE;

    private static final Path dotMinecraftFolder;
    private static final File configFile;
    private static final File statsFile;
    private static final File questionsFolder;
    private static final File mediaFolder;

    private static ModConfig config;

    static {
        try {

            dotMinecraftFolder =
                    Minecraft.getInstance()
                            .gameDirectory
                            .toPath();

            configFile =
                    new File(
                            dotMinecraftFolder
                                    + "/config/flashcards/config.json"
                    );

            statsFile =
                    new File(
                            dotMinecraftFolder
                                    + "/config/flashcards/stats.json"
                    );

            questionsFolder =
                    new File(
                            dotMinecraftFolder
                                    + "/config/flashcards/questions/"
                    );

            mediaFolder =
                    new File(
                            dotMinecraftFolder
                                    + "/config/flashcards/media/"
                    );

            if (!questionsFolder.exists()) {

                //noinspection ResultOfMethodCallIgnored
                questionsFolder.mkdirs();

                Files.copy(
                        Objects.requireNonNull(
                                FileManager.class.getResourceAsStream(
                                        "/flashcards/config.json"
                                )
                        ),
                        Path.of(
                                dotMinecraftFolder
                                        + "/config/flashcards/config.json"
                        )
                );

                Files.copy(
                        Objects.requireNonNull(
                                FileManager.class.getResourceAsStream(
                                        "/flashcards/questions/default.json"
                                )
                        ),
                        Path.of(
                                questionsFolder
                                        + "/default.json"
                        )
                );
            }

            if (!statsFile.exists()) {

                Files.copy(
                        Objects.requireNonNull(
                                FileManager.class.getResourceAsStream(
                                        "/flashcards/stats.json"
                                )
                        ),
                        Path.of(
                                dotMinecraftFolder
                                        + "/config/flashcards/stats.json"
                        )
                );
            }

            if (!mediaFolder.exists()) {

                //noinspection ResultOfMethodCallIgnored
                mediaFolder.mkdirs();
            }

            Files.copy(
                    Objects.requireNonNull(
                            FileManager.class.getResourceAsStream(
                                    "/flashcards/flashcards-dp.zip"
                            )
                    ),
                    Path.of(
                            dotMinecraftFolder
                                    + "/config/flashcards/flashcards-dp.zip"
                    ),
                    StandardCopyOption.REPLACE_EXISTING
            );

            config =
                    new Gson().fromJson(
                            Files.readString(
                                    configFile.toPath()
                            ),
                            ModConfig.class
                    );

            importQuestions();

            ADD_IMAGE_IMAGE =
                    ImageUtils.getImagePackageFromAssets(
                            Objects.requireNonNull(
                                    FileManager.class.getResourceAsStream(
                                            "/assets/flashcards/media/add_image.png"
                                    )
                            ),
                            "add_image"
                    );

        } catch (IOException e) {

            throw new UncheckedIOException(e);
        }
    }

    public static void init() {
        // Run static initializer.
    }

    public static ModConfig getConfig() {
        return config;
    }

    public static void updateConfig(ModConfig config) {

        try {

            Gson gson =
                    new GsonBuilder()
                            .setPrettyPrinting()
                            .create();

            FileWriter writer =
                    new FileWriter(configFile);

            gson.toJson(
                    config,
                    ModConfig.class,
                    writer
            );

            writer.flush();
            writer.close();

            FileManager.config = config;

        } catch (IOException e) {

            throw new UncheckedIOException(e);
        }
    }

    public static Map<String, List<Question>> getQuestions() {

        Gson gson =
                new GsonBuilder()
                        .create();

        Map<String, List<Question>> map =
                new TreeMap<>();

        Arrays.stream(
                        Objects.requireNonNull(
                                questionsFolder.listFiles()
                        )
                )
                .filter(
                        f -> f.getName().endsWith(".json")
                )
                .forEachOrdered(f -> {

                    Question[] questions;

                    try {

                        questions =
                                gson.fromJson(
                                        Files.readString(
                                                f.toPath()
                                        ),
                                        Question[].class
                                );

                    } catch (IOException e) {

                        throw new RuntimeException(e);
                    }

                    map.put(
                            f.getName().split("\\.")[0],
                            new ArrayList<>(
                                    List.of(questions)
                            )
                    );
                });

        return map;
    }

    public static void updateQuestions(
            Map<String, List<Question>> map
    ) {

        Gson gson =
                new GsonBuilder()
                        .setPrettyPrinting()
                        .create();

        try {

            //noinspection ResultOfMethodCallIgnored
            Arrays.stream(
                            Objects.requireNonNull(
                                    questionsFolder.listFiles()
                            )
                    )
                    .filter(
                            f -> f.getName().endsWith(".json")
                    )
                    .forEach(File::delete);

            for (
                    Map.Entry<String, List<Question>> entry
                    : map.entrySet()
            ) {

                String filename =
                        entry.getKey();

                List<Question> questions =
                        entry.getValue();

                FileWriter writer =
                        new FileWriter(
                                questionsFolder.toPath()
                                        + "/"
                                        + filename
                                        + ".json"
                        );

                gson.toJson(
                        questions.toArray(),
                        Question[].class,
                        writer
                );

                writer.flush();
                writer.close();
            }

        } catch (IOException e) {

            throw new UncheckedIOException(e);
        }
    }

    public static ModStats getStats() {

        try {

            return new Gson().fromJson(
                    Files.readString(
                            statsFile.toPath()
                    ),
                    ModStats.class
            );

        } catch (IOException e) {

            throw new UncheckedIOException(e);
        }
    }

    public static void updateStats(
            ModStats stats
    ) {

        try {

            Gson gson =
                    new GsonBuilder()
                            .setPrettyPrinting()
                            .create();

            FileWriter writer =
                    new FileWriter(statsFile);

            gson.toJson(
                    stats,
                    ModStats.class,
                    writer
            );

            writer.flush();
            writer.close();

        } catch (IOException e) {

            throw new UncheckedIOException(e);
        }
    }

    /**
     * @param imageName Name of the image file in the mod's media folder.
     * @return The image file.
     */
    public static File getImageFile(
            String imageName
    ) {

        return new File(
                mediaFolder
                        + "/"
                        + imageName
        );
    }

    /**
     * @return The amended filename if there was a duplicate file with the same
     * name, otherwise the original filename.
     */
    public static String saveImage(
            Path image
    ) {

        try {

            final AtomicReference<String> amendedFilename =
                    new AtomicReference<>(
                            image.getFileName()
                                    .toString()
                    );

            if (!image.toFile().exists()) {

                return amendedFilename.get();
            }

            while (
                    Arrays.stream(
                                    Objects.requireNonNull(
                                            mediaFolder.list()
                                    )
                            )
                            .anyMatch(
                                    s -> s.equalsIgnoreCase(
                                            amendedFilename.get()
                                    )
                            )
            ) {

                int dotIndex =
                        amendedFilename
                                .get()
                                .lastIndexOf('.');

                amendedFilename.set(
                        amendedFilename
                                .get()
                                .substring(
                                        0,
                                        dotIndex
                                )
                                + " (1)."
                                + amendedFilename
                                .get()
                                .substring(
                                        dotIndex + 1
                                )
                );
            }

            Files.copy(
                    image,
                    Path.of(
                            mediaFolder
                                    + "/"
                                    + amendedFilename.get()
                    )
            );

            return amendedFilename.get();

        } catch (IOException e) {

            throw new UncheckedIOException(e);
        }
    }

    private static void importQuestions() {

        File[] files =
                questionsFolder.listFiles();

        if (files == null) {

            Flashcards.LOGGER.warn(
                    "Could not list question files in {}",
                    questionsFolder.getAbsolutePath()
            );

            return;
        }

        Flashcards.LOGGER.info(
                "Looking for question imports in: {}",
                questionsFolder.getAbsolutePath()
        );

        List<File> list =
                Arrays.stream(files)
                        .filter(File::isFile)
                        .filter(
                                f -> !f.getName()
                                        .toLowerCase(Locale.ROOT)
                                        .endsWith(".json")
                        )
                        .toList();

        list.forEach(file ->

                Flashcards.LOGGER.info(
                        "Found import candidate: {}",
                        file.getName()
                )
        );

        importAnki(
                list.stream()
                        .filter(
                                f -> f.getName()
                                        .toLowerCase(Locale.ROOT)
                                        .contains("anki")
                        )
        );

        importCSV(
                list.stream()
                        .filter(
                                f -> f.getName()
                                        .toLowerCase(Locale.ROOT)
                                        .endsWith(".csv")
                        )
        );

        importTSV(
                list.stream()
                        .filter(
                                f -> f.getName()
                                        .toLowerCase(Locale.ROOT)
                                        .endsWith(".tsv")
                        )
        );
    }

    private static void importAnki(
            Stream<File> stream
    ) {

        Pattern imagePattern =
                Pattern.compile(
                        ".*(<img src=\"(.+)\">).*"
                );

        stream.forEach(f -> {

            try {

                Flashcards.LOGGER.info(
                        "Importing Anki file: {}",
                        f.getAbsolutePath()
                );

                List<Question> importedQuestions =
                        new ArrayList<>();

                CSVReader reader =
                        new CSVReaderBuilder(
                                Files.newBufferedReader(
                                        f.toPath(),
                                        StandardCharsets.UTF_8
                                )
                        )
                                .withCSVParser(
                                        new CSVParserBuilder()
                                                .withSeparator('\t')
                                                .build()
                                )
                                .build();

                List<String[]> rows =
                        reader.readAll();

                reader.close();

                for (String[] row : rows) {

                    /*
                     * Ignore malformed / metadata rows.
                     */
                    if (row.length < 2) {
                        continue;
                    }

                    if (
                            row[0] == null
                                    || row[0]
                                    .trim()
                                    .startsWith("#")
                    ) {

                        continue;
                    }

                    String question =
                            row[0]
                                    .replace(
                                            "<br>",
                                            "\n"
                                    )
                                    .trim();

                    String answer =
                            row[1]
                                    .trim();

                    /*
                     * Ignore blank cards.
                     */
                    if (
                            question.isBlank()
                                    || answer.isBlank()
                    ) {

                        continue;
                    }

                    Matcher matcher =
                            imagePattern.matcher(
                                    question
                            );

                    if (matcher.matches()) {

                        question =
                                question.replace(
                                        matcher.group(1),
                                        ""
                                );

                        String imageName =
                                matcher.group(2);

                        importedQuestions.add(
                                new Question(
                                        question,
                                        imageName,
                                        answer
                                )
                        );

                    } else {

                        importedQuestions.add(
                                new Question(
                                        question,
                                        null,
                                        answer
                                )
                        );
                    }
                }

                if (importedQuestions.isEmpty()) {

                    Flashcards.LOGGER.warn(
                            "{} contained no usable Anki questions.",
                            f.getName()
                    );

                    return;
                }

                String originalFilename =
                        f.getName();

                int extensionIndex =
                        originalFilename.lastIndexOf('.');

                String filename =
                        extensionIndex > 0
                                ? originalFilename.substring(
                                0,
                                extensionIndex
                        )
                                : originalFilename;

                filename =
                        filename.replaceAll(
                                "[^A-Za-z0-9_-]",
                                "_"
                        );

                File outputFile =
                        new File(
                                questionsFolder,
                                filename + ".json"
                        );

                Gson gson =
                        new GsonBuilder()
                                .setPrettyPrinting()
                                .create();

                try (
                        FileWriter writer =
                                new FileWriter(
                                        outputFile,
                                        StandardCharsets.UTF_8
                                )
                ) {

                    gson.toJson(
                            importedQuestions.toArray(),
                            Question[].class,
                            writer
                    );
                }

                Flashcards.LOGGER.info(
                        "Successfully imported {} Anki questions into {}",
                        importedQuestions.size(),
                        outputFile.getName()
                );

                if (!f.delete()) {

                    Flashcards.LOGGER.warn(
                            "Imported {}, but could not delete original file.",
                            f.getName()
                    );
                }

            } catch (Exception e) {

                Flashcards.LOGGER.warn(
                        "{} encountered an error during import!",
                        f.getName(),
                        e
                );
            }
        });
    }

    private static void importCSV(
            Stream<File> stream
    ) {

        stream.forEach(f -> {

            try {

                List<Question> importedQuestions =
                        new ArrayList<>();

                CSVReader reader =
                        new CSVReader(
                                new FileReader(f)
                        );

                List<String[]> questions =
                        reader.readAll();

                reader.close();

                questions.forEach(q -> {

                    if (q.length == 2) {

                        importedQuestions.add(
                                new Question(
                                        q[0],
                                        null,
                                        q[1]
                                )
                        );
                    }

                    if (q.length >= 3) {

                        importedQuestions.add(
                                new Question(
                                        q[0],
                                        q[2],
                                        q[1]
                                )
                        );
                    }
                });

                String filename =
                        "csv-"
                                + ThreadLocalRandom.current()
                                .nextInt(
                                        0,
                                        99999
                                );

                FileWriter writer =
                        new FileWriter(
                                questionsFolder.toPath()
                                        + "/"
                                        + filename
                                        + ".json"
                        );

                Gson gson =
                        new GsonBuilder()
                                .setPrettyPrinting()
                                .create();

                gson.toJson(
                        importedQuestions.toArray(),
                        Question[].class,
                        writer
                );

                writer.flush();
                writer.close();

                //noinspection ResultOfMethodCallIgnored
                f.delete();

            } catch (Exception e) {

                Flashcards.LOGGER.warn(
                        "{} encountered an error during import!",
                        f.getName(),
                        e
                );
            }
        });
    }

    private static void importTSV(
            Stream<File> stream
    ) {

        stream.forEach(f -> {

            try {

                List<Question> importedQuestions =
                        new ArrayList<>();

                CSVReader reader =
                        new CSVReaderBuilder(
                                new FileReader(f)
                        )
                                .withCSVParser(
                                        new CSVParserBuilder()
                                                .withSeparator('\t')
                                                .build()
                                )
                                .build();

                List<String[]> questions =
                        reader.readAll();

                reader.close();

                questions.forEach(q -> {

                    if (q.length == 2) {

                        importedQuestions.add(
                                new Question(
                                        q[0],
                                        null,
                                        q[1]
                                )
                        );
                    }

                    if (q.length == 3) {

                        importedQuestions.add(
                                new Question(
                                        q[0],
                                        q[2],
                                        q[1]
                                )
                        );
                    }
                });

                String filename =
                        "tsv-"
                                + ThreadLocalRandom.current()
                                .nextInt(
                                        0,
                                        99999
                                );

                FileWriter writer =
                        new FileWriter(
                                questionsFolder.toPath()
                                        + "/"
                                        + filename
                                        + ".json"
                        );

                Gson gson =
                        new GsonBuilder()
                                .setPrettyPrinting()
                                .create();

                gson.toJson(
                        importedQuestions.toArray(),
                        Question[].class,
                        writer
                );

                writer.flush();
                writer.close();

                //noinspection ResultOfMethodCallIgnored
                f.delete();

            } catch (Exception e) {

                Flashcards.LOGGER.warn(
                        "{} encountered an error during import!",
                        f.getName(),
                        e
                );
            }
        });
    }
}
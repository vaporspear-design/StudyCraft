package io.github.dennisochulor.flashcards;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.dennisochulor.flashcards.config.ConfigurationScreen;
import io.github.dennisochulor.flashcards.questions.QuestionScheduler;
import io.github.dennisochulor.flashcards.study.progress.ProgressManager;
import io.github.dennisochulor.flashcards.study.question.StudyQuestion;
import io.github.dennisochulor.flashcards.study.repository.StudyQuestionRepository;
import io.github.dennisochulor.flashcards.study.scheduler.StudyAutoQuizManager;
import io.github.dennisochulor.flashcards.study.scheduler.StudyScheduler;
import io.github.dennisochulor.flashcards.study.screen.StudyQuestionScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.UIManager;

public class Flashcards implements ClientModInitializer {

    public static final String MOD_ID = "flashcards";

    public static final Logger LOGGER =
            LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {

        /*
         * Needed for the original Flashcards
         * image chooser.
         */
        System.setProperty(
                "java.awt.headless",
                "false"
        );

        try {

            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
            );

        } catch (Exception e) {

            throw new RuntimeException(e);
        }

        LOGGER.info(
                "Initializing flashcards client"
        );

        /*
         * =========================================================
         * ORIGINAL FLASHCARDS CONNECTION EVENTS
         * =========================================================
         */

        ClientPlayConnectionEvents.JOIN.register(
                (_, _, _) ->
                        QuestionScheduler.schedule()
        );

        ClientPlayConnectionEvents.DISCONNECT.register(
                (_, _) ->
                        QuestionScheduler.stop()
        );

        ClientTickEvents.END_CLIENT_TICK.register(
                minecraft -> {

                    if (minecraft.player == null
                            || minecraft.level == null) {

                        return;
                    }

                    if (minecraft.player.hurtTime != 0) {

                        QuestionScheduler.playerLastHurtTime =
                                minecraft.level.getGameTime();
                    }
                }
        );

        /*
         * =========================================================
         * STARTUP
         * =========================================================
         */

        ClientLifecycleEvents.CLIENT_STARTED.register(
                _ -> {

                    /*
                     * Order matters.
                     *
                     * FileManager imports Anki first.
                     * StudyCraft then reads those imported files.
                     */
                    FileManager.init();

                    ProgressManager.init();

                    StudyQuestionRepository.init();

                    QuestionScheduler.reload();

                    LOGGER.info(
                            "StudyCraft initialized successfully."
                    );
                }
        );

        ClientLifecycleEvents.CLIENT_STOPPING.register(
                _ -> QuestionScheduler.close()
        );

        /*
         * =========================================================
         * KEYBIND CATEGORY
         * =========================================================
         */

        KeyMapping.Category keyBindingCategory =
                KeyMapping.Category.register(
                        Identifier.fromNamespaceAndPath(
                                MOD_ID,
                                "main"
                        )
                );

        /*
         * =========================================================
         * H = ORIGINAL CONFIG
         * =========================================================
         */

        KeyMapping keyBindingConfigMenu =
                KeyMappingHelper.registerKeyMapping(
                        new KeyMapping(
                                "Flashcards Config Menu",
                                InputConstants.Type.KEYSYM,
                                GLFW.GLFW_KEY_H,
                                keyBindingCategory
                        )
                );

        /*
         * =========================================================
         * G = ORIGINAL FLASHCARDS QUESTION
         * =========================================================
         */

        KeyMapping keyBindingPromptQuestion =
                KeyMappingHelper.registerKeyMapping(
                        new KeyMapping(
                                "Prompt a question",
                                InputConstants.Type.KEYSYM,
                                GLFW.GLFW_KEY_G,
                                keyBindingCategory
                        )
                );

        /*
         * =========================================================
         * J = STUDYCRAFT QUESTION NOW
         *
         * THIS WAS MISSING FROM YOUR FILE.
         * =========================================================
         */

        KeyMapping keyBindingStudyQuestion =
                KeyMappingHelper.registerKeyMapping(
                        new KeyMapping(
                                "Prompt a StudyCraft question",
                                InputConstants.Type.KEYSYM,
                                GLFW.GLFW_KEY_J,
                                keyBindingCategory
                        )
                );

        /*
         * =========================================================
         * K = TOGGLE AUTOMATIC STUDYCRAFT QUESTIONS
         * =========================================================
         */

        KeyMapping keyBindingToggleStudyCraft =
                KeyMappingHelper.registerKeyMapping(
                        new KeyMapping(
                                "Toggle automatic StudyCraft questions",
                                InputConstants.Type.KEYSYM,
                                GLFW.GLFW_KEY_K,
                                keyBindingCategory
                        )
                );

        /*
         * =========================================================
         * H HANDLER
         * =========================================================
         */

        ClientTickEvents.END_CLIENT_TICK.register(
                minecraft -> {

                    if (!keyBindingConfigMenu.consumeClick()) {
                        return;
                    }

                    /*
                     * Consume additional queued presses.
                     */
                    while (keyBindingConfigMenu.consumeClick()) {
                        // Do nothing.
                    }

                    if (minecraft.gui.screen()
                            instanceof ConfigurationScreen) {

                        minecraft.gui.screen().onClose();

                    } else if (
                            minecraft.gui.screen() == null
                    ) {

                        ConfigurationScreen screen =
                                new ConfigurationScreen(
                                        null
                                );

                        minecraft.gui.setScreen(
                                screen
                        );
                    }
                }
        );

        /*
         * =========================================================
         * G HANDLER
         *
         * Original Flashcards system.
         * =========================================================
         */

        ClientTickEvents.END_CLIENT_TICK.register(
                minecraft -> {

                    if (!keyBindingPromptQuestion.consumeClick()) {
                        return;
                    }

                    while (keyBindingPromptQuestion.consumeClick()) {
                        // Consume additional presses.
                    }

                    if (minecraft.player == null
                            || minecraft.level == null) {

                        return;
                    }

                    if (FileManager
                            .getConfig()
                            .intervalToggle()) {

                        MutableComponent text =
                                Component.literal(
                                                "The interval toggle must be off for you to prompt a question on-demand."
                                        )
                                        .withColor(
                                                CommonColors.SOFT_RED
                                        );

                        minecraft.player
                                .sendOverlayMessage(
                                        text
                                );

                        return;
                    }

                    QuestionScheduler.promptQuestion();
                }
        );

        /*
         * =========================================================
         * J HANDLER
         *
         * Smart StudyCraft question.
         * =========================================================
         */

        ClientTickEvents.END_CLIENT_TICK.register(
                minecraft -> {

                    if (!keyBindingStudyQuestion.consumeClick()) {
                        return;
                    }

                    while (keyBindingStudyQuestion.consumeClick()) {
                        // Consume additional presses.
                    }

                    if (minecraft.player == null
                            || minecraft.level == null) {

                        return;
                    }

                    /*
                     * Don't open a StudyCraft question over
                     * another screen/inventory/menu.
                     */
                    if (minecraft.gui.screen() != null) {
                        return;
                    }

                    StudyQuestion question =
                            StudyScheduler.nextQuestion();

                    if (question == null) {

                        minecraft.player
                                .sendOverlayMessage(
                                        Component.literal(
                                                "No StudyCraft questions loaded."
                                        )
                                );

                        return;
                    }

                    minecraft.gui.setScreen(
                            new StudyQuestionScreen(
                                    question
                            )
                    );

                    /*
                     * Reset the automatic timer because
                     * the player has just studied manually.
                     */
                    StudyAutoQuizManager.resetTimer();
                }
        );

        /*
         * =========================================================
         * K HANDLER
         *
         * Toggle automatic StudyCraft questions.
         * =========================================================
         */

        ClientTickEvents.END_CLIENT_TICK.register(
                minecraft -> {

                    if (!keyBindingToggleStudyCraft.consumeClick()) {
                        return;
                    }

                    while (keyBindingToggleStudyCraft.consumeClick()) {
                        // Consume additional presses.
                    }

                    boolean enabled =
                            StudyAutoQuizManager.toggle();

                    if (minecraft.player != null) {

                        minecraft.player
                                .sendOverlayMessage(
                                        Component.literal(
                                                enabled
                                                        ? "StudyCraft automatic questions: ON"
                                                        : "StudyCraft automatic questions: OFF"
                                        )
                                );
                    }
                }
        );

        /*
         * =========================================================
         * AUTOMATIC STUDYCRAFT TIMER
         * =========================================================
         */

        ClientTickEvents.END_CLIENT_TICK.register(
                StudyAutoQuizManager::tick
        );
    }
}
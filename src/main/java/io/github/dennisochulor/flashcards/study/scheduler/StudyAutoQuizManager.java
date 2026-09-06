package io.github.dennisochulor.flashcards.study.scheduler;

import io.github.dennisochulor.flashcards.study.question.StudyQuestion;
import io.github.dennisochulor.flashcards.study.screen.StudyQuestionScreen;
import net.minecraft.client.Minecraft;

public final class StudyAutoQuizManager {

    /*
     * Minecraft normally runs at 20 ticks/sec.
     *
     * 20 * 60 * 5
     * = approximately five minutes.
     */
    private static final int INTERVAL_TICKS =
            20 * 60 * 1;

    private static int ticksRemaining =
            INTERVAL_TICKS;

    private static boolean enabled =
            true;

    private StudyAutoQuizManager() {
    }

    public static void tick(
            Minecraft minecraft
    ) {

        if (!enabled) {
            return;
        }

        /*
         * Only count actual gameplay time.
         */
        if (minecraft.player == null
                || minecraft.level == null) {

            return;
        }

        /*
         * Don't interrupt menus,
         * inventories or another question.
         */
        if (minecraft.gui.screen() != null) {
            return;
        }

        ticksRemaining--;

        if (ticksRemaining > 0) {
            return;
        }

        ticksRemaining =
                INTERVAL_TICKS;

        StudyQuestion question =
                StudyScheduler.nextQuestion();

        if (question == null) {
            return;
        }

        minecraft.gui.setScreen(
                new StudyQuestionScreen(
                        question
                )
        );
    }

    public static boolean toggle() {

        enabled =
                !enabled;

        ticksRemaining =
                INTERVAL_TICKS;

        return enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void resetTimer() {

        ticksRemaining =
                INTERVAL_TICKS;
    }
}
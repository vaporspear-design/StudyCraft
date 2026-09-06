package io.github.dennisochulor.flashcards.study.screen;

import io.github.dennisochulor.flashcards.questions.ScalableMultilineTextWidget;
import io.github.dennisochulor.flashcards.study.progress.ProgressManager;
import io.github.dennisochulor.flashcards.study.progress.QuestionProgress;
import io.github.dennisochulor.flashcards.study.question.StudyQuestion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class StudyResultScreen extends Screen {

    private static final int MAX_CONTENT_WIDTH = 420;

    private final StudyQuestion question;

    @Nullable
    private final String userAnswer;

    @Nullable
    private final Boolean correct;

    @Nullable
    private final QuestionProgress progress;

    private final boolean waitingForSelfMark;

    /*
     * Constructor for SELF_MARK questions.
     */
    public StudyResultScreen(
            StudyQuestion question
    ) {
        super(
                Component.literal(
                        "StudyCraft Answer"
                )
        );

        this.question = question;
        this.userAnswer = null;
        this.correct = null;
        this.progress = null;
        this.waitingForSelfMark = true;
    }

    /*
     * Constructor for automatically marked
     * TEXT / NUMERIC questions.
     */
    public StudyResultScreen(
            StudyQuestion question,
            String userAnswer,
            boolean correct,
            QuestionProgress progress
    ) {
        super(
                Component.literal(
                        "StudyCraft Result"
                )
        );

        this.question = question;
        this.userAnswer = userAnswer;
        this.correct = correct;
        this.progress = progress;
        this.waitingForSelfMark = false;
    }

    @Override
    public void init() {

        int contentWidth =
                Math.min(
                        MAX_CONTENT_WIDTH,
                        Math.max(200, width - 40)
                );

        String titleText;

        if (waitingForSelfMark) {

            titleText =
                    "CHECK YOUR ANSWER";

        } else if (Boolean.TRUE.equals(correct)) {

            titleText =
                    "CORRECT!";

        } else {

            titleText =
                    "WRONG";
        }

        StringWidget title =
                new StringWidget(
                        Component.literal(
                                titleText
                        ),
                        Minecraft.getInstance().font
                );

        title.setPosition(
                width / 2 - title.getWidth() / 2,
                25
        );

        addRenderableWidget(title);

        StringWidget subject =
                new StringWidget(
                        Component.literal(
                                question.subject()
                                        .displayName()
                                        + " • "
                                        + question.topic()
                        ),
                        Minecraft.getInstance().font
                );

        subject.setPosition(
                width / 2 - subject.getWidth() / 2,
                48
        );

        addRenderableWidget(subject);

        /*
         * Correct/model answer.
         */
        String answerHeading =
                waitingForSelfMark
                        ? "Model answer:"
                        : "Correct answer:";

        StringWidget answerLabel =
                new StringWidget(
                        Component.literal(
                                answerHeading
                        ),
                        Minecraft.getInstance().font
                );

        answerLabel.setPosition(
                width / 2 - answerLabel.getWidth() / 2,
                78
        );

        addRenderableWidget(answerLabel);

        ScalableMultilineTextWidget answerText =
                new ScalableMultilineTextWidget(
                        Component.literal(
                                question.answer()
                        ),
                        Minecraft.getInstance().font,
                        90
                );

        answerText
                .setCentered(true)
                .setMaxWidth(contentWidth);

        answerText.setPosition(
                width / 2 - answerText.getWidth() / 2,
                100
        );

        addRenderableWidget(answerText);

        if (waitingForSelfMark) {

            addSelfMarkButtons();

        } else {

            addAutomaticResultWidgets();
        }
    }

    private void addSelfMarkButtons() {

        Button correctButton =
                Button.builder(
                                Component.literal(
                                        "I Knew It"
                                ),
                                _ -> recordManualResult(
                                        true
                                )
                        )
                        .size(120, 20)
                        .build();

        correctButton.setPosition(
                width / 2 - 125,
                height - 55
        );

        Button wrongButton =
                Button.builder(
                                Component.literal(
                                        "I Got It Wrong"
                                ),
                                _ -> recordManualResult(
                                        false
                                )
                        )
                        .size(120, 20)
                        .build();

        wrongButton.setPosition(
                width / 2 + 5,
                height - 55
        );

        addRenderableWidget(correctButton);
        addRenderableWidget(wrongButton);
    }

    private void addAutomaticResultWidgets() {

        if (userAnswer != null) {

            StringWidget yourAnswer =
                    new StringWidget(
                            Component.literal(
                                    "Your answer: "
                                            + userAnswer
                            ),
                            Minecraft.getInstance().font
                    );

            yourAnswer.setPosition(
                    width / 2
                            - yourAnswer.getWidth() / 2,
                    height - 105
            );

            addRenderableWidget(yourAnswer);
        }

        if (progress != null) {

            String progressText =
                    "Mastery: "
                            + progress.mastery()
                            + "/5"
                            + "  •  Accuracy: "
                            + Math.round(
                            progress.accuracy()
                                    * 100
                    )
                            + "%";

            StringWidget mastery =
                    new StringWidget(
                            Component.literal(
                                    progressText
                            ),
                            Minecraft.getInstance().font
                    );

            mastery.setPosition(
                    width / 2
                            - mastery.getWidth() / 2,
                    height - 80
            );

            addRenderableWidget(mastery);
        }

        Button done =
                Button.builder(
                                Component.literal(
                                        "Done"
                                ),
                                _ -> Minecraft
                                        .getInstance()
                                        .gui
                                        .setScreen(null)
                        )
                        .size(100, 20)
                        .build();

        done.setPosition(
                width / 2 - 50,
                height - 45
        );

        addRenderableWidget(done);
    }

    private void recordManualResult(
            boolean knewAnswer
    ) {

        long now =
                System.currentTimeMillis();

        QuestionProgress updated;

        if (knewAnswer) {

            updated =
                    ProgressManager.recordCorrect(
                            question.id(),
                            now
                    );

        } else {

            updated =
                    ProgressManager.recordWrong(
                            question.id(),
                            now
                    );
        }

        Minecraft.getInstance()
                .gui
                .setScreen(
                        new StudyResultScreen(
                                question,
                                "",
                                knewAnswer,
                                updated
                        )
                );
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
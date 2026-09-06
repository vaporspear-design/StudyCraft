package io.github.dennisochulor.flashcards.study.screen;

import io.github.dennisochulor.flashcards.questions.ScalableMultilineTextWidget;
import io.github.dennisochulor.flashcards.study.progress.ProgressManager;
import io.github.dennisochulor.flashcards.study.progress.QuestionProgress;
import io.github.dennisochulor.flashcards.study.question.AnswerValidator;
import io.github.dennisochulor.flashcards.study.question.QuestionType;
import io.github.dennisochulor.flashcards.study.question.StudyQuestion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class StudyQuestionScreen extends Screen {

    private static final int MAX_CONTENT_WIDTH = 420;

    private final StudyQuestion question;

    private MultiLineEditBox answerBox;
    private Button submitButton;

    public StudyQuestionScreen(StudyQuestion question) {
        super(Component.literal("StudyCraft Question"));
        this.question = question;
    }

    @Override
    public void init() {

        int contentWidth =
                Math.min(
                        MAX_CONTENT_WIDTH,
                        Math.max(200, width - 40)
                );

        /*
         * STUDYCRAFT title
         */
        StringWidget title =
                new StringWidget(
                        Component.literal("STUDYCRAFT"),
                        Minecraft.getInstance().font
                );

        title.setPosition(
                width / 2 - title.getWidth() / 2,
                25
        );

        addRenderableWidget(title);

        /*
         * Subject • Topic • Type
         */
        String details =
                question.subject().displayName()
                        + "  •  "
                        + question.topic()
                        + "  •  "
                        + question.type().displayName();

        StringWidget subjectText =
                new StringWidget(
                        Component.literal(details),
                        Minecraft.getInstance().font
                );

        subjectText.setPosition(
                width / 2 - subjectText.getWidth() / 2,
                48
        );

        addRenderableWidget(subjectText);

        /*
         * Question text
         */
        ScalableMultilineTextWidget questionText =
                new ScalableMultilineTextWidget(
                        Component.literal(
                                question.question()
                        ),
                        Minecraft.getInstance().font,
                        110
                );

        questionText
                .setCentered(true)
                .setMaxWidth(contentWidth);

        questionText.setPosition(
                width / 2 - questionText.getWidth() / 2,
                80
        );

        addRenderableWidget(questionText);

        /*
         * Different UI depending on question type.
         */
        if (question.type() == QuestionType.SELF_MARK) {

            initialiseSelfMarkControls();

        } else {

            initialiseAutomaticControls(
                    contentWidth
            );
        }
    }

    private void initialiseAutomaticControls(
            int contentWidth
    ) {

        answerBox =
                MultiLineEditBox.builder()
                        .setPlaceholder(
                                Component.literal(
                                        "Type your answer here..."
                                )
                        )
                        .build(
                                Minecraft.getInstance().font,
                                contentWidth,
                                60,
                                Component.literal(
                                        "StudyCraft answer"
                                )
                        );

        answerBox.setPosition(
                width / 2 - contentWidth / 2,
                height - 135
        );

        answerBox.setCharacterLimit(500);

        submitButton =
                Button.builder(
                                Component.literal(
                                        "Submit"
                                ),
                                _ -> submitAnswer()
                        )
                        .size(100, 20)
                        .build();

        submitButton.setPosition(
                width / 2 - 105,
                height - 55
        );

        submitButton.active = false;

        answerBox.setValueListener(
                value ->
                        submitButton.active =
                                !value.isBlank()
        );

        Button cancelButton =
                Button.builder(
                                Component.literal(
                                        "Cancel"
                                ),
                                _ -> Minecraft
                                        .getInstance()
                                        .gui
                                        .setScreen(null)
                        )
                        .size(100, 20)
                        .build();

        cancelButton.setPosition(
                width / 2 + 5,
                height - 55
        );

        addRenderableWidget(answerBox);
        addRenderableWidget(submitButton);
        addRenderableWidget(cancelButton);
    }

    private void initialiseSelfMarkControls() {

        Button showAnswerButton =
                Button.builder(
                                Component.literal(
                                        "Show Answer"
                                ),
                                _ -> Minecraft
                                        .getInstance()
                                        .gui
                                        .setScreen(
                                                new StudyResultScreen(
                                                        question
                                                )
                                        )
                        )
                        .size(120, 20)
                        .build();

        showAnswerButton.setPosition(
                width / 2 - 125,
                height - 55
        );

        Button cancelButton =
                Button.builder(
                                Component.literal(
                                        "Cancel"
                                ),
                                _ -> Minecraft
                                        .getInstance()
                                        .gui
                                        .setScreen(null)
                        )
                        .size(120, 20)
                        .build();

        cancelButton.setPosition(
                width / 2 + 5,
                height - 55
        );

        addRenderableWidget(showAnswerButton);
        addRenderableWidget(cancelButton);
    }

    private void submitAnswer() {

        String userAnswer =
                answerBox.getValue();

        if (userAnswer.isBlank()) {
            return;
        }

        boolean correct =
                AnswerValidator.isCorrect(
                        question,
                        userAnswer
                );

        long now =
                System.currentTimeMillis();

        QuestionProgress progress;

        if (correct) {

            progress =
                    ProgressManager.recordCorrect(
                            question.id(),
                            now
                    );

        } else {

            progress =
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
                                userAnswer,
                                correct,
                                progress
                        )
                );
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
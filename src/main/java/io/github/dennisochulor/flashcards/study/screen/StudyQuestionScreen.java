package io.github.dennisochulor.flashcards.study.screen;

import io.github.dennisochulor.flashcards.questions.ScalableMultilineTextWidget;
import io.github.dennisochulor.flashcards.study.question.StudyQuestion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class StudyQuestionScreen extends Screen {

    private static final int TEXT_WIDTH = 400;

    private final StudyQuestion question;

    public StudyQuestionScreen(StudyQuestion question) {
        super(Component.literal("StudyCraft Question"));
        this.question = question;
    }

    @Override
    public void init() {

        StringWidget title =
                new StringWidget(
                        Component.literal("STUDYCRAFT"),
                        Minecraft.getInstance().font
                );

        String subjectLine =
                question.subject().displayName()
                        + "  •  "
                        + question.topic()
                        + "  •  "
                        + question.type().displayName();

        StringWidget subjectText =
                new StringWidget(
                        Component.literal(subjectLine),
                        Minecraft.getInstance().font
                );

        ScalableMultilineTextWidget questionText =
                new ScalableMultilineTextWidget(
                        Component.literal(question.question()),
                        Minecraft.getInstance().font,
                        120
                );

        questionText
                .setCentered(true)
                .setMaxWidth(
                        Math.min(
                                TEXT_WIDTH,
                                Math.max(100, width - 40)
                        )
                );

        Button showAnswerButton =
                Button.builder(
                                Component.literal("Show Answer"),
                                _ -> openAnswerScreen()
                        )
                        .size(120, 20)
                        .build();

        Button doneButton =
                Button.builder(
                                Component.literal("Done"),
                                _ -> Minecraft.getInstance()
                                        .gui
                                        .setScreen(null)
                        )
                        .size(75, 20)
                        .build();

        LinearLayout buttons =
                LinearLayout.horizontal()
                        .spacing(10);

        buttons.defaultCellSetting()
                .alignVerticallyMiddle();

        buttons.addChild(showAnswerButton);
        buttons.addChild(doneButton);

        LinearLayout root =
                LinearLayout.vertical()
                        .spacing(16);

        root.defaultCellSetting()
                .alignHorizontallyCenter();

        root.addChild(title);
        root.addChild(subjectText);
        root.addChild(questionText);
        root.addChild(buttons);

        root.arrangeElements();

        FrameLayout.alignInRectangle(
                root,
                0,
                0,
                width,
                height,
                0.5F,
                0.2F
        );

        root.visitWidgets(
                this::addRenderableWidget
        );
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private void openAnswerScreen() {

        Minecraft.getInstance().gui.setScreen(
                new Screen(
                        Component.literal(
                                "StudyCraft Answer"
                        )
                ) {

                    @Override
                    public void init() {

                        StringWidget title =
                                new StringWidget(
                                        Component.literal(
                                                "ANSWER"
                                        ),
                                        Minecraft.getInstance().font
                                );

                        ScalableMultilineTextWidget answerText =
                                new ScalableMultilineTextWidget(
                                        Component.literal(
                                                question.answer()
                                        ),
                                        Minecraft.getInstance().font,
                                        140
                                );

                        answerText
                                .setCentered(true)
                                .setMaxWidth(
                                        Math.min(
                                                TEXT_WIDTH,
                                                Math.max(
                                                        100,
                                                        width - 40
                                                )
                                        )
                                );

                        Button backButton =
                                Button.builder(
                                                Component.literal(
                                                        "Back to Question"
                                                ),
                                                _ -> Minecraft
                                                        .getInstance()
                                                        .gui
                                                        .setScreen(
                                                                StudyQuestionScreen.this
                                                        )
                                        )
                                        .size(120, 20)
                                        .build();

                        Button doneButton =
                                Button.builder(
                                                Component.literal(
                                                        "Done"
                                                ),
                                                _ -> Minecraft
                                                        .getInstance()
                                                        .gui
                                                        .setScreen(null)
                                        )
                                        .size(75, 20)
                                        .build();

                        LinearLayout buttons =
                                LinearLayout.horizontal()
                                        .spacing(10);

                        buttons.addChild(backButton);
                        buttons.addChild(doneButton);

                        LinearLayout root =
                                LinearLayout.vertical()
                                        .spacing(20);

                        root.defaultCellSetting()
                                .alignHorizontallyCenter();

                        root.addChild(title);
                        root.addChild(answerText);
                        root.addChild(buttons);

                        root.arrangeElements();

                        FrameLayout.alignInRectangle(
                                root,
                                0,
                                0,
                                width,
                                height,
                                0.5F,
                                0.25F
                        );

                        root.visitWidgets(
                                this::addRenderableWidget
                        );
                    }

                    @Override
                    public boolean shouldCloseOnEsc() {
                        return false;
                    }
                }
        );
    }
}
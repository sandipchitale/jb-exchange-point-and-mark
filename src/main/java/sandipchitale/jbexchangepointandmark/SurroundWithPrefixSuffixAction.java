package sandipchitale.jbexchangepointandmark;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class SurroundWithPrefixSuffixAction extends AnAction {

    public SurroundWithPrefixSuffixAction() {
    }

    @Override
    @SuppressWarnings("DialogTitleCapitalization")
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        Editor editor = CommonDataKeys.EDITOR.getData(actionEvent.getDataContext());
        if (editor != null) {
            Document document = editor.getDocument();
            if (!document.isWritable()) {
                Messages.showErrorDialog("The document is not writable!", "Error");
            } else {

                DialogBuilder builder = new DialogBuilder(editor.getComponent());

                JPanel prefixSuffixPaneWrapper = new JPanel(new BorderLayout(10, 10));

                JPanel prefixPanel = new JPanel(new BorderLayout(10, 10));
                prefixPanel.add(new JLabel("Prefix:"), BorderLayout.WEST);
                JTextField prefixTextField = new JTextField();
                prefixTextField.setColumns(20);
                prefixPanel.add(prefixTextField, BorderLayout.CENTER);
                prefixSuffixPaneWrapper.add(prefixPanel, BorderLayout.NORTH);

                JPanel suffixPanel = new JPanel(new BorderLayout(10, 10));
                suffixPanel.add(new JLabel("Suffix:"), BorderLayout.WEST);
                JTextField suffixTextField = new JTextField();
                suffixTextField.setColumns(20);
                suffixPanel.add(suffixTextField, BorderLayout.CENTER);
                prefixSuffixPaneWrapper.add(suffixPanel, BorderLayout.SOUTH);

                builder.setNorthPanel(prefixSuffixPaneWrapper);
                builder.setCenterPanel(new JLabel("<html>Enter Prefix and Suffix. Use '(start, step)'<br/>in prefix, suffix strings to generate sequence."));

                builder.setDimensionServiceKey("SurroundWithPrefixSuffix");
                builder.setTitle("[prefix]|[suffix]");
                builder.removeAllActions();
                builder.addOkAction();
                builder.addCancelAction();
                builder.setPreferredFocusComponent(prefixTextField);

                if (builder.show() == DialogWrapper.OK_EXIT_CODE) {
                    String prefix = prefixTextField.getText();
                    String suffix = suffixTextField.getText();
                    if (prefix.length() == 0 && suffix.length() == 0) {
                        Messages.showErrorDialog("Prefix and suffix are both zero length!\nMust specify at least one with non-zero length.", "Error");
                        return;
                    }
                    WriteCommandAction.runWriteCommandAction(
                            editor.getProject(),
                            (Computable<String>) () -> {
                                CaretModel caretModel = editor.getCaretModel();
                                List<Caret> carets = caretModel.getAllCarets();
                                boolean[] doNotCallExtraExchangeStartAndEndOfSelectionAction = {false};
                                IntStream.range(0, carets.size()).forEach((int index) -> {
                                    index = carets.size() - index - 1;
                                    Caret caret = carets.get(index);
                                    int selectionStart = caret.getSelectionStart();
                                    int selectionEnd = caret.getSelectionEnd();

                                    if (caret.getOffset() == selectionStart) {
                                        if (!doNotCallExtraExchangeStartAndEndOfSelectionAction[0]) {
                                            doNotCallExtraExchangeStartAndEndOfSelectionAction[0] = true;
                                        }
                                    }

                                    int insertPrefixAt = selectionStart;
                                    int insertSuffixAt = selectionEnd;
                                    if (selectionStart > selectionEnd) {
                                        insertPrefixAt = selectionEnd;
                                        insertSuffixAt = selectionStart;
                                    }
                                    if (suffix.length() != 0) {
                                        document.insertString(insertSuffixAt, process(suffix, index));
                                    }
                                    if (prefix.length() != 0) {
                                        document.insertString(insertPrefixAt, process(prefix, index));
                                    }
                                });
                                ActionManager actionManager = ActionManager.getInstance();
                                AnAction anAction = actionManager.getAction("ExchangeStartAndEndOfSelectionAction");

                                actionManager.tryToExecute(
                                        anAction,
                                        null,
                                        actionEvent.getDataContext().getData(PlatformDataKeys.CONTEXT_COMPONENT),
                                        "editor",
                                        true
                                );

                                // anAction.actionPerformed(actionEvent);
                                if (!doNotCallExtraExchangeStartAndEndOfSelectionAction[0]) {
                                    actionManager.tryToExecute(
                                            anAction,
                                            null,
                                            actionEvent.getDataContext().getData(PlatformDataKeys.CONTEXT_COMPONENT),
                                            "editor",
                                            true
                                    );
                                    // anAction.actionPerformed(actionEvent);
                                }
                                return "";
                            }
                    );
                }
            }
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = CommonDataKeys.EDITOR.getData(e.getDataContext());
        e.getPresentation().setEnabled(editor != null && editor.getDocument().isWritable());
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

    private static final Pattern stepSpecsPattern = Pattern.compile("\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)");

    private static String process(String prefixOrSuffix, int index) {
        Matcher matcher = stepSpecsPattern.matcher(prefixOrSuffix);
        return matcher.replaceAll((MatchResult match) -> {
            int start = Integer.parseInt(match.group(1));
            int step = Integer.parseInt(match.group(2));
            return String.valueOf(start + index * step);
        });
    }
}

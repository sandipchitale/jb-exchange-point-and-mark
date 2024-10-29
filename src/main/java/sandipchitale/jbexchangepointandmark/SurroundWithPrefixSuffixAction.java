package sandipchitale.jbexchangepointandmark;

import com.google.common.collect.Lists;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class SurroundWithPrefixSuffixAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        Editor editor = CommonDataKeys.EDITOR.getData(actionEvent.getDataContext());
        if (editor != null) {
            Document document = editor.getDocument();
            if (!document.isWritable()) {
                Messages.showErrorDialog("The document is not writable!", "Error");
            } else {
                String prefixSuffix = Messages.showInputDialog(editor.getComponent(), "Specify [prefix]|[suffix]:", "[prefix]|[suffix]", AllIcons.Actions.ArrowExpand);
                if (prefixSuffix != null && !prefixSuffix.isEmpty() && !prefixSuffix.equals("|")) {
                    String[] prefixSuffixParts = prefixSuffix.split("\\|");
                    String prefix = null;
                    String suffix = null;
                    if (prefixSuffixParts.length == 2) {
                        prefix = prefixSuffixParts[0];
                        suffix = prefixSuffixParts[1];
                    } else if (prefixSuffixParts.length == 1) {
                        prefix = prefixSuffixParts[0];
                    }
                    String finalPrefix = prefix;
                    String finalSuffix = suffix;
                    WriteCommandAction.runWriteCommandAction(
                            editor.getProject(),
                            (Computable<String>) () -> {
                                CaretModel caretModel = editor.getCaretModel();
                                List<Caret> carets = caretModel.getAllCarets();
                                boolean[] doNotCallExtraExchangeStartAndEndOfSelectionAction = { false };
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
                                    if (finalSuffix != null) {
                                        document.insertString(insertSuffixAt, process(finalSuffix, index));
                                    }
                                    if (finalPrefix != null) {
                                        document.insertString(insertPrefixAt, process(finalPrefix, index));
                                    }
                                });
                                AnAction anAction = ActionManager.getInstance().getAction("ExchangeStartAndEndOfSelectionAction");
                                anAction.actionPerformed(actionEvent);
                                if (!doNotCallExtraExchangeStartAndEndOfSelectionAction[0]) {
                                    anAction.actionPerformed(actionEvent);
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
        e.getPresentation().setEnabled(editor != null && editor.getSelectionModel().hasSelection(true));
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
            return String.valueOf(start + index*step);
        });
    }
}

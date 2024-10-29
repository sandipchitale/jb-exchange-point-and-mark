package sandipchitale.jbexchangepointandmark;

import com.google.common.collect.Lists;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SurroundWithPrefixSuffixAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = CommonDataKeys.EDITOR.getData(e.getDataContext());
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
                                Lists.reverse(carets).forEach((Caret caret) -> {
                                    int selectionStart = caret.getSelectionStart();
                                    int selectionEnd = caret.getSelectionEnd();
                                    if (selectionStart < selectionEnd) {
                                        if (finalSuffix != null) {
                                            document.insertString(selectionEnd, finalSuffix);
                                        }
                                        if (finalPrefix != null) {
                                            document.insertString(selectionStart, finalPrefix);
                                        }
                                    } else {
                                        if (finalSuffix != null) {
                                            document.insertString(selectionStart, finalSuffix);
                                        }
                                        if (finalPrefix != null) {
                                            document.insertString(selectionEnd, finalPrefix);
                                        }
                                    }
                                });
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
}

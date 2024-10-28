package sandipchitale.jbexchangepointandmark;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ExchangeStartAndEndOfSelectionAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = CommonDataKeys.EDITOR.getData(e.getDataContext());
        if (editor != null) {
            CaretModel caretModel = editor.getCaretModel();
            List<Caret> carets = caretModel.getAllCarets();
            carets.forEach((Caret caret) -> {
                int selectionStart = caret.getSelectionStart();
                int selectionEnd = caret.getSelectionEnd();
                if (selectionStart != selectionEnd) {
                    // Swap
                    if (caret.getOffset() == selectionStart) {
                        caret.moveToOffset(selectionEnd);
                    } else {
                        caret.moveToOffset(selectionStart);
                    }
                }
            });
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

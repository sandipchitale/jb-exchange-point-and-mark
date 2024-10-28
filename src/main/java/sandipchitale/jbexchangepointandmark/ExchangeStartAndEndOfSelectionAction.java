package sandipchitale.jbexchangepointandmark;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExchangeStartAndEndOfSelectionAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        if (editor != null) {
            CaretModel caretModel = editor.getCaretModel();
            SelectionModel selectionModel = editor.getSelectionModel();
            List<Caret> carets = caretModel.getAllCarets();
            Map<Caret, Integer> newSelectionStarts = new LinkedHashMap<>();
            Map<Caret, Integer> newSelectionEnds = new LinkedHashMap<>();
            List<CaretState> caretStates = new ArrayList<>();
            carets.forEach((Caret caret) -> {
                int selectionStart = caret.getSelectionStart();
                int selectionEnd = caret.getSelectionEnd();
                // Swap
                if (caret.getOffset() == selectionStart) {
                    caret.moveToOffset(selectionEnd);
                } else {
                    caret.moveToOffset(selectionStart);
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

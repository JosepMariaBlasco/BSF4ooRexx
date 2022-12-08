package org.oorexx.ide.intellij.completion;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.completion.CompletionPhase;
import com.intellij.codeInsight.completion.impl.CompletionServiceImpl;
import com.intellij.codeInsight.editorActions.CompletionAutoPopupHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class OoRexxCompletionAutoPopupHandler extends CompletionAutoPopupHandler {
    @Override
    public @NotNull Result checkAutoPopup(char charTyped, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {

        CompletionPhase phase = CompletionServiceImpl.getCompletionPhase();
        Result result = super.checkAutoPopup(charTyped, project, editor, file);

        if(result == Result.CONTINUE) {
            if (Character.isLetter(charTyped) || charTyped == '_' || charTyped == ':') {
                if (phase instanceof CompletionPhase.EmptyAutoPopup && ((CompletionPhase.EmptyAutoPopup) phase).allowsSkippingNewAutoPopup(editor, charTyped)) {
                    return Result.CONTINUE;
                }

                AutoPopupController.getInstance(project).scheduleAutoPopup(editor);
                return Result.STOP;
            }
        }

        return result;
    }
}

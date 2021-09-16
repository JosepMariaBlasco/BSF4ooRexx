package org.oorexx.ide.intellij.annotator.fix;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by alex on 03.09.16.
 */
public class UniqueContraintFix implements IntentionAction {

    private PsiElement psiElement;

    public UniqueContraintFix(PsiElement psiElement) {
        this.psiElement = psiElement;
    }

    @Nls
    @NotNull
    @Override
    public String getText() {
        return "Remove";
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Unique Constraints";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        return true;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        psiFile.getNode().removeChild(psiElement.getNode());
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }
}

package org.oorexx.ide.intellij.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.oorexx.ide.intellij.annotator.fix.UniqueContraintFix;
import org.oorexx.ide.intellij.util.PsiUtil;
import org.oorexx.ide.lang.constraint.UniqueConstraint;

import java.util.List;

/**
 * Created by alex on 03.09.16.
 */
public class UniqueConstraintAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        if(psiElement instanceof UniqueConstraint){
            List<PsiElement> siblings = PsiUtil.filter(psiElement.getParent().getChildren(), psiElement.getNode().getElementType());

            if(siblings.size() > 1){
                for(PsiElement element: siblings) {
                    TextRange range = new TextRange(element.getTextRange().getStartOffset(),
                            element.getTextRange().getEndOffset());
                    if(!element.equals(psiElement))
                    annotationHolder.createErrorAnnotation(range, "'" + element.getText() + "' is not allowed with '" + psiElement.getText() + "'").registerFix(new UniqueContraintFix(element));
                }
            }
        }
    }
}


package org.oorexx.ide.intellij.annotator;

import com.google.common.collect.Lists;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NotNull;
import org.oorexx.ide.intellij.highlighting.OoRexxSyntaxHighlighter;
import org.oorexx.ide.intellij.util.KeywordHelper;

import org.oorexx.ide.lang.core.DirectiveHeader;
import org.oorexx.ide.lang.psi.*;
import org.oorexx.ide.lexer.token.OoRexxTokenType;

import java.util.List;


public class KeywordHighlighter implements Annotator {

    private List<String> keywordList = Lists.newArrayList(KeywordHelper.getList());

    @Override
    public synchronized void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        if (psiElement instanceof OoRexxDirective) {
            PsiElement directiveStart = psiElement.getFirstChild();
            PsiElement directiveHeader = PsiTreeUtil.findChildOfType(psiElement, DirectiveHeader.class);

            annotationHolder.createInfoAnnotation(directiveStart.getNode(), null).setTextAttributes(OoRexxSyntaxHighlighter.OOREXX_KEYWORD);

            if (directiveHeader != null) {
                annotationHolder.createInfoAnnotation(directiveHeader.getFirstChild().getNode(), null).setTextAttributes(OoRexxSyntaxHighlighter.OOREXX_KEYWORD);
            }

        } else if (psiElement instanceof OoRexxDirectiveResourceData) {
            annotationHolder.createInfoAnnotation(psiElement.getNode(), null).setTextAttributes(OoRexxSyntaxHighlighter.OOREXX_STRINGS);
        } else if (psiElement instanceof OoRexxNumericExpr) {
            annotationHolder.createInfoAnnotation(psiElement.getNode(), null).setTextAttributes(OoRexxSyntaxHighlighter.OOREXX_NUMBER);
        } else if (psiElement instanceof OoRexxNegativeNumericExpr) {
            annotationHolder.createInfoAnnotation(psiElement.getNode(), null).setTextAttributes(OoRexxSyntaxHighlighter.OOREXX_NUMBER);
        } else if (psiElement instanceof OoRexxDirectiveResourceEnd) {
            annotationHolder.createInfoAnnotation(psiElement.getNode(), null).setTextAttributes(OoRexxSyntaxHighlighter.OOREXX_KEYWORD);
        } else {
            IElementType iElementType = PsiUtilCore.getElementType(psiElement);
                if(iElementType instanceof OoRexxTokenType){
                    OoRexxTokenType ooRexxTokenType = (OoRexxTokenType) iElementType;

                    if((keywordList.contains(ooRexxTokenType.getTokenName().toUpperCase())
                            && !ooRexxTokenType.getTokenName().toUpperCase().equals("SUPER")
                            && !ooRexxTokenType.getTokenName().toUpperCase().equals("SELF")
                            && (psiElement.getParent() instanceof OoRexxKeywordList))){
                        annotationHolder.createInfoAnnotation(psiElement.getNode(), null) .setEnforcedTextAttributes(TextAttributes.ERASE_MARKER);
                    }
                }
        }
    }

}

package org.oorexx.ide.intellij.annotator;

import java.util.List;

import com.google.common.collect.Lists;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NotNull;
import org.oorexx.ide.intellij.highlighting.OoRexxSyntaxHighlighter;
import org.oorexx.ide.intellij.util.KeywordHelper;
import org.oorexx.ide.lang.core.DirectiveHeader;
import org.oorexx.ide.lang.psi.OoRexxBuiltInFunctions;
import org.oorexx.ide.lang.psi.OoRexxDirective;
import org.oorexx.ide.lang.psi.OoRexxDirectiveResourceData;
import org.oorexx.ide.lang.psi.OoRexxDirectiveResourceEnd;
import org.oorexx.ide.lang.psi.OoRexxEnvironment;
import org.oorexx.ide.lang.psi.OoRexxKeywordList;
import org.oorexx.ide.lang.psi.OoRexxNegativeNumericExpr;
import org.oorexx.ide.lang.psi.OoRexxNumericExpr;
import org.oorexx.ide.lexer.token.OoRexxTokenType;

import static org.oorexx.ide.intellij.highlighting.OoRexxSyntaxHighlighter.OOREXX_KEYWORD;
import static org.oorexx.ide.intellij.highlighting.OoRexxSyntaxHighlighter.OOREXX_NUMBER;
import static org.oorexx.ide.intellij.highlighting.OoRexxSyntaxHighlighter.OOREXX_STRINGS;


public class KeywordHighlighter implements Annotator {

    private final List<String> keywordList = Lists.newArrayList(KeywordHelper.getList());

    @Override
    public synchronized void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        if (psiElement instanceof OoRexxDirective) {
            PsiElement directiveStart = psiElement.getFirstChild();
            PsiElement directiveHeader = PsiTreeUtil.findChildOfType(psiElement, DirectiveHeader.class);

            annotationHolder.newAnnotation(HighlightSeverity.TEXT_ATTRIBUTES, "")
                    .textAttributes(OOREXX_KEYWORD)
                    .range(directiveStart.getNode()).create();

            if (directiveHeader != null) {
                annotationHolder.newAnnotation(HighlightSeverity.TEXT_ATTRIBUTES, "")
                        .textAttributes(OOREXX_KEYWORD)
                        .range(directiveHeader.getFirstChild().getNode()).create();
            }

        } else if (psiElement instanceof OoRexxBuiltInFunctions) {
            TextRange textRange = psiElement.getNode().getFirstChildNode().getFirstChildNode().getTextRange();
            annotate(annotationHolder, textRange, OoRexxSyntaxHighlighter.OOREXX_BUILT_IN);
        } else if (psiElement instanceof OoRexxDirectiveResourceData) {
            annotationHolder.newAnnotation(HighlightSeverity.TEXT_ATTRIBUTES, "")
                    .textAttributes(OOREXX_STRINGS)
                    .range(psiElement.getNode()).create();

        } else if (psiElement instanceof OoRexxNumericExpr) {
            annotationHolder.newAnnotation(HighlightSeverity.TEXT_ATTRIBUTES, "")
                    .textAttributes(OOREXX_NUMBER)
                    .range(psiElement.getNode()).create();

        } else if (psiElement instanceof OoRexxNegativeNumericExpr) {
            annotationHolder.newAnnotation(HighlightSeverity.TEXT_ATTRIBUTES, "")
                    .textAttributes(OOREXX_NUMBER)
                    .range(psiElement.getNode()).create();

        } else if (psiElement instanceof OoRexxDirectiveResourceEnd) {
            annotationHolder.newAnnotation(HighlightSeverity.TEXT_ATTRIBUTES, "")
                    .textAttributes(OOREXX_KEYWORD)
                    .range(psiElement.getNode()).create();

        } else if (psiElement instanceof OoRexxEnvironment) {
            annotationHolder.newAnnotation(HighlightSeverity.TEXT_ATTRIBUTES, "")
                    .textAttributes(OOREXX_KEYWORD)
                    .range(psiElement.getNode()).create();

        } else {
            IElementType iElementType = PsiUtilCore.getElementType(psiElement);
            if (iElementType instanceof OoRexxTokenType) {
                OoRexxTokenType ooRexxTokenType = (OoRexxTokenType) iElementType;

                if ((!keywordList.contains(ooRexxTokenType.getTokenName().toUpperCase())
                        || ooRexxTokenType.getTokenName().equalsIgnoreCase("SUPER")
                        || ooRexxTokenType.getTokenName().equalsIgnoreCase("SELF")
                        || (!(psiElement.getParent() instanceof OoRexxKeywordList)))) {
                    if (keywordList.contains(ooRexxTokenType.getTokenName())) {
                        annotationHolder.newAnnotation(HighlightSeverity.TEXT_ATTRIBUTES, "")
                                .textAttributes(OOREXX_KEYWORD)
                                .range(psiElement.getNode()).create();
                    }
                }
            }
        }
    }

    private void annotate(@NotNull AnnotationHolder annotationHolder, TextRange textRange, TextAttributesKey textAttributesKey) {
        annotationHolder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                        .range(textRange).textAttributes(textAttributesKey).create();
    }

}

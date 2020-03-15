package org.oorexx.doc.analyse;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.oorexx.doc.analyse.model.OoRexxElement;
import org.oorexx.doc.generate.Settings;
import org.oorexx.ide.lang.psi.*;

import java.util.function.Predicate;

import static com.intellij.psi.util.PsiTreeUtil.findChildOfType;

public abstract class Analyzer {

    protected Settings settings;
    protected CommentContext commentContext;

    public Analyzer(Settings settings, CommentContext commentContext) {
        this.settings = settings;
        this.commentContext = commentContext;
    }

    protected Predicate<PsiElement> filterByDirectiveAccess(Class<?> defaultAccess){
        return psiElement -> {
            OoRexxDirectiveAccessPrivate accessPrivate = PsiTreeUtil.findChildOfType(psiElement, OoRexxDirectiveAccessPrivate.class);
            OoRexxDirectiveAccessPackage accessPackage = PsiTreeUtil.findChildOfType(psiElement, OoRexxDirectiveAccessPackage.class);
            OoRexxDirectiveAccessPublic accessPublic = PsiTreeUtil.findChildOfType(psiElement, OoRexxDirectiveAccessPublic.class);

            if((defaultAccess.equals(OoRexxDirectiveAccessPrivate.class) || accessPrivate != null) && settings.isIncludePrivate()){
                return true;
            }

            if((defaultAccess.equals(OoRexxDirectiveAccessPackage.class) || accessPackage != null) && settings.isIncludePackage()){
                return true;
            }

            return (defaultAccess.equals(OoRexxDirectiveAccessPublic.class) || accessPublic != null) && settings.isIncludePublic();
        };
    }

    protected Predicate<PsiElement> hasNotParent(Class<? extends PsiElement> parent){
        return psiElement -> PsiTreeUtil.getParentOfType(psiElement, parent) == null;
    }

    protected void setComment(OoRexxElement result, PsiElement directive){
        PsiComment psiComment = commentContext.getComment(PsiTreeUtil.getParentOfType(directive, OoRexxDirective.class));

        result.setCommentText(psiComment);
    }

    protected String getText(PsiElement parent, Class<? extends PsiElement> search){
        PsiElement name = findChildOfType(parent, search);
        return name != null ? name.getText() : null;
    }

    protected String extractArgVariables(OoRexxKeywordUse ooRexxKeywordUse){
        OoRexxExpr ooRexxExpr = findChildOfType(ooRexxKeywordUse, OoRexxExpr.class);
        if(ooRexxExpr != null) {
            return ooRexxExpr.getText();
        }

        return null;
    }

    protected String extractArgVariables(OoRexxKeywordParse parse){
        OoRexxKeywordParseArg parseArg = findChildOfType(parse, OoRexxKeywordParseArg.class);
        if(parseArg != null){
            OoRexxTemplateList templateList = findChildOfType(parse, OoRexxTemplateList.class);

            return templateList.getText();
        }

        return null;
    }

    public Settings getSettings() {
        return settings;
    }
}

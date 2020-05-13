package org.oorexx.doc.analyse;

import com.intellij.psi.PsiElement;
import org.oorexx.doc.analyse.model.AccessModifier;
import org.oorexx.doc.analyse.model.OoRexxMethod;

import org.oorexx.doc.generate.Settings;
import org.oorexx.ide.lang.psi.*;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.intellij.psi.util.PsiTreeUtil.findChildOfType;
import static com.intellij.psi.util.PsiTreeUtil.findChildrenOfType;

public class MethodAnalyzer extends Analyzer{

    public MethodAnalyzer(Settings settings, CommentContext commentContext) {
        super(settings, commentContext);
    }

    public OoRexxMethod analyze(OoRexxDirectiveMethod directive){
        OoRexxMethod ooRexxMethod = new OoRexxMethod(directive);
        OoRexxDirectiveMethodName name = findChildOfType(directive, OoRexxDirectiveMethodName.class);
        ooRexxMethod.setName(name.getText());

        OoRexxKeywordExpose expose = findChildOfType(directive, OoRexxKeywordExpose.class);
        if(expose != null){
            extractExposedVariables(ooRexxMethod, expose);
        }

        OoRexxKeywordUse use = findChildOfType(directive, OoRexxKeywordUse.class);
        if(use != null){
            ooRexxMethod.setUseArg(extractArgVariables(settings, use));
        }

        OoRexxKeywordParse parseArg = findChildOfType(directive, OoRexxKeywordParse.class);
        if(parseArg != null){
            ooRexxMethod.setParseArg(extractArgVariables(parseArg));
        }

        OoRexxDirectiveMethodAccess access = findChildOfType(directive, OoRexxDirectiveMethodAccess.class);
        if(access != null){
            ooRexxMethod.setAccessModifier(AccessModifier.fromText(access.getText()));
        }

        OoRexxDirectiveMethodClass classMethod = findChildOfType(directive, OoRexxDirectiveMethodClass.class);
        if(classMethod != null){
            ooRexxMethod.setClassMethod(true);
        }

        OoRexxDirectiveMethodAttribute attributeMethod = findChildOfType(directive, OoRexxDirectiveMethodAttribute.class);
        if(attributeMethod != null){
            ooRexxMethod.setAttributeMethod(true);
        }

        OoRexxDirectiveMethodGuardedUnguarded guardedOrUnguarded = findChildOfType(directive, OoRexxDirectiveMethodGuardedUnguarded.class);
        if(guardedOrUnguarded != null && "unguarded".equalsIgnoreCase(guardedOrUnguarded.getText())){
            ooRexxMethod.setGuarded(false);
        }

        OoRexxDirectiveMethodUnprotectedProtected protectedOrUnprotected = findChildOfType(directive, OoRexxDirectiveMethodUnprotectedProtected.class);
        if(protectedOrUnprotected != null && "protected".equalsIgnoreCase(protectedOrUnprotected.getText())){
            ooRexxMethod.setUnprotected(false);
        }

        OoRexxDirectiveMethodAbstract abstracteMethod = findChildOfType(directive, OoRexxDirectiveMethodAbstract.class);
        if(abstracteMethod != null){
            ooRexxMethod.setAbstractMethod(true);
        }

        OoRexxDirectiveMethodExternal external = findChildOfType(directive, OoRexxDirectiveMethodExternal.class);
        if(external != null){
            String text = external.getStringLit().getText();
            ooRexxMethod.setExternal(text);
        }

        OoRexxDirectiveMethodDelegate delegate = findChildOfType(directive, OoRexxDirectiveMethodDelegate.class);
        if(delegate != null){
            String text = delegate.getSymbolOrString().getText();
            ooRexxMethod.setDelegate(text);
        }

        if(settings.isIncludeVariableReferences()) {
            Collection<OoRexxVariableReferenceExpr> references = findChildrenOfType(directive, OoRexxVariableReferenceExpr.class);
            List<String> stringReferences
                    = references.stream().map(PsiElement::getText).collect(Collectors.toList());

            ooRexxMethod.setVariableReferences(stringReferences);
        }

        setComment(ooRexxMethod, directive);

        return ooRexxMethod;
    }

    private void extractExposedVariables(OoRexxMethod result, OoRexxKeywordExpose keywordExpose){
        Collection<OoRexxSymbolOrString> ooRexxSymbolOrStrings = findChildrenOfType(keywordExpose, OoRexxSymbolOrString.class);
        ooRexxSymbolOrStrings.forEach(ooRexxSymbolOrString -> result.getExposedVariables().add(ooRexxSymbolOrString.getText()));
    }

}

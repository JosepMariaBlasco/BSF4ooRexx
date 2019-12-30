package org.oorexx.doc.analyse;

import com.intellij.psi.util.PsiTreeUtil;
import org.oorexx.doc.analyse.model.AccessModifier;
import org.oorexx.doc.analyse.model.OoRexxAttribute;
import org.oorexx.doc.analyse.model.OoRexxMethod;

import org.oorexx.ide.file.OoRexxFile;
import org.oorexx.ide.lang.psi.*;

import java.util.Collection;

import static com.intellij.psi.util.PsiTreeUtil.findChildOfType;
import static com.intellij.psi.util.PsiTreeUtil.findChildrenOfType;

public class MethodAnalyzer extends Analyzer{

    public MethodAnalyzer(CommentContext commentContext) {
        super(commentContext);
    }

    public OoRexxMethod analyze(OoRexxDirectiveMethod directive){

        OoRexxFile parentOfType = PsiTreeUtil.getParentOfType(directive, OoRexxFile.class);

        OoRexxMethod ooRexxMethod = new OoRexxMethod(directive);

        OoRexxDirectiveMethodName name = findChildOfType(directive, OoRexxDirectiveMethodName.class);

        ooRexxMethod.setName(name.getText());

        OoRexxKeywordExpose expose = findChildOfType(directive, OoRexxKeywordExpose.class);

        if(expose != null){
            extractExposedVariables(ooRexxMethod, expose);
        }

        OoRexxKeywordUse use = findChildOfType(directive, OoRexxKeywordUse.class);

        if(use != null){
            ooRexxMethod.setUseArg(extractArgVariables(use));
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


        setComment(ooRexxMethod, directive);

        return ooRexxMethod;
    }

    private void extractExposedVariables(OoRexxMethod result, OoRexxKeywordExpose keywordExpose){
        Collection<OoRexxSymbolOrString> ooRexxSymbolOrStrings = findChildrenOfType(keywordExpose, OoRexxSymbolOrString.class);
        ooRexxSymbolOrStrings.forEach(ooRexxSymbolOrString -> result.getExposedVariables().add(ooRexxSymbolOrString.getText()));
    }

}

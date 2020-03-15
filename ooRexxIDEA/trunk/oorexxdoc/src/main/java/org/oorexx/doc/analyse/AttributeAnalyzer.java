package org.oorexx.doc.analyse;

import org.oorexx.doc.analyse.model.AccessModifier;
import org.oorexx.doc.analyse.model.OoRexxAttribute;
import org.oorexx.doc.generate.Settings;
import org.oorexx.ide.lang.psi.*;

import static com.intellij.psi.util.PsiTreeUtil.findChildOfType;

public class AttributeAnalyzer extends Analyzer{

    public AttributeAnalyzer(Settings settings, CommentContext commentContext) {
        super(settings, commentContext);
    }

    public OoRexxAttribute analyze(OoRexxDirectiveAttribute directive){
        OoRexxAttribute ooRexxAttribute = new OoRexxAttribute(directive);

        if(directive.getSymbolOrString() != null) {
            String name = directive.getSymbolOrString().getText();

            ooRexxAttribute.setName(name);
        }

        OoRexxDirectiveAttributeAccess access = findChildOfType(directive, OoRexxDirectiveAttributeAccess.class);

        if(access != null){
            ooRexxAttribute.setAccessModifier(AccessModifier.fromText(access.getText()));
        }

        OoRexxDirectiveAttributeClass classMethod = findChildOfType(directive, OoRexxDirectiveAttributeClass.class);

        if(classMethod != null){
            ooRexxAttribute.setClassMethod(true);
        }

        OoRexxDirectiveAttributeGetSet getterOrSetter = findChildOfType(directive, OoRexxDirectiveAttributeGetSet.class);

        if(getterOrSetter != null){
            ooRexxAttribute.setType(OoRexxAttribute.Type.fromText(getterOrSetter.getText()));
        }

        OoRexxDirectiveAttributeGuardUnguarded guardedOrUnguarded = findChildOfType(directive, OoRexxDirectiveAttributeGuardUnguarded.class);

        if(guardedOrUnguarded != null && "unguarded".equalsIgnoreCase(guardedOrUnguarded.getText())){
            ooRexxAttribute.setGuarded(false);
        }

        OoRexxDirectiveAttributeUnprotectedProtected protectedOrUnprotected = findChildOfType(directive, OoRexxDirectiveAttributeUnprotectedProtected.class);

        if(protectedOrUnprotected != null && "protected".equalsIgnoreCase(protectedOrUnprotected.getText())){
            ooRexxAttribute.setUnprotected(false);
        }

        OoRexxDirectiveAttributeAbstract abstracteMethod = findChildOfType(directive, OoRexxDirectiveAttributeAbstract.class);

        if(abstracteMethod != null){
            ooRexxAttribute.setAbstractMethod(true);
        }

        OoRexxDirectiveAttributeExternal external = findChildOfType(directive, OoRexxDirectiveAttributeExternal.class);

        if(external != null){
            String text = external.getStringLit().getText();
            ooRexxAttribute.setExternal(text);
        }

        OoRexxDirectiveAttributeDelegate delegate = findChildOfType(directive, OoRexxDirectiveAttributeDelegate.class);

        if(delegate != null){
            String text = delegate.getSymbolOrString().getText();
            ooRexxAttribute.setDelegate(text);
        }

        OoRexxKeywordUse use = findChildOfType(directive, OoRexxKeywordUse.class);

        if(use != null){
            ooRexxAttribute.setUseArg(extractArgVariables(use));
        }

        OoRexxKeywordParse parseArg = findChildOfType(directive, OoRexxKeywordParse.class);

        if(parseArg != null){
            ooRexxAttribute.setParseArg(extractArgVariables(parseArg));
        }

        setComment(ooRexxAttribute, directive);

        return ooRexxAttribute;
    }

}

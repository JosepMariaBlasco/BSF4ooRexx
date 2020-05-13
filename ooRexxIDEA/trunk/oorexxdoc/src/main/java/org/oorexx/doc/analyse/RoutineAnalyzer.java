package org.oorexx.doc.analyse;

import com.intellij.psi.PsiElement;
import org.oorexx.doc.analyse.model.AccessModifier;
import org.oorexx.doc.analyse.model.OoRexxRoutine;
import org.oorexx.doc.generate.Settings;
import org.oorexx.ide.lang.psi.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.intellij.psi.util.PsiTreeUtil.findChildOfType;
import static com.intellij.psi.util.PsiTreeUtil.findChildrenOfType;

public class RoutineAnalyzer extends Analyzer{

    public RoutineAnalyzer(Settings settings, CommentContext commentContext) {
        super(settings, commentContext);
    }

    public OoRexxRoutine analyze(OoRexxDirectiveRoutine directive){
        OoRexxRoutine ooRexxRoutine = new OoRexxRoutine(directive);

        OoRexxSymbolOrString symbolOrString = directive.getSymbolOrString();
        AccessModifier accessModifier = AccessModifier.fromText(getText(directive, OoRexxDirectiveAccess.class));
        ooRexxRoutine.setName(symbolOrString.getText());
        ooRexxRoutine.setAccessModifier(accessModifier);

        OoRexxDirectiveRoutineExternal element = findChildOfType(directive, OoRexxDirectiveRoutineExternal.class);

        if(element != null){
            OoRexxSymbolOrString externalSpec = element.getSymbolOrString();

            if(externalSpec != null){
                ooRexxRoutine.setExternal(externalSpec.getText());
            }
        }

        OoRexxKeywordUse use = findChildOfType(directive, OoRexxKeywordUse.class);

        if(use != null){
            ooRexxRoutine.setUseArg(extractArgVariables(settings, use));
        }

        OoRexxKeywordParse parseArg = findChildOfType(directive, OoRexxKeywordParse.class);

        if(parseArg != null){
            ooRexxRoutine.setParseArg(extractArgVariables(parseArg));
        }

        setComment(ooRexxRoutine, directive);

        if(settings.isIncludeVariableReferences()) {
            Collection<OoRexxVariableReferenceExpr> references = findChildrenOfType(directive, OoRexxVariableReferenceExpr.class);
            List<String> stringReferences
                    = references.stream().map(PsiElement::getText).collect(Collectors.toList());

            ooRexxRoutine.setVariableReferences(stringReferences);
        }

        return ooRexxRoutine;
    }

}

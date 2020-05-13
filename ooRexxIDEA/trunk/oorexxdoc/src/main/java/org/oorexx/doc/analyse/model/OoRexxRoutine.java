package org.oorexx.doc.analyse.model;

import com.intellij.psi.PsiElement;

import java.util.ArrayList;
import java.util.List;

public class OoRexxRoutine extends OoRexxNamedElement{

    private String useArg = "";
    private String parseArg;
    private AccessModifier accessModifier;
    private String external;
    private List<String> variableReferences = new ArrayList<>();

    public OoRexxRoutine(PsiElement psiElement) {
        super(psiElement);
    }

    public AccessModifier getAccessModifier() {
        return accessModifier;
    }

    public void setAccessModifier(AccessModifier accessModifier) {
        this.accessModifier = accessModifier;
    }

    public String getExternal() {
        return external;
    }

    public void setExternal(String external) {
        this.external = external;
    }

    public String getUseArg() {
        return useArg;
    }

    public void setUseArg(String useArg) {
        this.useArg = useArg;
    }

    public String getParseArg() {
        return parseArg;
    }

    public void setParseArg(String parseArg) {
        this.parseArg = parseArg;
    }

    public List<String> getVariableReferences() {
        return variableReferences;
    }

    public void setVariableReferences(List<String> variableReferences) {
        this.variableReferences = variableReferences;
    }
}

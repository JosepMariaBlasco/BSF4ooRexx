package org.oorexx.doc.analyse.model;

import com.intellij.psi.PsiElement;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class OoRexxMethod extends OoRexxNamedElement {


    private AccessModifier accessModifier = AccessModifier.PUBLIC;
    private boolean guarded = true;
    private boolean unprotected = true;
    private boolean attributeMethod = false;
    private boolean classMethod = false;
    private boolean abstractMethod = false;
    private String delegate;
    private String external;

    private List<String> exposedVariables = new ArrayList<>();
    private String useArg = "";
    private String parseArg;

    public OoRexxMethod(PsiElement psiElement) {
        super(psiElement);
    }

    public String getExposedString(){
        if(hasExpose()){
            return StringUtils.join(exposedVariables, " ");
        }

        return "";
    }

    public boolean hasExpose(){
        return exposedVariables.size() > 0;
    }

    public List<String> getExposedVariables() {
        return exposedVariables;
    }

    public void setExposedVariables(List<String> exposedVariables) {
        this.exposedVariables = exposedVariables;
    }

    public String getUseArg() {
        return useArg;
    }

    public void setUseArg(String useArg) {
        this.useArg = useArg;
    }

    public AccessModifier getAccessModifier() {
        return accessModifier;
    }

    public void setAccessModifier(AccessModifier accessModifier) {
        this.accessModifier = accessModifier;
    }

    public boolean isGuarded() {
        return guarded;
    }

    public void setGuarded(boolean guarded) {
        this.guarded = guarded;
    }

    public boolean isUnprotected() {
        return unprotected;
    }

    public void setUnprotected(boolean unprotected) {
        this.unprotected = unprotected;
    }

    public boolean isAttributeMethod() {
        return attributeMethod;
    }

    public void setAttributeMethod(boolean attributeMethod) {
        this.attributeMethod = attributeMethod;
    }

    public boolean isClassMethod() {
        return classMethod;
    }

    public void setClassMethod(boolean classMethod) {
        this.classMethod = classMethod;
    }

    public boolean isAbstractMethod() {
        return abstractMethod;
    }

    public void setAbstractMethod(boolean abstractMethod) {
        this.abstractMethod = abstractMethod;
    }

    public String getDelegate() {
        return delegate;
    }

    public void setDelegate(String delegate) {
        this.delegate = delegate;
    }

    public String getExternal() {
        return external;
    }

    public void setExternal(String external) {
        this.external = external;
    }

    public String getParseArg() {
        return parseArg;
    }

    public void setParseArg(String parseArg) {
        this.parseArg = parseArg;
    }
}

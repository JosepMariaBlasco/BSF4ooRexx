package org.oorexx.doc.analyse.model;

import com.intellij.psi.PsiElement;

public class OoRexxAttribute extends OoRexxNamedElement{

    private AccessModifier accessModifier = AccessModifier.PUBLIC;
    private boolean guarded = true;
    private boolean unprotected = true;
    private Type type = Type.BOTH;
    private boolean classMethod = false;
    private boolean abstractMethod = false;
    private String delegate;
    private String external;
    private String useArg;
    private String parseArg;

    public OoRexxAttribute(PsiElement psiElement) {
        super(psiElement);
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

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
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

    public enum Type {
        GETTER, SETTER, BOTH;

        public static Type fromText(String text) {
            if("get".equalsIgnoreCase(text)){
                return GETTER;
            }

            if("set".equalsIgnoreCase(text)){
                return SETTER;
            }

            return BOTH;
        }
    }
}

package org.oorexx.doc.analyse.model;

import com.intellij.psi.PsiElement;

public class OoRexxConstant extends OoRexxNamedElement {

    private String value;

    public OoRexxConstant(PsiElement psiElement) {
        super(psiElement);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

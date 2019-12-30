package org.oorexx.doc.analyse.model;

import com.intellij.psi.PsiElement;

public class OoRexxRequires extends OoRexxElement {

    private String path;
    private OoRexxPackage requiredPackage;

    public OoRexxRequires(PsiElement psiElement) {
        super(psiElement);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public OoRexxPackage getRequiredPackage() {
        return requiredPackage;
    }

    public void setRequiredPackage(OoRexxPackage requiredPackage) {
        this.requiredPackage = requiredPackage;
    }

    public String getReference(){
        if(requiredPackage == null){
            return path;
        }

        String href = "../packages/" + requiredPackage.getName();
        return String.format("<a href=\"%s.html\">%s</a>", href, requiredPackage.getName());
    }
}

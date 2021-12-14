package org.oorexx.doc.analyse.model;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.intellij.psi.PsiElement;
import org.apache.commons.text.StringEscapeUtils;
import org.oorexx.doc.util.IdGenerator;

public class OoRexxNamedElement extends OoRexxElement {

    private String name;
    private OoRexxNamedElement parent;
    private List<OoRexxAnnotation> annotations = new ArrayList<>();
    private String id;

    public OoRexxNamedElement(PsiElement psiElement) {
        super(psiElement);
    }

    public String getId() {
        return id;
    }

    public OoRexxNamedElement getParent() {
        return parent;
    }

    public void setParent(OoRexxNamedElement parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name.contains("\"")) {
            name = name.replace("\"", "");
        }
        this.name = name;
        if (this.id == null) {
            this.id = IdGenerator.getId(name);
        }
    }

    public String getSanitizedName() {
        String sanitizedName = getName();
        if (sanitizedName == null
                || (!sanitizedName.startsWith("\"") && !sanitizedName.startsWith("'"))
        ) {
            return this.name;
        }
        return sanitizedName.substring(1, sanitizedName.length() - 1);
    }

    public String getHtmlName() {
        return StringEscapeUtils.escapeHtml4(getName());
    }

    public String getJsName() {
        return StringEscapeUtils.escapeJava(name);
    }

    public List<OoRexxAnnotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<OoRexxAnnotation> annotations) {
        this.annotations = annotations;
    }

    public List<OoRexxNamedElement> getAllAnnotateableElements() {
        return Arrays.asList(this);
    }
}

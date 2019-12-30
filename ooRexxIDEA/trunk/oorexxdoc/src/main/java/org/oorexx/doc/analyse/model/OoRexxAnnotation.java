package org.oorexx.doc.analyse.model;


import com.intellij.psi.PsiElement;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class OoRexxAnnotation extends OoRexxNamedElement{

    private AnnotationType annotationType;
    private Map<String, String> data = new HashMap<>();

    public OoRexxAnnotation(PsiElement psiElement) {
        super(psiElement);
    }

    public AnnotationType getAnnotationType() {
        return annotationType;
    }

    public void setAnnotationType(AnnotationType annotationType) {
        this.annotationType = annotationType;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public enum AnnotationType {
        ATTRIBUTE (OoRexxAttribute.class), CLASS (OoRexxClass.class), CONSTANT (OoRexxConstant.class), METHOD (OoRexxMethod.class), PACKAGE (OoRexxPackage.class), ROUTINE (OoRexxRoutine.class);

        private Class<? extends OoRexxNamedElement> type;

        AnnotationType(Class<? extends OoRexxNamedElement> type) {
            this.type = type;
        }

        public static AnnotationType fromText(String text){
            for(AnnotationType annotationType: values()){
                if(StringUtils.startsWithIgnoreCase(text, annotationType.name())){
                    return annotationType;
                }
            }
            return null;
        }

        public Class<? extends OoRexxNamedElement> getType() {
            return type;
        }


    }

    public String dataToString(){
        return data.toString();
    }

    public boolean appliesTo(OoRexxNamedElement ooRexxNamedElement){
        return ooRexxNamedElement.getName().equals(getName()) && getAnnotationType().getType().isInstance(ooRexxNamedElement);
    }
}

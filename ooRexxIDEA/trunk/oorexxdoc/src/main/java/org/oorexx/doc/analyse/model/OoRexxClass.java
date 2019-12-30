package org.oorexx.doc.analyse.model;

import com.intellij.psi.PsiElement;
import org.oorexx.doc.analyse.model.reference.ClassReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class OoRexxClass extends OoRexxNamedElement {

    private OoRexxPackage ooRexxPackage;
    private ClassReference metaClass;
    private List<ClassReference> mixinClasses = new ArrayList<>();
    private OoRexxMethod initMethod;
    private List<OoRexxMethod> methods = new ArrayList<>();
    private ClassReference subClass;
    private List<ClassReference> inheritClasses = new ArrayList<>();
    private List<OoRexxConstant> constants = new ArrayList<>();
    private List<OoRexxAttribute> attributes = new ArrayList<>();
    private boolean abstractClass;
    private AccessModifier accessModifier = AccessModifier.PRIVATE;

    public OoRexxClass(PsiElement psiElement) {
        super(psiElement);
    }

    public List<OoRexxMethod> getMethods() {
        return methods;
    }

    public void setMethods(List<OoRexxMethod> methods) {
        this.methods = methods;
    }

    public OoRexxMethod getInitMethod() {
        return initMethod;
    }

    public void setInitMethod(OoRexxMethod initMethod) {
        this.initMethod = initMethod;
    }

    public ClassReference getMetaClass() {
        return metaClass;
    }

    public void setMetaClass(ClassReference metaClass) {
        this.metaClass = metaClass;
    }

    public List<ClassReference> getMixinClasses() {
        return mixinClasses;
    }

    public void setMixinClasses(List<ClassReference> mixinClasses) {
        this.mixinClasses = mixinClasses;
    }

    public ClassReference getSubClass() {
        return subClass;
    }

    public void setSubClass(ClassReference subClass) {
        this.subClass = subClass;
    }

    public List<ClassReference> getInheritClasses() {
        return inheritClasses;
    }

    public void setInheritClasses(List<ClassReference> inheritClasses) {
        this.inheritClasses = inheritClasses;
    }

    public boolean isAbstractClass() {
        return abstractClass;
    }

    public void setAbstractClass(boolean abstractClass) {
        this.abstractClass = abstractClass;
    }

    public AccessModifier getAccessModifier() {
        return accessModifier;
    }

    public void setAccessModifier(AccessModifier accessModifier) {
        this.accessModifier = accessModifier;
    }

    public OoRexxPackage getOoRexxPackage() {
        return ooRexxPackage;
    }

    public void setOoRexxPackage(OoRexxPackage ooRexxPackage) {
        this.ooRexxPackage = ooRexxPackage;
    }

    public List<OoRexxConstant> getConstants() {
        return constants;
    }

    public void setConstants(List<OoRexxConstant> constants) {
        this.constants = constants;
    }

    public List<OoRexxAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<OoRexxAttribute> attributes) {
        this.attributes = attributes;
    }

    @Override
    public List<OoRexxNamedElement> getAllAnnotateableElements() {
        ArrayList<OoRexxNamedElement> list = new ArrayList<>();
        list.add(this);
        list.add(initMethod);
        list.addAll(methods);
        list.addAll(constants);
        list.addAll(attributes);
        return list.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }
}

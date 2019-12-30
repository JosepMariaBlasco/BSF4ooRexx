package org.oorexx.doc.analyse.model;

import com.intellij.psi.PsiElement;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class OoRexxPackage extends OoRexxNamedElement implements Comparable<OoRexxPackage> {

    private File file;
    private List<OoRexxClass> classes = new ArrayList<>();
    private List<OoRexxMethod> methods = new ArrayList<>();
    private List<OoRexxRequires> requires = new ArrayList<>();
    private List<OoRexxOption> options = new ArrayList<>();
    private List<OoRexxResource> resources = new ArrayList<>();
    private List<OoRexxRoutine> routines = new ArrayList<>();
    private List<OoRexxPackage> requiredPackages = new ArrayList<>();
    private Set<OoRexxPackage> requiredBy = new TreeSet<>();

    public OoRexxPackage(PsiElement psiElement) {
        super(psiElement);
    }

    public boolean hasElements(){
        return true;
    }

    public List<OoRexxClass> getClasses() {
        return classes;
    }

    public void setClasses(List<OoRexxClass> classes) {
        this.classes = classes;
    }

    public List<OoRexxMethod> getMethods() {
        return methods;
    }

    public void setMethods(List<OoRexxMethod> methods) {
        this.methods = methods;
    }

    public List<OoRexxRequires> getRequires() {
        return requires;
    }

    public void setRequires(List<OoRexxRequires> requires) {
        this.requires = requires;
    }

    public List<OoRexxOption> getOptions() {
        return options;
    }

    public void setOptions(List<OoRexxOption> options) {
        this.options = options;
    }

    public List<OoRexxResource> getResources() {
        return resources;
    }

    public void setResources(List<OoRexxResource> resources) {
        this.resources = resources;
    }

    public List<OoRexxRoutine> getRoutines() {
        return routines;
    }

    public void setRoutines(List<OoRexxRoutine> routines) {
        this.routines = routines;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public List<OoRexxPackage> getRequiredPackages() {
        return requiredPackages;
    }

    public void setRequiredPackages(List<OoRexxPackage> requiredPackages) {
        this.requiredPackages = requiredPackages;
    }

    public Set<OoRexxPackage> getRequiredBy() {
        return requiredBy;
    }

    public void setRequiredBy(Set<OoRexxPackage> requiredBy) {
        this.requiredBy = requiredBy;
    }

    @Override
    public List<OoRexxNamedElement> getAllAnnotateableElements() {
        ArrayList<OoRexxNamedElement> list = new ArrayList<>();
        list.add(this);
        classes.forEach(ooRexxClass -> list.addAll(ooRexxClass.getAllAnnotateableElements()));
        list.addAll(routines);
        list.addAll(methods);
        return list.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    public List<OoRexxNamedElement> getAllNonClassAnnotateableElements(){
        ArrayList<OoRexxNamedElement> list = new ArrayList<>();
        list.add(this);
        list.addAll(routines);
        list.addAll(methods);
        return list.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OoRexxPackage that = (OoRexxPackage) o;

        return file != null ? file.equals(that.file) : that.file == null;
    }

    @Override
    public int hashCode() {
        return file != null ? file.hashCode() : 0;
    }

    @Override
    public int compareTo(OoRexxPackage o) {
        return this.equals(o) ? 0 : this.getName().compareTo(o.getName());
    }
}

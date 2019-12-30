package org.oorexx.doc.analyse;

import org.oorexx.doc.analyse.model.OoRexxClass;
import org.oorexx.doc.analyse.model.OoRexxPackage;
import org.oorexx.doc.analyse.model.OoRexxRequires;
import org.oorexx.doc.analyse.model.reference.ClassReference;
import org.oorexx.doc.analyse.model.reference.LinkedClassReference;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ClassReferenceAnalyzer {


    public void setReferences(List<OoRexxPackage> packages){
        setAvailablePackages(packages);
        linkClasses(packages);
    }

    private void linkClasses(List<OoRexxPackage> packages){
        packages.stream()
                .flatMap(pckg -> pckg.getClasses().stream())
                .forEach(ooRexxClass -> {
                    ooRexxClass.setMetaClass(fixReference(ooRexxClass.getOoRexxPackage(), ooRexxClass.getMetaClass()));
                    ooRexxClass.setSubClass(fixReference(ooRexxClass.getOoRexxPackage(), ooRexxClass.getSubClass()));
                    ooRexxClass.setMixinClasses(ooRexxClass.getMixinClasses().stream().map(c -> fixReference(ooRexxClass.getOoRexxPackage(), c)).collect(Collectors.toList()));
                    ooRexxClass.setInheritClasses(ooRexxClass.getInheritClasses().stream().map(c -> fixReference(ooRexxClass.getOoRexxPackage(), c)).collect(Collectors.toList()));
                });
    }

    private ClassReference fixReference(OoRexxPackage ooRexxPackage, ClassReference classReference){
        if(classReference == null){
            return null;
        }

        Optional<OoRexxClass> ooRexxClassOptional = searchForClass(ooRexxPackage, classReference.getName());

        if(ooRexxClassOptional.isPresent()){
            return new LinkedClassReference(ooRexxClassOptional.get());
        }

        return classReference;
    }

    private void setAvailablePackages(List<OoRexxPackage> packages){
        packages.forEach(ooRexxPackage -> packages.stream()
                .filter(p -> !p.equals(ooRexxPackage))
                .forEach(test -> {
                    Optional<OoRexxRequires> any = ooRexxPackage.getRequires()
                            .stream()
                            .filter(ooRexxRequires -> hasMatch(ooRexxPackage, ooRexxRequires, test)).findFirst();
                    if(any.isPresent()){
                        any.get().setRequiredPackage(ooRexxPackage);
                        ooRexxPackage.getRequiredPackages().add(test);
                    } else {
                        Optional<OoRexxRequires> first = ooRexxPackage.getRequires().stream().filter(ooRexxRequires -> test.getName().equalsIgnoreCase(ooRexxRequires.getPath())).findFirst();
                        if(first.isPresent()) {
                            first.get().setRequiredPackage(test);
                            ooRexxPackage.getRequiredPackages().add(test);
                            test.getRequiredBy().add(ooRexxPackage);
                        }
                    }
                }));
    }

    private boolean hasMatch(OoRexxPackage ooRexxPackage, OoRexxRequires ooRexxRequires, OoRexxPackage testPackage){
        try {
            return getRelativePath(ooRexxPackage.getFile(), ooRexxRequires.getPath()).getCanonicalPath().equalsIgnoreCase(testPackage.getFile().getCanonicalPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File getRelativePath(File packageFile, String requiresPath){
        return new File(packageFile.getParent(), requiresPath);
    }

    private Optional<OoRexxClass> searchForClass(OoRexxPackage ooRexxPackage, String name){
        for(OoRexxClass ooRexxClass: ooRexxPackage.getClasses()){
            if(ooRexxClass.getName().equalsIgnoreCase(name)){
                return Optional.of(ooRexxClass);
            }
        }

        for(OoRexxPackage requiredPackage: ooRexxPackage.getRequiredPackages()){
            if(!requiredPackage.getClasses().isEmpty()){
                Optional<OoRexxClass> optional = searchForClass(requiredPackage, name);
                if(optional.isPresent()){
                    return optional;
                }
            }
        }

        return Optional.empty();
    }

}

package org.oorexx.doc.analyse;

import org.oorexx.doc.analyse.model.*;
import org.oorexx.doc.analyse.model.reference.ClassReference;
import org.oorexx.doc.analyse.model.reference.LinkedClassReference;
import org.oorexx.ide.lang.psi.*;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.intellij.psi.util.PsiTreeUtil.findChildOfType;
import static com.intellij.psi.util.PsiTreeUtil.findChildrenOfType;

public class ClassAnalyzer extends Analyzer{

    private MethodAnalyzer methodAnalyzer;
    private ConstantAnalyzer constantAnalyzer;
    private OoRexxDirectiveClass directiveClass;
    private AttributeAnalyzer attributeAnalyzer;
    private AnnotationAnalyzer annotationAnalyzer;

    public ClassAnalyzer(CommentContext commentContext) {
        super(commentContext);

        methodAnalyzer = new MethodAnalyzer(commentContext);
        constantAnalyzer = new ConstantAnalyzer(commentContext);
        attributeAnalyzer = new AttributeAnalyzer(commentContext);
        annotationAnalyzer = new AnnotationAnalyzer(commentContext);
    }

    public OoRexxClass analyze(OoRexxPackage ooRexxPackage, OoRexxDirectiveClass directiveClass) {
        this.directiveClass = directiveClass;
        Collection<OoRexxDirectiveMethod> methods = findChildrenOfType(directiveClass, OoRexxDirectiveMethod.class);
        Collection<OoRexxDirectiveAttribute> attributes = findChildrenOfType(directiveClass, OoRexxDirectiveAttribute.class);
        Collection<OoRexxDirectiveAnnotate> annotates = findChildrenOfType(directiveClass, OoRexxDirectiveAnnotate.class);

        OoRexxClass ooRexxClass = new OoRexxClass(directiveClass);

        ooRexxClass.setOoRexxPackage(ooRexxPackage);

        ooRexxClass.setName(getText(directiveClass, OoRexxDirectiveClassName.class));
        ooRexxClass.setMetaClass(getMetaClass());
        ooRexxClass.setMixinClasses(getMixinClasses());
        ooRexxClass.setInheritClasses(getInheritClasses());
        ooRexxClass.setSubClass(getSubClass());
        ooRexxClass.setAbstractClass(isAbstract());
        ooRexxClass.setConstants(getConstants());

        AccessModifier accessModifier = getAccessModifier();

        if(accessModifier != null){
            ooRexxClass.setAccessModifier(accessModifier);
        }

        List<OoRexxMethod> methodAnalyzingResults = analyzeMethods(methods);
        ooRexxClass.setMethods(methodAnalyzingResults);

        Optional<OoRexxMethod> init = methodAnalyzingResults.stream().filter(methodAnalyzingResult -> methodAnalyzingResult.getName().equalsIgnoreCase("init")).findFirst();
        init.ifPresent(ooRexxClass::setInitMethod);

        List<OoRexxAttribute> attributeAnalyzingResult = analyzeAttributes(attributes);

        ooRexxClass.setAttributes(attributeAnalyzingResult);

        setComment(ooRexxClass, directiveClass);

        List<OoRexxAnnotation> annotations = analyzeAnnotations(ooRexxPackage, annotates);

        List<OoRexxNamedElement> allAnnotateableElements = ooRexxClass.getAllAnnotateableElements();

        annotations.forEach(ooRexxAnnotation -> {
            Optional<OoRexxNamedElement> any = allAnnotateableElements.stream().filter(ooRexxAnnotation::appliesTo).findAny();
            if(any.isPresent()){
                OoRexxNamedElement ooRexxNamedElement = any.get();
                ooRexxNamedElement.getAnnotations().add(ooRexxAnnotation);
            }
        });

        ooRexxClass.getMethods().removeIf(method -> AccessModifier.PRIVATE.equals(method.getAccessModifier()));
        ooRexxClass.getAttributes().removeIf(attribute -> AccessModifier.PRIVATE.equals(attribute.getAccessModifier()));

        return ooRexxClass;
    }

    private List<OoRexxAnnotation> analyzeAnnotations(OoRexxPackage ooRexxPackage,Collection<OoRexxDirectiveAnnotate> annotates) {
        return annotates.stream().map(directive -> annotationAnalyzer.analyze(ooRexxPackage, directive)).collect(Collectors.toList());
    }

    private List<OoRexxAttribute> analyzeAttributes(Collection<OoRexxDirectiveAttribute> attributes) {
        return attributes.stream().map(directive -> attributeAnalyzer.analyze(directive)).collect(Collectors.toList());
    }

    private List<OoRexxConstant> getConstants(){
        return findChildrenOfType(directiveClass, OoRexxDirectiveConstant.class)
                .stream()
                .map(directive -> constantAnalyzer.analyze(directive))
                .collect(Collectors.toList());
    }


    private ClassReference getMetaClass(){
        String name = getText(directiveClass, OoRexxDirectiveClassMetaclassName.class);
        return createClassReference(name);
    }

    private ClassReference getSubClass() {
        String name = getText(directiveClass, OoRexxDirectiveClassSubclassName.class);
        return createClassReference(name);
    }

    private List<ClassReference> getMixinClasses(){
        Collection<OoRexxDirectiveClassMixinclassName> mixins = findChildrenOfType(directiveClass, OoRexxDirectiveClassMixinclassName.class);
        return mixins.stream().map(name -> name.getText() == null ? null : new ClassReference(name.getText()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<OoRexxMethod> analyzeMethods(Collection<OoRexxDirectiveMethod> directiveMethods) {
        return directiveMethods.stream()
                .filter(hasDefaultPublicAccessWithParent(OoRexxDirectiveMethod.class))
                .map(method -> methodAnalyzer.analyze(method))
                .collect(Collectors.toList());
    }

    private List<ClassReference> getInheritClasses() {
        Collection<OoRexxDirectiveClassInheritName> inherit = findChildrenOfType(directiveClass, OoRexxDirectiveClassInheritName.class);
        return inherit.stream().map(name -> name.getText() == null ? null : new ClassReference(name.getText()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private boolean isAbstract() {
        OoRexxDirectiveClassAbstract name = findChildOfType(directiveClass, OoRexxDirectiveClassAbstract.class);
        return name != null;
    }

    private AccessModifier getAccessModifier(){
        OoRexxDirectiveClassAccess access = findChildOfType(directiveClass, OoRexxDirectiveClassAccess.class);
        OoRexxDirectiveAccessPublic publicAccess = findChildOfType(access, OoRexxDirectiveAccessPublic.class);

        if(publicAccess != null){
            return AccessModifier.PUBLIC;
        }

        OoRexxDirectiveAccessPrivate privateAccess = findChildOfType(access, OoRexxDirectiveAccessPrivate.class);

        if(privateAccess != null){
            return AccessModifier.PRIVATE;
        }

        return null;
    }

    private ClassReference createClassReference(String name){
        return name != null ? new ClassReference(name) : null;
    }
}

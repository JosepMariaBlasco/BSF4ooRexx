package org.oorexx.doc.analyse;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.oorexx.doc.analyse.model.AccessModifier;
import org.oorexx.doc.analyse.model.OoRexxAnnotation;
import org.oorexx.doc.analyse.model.OoRexxAttribute;
import org.oorexx.doc.analyse.model.OoRexxClass;
import org.oorexx.doc.analyse.model.OoRexxConstant;
import org.oorexx.doc.analyse.model.OoRexxMethod;
import org.oorexx.doc.analyse.model.OoRexxNamedElement;
import org.oorexx.doc.analyse.model.OoRexxPackage;
import org.oorexx.doc.analyse.model.reference.ClassReference;
import org.oorexx.doc.generate.Settings;
import org.oorexx.ide.lang.psi.OoRexxDirectiveAccessPrivate;
import org.oorexx.ide.lang.psi.OoRexxDirectiveAccessPublic;
import org.oorexx.ide.lang.psi.OoRexxDirectiveAnnotate;
import org.oorexx.ide.lang.psi.OoRexxDirectiveAttribute;
import org.oorexx.ide.lang.psi.OoRexxDirectiveClass;
import org.oorexx.ide.lang.psi.OoRexxDirectiveClassAbstract;
import org.oorexx.ide.lang.psi.OoRexxDirectiveClassAccess;
import org.oorexx.ide.lang.psi.OoRexxDirectiveClassInheritName;
import org.oorexx.ide.lang.psi.OoRexxDirectiveClassMetaclassName;
import org.oorexx.ide.lang.psi.OoRexxDirectiveClassMixinclassName;
import org.oorexx.ide.lang.psi.OoRexxDirectiveClassName;
import org.oorexx.ide.lang.psi.OoRexxDirectiveClassSubclassName;
import org.oorexx.ide.lang.psi.OoRexxDirectiveConstant;
import org.oorexx.ide.lang.psi.OoRexxDirectiveMethod;

import static com.intellij.psi.util.PsiTreeUtil.findChildOfType;
import static com.intellij.psi.util.PsiTreeUtil.findChildrenOfType;
import static org.oorexx.doc.analyse.model.OoRexxMethod.METHOD_COMPARATOR;

public class ClassAnalyzer extends Analyzer {

    private final MethodAnalyzer methodAnalyzer;
    private final ConstantAnalyzer constantAnalyzer;
    private final AttributeAnalyzer attributeAnalyzer;
    private final AnnotationAnalyzer annotationAnalyzer;
    private OoRexxDirectiveClass directiveClass;

    public ClassAnalyzer(Settings settings, CommentContext commentContext) {
        super(settings, commentContext);

        methodAnalyzer = new MethodAnalyzer(settings, commentContext);
        constantAnalyzer = new ConstantAnalyzer(settings, commentContext);
        attributeAnalyzer = new AttributeAnalyzer(settings, commentContext);
        annotationAnalyzer = new AnnotationAnalyzer(settings, commentContext);
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

        if (accessModifier != null) {
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
            if (any.isPresent()) {
                OoRexxNamedElement ooRexxNamedElement = any.get();
                ooRexxNamedElement.getAnnotations().add(ooRexxAnnotation);
            }
        });

        return ooRexxClass;
    }

    private List<OoRexxAnnotation> analyzeAnnotations(OoRexxPackage ooRexxPackage, Collection<OoRexxDirectiveAnnotate> annotates) {
        return annotates.stream().map(directive -> annotationAnalyzer.analyze(ooRexxPackage, directive)).collect(Collectors.toList());
    }

    private List<OoRexxAttribute> analyzeAttributes(Collection<OoRexxDirectiveAttribute> attributes) {
        return attributes.stream()
                         .filter(filterByDirectiveAccess(OoRexxDirectiveAccessPublic.class))
                         .map(attributeAnalyzer::analyze)
                         .sorted(Comparator.comparing(o -> o.getSanitizedName().toLowerCase()))
                         .collect(Collectors.toList());
    }

    private List<OoRexxConstant> getConstants() {
        return findChildrenOfType(directiveClass, OoRexxDirectiveConstant.class)
                .stream()
                .map(constantAnalyzer::analyze)
                .sorted(Comparator.comparing(o -> o.getSanitizedName().toLowerCase()))
                .collect(Collectors.toList());
    }

    private ClassReference getMetaClass() {
        String name = getText(directiveClass, OoRexxDirectiveClassMetaclassName.class);
        return createClassReference(name);
    }

    private ClassReference getSubClass() {
        String name = getText(directiveClass, OoRexxDirectiveClassSubclassName.class);
        return createClassReference(name);
    }

    private List<ClassReference> getMixinClasses() {
        Collection<OoRexxDirectiveClassMixinclassName> mixins = findChildrenOfType(directiveClass, OoRexxDirectiveClassMixinclassName.class);
        return mixins.stream()
                     .map(name -> name.getText() == null ? null : new ClassReference(name.getText()))
                     .filter(Objects::nonNull)
                     .collect(Collectors.toList());
    }

    private List<OoRexxMethod> analyzeMethods(Collection<OoRexxDirectiveMethod> directiveMethods) {
        return directiveMethods.stream()
                               .filter(filterByDirectiveAccess(OoRexxDirectiveAccessPublic.class))
                               .map(methodAnalyzer::analyze)
                               .sorted(METHOD_COMPARATOR)
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

    private AccessModifier getAccessModifier() {
        OoRexxDirectiveClassAccess access = findChildOfType(directiveClass, OoRexxDirectiveClassAccess.class);
        OoRexxDirectiveAccessPublic publicAccess = findChildOfType(access, OoRexxDirectiveAccessPublic.class);

        if (publicAccess != null) {
            return AccessModifier.PUBLIC;
        }

        OoRexxDirectiveAccessPrivate privateAccess = findChildOfType(access, OoRexxDirectiveAccessPrivate.class);

        if (privateAccess != null) {
            return AccessModifier.PRIVATE;
        }

        return null;
    }

    private ClassReference createClassReference(String name) {
        return name != null ? new ClassReference(name) : null;
    }
}

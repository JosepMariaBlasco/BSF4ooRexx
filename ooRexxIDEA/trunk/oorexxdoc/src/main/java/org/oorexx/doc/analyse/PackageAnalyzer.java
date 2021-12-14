package org.oorexx.doc.analyse;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.oorexx.doc.analyse.model.OoRexxAnnotation;
import org.oorexx.doc.analyse.model.OoRexxAttribute;
import org.oorexx.doc.analyse.model.OoRexxClass;
import org.oorexx.doc.analyse.model.OoRexxConstant;
import org.oorexx.doc.analyse.model.OoRexxMethod;
import org.oorexx.doc.analyse.model.OoRexxNamedElement;
import org.oorexx.doc.analyse.model.OoRexxOption;
import org.oorexx.doc.analyse.model.OoRexxPackage;
import org.oorexx.doc.analyse.model.OoRexxRequires;
import org.oorexx.doc.analyse.model.OoRexxResource;
import org.oorexx.doc.analyse.model.OoRexxRoutine;
import org.oorexx.doc.generate.Settings;
import org.oorexx.ide.file.OoRexxFile;
import org.oorexx.ide.lang.psi.OoRexxDirectiveAccessPackage;
import org.oorexx.ide.lang.psi.OoRexxDirectiveAccessPublic;
import org.oorexx.ide.lang.psi.OoRexxDirectiveAnnotate;
import org.oorexx.ide.lang.psi.OoRexxDirectiveAttribute;
import org.oorexx.ide.lang.psi.OoRexxDirectiveClass;
import org.oorexx.ide.lang.psi.OoRexxDirectiveConstant;
import org.oorexx.ide.lang.psi.OoRexxDirectiveMethod;
import org.oorexx.ide.lang.psi.OoRexxDirectiveOptions;
import org.oorexx.ide.lang.psi.OoRexxDirectiveRequires;
import org.oorexx.ide.lang.psi.OoRexxDirectiveResource;
import org.oorexx.ide.lang.psi.OoRexxDirectiveRoutine;

import static com.intellij.psi.util.PsiTreeUtil.findChildrenOfType;
import static org.oorexx.doc.analyse.model.OoRexxMethod.METHOD_COMPARATOR;

public class PackageAnalyzer extends Analyzer {

    private final ClassAnalyzer classAnalyzer;
    private final MethodAnalyzer methodAnalyzer;
    private final RequiresAnalyzer requiresAnalyzer;
    private final OptionAnalyzer optionAnalyzer;
    private final ResourceAnalyzer resourceAnalyzer;
    private final RoutineAnalyzer routineAnalyzer;
    private final AnnotationAnalyzer annotationAnalyzer;
    private final AttributeAnalyzer attributeAnalyzer;
    private final ConstantAnalyzer constantAnalyzer;
    private OoRexxPackage ooRexxPackage;

    public PackageAnalyzer(Settings settings, CommentContext commentContext) {
        super(settings, commentContext);

        classAnalyzer = new ClassAnalyzer(settings, commentContext);
        methodAnalyzer = new MethodAnalyzer(settings, commentContext);
        requiresAnalyzer = new RequiresAnalyzer(settings, commentContext);
        optionAnalyzer = new OptionAnalyzer(settings, commentContext);
        resourceAnalyzer = new ResourceAnalyzer(settings, commentContext);
        routineAnalyzer = new RoutineAnalyzer(settings, commentContext);
        annotationAnalyzer = new AnnotationAnalyzer(settings, commentContext);
        attributeAnalyzer = new AttributeAnalyzer(settings, commentContext);
        constantAnalyzer = new ConstantAnalyzer(settings, commentContext);
    }

    public OoRexxPackage analyze(File file, OoRexxFile ooRexxFile) {


        Collection<OoRexxDirectiveClass> classes = findChildrenOfType(ooRexxFile, OoRexxDirectiveClass.class);
        Collection<OoRexxDirectiveRoutine> routines = findChildrenOfType(ooRexxFile, OoRexxDirectiveRoutine.class);
        Collection<OoRexxDirectiveRequires> requires = findChildrenOfType(ooRexxFile, OoRexxDirectiveRequires.class);
        Collection<OoRexxDirectiveAnnotate> annotates = findChildrenOfType(ooRexxFile, OoRexxDirectiveAnnotate.class).stream().filter(hasNotParent(OoRexxDirectiveClass.class)).collect(Collectors.toList());
        Collection<OoRexxDirectiveResource> resources = findChildrenOfType(ooRexxFile, OoRexxDirectiveResource.class);
        Collection<OoRexxDirectiveOptions> options = findChildrenOfType(ooRexxFile, OoRexxDirectiveOptions.class);
        Collection<OoRexxDirectiveMethod> methods = findChildrenOfType(ooRexxFile, OoRexxDirectiveMethod.class);
        Collection<OoRexxDirectiveAttribute> attributes = findChildrenOfType(ooRexxFile, OoRexxDirectiveAttribute.class);
        Collection<OoRexxDirectiveConstant> constants = findChildrenOfType(ooRexxFile, OoRexxDirectiveConstant.class);

        ooRexxPackage = new OoRexxPackage(ooRexxFile);
        ooRexxPackage.setFile(file);
        ooRexxPackage.setName(ooRexxFile.getName());
        ooRexxPackage.setClasses(analyzeClasses(classes));
        ooRexxPackage.setMethods(analyzeMethods(methods));
        ooRexxPackage.setRequires(analyzeRequires(requires));
        ooRexxPackage.setOptions(analyzeOptions(options));
        ooRexxPackage.setResources(analyzeResources(resources));
        ooRexxPackage.setRoutines(analyzeRoutines(routines));
        ooRexxPackage.setAttributes(analyzeAttributes(attributes));
        ooRexxPackage.setConstants(analyzeConstants(constants));

        List<OoRexxAnnotation> annotations = getAllNonClassAnnotations(ooRexxPackage, annotates);

        distributeAnnotations(ooRexxPackage, annotations);

        setComment(ooRexxPackage, ooRexxFile);

        return ooRexxPackage;
    }

    private List<OoRexxAttribute> analyzeAttributes(Collection<OoRexxDirectiveAttribute> attributes) {
        return attributes.stream()
                         .filter(hasNotParent(OoRexxDirectiveClass.class))
                         .filter(filterByDirectiveAccess(OoRexxDirectiveAccessPublic.class))
                         .map(directive -> attributeAnalyzer.analyze(directive))
                         .sorted(Comparator.comparing(o -> o.getSanitizedName().toLowerCase()))
                         .collect(Collectors.toList());
    }

    private List<OoRexxConstant> analyzeConstants(Collection<OoRexxDirectiveConstant> constants) {
        return constants.stream()
                        .filter(hasNotParent(OoRexxDirectiveClass.class))
                        .map(directive -> constantAnalyzer.analyze(directive))
                        .sorted(Comparator.comparing(o -> o.getSanitizedName().toLowerCase()))
                        .collect(Collectors.toList());
    }

    private List<OoRexxAnnotation> getAllNonClassAnnotations(OoRexxPackage ooRexxPackage, Collection<OoRexxDirectiveAnnotate> annotation) {
        return annotation.stream().map(directive -> annotationAnalyzer.analyze(ooRexxPackage, directive)).collect(Collectors.toList());
    }

    private List<OoRexxRoutine> analyzeRoutines(Collection<OoRexxDirectiveRoutine> routines) {
        return routines.stream()
                       .filter(filterByDirectiveAccess(OoRexxDirectiveAccessPackage.class))
                       .map(directive -> routineAnalyzer.analyze(directive))
                       .sorted(Comparator.comparing(o -> o.getSanitizedName().toLowerCase()))
                       .collect(Collectors.toList());
    }

    private List<OoRexxResource> analyzeResources(Collection<OoRexxDirectiveResource> resources) {
        return resources.stream().map(directive -> resourceAnalyzer.analyze(directive)).collect(Collectors.toList());
    }

    private List<OoRexxOption> analyzeOptions(Collection<OoRexxDirectiveOptions> options) {
        return options.stream()
                      .flatMap((Function<OoRexxDirectiveOptions, Stream<? extends OoRexxOption>>) ooRexxDirectiveOptions -> optionAnalyzer.analyze(ooRexxDirectiveOptions).stream())
                      .collect(Collectors.toList());
    }

    private List<OoRexxClass> analyzeClasses(Collection<OoRexxDirectiveClass> directiveClasses) {
        return directiveClasses.stream()
                               .filter(filterByDirectiveAccess(OoRexxDirectiveAccessPackage.class))
                               .map(ooRexxDirectiveClass -> classAnalyzer.analyze(ooRexxPackage, ooRexxDirectiveClass))
                               .sorted(Comparator.comparing(o -> o.getSanitizedName().toLowerCase()))
                               .collect(Collectors.toList());
    }

    private List<OoRexxMethod> analyzeMethods(Collection<OoRexxDirectiveMethod> directiveMethods) {

        return directiveMethods.stream()
                               .filter(hasNotParent(OoRexxDirectiveClass.class))
                               .filter(filterByDirectiveAccess(OoRexxDirectiveAccessPublic.class))
                               .map(directiveMethod -> methodAnalyzer.analyze(directiveMethod))
                               .sorted(METHOD_COMPARATOR)
                               .collect(Collectors.toList());
    }

    private ArrayList<OoRexxRequires> analyzeRequires(Collection<OoRexxDirectiveRequires> directiveRequires) {
        return directiveRequires.stream()
                                .map(directiveRequires1 -> requiresAnalyzer.analyze(directiveRequires1))
                                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void distributeAnnotations(OoRexxPackage ooRexxPackage, List<OoRexxAnnotation> annotations) {
        List<OoRexxNamedElement> allNonClassAnnotateableElements = ooRexxPackage.getAllNonClassAnnotateableElements();
        annotations.forEach(ooRexxAnnotation -> {
            if (OoRexxAnnotation.AnnotationType.PACKAGE.equals(ooRexxAnnotation.getAnnotationType())) {
                ooRexxPackage.getAnnotations().add(ooRexxAnnotation);
                return;
            }

            Optional<OoRexxNamedElement> any = allNonClassAnnotateableElements.stream().filter(ooRexxAnnotation::appliesTo).findAny();

            if (any.isPresent()) {
                OoRexxNamedElement ooRexxNamedElement = any.get();
                ooRexxNamedElement.getAnnotations().add(ooRexxAnnotation);
            }
        });
    }

}

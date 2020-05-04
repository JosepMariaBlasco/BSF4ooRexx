package org.oorexx.doc.analyse;

import org.oorexx.doc.analyse.model.*;
import org.oorexx.doc.generate.Settings;
import org.oorexx.ide.file.OoRexxFile;
import org.oorexx.ide.lang.psi.*;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.intellij.psi.util.PsiTreeUtil.findChildrenOfType;

public class PackageAnalyzer extends Analyzer{

    private ClassAnalyzer classAnalyzer;
    private MethodAnalyzer methodAnalyzer;
    private RequiresAnalyzer requiresAnalyzer;
    private OptionAnalyzer optionAnalyzer;
    private ResourceAnalyzer resourceAnalyzer;
    private OoRexxPackage ooRexxPackage;
    private RoutineAnalyzer routineAnalyzer;
    private AnnotationAnalyzer annotationAnalyzer;

    public PackageAnalyzer(Settings settings, CommentContext commentContext) {
        super(settings, commentContext);

        classAnalyzer = new ClassAnalyzer(settings, commentContext);
        methodAnalyzer = new MethodAnalyzer(settings, commentContext);
        requiresAnalyzer = new RequiresAnalyzer(settings, commentContext);
        optionAnalyzer = new OptionAnalyzer(settings, commentContext);
        resourceAnalyzer = new ResourceAnalyzer(settings, commentContext);
        routineAnalyzer = new RoutineAnalyzer(settings, commentContext);
        annotationAnalyzer = new AnnotationAnalyzer(settings, commentContext);
    }

    public OoRexxPackage analyze(File file, OoRexxFile ooRexxFile){


        Collection<OoRexxDirectiveClass> classes = findChildrenOfType(ooRexxFile, OoRexxDirectiveClass.class);
        Collection<OoRexxDirectiveRoutine> routines = findChildrenOfType(ooRexxFile, OoRexxDirectiveRoutine.class);
        Collection<OoRexxDirectiveRequires> requires = findChildrenOfType(ooRexxFile, OoRexxDirectiveRequires.class);
        Collection<OoRexxDirectiveAnnotate> annotates = findChildrenOfType(ooRexxFile, OoRexxDirectiveAnnotate.class).stream().filter(hasNotParent(OoRexxDirectiveClass.class)).collect(Collectors.toList());
        Collection<OoRexxDirectiveResource> resources = findChildrenOfType(ooRexxFile, OoRexxDirectiveResource.class);
        Collection<OoRexxDirectiveOptions> options = findChildrenOfType(ooRexxFile, OoRexxDirectiveOptions.class);
        Collection<OoRexxDirectiveMethod> methods = findChildrenOfType(ooRexxFile, OoRexxDirectiveMethod.class);

        ooRexxPackage = new OoRexxPackage(ooRexxFile);
        ooRexxPackage.setFile(file);
        ooRexxPackage.setName(ooRexxFile.getName());
        ooRexxPackage.setClasses(analyzeClasses(classes));
        ooRexxPackage.setMethods(analyzeMethods(methods));
        ooRexxPackage.setRequires(analyzeRequires(requires));
        ooRexxPackage.setOptions(analyzeOptions(options));
        ooRexxPackage.setResources(analyzeResources(resources));
        ooRexxPackage.setRoutines(analyzeRoutines(routines));

        List<OoRexxAnnotation> annotations = getAllNonClassAnnotations(ooRexxPackage, annotates);

        distributeAnnotations(ooRexxPackage, annotations);

        setComment(ooRexxPackage, ooRexxFile);

        return ooRexxPackage;
    }

    private List<OoRexxAnnotation> getAllNonClassAnnotations(OoRexxPackage ooRexxPackage, Collection<OoRexxDirectiveAnnotate> annotation) {
        return annotation.stream().map(directive -> annotationAnalyzer.analyze(ooRexxPackage, directive)).collect(Collectors.toList());
    }

    private List<OoRexxRoutine> analyzeRoutines(Collection<OoRexxDirectiveRoutine> routines) {
        return routines.stream()
                .filter(filterByDirectiveAccess(OoRexxDirectiveAccessPrivate.class))
                .map(directive -> routineAnalyzer.analyze(directive))
                .sorted(Comparator.comparing(o -> o.getName().toLowerCase()))
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

    private List<OoRexxClass> analyzeClasses(Collection<OoRexxDirectiveClass> directiveClasses){
        return directiveClasses.stream()
                .filter(filterByDirectiveAccess(OoRexxDirectiveAccessPackage.class))
                .map(ooRexxDirectiveClass -> classAnalyzer.analyze(ooRexxPackage, ooRexxDirectiveClass))
                .sorted(Comparator.comparing(o -> o.getName().toLowerCase()))
                .collect(Collectors.toList());
    }

    private List<OoRexxMethod> analyzeMethods(Collection<OoRexxDirectiveMethod> directiveMethods){
        return directiveMethods.stream()
                .filter(hasNotParent(OoRexxDirectiveClass.class))
                .filter(filterByDirectiveAccess(OoRexxDirectiveAccessPublic.class))
                .map(directiveMethod -> methodAnalyzer.analyze(directiveMethod))
                .sorted(Comparator.comparing(o -> o.getName().toLowerCase()))
                .collect(Collectors.toList());
    }

    private ArrayList<OoRexxRequires> analyzeRequires(Collection<OoRexxDirectiveRequires> directiveRequires){
         return directiveRequires.stream()
                 .map(directiveRequires1 -> requiresAnalyzer.analyze(directiveRequires1))
                 .collect(Collectors.toCollection(ArrayList::new));
    }

    private void distributeAnnotations(OoRexxPackage ooRexxPackage, List<OoRexxAnnotation> annotations){
        List<OoRexxNamedElement> allNonClassAnnotateableElements = ooRexxPackage.getAllNonClassAnnotateableElements();
        annotations.forEach(ooRexxAnnotation -> {
            if(OoRexxAnnotation.AnnotationType.PACKAGE.equals(ooRexxAnnotation.getAnnotationType())){
                ooRexxPackage.getAnnotations().add(ooRexxAnnotation);
                return;
            }

            Optional<OoRexxNamedElement> any = allNonClassAnnotateableElements.stream().filter(ooRexxAnnotation::appliesTo).findAny();

            if(any.isPresent()){
                OoRexxNamedElement ooRexxNamedElement = any.get();
                ooRexxNamedElement.getAnnotations().add(ooRexxAnnotation);
            }
        });
    }

}

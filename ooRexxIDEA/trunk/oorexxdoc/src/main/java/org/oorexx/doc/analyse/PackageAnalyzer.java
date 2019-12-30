package org.oorexx.doc.analyse;

import org.oorexx.doc.analyse.model.*;
import org.oorexx.ide.file.OoRexxFile;
import org.oorexx.ide.lang.psi.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
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

    public PackageAnalyzer(CommentContext commentContext) {
        super(commentContext);

        classAnalyzer = new ClassAnalyzer(commentContext);
        methodAnalyzer = new MethodAnalyzer(commentContext);
        requiresAnalyzer = new RequiresAnalyzer(commentContext);
        optionAnalyzer = new OptionAnalyzer(commentContext);
        resourceAnalyzer = new ResourceAnalyzer(commentContext);
        routineAnalyzer = new RoutineAnalyzer(commentContext);
        annotationAnalyzer = new AnnotationAnalyzer(commentContext);
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

        ooRexxPackage.getClasses().removeIf(ooRexxClass -> AccessModifier.PRIVATE.equals(ooRexxClass.getAccessModifier()));
        ooRexxPackage.getRoutines().removeIf(routine -> AccessModifier.PRIVATE.equals(routine.getAccessModifier()));

        return ooRexxPackage;
    }

    private List<OoRexxAnnotation> getAllNonClassAnnotations(OoRexxPackage ooRexxPackage, Collection<OoRexxDirectiveAnnotate> annotation) {
        return annotation.stream().map(directive -> annotationAnalyzer.analyze(ooRexxPackage, directive)).collect(Collectors.toList());
    }

    private List<OoRexxRoutine> analyzeRoutines(Collection<OoRexxDirectiveRoutine> routines) {
        return routines.stream().map(directive -> routineAnalyzer.analyze(directive)).collect(Collectors.toList());
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
                .map(ooRexxDirectiveClass -> classAnalyzer.analyze(ooRexxPackage, ooRexxDirectiveClass))
                .collect(Collectors.toList());
    }

    private List<OoRexxMethod> analyzeMethods(Collection<OoRexxDirectiveMethod> directiveMethods){
        return directiveMethods.stream()
                .filter(hasNotParent(OoRexxDirectiveClass.class))
                .map(directiveMethod -> methodAnalyzer.analyze(directiveMethod))
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

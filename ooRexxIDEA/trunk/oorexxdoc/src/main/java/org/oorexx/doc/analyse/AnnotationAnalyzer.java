package org.oorexx.doc.analyse;

import org.oorexx.doc.analyse.model.OoRexxAnnotation;
import org.oorexx.doc.analyse.model.OoRexxPackage;
import org.oorexx.doc.generate.Settings;
import org.oorexx.ide.lang.psi.OoRexxDirectiveAnnotate;
import org.oorexx.ide.lang.psi.OoRexxDirectiveAnnotateName;
import org.oorexx.ide.lang.psi.OoRexxDirectiveAnnotateReferenceName;
import org.oorexx.ide.lang.psi.OoRexxDirectiveAnnotateValue;

import static com.intellij.psi.util.PsiTreeUtil.findChildOfType;

public class AnnotationAnalyzer extends Analyzer {

    public AnnotationAnalyzer(Settings settings, CommentContext commentContext) {
        super(settings, commentContext);
    }

    public OoRexxAnnotation analyze(OoRexxPackage ooRexxPackage, OoRexxDirectiveAnnotate directive){
        OoRexxAnnotation ooRexxAnnotation = new OoRexxAnnotation(directive);

        if(directive.getDirectiveAnnotateReference() != null) {
            OoRexxAnnotation.AnnotationType annotationType = OoRexxAnnotation.AnnotationType.fromText(directive.getDirectiveAnnotateReference().getText());
            ooRexxAnnotation.setAnnotationType(annotationType);
        }

        OoRexxDirectiveAnnotateReferenceName name = findChildOfType(directive, OoRexxDirectiveAnnotateReferenceName.class);

        if(name != null){
            ooRexxAnnotation.setName(name.getText());
        }

        directive.getDirectiveAnnotateDataList().forEach(ooRexxDirectiveAnnotateData -> {
            OoRexxDirectiveAnnotateName directiveAnnotateName = ooRexxDirectiveAnnotateData.getDirectiveAnnotateName();
            OoRexxDirectiveAnnotateValue directiveAnnotateValue = ooRexxDirectiveAnnotateData.getDirectiveAnnotateValue();

            ooRexxAnnotation.getData().put(directiveAnnotateName.getText(), directiveAnnotateValue.getText());
        });

        setComment(ooRexxAnnotation, directive);

        return ooRexxAnnotation;
    }

}

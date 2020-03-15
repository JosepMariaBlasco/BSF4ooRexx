package org.oorexx.doc.analyse;

import org.oorexx.doc.analyse.model.OoRexxRequires;
import org.oorexx.doc.generate.Settings;
import org.oorexx.ide.lang.psi.OoRexxDirectiveRequires;
import org.oorexx.ide.lang.psi.OoRexxDirectiveRequiresPath;

import static com.intellij.psi.util.PsiTreeUtil.findChildOfType;

public class RequiresAnalyzer extends Analyzer {

    public RequiresAnalyzer(Settings settings, CommentContext commentContext) {
        super(settings, commentContext);
    }

    public OoRexxRequires analyze(OoRexxDirectiveRequires directiveRequires){
        OoRexxRequires result = new OoRexxRequires(directiveRequires);

        OoRexxDirectiveRequiresPath path = findChildOfType(directiveRequires, OoRexxDirectiveRequiresPath.class);

        if(path != null) {
            result.setPath(path.getText().replace("\"", ""));
        }

        setComment(result, directiveRequires);

        return result;
    }

}

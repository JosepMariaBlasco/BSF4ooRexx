package org.oorexx.doc.analyse;

import org.oorexx.doc.analyse.model.OoRexxResource;
import org.oorexx.doc.generate.Settings;
import org.oorexx.ide.lang.psi.OoRexxDirectiveResource;

public class ResourceAnalyzer extends Analyzer{

    public ResourceAnalyzer(Settings settings, CommentContext commentContext) {
        super(settings, commentContext);
    }

    public OoRexxResource analyze(OoRexxDirectiveResource ooRexxDirectiveResource){
        OoRexxResource ooRexxResource = new OoRexxResource(ooRexxDirectiveResource);
        ooRexxResource.setName(ooRexxDirectiveResource.getSymbolOrStringOrKeyword().getText());

        setComment(ooRexxResource, ooRexxDirectiveResource);

        return ooRexxResource;
    }
}

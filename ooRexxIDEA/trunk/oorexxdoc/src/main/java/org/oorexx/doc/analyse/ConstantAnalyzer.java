package org.oorexx.doc.analyse;

import org.oorexx.doc.analyse.model.OoRexxConstant;
import org.oorexx.ide.lang.psi.OoRexxDirectiveConstant;
import org.oorexx.ide.lang.psi.OoRexxDirectiveConstantValue;
import org.oorexx.ide.lang.psi.OoRexxSymbolOrString;

public class ConstantAnalyzer extends Analyzer{

    public ConstantAnalyzer(CommentContext commentContext) {
        super(commentContext);
    }

    public OoRexxConstant analyze(OoRexxDirectiveConstant ooRexxDirectiveConstant){
        OoRexxSymbolOrString symbolOrString = ooRexxDirectiveConstant.getSymbolOrString();
        OoRexxDirectiveConstantValue directiveConstantValue = ooRexxDirectiveConstant.getDirectiveConstantValue();

        if(symbolOrString == null){
            return null;
        }

        OoRexxConstant ooRexxConstant = new OoRexxConstant(ooRexxDirectiveConstant);

        ooRexxConstant.setName(symbolOrString.getText());

        if(directiveConstantValue != null) {
            ooRexxConstant.setValue(directiveConstantValue.getText());
        }

        setComment(ooRexxConstant, ooRexxDirectiveConstant);

        return ooRexxConstant;
    }
}

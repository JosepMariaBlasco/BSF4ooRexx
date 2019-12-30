package org.oorexx.doc.analyse;

import org.oorexx.doc.analyse.model.OoRexxOption;
import org.oorexx.ide.lang.psi.OoRexxDirectiveOptions;

import java.util.List;
import java.util.stream.Collectors;

public class OptionAnalyzer extends Analyzer{

    public OptionAnalyzer(CommentContext commentContext) {
        super(commentContext);
    }

    public List<? extends OoRexxOption> analyze(OoRexxDirectiveOptions ooRexxDirectiveOptions) {
        return ooRexxDirectiveOptions.getDirectiveOptionsKeyValuePairList()
        .stream().map(ooRexxDirectiveOptionsKeyValuePair -> {
            String text = ooRexxDirectiveOptionsKeyValuePair.getText();
            if(text.contains(" ")){
                String[] split = text.split(" +");
                if(split.length > 1) {
                    return new OoRexxOption(split[0], split[1]);
                }

                return new OoRexxOption(split[0]);
            }
            return null;
        }).collect(Collectors.toList());

    }
}

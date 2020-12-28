package org.oorexx.doc.generate;

import com.intellij.lang.ParserDefinition;
import com.intellij.psi.PsiFile;
import org.oorexx.ide.parser.standalone.OoRexxStandaloneParserDefinition;

import java.io.File;
import java.lang.reflect.Method;

public class StandaloneAstUtil extends AstUtilBase{

    @Override
    protected AstWrapper getPsiFile(File file) {
        try {
            Class<?> aClass = Class.forName("org.intellij.grammar.LightPsi");
            Method parseFile = aClass.getMethod("parseFile", File.class, ParserDefinition.class);
            PsiFile psiFile = (PsiFile) parseFile.invoke(null, file, new OoRexxStandaloneParserDefinition());
            return new AstWrapper(file, psiFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

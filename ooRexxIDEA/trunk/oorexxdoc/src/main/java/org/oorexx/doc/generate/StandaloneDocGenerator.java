package org.oorexx.doc.generate;

import com.intellij.psi.PsiElement;
import org.oorexx.doc.analyse.CommentContext;
import org.oorexx.doc.analyse.PackageAnalyzer;
import org.oorexx.doc.analyse.model.OoRexxPackage;
import org.oorexx.doc.input.cli.GenerateCommand;
import org.oorexx.doc.input.model.xml.OoRexxDocXml;
import org.oorexx.ide.file.OoRexxFile;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class StandaloneDocGenerator extends DocGeneratorBase{

    public StandaloneDocGenerator(GenerateCommand generateCommand) {
        super(generateCommand);
    }

    public StandaloneDocGenerator(OoRexxDocXml ooRexxDocXml) {
        super(ooRexxDocXml);
    }

    @Override
    protected AstUtilBase getAstUtil() {
        return new StandaloneAstUtil();
    }

    @Override
    protected List<OoRexxPackage> getOoRexxPackages() {
        List<AstWrapper> psiFiles = getAstUtil().getAbstractSyntaxTrees(ooRexxDocGenerationContext.getInputs());

        return psiFiles.parallelStream()
                .map(psiFile -> {
                    final OoRexxPackage[] ooRexxPackage = new OoRexxPackage[1];

                    PsiElement psi = psiFile.getPsiFile().getNode().getPsi();
                    CommentContext commentContext = new CommentContext((OoRexxFile) psi);
                    ooRexxPackage[0] = new PackageAnalyzer(ooRexxDocGenerationContext.getSettings(), commentContext).analyze(psiFile.getFile(), (OoRexxFile) psi);

                    return ooRexxPackage[0];
                })
                .filter(OoRexxPackage::hasElements)
                .sorted(Comparator.comparing(o -> o.getName().toLowerCase()))
                .collect(Collectors.toList());
    }
}

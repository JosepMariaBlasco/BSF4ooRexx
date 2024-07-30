package org.oorexx.doc.generate;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import org.oorexx.doc.analyse.CommentContext;
import org.oorexx.doc.analyse.PackageAnalyzer;
import org.oorexx.doc.analyse.model.OoRexxPackage;
import org.oorexx.doc.input.model.OoRexxDocGenerationContext;
import org.oorexx.ide.file.OoRexxFile;
import org.oorexx.ide.file.OoRexxFileTypeFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AstUtilBase {

    public List<AstWrapper> getAbstractSyntaxTrees(List<String> filesOrDirectories) {
        List<String> extensions = OoRexxFileTypeFactory.fileExtensions;
        return filesOrDirectories.stream()
                .map(s -> Paths.get(s))
                .flatMap(path -> {
                    try {
                        return Files.walk(path).filter(Files::isRegularFile).map(Path::toFile);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(file -> extensions.stream().anyMatch(ext -> file.getName().toLowerCase().endsWith(ext)))
                .map(this::getPsiFile)
                .collect(Collectors.toList());
    }

    protected abstract AstWrapper getPsiFile(File file);

    protected List<OoRexxPackage> getOoRexxPackages(OoRexxDocGenerationContext ooRexxDocGenerationContext) {
        return getAbstractSyntaxTrees(ooRexxDocGenerationContext.getInputs())
                .parallelStream()
                .map(psiFile -> {
                    final OoRexxPackage[] ooRexxPackage = new OoRexxPackage[1];
                    ApplicationManager.getApplication().runReadAction(() -> {
                        PsiElement psi = psiFile.getPsiFile().getNode().getPsi();
                        CommentContext commentContext = new CommentContext((OoRexxFile) psi);
                        ooRexxPackage[0] = new PackageAnalyzer(ooRexxDocGenerationContext.getSettings(), commentContext).analyze(psiFile.getFile(), (OoRexxFile) psi);
                    });
                    return ooRexxPackage[0];
                })
                .filter(OoRexxPackage::hasElements)
                .sorted(Comparator.comparing(o -> o.getName().toLowerCase()))
                .collect(Collectors.toList());
    }
}

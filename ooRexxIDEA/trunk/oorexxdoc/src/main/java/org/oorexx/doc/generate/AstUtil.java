package org.oorexx.doc.generate;

import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.util.FileContentUtilCore;
import org.apache.commons.io.FileUtils;
import org.intellij.grammar.LightPsi;
import org.intellij.grammar.generator.ParserGeneratorUtil;
import org.oorexx.ide.file.OoRexxFileTypeFactory;
import org.oorexx.ide.lang.OoRexx;
import org.oorexx.ide.parser.OoRexxParser;
import org.oorexx.ide.parser.OoRexxParserDefinition;
import org.oorexx.ide.parser.standalone.OoRexxStandaloneParserDefinition;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AstUtil {

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

    private AstWrapper getPsiFile(File file){
        final PsiFileFactory factory = PsiFileFactory.getInstance(OoRexxParserDefinition.getProject());

        try {
            return new AstWrapper(file, factory.createFileFromText(new OoRexx(), FileUtils.readFileToString(file, "utf-8")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public class AstWrapper {
        private File file;
        private PsiFile psiFile;

        public AstWrapper(File file, PsiFile psiFile) {
            this.file = file;
            this.psiFile = psiFile;
        }

        public File getFile() {
            return file;
        }

        public PsiFile getPsiFile() {
            return psiFile;
        }
    }
}

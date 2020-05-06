package org.oorexx.doc.generate;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import org.apache.commons.io.FileUtils;

import org.oorexx.ide.file.OoRexxFileType;
import org.oorexx.ide.file.OoRexxFileTypeFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        Project defaultProject = ProjectManager.getInstance().getDefaultProject();
        final PsiFileFactory factory = PsiFileFactory.getInstance(defaultProject);
        return ApplicationManager.getApplication().runReadAction((Computable<AstWrapper>) () -> {
            try {
                return new AstWrapper(file, factory.createFileFromText(file.getName(), OoRexxFileType.INSTANCE, FileUtils.readFileToString(file, "utf-8")));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

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

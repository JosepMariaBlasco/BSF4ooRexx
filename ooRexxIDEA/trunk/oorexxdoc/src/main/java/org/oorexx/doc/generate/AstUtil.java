package org.oorexx.doc.generate;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import org.apache.commons.io.FileUtils;
import org.oorexx.doc.analyse.CommentContext;
import org.oorexx.doc.analyse.PackageAnalyzer;
import org.oorexx.doc.analyse.model.OoRexxPackage;
import org.oorexx.doc.input.model.OoRexxDocGenerationContext;
import org.oorexx.ide.file.OoRexxFile;
import org.oorexx.ide.file.OoRexxFileType;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AstUtil extends AstUtilBase{
    @Override
    protected AstWrapper getPsiFile(File file) {
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



}

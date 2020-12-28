package org.oorexx.doc.generate;

import com.intellij.psi.PsiFile;

import java.io.File;

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

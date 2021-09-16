package org.oorexx.ide.file;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.oorexx.ide.lang.OoRexx;

import javax.swing.*;


public class OoRexxFile extends PsiFileBase {

    public OoRexxFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, OoRexx.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return OoRexxFileType.INSTANCE;
    }

    @Nullable
    @Override
    public Icon getIcon(int flags) {
        return super.getIcon(flags);
    }

    @Override
    public String toString() {
        return "ooRexx File";
    }
}

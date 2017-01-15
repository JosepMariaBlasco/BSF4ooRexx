package org.oorexx.ide.intellij.file;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.oorexx.ide.lang.OoRexx;

import javax.swing.*;


public class OoRexxFileType extends LanguageFileType {



    public static final OoRexxFileType INSTANCE = new OoRexxFileType();

    private OoRexxFileType() {
        super(OoRexx.INSTANCE);
    }

    @NotNull
    public String getName() {
        return "ooRexx-File";
    }

    @NotNull
    public String getDescription() {
        return "ooRexx File";
    }

    @NotNull
    public String getDefaultExtension() {
        return "rexx";
    }

    @Nullable
    public Icon getIcon() {
        return OoRexxIcons.FILE;
    }
}

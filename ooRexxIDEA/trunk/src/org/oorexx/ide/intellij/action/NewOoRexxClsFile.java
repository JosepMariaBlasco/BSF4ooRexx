package org.oorexx.ide.intellij.action;

import com.intellij.ide.actions.CreateFileAction;
import org.jetbrains.annotations.Nullable;
import org.oorexx.ide.intellij.file.OoRexxIcons;

public class NewOoRexxClsFile extends CreateFileAction{

    public static final String CREATE_NEW_OO_REXX_FILE = "Create new ooRexx Cls File";

    public NewOoRexxClsFile() {
        super(CREATE_NEW_OO_REXX_FILE, "", OoRexxIcons.FILE);
    }

    @Nullable
    @Override
    protected String getDefaultExtension() {
        return "cls";
    }
}

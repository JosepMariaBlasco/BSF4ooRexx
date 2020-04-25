package org.oorexx.ide.intellij.action;

import com.intellij.ide.actions.CreateFileAction;
import org.jetbrains.annotations.Nullable;
import org.oorexx.ide.file.OoRexxIcons;

public class NewOoRexxFile extends CreateFileAction{

    public static final String CREATE_NEW_OO_REXX_FILE = "Create new ooRexx File";

    public NewOoRexxFile() {
        super(CREATE_NEW_OO_REXX_FILE, "", OoRexxIcons.FILE);
    }

    @Nullable
    @Override
    protected String getDefaultExtension() {
        return "rexx";
    }
}

package org.oorexx.ide.intellij.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.oorexx.ide.intellij.oorexxdoc.OoRexxDocGui;
import org.oorexx.ide.intellij.util.OoRexxFileUtil;

import java.util.Arrays;
import java.util.List;

public class OoRexxDocAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        List<VirtualFile> virtualFiles = getVirtualFiles(e);
        e.getPresentation().setVisible(virtualFiles != null && !virtualFiles.isEmpty());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        List<VirtualFile> virtualFileList = getVirtualFiles(e);
        if (virtualFileList == null) return;
        OoRexxDocGui.openUi(e.getProject(), virtualFileList);
    }

    @Nullable
    private List<VirtualFile> getVirtualFiles(@NotNull AnActionEvent e) {
        VirtualFile[] virtualFiles = e.getDataContext().getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        if(virtualFiles == null){
            return null;
        }
        return OoRexxFileUtil.getFiles(Arrays.asList(virtualFiles));
    }


}

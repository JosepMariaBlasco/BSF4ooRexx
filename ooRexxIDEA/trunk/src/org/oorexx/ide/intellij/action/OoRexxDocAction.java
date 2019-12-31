package org.oorexx.ide.intellij.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.oorexx.ide.intellij.oorexxdoc.OoRexxDocGui;
import org.oorexx.ide.intellij.util.OoRexxFileUtil;

import java.util.Arrays;
import java.util.List;

public class OoRexxDocAction extends AnAction {


    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {



        VirtualFile[] virtualFiles = e.getDataContext().getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        if(virtualFiles == null){
            return;
        }
        List<VirtualFile> virtualFileList = OoRexxFileUtil.getFiles(Arrays.asList(virtualFiles));
        OoRexxDocGui.openUi(e.getProject(), virtualFileList);
        /*try {
            new DocGenerator(virtualFileList, new File(e.getProject().getBaseDir().getCanonicalPath() + "/oorexxdoc"), "myFirstDoc").generate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }*/
    }



}

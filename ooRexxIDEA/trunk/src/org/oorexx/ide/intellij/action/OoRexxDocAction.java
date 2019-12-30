package org.oorexx.ide.intellij.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import org.jetbrains.annotations.NotNull;
import org.oorexx.doc.generate.DocGenerator;
import org.oorexx.ide.file.OoRexxFileTypeFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OoRexxDocAction extends AnAction {


    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        VirtualFile[] virtualFiles = e.getDataContext().getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        if(virtualFiles == null){
            return;
        }
        List<VirtualFile> virtualFileList = getFiles(virtualFiles);
        try {
            new DocGenerator(virtualFileList, new File(e.getProject().getBaseDir().getCanonicalPath() + "/oorexxdoc"), "myFirstDoc").generate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public List<VirtualFile> getFiles(VirtualFile[] virtualFiles){
        List<VirtualFile> files = new ArrayList<>();

        for (VirtualFile virtualFile : virtualFiles) {
            OoRexxFileTypeFilter ooRexxFileTypeFilter = new OoRexxFileTypeFilter();
            if (virtualFile.isDirectory()) {
                VfsUtilCore.visitChildrenRecursively(virtualFile, new VirtualFileVisitor() {
                    @Override
                    public boolean visitFile(@NotNull VirtualFile file) {
                        if (!file.isDirectory() && ooRexxFileTypeFilter.accept(file)) {
                            files.add(file);
                        }
                        return super.visitFile(file);
                    }
                });
            } else if(ooRexxFileTypeFilter.accept(virtualFile)){
                files.add(virtualFile);
            }
        }
        return files;
    }

}

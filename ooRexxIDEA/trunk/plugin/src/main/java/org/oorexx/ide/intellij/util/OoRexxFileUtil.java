package org.oorexx.ide.intellij.util;

import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import org.jetbrains.annotations.NotNull;
import org.oorexx.ide.file.OoRexxFileTypeFilter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class OoRexxFileUtil {

    public static List<VirtualFile> getFiles(List<VirtualFile> virtualFiles){
        Set<VirtualFile> files = new LinkedHashSet<>();

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
        return new ArrayList<>(files);
    }

}

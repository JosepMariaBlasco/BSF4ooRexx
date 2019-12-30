package org.oorexx.ide.file;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileFilter;

public class OoRexxFileTypeFilter implements VirtualFileFilter {

    @Override
    public boolean accept(VirtualFile file) {
        return OoRexxFileTypeFactory.fileExtensions.contains(file.getExtension());
    }
}

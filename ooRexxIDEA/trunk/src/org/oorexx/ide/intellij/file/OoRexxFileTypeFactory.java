package org.oorexx.ide.intellij.file;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;


public class OoRexxFileTypeFactory extends FileTypeFactory{

    public static String[] fileExtensions = new String[]{"rexx", "rex", "cls", "rxj"};

    @Override
    public void createFileTypes(@NotNull FileTypeConsumer fileTypeConsumer) {
        for(String fileExtension: fileExtensions)
            fileTypeConsumer.consume(OoRexxFileType.INSTANCE, fileExtension);
    }
}

package org.oorexx.ide.file;

import com.google.common.collect.Lists;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;


public class OoRexxFileTypeFactory extends FileTypeFactory{

    public static List<String> fileExtensions = Arrays.asList("cls", "frm", "jrexx", "orx", "rex", "rexx", "rexxj", "rxj", "rxo", "testGroup", "testUnit");

    @Override
    public void createFileTypes(@NotNull FileTypeConsumer fileTypeConsumer) {
        for(String fileExtension: fileExtensions)
            fileTypeConsumer.consume(OoRexxFileType.INSTANCE, fileExtension);
    }
}

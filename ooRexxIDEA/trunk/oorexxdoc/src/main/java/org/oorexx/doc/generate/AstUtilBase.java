package org.oorexx.doc.generate;

import org.oorexx.ide.file.OoRexxFileTypeFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AstUtilBase {

    public List<AstWrapper> getAbstractSyntaxTrees(List<String> filesOrDirectories) {
        List<String> extensions = OoRexxFileTypeFactory.fileExtensions;
        return filesOrDirectories.stream()
                .map(s -> Paths.get(s))
                .flatMap(path -> {
                    try {
                        return Files.walk(path).filter(Files::isRegularFile).map(Path::toFile);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(file -> extensions.stream().anyMatch(ext -> file.getName().toLowerCase().endsWith(ext)))
                .map(this::getPsiFile)
                .collect(Collectors.toList());
    }

    protected abstract AstWrapper getPsiFile(File file);

}

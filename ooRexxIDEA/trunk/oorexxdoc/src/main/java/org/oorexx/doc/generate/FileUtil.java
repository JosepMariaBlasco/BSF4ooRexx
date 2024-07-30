package org.oorexx.doc.generate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.security.CodeSource;
import java.util.HashMap;

public class FileUtil {

    public static void copyDirectoryFromFileSystem(Path source, Path target) throws IOException {
        Files.walk(source)
                .forEach(sourcePath -> {
                    Path targetPath = target.resolve(source.relativize(sourcePath));
                    try {
                        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                });
    }

    public static void copyDirectoryFromJar(Path jarPath, String sourceDir, Path targetDir) throws IOException {
        try (FileSystem fs = FileSystems.newFileSystem(jarPath, new HashMap<>())) {
            Path jarSourcePath = fs.getPath(sourceDir);
            Files.walk(jarSourcePath).forEach(path -> {
                try {
                    Path destPath = targetDir.resolve(jarSourcePath.relativize(path).toString());
                    Files.createDirectories(destPath.getParent());
                    if (Files.isDirectory(path)) return;
                    Files.copy(path, destPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            });
        }
    }

    public static void copyDirectory(String sourceDir, String targetDir) throws IOException {
        Path targetPath = Paths.get(targetDir);

        // Determine if running from a JAR file
        CodeSource codeSource = FileUtil.class.getProtectionDomain().getCodeSource();
        if (codeSource != null && codeSource.getLocation().toString().endsWith(".jar")) {
            // Running from within a JAR file
            URI jarUri = null;
            try {
                jarUri = codeSource.getLocation().toURI();
            } catch (URISyntaxException e) {
                throw new IOException("Failed to get JAR URI", e);
            }
            Path jarPath = Paths.get(jarUri);
            copyDirectoryFromJar(jarPath, sourceDir, targetPath);
        } else {
            // Running from filesystem
            Path sourcePath = Paths.get(sourceDir);
            copyDirectoryFromFileSystem(sourcePath, targetPath);
        }
    }
}

package org.oorexx.doc.generate;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.oorexx.doc.analyse.ClassReferenceAnalyzer;
import org.oorexx.doc.analyse.CommentContext;
import org.oorexx.doc.analyse.PackageAnalyzer;
import org.oorexx.doc.analyse.model.OoRexxClass;
import org.oorexx.doc.analyse.model.OoRexxPackage;
import org.oorexx.doc.input.cli.GenerateCommand;
import org.oorexx.doc.input.xml.OoRexxDocXml;
import org.oorexx.ide.file.OoRexxFile;
import org.oorexx.ide.file.OoRexxFileType;
import org.oorexx.ide.parser.OoRexxParserDefinition;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class DocGenerator {

    private ClassReferenceAnalyzer classReferenceAnalyzer = new ClassReferenceAnalyzer();
    private static final File localFile = new File(new File(System.getProperty("user.dir")), "/templates");
    private List<String> inputFiles;
    private String outputDirectory;
    private String title;
    private String overview;
    private Map<String, String> header = new HashMap<>();
    private List<VirtualFile> virtualFiles;

    public DocGenerator(List<VirtualFile> virtualFiles, File output, String title){
        this.virtualFiles = virtualFiles;
        this.outputDirectory = output.getAbsolutePath();
        this.title = title;
    }
    public DocGenerator(GenerateCommand generateCommand) {
        inputFiles = generateCommand.getFilesAndDirectories();
        outputDirectory = generateCommand.getOutputDirectory();
        title = generateCommand.getTitle();
    }

    public DocGenerator(OoRexxDocXml generateXml){
        inputFiles = generateXml.getParser().getInput();
        outputDirectory = generateXml.getParser().getOutput();
        title = generateXml.getTitle();
        overview = generateXml.getOverview();
        generateXml.getHeader().getHeaderProperties().forEach(headerProperty -> header.put(headerProperty.getKey(), headerProperty.getValue()));
    }

    public void generate() throws Exception{
        List<OoRexxPackage> ooRexxPackages = getOoRexxPackages();

        classReferenceAnalyzer.setReferences(ooRexxPackages);

        File jsDestination = new File(outputDirectory, "js");

        if(localFile.exists()) {
            FileUtils.copyDirectory(new File(localFile, "js"), jsDestination);
        } else {
            try {
                copyFromJar("/templates/js", Paths.get(jsDestination.getPath()));
            } catch (Exception e){
                File resourceFile = new File(DocGenerator.class.getClassLoader().getResource("/templates").getFile());
                FileUtils.copyDirectory(new File(resourceFile, "js"), jsDestination);
            }
        }

        fillTemplates(ooRexxPackages);
        fillSearchTemplate(ooRexxPackages);

    }

    @NotNull
    private List<OoRexxPackage> getOoRexxPackages() {
        if(virtualFiles != null){
            return getOoRexxPackgesForVirtualFiles();
        }
        List<AstUtil.AstWrapper> psiFiles = new AstUtil().getAbstractSyntaxTrees(inputFiles);

        return psiFiles.parallelStream()
                .map(psiFile -> {
                    PsiElement psi = psiFile.getPsiFile().getNode().getPsi();
                    CommentContext commentContext = new CommentContext((OoRexxFile) psi);
                    return new PackageAnalyzer(commentContext).analyze(psiFile.getFile(), (OoRexxFile) psi);
                })
                .filter(OoRexxPackage::hasElements)
                .collect(Collectors.toList());
    }

    private PsiFile getPsiFile(File file){
        final PsiFileFactory factory = PsiFileFactory.getInstance(OoRexxParserDefinition.getProject());

        try {
            return factory.createFileFromText("ooRexx", OoRexxFileType.INSTANCE, FileUtils.readFileToString(file, "utf-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private List<OoRexxPackage> getOoRexxPackgesForVirtualFiles() {
        return virtualFiles.parallelStream()
                .map(virtualFile -> {
                    File file = new File(virtualFile.getCanonicalPath());
                    PsiFile psiFile = getPsiFile(file);
                    PsiElement psi = psiFile.getNode().getPsi();
                    CommentContext commentContext = new CommentContext((OoRexxFile) psi);
                    return new PackageAnalyzer(commentContext).analyze(file, (OoRexxFile) psi);
                })
                .filter(OoRexxPackage::hasElements)
                .collect(Collectors.toList());
    }

    private void fillTemplates(List<OoRexxPackage> ooRexxPackages) throws Exception{
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_23);
        if(!localFile.exists()) {
            configuration.setClassForTemplateLoading(DocGenerator.class, "/templates");
        } else {
            configuration.setTemplateLoader(new FileTemplateLoader(localFile));
        }
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.DEBUG_HANDLER);


        writePackages(configuration, "package.html", ooRexxPackages);

        Template template = configuration.getTemplate("main.html");

        try (FileWriter fileWriter = new FileWriter(new File(outputDirectory,"oorexxdoc.html"))){
            HashMap<String, Object> map = new HashMap<>();
            map.put("packages", ooRexxPackages);
            map.put("title", title);
            map.put("overview", overview);
            map.put("header", header);
            template.process(map, fileWriter);
        }

    }

    private void writePackages(Configuration configuration, String templatePath, List<OoRexxPackage> packages) throws Exception{
        Template template = configuration.getTemplate(templatePath);

        for(OoRexxPackage ooRexxPackage: packages) {
            FileWriter fileWriter = null;

            try {
                File file = new File(outputDirectory, String.format("packages/%s.html", ooRexxPackage.getId()));

                System.out.println("Writing " + ooRexxPackage.getName() + " documentation to " + outputDirectory);

                FileUtils.touch(file);
                fileWriter = new FileWriter(file);
                HashMap<String, Object> map = new HashMap<>();
                map.put("packages", packages);
                map.put("package", ooRexxPackage);
                map.put("title", title);
                map.put("header", header);
                List<OoRexxClass> classes = ooRexxPackage.getClasses();
                map.put("classes", classes);
                template.process(map, fileWriter);
            }catch (Exception e){
                throw new RuntimeException(e);
            } finally {
                fileWriter.close();
            }
        }
    }

    private void fillSearchTemplate(List<OoRexxPackage> ooRexxPackages) throws Exception{
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_23);
        if(!localFile.exists()) {
            configuration.setClassForTemplateLoading(DocGenerator.class, "/templates");
        } else {
            configuration.setTemplateLoader(new FileTemplateLoader(localFile));
        }
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.DEBUG_HANDLER);


        Template template = configuration.getTemplate("search-data-template.ftl");
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(new File(outputDirectory,"js/search-data.js"));
            HashMap<String, Object> map = new HashMap<>();
            map.put("packages", ooRexxPackages);
            template.process(map, fileWriter);
        } finally {
            fileWriter.close();
        }

    }

    public void copyFromJar(String source, final Path target) throws URISyntaxException, IOException {
        URI resource = getClass().getResource("").toURI();
        try (FileSystem fileSystem = FileSystems.newFileSystem(resource, Collections.<String, String>emptyMap())) {
            final Path jarPath = fileSystem.getPath(source);

            Files.walkFileTree(jarPath, new SimpleFileVisitor<Path>() {
                private Path currentTarget;

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    currentTarget = target.resolve(jarPath.relativize(dir).toString());
                    Files.createDirectories(currentTarget);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.copy(file, target.resolve(jarPath.relativize(file).toString()), REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }

            });
        }
    }
}

package org.oorexx.doc.generate;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.StringTemplateLoader;
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
import org.oorexx.doc.input.intellij.IntelliJGenerationCommand;
import org.oorexx.doc.input.xml.OoRexxDocXml;
import org.oorexx.doc.util.IdGenerator;
import org.oorexx.ide.file.OoRexxFile;
import org.oorexx.ide.file.OoRexxFileType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class DocGenerator {

    private ClassReferenceAnalyzer classReferenceAnalyzer = new ClassReferenceAnalyzer();
    private static final File localFile = new File(new File(System.getProperty("user.dir")), "/templates");
    private List<String> inputFiles;
    private String outputDirectory;
    private String title;
    private String overview;
    private Map<String, String> headers = new HashMap<>();
    private List<VirtualFile> virtualFiles;
    private Settings settings = new Settings();
    private File outputFile;

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
        generateXml.getHeader().getHeaderProperties().forEach(headerProperty -> headers.put(headerProperty.getKey(), headerProperty.getValue()));
    }

    public DocGenerator(IntelliJGenerationCommand command) {
        this.virtualFiles = command.getInputs();
        this.outputDirectory = command.getOutput().getCanonicalPath();
        this.title = command.getTitle();
        this.headers = command.getHeaders();
        this.overview = command.getOverview();
        this.settings.setIncludePrivate(command.isIncludePrivate());
        this.settings.setIncludePackage(command.isIncludePackage());
        this.settings.setIncludePublic(command.isIncludePublic());
        this.settings.setIncludeVariableReferences(command.isIncludeVariableReferences());
    }

    public void generate() throws Exception{
        IdGenerator.reset();
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
                    final OoRexxPackage[] ooRexxPackage = new OoRexxPackage[1];
                    ApplicationManager.getApplication().runReadAction(() -> {
                        PsiElement psi = psiFile.getPsiFile().getNode().getPsi();
                        CommentContext commentContext = new CommentContext((OoRexxFile) psi);
                        ooRexxPackage[0] = new PackageAnalyzer(settings, commentContext).analyze(psiFile.getFile(), (OoRexxFile) psi);
                    });
                    return ooRexxPackage[0];
                })
                .filter(OoRexxPackage::hasElements)
                .collect(Collectors.toList());
    }

    private PsiFile getPsiFile(File file){
        final PsiFileFactory factory = PsiFileFactory.getInstance(IntelliJGenerationCommand.getProject());

        try {
            return factory.createFileFromText(file.getName(), OoRexxFileType.INSTANCE, FileUtils.readFileToString(file, "utf-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private List<OoRexxPackage> getOoRexxPackgesForVirtualFiles() {
        return virtualFiles.parallelStream()
                .map(virtualFile -> {
                    final OoRexxPackage[] ooRexxPackage = {null};
                    ApplicationManager.getApplication().runReadAction(() -> {
                        File file = new File(virtualFile.getCanonicalPath());
                        PsiFile psiFile = getPsiFile(file);
                        PsiElement psi = psiFile.getNode().getPsi();
                        CommentContext commentContext = new CommentContext((OoRexxFile) psi);
                        ooRexxPackage[0] = new PackageAnalyzer(settings, commentContext).analyze(file, (OoRexxFile) psi);
                    });
                    return ooRexxPackage[0];
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


        writePackages(configuration, "package.ftl", ooRexxPackages);

        Template template = configuration.getTemplate("main.ftl");

        outputFile = new File(outputDirectory, "oorexxdoc.html");
        try (FileWriter fileWriter = new FileWriter(outputFile)){
            HashMap<String, Object> map = new HashMap<>();
            map.put("packages", ooRexxPackages);
            map.put("title", title);
            map.put("overview", overview);
            map.put("header", processHeaders(headers));
            template.process(map, fileWriter);
        }

    }

    private Map<String, String> processHeaders(Map<String, String> headers) {
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        headers.entrySet().forEach(entry -> stringTemplateLoader.putTemplate(entry.getKey(), entry.getValue()));
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_23);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.DEBUG_HANDLER);
        configuration.setTemplateLoader(stringTemplateLoader);
        LinkedHashMap<String, String> processedHeaders = new LinkedHashMap<>();
        headers.entrySet().forEach(entry -> {
            try {
                Template template = configuration.getTemplate(entry.getKey());
                StringWriter stringWriter = new StringWriter();
                template.process(new HashMap<String, String>(), stringWriter);
                processedHeaders.put(entry.getKey(), stringWriter.toString());
            } catch (Exception e) {
                processedHeaders.put(entry.getKey(), entry.getValue());
                e.printStackTrace();
            }
        });
        return processedHeaders;
    }

    public String generateXML4IntelliJ(){
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<oorexxdoc>\n<parser>\n";
        for (int i = 0; i < virtualFiles.size(); i++)
            xml += "<input>" + virtualFiles.get(0).getCanonicalPath() + "</input>\n";
        xml += "<output>" + outputDirectory + "</output>\n</parser>\n";
        xml += "<title>" + title + "</title>\n";
        xml += "<overview><![CDATA[" + overview
                + "]]></overview>\n";
        xml += "<header>\n";
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            xml += "<property><key>" + entry.getKey()
                    + "</key><value>" + entry.getValue()
                    + "</value></property>\n";
        }
        xml += "</header>\n";
        xml += "</oorexxdoc>";

        return xml;
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
                map.put("header", processHeaders(headers));
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

    public File getOutputFile() {
        return outputFile;
    }
}

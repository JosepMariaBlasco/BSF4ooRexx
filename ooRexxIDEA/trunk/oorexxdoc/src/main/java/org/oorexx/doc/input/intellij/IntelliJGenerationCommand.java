package org.oorexx.doc.input.intellij;

import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.oorexx.doc.input.model.xml.HeaderProperty;
import org.oorexx.doc.input.model.xml.OoRexxDocXml;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IntelliJGenerationCommand {

    private VirtualFile output;
    private List<VirtualFile> inputs = new ArrayList<>();
    private String title;
    private Map<String, String> headers = new LinkedHashMap<>();
    private String overview;
    private boolean includePublic = true;
    private boolean includePackage;
    private boolean includePrivate;
    private boolean includeVariableReferences;

    public static IntelliJGenerationCommand of(OoRexxDocXml ooRexxDocXml) throws Exception {
        IntelliJGenerationCommand intelliJGenerationCommand = new IntelliJGenerationCommand();
        List<VirtualFile> inputs = ooRexxDocXml.getParser().getInput()
                .stream()
                .map(s -> VfsUtil.findFileByIoFile(new File(s), true))
                .collect(Collectors.toList());
        intelliJGenerationCommand.setInputs(inputs);
        intelliJGenerationCommand.setOutput(VfsUtil.createDirectoryIfMissing(ooRexxDocXml.getParser().getOutput()));

        intelliJGenerationCommand.setTitle(ooRexxDocXml.getTitle());
        intelliJGenerationCommand.setOverview(ooRexxDocXml.getOverview());
        intelliJGenerationCommand.setHeaders(ooRexxDocXml.getHeader().getHeaderProperties().stream().collect(Collectors.toMap(HeaderProperty::getKey, HeaderProperty::getValue)));
        intelliJGenerationCommand.setIncludePrivate(ooRexxDocXml.getSettings().isIncludePrivate());
        intelliJGenerationCommand.setIncludePublic(ooRexxDocXml.getSettings().isIncludePublic());
        intelliJGenerationCommand.setIncludePackage(ooRexxDocXml.getSettings().isIncludePackage());
        intelliJGenerationCommand.setIncludeVariableReferences(ooRexxDocXml.getSettings().isIncludeVariableReferences());
        return intelliJGenerationCommand;
    }

    public VirtualFile getOutput() {
        return output;
    }

    public void setOutput(VirtualFile output) {
        this.output = output;
    }

    public List<VirtualFile> getInputs() {
        return inputs;
    }

    public void setInputs(List<VirtualFile> inputs) {
        this.inputs = inputs;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public boolean isIncludePublic() {
        return includePublic;
    }

    public void setIncludePublic(boolean includePublic) {
        this.includePublic = includePublic;
    }

    public boolean isIncludePackage() {
        return includePackage;
    }

    public void setIncludePackage(boolean includePackage) {
        this.includePackage = includePackage;
    }

    public boolean isIncludePrivate() {
        return includePrivate;
    }

    public void setIncludePrivate(boolean includePrivate) {
        this.includePrivate = includePrivate;
    }

    public void setIncludeVariableReferences(boolean includeVariableReferences) {
        this.includeVariableReferences = includeVariableReferences;
    }

    public boolean isIncludeVariableReferences() {
        return includeVariableReferences;
    }
}

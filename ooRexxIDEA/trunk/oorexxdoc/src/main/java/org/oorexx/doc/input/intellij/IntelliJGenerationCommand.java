package org.oorexx.doc.input.intellij;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IntelliJGenerationCommand {

    private static Project project;
    private VirtualFile output;
    private List<VirtualFile> inputs = new ArrayList<>();
    private String title;
    private Map<String, String> headers = new LinkedHashMap<>();
    private String overview;

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

    public static Project getProject() {
        return project;
    }

    public static void setProject(Project project) {
        IntelliJGenerationCommand.project = project;
    }
}

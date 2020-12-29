package org.oorexx.doc.input.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.CommaParameterSplitter;

import java.util.List;

@Parameters(commandNames = "generate", separators = "=")
public class GenerateCommand {

    @Parameter(names = {"--title", "-t"}, required = true)
    private String title;

    @Parameter(names = {"--inputFilesAndDirectories", "-i"}, required = true)
    private List<String> filesAndDirectories;

    @Parameter(names = {"--outputFiles", "-o"}, required = true)
    private String outputDirectory;

    public String getTitle() {
        return title;
    }

    public List<String> getFilesAndDirectories() {
        return filesAndDirectories;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setFilesAndDirectories(List<String> filesAndDirectories) {
        this.filesAndDirectories = filesAndDirectories;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
}

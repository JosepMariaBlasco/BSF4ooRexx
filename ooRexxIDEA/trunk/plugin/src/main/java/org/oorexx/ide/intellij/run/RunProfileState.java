package org.oorexx.ide.intellij.run;

import java.io.File;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.oorexx.ide.intellij.run.configuration.RunConfiguration;

/**
 * Created by alex on 13.09.16.
 */
public class RunProfileState extends CommandLineState {

    public RunProfileState(ExecutionEnvironment environment) {
        super(environment);
    }

    @NotNull
    @Override
    protected ProcessHandler startProcess() throws ExecutionException {

        RunConfiguration runConfiguration = (RunConfiguration) getEnvironment().getRunProfile();

        String scriptFile = runConfiguration.getScriptFile();

        if (scriptFile == null) {
            throw new ExecutionException("Script file: " + scriptFile + " does not exist.");
        }

        VirtualFile file = VfsUtil.findFileByIoFile(new File(scriptFile), true);

        GeneralCommandLine commandLine = new GeneralCommandLine();
        commandLine.setExePath(runConfiguration.getExecutablePath());
        commandLine.addParameter(FileUtil.toSystemDependentName(file.getPath()));


        OSProcessHandler handler = new OSProcessHandler(commandLine.createProcess(), commandLine.getCommandLineString());
        ProcessTerminatedListener.attach(handler, getEnvironment().getProject());

        return handler;
    }

}

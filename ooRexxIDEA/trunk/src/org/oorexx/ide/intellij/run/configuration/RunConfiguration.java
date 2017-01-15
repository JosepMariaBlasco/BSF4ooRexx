package org.oorexx.ide.intellij.run.configuration;

import com.intellij.execution.CommonProgramRunConfigurationParameters;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.oorexx.ide.intellij.run.*;

/**
 * Created by alex on 12.09.16.
 */
public class RunConfiguration extends LocatableConfigurationBase {

    private VirtualFile scriptFile;
    private String executablePath = "rexx";

    protected RunConfiguration(Project project, ConfigurationFactory factory, String name) {
        super(project, factory, name);
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {

    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment executionEnvironment) throws ExecutionException {
        return new org.oorexx.ide.intellij.run.RunProfileState(executionEnvironment);
    }

    @Override
    public boolean isGeneratedName() {
        return false;
    }

    @Override
    public String suggestedName(){
        VirtualFile scriptFile = getScriptFile();
        return scriptFile == null ? null : scriptFile.getName();
    }

    public VirtualFile getScriptFile() {
        return scriptFile;
    }

    public void setScriptFile(VirtualFile scriptFile) {
        this.scriptFile = scriptFile;
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
        super.readExternal(element);
        XmlSerializer.deserializeInto(this, element);
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);
        XmlSerializer.serializeInto(this, element);
    }

    @NotNull
    @Override
    public SettingsEditor<? extends com.intellij.execution.configurations.RunConfiguration> getConfigurationEditor() {
        return new RunSettingsEditor(getProject());
    }

    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }

    public String getExecutablePath() {
        return executablePath;
    }
}

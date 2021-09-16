package org.oorexx.ide.intellij.run;

import java.awt.*;
import javax.swing.*;

import com.intellij.execution.ui.CommonProgramParametersPanel;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.RawCommandLineEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.oorexx.ide.intellij.run.configuration.RunConfiguration;
import org.oorexx.ide.intellij.run.configuration.RunConfigurationProducer;

/**
 * Created by alex on 12.09.16.
 */
public class RunSettingsEditor extends SettingsEditor<RunConfiguration> {

    private final Project project;
    private JTextField executablePath;
    private TextFieldWithBrowseButton myScriptField;
    private CommonProgramParametersPanel myParametersPanel;
    private ComboBox myConsoleCharset;
    private RawCommandLineEditor myPerlParametersPanel;
    private JPanel panel;

    public RunSettingsEditor(Project project) {
        this.project = project;
    }

    @Override
    protected void resetEditorFrom(@NotNull RunConfiguration runConfiguration) {
        if (runConfiguration.getScriptFile() != null) {
            myScriptField.setText(runConfiguration.getScriptFile());
        }
        executablePath.setText(runConfiguration.getExecutablePath());
    }

    @Override
    protected void applyEditorTo(@NotNull RunConfiguration runConfiguration) throws ConfigurationException {
        runConfiguration.setScriptFile(LocalFileSystem.getInstance().findFileByPath(myScriptField.getText()).getCanonicalPath());
        runConfiguration.setExecutablePath(executablePath.getText());
    }

    @Nullable
    @Override
    protected JComponent createEditor() {
        executablePath = new JTextField();
        myScriptField = new TextFieldWithBrowseButton();
        myScriptField.addBrowseFolderListener(
                "Choose Rexx File",
                "Choose rexx file",
                project,
                FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor().withFileFilter(new Condition<VirtualFile>() {
                    @Override
                    public boolean value(VirtualFile virtualFile) {
                        return RunConfigurationProducer.isExecutableFile(virtualFile);
                    }
                }), TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT);


        panel = new JPanel();
        panel.setLayout(new GridLayout(4, 1));
        panel.add(new JLabel("Rexx Script File:"));
        panel.add(myScriptField);
        panel.add(new JLabel("Rexx Executeable Path:"));
        panel.add(executablePath);

        return panel;
    }
}

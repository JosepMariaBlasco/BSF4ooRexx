package org.oorexx.ide.intellij.run.configuration;

import com.intellij.execution.configurations.*;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.oorexx.ide.intellij.file.OoRexxIcons;

import javax.swing.*;

public class RunConfigurationType extends ConfigurationTypeBase {

    public RunConfigurationType() {
        super("#OoRexxConfiguratioType", "OoRexx", "", OoRexxIcons.FILE);

        addFactory(new ConfigurationFactory(this)
        {
            @Override
            public com.intellij.execution.configurations.RunConfiguration createTemplateConfiguration(Project project)
            {
                return new RunConfiguration(project, this, "Unnamed");
            }
        });
    }

    public static RunConfigurationType getInstance() {
        return CONFIGURATION_TYPE_EP.findExtension(RunConfigurationType.class);
    }
}

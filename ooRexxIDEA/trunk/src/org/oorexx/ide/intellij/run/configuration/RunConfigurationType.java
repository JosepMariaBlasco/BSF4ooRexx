package org.oorexx.ide.intellij.run.configuration;

import com.intellij.execution.configurations.*;
import com.intellij.openapi.project.Project;
import org.oorexx.ide.file.OoRexxIcons;

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

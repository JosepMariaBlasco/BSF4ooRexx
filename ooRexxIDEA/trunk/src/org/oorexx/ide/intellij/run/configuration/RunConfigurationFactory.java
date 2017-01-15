package org.oorexx.ide.intellij.run.configuration;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.openapi.project.Project;

/**
 * Created by alex on 12.09.16.
 */
public class RunConfigurationFactory extends ConfigurationFactory {
    private static final String FACTORY_NAME = "OoRexx configuration factory";

    protected RunConfigurationFactory(ConfigurationType type) {
        super(type);
    }

    @Override
    public RunConfiguration createTemplateConfiguration(Project project) {
        return new RunConfiguration(project, this, "OoRexx");
    }

    @Override
    public String getName() {
        return FACTORY_NAME;
    }
}

package org.oorexx.ide.intellij.run.configuration;

import com.intellij.execution.configurations.ConfigurationTypeBase;
import org.oorexx.ide.file.OoRexxIcons;

public class RunConfigurationType extends ConfigurationTypeBase {

    public RunConfigurationType() {
        super("OoRexxRunConfigurationType", "OoRexx", "", OoRexxIcons.FILE);

        addFactory(new RunConfigurationFactory(this));
    }


    public static RunConfigurationType getInstance() {
        return CONFIGURATION_TYPE_EP.findExtension(RunConfigurationType.class);
    }
}

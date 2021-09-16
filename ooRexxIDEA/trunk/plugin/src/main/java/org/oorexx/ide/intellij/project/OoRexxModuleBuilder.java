package org.oorexx.ide.intellij.project;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModifiableRootModel;

public class OoRexxModuleBuilder extends ModuleBuilder {

    @Override
    public void setupRootModel(ModifiableRootModel rootModel) throws ConfigurationException {
        this.doAddContentEntry(rootModel);
    }

    @Override
    public ModuleType getModuleType() {
        return new OoRexxModuleType();
    }
}

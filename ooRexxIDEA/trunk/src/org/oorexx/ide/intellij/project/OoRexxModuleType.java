package org.oorexx.ide.intellij.project;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.NotNull;
import org.oorexx.ide.intellij.file.OoRexxIcons;

import javax.swing.*;

public class OoRexxModuleType extends ModuleType {

    public static final String OOREXX_MODULE = "OOREXX_MODULE";

    public OoRexxModuleType() {
        super(OOREXX_MODULE);
    }

    @NotNull
    @Override
    public ModuleBuilder createModuleBuilder() {
        return new OoRexxModuleBuilder();
    }

    @NotNull
    @Override
    public String getName() {
        return "ooRexx";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Add Support for ooRexx";
    }

    @Override
    public Icon getNodeIcon(boolean b) {
        return OoRexxIcons.NODE;
    }
}

package org.oorexx.ide.intellij.setting;


import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurableProvider;
import org.jetbrains.annotations.Nullable;

public class OoRexxConfigurableProvider extends ConfigurableProvider{

    @Nullable
    @Override
    public Configurable createConfigurable() {
        return new OoRexxLanguageConfigurable();
    }
}

package org.oorexx.ide.setting;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.Nullable;

@State(
        name = "OoRexxLanguageSettings",
        storages = {
                @Storage("ooRexx.xml"),
        }
)
public class OoRexxLanguageSettings implements PersistentStateComponent<OoRexxLanguageSettings> {

    private boolean allowSpecialChars = false;

    @Nullable
    @Override
    public OoRexxLanguageSettings getState() {
        return this;
    }

    @Override
    public void loadState(OoRexxLanguageSettings ooRexxLanguageSettings) {
        this.allowSpecialChars = ooRexxLanguageSettings.allowSpecialChars;
    }

    public static OoRexxLanguageSettings getInstance() {
        return ServiceManager.getService(OoRexxLanguageSettings.class);
    }

    public boolean isAllowSpecialChars() {
        return allowSpecialChars;
    }

    public void setAllowSpecialChars(boolean allowSpecialChars) {
        this.allowSpecialChars = allowSpecialChars;
    }
}

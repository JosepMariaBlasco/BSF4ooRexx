package org.oorexx.ide.intellij.setting;

import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;
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
        XmlSerializerUtil.copyBean(ooRexxLanguageSettings, this);
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

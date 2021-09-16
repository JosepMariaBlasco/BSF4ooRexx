package org.oorexx.ide.intellij.action.configuration;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "ooRexxDocQuickActionPersistentState", storages = @Storage("ooRexxDocQuickSave.xml"))
public class OoRexxDocQuickActionPersistentStateComponent implements PersistentStateComponent<OoRexxDocQuickActionPersistentStateComponent> {

    private String xml;

    @Override
    public void loadState(@NotNull OoRexxDocQuickActionPersistentStateComponent state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @Override
    public @Nullable OoRexxDocQuickActionPersistentStateComponent getState() {
        return this;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }
}

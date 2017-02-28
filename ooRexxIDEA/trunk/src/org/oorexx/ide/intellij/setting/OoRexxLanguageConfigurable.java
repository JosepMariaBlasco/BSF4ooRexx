package org.oorexx.ide.intellij.setting;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class OoRexxLanguageConfigurable implements Configurable {

    private JCheckBox allowSpecialCharsCheckbox;

    private OoRexxLanguageSettings ooRexxLanguageSettings;

    public OoRexxLanguageConfigurable() {
        ooRexxLanguageSettings = OoRexxLanguageSettings.getInstance();
        ooRexxLanguageSettings.isAllowSpecialChars();
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "ooRexx";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        FormBuilder builder = FormBuilder.createFormBuilder();
        builder.getPanel().setLayout(new VerticalFlowLayout());

        allowSpecialCharsCheckbox = new JCheckBox("Allow special characters (\u0040, \u0023, \u00A2, \u0024). Requires restart.");

        return builder
                .addComponent(allowSpecialCharsCheckbox)
                .getPanel();
    }

    @Override
    public boolean isModified() {
        return allowSpecialCharsCheckbox.isSelected() != ooRexxLanguageSettings.isAllowSpecialChars();
    }

    @Override
    public void apply() throws ConfigurationException {
        ooRexxLanguageSettings.setAllowSpecialChars(allowSpecialCharsCheckbox.isSelected());
    }

    @Override
    public void reset() {
        allowSpecialCharsCheckbox.setSelected(ooRexxLanguageSettings.isAllowSpecialChars());
    }

    @Override
    public void disposeUIResources() {

    }
}

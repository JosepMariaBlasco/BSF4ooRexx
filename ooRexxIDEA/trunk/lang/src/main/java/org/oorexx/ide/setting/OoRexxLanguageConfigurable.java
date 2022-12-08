package org.oorexx.ide.setting;

import javax.swing.*;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class OoRexxLanguageConfigurable implements Configurable {

    private final OoRexxLanguageSettings ooRexxLanguageSettings;
    private JCheckBox allowSpecialCharsCheckbox;
    private JCheckBox enableExecutorFeatureCheckbox;
    private ComboBox<String> completionCasing;

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
        enableExecutorFeatureCheckbox = new JCheckBox("Enable executor parsing (https://github.com/jlfaucher/executor)");
        completionCasing = new ComboBox<>(OoRexxLanguageSettings.AutoCompletionCasing.getReadableNames());

        JPanel completionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        completionPanel.add(new JLabel("Auto Completion Casing. Requires restart."));
        completionPanel.add(completionCasing);


        return builder
                .addComponent(allowSpecialCharsCheckbox)
                .addComponent(enableExecutorFeatureCheckbox)
                .addComponent(completionPanel)
                .getPanel();
    }

    @Override
    public boolean isModified() {
        return
                allowSpecialCharsCheckbox.isSelected() != ooRexxLanguageSettings.isAllowSpecialChars() ||
                        enableExecutorFeatureCheckbox.isSelected() != ooRexxLanguageSettings.isEnableExecutorFeature() ||
                        completionCasing.getSelectedItem() != ooRexxLanguageSettings.getAutoCompletionCasing()
                ;
    }

    @Override
    public void apply() throws ConfigurationException {
        ooRexxLanguageSettings.setAllowSpecialChars(allowSpecialCharsCheckbox.isSelected());
        ooRexxLanguageSettings.setEnableExecutorFeature(enableExecutorFeatureCheckbox.isSelected());
        ooRexxLanguageSettings.setAutoCompletionCasing(
                OoRexxLanguageSettings.AutoCompletionCasing.fromReadableName((String) completionCasing.getSelectedItem())
        );
    }

    @Override
    public void reset() {
        allowSpecialCharsCheckbox.setSelected(ooRexxLanguageSettings.isAllowSpecialChars());
        enableExecutorFeatureCheckbox.setSelected(ooRexxLanguageSettings.isEnableExecutorFeature());
        completionCasing.setSelectedItem(ooRexxLanguageSettings.getAutoCompletionCasing().getReadableName());
    }

    @Override
    public void disposeUIResources() {

    }
}

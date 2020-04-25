package org.oorexx.ide.intellij.oorexxdoc;

import javax.swing.*;

public class KeyValueForm {
    private JTextField key;
    private JTextField value;
    private JPanel panel;

    public KeyValueForm() {
    }

    public KeyValueForm(KeyValuePair keyValuePair) {
        this.key.setText(keyValuePair.getKey());
        this.value.setText(keyValuePair.getValue());
    }


    public KeyValuePair getKeyValuePair(){
        return new KeyValuePair(key.getText(), value.getText());
    }

    public JPanel getRoot(){
        return panel;
    }
}

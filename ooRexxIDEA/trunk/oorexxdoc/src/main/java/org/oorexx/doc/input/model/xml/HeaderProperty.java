package org.oorexx.doc.input.model.xml;

public class HeaderProperty {

    private String key;
    private String value;

    public HeaderProperty(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public HeaderProperty() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

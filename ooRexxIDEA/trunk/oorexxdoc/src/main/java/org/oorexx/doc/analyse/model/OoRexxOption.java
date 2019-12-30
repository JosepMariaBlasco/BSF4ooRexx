package org.oorexx.doc.analyse.model;

public class OoRexxOption {

    private String key;
    private String value;

    public OoRexxOption(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public OoRexxOption(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}

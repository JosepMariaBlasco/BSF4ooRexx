package org.oorexx.doc.analyse.model;

import com.google.common.collect.Lists;

import java.util.ArrayList;

public class OoRexxCommentAnnotation {

    private String key;
    private String value;
    private String firstWord;
    private String remainder;

    public OoRexxCommentAnnotation(String key, String value) {
        this.key = key;
        this.value = value;

        if(value == null) {
            this.firstWord = "";
            this.remainder = "";
            return;
        }

        if(!value.contains(" ")) {
            this.firstWord = value;
            this.remainder = "";
            return;
        }

        String[] valueParts = value.split(" ");
        this.firstWord = valueParts[0];

        ArrayList<String> words = Lists.newArrayList(valueParts);
        words.remove(0);

        this.remainder = String.join(" ", words);
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

    public String getFirstWord() {
        return firstWord;
    }

    public void setFirstWord(String firstWord) {
        this.firstWord = firstWord;
    }

    public String getRemainder() {
        return remainder;
    }

    public void setRemainder(String remainder) {
        this.remainder = remainder;
    }
}

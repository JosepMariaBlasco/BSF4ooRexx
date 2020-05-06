package org.oorexx.doc.input.model.xml;


import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

public class Parser {
    @JacksonXmlProperty(localName = "input")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<String> input;
    private String output;

    public List<String> getInput() {
        return input;
    }

    public void setInput(List<String> input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}

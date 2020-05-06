package org.oorexx.doc.input.model;

import org.oorexx.doc.generate.Settings;
import org.oorexx.doc.input.model.xml.OoRexxDocXml;
import org.oorexx.doc.input.model.xml.Header;
import org.oorexx.doc.input.model.xml.HeaderProperty;
import org.oorexx.doc.input.model.xml.Parser;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OoRexxDocGenerationContext {

    private List<String> inputs;
    private String output;
    private Map<String, String> headers;
    private String title;
    private String overview;
    private Settings settings = new Settings();

    public List<String> getInputs() {
        return inputs;
    }

    public void setInputs(List<String> inputs) {
        this.inputs = inputs;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public OoRexxDocXml toXml(){
        OoRexxDocXml ooRexxDocXml = new OoRexxDocXml();
        ooRexxDocXml.setSettings(new org.oorexx.doc.input.model.xml.Settings());
        ooRexxDocXml.getSettings().setIncludePackage(getSettings().isIncludePackage());
        ooRexxDocXml.getSettings().setIncludePrivate(getSettings().isIncludePrivate());
        ooRexxDocXml.getSettings().setIncludePublic(getSettings().isIncludePublic());
        ooRexxDocXml.getSettings().setIncludeVariableReferences(getSettings().isIncludeVariableReferences());
        ooRexxDocXml.setTitle(getTitle());
        ooRexxDocXml.setOverview(getOverview());
        ooRexxDocXml.setParser(new Parser());
        ooRexxDocXml.getParser().setInput(getInputs());
        ooRexxDocXml.getParser().setOutput(getOutput());
        ooRexxDocXml.setHeader(new Header());
        List<HeaderProperty> headerProperties = getHeaders().entrySet().stream().map(entry -> new HeaderProperty(entry.getKey(), entry.getValue())).collect(Collectors.toList());
        ooRexxDocXml.getHeader().setHeaderProperties(headerProperties);
        return ooRexxDocXml;
    }
}

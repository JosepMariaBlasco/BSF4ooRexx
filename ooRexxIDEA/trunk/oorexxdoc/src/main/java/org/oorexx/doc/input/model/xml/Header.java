package org.oorexx.doc.input.model.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.ArrayList;
import java.util.List;

public class Header {

    @JacksonXmlProperty(localName = "property")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<HeaderProperty> headerProperties = new ArrayList<>();

    public List<HeaderProperty> getHeaderProperties() {
        return headerProperties;
    }

    public void setHeaderProperties(List<HeaderProperty> headerProperties) {
        this.headerProperties = headerProperties;
    }
}

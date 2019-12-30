package org.oorexx.doc.input.xml;

import java.util.ArrayList;
import java.util.List;

public class Header {

    private List<HeaderProperty> headerProperties = new ArrayList<>();

    public List<HeaderProperty> getHeaderProperties() {
        return headerProperties;
    }

    public void setHeaderProperties(List<HeaderProperty> headerProperties) {
        this.headerProperties = headerProperties;
    }
}

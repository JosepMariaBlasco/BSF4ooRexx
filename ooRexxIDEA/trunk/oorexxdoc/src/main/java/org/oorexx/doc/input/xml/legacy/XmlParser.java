package org.oorexx.doc.input.xml.legacy;

import org.oorexx.doc.input.model.xml.Header;
import org.oorexx.doc.input.model.xml.HeaderProperty;
import org.oorexx.doc.input.model.xml.OoRexxDocXml;
import org.oorexx.doc.input.model.xml.Parser;

import java.io.File;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Deprecated
public class XmlParser {

    public static OoRexxDocXml get(String xml){
        OoRexxDocXmlParser p = new OoRexxDocXmlParser(xml);
        OoRexxDocXml ooRexxDocXml = new OoRexxDocXml();
        ooRexxDocXml.setOverview(p.getOverview());
        ooRexxDocXml.setTitle(p.getTitle());
        ooRexxDocXml.setSettings(p.getSettings());

        Parser parser = new Parser();
        parser.setInput(p.getSrcFiles().stream().map(File::getAbsolutePath).collect(Collectors.toList()));
        parser.setOutput(p.getDestFile().getAbsolutePath());
        ooRexxDocXml.setParser(parser);
        Header header = new Header();
        p.getHeaderData().forEach(new BiConsumer<String, String>() {
            @Override
            public void accept(String s, String s2) {
                header.getHeaderProperties().add(new HeaderProperty(s, s2));
            }
        });
        ooRexxDocXml.setHeader(header);
        return ooRexxDocXml;
    }

}

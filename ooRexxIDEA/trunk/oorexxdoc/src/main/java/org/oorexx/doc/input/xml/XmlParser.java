package org.oorexx.doc.input.xml;

import org.oorexx.doc.input.xml.legacy.OoRexxDocXmlParser;

import java.io.File;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class XmlParser {

    public static OoRexxDocXml get(String xml){
        OoRexxDocXmlParser p = new OoRexxDocXmlParser(xml);
        OoRexxDocXml ooRexxDocXml = new OoRexxDocXml();
        ooRexxDocXml.setOverview(p.getOverview());
        ooRexxDocXml.setTitle(p.getTitle());

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

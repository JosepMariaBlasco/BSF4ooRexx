package org.oorexx.doc.input.xml.legacy;

import org.apache.commons.io.FileUtils;
import org.oorexx.doc.generate.DocGenerator;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import sun.applet.Main;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

@Deprecated
public class OoRexxDocXmlParser {

    private String xml;
    private DocGenerator generator;
    private File destFile;
    private String title;
    private String overview;
    private LinkedHashMap<String, String> headerData;
    private List<File> srcFiles;

    public OoRexxDocXmlParser(String xml) {

        this.xml = xml;
        try {
            parse();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parse() {
        try {
            Document document = loadXMLFromString();
            document.getDocumentElement().normalize();

            NodeList nodelist = document.getChildNodes().item(0)
                    .getChildNodes();
            for (int i = 0; i < nodelist.getLength(); i++) {
                if (nodelist.item(i).getNodeName().toLowerCase()
                        .equals("parser"))
                    parseParser(nodelist.item(i));
                else {
                    parseDoc(nodelist.item(i));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void parseParser(Node node) {
        this.srcFiles = new ArrayList<File>();
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            if (node.getChildNodes().item(i).getNodeName().toLowerCase()
                    .equals("input")) {
                File srcFile = new File(node.getChildNodes().item(i)
                        .getTextContent());
//				if (srcFile.isFile())
//					srcFiles.add(srcFile.getParentFile());
//				else
                srcFiles.add(srcFile);
            }
            if (node.getChildNodes().item(i).getNodeName().toLowerCase()
                    .equals("output"))
                destFile = new File(node.getChildNodes().item(i)
                        .getTextContent());
        }
    }

    private void parseDoc(Node node) throws DOMException, IOException {
        if (node.getNodeName().toLowerCase().equals("title"))
            title = node.getTextContent();

        if (node.getNodeName().toLowerCase().equals("overview")) {
            overview = node.getTextContent();

            if (new File(overview).exists())
                overview = FileUtils.readFileToString(new File(node
                        .getTextContent()));
        }

        if (node.getNodeName().toLowerCase().equals("header"))
            parseHeader(node.getChildNodes());
    }

    private void parseHeader(NodeList nodes) {
        headerData = new LinkedHashMap<String, String>();

        for (int i = 0; i < nodes.getLength(); i++) {
            String key = null;
            String value = null;

            for (int k = 0; k < nodes.item(i).getChildNodes().getLength(); k++) {
                if (nodes.item(i).getChildNodes().item(k).getNodeName()
                        .toLowerCase().equals("key"))
                    key = nodes.item(i).getChildNodes().item(k)
                            .getTextContent();
                if (nodes.item(i).getChildNodes().item(k).getNodeName()
                        .toLowerCase().equals("value"))
                    value = nodes.item(i).getChildNodes().item(k)
                            .getTextContent();
            }

            if (key != null)
                this.headerData.put(key, value);
        }
    }


    public DocGenerator getGenerator() {
        return generator;
    }

    public Document loadXMLFromString() throws ParserConfigurationException,
            SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));

        return builder.parse(is);
    }

    public String getXml() {
        return xml;
    }

    public File getDestFile() {
        return destFile;
    }

    public String getTitle() {
        return title;
    }

    public String getOverview() {
        return overview;
    }

    public LinkedHashMap<String, String> getHeaderData() {
        return headerData;
    }

    public List<File> getSrcFiles() {
        return srcFiles;
    }
}

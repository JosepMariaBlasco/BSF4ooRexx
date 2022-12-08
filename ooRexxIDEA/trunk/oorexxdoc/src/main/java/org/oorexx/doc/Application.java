package org.oorexx.doc;

import org.apache.commons.io.FileUtils;
import org.oorexx.doc.generate.DocGeneratorBase;
import org.oorexx.doc.generate.StandaloneDocGenerator;
import org.oorexx.doc.input.model.xml.OoRexxDocXml;
import org.oorexx.doc.input.xml.legacy.XmlParser;

import java.io.File;

public class Application {

    public static void main(String[] args) {
        if (args.length == 1) {
            File file = null;
            String arg = args[0];
            try {
                file = new File(arg);
            } catch (Exception e) {
                // not a file
            }

            if (file != null && file.exists()) {
                System.out.println("File input: " + file.getAbsolutePath());
                try {
                    generateXml(file);
                    printSuccess();
                } catch (Exception e) {
                    e.printStackTrace();
                    printXMLHint(args);
                }
            } else {
                try {
                    if(!arg.contains("<")){
                        System.out.println("File does not exist: " + file.getAbsolutePath());
                    }
                    System.out.println("using input as string");
                    generateXml(arg);
                    printSuccess();
                } catch (Exception e) {
                    e.printStackTrace();
                    printXMLHint(args);
                }
            }
        }

        if (args.length != 1) {
            printXMLHint(new String[]{});
        }

    }

    private static void printSuccess() {
        System.out.println("ooRexxDoc Documentation Generated");
    }

    private static void printXMLHint(String[] args) {
        System.out.println("The input is neither a file nor an valid XML content.\n A valid ooRexxDoc xml looks like:" + args[0]);
        System.out.println("See Usage.md");
    }

    private static void generateXml(File file) throws Exception {
        String xmlContent = FileUtils.readFileToString(file, "UTF-8");
        OoRexxDocXml ooRexxDocXml = XmlParser.get(xmlContent);
        DocGeneratorBase docGenerator = new StandaloneDocGenerator(ooRexxDocXml);
        docGenerator.generate();
    }

    private static void generateXml(String xmlContent) throws Exception {
        OoRexxDocXml ooRexxDocXml = XmlParser.get(xmlContent);
        DocGeneratorBase docGenerator = new StandaloneDocGenerator(ooRexxDocXml);
        docGenerator.generate();
    }
}

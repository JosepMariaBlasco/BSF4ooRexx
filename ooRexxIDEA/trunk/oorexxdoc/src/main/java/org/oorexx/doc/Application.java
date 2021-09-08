package org.oorexx.doc;

import java.io.File;
import java.util.Optional;
import java.util.stream.Stream;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.MissingCommandException;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.internal.Console;
import org.apache.commons.io.FileUtils;
import org.oorexx.doc.generate.DocGeneratorBase;
import org.oorexx.doc.generate.StandaloneDocGenerator;
import org.oorexx.doc.input.cli.GenerateCommand;
import org.oorexx.doc.input.cli.HelpCommand;
import org.oorexx.doc.input.model.xml.OoRexxDocXml;
import org.oorexx.doc.input.xml.legacy.XmlParser;

public class Application {

    private static JCommander jCommander;
    private static GenerateCommand generateCommand;
    private static HelpCommand helpCommand;

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            File file = null;

            try {
                file = new File(System.getProperty("user.dir"));
            } catch (Exception e) {
                // not a file
                e.printStackTrace();
            }
            if (file != null) {
                Optional<File> ooRexxDocConfig = Stream.of(file.listFiles()).filter(file1 -> file1.getName().equalsIgnoreCase("oorexxdoc.xml")).findAny();

                if (ooRexxDocConfig.isPresent()) {
                    generateXml(ooRexxDocConfig.get());
                    return;
                }
            }
        }

        if (args.length == 1) {
            File file = null;
            try {
                file = new File(args[0]);
            } catch (Exception e) {
                // not a file
            }
            if (file != null && file.exists()) {
                generateXml(file);
                return;
            }
        }

        initCommands();

        try {
            String parsedCommand = parse(args);

            if ("help".equals(parsedCommand)) {
                printHelp();
            }

            if ("generate".equals(parsedCommand)) {
                generate();
            }
        } catch (MissingCommandException e) {
            jCommander.usage();
        } catch (ParameterException e) {
            System.out.println(e.getMessage());
        }

        if (args.length == 0) {
            printHelp();
            return;
        }

    }

    protected static void initCommands() {
        generateCommand = new GenerateCommand();
        helpCommand = new HelpCommand();
        jCommander = JCommander
                .newBuilder()
                .addCommand(generateCommand)
                .addCommand(helpCommand)
                .build();
    }

    protected static String parse(String[] args) {
        jCommander.parse(args);
        return jCommander.getParsedCommand();
    }


    private static void printHelp() {
        jCommander.usage();
    }

    private static void generate() throws Exception {
        DocGeneratorBase docGenerator = new StandaloneDocGenerator(generateCommand);
        docGenerator.generate();
    }

    protected static GenerateCommand getGenerateCommand() {
        return generateCommand;
    }

    protected static HelpCommand getHelpCommand() {
        return helpCommand;
    }

    private static void generateXml(File file) throws Exception {
        String xmlContent = FileUtils.readFileToString(file, "UTF-8");
        OoRexxDocXml ooRexxDocXml = XmlParser.get(xmlContent);
        DocGeneratorBase docGenerator = new StandaloneDocGenerator(ooRexxDocXml);
        docGenerator.generate();
    }

    public static void setConsole(Console console) {
        jCommander.setConsole(console);
    }

    public static void printUsage() {
        jCommander.usage();
    }
}

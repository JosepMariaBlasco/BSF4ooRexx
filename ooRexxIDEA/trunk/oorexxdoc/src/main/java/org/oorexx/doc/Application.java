package org.oorexx.doc;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.MissingCommandException;
import com.beust.jcommander.ParameterException;
import org.apache.commons.io.FileUtils;
import org.oorexx.doc.generate.DocGenerator;
import org.oorexx.doc.input.cli.GenerateCommand;
import org.oorexx.doc.input.cli.HelpCommand;
import org.oorexx.doc.input.gui.OORexxDocGUI;
import org.oorexx.doc.input.model.xml.OoRexxDocXml;
import org.oorexx.doc.input.xml.legacy.XmlParser;

import java.io.File;
import java.util.Optional;
import java.util.stream.Stream;

public class Application {

    private static JCommander jCommander;
    private static GenerateCommand generateCommand;
    private static HelpCommand helpCommand;

    public static void main(String[] args) throws Exception{

        if(System.console() == null && args.length == 0){
            new OORexxDocGUI().setVisible(true);
            return;
        }

        if(args.length == 0){
            File file = null;

            try {
                file = new File(System.getProperty("user.dir"));
            }catch (Exception e){
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

        if(args.length == 1){
            File file = null;
            try {
                file = new File(args[0]);
            }catch (Exception e){
                // not a file
            }
            if(file != null && file.exists()){
                generateXml(file);
                return;
            }
        }

        initCommands();

        try {
            String parsedCommand = parse(args);

            if("help".equals(parsedCommand)){
                printHelp();
            }

            if("generate".equals(parsedCommand)){
                generate();
            }
        } catch (MissingCommandException e){
            System.out.println(getUsage());
        } catch (ParameterException e){
            System.out.println(e.getMessage());
        }

        if(args.length == 0) {
            printHelp();
            return;
        }

    }

    protected static void initCommands(){
        generateCommand = new GenerateCommand();
        helpCommand = new HelpCommand();
        jCommander = JCommander
                .newBuilder()
                .addCommand(generateCommand)
                .addCommand(helpCommand)
                .build();
    }

    protected static String parse(String[] args){
        jCommander.parse(args);
        return jCommander.getParsedCommand();
    }


    private static void printHelp(){
        System.out.println(getUsage());
    }

    protected static String getUsage(){
        StringBuilder stringBuilder = new StringBuilder();
        jCommander.usage(stringBuilder);
        return stringBuilder.toString();
    }

    private static void generate() throws Exception{
        DocGenerator docGenerator = new DocGenerator(generateCommand);
        docGenerator.generate();
    }

    protected static GenerateCommand getGenerateCommand() {
        return generateCommand;
    }

    protected static HelpCommand getHelpCommand() {
        return helpCommand;
    }

    private static void generateXml(File file)throws Exception{
        String xmlContent = FileUtils.readFileToString(file, "UTF-8");
        OoRexxDocXml ooRexxDocXml = XmlParser.get(xmlContent);
        DocGenerator docGenerator = new DocGenerator(ooRexxDocXml);
        docGenerator.generate();
    }
}

package org.oorexx.doc;

import com.beust.jcommander.MissingCommandException;
import org.junit.Before;
import org.junit.Test;
import org.oorexx.doc.generate.DocGenerator;
import org.oorexx.doc.input.cli.GenerateCommand;
import org.oorexx.doc.input.model.xml.OoRexxDocXml;
import org.oorexx.doc.input.xml.legacy.XmlParser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;

public class ApplicationTest {

    @Before
    public void init(){
        Application.initCommands();
    }

    @Test
    public void testSingleInputParam() throws Exception {

        String inputDirectory = "/home/test/";
        String outputDirectory = "/home/test/output";
        String title = "title";

        String[] args = {"generate", "-i="+inputDirectory, "-o="+outputDirectory, "-t="+title};

        Application.parse(args);

        GenerateCommand command = Application.getGenerateCommand();

        assertThat(command.getTitle(), is(title));
        assertThat(command.getFilesAndDirectories().size(), is(1));
        assertThat(command.getFilesAndDirectories().get(0), is(inputDirectory));
        assertThat(command.getOutputDirectory(), is(outputDirectory));
    }

    @Test
    public void testMultipleInputParam() throws Exception {
        String inputFile = "/home/test/a.file";
        String inputDirectory = "/home/test/";
        String outputDirectory = "/home/test/output";
        String title = "title";

        String[] args = {"generate", "-i="+inputDirectory +  "," + inputFile, "-o="+outputDirectory, "-t="+title};
        Application.parse(args);

        GenerateCommand command = Application.getGenerateCommand();

        assertThat(command.getTitle(), is(title));
        assertThat(command.getFilesAndDirectories().size(), is(2));
        assertThat(command.getFilesAndDirectories().get(0), is(inputDirectory));
        assertThat(command.getFilesAndDirectories().get(1), is(inputFile));
        assertThat(command.getOutputDirectory(), is(outputDirectory));
    }

    @Test(expected = MissingCommandException.class)
    public void testUnknownParam() throws Exception {
        String inputFile = "/home/test/a.file";
        String inputDirectory = "/home/test/";
        String outputDirectory = "/home/test/output";
        String title = "title";

        String[] args = {"unknown", "-i="+inputDirectory +  "," + inputFile, "-o="+outputDirectory, "-t="+title};
        Application.parse(args);
    }

    @Test
    public void testUsage() throws Exception {
        String inputFile = "/home/test/a.file";
        String inputDirectory = "/home/test/";
        String outputDirectory = "/home/test/output";
        String title = "title";

        String[] args = {"generate", "-i="+inputDirectory +  "," + inputFile, "-o="+outputDirectory, "-t="+title};
        Application.parse(args);

        String usage = Application.getUsage();

        assertNotNull(usage);
    }

    @Test
    public void testHelp() throws Exception{
        String inputFile = "/home/test/a.file";
        String inputDirectory = "/home/test/";
        String outputDirectory = "/home/test/output";
        String title = "title";

        String help = "help";

        String[] args = {help, "-i="+inputDirectory +  "," + inputFile, "-o="+outputDirectory, "-t="+title};

        String parsedCommand = Application.parse(args);
        assertThat(parsedCommand, is(help));

    }

    @Test
    public void testBsf4ooRexx() throws Exception {
        String inputDirectory = "/opt/BSF4ooRexx/";
        String outputDirectory = "/home/alex/test/output";
        String title = "title";

        String[] args = {"generate", "-i="+inputDirectory, "-o="+outputDirectory, "-t="+title};

        Application.main(args);

    }

    @Test
    public void testBsf4ooRexxClR() throws Exception {
        String inputDirectory = "C:\\Users\\seikal\\IdeaProjects\\ooRexxTestProject\\a.rexx";
        String outputDirectory = "/home/alex/test/output";
        String title = "title";

        String[] args = {"generate", "-i="+inputDirectory, "-o="+outputDirectory, "-t="+title};

        Application.main(args);

    }

    @Test
    public void testBsf4ooRexxClRAndBSF() throws Exception {
        String inputDirectory = "/opt/BSF4ooRexx/clr/CLR.CLS,/opt/BSF4ooRexx/BSF.CLS";
        String outputDirectory = "/home/alex/test/output";
        String title = "title";

        String[] args = {"generate", "-i="+inputDirectory, "-o="+outputDirectory, "-t="+title};

        Application.main(args);

    }

    @Test
    public void testAnnotate() throws Exception {
        String inputDirectory = "/home/alex/test/";
        String outputDirectory = "/home/alex/test/output";
        String title = "title";

        String[] args = {"generate", "-i="+inputDirectory, "-o="+outputDirectory, "-t="+title};

        Application.main(args);

    }

    @Test
    public void testRequires() throws Exception {
        String inputDirectory = "/home/alex/Workspace/Java/RexxIDE/src/req";
        String outputDirectory = "/home/alex/test/output";
        String title = "title";

        String[] args = {"generate", "-i="+inputDirectory, "-o="+outputDirectory, "-t="+title};

        Application.main(args);

    }

    @Test
    public void testMulti() throws Exception {
        String inputDirectory = "/home/alex/test/test-multi";
        String outputDirectory = "/home/alex/test/output";
        String title = "title";

        String[] args = {"generate", "-i="+inputDirectory, "-o="+outputDirectory, "-t="+title};

        Application.main(args);

    }

    @Test
    public void testStrange() throws Exception {
        String inputDirectory = "/opt/BSF4ooRexx/utilities/OOo/addLinks2OOoSnippets.rxo";
        String outputDirectory = "/home/alex/test/output";
        String title = "title";

        String[] args = {"generate", "-i="+inputDirectory, "-o="+outputDirectory, "-t="+title};

        Application.main(args);

    }

    @Test
    public void testXmlConf() throws Exception{
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<oorexxdoc>\n" +
                "\t<parser>\n" +
                "\t\t<input>/opt/BSF4ooRexx/clr/CLR.CLS</input>\n" +
                "\t\t<input>/opt/BSF4ooRexx/BSF.CLS</input>\n" +
                "\t\t<output>/home/alex/test/output</output>\n" +
                "\t</parser>\n" +
                "\t<title>My Title</title>\n" +
                "\t<header>\n" +
                "\t\t<property>\n" +
                "\t\t\t<key>Author</key>\n" +
                "\t\t\t<value>Not Alex</value>\n" +
                "\t\t</property>\n" +
                "\t\t<property>\n" +
                "\t\t\t<key>Version</key>\n" +
                "\t\t\t<value>> 1.0</value>\n" +
                "\t\t</property>\n" +
                "\t</header>\n" +
                "\t<overview>Sample Text</overview>\n" +
                "</oorexxdoc>";

        OoRexxDocXml ooRexxDoc = XmlParser.get(xml);

        DocGenerator docGenerator = new DocGenerator(ooRexxDoc);
        docGenerator.generate();
    }

    @Test
    public void testXmlFileConf() throws Exception {
        String confPath= "/home/alex/test/doc.xml";
        Application.main(new String[]{confPath});
    }


    @Test
    public void testXmlFileConfRelative() throws Exception {
        Application.main(new String[]{});
    }

    @Test
    public void testAnnotation() throws Exception{
        String inputDirectory = "/home/alex/test/test.rexx";
        String outputDirectory = "/home/alex/test/output";
        String title = "title";

        String[] args = {"generate", "-i="+inputDirectory, "-o="+outputDirectory, "-t="+title};

        Application.main(args);
    }

}
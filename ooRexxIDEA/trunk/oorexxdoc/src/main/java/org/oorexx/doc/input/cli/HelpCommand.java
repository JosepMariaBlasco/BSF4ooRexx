package org.oorexx.doc.input.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.util.List;

@Parameters(commandNames = {"help", "?"})
public class HelpCommand {

    @Parameter
    private List<String> parameters;

}

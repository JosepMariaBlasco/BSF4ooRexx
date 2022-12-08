package org.oorexx.doc.input.model;

import com.intellij.openapi.vfs.VirtualFile;
import org.oorexx.doc.input.intellij.IntelliJGenerationCommand;
import org.oorexx.doc.input.model.xml.OoRexxDocXml;
import org.oorexx.doc.input.model.xml.HeaderProperty;

import java.util.stream.Collectors;

public class OoRexxDocGenerationContextFactory {
    private OoRexxDocGenerationContextFactory() {
    }

    public static OoRexxDocGenerationContext of(OoRexxDocXml ooRexxDocXml){
        OoRexxDocGenerationContext ooRexxDocGenerationContext = new OoRexxDocGenerationContext();

        ooRexxDocGenerationContext.setInputs(ooRexxDocXml.getParser().getInput());
        ooRexxDocGenerationContext.setOutput(ooRexxDocXml.getParser().getOutput());
        ooRexxDocGenerationContext.setTitle(ooRexxDocXml.getTitle());
        ooRexxDocGenerationContext.setOverview(ooRexxDocXml.getOverview());
        ooRexxDocGenerationContext.setHeaders(ooRexxDocXml.getHeader().getHeaderProperties().stream().collect(Collectors.toMap(HeaderProperty::getKey, HeaderProperty::getValue)));
        ooRexxDocGenerationContext.getSettings().setIncludePrivate(ooRexxDocXml.getSettings().isIncludePrivate());
        ooRexxDocGenerationContext.getSettings().setIncludePublic(ooRexxDocXml.getSettings().isIncludePublic());
        ooRexxDocGenerationContext.getSettings().setIncludePackage(ooRexxDocXml.getSettings().isIncludePackage());
        ooRexxDocGenerationContext.getSettings().setIncludeVariableReferences(ooRexxDocXml.getSettings().isIncludeVariableReferences());

        return ooRexxDocGenerationContext;
    }

    public static OoRexxDocGenerationContext of(IntelliJGenerationCommand command){
        OoRexxDocGenerationContext ooRexxDocGenerationContext = new OoRexxDocGenerationContext();

        ooRexxDocGenerationContext.setInputs(command.getInputs().stream().map(VirtualFile::getCanonicalPath).collect(Collectors.toList()));
        ooRexxDocGenerationContext.setOutput(command.getOutput().getCanonicalPath());
        ooRexxDocGenerationContext.setTitle(command.getTitle());
        ooRexxDocGenerationContext.setHeaders(command.getHeaders());
        ooRexxDocGenerationContext.setOverview(command.getOverview());
        ooRexxDocGenerationContext.getSettings().setIncludePrivate(command.isIncludePrivate());
        ooRexxDocGenerationContext.getSettings().setIncludePublic(command.isIncludePublic());
        ooRexxDocGenerationContext.getSettings().setIncludePackage(command.isIncludePackage());
        ooRexxDocGenerationContext.getSettings().setIncludeVariableReferences(command.isIncludeVariableReferences());

        return ooRexxDocGenerationContext;
    }
}

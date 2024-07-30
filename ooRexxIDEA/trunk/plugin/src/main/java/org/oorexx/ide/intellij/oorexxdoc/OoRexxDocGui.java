package org.oorexx.ide.intellij.oorexxdoc;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import org.oorexx.doc.generate.v2.IntegratedGenerator;
import org.oorexx.doc.generate.v2.TemplateBundle;
import org.oorexx.doc.generate.v2.TemplateRegistry;
import org.oorexx.doc.input.intellij.IntelliJGenerationCommand;
import org.oorexx.doc.input.model.OoRexxDocGenerationContext;
import org.oorexx.doc.input.model.OoRexxDocGenerationContextFactory;
import org.oorexx.ide.intellij.action.configuration.OoRexxDocQuickActionPersistentStateComponent;

public class OoRexxDocGui {

    public static void openUi(Project project, List<VirtualFile> inputs) {
        IntelliJGenerationCommand intelliJGenerationCommand = new IntelliJGenerationCommand();
        intelliJGenerationCommand.setInputs(inputs);
        openUi(project, intelliJGenerationCommand);
    }

    public static void openUi(Project project, IntelliJGenerationCommand intelliJGenerationCommand) {
        OoRexxDocForm ooRexxDocForm = new OoRexxDocForm(project, intelliJGenerationCommand);
        DialogBuilder builder = new DialogBuilder(project);
        builder.setCenterPanel(ooRexxDocForm.getRoot());
        builder.setDimensionServiceKey("GrepConsoleTailFileDialog");
        builder.setTitle("ooRexxDoc Settings");
        builder.removeAllActions();
        builder.addOkAction();
        builder.setOkOperation(() -> {
            if (ooRexxDocForm.isValid()) {
                IntelliJGenerationCommand command = ooRexxDocForm.createCommand();
                builder.getDialogWrapper().close(DialogWrapper.OK_EXIT_CODE);
                ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
                    ApplicationManager.getApplication().runReadAction(() -> {
                        try {
                            OoRexxDocGenerationContext ooRexxDocGenerationContext = OoRexxDocGenerationContextFactory.of(command);
                            TemplateBundle templateBundle = TemplateRegistry.Companion.getTemplateByName(ooRexxDocGenerationContext.getTemplateName());

                            IntegratedGenerator integratedGenerator = new IntegratedGenerator();
                            integratedGenerator.generate(ooRexxDocGenerationContext, templateBundle);

                            if (ooRexxDocForm.isQuickSave()) {
                                quickSave(project, ooRexxDocGenerationContext);
                            }
                            if (ooRexxDocForm.isAutoOpen()) {
                                openBrowser(ooRexxDocGenerationContext);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                }, "Generating ooRexxDocs", false, project);
            }
        });
        builder.addCancelAction();
        builder.show();
    }

    private static void openBrowser(OoRexxDocGenerationContext ooRexxDocGenerationContext) {
        try {
            Desktop.getDesktop().browse(new File(ooRexxDocGenerationContext.getOutput()).toURI());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void quickSave(Project project, OoRexxDocGenerationContext ooRexxDocGenerationContext) throws IOException {
        String xml = generateXML4IntelliJ(ooRexxDocGenerationContext);
        OoRexxDocQuickActionPersistentStateComponent service = project.getService(OoRexxDocQuickActionPersistentStateComponent.class);
        service.setXml(xml);
    }

    public void runWriteAction(final Runnable runnable) {
        Application application = ApplicationManager.getApplication();
        if (application.isDispatchThread()) {

            application.runWriteAction(runnable);

        } else {
            application.invokeLater(() -> application.runWriteAction(runnable));
        }
    }

    private static String generateXML4IntelliJ(OoRexxDocGenerationContext ooRexxDocGenerationContext) {
        XmlMapper xmlMapper = new XmlMapper();
        try {
            xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
            return xmlMapper.writeValueAsString(ooRexxDocGenerationContext.toXml());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}

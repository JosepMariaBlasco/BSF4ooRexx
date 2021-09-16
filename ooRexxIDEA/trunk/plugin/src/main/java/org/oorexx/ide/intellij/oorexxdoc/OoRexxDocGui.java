package org.oorexx.ide.intellij.oorexxdoc;

import java.awt.*;
import java.io.IOException;
import java.util.List;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import org.oorexx.doc.generate.DocGenerator;
import org.oorexx.doc.generate.DocGeneratorBase;
import org.oorexx.doc.input.intellij.IntelliJGenerationCommand;
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
                            DocGeneratorBase docGenerator = new DocGenerator(command);
                            docGenerator.generate();
                            if (ooRexxDocForm.isQuickSave()) {
                                quickSave(project, docGenerator);
                            }
                            if (ooRexxDocForm.isAutoOpen()) {
                                openBrowser(docGenerator);
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

    private static void openBrowser(DocGeneratorBase docGenerator) {
        try {
            Desktop.getDesktop().browse(docGenerator.getOutputFile().toURI());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void quickSave(Project project, DocGeneratorBase docGenerator) throws IOException {
        String xml = docGenerator.generateXML4IntelliJ();
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

}

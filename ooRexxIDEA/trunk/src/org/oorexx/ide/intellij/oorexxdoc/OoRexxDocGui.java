package org.oorexx.ide.intellij.oorexxdoc;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.FileUtils;
import org.oorexx.doc.generate.DocGenerator;
import org.oorexx.doc.input.intellij.IntelliJGenerationCommand;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class OoRexxDocGui {

    public static void openUi(Project project, List<VirtualFile> inputs){
        OoRexxDocForm ooRexxDocForm = new OoRexxDocForm(project, inputs);
        DialogBuilder builder = new DialogBuilder(project);
        builder.setCenterPanel(ooRexxDocForm.getRoot());
        builder.setDimensionServiceKey("GrepConsoleTailFileDialog");
        builder.setTitle("ooRexxDoc Settings");
        builder.removeAllActions();
        builder.addOkAction();
        builder.setOkOperation(() -> {
            IntelliJGenerationCommand command = ooRexxDocForm.createCommand();
            if(ooRexxDocForm.isValid()) {
                builder.getDialogWrapper().close(DialogWrapper.OK_EXIT_CODE);
                ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
                    ApplicationManager.getApplication().runReadAction(() -> {
                        try {
                            DocGenerator docGenerator = new DocGenerator(command);
                            docGenerator.generate();
                            if(ooRexxDocForm.isQuickSave()) {
                                quickSave(project, docGenerator);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            JOptionPane.showMessageDialog(null, e.getMessage());
                        }
                    });
                }, "Generating ooRexxDocs", false, project);
            }
        });
        builder.addCancelAction();
        builder.show();
    }

    private static void quickSave(Project project, DocGenerator docGenerator) throws IOException {
        String xml = docGenerator.generateXML4IntelliJ();
        String canonicalPath = project.getProjectFile().getParent().getCanonicalPath();
        File file = new File(canonicalPath, "ooRexxDoc.xml");
        FileUtils.writeStringToFile(file, xml, "utf-8");
    }

}

package org.oorexx.ide.intellij.oorexxdoc;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import org.oorexx.doc.generate.DocGenerator;
import org.oorexx.doc.input.intellij.IntelliJGenerationCommand;

import javax.swing.*;
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
                try {
                    new DocGenerator(command).generate();
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, e.getMessage());
                }
            }
        });
        builder.addCancelAction();

        builder.show();
    }

}

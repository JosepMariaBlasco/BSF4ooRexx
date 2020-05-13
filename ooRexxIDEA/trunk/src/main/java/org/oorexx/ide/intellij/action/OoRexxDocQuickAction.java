package org.oorexx.ide.intellij.action;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.oorexx.doc.generate.DocGenerator;
import org.oorexx.doc.input.intellij.IntelliJGenerationCommand;
import org.oorexx.doc.input.model.xml.OoRexxDocXml;
import org.oorexx.ide.intellij.oorexxdoc.OoRexxDocGui;

import java.io.File;

public class OoRexxDocQuickAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setVisible(getFile(e).exists());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            File file = getFile(e);
            if(file.exists()) {
                String xml = FileUtils.readFileToString(file, "utf-8");
                XmlMapper xmlMapper = new XmlMapper();
                OoRexxDocXml ooRexxDocXml = xmlMapper.readValue(xml, OoRexxDocXml.class);
                IntelliJGenerationCommand intelliJGenerationCommand = IntelliJGenerationCommand.of(ooRexxDocXml);
                OoRexxDocGui.openUi(e.getProject(), intelliJGenerationCommand);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @NotNull
    private File getFile(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile parent = project.getWorkspaceFile().getParent();
        return new File(parent.getCanonicalPath(), "ooRexxDoc.xml");
    }


}

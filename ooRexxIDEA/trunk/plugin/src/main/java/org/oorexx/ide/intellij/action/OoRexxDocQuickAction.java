package org.oorexx.ide.intellij.action;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.oorexx.doc.input.intellij.IntelliJGenerationCommand;
import org.oorexx.doc.input.model.xml.OoRexxDocXml;
import org.oorexx.ide.intellij.action.configuration.OoRexxDocQuickActionPersistentStateComponent;
import org.oorexx.ide.intellij.oorexxdoc.OoRexxDocGui;

public class OoRexxDocQuickAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setVisible(getXml(e.getProject()) != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            final IntelliJGenerationCommand[] intelliJGenerationCommand = new IntelliJGenerationCommand[1];
            ApplicationManager.getApplication().runWriteAction(() -> {
                String xml = getXml(e.getProject());
                FileDocumentManager.getInstance().saveAllDocuments();
                if (xml != null) {
                    XmlMapper xmlMapper = new XmlMapper();
                    OoRexxDocXml ooRexxDocXml = null;
                    try {
                        ooRexxDocXml = xmlMapper.readValue(xml, OoRexxDocXml.class);
                        intelliJGenerationCommand[0] = IntelliJGenerationCommand.of(ooRexxDocXml);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });

            OoRexxDocGui.openUi(e.getProject(), intelliJGenerationCommand[0]);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private String getXml(Project project) {
        OoRexxDocQuickActionPersistentStateComponent service = project.getService(OoRexxDocQuickActionPersistentStateComponent.class);
        return service.getXml();
    }


}

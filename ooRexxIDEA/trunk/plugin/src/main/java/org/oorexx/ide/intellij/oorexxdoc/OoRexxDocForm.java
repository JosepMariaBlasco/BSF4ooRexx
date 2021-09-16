package org.oorexx.ide.intellij.oorexxdoc;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.PathChooserDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.AddEditRemovePanel;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.oorexx.doc.input.intellij.IntelliJGenerationCommand;
import org.oorexx.ide.file.OoRexxFileType;
import org.oorexx.ide.intellij.oorexxdoc.model.FileNotFoundException;
import org.oorexx.ide.intellij.util.OoRexxFileUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class OoRexxDocForm {
    private JPanel container;
    private JTextField title;
    private TextFieldWithBrowseButton output;
    private JPanel panelTable;
    private JPanel filePanel;
    private JLabel error;
    private JEditorPane overview;
    private JCheckBox quickConfiguration;
    private JCheckBox modifierPublic;
    private JCheckBox modifierPrivate;
    private JCheckBox modifierPackage;
    private JCheckBox variableReferences;
    private JCheckBox openDocumentationAfterCreation;
    private IntelliJGenerationCommand intelliJGenerationCommand = new IntelliJGenerationCommand();
    private DefaultListModel<VirtualFile> listModel = new DefaultListModel<>();
    private VirtualFile lastAdded = null;
    private List<KeyValuePair> keyValuePairs = new ArrayList<>();
    private AddEditRemovePanel<KeyValuePair> headerTable;
    private AddEditRemovePanel<VirtualFile> fileTable;

    public boolean isQuickSave() {
        return quickConfiguration.isSelected();
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    private class HeaderTableModel extends AddEditRemovePanel.TableModel<KeyValuePair> {
        private int i = 0; // columnIndex always 0 so counting calls to function
        @Override
        public int getColumnCount() {
            return 2;
        }

        @Nullable
        @Override
        public String getColumnName(int columnIndex) {
            return new String[]{"Key", "Value"}[columnIndex];
        }

        @Override
        public Object getField(KeyValuePair o, int columnIndex) {
            i++;
            return i%2==1 ? o.getKey() : o.getValue();
        }
    }

    private class FileTableModel extends AddEditRemovePanel.TableModel<VirtualFile> {

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Nullable
        @Override
        public String getColumnName(int columnIndex) {
            return new String[]{"File"}[columnIndex];
        }

        @Override
        public Object getField(VirtualFile o, int columnIndex) {
            return o;
        }
    }

    public OoRexxDocForm(Project project, IntelliJGenerationCommand intelliJGenerationCommand){
        this.intelliJGenerationCommand = intelliJGenerationCommand;
        DefaultTableColumnModel defaultTableColumnModel = new DefaultTableColumnModel();
        TableColumn key = new TableColumn();
        key.setHeaderValue("Key");
        defaultTableColumnModel.addColumn(key);
        TableColumn value = new TableColumn();
        value.setHeaderValue("Value");
        defaultTableColumnModel.addColumn(value);

        if(intelliJGenerationCommand.getHeaders().isEmpty()) {
            keyValuePairs.add(new KeyValuePair("Run-Date", "${.now?datetime}"));
        } else {
            intelliJGenerationCommand.getHeaders().forEach((s, s2) -> keyValuePairs.add(new KeyValuePair(s, s2)));
        }

        headerTable = new AddEditRemovePanel<KeyValuePair>(new HeaderTableModel(), keyValuePairs) {
            @Nullable
            @Override
            protected KeyValuePair addItem() {
                KeyValueForm keyValueForm = new KeyValueForm();
                DialogBuilder builder = new DialogBuilder(project);
                builder.setCenterPanel(keyValueForm.getRoot());
                builder.setTitle("ooRexxDoc Settings - Header Entry");
                builder.removeAllActions();
                builder.addOkAction();
                builder.addCancelAction();

                boolean isOk = builder.show() == DialogWrapper.OK_EXIT_CODE;
                if (isOk) {
                    return keyValueForm.getKeyValuePair();
                }
                return null;
            }

            @Override
            protected boolean removeItem(KeyValuePair o) {
                return true;
            }

            @Nullable
            @Override
            protected KeyValuePair editItem(KeyValuePair o) {
                KeyValueForm keyValueForm = new KeyValueForm(o);
                DialogBuilder builder = new DialogBuilder(project);
                builder.setCenterPanel(keyValueForm.getRoot());
                builder.setTitle("ooRexxDoc Settings - Header Entry");
                builder.removeAllActions();
                builder.addOkAction();
                builder.addCancelAction();

                boolean isOk = builder.show() == DialogWrapper.OK_EXIT_CODE;
                if (isOk) {
                    return keyValueForm.getKeyValuePair();
                }
                return o;
            }
        };
        panelTable.add(headerTable, BorderLayout.CENTER);
        headerTable.getTable().setColumnModel(defaultTableColumnModel);
        headerTable.getTable().setShowColumns(true);

        fileTable = new AddEditRemovePanel<VirtualFile>(new FileTableModel(), OoRexxDocForm.this.intelliJGenerationCommand.getInputs()) {
            @Nullable
            @Override
            protected VirtualFile addItem() {
                FileChooserDialog fileChooser = FileChooserFactory.getInstance().createFileChooser(FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor(OoRexxFileType.INSTANCE), project, null);
                VirtualFile[] choose = fileChooser.choose(project, getVirtualFileStartingPoint(project));
                return choose.length > 0 ? choose[0] : null;
            }

            @Override
            protected boolean removeItem(VirtualFile o) {
                return true;
            }

            @Nullable
            @Override
            protected VirtualFile editItem(VirtualFile o) {
                FileChooserDialog fileChooser = FileChooserFactory.getInstance().createFileChooser(FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor(OoRexxFileType.INSTANCE), project, null);
                VirtualFile[] choose = fileChooser.choose(project, o);
                return choose.length > 0 ? choose[0] : null;
            }
        };

        filePanel.add(fileTable, BorderLayout.CENTER);
        fileTable.getTable().setShowColumns(true);

        if(this.intelliJGenerationCommand.getOutput() != null){
            output.setText(this.intelliJGenerationCommand.getOutput().toString());
        }

        output.getButton().addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PathChooserDialog pathChooser = FileChooserFactory.getInstance().createPathChooser(FileChooserDescriptorFactory.createSingleFolderDescriptor(), project, null);
                pathChooser.choose(null, virtualFiles -> {
                    if(!virtualFiles.isEmpty()) {
                        OoRexxDocForm.this.intelliJGenerationCommand.setOutput(virtualFiles.get(0));
                        output.setText(OoRexxDocForm.this.intelliJGenerationCommand.getOutput().toString());
                    }
                });
            }
        });

        if(intelliJGenerationCommand.getOverview() != null){
            overview.setText(intelliJGenerationCommand.getOverview());
        }

        if(intelliJGenerationCommand.getTitle() != null){
            title.setText(intelliJGenerationCommand.getTitle());
        }

        modifierPublic.setSelected(intelliJGenerationCommand.isIncludePublic());
        modifierPackage.setSelected(intelliJGenerationCommand.isIncludePackage());
        modifierPrivate.setSelected(intelliJGenerationCommand.isIncludePrivate());
        variableReferences.setSelected(intelliJGenerationCommand.isIncludeVariableReferences());

    }

    private VirtualFile getVirtualFileStartingPoint(Project project) {
        if(lastAdded != null && !lastAdded.isDirectory()){
            return lastAdded.getParent();
        }
        return lastAdded == null ? project.getProjectFile().getParent() : lastAdded;
    }

    public IntelliJGenerationCommand createCommand() {
        intelliJGenerationCommand.setOutput(getVirtualFile(output.getText()));
        intelliJGenerationCommand.setInputs(OoRexxFileUtil.getFiles(fileTable.getData()));
        intelliJGenerationCommand.setTitle(title.getText());
        intelliJGenerationCommand.setHeaders(headerTable.getData().stream().filter(Objects::nonNull).collect(Collectors.toMap(KeyValuePair::getKey, KeyValuePair::getValue)));
        intelliJGenerationCommand.setOverview(overview.getText());

        intelliJGenerationCommand.setIncludePrivate(modifierPrivate.isSelected());
        intelliJGenerationCommand.setIncludePublic(modifierPublic.isSelected());
        intelliJGenerationCommand.setIncludePackage(modifierPackage.isSelected());
        intelliJGenerationCommand.setIncludeVariableReferences(variableReferences.isSelected());

        return intelliJGenerationCommand;
    }

    private VirtualFile getVirtualFile(String path) {
        if(path.toLowerCase().startsWith("file://")){
            path = path.replace("file://", "");
        }
        try {
            return VfsUtil.createDirectoryIfMissing(path);
        } catch (IOException e) {
            throw new FileNotFoundException("could not find file " + path);
        }
    }

    public String validateMessage(){
        List<String> messages = new ArrayList<>();

        if(output.getText().trim().equals("")){
            messages.add("Output required");
        }

        if(fileTable.getData().isEmpty()){
            messages.add("Input(s) required");
        }
        try {
            VirtualFile virtualFile = getVirtualFile(output.getText());
            if(!virtualFile.isDirectory()){
                messages.add("Output is not a directory");
            }
        } catch (FileNotFoundException e){
            messages.add("Output directory not found");
        }
        return StringUtils.join(messages, ", ");
    }

    public boolean isValid(){
        String message = validateMessage();
        if(message.length() == 0){
            return true;
        }

        error.setText(message);

        return false;
    }

    public JComponent getRoot(){
        return container;
    }

    public boolean isAutoOpen() {
        return openDocumentationAfterCreation.isSelected();
    }
}

package org.oorexx.ide.intellij.oorexxdoc;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.PathChooserDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.AddEditRemovePanel;
import org.jetbrains.annotations.Nullable;
import org.oorexx.doc.input.intellij.IntelliJGenerationCommand;
import org.oorexx.ide.file.OoRexxFileType;
import org.oorexx.ide.intellij.util.OoRexxFileUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
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
    private IntelliJGenerationCommand intelliJGenerationCommand = new IntelliJGenerationCommand();
    private DefaultListModel<VirtualFile> listModel = new DefaultListModel<>();
    private VirtualFile lastAdded = null;
    private List<KeyValuePair> keyValuePairs = new ArrayList<>();
    private AddEditRemovePanel<KeyValuePair> headerTable;
    private AddEditRemovePanel<VirtualFile> fileTable;

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

    public OoRexxDocForm(Project project, List<VirtualFile> inputs){
        DefaultTableColumnModel defaultTableColumnModel = new DefaultTableColumnModel();
        TableColumn key = new TableColumn();
        key.setHeaderValue("Key");
        defaultTableColumnModel.addColumn(key);
        TableColumn value = new TableColumn();
        value.setHeaderValue("Value");
        defaultTableColumnModel.addColumn(value);

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

        fileTable = new AddEditRemovePanel<VirtualFile>(new FileTableModel(), inputs) {
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
        output.getButton().addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PathChooserDialog pathChooser = FileChooserFactory.getInstance().createPathChooser(FileChooserDescriptorFactory.createSingleFolderDescriptor(), project, null);
                pathChooser.choose(null, virtualFiles -> {
                    if(!virtualFiles.isEmpty()) {
                        intelliJGenerationCommand.setOutput(virtualFiles.get(0));
                        output.setText(intelliJGenerationCommand.getOutput().toString());
                    }
                });
            }
        });

    }

    private VirtualFile getVirtualFileStartingPoint(Project project) {
        if(lastAdded != null && !lastAdded.isDirectory()){
            return lastAdded.getParent();
        }
        return lastAdded == null ? project.getProjectFile().getParent() : lastAdded;
    }

    public IntelliJGenerationCommand createCommand() {
        intelliJGenerationCommand.setInputs(OoRexxFileUtil.getFiles(fileTable.getData()));
        intelliJGenerationCommand.setTitle(title.getText());
        intelliJGenerationCommand.setHeaders(headerTable.getData().stream().filter(Objects::nonNull).collect(Collectors.toMap(KeyValuePair::getKey, KeyValuePair::getValue)));
        intelliJGenerationCommand.setOverview(overview.getText());

        return intelliJGenerationCommand;
    }

    public StringBuilder validateMessage(){
        StringBuilder stringBuilder = new StringBuilder();
        if(intelliJGenerationCommand.getOutput() == null){
            stringBuilder.append("Output required").append(" ");
        }

        if(intelliJGenerationCommand.getInputs().isEmpty()){
            stringBuilder.append("Input(s) required").append(" ");
        }
        return stringBuilder;
    }

    public boolean isValid(){
        StringBuilder stringBuilder = validateMessage();
        if(stringBuilder.length() == 0){
            return true;
        }

        error.setText(stringBuilder.toString());

        return false;
    }

    public JComponent getRoot(){
        return container;
    }
}

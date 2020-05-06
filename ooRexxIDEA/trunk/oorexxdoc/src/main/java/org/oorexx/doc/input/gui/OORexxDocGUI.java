package org.oorexx.doc.input.gui;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.oorexx.doc.generate.DocGenerator;
import org.oorexx.doc.input.model.xml.HeaderProperty;
import org.oorexx.doc.input.model.xml.OoRexxDocXml;
import org.oorexx.doc.input.xml.legacy.XmlParser;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class OORexxDocGUI extends JFrame {

	private static final long serialVersionUID = -7624591570094927788L;
	private JTextField titleTextField, output;
	private JButton browseOutput, inputAdd, inputRemove, addProperty,
			removeProperty, createDoc, saveConfig, loadConfig;
	private JList inputList, propertyList;
	private JTextArea overviewText;
	private DefaultListModel inputListModel, propertyListModel;
	private JLabel statusLabel;
	private JLabel lblNewLabel;
	private JLabel lbltextDisplayedOn;
	private JLabel lbldirectoryWillBe;
	private JLabel statusLabel2;

	public OORexxDocGUI() {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		setTitle("ooRexxDoc");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JLabel lblTitle = new JLabel("Title");

		JLabel lblInput = new JLabel("Input (required)");

		titleTextField = new JTextField();
		titleTextField.setColumns(10);

		inputList = new JList();
		inputListModel = new DefaultListModel();
		inputList.setModel(inputListModel);
		JScrollPane inputScrollPane = new JScrollPane(inputList);

		JLabel lblOutput = new JLabel("Output (required)");

		output = new JTextField();
		output.setColumns(10);

		JLabel label_4 = new JLabel("Overview Text");

		overviewText = new JTextArea();
		JScrollPane overviewTextScrollPane = new JScrollPane(overviewText);

		JLabel lblInfoproperties = new JLabel("Info-Properties");

		propertyList = new JList();
		propertyListModel = new DefaultListModel();
		propertyList.setModel(propertyListModel);
		JScrollPane propertyListScrollPane = new JScrollPane(propertyList);

		ActionHandler actionHandler = new ActionHandler();

		createDoc = new JButton("Create Doc");
		createDoc.addActionListener(actionHandler);
		inputAdd = new JButton("Add");
		inputAdd.addActionListener(actionHandler);
		inputRemove = new JButton("Remove");
		inputRemove.addActionListener(actionHandler);
		browseOutput = new JButton("Browse");
		browseOutput.addActionListener(actionHandler);
		addProperty = new JButton("Add");
		addProperty.addActionListener(actionHandler);
		removeProperty = new JButton("Remove");
		removeProperty.addActionListener(actionHandler);
		saveConfig = new JButton("Save Config");
		saveConfig.addActionListener(actionHandler);
		loadConfig = new JButton("Load Config");
		loadConfig.addActionListener(actionHandler);

		statusLabel = new JLabel("");

		lblNewLabel = new JLabel("(E.g. Author: Rexxler)");

		lbltextDisplayedOn = new JLabel(
				"<html>(Text will be displayed on the <br /> start page)</html>");

		JSeparator separator = new JSeparator();

		lbldirectoryWillBe = new JLabel("(Directory will be created if it does not exist. Content will be appended to an existing directory)");

		statusLabel2 = new JLabel("ooRexxDoc is ready!");

		propertyList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				JList list = (JList)evt.getSource();
				if (evt.getClickCount() == 2) {
					int index = list.locationToIndex(evt.getPoint());

					ListProperty listProperty = (ListProperty) propertyListModel.getElementAt(index);

					String key = JOptionPane.showInputDialog(null, "Property key", listProperty.getKey());
					String val = JOptionPane
							.showInputDialog(null, "Property value", listProperty.getValue());
					propertyListModel.remove(index);
					propertyListModel.add(index, new ListProperty((key == null ? listProperty.getKey() : key)
							, (val == null ? listProperty.getValue() : val)));
				}
			}
		});

		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
								.addContainerGap()
								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
										.addGroup(groupLayout.createSequentialGroup()
												.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
														.addGroup(groupLayout.createSequentialGroup()
																.addComponent(lblTitle, GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
																.addGap(18)
																.addComponent(titleTextField, GroupLayout.PREFERRED_SIZE, 282, GroupLayout.PREFERRED_SIZE))
														.addGroup(groupLayout.createSequentialGroup()
																.addComponent(lblInput, GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
																.addGap(18)
																.addComponent(inputScrollPane, GroupLayout.PREFERRED_SIZE, 280, GroupLayout.PREFERRED_SIZE))
														.addGroup(groupLayout.createSequentialGroup()
																.addComponent(lblOutput, GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
																.addGap(18)
																.addComponent(output, GroupLayout.PREFERRED_SIZE, 282, GroupLayout.PREFERRED_SIZE))
														.addGroup(groupLayout.createSequentialGroup()
																.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
																		.addComponent(lblInfoproperties, GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
																		.addComponent(label_4, GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
																		.addComponent(lblNewLabel, GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
																		.addComponent(lbltextDisplayedOn, GroupLayout.PREFERRED_SIZE, 148, GroupLayout.PREFERRED_SIZE))
																.addPreferredGap(ComponentPlacement.RELATED)
																.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
																		.addComponent(overviewTextScrollPane, GroupLayout.PREFERRED_SIZE, 280, GroupLayout.PREFERRED_SIZE)
																		.addGroup(groupLayout.createSequentialGroup()
																				.addComponent(createDoc)
																				.addPreferredGap(ComponentPlacement.RELATED)
																				.addComponent(saveConfig)
																				.addPreferredGap(ComponentPlacement.RELATED)
																				.addComponent(loadConfig)
																				.addPreferredGap(ComponentPlacement.RELATED)
																				.addComponent(statusLabel))
																		.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
																				.addComponent(statusLabel2, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																				.addComponent(propertyListScrollPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)))
																.addPreferredGap(ComponentPlacement.RELATED)))
												.addGap(34)
												.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
														.addComponent(addProperty, GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
														.addComponent(browseOutput, GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
														.addComponent(inputRemove, GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
														.addComponent(inputAdd, GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
														.addComponent(removeProperty, GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)))
										.addGroup(groupLayout.createSequentialGroup()
												.addComponent(lbldirectoryWillBe)
												.addGap(93)
												.addComponent(separator, GroupLayout.PREFERRED_SIZE, 1, GroupLayout.PREFERRED_SIZE)))
								.addContainerGap())
		);
		groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
								.addContainerGap()
								.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
										.addComponent(titleTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(lblTitle))
								.addGap(18)
								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
										.addGroup(groupLayout.createSequentialGroup()
												.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
														.addComponent(inputScrollPane, GroupLayout.PREFERRED_SIZE, 54, GroupLayout.PREFERRED_SIZE)
														.addComponent(lblInput))
												.addGap(18)
												.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
														.addComponent(output, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
														.addComponent(lblOutput))
												.addPreferredGap(ComponentPlacement.RELATED)
												.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
														.addComponent(separator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
														.addComponent(lbldirectoryWillBe))
												.addGap(30)
												.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
														.addGroup(groupLayout.createSequentialGroup()
																.addComponent(label_4)
																.addPreferredGap(ComponentPlacement.RELATED)
																.addComponent(lbltextDisplayedOn, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE))
														.addComponent(overviewTextScrollPane, GroupLayout.PREFERRED_SIZE, 98, GroupLayout.PREFERRED_SIZE)))
										.addGroup(groupLayout.createSequentialGroup()
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(inputAdd)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(inputRemove)
												.addGap(18)
												.addComponent(browseOutput)))
								.addGap(18)
								.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
										.addGroup(groupLayout.createSequentialGroup()
												.addComponent(addProperty)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(removeProperty)
												.addPreferredGap(ComponentPlacement.RELATED))
										.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
												.addComponent(propertyListScrollPane, GroupLayout.PREFERRED_SIZE, 54, GroupLayout.PREFERRED_SIZE)
												.addGroup(groupLayout.createSequentialGroup()
														.addComponent(lblInfoproperties)
														.addGap(8)
														.addComponent(lblNewLabel))))
								.addGap(2)
								.addComponent(statusLabel2)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
										.addGroup(groupLayout.createSequentialGroup()
												.addComponent(statusLabel, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
												.addGap(15))
										.addGroup(groupLayout.createSequentialGroup()
												.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
														.addComponent(createDoc)
														.addComponent(saveConfig)
														.addComponent(loadConfig))
												.addContainerGap())))
		);
		getContentPane().setLayout(groupLayout);

		pack();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height
				/ 2 - this.getSize().height / 2);

		setVisible(true);
	}

	public void setStatusText(String text) {
		statusLabel2.setText(text);
	}

	private class ActionHandler implements ActionListener {
		private JFileChooser inputFC = new JFileChooser();
		private JFileChooser outputFC = new JFileChooser();
		private JFileChooser loadFC = new JFileChooser();
		private JFileChooser saveFC = new JFileChooser(){

			private static final long serialVersionUID = 1L;

			@Override
			public void approveSelection(){
				File f = getSelectedFile();
				if(f.exists() && getDialogType() == SAVE_DIALOG){
					int result = JOptionPane.showConfirmDialog(this,"The file exists! Do you want to overwrite?","Existing file",JOptionPane.YES_NO_CANCEL_OPTION);
					switch(result){
						case JOptionPane.YES_OPTION:
							super.approveSelection();
							return;
						case JOptionPane.NO_OPTION:
							return;
						case JOptionPane.CLOSED_OPTION:
							return;
						case JOptionPane.CANCEL_OPTION:
							cancelSelection();
							return;
					}
				}
				super.approveSelection();
			}
		};


		public ActionHandler() {
			saveFC.removeChoosableFileFilter(inputFC.getAcceptAllFileFilter());
			loadFC.removeChoosableFileFilter(inputFC.getAcceptAllFileFilter());

			saveFC.addChoosableFileFilter(new FileTypeFilter("xml", ""));
			loadFC.addChoosableFileFilter(new FileTypeFilter("xml", ""));

			inputFC.removeChoosableFileFilter(inputFC.getAcceptAllFileFilter());
			inputFC.addChoosableFileFilter(new FileTypeFilter(Arrays.asList(new String[]{"cls", "frm", "jrexx", "orx", "rex", "rexx", "rexxj", "rxj", "rxo", "testGroup", "testUnit"}), ""));
			inputFC.setMultiSelectionEnabled(true);
			inputFC.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		}

		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == inputAdd) {
				int returnVal = inputFC.showOpenDialog(OORexxDocGUI.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					if (inputFC.getSelectedFiles() != null)
						for (File file : inputFC.getSelectedFiles()) {
							inputListModel.addElement(file.getAbsolutePath());
						}
					else
						inputListModel.addElement(inputFC.getSelectedFile()
								.getAbsolutePath());
				}
			}
			if (e.getSource() == inputRemove) {
				if (inputList.getSelectedIndex() >= 0)
					inputListModel.remove(inputList.getSelectedIndex());
			}
			if (e.getSource() == browseOutput) {
				outputFC.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = outputFC.showSaveDialog(OORexxDocGUI.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					output.setText(outputFC.getSelectedFile().getAbsolutePath());
				}
			}

			if (e.getSource() == addProperty) {
				String key = JOptionPane.showInputDialog(null, "Property key");
				String val = JOptionPane
						.showInputDialog(null, "Property value");
				propertyListModel.addElement(new ListProperty(key, val));
			}
			if (e.getSource() == removeProperty) {
				if (propertyList.getSelectedIndex() >= 0)
					propertyListModel.remove(propertyList.getSelectedIndex());
			}
			if (e.getSource() == createDoc) {
				createDoc.setEnabled(false);
				if (inputListModel.getSize() == 0) {
					statusLabel.setText("Add an input to continue!");
					createDoc.setEnabled(true);
					return;
				}
				if (output.getText().trim().equals("")) {
					statusLabel.setText("Add an output to continue!");
					createDoc.setEnabled(true);
					return;
				}
				if (new File(output.getText()).isFile()) {
					statusLabel.setText("The output must be a directory!");
					createDoc.setEnabled(true);
					return;
				}

				new Thread(new Runnable() {

					public void run() {
						try {
							createDoc.setEnabled(false);
							OoRexxDocXml ooRexxDocXml = XmlParser.get(generateXML());
							DocGenerator docGenerator = new DocGenerator(ooRexxDocXml);
							docGenerator.generate();
							createDoc.setEnabled(true);
						} catch (Exception e1) {
							e1.printStackTrace();
							printError(ExceptionUtils.getStackTrace(e1));
							createDoc.setEnabled(true);
						}
					}
				}).start();




			}
			if(e.getSource() == loadConfig){
				int returnVal = loadFC.showOpenDialog(OORexxDocGUI.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					try {
						String xml = FileUtils.readFileToString(loadFC.getSelectedFile());

						titleTextField.setText("");
						inputListModel.removeAllElements();
						output.setText("");
						overviewText.setText("");
						propertyListModel.removeAllElements();

						OoRexxDocXml ooRexxDocXml = XmlParser.get(xml);

						titleTextField.setText(ooRexxDocXml.getTitle());
						for(String srcFile: ooRexxDocXml.getParser().getInput()){
							inputListModel.addElement(new File(srcFile).getAbsolutePath());
						}
						output.setText(new File(ooRexxDocXml.getParser().getOutput()).getAbsolutePath());
						overviewText.setText(ooRexxDocXml.getOverview());

						for (HeaderProperty headerProperty : ooRexxDocXml.getHeader().getHeaderProperties()) {
							propertyListModel.addElement(new ListProperty(headerProperty.getKey(), headerProperty.getValue()));
						}

						setStatusText("Configuration Loaded!");
					} catch (Exception e1) {
						printError(ExceptionUtils.getStackTrace(e1));
					}
				}
			}
			if(e.getSource() == saveConfig){
				String xml = generateXML();
				int returnVal = saveFC.showSaveDialog(OORexxDocGUI.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					try {

						File xmlFile = saveFC.getSelectedFile();

						if(!xmlFile.getName().toLowerCase().endsWith(".xml"))
							xmlFile = new File(xmlFile.getAbsolutePath().concat(".xml"));

						FileUtils.writeStringToFile(xmlFile, xml);

						setStatusText("Configuration Saved!");
					} catch (IOException e1) {
						printError(ExceptionUtils.getStackTrace(e1));
					}


				}
			}
		}

		private String generateXML(){
			String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<oorexxdoc>\n<parser>\n";
			for (int i = 0; i < inputListModel.getSize(); i++)
				xml += "<input>" + inputListModel.get(i) + "</input>\n";
			xml += "<output>" + output.getText() + "</output>\n</parser>\n";
			xml += "<title>" + titleTextField.getText() + "</title>\n";
			xml += "<overview><![CDATA[" + overviewText.getText()
					+ "]]></overview>\n";
			xml += "<header>\n";
			for (int i = 0; i < propertyListModel.getSize(); i++) {
				ListProperty listProperty = (ListProperty) propertyListModel
						.get(i);
				xml += "<property><key>" + listProperty.getKey()
						+ "</key><value>" + listProperty.getValue()
						+ "</value></property>\n";
			}
			xml += "</header>\n";
			xml += "</oorexxdoc>";

			return xml;
		}

	}

	private void printError(String text){
		JOptionPane.showMessageDialog(null, text, "Error", JOptionPane.ERROR_MESSAGE);
	}

	private class ListProperty {
		private String key, value;

		public ListProperty(String key, String value) {
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		@SuppressWarnings("unused")
		public void setKey(String key) {
			this.key = key;
		}

		public String getValue() {
			return value;
		}

		@SuppressWarnings("unused")
		public void setValue(String value) {
			this.value = value;
		}

		public String toString() {
			return key + ": " + value;
		}
	}
}

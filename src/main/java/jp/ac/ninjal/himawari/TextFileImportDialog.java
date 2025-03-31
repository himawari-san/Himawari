/*
    Copyright (C) 2004-2025 Masaya YAMAGUCHI

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jp.ac.ninjal.himawari;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JRadioButton;
import javax.xml.xpath.XPathExpressionException;

import jp.ac.ninjal.himawari.HimawariLogger.HimawariListedLog;
import jp.ac.ninjal.himawari.HimawariLogger.HimawariResultLog;




public class TextFileImportDialog extends JDialog {
	public static final String TEXT_NORMALIZATION_TYPE_NONE = "none"; //$NON-NLS-1$
	public static final String TEXT_NORMALIZATION_TYPE_TABLE = "user_defined"; //$NON-NLS-1$
	public static final String TEXT_NORMALIZATION_TYPE_NFKC = "nfkc"; //$NON-NLS-1$

	public static final String FILE_TYPE_TXT = "TXT"; //$NON-NLS-1$
	public static final String FILE_TYPE_XHTML = "XHTML"; //$NON-NLS-1$
	public static final String FILE_TYPE_XML = "XML"; //$NON-NLS-1$
	
	public static final String NOT_USE_ANNOTATOR = Messages.getString("TextFileImportDialog.1"); //$NON-NLS-1$
	
	public static final String[] TXT_SUFFIXES = {"txt", "csv", "tsv"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	public static final String[] XHTML_SUFFIXES = {"xhtml", "html", "htm"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	public static final String[] XML_SUFFIXES = {"xml"}; //$NON-NLS-1$
	
	// configuration file for import
	public static final String CONFIG_FILE_IMPORT = ".himawari_import_config.xml"; //$NON-NLS-1$
	
	private static final long serialVersionUID = 1L;

	private Frame1 parent;

	private JPanel jContentPane1_importText = null;

	// TextFileImporter
	private TextFileImporter textFileImporter = null;
	
	//テキストファイルインポート対象フォルダ
	private JTextField tFieldTargetDir;

	//変換対象ファイル（テキスト）
	private JCheckBox checkTargetFile_text;

	//変換対象ファイル（XHTML）
	private JCheckBox checkTargetFile_xhtml;

	//変換対象ファイル（XML）
	private JCheckBox checkTargetFile_xml;

	//XHTMLファイルの変換：スタイルシート
	private JComboBox<String> comboTextImport_xhtml_xsl;

	//XMLファイルの変換：スタイルシート
	private JComboBox<String> comboTextImport_xml_xsl;

	//XHTMLファイルの変換：HTMLの変換を試みる
	private JCheckBox checkTextConvert_html;

	// 文字正規化オプション
	private JRadioButton radioButtonCharNormNone;
	private JRadioButton radioButtonCharNormUserDefined;
	private JRadioButton radioButtonCharNormNFKC;

	// コーパス構築オプション
	private JCheckBox checkBoxConstructIncludeSubcorpora;
	private JCheckBox checkBoxConstructNotNowIndexing;
	
	// テキストファイル変換規則
	private JComboBox<String> comboTextFileTransform;
//	JComboBox comboTextFileTransform;
	
	// config file
	private JComboBox<String> comboConfigFile;
	
	// morphological analyzers
	private JComboBox<String> comboMorphAnalyzers;
	private JTextField textFieldAnalyzedElement;
	private JTextField textFieldAnalyzedAttribute;
	private JTextField textFieldAnalyzedAttributeValue;
	
	//出力コーパス名
	private JTextField tFieldImportCorpusName;

	private Component jPanelConvOpt;

	private JPanel jContentPane1_importText_1;

	private JPanel jContentPane1_importText_2;

	private JPanel jContentPane1_importText_3;

	private JPanel jContentPane1_importText_4;

	private JPanel jContentPane1_importText_advancedOption;
	
	private JButton btnImportText;

	private JLabel jLabel_advancedOption;
	
	private JDialog thisDialog;

	private JPanel panel_rbCharNorm;
	private JPanel panel_rbTextType;
	private JPanel panelConstruction;

	// インポート設定
	private UserSettings importConfig = null;
	
	// テキスト置換定義ファイル用ディレクトリ
	private String dir_xsl_txt;
	// XHTML スタイルシート用ディレクトリ
	private String dir_xsl_xhtml;
	// XML スタイルシート用ディレクトリ
	private String dir_xsl_xml;

	private String txtStylesheetDefault;
	private String xhtmlStylesheetDefault;
	private String xmlStylesheetDefault;

	private String constructIncludeSubcorpora;
	private String constructNotNowIndeing;

	private String configuredSrcDir;

	
	/**
	 * This method initializes jDialog_importAnnotation
	 *
	 * @return javax.swing.JDialog
	 */
	public TextFileImportDialog(Frame1 parent, String defaultTargetDir) {
		super(parent);
		this.parent = parent;
		thisDialog = this;

		try{
			init();
			pack();
		}catch(Exception e){
			e.printStackTrace();
		}
		tFieldTargetDir.setText(defaultTargetDir);
		tFieldImportCorpusName.setText(new File(defaultTargetDir).getName());
		
	}
	

	private void init(){
		setContentPane(getJContentPane1_importText());
		setTitle(Messages.getString("TextFileImportDialog.0")); //$NON-NLS-1$
		setMinimumSize(new Dimension(650, (int)getPreferredSize().getHeight()));


		// close this dialog by pushing ESC key
		// based on http://ateraimemo.com/Swing/InputMap.html
		@SuppressWarnings("serial")
		AbstractAction act = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		};
		InputMap imap = getRootPane().getInputMap(
			JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
			imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close-it"); //$NON-NLS-1$
			getRootPane().getActionMap().put("close-it", act); //$NON-NLS-1$

	}

	/**
	 * This method initializes jContentPane1_importText
	 *
	 * @return javax.swing.JPanel
	 * @throws PropertyVetoException
	 */
	private JPanel getJContentPane1_importText() {
		try{
//			if (jContentPane1_importText == null) {
				jContentPane1_importText = new JPanel();
				jContentPane1_importText.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
				jContentPane1_importText.setLayout(new BoxLayout(jContentPane1_importText, BoxLayout.PAGE_AXIS));

				jContentPane1_importText_1 = getJpanel_importText_1();
				jContentPane1_importText_3 = getJpanel_importText_3();
				jContentPane1_importText_2 = getJpanel_importText_2();
				jContentPane1_importText_4 = getJpanel_importText_4();
				jContentPane1_importText_advancedOption = getJpanel_importText_advancedOption();
				
				
				//レイアウトパネルの配置
				jContentPane1_importText.add(jContentPane1_importText_1);
				jContentPane1_importText.add(jContentPane1_importText_3);
				jContentPane1_importText.add(jContentPane1_importText_advancedOption);
				jContentPane1_importText.add(jContentPane1_importText_2);
				jContentPane1_importText.add(jContentPane1_importText_4);
				jContentPane1_importText_2.setVisible(false);

//			}
		}catch(Exception e){
			e.printStackTrace();
		}

		return jContentPane1_importText;
	}

	/**
	 * 元データがあるフォルダ設定エリア
	 * @return
	 */
	private JPanel getJpanel_importText_1() {
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(Messages
				.getString("TextFileImportDialog.2"))); //$NON-NLS-1$

		tFieldTargetDir = new JTextField();

		JButton btnOpenFileChooser = new JButton(
				Messages.getString("TextFileImportDialog.7")); //$NON-NLS-1$
		btnOpenFileChooser.setMaximumSize(new Dimension(80, btnOpenFileChooser
				.getPreferredSize().height));
		btnOpenFileChooser
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JFileChooser fileChooser = new JFileChooser();
						fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

						if (fileChooser.showDialog(null, Messages.getString("AnnotationImportDialog.12")) == JFileChooser.APPROVE_OPTION) { //$NON-NLS-1$
							tFieldTargetDir.setText(fileChooser.getSelectedFile().getAbsolutePath());
						}

						String tmpname = new File(tFieldTargetDir.getText())
								.getName();
						String importConfigFilename = tFieldTargetDir.getText()
								+ "/" + CONFIG_FILE_IMPORT; //$NON-NLS-1$
						if (new File(importConfigFilename).exists()) {
							importConfig = new UserSettings();
							try {
								importConfig.init(importConfigFilename, false);
								setImportParameters(importConfig);
							} catch (Exception e1) {
								System.err
										.println("Warning(TextFileImportDialog): invalid import config file " //$NON-NLS-1$
												+ importConfigFilename + "\n"); //$NON-NLS-1$
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						} else {
							importConfig = null;
							tFieldImportCorpusName.setText(tmpname);
						}
					}
				});

		Component[][] compos = {
			{tFieldTargetDir , btnOpenFileChooser},
		};
		parent.setGroupLayoutUtil(compos, panel);
		
		return panel;
	}


	private void setImportParameters(UserSettings localUserSettings) throws XPathExpressionException{
		// 対象ファイルオプション
		String defaultFileTypes = localUserSettings.evaluateOneNode2("/setting/import/target_file_type/@names",false); //$NON-NLS-1$
		String defaultNormType =  localUserSettings.evaluateOneNode2("/setting/import/char_normalization/@name",false); //$NON-NLS-1$
		String defaultIsHtmlTidied = localUserSettings.evaluateOneNode2("/setting/import/xhtml_style_sheet/@isTidied",false); //$NON-NLS-1$
		selectCheckBox(checkTargetFile_text, defaultFileTypes);
		selectCheckBox(checkTargetFile_xhtml, defaultFileTypes);
		selectCheckBox(checkTargetFile_xml, defaultFileTypes);

		
		dir_xsl_txt = localUserSettings.evaluateOneNode2("/setting/import/text_transformation_definition/@dir",false); //$NON-NLS-1$
		dir_xsl_xhtml = localUserSettings.evaluateOneNode2("/setting/import/xhtml_style_sheet/@dir",false); //$NON-NLS-1$
		dir_xsl_xml = localUserSettings.evaluateOneNode2("/setting/import/xml_style_sheet/@dir",false); //$NON-NLS-1$

		txtStylesheetDefault = localUserSettings.evaluateOneNode2("/setting/import/text_transformation_definition/@default",false); //$NON-NLS-1$
		xhtmlStylesheetDefault = localUserSettings.evaluateOneNode2("/setting/import/xhtml_style_sheet/@default",false); //$NON-NLS-1$
		xmlStylesheetDefault = localUserSettings.evaluateOneNode2("/setting/import/xml_style_sheet/@default",false); //$NON-NLS-1$

		constructIncludeSubcorpora = localUserSettings.evaluateOneNode2("/setting/import/as_subcorpora/@value",false); //$NON-NLS-1$
		constructNotNowIndeing = localUserSettings.evaluateOneNode2("/setting/import/not_now_indexing/@value",false); //$NON-NLS-1$
		
		configuredSrcDir = localUserSettings.evaluateOneNode2("/setting/import/source_files/@src_dir",false); //$NON-NLS-1$
		String corpusName = localUserSettings.evaluateOneNode2("/setting/import/source_files/@corpus_name",false); //$NON-NLS-1$
		if(corpusName != null && !corpusName.isEmpty()){
			tFieldImportCorpusName.setText(corpusName);
		} else if(configuredSrcDir != null && !configuredSrcDir.isEmpty()){
			tFieldImportCorpusName.setText(new File(configuredSrcDir).getName());
		} else {
			tFieldImportCorpusName.setText(""); //$NON-NLS-1$
		}
		
		

		// 文字正規化オプション
		radioButtonCharNormNone.setSelected(true);
		if(defaultNormType.equals(TEXT_NORMALIZATION_TYPE_TABLE)){
			radioButtonCharNormUserDefined.setSelected(true);
		} else if(defaultNormType.equals(TEXT_NORMALIZATION_TYPE_NFKC)){
			radioButtonCharNormNFKC.setSelected(true);
		} else {
			radioButtonCharNormNone.setSelected(true);
		}

		// テキスト変換規則
		ArrayList<String> txtStyleSheetPaths =
				getFileList(dir_xsl_txt, Messages.getString("TextFileImportDialog.11"), ".*\\.htd"); //$NON-NLS-1$ //$NON-NLS-2$
		selectComboBox(comboTextFileTransform, txtStyleSheetPaths, txtStylesheetDefault);

		// XHTML 用 XSL
		ArrayList<String> xhtmlStyleSheetPaths =
				getFileList(dir_xsl_xhtml, Messages.getString("TextFileImportDialog.11"), ".*\\.xsl"); //$NON-NLS-1$ //$NON-NLS-2$
		selectComboBox(comboTextImport_xhtml_xsl, xhtmlStyleSheetPaths, xhtmlStylesheetDefault);
		// HTML -> XHTML
		if(defaultIsHtmlTidied.equals("true")){ //$NON-NLS-1$
			checkTextConvert_html.setSelected(true);
		} else {
			checkTextConvert_html.setSelected(false);
		}
		
		// XML 用 XSL
		ArrayList<String> xmlStyleSheetPaths =
				getFileList(dir_xsl_xml, Messages.getString("TextFileImportDialog.25"), ".*\\.xsl"); //$NON-NLS-1$ //$NON-NLS-2$
		selectComboBox(comboTextImport_xml_xsl, xmlStyleSheetPaths, xmlStylesheetDefault);
		
		// config file
		ArrayList<String> configFiles = getFileList(TextFileImporter.DIR_RESOURCE_TEMPLATE, "", ".*[Cc]onfig.*\\.xml");  //$NON-NLS-1$//$NON-NLS-2$
		selectComboBox(comboConfigFile, configFiles, TextFileImporter.DEFAULT_CONFIG_TEMPLATE);

		// 構築オプション サブコーパス？
		if(constructIncludeSubcorpora != null && constructIncludeSubcorpora.equalsIgnoreCase("true")){ //$NON-NLS-1$
			checkBoxConstructIncludeSubcorpora.setSelected(true);
		} else {
			checkBoxConstructIncludeSubcorpora.setSelected(false);
			constructIncludeSubcorpora = "false"; //$NON-NLS-1$
		}
		// 構築オプション indexing しない？
		if(constructNotNowIndeing == null || !constructNotNowIndeing.equalsIgnoreCase("true")){ //$NON-NLS-1$
			checkBoxConstructNotNowIndexing.setSelected(false);
			constructNotNowIndeing = "true"; //$NON-NLS-1$
		} else {
			checkBoxConstructNotNowIndexing.setSelected(true);
		}
		
		// option for morphological analysis
		String morphAnalyzerNames[] = localUserSettings.evaluateAtributeList("/setting/annotator/li", "name", true); //$NON-NLS-1$ //$NON-NLS-2$
		comboMorphAnalyzers.removeAllItems();
		comboMorphAnalyzers.addItem(NOT_USE_ANNOTATOR);
		for(String morphAnalyzerName : morphAnalyzerNames){
			comboMorphAnalyzers.addItem(morphAnalyzerName);
		}
	}
	

	/**
	 * 変換オプション設定画面
	 * @return
	 */
	private JPanel getJpanel_importText_2() {
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(Messages.getString("TextFileImportDialog.8"))); //$NON-NLS-1$
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		jPanelConvOpt = getPanelConvOpt();
//		panel.add(jPanelConvOptText);
		panel.add(jPanelConvOpt);

		return panel;
	}


	
	private Component getPanelConvOpt() {
		JPanel panel = new JPanel();

		// text type
		JLabel lbTextType = new JLabel(Messages.getString("TextFileImportDialog.64")); //$NON-NLS-1$
		//// radio button
		panel_rbTextType = new JPanel();
		panel_rbTextType.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		checkTargetFile_text = new JCheckBox(FILE_TYPE_TXT);
		checkTargetFile_xhtml = new JCheckBox(FILE_TYPE_XHTML);
		checkTargetFile_xml = new JCheckBox(FILE_TYPE_XML);
		panel_rbTextType.add(checkTargetFile_text);
		panel_rbTextType.add(checkTargetFile_xhtml);
		panel_rbTextType.add(checkTargetFile_xml);
		
		// character normalization
		//// label
		JLabel lbCharNorm = new JLabel(Messages.getString("TextFileImportDialog.lbCharNorm")); //$NON-NLS-1$
		//// radio button
		panel_rbCharNorm = new JPanel();
		panel_rbCharNorm.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		ButtonGroup bgCharNorm = new ButtonGroup();
		radioButtonCharNormNone = new JRadioButton(Messages.getString("TextFileImportDialog.rbCharNormNo")); //$NON-NLS-1$
		radioButtonCharNormUserDefined = new JRadioButton(Messages.getString("TextFileImportDialog.rbCharNormTable")); //$NON-NLS-1$
		radioButtonCharNormNFKC = new JRadioButton(Messages.getString("TextFileImportDialog.rbCharNormUnicode")); //$NON-NLS-1$
		bgCharNorm.add(radioButtonCharNormNone);
		bgCharNorm.add(radioButtonCharNormUserDefined);
		bgCharNorm.add(radioButtonCharNormNFKC);
		panel_rbCharNorm.add(radioButtonCharNormNone);
		panel_rbCharNorm.add(radioButtonCharNormUserDefined);
		panel_rbCharNorm.add(radioButtonCharNormNFKC);

		// text transformation
		JLabel lbTextTran = new JLabel(
				Messages.getString("TextFileImportDialog.lbTextTran")); //$NON-NLS-1$
		comboTextFileTransform = new JComboBox<String>();
		
		// xhtml stylesheet
		JLabel labelXHTMLConvert =  new JLabel(Messages.getString("TextFileImportDialog.10")); //$NON-NLS-1$
		comboTextImport_xhtml_xsl = new JComboBox<String>();

		checkTextConvert_html = new JCheckBox(Messages.getString("TextFileImportDialog.12")); //$NON-NLS-1$
		
		// xhtml stylesheet
		JLabel labelXMLConvert =  new JLabel(Messages.getString("TextFileImportDialog.14")); //$NON-NLS-1$
		comboTextImport_xml_xsl = new JComboBox<String>();

		// config file
		JLabel labelConfigFile =  new JLabel(Messages.getString("TextFileImportDialog.36")); //$NON-NLS-1$
		comboConfigFile = new JComboBox<String>();
		
		// construction option
		panelConstruction = new JPanel();
		panelConstruction.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		JLabel labelConstruction = new JLabel(Messages.getString("TextFileImportDialog.71")); //$NON-NLS-1$
		checkBoxConstructIncludeSubcorpora = new JCheckBox(Messages.getString("TextFileImportDialog.72")); //$NON-NLS-1$
		checkBoxConstructNotNowIndexing = new JCheckBox(Messages.getString("TextFileImportDialog.73")); //$NON-NLS-1$
		panelConstruction.add(checkBoxConstructIncludeSubcorpora);
		panelConstruction.add(checkBoxConstructNotNowIndexing);
		
		// morphological analysis option
		JLabel labelMorph = new JLabel(Messages.getString("TextFileImportDialog.30")); //$NON-NLS-1$
		comboMorphAnalyzers = new JComboBox<String>();
		JPanel panelTargetElement = new JPanel();
		textFieldAnalyzedElement = new JTextField();
		textFieldAnalyzedAttribute = new JTextField();
		textFieldAnalyzedAttributeValue = new JTextField();
		panelTargetElement.setLayout(new BoxLayout(panelTargetElement, BoxLayout.X_AXIS));
		panelTargetElement.add(new JLabel(Messages.getString("TextFileImportDialog.27"))); //$NON-NLS-1$
		panelTargetElement.add(Box.createHorizontalStrut(5));
		panelTargetElement.add(textFieldAnalyzedElement);
		panelTargetElement.add(Box.createHorizontalStrut(3));
		panelTargetElement.add(textFieldAnalyzedAttribute);
		panelTargetElement.add(Box.createHorizontalStrut(3));
		panelTargetElement.add(textFieldAnalyzedAttributeValue);
		
		try {
			setImportParameters(parent.getUserSettings());
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Component[][] compos = {
				{lbTextType, panel_rbTextType},
				{lbCharNorm, panel_rbCharNorm},
				{lbTextTran, comboTextFileTransform},
				{labelXHTMLConvert, comboTextImport_xhtml_xsl},
				{null, checkTextConvert_html},
				{labelXMLConvert, comboTextImport_xml_xsl},
				{labelConfigFile, comboConfigFile},
				{labelConstruction, panelConstruction},
				{labelMorph, comboMorphAnalyzers},
				{null, panelTargetElement},
		};

		parent.setGroupLayoutUtil(compos, panel);

		return panel;
	}

	private void selectCheckBox(JCheckBox jc, String items){
		if(items == null){
			items = ""; //$NON-NLS-1$
		}
		
		items = items.toLowerCase();
		HashSet<String> hs = new HashSet<String>(Arrays.asList(items.split("[,\\s]+"))); //$NON-NLS-1$
		if(hs.contains(jc.getText().toLowerCase())){
			jc.setSelected(true);
		} else {
			jc.setSelected(false);
		}
	}


	// add items and defaultItem to jb and select defaultItem
	private void selectComboBox(JComboBox<String> jb, ArrayList<String> items, String defaultItem){

		for(String item: items){
			jb.addItem(item);
		}
		
		if(!items.contains(defaultItem) && !defaultItem.isEmpty()){
			jb.addItem(defaultItem);
		}
		
		for(int i = 0; i < jb.getItemCount(); i++){
			if(jb.getItemAt(i).equals(defaultItem)){
				jb.setSelectedIndex(i);
			}
		}
	}
	
	
	/**
	 * Get a file list from the path
	 * @return
	 */
	private ArrayList<String> getFileList(String path, String labelNotSelected, String filterRegex) {
		ArrayList<String> list = new ArrayList<String>();

		// item for no selection
		if(!labelNotSelected.isEmpty()){
			list.add(labelNotSelected);
		}
		
		try {
			File dir = new File(path); //$NON-NLS-1$

			for (String name : dir.list()) {
				if (!list.contains(name) && name.matches(filterRegex)) {
					list.add(name);
				}
			}
		} catch (NullPointerException e) {
			// do nothing
			e.printStackTrace();
		}

		return list;
	}


	private JPanel getJpanel_importText_3() {
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(Messages.getString("TextFileImportDialog.20"))); //$NON-NLS-1$

		JLabel label1 =  new JLabel(Messages.getString("TextFileImportDialog.21")); //$NON-NLS-1$

		tFieldImportCorpusName = new JTextField();

		Component[][] compos = {
			{label1 ,tFieldImportCorpusName},
		};

		parent.setGroupLayoutUtil(compos, panel);
		return panel;
	}

	private JPanel getJpanel_importText_4() {

		final JDialog thisDialog = this;

		JPanel panelSubmit = new JPanel();
		panelSubmit.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		// ボタン
		btnImportText = new JButton(
				Messages.getString("TextFileImportDialog.22")); //$NON-NLS-1$
		btnImportText.setPreferredSize(new Dimension(100, btnImportText
				.getPreferredSize().height));
		btnImportText.setMinimumSize(new Dimension(100, 25));
		panelSubmit.add(btnImportText);

		btnImportText.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					btnImportText_actionPerormed(e);
				} catch (Exception ex) {
					System.err.println(Messages.getString("TextFileImportDialog.76")); //$NON-NLS-1$
				}
			}
		});

		JButton button = new JButton(
				Messages.getString("TextFileImportDialog.23")); //$NON-NLS-1$
		button.setPreferredSize(new Dimension(100, btnImportText
				.getPreferredSize().height));
		panelSubmit.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// ウインドウを閉じる
				thisDialog.setVisible(false);
			}
		});

		return panelSubmit;
	}

	private JPanel getJpanel_importText_advancedOption() {

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		jContentPane1_importText.add(panel);
		
		jLabel_advancedOption = new JLabel(Messages.getString("TextFileImportDialog.13")); //$NON-NLS-1$
		jLabel_advancedOption.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				jLabel_advancedOption.setForeground(new Color(0, 0, 128));
			}
			@Override
			public void mouseExited(MouseEvent e) {
				jLabel_advancedOption.setForeground(new Color(51, 51, 51));
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				if(!jContentPane1_importText_2.isVisible()){
					jContentPane1_importText_2.setVisible(true);
					jLabel_advancedOption.setVisible(false);
				}
				thisDialog.pack();
				thisDialog.setLocationRelativeTo(parent);
			}
		});
		jLabel_advancedOption.setFont(jLabel_advancedOption.getFont().deriveFont(jLabel_advancedOption.getFont().getSize() - 2f));
		panel.add(jLabel_advancedOption);
		return panel;
	}

	
	/**
	 * [変換する]ボタンアクション
	 * @throws Exception
	 */
	private void btnImportText_actionPerormed(ActionEvent e) {
		HimawariLogger logger = new HimawariLogger();

		btnImportText.setEnabled(false);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JTextPane pane = new JTextPane();
		pane.setContentType("text/html"); //$NON-NLS-1$
		JScrollPane scrollPane = new JScrollPane();
		pane.setPreferredSize(new Dimension(600,250));
		scrollPane.setViewportView(pane);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		panel.add(scrollPane);

		
		try{
			btnImportText.setEnabled(false);

			/**TODO データバリデーション**/
			if( !validateInputImportText()){
				return;
			}

			//対象データルートフォルダ
			String srcDir = tFieldTargetDir.getText();
			if(configuredSrcDir != null && !configuredSrcDir.isEmpty()){
				srcDir += "/" + configuredSrcDir; //$NON-NLS-1$
			}


			//対象拡張子
			ArrayList<String> extList = new ArrayList<String>();
			if(checkTargetFile_text.isSelected()){
				extList.addAll(Arrays.asList(TXT_SUFFIXES));
			}
			if(checkTargetFile_xhtml.isSelected()){
				extList.addAll(Arrays.asList(XHTML_SUFFIXES));
			} 
			if(checkTargetFile_xml.isSelected()){
				extList.addAll(Arrays.asList(XML_SUFFIXES));
			}

			//テキスト関連オプション
			String optTextNorm = TEXT_NORMALIZATION_TYPE_NONE;
			String optTxtStyle = null;
			
			if (radioButtonCharNormNone.isSelected()) {
				optTextNorm = TEXT_NORMALIZATION_TYPE_NONE;
			} else if (radioButtonCharNormUserDefined.isSelected()) {
				optTextNorm = TEXT_NORMALIZATION_TYPE_TABLE;
			} else if (radioButtonCharNormNFKC.isSelected()) {
				optTextNorm = TEXT_NORMALIZATION_TYPE_NFKC;
			}

			if (comboTextFileTransform.getSelectedIndex() != 0) {
				optTxtStyle = dir_xsl_txt + "/" //$NON-NLS-1$
						+ (String) comboTextFileTransform.getSelectedItem();
			}

			//xhtml関連オプション
			String optXhtmlStyle = null;
			boolean optXhtmlIsConvertHtml = false;
			if(checkTargetFile_xhtml.isSelected()){
				if(comboTextImport_xhtml_xsl.getSelectedIndex() != 0){
					optXhtmlStyle = dir_xsl_xhtml + "/" + (String)comboTextImport_xhtml_xsl.getSelectedItem(); //$NON-NLS-1$
				}
				optXhtmlIsConvertHtml = checkTextConvert_html.isSelected();
			}

			//XML 関連オプション
			String optXmlStyle = null;
			if(checkTargetFile_xml.isSelected()){
				if(comboTextImport_xml_xsl.getSelectedIndex() != 0){
					optXmlStyle = dir_xsl_xml + "/" + (String)comboTextImport_xml_xsl.getSelectedItem(); //$NON-NLS-1$
				}
			}

			// template config file
			String optTemplateConfigFile = TextFileImporter.DIR_RESOURCE_TEMPLATE + "/" + (String)comboConfigFile.getSelectedItem(); //$NON-NLS-1$

			
			// 構築関連オプション
			boolean optConstuctIncludeSubcorpora = false;
			if(checkBoxConstructIncludeSubcorpora.isSelected()){
				optConstuctIncludeSubcorpora = true;
			}
			String selectedAnnotator = (String)comboMorphAnalyzers.getSelectedItem();
			
			//出力コーパス名
			String outputName = tFieldImportCorpusName.getText();

			textFileImporter = new TextFileImporter(this, parent.getUserSettings());

			//インポートの実行
			String configFileName = textFileImporter.execute(srcDir, extList.toArray(new String[0]),
					optTextNorm,
					optTxtStyle,
					optXhtmlStyle, optXhtmlIsConvertHtml,
					optXmlStyle,
					outputName,
					optConstuctIncludeSubcorpora,
					selectedAnnotator,
//					optConstructNotNowIndexing,
					importConfig,
					optTemplateConfigFile);

			//ユーザがキャンセルした場合は終了
			if(textFileImporter.getStatus() == TextFileImporter.STATUS_CANCEL ){
				btnImportText.setEnabled(true);
				return;
			}

			// initialize himawari by configFileName
			parent.setConfigFileName(configFileName);
			
			parent.myInit();

			// generate indexes
			if(!checkBoxConstructNotNowIndexing.isSelected()){
				if(!selectedAnnotator.equals(TextFileImportDialog.NOT_USE_ANNOTATOR)){
					String strXPathCommand = "/setting/annotator/li[@name='" + selectedAnnotator + "']"; //$NON-NLS-1$ //$NON-NLS-2$
					UserSettings newUserSettings = parent.getUserSettings();
					newUserSettings.setAttribute(strXPathCommand, "extract", "element", textFieldAnalyzedElement.getText()); //$NON-NLS-1$ //$NON-NLS-2$
					newUserSettings.setAttribute(strXPathCommand, "extract", "attribute", textFieldAnalyzedAttribute.getText()); //$NON-NLS-1$ //$NON-NLS-2$
					newUserSettings.setAttribute(strXPathCommand, "extract", "value", textFieldAnalyzedAttributeValue.getText());		 //$NON-NLS-1$ //$NON-NLS-2$
				}

				
				if(!parent.genarateIndex(this, false, (String) comboMorphAnalyzers.getSelectedItem())){
					btnImportText.setEnabled(true);
					//ウインドウを閉じる
					this.setVisible(false);
					return;
				}

				parent.myInit(); // init again for loading generated corpus structures
			}

			btnImportText.setEnabled(true);
			this.setVisible(false);

			HimawariListedLog successfulFilesLog = textFileImporter.getSuccessfulFilesLog();
			HimawariListedLog failedFilesLog = textFileImporter.getFailedFilesLog();
			HimawariListedLog summaryLog = textFileImporter.getSummaryLog();
			summaryLog.add(successfulFilesLog.getSize() + Messages.getString("TextFileImportDialog.26")); //$NON-NLS-1$
			
			if(failedFilesLog.getSize() == 0){
				// success with no error
				logger.add(new HimawariResultLog(Messages.getString("TextFileImportDialog.32"))); //$NON-NLS-1$
				logger.add(summaryLog);
				logger.add(successfulFilesLog);
				pane.setText(logger.write());
				pane.setCaretPosition(0);
				JOptionPane.showMessageDialog(parent, panel); //$NON-NLS-1$ //$NON-NLS-2$
			}else{
				// success with some errors
				summaryLog.add(failedFilesLog.getSize() + Messages.getString("TextFileImportDialog.29")); //$NON-NLS-1$
				logger.add(new HimawariResultLog(Messages.getString("TextFileImportDialog.37"))); //$NON-NLS-1$
				logger.add(summaryLog);
				logger.add(failedFilesLog);
				logger.add(successfulFilesLog);
				pane.setText(logger.write());
				pane.setCaretPosition(0);
				JOptionPane.showMessageDialog(parent, panel, Messages.getString("TextFileImportDialog.45"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}catch(Exception err){
			err.printStackTrace();

			logger.add(new HimawariResultLog(Messages.getString("TextFileImportDialog.33"))); //$NON-NLS-1$
			HimawariListedLog failedFilesLog = textFileImporter.getFailedFilesLog();
			HimawariListedLog summaryLog = textFileImporter.getSummaryLog();

			try {
				summaryLog.add(Messages.getString("TextFileImportDialog.34") + err.getMessage()); //$NON-NLS-1$
				// remove created files
				textFileImporter.deleteCreatedFiles();
			} catch (Exception e1) {
				summaryLog.add(Messages.getString("TextFileImportDialog.35")); //$NON-NLS-1$
				e1.printStackTrace();
			}
			logger.add(summaryLog);
			logger.add(failedFilesLog);
			pane.setText(logger.write());
			pane.setCaretPosition(0);
			JOptionPane.showMessageDialog(parent, panel, Messages.getString("TextFileImportDialog.51"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			btnImportText.setEnabled(true);
			this.setVisible(false);
		}
	}

	

	/**
	 * 入力内容のチェックを行う
	 * @return
	 */
	private boolean validateInputImportText() {

		String validateMessage = ""; //$NON-NLS-1$

		//テキストファイルインポート対象フォルダ
		if(tFieldTargetDir.getText().length() > 0){
			File targetDirTmp = new File(tFieldTargetDir.getText());
			if(!targetDirTmp.isDirectory()){
				validateMessage += tFieldTargetDir.getText() + Messages.getString("TextFileImportDialog.53") +"<br />"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}else{
			validateMessage += Messages.getString("TextFileImportDialog.55") + "<br />"; //$NON-NLS-1$ //$NON-NLS-2$
		}

		//変換対象ファイル
		ArrayList<String> extList = new ArrayList<String>();
		if(checkTargetFile_text.isSelected()){
			extList.addAll(Arrays.asList(TXT_SUFFIXES));
		}
		if(checkTargetFile_xhtml.isSelected()){
			extList.addAll(Arrays.asList(TXT_SUFFIXES));
		}
		if(checkTargetFile_xml.isSelected()){
			extList.addAll(Arrays.asList(TXT_SUFFIXES));
		}
		if(extList.size() == 0){
			validateMessage += Messages.getString("TextFileImportDialog.60") + "<br />"; //$NON-NLS-1$ //$NON-NLS-2$
		}

		//出力コーパス名
		if(tFieldImportCorpusName.getText().length() == 0){
			validateMessage += Messages.getString("TextFileImportDialog.62") + "<br />"; //$NON-NLS-1$ //$NON-NLS-2$
		}

		if(validateMessage.length() > 0){
			JLabel label = new JLabel("<html>"+ validateMessage + "</html>"); //$NON-NLS-1$ //$NON-NLS-2$
			JOptionPane.showMessageDialog(this, label, Messages.getString("TextFileImportDialog.66"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
			btnImportText.setEnabled(true);
			return false;
		}

		return true;
	}
}

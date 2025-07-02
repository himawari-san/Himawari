/*
    Copyright (C) 2004-2025 Masaya YAMAGUCHI, Hajime Osada

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

import java.awt.BorderLayout;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.JTabbedPane;
import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.JComboBox;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import java.awt.Font;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JRadioButton;
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.JPopupMenu;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import jp.ac.ninjal.soundplayer.SoundPlayerFrame;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.String;
import java.lang.Integer;
import java.nio.file.Path;
import java.awt.event.InputEvent;

/**
 * @author osada
 *
 */
public class Frame1 extends JFrame {

	// System Name
	final private String systemName = Messages.getString("Frame1.0"); //$NON-NLS-1$  //  @jve:decl-index=0:
	
	final static int DEFAULT_FONT_SIZE = 14;

	// 有効フラグ
	private final static int VALID_FLAG = 0;

	// 無効フラグ
	private final static int INVALID_FLAG = 1;

	private final static int DEFAULT_KWIC_CONTEXT_LENGTH = 10;

	private UserSettings userSetting = new UserSettings(); // @jve:decl-index=0:
	
	private LocaleUtil localeUtil = new LocaleUtil();

	// configuration file name (default)
	private String configFileName = UserSettings.DEFAULT_CONFIG_FILE;

	private HashMap<String, SIXDic> dicFarm = new HashMap<String, SIXDic>();
	
	public String getConfigFileName() {return configFileName;}
	public void setConfigFileName(String configFileName) {this.configFileName = configFileName;}
	public SIXDic getDic(String elementName){
		if(dicFarm.containsKey(elementName)){
			return dicFarm.get(elementName);
		} else {
			return new SIXDic(null, ""); //$NON-NLS-1$
		}
	}
	public UserSettings getUserSettings(){
		return userSetting;
	}

	private HashMap<String, CorpusElementInfo> elementMap = new HashMap<String, CorpusElementInfo>(); 


	// font size of GUI (default)
	private int fontsize = 12;

	// saving user setting (default)
	private int saveSetting = VALID_FLAG;

	// id for the selected browser
	private int iSelectedBrowser = 0;

	// id for the selected xsl style sheet
	private int iSelectedXSL = 0;

	// annotator
	private String annotatorNames[] = null;
	private String cAnnotatorName = ""; //$NON-NLS-1$

	// 字体辞書
	private KanjiCharTransTable kt;

	// 外部データベース
	private Xdb extDB1;

	private Xdb extDB2;

	// 結果レコードのフィールド名
	private FieldInfo fieldInfo; // @jve:decl-index=0:

	// 検索対象(対象とする cix, aix)
	private String searchTarget[] = null;

	// バージョン情報ウインドウ用アイコンファイル
	final private String[] picfiles = {
			"ogiso.png", "hebaragi3.png", "himawari_chan.png" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	final private String iconFile = "/jp/ac/ninjal/himawari/images/himawari_chan_icon.png"; //$NON-NLS-1$  //  @jve:decl-index=0:

	// フィルタタブの項目数
	final private int nFilter = 3;

	// 最後に選択した検索文字列用 JTextArea
	private SearchTextField selectedJTextField;

	private ResultTable selectedTable = null;

	private int iTab = 1;

	// 検索結果を格納する Vector
	private ArrayList<ArrayList<ResultRecord>> resultRecordSetFarm = new ArrayList<ArrayList<ResultRecord>>();

	// バージョンウインドウの画像選択用乱数
	private int nHelpCalled = (int) (Math.random() * picfiles.length);

	// indexing 機能の表示設定
	private boolean isIndexingEnable = false;

	// Change this variable in <code>searchConditionLabelChange()</code>
	private boolean isRecordBased = false;

	private boolean isRegexKey = false;

	private boolean isErrorOccur = false;

	private JTableHeader selectedHeader = null;

	public ResultTableModel selectedTableModel = null;

	private JList<CorpusFile> jList_nonTargetCorpus = null;

	private JList<CorpusFile> jList_targetCorpus = null;

	private SearchTextField jTextField_filter[] = new SearchTextField[nFilter];

	private HimawariJComboBox jComboBox_filterTarget[] = new HimawariJComboBox[nFilter];

	private HimawariJComboBox jComboBox_filterCondition[] = new HimawariJComboBox[nFilter];

	// 設定保存の有効・無効用ラベル
	private String saveSettingLabel[] = { Messages.getString("Frame1.512"), //$NON-NLS-1$
			Messages.getString("Frame1.513") }; //$NON-NLS-1$

	// ユーザ作業設定
	private UserWorkSetting userWorkSetting; // @jve:decl-index=0:

	private static int MIN_FONT_SIZE = 7;
	private static int MAX_FONT_SIZE = 27;
	private Integer[] availableFontSizes = new Integer[MAX_FONT_SIZE - MIN_FONT_SIZE + 1];
	
	private ExternalApplicationPool externalApplicationPool = new ExternalApplicationPool();

	
	/***************************
	 * Visual Editor 自動生成部分 ↓↓↓↓↓↓↓↓↓↓↓
	 ***************************/
	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JMenuBar jMenuBar1 = null;
	private JMenu jMenuFile = null;
	private JMenuItem jMenuItemNew = null;
	private JMenuItem jMenuSelectCorpus = null;
	private JMenuItem jMenuSaveAs = null;
	private JMenuItem jMenuInstall = null;
	private JMenuItem jMenuFileExit = null;
	private JMenu jMenuEdit = null;
	private JMenuItem jMenuCopy = null;
	private JMenuItem jMenuCopyWithHeader = null;
	private JMenuItem jMenuPaste = null;
	private JMenuItem jMenuCopyHeadValueAndPaste = null;
	private JMenuItem jMenuItem_search = null;
	private JMenuItem jMenuItemReplace = null;
	private JMenuItem jMenuItem_jump = null;
	private JMenuItem jMenuItem_selectAll = null;
	private JMenu jMenuTools = null;
	private JMenu jMenuSort = null;
	private JMenuItem jMenuItem_sort_ascending = null;
	private JMenuItem jMenuItem_sort_descending = null;
	private JMenuItem jMenuItem_sort_random = null;
	private JMenu jMenu_filter = null;
	private JMenuItem jMenuItem_filter_execute = null;
	private JMenuItem jMenuItem_filter_randam = null;
	private JMenuItem jMenuItem_filter_release = null;
	private JMenu jMenu_browse = null;
	private JMenuItem jMenuItem_browse_default = null;
	private ArrayList<JMenuItem> jMenuItem_browse_six_list = null;
	private JMenu jMenuListingElements = null;
	private JMenuItem jMenuItem_listing_corpus = null;
	private JMenuItem jMenuItem_listing_browsed = null;
	private JMenuItem jMenuListingItemUserInput = null;
	private JMenu jMenuUserDefinedLists = null;
	private JMenuItem jMenuItem_external_database1 = null;
	private JMenuItem jMenuItem_external_database2 = null;
	private JMenu jMenuStat = null;
	private JMenuItem jMenuItem_stat_1 = null;
	private JMenuItem jMenuItem_stat_2 = null;
	private JMenuItem jMenuItem_stat_3 = null;
	private JMenuItem jMenuItem_stat_user_defined = null;
	private JMenu jMenu_Option = null;
	private JMenuItem jMenuItem_font_size = null;
	private JMenuItem jMenuItem_browser = null;
	private JMenuItem jMenuItem_xsl = null;
	private JMenuItem jMenuItem_saveSetting = null;
	private JMenuItem jMenuItem_language = null;
	private JMenuItem jMenuItemOptionAnnotation = null;
	private JMenuItem jMenuItemOptionAnalyze = null;
	private JMenuItem jMenuGenarateIndex = null;
	private JMenu jMenuConstruct = null;
	private JMenu jMenuHelp = null;
	private JMenuItem jMenuItem_help_man = null;
	private JMenuItem jMenuItem_help_hp = null;
	private JMenuItem jMenuItem_help_package_man = null;
	private JMenuItem jMenuItem_help_package_hp = null;
	private JMenuItem jMenuItem_help_about = null;
	private JPanel jPanel4 = null;
	private JPanel jPanel_Search_Conditions = null;
	private JPanel jPanel_Search_Keys = null;
	private JTabbedPane jTabbedPane_Search_Keys = null; // @jve:decl-index=0:
	private JPanel jPanel_Biblio = null;
	private JPanel jPanel7 = null;
	private JPanel jPanel8 = null;
	private JPanel jPanel9 = null;
	private JPanel jPanel24 = null;
	private JPanel jPanel_TargetSubCorpusList = null;
	private JLabel jLabel10 = null;
	private JScrollPane jScrollPane_TargetSubCorpusList = null;
	private JPanel jPanelSubCorpusSelectorButtons = null;
	private JButton jButton_addCorpus = null;
	private JButton jButton_deleteCorpus = null;
	private JPanel jPanel_NonTargetSubCorpusList = null;
	private JScrollPane jScrollPane_NonTargetSubCorpusList = null;
	private JLabel jLabel9 = null;
	private JPanel jPanel1_Option = null;
	private JTabbedPane jTabbedPaneSearchOptions = null;
	private JPanel jPanel_Jitai = null;
	private JCheckBox jCheckBox_Equivalent = null;
	private JCheckBox jCheckBox_Additional = null;
	private JCheckBox jCheckBox_Itself = null;
	private JCheckBox jCheckBox_Not_Used = null;
	private JPanel jPanel18 = null;
	private JPanel jPanel13 = null;
	private JPanel jPanel19 = null;
	private JLabel jLabelKeyRange = null;
	private JPanel jPanel21 = null;
	private JCheckBox jCheckBox_KeyRangePrev = null;
	private JCheckBox jCheckBox_KeyRangeFol = null;
	private JPanel jPanel10 = null;
	private JPanel jPanel22 = null;
	private JLabel jLabelContextLength = null;
	private JPanel jPanel15 = null;
	private JLabel jLabelNumberOfCharacters = null;
	private JTextField jTextField_Length_of_Context = null;
	private JPanel jPanel11 = null;
	private JPanel jPanel3 = null;
	private JLabel jLabelSearchRange = null;
	private JPanel jPanel5 = null;
	private JTextField jTextField_SearchRange = null;
	private JLabel jLabelNumberOfCharacters2 = null;
	private JPanel jPanel_extraction_options = null;
	private JRadioButton jRadioButton_extraction_all = null;
	private JRadioButton jRadioButton_extraction_random = null;
	private JRadioButton jRadioButton_extraction_countonly = null;
	private JRadioButton jRadioButton_countonly_option1 = null;
	private JRadioButton jRadioButton_countonly_option2 = null;

	private JTextField jTextField_nExtraction = null;
	private JTextField jTextField_nSample = null;
	private JPanel jPanel_Buttons = null;
	private JButton jButton_Search = null;
	private JButton jButton_Trans2OldChar = null;
	private JButton jButton_ClearSearchKey = null;
	private JPanel jPanel14 = null;
	private JLabel jLabel1 = null;
	private JPanel jPanel2 = null;
	private JPanel jPanel1 = null;
	private JTextField jTextField_SelectedValue = null;
	private JLabel statusBar = null;

	private JTabbedPane jTabbedPane_ResultTables = null;

	private JPanel jPanel_Target_Words = null;

	private JPanel jPanel20 = null;

	private SearchTextField jTextField_Key = null;

	private JPanel jPanel6 = null;

	private JLabel jLabel_PrevContext = null;

	private SearchTextField jTextField_KeyPrev = null;

	private HimawariJComboBox jComboBox_KeyPrev;

	private JPanel jPanel12 = null;

	private JLabel jLabel_FolContext = null;

	private SearchTextField jTextField_KeyFol = null;

	private HimawariJComboBox jComboBox_KeyFol;

	// private JComboBox jComboBox_searchTarget = null;
	private HimawariJComboBox jComboBox_searchTarget = null;

	private ArrayList<SearchResultFrame> resultFrames = new ArrayList<SearchResultFrame>();
	
	/***************************
	 * TriAx 追加プロパティ
	 ***************************/
	// 検索用worker(マルチスレッド実装用)
	private SearchEngineWorker searchEngineWorker = new SearchEngineWorker(null); // @jve:decl-index=0:

	// Database管理クラス
	private DBController dbController = null;

	// コーポラパス
	private String corporaPath = ""; //$NON-NLS-1$

	// DB編集可能フラグ
//	private boolean dbEditable = false;

	// 検索ボタンアクション
	private Action actionSearch = new ActionSearch(); // @jve:decl-index=0:

	private JButton jButton_save_db = null;

	private JButton jButton_revert_db = null;

	private JPanel jPanel_status = null;

	private JLabel jLabel_save_db = null;

	private JDialog jDialog_resetDb = null;  //  @jve:decl-index=0:visual-constraint="19,3469"

	private JPanel jContentPane11 = null;
	private JMenuItem jMenuFileImportText;

	/**
	 * テキストデータインポートウインドウの要素
	 */
	private JDialog jDialog_importText = null;  //  @jve:decl-index=0:visual-constraint="23,4340"

	private JComboBox<String> jComboBox_SelectedValue = null;
	private JPanel jPanel_save;
	private JPanel jPanel_message;


	/**
	 * This is the default constructor
	 */
	public Frame1() {
		super();
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);

		try {
			jbInit();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		try {
			// ユーザ設定ファイルの読み込み1
			// (保存の有効・無効, configファイルのみ読み込む)
			readUserSetting1();

			// config設定の読み込み
			myInit();

			// ユーザ設定ファイルの読み込み2
			// (configファイル情報の取得後に実行)
			readUserSetting2();

		} catch (Exception e2) {
			Util.showErrorMessages(this, e2);
			e2.printStackTrace();
			System.err.println("Error: " + e2.getMessage()); //$NON-NLS-1$
		}

	}

	/**
	 * ユーザ設定ファイルの読み込み1 (保存の有効・無効, configファイルのみ読み込む)
	 */
	private void readUserSetting1() {
		try {
			userWorkSetting = new UserWorkSetting();
		} catch (Exception e) {
			e.printStackTrace();
			//エラー時は警告ダイアログを表示する
			JOptionPane.showMessageDialog(null, Messages.getString("Frame1.514")); //$NON-NLS-1$
		}

		// ユーザ設定保存
		int tempSaveSetting = userWorkSetting.getSave_setting();
		saveSetting = tempSaveSetting != UserWorkSetting.INT_ERROR ? tempSaveSetting
				: saveSetting;

		// ユーザ設定ファイルの読み込み2
		if (saveSetting == VALID_FLAG && userWorkSetting.existSettingFile) {
			String tempConfigFilePath = userWorkSetting.getConfig_file_path();
			configFileName = tempConfigFilePath.equals(UserWorkSetting.STR_ERROR) ? configFileName : tempConfigFilePath;
		}
	}

	/**
	 * ユーザ設定保存ファイルの読み込み (configファイル情報に依存するものを含むため、 configファイル情報の取得後に実行すること)
	 */
	private void readUserSetting2() {

		if (saveSetting == VALID_FLAG && userWorkSetting.existSettingFile) {
			setValueFromUserWorkSetting();
			// フォントの設定
			setFontSize(fontsize);
		}
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void jbInit() {
		this.setIconImage(Toolkit.getDefaultToolkit().createImage(
				getClass().getResource(iconFile)));
		this.setSize(new Dimension(800, 600));
		this.setJMenuBar(getJMenuBar1());
		this.setContentPane(getJContentPane());

		new DropTarget(this, new DropFileAdapter());

		// ウインドウを閉じても自動的に終了しないようにする
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if(e.isControlDown()){
					int wheelRotation = e.getWheelRotation();
					if(fontsize > MIN_FONT_SIZE && wheelRotation < 0){
						fontsize--;
					} else if(fontsize < MAX_FONT_SIZE && wheelRotation > 0){
						fontsize++;
					} else {
						return;
					}
					setFontSize(fontsize);
				}
			}
		});
	}
	
	/**
	 * アプリケーションを終了する
	 */
	private void doExit() {
		// 設定保存が失敗し、ユーザが保存せずに終了を選んだ場合
		// もしくはウインドウを閉じた場合は、終了しない
		int result = saveUserWorkSetting();
		if (result == JOptionPane.NO_OPTION) {
			return;
		}

		//データ保存の確認
		if(isTableEditted()){
			int editResult = JOptionPane.showConfirmDialog(this, Messages.getString("Frame1.616"), //$NON-NLS-1$
					Messages.getString("Frame1.1"), JOptionPane.YES_NO_CANCEL_OPTION); //$NON-NLS-1$
			//Yes: データを保存
			if (editResult == JOptionPane.YES_OPTION) {
				doSaveDb();
			}
			//No: キャンセル終了しない
			else if(editResult == JOptionPane.CANCEL_OPTION){
				return;
			}
			//Cancel: 保存せずに終了
		}

		//dbコネクションの切断
		if(dbController != null){
			dbController.closeConnection();
		}

		this.dispose();
		System.exit(0);
	}

	/**
	 * initialization
	 *
	 * @throws java.lang.Exception
	 */
	void myInit() throws Exception {

		iTab = 1;
		jTabbedPane_ResultTables.removeAll();
		
		// close existed searchResultFrames
		for(SearchResultFrame sf : resultFrames){
			sf.setVisible(false);
			sf.dispose();
		}
		resultFrames.clear();
		

		// font size
		for(int i = MIN_FONT_SIZE; i <= MAX_FONT_SIZE; i++){
			availableFontSizes[i-MIN_FONT_SIZE] = i;
		}
		
		// initialize by configuration file
		File configFile = new File(configFileName);
		if (!configFile.exists()) {
			JOptionPane
					.showMessageDialog(
							this,
							Messages.getString("Frame1.111") + configFileName + Messages.getString("Frame1.112") + //$NON-NLS-1$ //$NON-NLS-2$
									Messages.getString("Frame1.113"), //$NON-NLS-1$
							Messages.getString("Frame1.114"), //$NON-NLS-1$
							JOptionPane.ERROR_MESSAGE);

			if(new File(UserSettings.DEFAULT_CONFIG_FILE).exists()){
				CorpusChooserPane op = new CorpusChooserPane(new File("./"), UserSettings.DEFAULT_CONFIG_FILE, new Runnable() { //$NON-NLS-1$
					// Executed if a loaded package is going to be removed 
					@Override
					public void run() {
						// this thread is an EDT
						openConfigFile(UserSettings.DEFAULT_CONFIG_FILE);
						System.gc();
					}
				});
				JDialog dialog = op.createDialog(this, Messages.getString("Frame1.84")); //$NON-NLS-1$
				dialog.setVisible(true);
				
				// ok or cancel?
				Object selectedValue = op.getValue();
				if(selectedValue == null || (Integer)selectedValue != JOptionPane.OK_OPTION){
					System.err.println("cancel"); //$NON-NLS-1$
					System.exit(-1);
				}
					
				// get and open config filename
				String selectedConfigFilename = op.getSelectedConfigFilename();
				if(selectedConfigFilename != null && !selectedConfigFilename.isEmpty()){
					openConfigFile(selectedConfigFilename);
				}
			} else {
				JFileChooser fileChooser = new JFileChooser("."); //$NON-NLS-1$
				HimawariFileFilter hff = new HimawariFileFilter("xml", Messages.getString("Frame1.117")); //$NON-NLS-1$ //$NON-NLS-2$
				fileChooser.addChoosableFileFilter(hff);
				fileChooser.setFileFilter(hff);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	
				if (fileChooser.showDialog(this, Messages.getString("Frame1.118")) == JFileChooser.APPROVE_OPTION) { //$NON-NLS-1$
					configFileName = fileChooser.getSelectedFile()
							.getAbsolutePath();
				} else {
					System.exit(-1);
				}
			}
		}

		// configファイルの初期化
		try {
			userSetting.init(configFileName, true);
		}
		// アウトオブメモリエラーの補足
		catch (IOException e) {
			// IOエラーの場合は別のconfigファイルエラーが指定されている
			// 可能性があるため、configファイル名をオーバーライド
			configFileName = userSetting.filename;

		} catch (Exception ex1) {
			JOptionPane.showMessageDialog(null,
					Messages.getString("Frame1.119") + //$NON-NLS-1$
							Messages.getString("Frame1.120"), //$NON-NLS-1$
					Messages.getString("Frame1.121"), //$NON-NLS-1$
					JOptionPane.ERROR_MESSAGE);
		}

		
		// コーポラパスの設定
		corporaPath = userSetting.getAttribute("corpora", "dbpath"); //$NON-NLS-1$ //$NON-NLS-2$
		//"corpora"タグにpathがない場合は、最上位のコーパスのパスをコーポラパスとする
		if(corporaPath == null){
			String pathArray[] = userSetting.getAttributeList("corpora", "path"); //$NON-NLS-1$ //$NON-NLS-2$
			corporaPath = pathArray[0];
			if (!corporaPath.endsWith("/")){ //$NON-NLS-1$
				corporaPath = corporaPath.substring(0, corporaPath.lastIndexOf("/")); //$NON-NLS-1$
			}
		}

		// Get field information
		fieldInfo = FieldInfo.readFieldDiscription(userSetting, "field_setting"); //$NON-NLS-1$
		
		dicFarm.clear();
		for(int i = 0; i < fieldInfo.size(); i++){
			if(fieldInfo.getType(i).equals("dic")){ //$NON-NLS-1$
				String elementNamePS = fieldInfo.getElementName(i);
				String elementName = elementNamePS.replaceFirst("(.+)\\[-?\\d+\\]$", "$1"); //$NON-NLS-1$ //$NON-NLS-2$

				SIXDic dic = null;
				if(dicFarm.containsKey(elementName)){
					dic = dicFarm.get(elementName);
				} else {
					System.err.println(corporaPath + "/himawari." + elementName + SIXDic.suffix); //$NON-NLS-1$
					dic = new SIXDic(new File(corporaPath + "/himawari." + elementName + SIXDic.suffix), elementName); //$NON-NLS-1$
					dic.readDic();
					dicFarm.put(elementName, dic);
				}
				if(!dicFarm.containsKey(elementNamePS)){
					dicFarm.put(elementNamePS, dic);
				}
			}
		}
		
		// This SearchEngineWorker object is used to get corpora list
		// not to be used in the next search process. 
		// A new SearchEngineWorker object will be created in every search
		searchEngineWorker = new SearchEngineWorker(this);
		searchEngineWorker.setFieldInfo(fieldInfo);
		searchEngineWorker.init(userSetting);

		// sets corpus list
		CorpusFile[] corpora = searchEngineWorker.getCorpus();
		
		
		if(dbController != null && dbController.isConnected()){
			dbController.closeConnection();
		}
				
		// Create a DBController object (not connect to the db yet)
		dbController = new DBController(this,corporaPath);
		dbController.setCorpora(corpora);

		if(fieldInfo.isDbReferred()){			
			dbController.openConnection();
			dbController.createCorpusIdMap();

			dbController.initManualAnnotationTable(corpora, fieldInfo);

			if(fieldInfo.isDbEditable()){
				jPanel_save.setVisible(true);
			}else{
				jPanel_save.setVisible(false);
			}
			jMenuCopyHeadValueAndPaste.setEnabled(true);
		} else {
			jPanel_save.setVisible(false);
			jMenuCopyHeadValueAndPaste.setEnabled(false);
		}

		addTable2Tab();


		// clear the set of seach result
		resultRecordSetFarm.clear();
		resultRecordSetFarm.add(new ArrayList<ResultRecord>());

		// set a window title
		this.setTitle(systemName
				+ " - [" + userSetting.getAttribute("corpora", "name") + "] - " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				+ configFileName);


		// 字体変換対象の jTextField 初期値
		selectedJTextField = jTextField_Key;

		// indexing 機能使用の可否
		String resultTemp = userSetting.getAttribute("isIndexingEnable", "value"); //$NON-NLS-1$ //$NON-NLS-2$
		if (resultTemp != null &&  resultTemp.compareTo("true") == 0) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			isIndexingEnable = true;
		} else {
			isIndexingEnable = false;
		}

		// indexing 用の項目の表示可否
		jMenuConstruct.setEnabled(isIndexingEnable);

		// 字体辞書
		if (userSetting.getAttribute("jitaidic", "url") != null) { // jitaidic //$NON-NLS-1$ //$NON-NLS-2$
			kt = new KanjiCharTransTable();
			kt.load(userSetting.getAttribute("jitaidic", "url")); //$NON-NLS-1$ //$NON-NLS-2$
			jButton_Trans2OldChar.setEnabled(true);
			jTabbedPaneSearchOptions.remove(jPanel_Jitai);
			jTabbedPaneSearchOptions.add(jPanel_Jitai, Messages.getString("Frame1.137")); //$NON-NLS-1$

		} else {
			jButton_Trans2OldChar.setEnabled(false);
			jTabbedPaneSearchOptions.remove(jPanel_Jitai);
		}

		// browsedElements
		getJMenuBrowse();

		// lists
		jMenuListingElements.removeAll();
		//// corpora
		if (userSetting.getAttribute("corpus_fields", "name") != null) { //$NON-NLS-1$ //$NON-NLS-2$
			jMenuListingElements.add(jMenuItem_listing_corpus);
			jMenuItem_listing_corpus.setText(userSetting.getAttribute(
					"corpus_fields", "name")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		//// browsed elements
		if (userSetting.getAttribute("unit_fields", "name") != null) { //$NON-NLS-1$ //$NON-NLS-2$
			jMenuListingElements.add(jMenuItem_listing_browsed);
			jMenuItem_listing_browsed.setText(userSetting.getAttribute(
					"unit_fields", "name")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		//// user-defined lists
		List<Node> userDefinedLists = userSetting.evaluateNodes("/setting/user_defined_lists/element", false); //$NON-NLS-1$
		if (userDefinedLists.size() > 0) { //$NON-NLS-1$
			for (int i = 0; i < userDefinedLists.size(); i++) {
				Element element = (Element) userDefinedLists.get(i);
				String label = element.getAttribute("label"); //$NON-NLS-1$
				JMenuItem jm = new JMenuItem(label);
				ActionListenerListingElements lm = new ActionListenerListingElements(
						userSetting, element);
				jm.addActionListener(lm);
				jMenuListingElements.add(jm);
			}
		}
		
		// listing based on user input
		//// initialize map
		elementMap = new HashMap<String, CorpusElementInfo>();
		//// load document structure files
		searchEngineWorker.loadStructure(elementMap);

		HashSet<String> hasIndex = new HashSet<String>(Arrays.asList(userSetting.getAttributeList("index_eix", "name"))); //$NON-NLS-1$ //$NON-NLS-2$
		String[] aixElementNames = userSetting.getAttributeList("index_aix", "name"); //$NON-NLS-1$ //$NON-NLS-2$
		String[] aixTypeNames = userSetting.getAttributeList("index_aix", "type"); //$NON-NLS-1$ //$NON-NLS-2$
		for(int i = 0; i < aixElementNames.length; i++){
			if(aixTypeNames[i].equals("dic")){ //$NON-NLS-1$
				hasIndex.add(aixElementNames[i]);
			}
		}
		for(String elementName : elementMap.keySet().toArray(new String[0])){
			if(!hasIndex.contains(elementName)){
				elementMap.remove(elementName);
			}
		}
		userSetting.getElementInfo(elementMap);
		
		jMenuListingItemUserInput = null;
		jMenuListingItemUserInput = getJMenuListingItemUserInput();
		jMenuListingElements.add(jMenuListingItemUserInput);
		jMenuListingItemUserInput.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Filter newFileter = createFilter();
				if(newFileter.size() > 0){
					int result = JOptionPane.showConfirmDialog(Frame1.this, Messages.getString("Frame1.46")); //$NON-NLS-1$
					if(result == JOptionPane.NO_OPTION){
						newFileter.clear();
					} else if(result == JOptionPane.CANCEL_OPTION){
						return;
					}
				}
				
				HashMap<String, CorpusElementInfo> tmpMap = new HashMap<String, CorpusElementInfo>(elementMap);
				
				ElementSelectionPane op = new ElementSelectionPane(tmpMap);
				JDialog dialog = op.createDialog(Frame1.this, Messages.getString("Frame1.96")); //$NON-NLS-1$
				dialog.setVisible(true);
				
				Object selectedValue = op.getValue();
				if(selectedValue == null || (Integer)selectedValue != JOptionPane.OK_OPTION){
					System.err.println("cancel"); //$NON-NLS-1$
					return;
				}
				
				String selectedElementName1 = op.getSelectedElement(0);
				String selectedElementName2 = op.getSelectedElement(1);
				String selectedElementName3 = op.getSelectedElement(2);
				String selectedElementName;
				ArrayList<String> order = new ArrayList<String>();

				if(!selectedElementName1.equals(ElementSelectionPane.LabelNotSelected)){
					order.add(selectedElementName1);
				}
				if(!selectedElementName2.equals(ElementSelectionPane.LabelNotSelected)){
					order.add(selectedElementName2);
				}
				if(!selectedElementName3.equals(ElementSelectionPane.LabelNotSelected)){
					order.add(selectedElementName3);
				}
				
				if(order.size() == 0){
					JOptionPane.showMessageDialog(Frame1.this, Messages.getString("Frame1.109")); //$NON-NLS-1$
					return;
				} else {
					// last element
					selectedElementName = order.get(order.size()-1);
				}


				for(String elementName : tmpMap.keySet().toArray(new String[0])){
					if(!elementName.equals(selectedElementName1) 
							&& !elementName.equals(selectedElementName2)
							&& !elementName.equals(selectedElementName3)){
						tmpMap.remove(elementName);
						System.err.println("remove:" + elementName); //$NON-NLS-1$
					}
				}

				final int options = op.getOptionalFields();
				final int contextLength = op.getContextLength();
				System.err.println("len:" + contextLength); //$NON-NLS-1$
				FieldInfo newFieldInfo = FieldInfo.readFieldDiscription(tmpMap, order, options, contextLength);
				
				final SearchResultFrame sf = new SearchResultFrame(Frame1.this);
				resultFrames.add(sf);
				sf.setFieldInfo(newFieldInfo);

				final String tmpSelectedElementName = selectedElementName;
				sf.getTable().addMouseListener(new java.awt.event.MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
							int selectedRow = sf.getTable().getSelectedRow();
							ResultRecord selectedRecord = ((ResultTableModel) sf.getTable()
									.getModel()).getRecordAt(selectedRow);
							int iCorpus = selectedRecord.getResourceID();
							int index = selectedRecord.getPosition();
							int lengthSP = 1;
							if((options & FieldInfo.OPTION_LENGTH) > 0){
								lengthSP = (Integer)selectedRecord.get(tmpSelectedElementName + "%" + FieldInfo.LABEL_LENGTH); //$NON-NLS-1$
							} else if((options&FieldInfo.OPTION_CONTENTS) > 0){
								lengthSP = Util.strlenSP((String)selectedRecord.get(tmpSelectedElementName + "%" + FieldInfo.LABEL_CONTENTS)); //$NON-NLS-1$
							}
							if(index == -1){
								return;
							}
							
							try {
								searchEngineWorker.getBrowsedElement(iCorpus, index, lengthSP, CorpusBrowser.ANCHOR_NAME, CorpusBrowser.ANCHOR_ID);
							} catch(Exception e2){
								JOptionPane.showMessageDialog(Frame1.this, Messages.getString("Frame1.122")); //$NON-NLS-1$
								return;
							}
							CorpusBrowser corpusBrowser = new CorpusBrowser(userSetting, searchEngineWorker, Frame1.this);
							corpusBrowser.browse(iCorpus, index, lengthSP, iSelectedBrowser, iSelectedXSL);
						}
					}
				});

				try {
					searchEngineWorker.setFilter(newFileter);
					sf.invokeStatisticsFrame(searchEngineWorker.getElementList2(newFieldInfo, order.toArray(new String[0]), contextLength).getResults(), fontsize);
					sf.setTitle(Messages.getString("Frame1.380") + ": " + String.join("/", order)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				} catch (Exception e) {
					JOptionPane.showConfirmDialog(null, e.getMessage(), Messages.getString("Frame1.191"), //$NON-NLS-1$
							JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
			}
		});

		//// external database1
		try {
			FieldInfo dbFieldInfo = FieldInfo.readFieldDiscription(userSetting, "ext_db1"); //$NON-NLS-1$
			extDB1 = new Xdb(
					userSetting.getAttribute("ext_db1", "name"), dbFieldInfo); //$NON-NLS-1$ //$NON-NLS-2$
			extDB1.load(userSetting.getAttribute("ext_db1", "url"), //$NON-NLS-1$ //$NON-NLS-2$
					userSetting.getAttribute("ext_db1", "record_name"), //$NON-NLS-1$ //$NON-NLS-2$
					userSetting.getAttribute("ext_db1", "key")); //$NON-NLS-1$ //$NON-NLS-2$
			jMenuListingElements.remove(jMenuItem_external_database1);
			jMenuListingElements.add(jMenuItem_external_database1);
			jMenuItem_external_database1.setText(userSetting.getAttribute(
					"ext_db1", "name")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (NullPointerException ex) {
			extDB1 = null;
			jMenuListingElements.remove(jMenuItem_external_database1);
		}
		
		//// external database2
		try {
			FieldInfo dbFieldInfo = FieldInfo.readFieldDiscription(userSetting, "ext_db2"); //$NON-NLS-1$
			extDB2 = new Xdb(
					userSetting.getAttribute("ext_db2", "name"), dbFieldInfo); //$NON-NLS-1$ //$NON-NLS-2$
			extDB2.load(userSetting.getAttribute("ext_db2", "url"), //$NON-NLS-1$ //$NON-NLS-2$
					userSetting.getAttribute("ext_db2", "record_name"), //$NON-NLS-1$ //$NON-NLS-2$
					userSetting.getAttribute("ext_db2", "key")); //$NON-NLS-1$ //$NON-NLS-2$
			jMenuListingElements.remove(jMenuItem_external_database2);
			jMenuListingElements.add(jMenuItem_external_database2);
			jMenuItem_external_database2.setText(userSetting.getAttribute(
					"ext_db2", "name")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (NullPointerException ex) {
			extDB2 = null;
			jMenuListingElements.remove(jMenuItem_external_database2);
		}

		
		
		// 閲覧メニュー
		for(Node externalTool : userSetting.evaluateNodes("/setting/external_tools/li", true)){ //$NON-NLS-1$
			String label = ((Element)externalTool).getAttribute("label"); //$NON-NLS-1$
			final String command = ((Element)externalTool).getAttribute("path"); //$NON-NLS-1$
			final String arguments = ((Element)externalTool).getAttribute("argument"); //$NON-NLS-1$
			final String os = ((Element)externalTool).getAttribute("os"); //$NON-NLS-1$

			JMenuItem item = new JMenuItem(label);
			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					accessExternalData(command, arguments, os);
				}
			});
			jMenu_browse.add(item);
		}

		jMenuItem_browse_default.setText(userSetting.getAttribute(
				"browsers", "label")); //$NON-NLS-1$ //$NON-NLS-2$

		// 統計機能用メニュー
		if (userSetting.getAttribute("stat_fields_1", "label") != null) { //$NON-NLS-1$ //$NON-NLS-2$
			jMenuStat.remove(jMenuItem_stat_1);
			jMenuStat.add(jMenuItem_stat_1);
			jMenuItem_stat_1.setText(userSetting.getAttribute(
					"stat_fields_1", "label")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			jMenuStat.remove(jMenuItem_stat_1);
		}
		if (userSetting.getAttribute("stat_fields_2", "label") != null) { //$NON-NLS-1$ //$NON-NLS-2$
			jMenuStat.remove(jMenuItem_stat_2);
			jMenuStat.add(jMenuItem_stat_2);
			jMenuItem_stat_2.setText(userSetting.getAttribute(
					"stat_fields_2", "label")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			jMenuStat.remove(jMenuItem_stat_2);
		}
		if (userSetting.getAttribute("stat_fields_3", "label") != null) { //$NON-NLS-1$ //$NON-NLS-2$
			jMenuStat.remove(jMenuItem_stat_3);
			jMenuStat.add(jMenuItem_stat_3);
			jMenuItem_stat_3.setText(userSetting.getAttribute(
					"stat_fields_3", "label")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			jMenuStat.remove(jMenuItem_stat_3);
		}
		jMenuStat.remove(jMenuItem_stat_user_defined);
		jMenuStat.add(jMenuItem_stat_user_defined);
		jMenuItem_stat_user_defined.setText(Messages.getString("Frame1.67")); //$NON-NLS-1$

		
		// Add items to Help menu
		jMenuHelp.removeAll();
		jMenuHelp.add(getJMenuItem_help_man());
		jMenuHelp.add(getJMenuItem_help_hp());
		String tempURL;
		if((tempURL = userSetting.getAttribute("package_manual", "url")) != null) { //$NON-NLS-1$ //$NON-NLS-2$
			jMenuHelp.add(getJMenuItem_help_package_man(tempURL));
		}
		if((tempURL = userSetting.getAttribute("package_hp", "url")) != null) { //$NON-NLS-1$ //$NON-NLS-2$
			jMenuHelp.add(getJMenuItem_help_package_hp(tempURL));
		}
		jMenuHelp.add(getJMenuItem_help_about());

		
		// length of the previous and following context in KWIC
		jTextField_Length_of_Context.setText(userSetting.getAttribute(
				"length_context_kwic", "value")); //$NON-NLS-1$ //$NON-NLS-2$
		// 検索時の前後文脈長
		jTextField_SearchRange.setText(userSetting.getAttribute(
				"length_context_search", "value")); //$NON-NLS-1$ //$NON-NLS-2$
		// 前後文脈欄の初期値
		String condPrevContext = userSetting.getAttribute(
				"preceding_context_constraint", "value"); //$NON-NLS-1$ //$NON-NLS-2$
		if (condPrevContext != null && condPrevContext.compareTo("") != 0) { //$NON-NLS-1$
			jTextField_KeyPrev.setText(condPrevContext);
			jTextField_KeyPrev.registerKeyHistory();
		}
		String condFolContext = userSetting.getAttribute(
				"following_context_constraint", "value"); //$NON-NLS-1$ //$NON-NLS-2$
		if (condFolContext != null && condFolContext.compareTo("") != 0) { //$NON-NLS-1$
			jTextField_KeyFol.setText(condFolContext);
			jTextField_KeyFol.registerKeyHistory();
		}
		// 検索キーのメニュー変更
		jComboBox_searchTarget.removeAllItems();

		searchTarget = searchEngineWorker.getSearchTarget();
		jComboBox_searchTarget.removeAllItems();
		for (int i = 0; i < searchTarget.length; i++) {
			jComboBox_searchTarget.addItem(searchTarget[i]);
		}

		searchConditionLabelChange();

		// registration of filter menu
		for (int i = 0; i < nFilter; i++) {
			jTextField_filter[i].setText(""); //$NON-NLS-1$
			jComboBox_filterTarget[i].removeAllItems();
			int registeredItems = 0;
			for (int j = 0; j < fieldInfo.getNames().length; j++) {
				String fieldType = fieldInfo.getType(j);
				// フィルタに登録する項目を属性値，兄弟要素，隣接要素の検索結果だけに限定
				if (fieldType.compareTo("argument") == 0 || //$NON-NLS-1$
						fieldType.compareTo("sibling") == 0 || //$NON-NLS-1$
						fieldType.compareTo("relative") == 0 || //$NON-NLS-1$
						fieldType.compareTo("db") == 0|| //$NON-NLS-1$
						fieldType.compareTo("dic") == 0) { //$NON-NLS-1$
					registeredItems++;
					jComboBox_filterTarget[i].addItem(fieldInfo.getNames()[j]);
				}
			}
			for (int j = 0; j < fieldInfo.getNames().length; j++) {
				String fieldType = fieldInfo.getType(j);
				// 前後文脈とキーを末尾に追加
				if (fieldType.compareTo("preceding_context") == 0 || //$NON-NLS-1$
						fieldType.compareTo("following_context") == 0){ //$NON-NLS-1$
					registeredItems++;
					jComboBox_filterTarget[i].addItem(fieldInfo.getNames()[j]);
				}
			}
				
			if(registeredItems == 0){
				continue;
			} else if (i >= registeredItems) {
				// nFilter よりも filterName の数が大きい場合
				jComboBox_filterTarget[i].setSelectedIndex(0);
			} else {
				jComboBox_filterTarget[i].setSelectedIndex(i);
			}
		}


		DefaultListModel<CorpusFile> listTargetCorpora = new DefaultListModel<CorpusFile>();
		DefaultListModel<CorpusFile> listNonTargetCorpora = new DefaultListModel<CorpusFile>();

		// jList_targetCorpus.setListData(searchEngine.getCorpus());
		Arrays.sort(corpora, new ObjectCmp());
		for (int i = 0; i < corpora.length; i++) {
			if (corpora[i].isSelected()) {
				listTargetCorpora.addElement(corpora[i]);
			} else {
				listNonTargetCorpora.addElement(corpora[i]);
			}
		}
		jList_targetCorpus.setModel(listTargetCorpora);
		jList_nonTargetCorpus.setModel(listNonTargetCorpora);

		// セル値表示欄の初期化
		jTextField_SelectedValue.setText(""); //$NON-NLS-1$

		// ブラウザ選択の初期化
		iSelectedBrowser = 0;
		iSelectedXSL = 0;
		
		// initialize a xslt directory in a temporary directory
		CorpusBrowser.init();

		// annotator
		annotatorNames = userSetting.evaluateAtributeList("/setting/annotator/li", "name", true); //$NON-NLS-1$ //$NON-NLS-2$
		if(annotatorNames != null && annotatorNames.length != 0){
			cAnnotatorName = annotatorNames[0]; 
		}

		// フォントサイズ決定
		fontsize = Integer.parseInt(userSetting.getAttribute(
				"fontsize", "value")); //$NON-NLS-1$ //$NON-NLS-2$
		// GUI をすべて登録後に実施する必要がある

		// 起動時に登録されていない GUI コンポーネント(検索条件のメニューなど)は，
		// 再度サイズを設定する必要がある
		setFontSize(fontsize);
	}

//	public void clean(){
//		searchEngineWorker = null;
//		elementMap = null;
//		jList_targetCorpus = new JList<CorpusFile>();
//		jList_nonTargetCorpus = new JList<CorpusFile>();
//	}
	
	
	public DBController getDBController(){
		return dbController;
	}

	/**
	 * ユーザ設定ファイルから値を読み込む
	 */
	private void setValueFromUserWorkSetting() {

		/**
		 * フォントサイズ：
		 */
		// 作業ファイルに設定されていない場合はconfigを参照
		// configにもない場合はデフォルトを反映
		int tempFontsize = userWorkSetting.getFont_size();
		if (tempFontsize != UserWorkSetting.INT_ERROR) {
			fontsize = tempFontsize;
		} else {
			try {
				fontsize = Integer.parseInt(userSetting.getAttribute(
						"fontsize", "value")); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (NumberFormatException ne) {

				ne.printStackTrace();
				// パース失敗の場合はデフォルト値のまま
			}
		}

		/**
		 * ブラウザ
		 */
		String tempSelectedBrowser = userWorkSetting.getBrowser();
		iSelectedBrowser = 0;
		if (!tempSelectedBrowser.equals(UserWorkSetting.STR_ERROR)) {
			String strArray[] = userSetting.evaluateAtributeList("/setting/browsers/li", "name", true); //$NON-NLS-1$ //$NON-NLS-2$
			for (int i = 0; i < strArray.length; i++) {
				if (strArray[i].equals(tempSelectedBrowser)) {
					iSelectedBrowser = i;
				}
			}
		}

		/**
		 * Xsl
		 */
		String tempSelectedXSL = userWorkSetting.getXsl();
		iSelectedXSL = 0;
		if (!tempSelectedXSL.equals(UserWorkSetting.STR_ERROR)) {
			iSelectedXSL = getISelectedValue(tempSelectedXSL, "xsl_files", //$NON-NLS-1$
					"name"); //$NON-NLS-1$
		}

		/**
		 * 前後文脈長
		 */
		String tempLength_of_Context = userWorkSetting.getLength_of_context();
		if (!tempLength_of_Context.equals(UserWorkSetting.STR_ERROR)) {
			jTextField_Length_of_Context.setText(userWorkSetting
					.getLength_of_context());
		} else {
			jTextField_Length_of_Context.setText(String.valueOf(DEFAULT_KWIC_CONTEXT_LENGTH));
		}

		/**
		 * 検索キー
		 */
		String tempKeyHistory[] = userWorkSetting.getKey_string();
		if (tempKeyHistory != UserWorkSetting.ARRAY_ERROR) {
			jTextField_Key.registerKeyHistory(tempKeyHistory);
		}

		/**
		 * 前文脈
		 */
		String tempKeyPrevHistory[] = userWorkSetting.getKey_prev();
		if (tempKeyPrevHistory != UserWorkSetting.ARRAY_ERROR) {
			jTextField_KeyPrev.registerKeyHistory(tempKeyPrevHistory);
		}

		/**
		 * 後文脈
		 */
		String tempKeyFolHistory[] = userWorkSetting.getKey_fol();
		if (tempKeyFolHistory != UserWorkSetting.ARRAY_ERROR) {
			jTextField_KeyFol.registerKeyHistory(tempKeyFolHistory);
		}

		/**
		 * フィルター上段
		 */
		String tempFilterTop[] = userWorkSetting.getFilter_top();
		if (tempFilterTop != UserWorkSetting.ARRAY_ERROR) {
			jTextField_filter[0].registerKeyHistory(tempFilterTop);
		}

		/**
		 * フィルター中段
		 */
		String tempFilterMiddle[] = userWorkSetting.getFilter_middle();
		if (tempFilterMiddle != UserWorkSetting.ARRAY_ERROR) {
			jTextField_filter[1].registerKeyHistory(tempFilterMiddle);
		}

		/**
		 * フィルター下段
		 */
		String tempFilterBottom[] = userWorkSetting.getFilter_bottom();
		if (tempFilterBottom != UserWorkSetting.ARRAY_ERROR) {
			jTextField_filter[2].registerKeyHistory(tempFilterBottom);
		}

	}

	/**
	 * 指定した文言が対象comboboxの何番目のリストに当たるかを取得する
	 *
	 * @param target
	 *            指定した文言
	 * @param tag_name
	 *            対象となるcomboboxのタグ名
	 * @param attr
	 *            対象となるcomboboxの属性名
	 * @return
	 */
	private int getISelectedValue(String target, String tag_name, String attr) {
		String strArray[] = userSetting.getAttributeList(tag_name, attr);
		for (int i = 0; i < strArray.length; i++) {
			if (strArray[i].equals(target)) {
				return i;
			}
		}
		// 該当するものがない場合0(default)を返す
		return 0;
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {

			GridBagConstraints gbc_jPanel_status = new GridBagConstraints();
			gbc_jPanel_status.fill = GridBagConstraints.HORIZONTAL;
			gbc_jPanel_status.gridx = 1;
			gbc_jPanel_status.gridy = 4;
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.setMinimumSize(new Dimension(850, 600));
			jContentPane.setPreferredSize(new Dimension(850, 600));
			jContentPane.add(getJPanel4(), new GridBagConstraints(1, 0, 1, 1,
					1.0, 1.0, GridBagConstraints.CENTER,
					GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			jContentPane.add(getJPanel_status(),gbc_jPanel_status);
			jContentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		}
		return jContentPane;
	}

	/**
	 * This method initializes jMenuBar1
	 *
	 * @return javax.swing.JMenuBar
	 */
	private JMenuBar getJMenuBar1() {
		if (jMenuBar1 == null) {
			jMenuBar1 = new JMenuBar();
			jMenuBar1.add(getJMenuFile());
			jMenuBar1.add(getJMenuEdit());
			jMenuBar1.add(getJMenuTools());
			jMenuBar1.add(getJMenuHelp());
		}
		return jMenuBar1;
	}

	/**
	 * This method initializes jMenuFile
	 *
	 * @return javax.swing.JMenu
	 */
	private JMenu getJMenuFile() {
		if (jMenuFile == null) {
			jMenuFile = new JMenu();
			jMenuFile.setActionMap(null);
			jMenuFile.setText(Messages.getString("Frame1.13")); //$NON-NLS-1$
			jMenuFile.setActionCommand(Messages.getString("Frame1.12")); //$NON-NLS-1$
			jMenuFile.add(getJMenuSelectCorpus());
			jMenuFile.add(getJMenuItemNew());
			jMenuFile.add(getJMenuSaveAs());
			jMenuFile.add(getJMenuInstall());
			
			jMenuFileImportText = new JMenuItem(Messages.getString("Frame1.541")); //$NON-NLS-1$
			jMenuFileImportText.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
			jMenuFile.add(jMenuFileImportText);
			jMenuFileImportText.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					executeImportText(""); //$NON-NLS-1$
				}
			});
			jMenuFile.add(getJMenuFileExit());
		}
		return jMenuFile;
	}

	/**
	 * This method initializes jMenuItemNew
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemNew() {
		if (jMenuItemNew == null) {
			jMenuItemNew = new JMenuItem();
			jMenuItemNew.setText(Messages.getString("Frame1.21")); //$NON-NLS-1$
			jMenuItemNew.setAccelerator(KeyStroke.getKeyStroke('N',
					KeyEvent.CTRL_DOWN_MASK, false));
			jMenuItemNew.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFileChooser fileChooser = new JFileChooser("."); //$NON-NLS-1$
					HimawariFileFilter hff = new HimawariFileFilter("xml", Messages.getString("Frame1.332")); //$NON-NLS-1$ //$NON-NLS-2$
					fileChooser.addChoosableFileFilter(hff);
					fileChooser.setFileFilter(hff);
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

					if (fileChooser.showDialog(Frame1.this, Messages.getString("Frame1.333")) == JFileChooser.APPROVE_OPTION) { //$NON-NLS-1$
						updateUserWorkSetting();
						openConfigFile(fileChooser.getSelectedFile().getAbsolutePath());
						readUserSetting2();
					}
				}
			});
		}
		return jMenuItemNew;
	}

	
	private void openConfigFile(String filename){
		setConfigFileName(filename);

		try {
			myInit();
		} catch (Exception ex) {
			Util.showErrorMessages(this, ex);
			ex.printStackTrace();
		}
	}
	

	private JMenuItem getJMenuSelectCorpus() {
		if (jMenuSelectCorpus == null) {
			jMenuSelectCorpus = new JMenuItem();
			jMenuSelectCorpus.setText(Messages.getString("Frame1.56")); //$NON-NLS-1$
			jMenuSelectCorpus.setAccelerator(KeyStroke.getKeyStroke('O',
					KeyEvent.CTRL_DOWN_MASK, false));
			jMenuSelectCorpus.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					CorpusChooserPane op = new CorpusChooserPane(new File("./"), new File(configFileName).getName(), new Runnable() {  //$NON-NLS-1$
						// Executed if a loaded package is going to be removed 
						@Override
						public void run() {
							// this thread is an EDT
							openConfigFile(UserSettings.DEFAULT_CONFIG_FILE);
							System.gc();
						}
					});
					JDialog dialog = op.createDialog(Frame1.this, Messages.getString("Frame1.84")); //$NON-NLS-1$
					dialog.setVisible(true);

					// ok or cancel?
					Object selectedValue = op.getValue();
					if(selectedValue == null || (Integer)selectedValue != JOptionPane.OK_OPTION){
						System.err.println("cancel"); //$NON-NLS-1$
						return;
					}
					
					// get and open config filename
					String selectedConfigFilename = op.getSelectedConfigFilename();
					if(selectedConfigFilename != null && !selectedConfigFilename.isEmpty()){
						updateUserWorkSetting();
						openConfigFile(selectedConfigFilename);
						readUserSetting2();
					}
				}
			});
		}
		return jMenuSelectCorpus;
	}

	
	/**
	 * This method initializes jMenuSaveAs
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuSaveAs() {
		if (jMenuSaveAs == null) {
			jMenuSaveAs = new JMenuItem();
			jMenuSaveAs.setText(Messages.getString("Frame1.19")); //$NON-NLS-1$
			jMenuSaveAs.setAccelerator(KeyStroke.getKeyStroke('S',
					KeyEvent.CTRL_DOWN_MASK, false));
			jMenuSaveAs.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					jMenuSaveAs_actionPerformed(e);
				}
			});
		}
		return jMenuSaveAs;
	}

	/**
	 * [ファイル|名前を付けて保存]
	 *
	 * @param e
	 *            ActionEvent
	 */
	void jMenuSaveAs_actionPerformed(ActionEvent e) {
		BufferedWriter bwResultTxt;
		ResultRecord resultRecord;

		// get an output filename
		JFileChooser fileChooser = new JFileChooser(); //$NON-NLS-1$
		HimawariFileFilter hff = new HimawariFileFilter("txt", Messages.getString("Frame1.297")); //$NON-NLS-1$  //$NON-NLS-2$
		fileChooser.addChoosableFileFilter(hff);
		fileChooser.setFileFilter(hff);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		if (fileChooser.showDialog(this, Messages.getString("Frame1.298")) != JFileChooser.APPROVE_OPTION) { //$NON-NLS-1$
			this.statusBar.setText(Messages.getString("Frame1.299")); //$NON-NLS-1$
			return;
		}

		selectedTableModel = (ResultTableModel) selectedTable.getModel();

		ArrayList<ResultRecord> filteredData = selectedTableModel.getFilteredData();
		// get the first index field number;
		int iNo = -1;
		int serialNo = 1;
		for(int i = 0; i < selectedTableModel.getFieldInfo().size(); i++){
			if(selectedTableModel.getFieldInfo().getType(i).equals(FieldInfo.TYPE_INDEX)){
				iNo = i;
				break;
			}
		}
		
		try {
			String filenamBody = fileChooser.getSelectedFile().getName();
			// add .txt to the filename if some filter is not adopted and the file extension is not .txt
			if (!filenamBody.endsWith(".txt") && !fileChooser.accept(new File("_dummy"))) { //$NON-NLS-1$ //$NON-NLS-2$
				filenamBody = filenamBody + ".txt"; //$NON-NLS-1$
			}
			String filename = fileChooser.getCurrentDirectory()
					.getAbsolutePath() + "/" + filenamBody; //$NON-NLS-1$
			File outputFile = new File(fileChooser.getCurrentDirectory()
					.getAbsolutePath() + "/" + filenamBody); //$NON-NLS-1$
			if (outputFile.exists()) {
				Object[] options = {
						Messages.getString("Frame1.305"), Messages.getString("Frame1.306") }; //$NON-NLS-1$ //$NON-NLS-2$
				int n = JOptionPane
						.showOptionDialog(
								this,
								Messages.getString("Frame1.307"), //$NON-NLS-1$
								Messages.getString("Frame1.308"), //$NON-NLS-1$
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, options,
								options[1]);
				if (n != 0) {
					JOptionPane.showConfirmDialog(
							this,
							Messages.getString("Frame1.309"), //$NON-NLS-1$
							Messages.getString("Frame1.310"), //$NON-NLS-1$
							JOptionPane.DEFAULT_OPTION,
							JOptionPane.WARNING_MESSAGE);
				}
			}

			bwResultTxt = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename), "utf-8")); //$NON-NLS-1$

			// write the header
			for(int i = 0; i < fieldInfo.size(); i++){
				bwResultTxt.write(fieldInfo.getName(i));
				if(i != fieldInfo.size() - 1){
					bwResultTxt.write("\t");  //$NON-NLS-1$
				} else {
					bwResultTxt.write("\n");  //$NON-NLS-1$
				}
			}			

			// write filtered data
			for (int i = 0; i < selectedTableModel.filteredDataSize(); i++) {
				resultRecord = (ResultRecord) filteredData.get(i);
				// write a line
				for (int j = 0; j < resultRecord.length(); j++) {
					if(j == iNo){
						bwResultTxt.write(String.valueOf(serialNo++));
					} else if (resultRecord.get(j) != null) {
						// replace \n with ' '
						bwResultTxt.write(resultRecord.get(j).toString().replace('\n',  ' '));
					}
					
					if(j != resultRecord.length() - 1){
						bwResultTxt.write("\t"); //$NON-NLS-1$
						
					} else {
						bwResultTxt.write("\n"); //$NON-NLS-1$
					}
				}
			}
			bwResultTxt.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	
	private JMenuItem getJMenuInstall() {
		if (jMenuInstall == null) {
			jMenuInstall = new JMenuItem();
			jMenuInstall.setText(Messages.getString("Frame1.127")); //$NON-NLS-1$
			jMenuInstall.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
					HimawariFileFilter hff = new HimawariFileFilter("zip", Messages.getString("Frame1.4")); //$NON-NLS-1$ //$NON-NLS-2$
					fileChooser.addChoosableFileFilter(hff);
					fileChooser.setFileFilter(hff);

					if (fileChooser.showDialog(Frame1.this, Messages.getString("Frame1.6")) == JFileChooser.APPROVE_OPTION) { //$NON-NLS-1$
						installPackage(fileChooser.getSelectedFile().toPath());
					}
				}
			});
		}
		return jMenuInstall;
	}

	
	private void installPackage(Path packagePath) {
		// Load the default corpus (config.xml) and GC to release the current corpus.xml from memory
		// even if DEFAULT_CONFIG_FILE is loaded
		openConfigFile(UserSettings.DEFAULT_CONFIG_FILE);
		System.gc();
		
		PackageInstaller installer = new PackageInstaller(this);
		String newConfigFileName = installer.install(packagePath);
		
		if(newConfigFileName != null){
			setConfigFileName(newConfigFileName);
			try {
				myInit();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	
	/**
	 * This method initializes jMenuFileExit
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuFileExit() {
		if (jMenuFileExit == null) {
			jMenuFileExit = new JMenuItem();
			jMenuFileExit.setText(Messages.getString("Frame1.14")); //$NON-NLS-1$
			jMenuFileExit.setAccelerator(KeyStroke.getKeyStroke('Q',
					KeyEvent.CTRL_DOWN_MASK, false));
			jMenuFileExit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					jMenuFileExit_actionPerformed(e);
				}
			});
		}
		return jMenuFileExit;
	}

	/**
	 * [ファイル|終了]
	 *
	 * @param e
	 *            ActionEvent
	 */
	public void jMenuFileExit_actionPerformed(ActionEvent e) {
		if (userSetting.getAttribute("browsers", "temp_file") != null) { //$NON-NLS-1$ //$NON-NLS-2$
			// ブラウザ用テンポラリファイル削除
			File tempFile = new File(userSetting.getAttribute(
					"browsers", "temp_file")); //$NON-NLS-1$ //$NON-NLS-2$
			if (tempFile.exists()) {
				tempFile.delete();
			}
		}

		// System.exit(0);
		doExit();
	}

	/**
	 * ユーザ設定ファイルの保存
	 *
	 * @return ユーザ設定ファイル保存結果
	 */
	private int saveUserWorkSetting() {
		updateUserWorkSetting();

		return userWorkSetting.save();
	}

	
	private void updateUserWorkSetting() {

		/**
		 * 値のセット
		 */
		userWorkSetting.setFont_size(fontsize);
		String[] attributes = userSetting.evaluateAtributeList("/setting/browsers/li", "name", true); //$NON-NLS-1$ //$NON-NLS-2$
		if(attributes.length > 0 && attributes.length > iSelectedBrowser){
			userWorkSetting.setBrowser(attributes[iSelectedBrowser]);
		} else {
			userWorkSetting.setBrowser(""); //$NON-NLS-1$
		}
		attributes = userSetting.getAttributeList("xsl_files", "name"); //$NON-NLS-1$ //$NON-NLS-2$
		if(attributes.length > 0 && attributes.length > iSelectedXSL){
			userWorkSetting.setXsl(attributes[iSelectedXSL]);
		} else {
			userWorkSetting.setXsl(""); //$NON-NLS-1$
		}
		userWorkSetting.setSave_setting(saveSetting);
		if(!localeUtil.getNextStartupLanguage().equals("")) { //$NON-NLS-1$
			userWorkSetting.setLanguage(localeUtil.getNextStartupLanguage());
		}
		userWorkSetting.setLength_of_context(jTextField_Length_of_Context
				.getText());
		userWorkSetting.setKey_string(jTextField_Key.getHistory().toArray(new String[0]));
		userWorkSetting.setKey_prev(jTextField_KeyPrev.getHistory().toArray(new String[0]));
		userWorkSetting.setKey_fol(jTextField_KeyFol.getHistory().toArray(new String[0]));
		userWorkSetting.setFilter_top(jTextField_filter[0].getHistory().toArray(new String[0]));
		userWorkSetting.setFilter_middle(jTextField_filter[1].getHistory().toArray(new String[0]));
		userWorkSetting.setFilter_bottom(jTextField_filter[2].getHistory().toArray(new String[0]));
		userWorkSetting.setConfig_file_path(configFileName);
	}


	/**
	 * This method initializes jMenu1
	 *
	 * @return javax.swing.JMenu
	 */
	private JMenu getJMenuEdit() {
		if (jMenuEdit == null) {
			jMenuEdit = new JMenu();
			jMenuEdit.setText(Messages.getString("Frame1.18")); //$NON-NLS-1$
			jMenuEdit.add(getJMenuCopy());
			jMenuEdit.add(getJMenuCopyWithHeader());
			jMenuEdit.add(getJMenuItem_search());
			jMenuEdit.add(getJMenuItemReplace());
			jMenuEdit.add(getJMenuItem_jump());
			jMenuEdit.add(getJMenuItem_selectAll());
			jMenuEdit.add(getJMenuPaste());
			jMenuEdit.add(getJMenuCopyHeadValueAndPaste());
		}
		return jMenuEdit;
	}

	private JMenuItem getJMenuCopyHeadValueAndPaste() {
		if (jMenuCopyHeadValueAndPaste == null) {
			jMenuCopyHeadValueAndPaste = new JMenuItem();
			jMenuCopyHeadValueAndPaste.setText(Messages.getString("Frame1.107")); //$NON-NLS-1$
			jMenuCopyHeadValueAndPaste.setAccelerator(KeyStroke.getKeyStroke('M',
					KeyEvent.CTRL_DOWN_MASK, false));
			jMenuCopyHeadValueAndPaste.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					jMenuCopyHeadValueAndPaste_actionPerformed(e);
				}
			});
		}
		return jMenuCopyHeadValueAndPaste;
	}

	void jMenuCopyHeadValueAndPaste_actionPerformed(ActionEvent e) {
		selectedTable.copyHeadValueAndPaste();
	}

	
	
	/**
	 * This method initializes jMenuCopy
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuCopy() {
		if (jMenuCopy == null) {
			jMenuCopy = new JMenuItem();
			jMenuCopy.setText(Messages.getString("Frame1.20")); //$NON-NLS-1$
			jMenuCopy.setAccelerator(KeyStroke.getKeyStroke('C',
					KeyEvent.CTRL_DOWN_MASK, false));
			jMenuCopy.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					selectedTable.copySelectedDataToClipboard(false);
				}
			});
		}
		return jMenuCopy;
	}

	
	private JMenuItem getJMenuCopyWithHeader() {
		if (jMenuCopyWithHeader == null) {
			jMenuCopyWithHeader = new JMenuItem();
			jMenuCopyWithHeader.setText(Messages.getString("Frame1.2")); //$NON-NLS-1$
			jMenuCopyWithHeader.setAccelerator(KeyStroke.getKeyStroke('C',
					InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, false));
			jMenuCopyWithHeader.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					selectedTable.copySelectedDataToClipboard(true);
				}
			});
		}
		return jMenuCopyWithHeader;
	}


	/**
	 * This method initializes jMenuPaste
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuPaste() {
		if (jMenuPaste == null) {
			jMenuPaste = new JMenuItem();
			jMenuPaste.setActionCommand(Messages.getString("Frame1.22")); //$NON-NLS-1$
			jMenuPaste.setAccelerator(KeyStroke.getKeyStroke('W',
					KeyEvent.CTRL_DOWN_MASK, false));
			jMenuPaste.setText(Messages.getString("Frame1.23")); //$NON-NLS-1$
			jMenuPaste.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					jMenuPaste_actionPerformed(e);
				}
			});
		}
		return jMenuPaste;
	}

	// クリップボードから検索文字列欄へペースト
	void jMenuPaste_actionPerformed(ActionEvent e) {
		Transferable content = this.getToolkit().getSystemClipboard()
				.getContents(this);
		if (content != null) {
			try {
				String strInClipboard = (String) content
						.getTransferData(DataFlavor.stringFlavor);
				selectedJTextField.setText(strInClipboard);
			} catch (Exception ex) {
				System.out.println(Messages.getString("Frame1.347") + //$NON-NLS-1$
						DataFlavor.stringFlavor.getHumanPresentableName());
			}
		}
	}

	/**
	 * This method initializes jMenuItem_search
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem_search() {
		if (jMenuItem_search == null) {
			jMenuItem_search = new JMenuItem();
			jMenuItem_search.setText(Messages.getString("Frame1.74")); //$NON-NLS-1$
			jMenuItem_search.setAccelerator(KeyStroke.getKeyStroke('F',
					KeyEvent.CTRL_DOWN_MASK, false));
			jMenuItem_search
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							jMenuItem_search_actionPerformed(e);
						}
					});
		}
		return jMenuItem_search;
	}

	// 検索
	void jMenuItem_search_actionPerformed(ActionEvent e) {
		int rowIndex = selectedTable.getSelectedRow();
		selectedTable.invokeDialogForFind(rowIndex);
	}

	
	private JMenuItem getJMenuItemReplace() {
		if (jMenuItemReplace == null) {
			jMenuItemReplace = new JMenuItem();
			jMenuItemReplace.setText(Messages.getString("Frame1.50")); //$NON-NLS-1$
			jMenuItemReplace.setAccelerator(KeyStroke.getKeyStroke('R',
					KeyEvent.CTRL_DOWN_MASK, false));
			jMenuItemReplace
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							selectedTable.invokeReplace();
						}
					});
		}
		return jMenuItemReplace;
	}

	
	/**
	 * This method initializes jMenuItem_jump
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem_jump() {
		if (jMenuItem_jump == null) {
			jMenuItem_jump = new JMenuItem();
			jMenuItem_jump.setText(Messages.getString("Frame1.75")); //$NON-NLS-1$
			jMenuItem_jump.setAccelerator(KeyStroke.getKeyStroke('G',
					KeyEvent.CTRL_DOWN_MASK, false));
			jMenuItem_jump
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							jMenuItem_jump_actionPerformed(e);
						}
					});
		}
		return jMenuItem_jump;
	}

	// 指定行にジャンプ
	void jMenuItem_jump_actionPerformed(ActionEvent e) {
		selectedTable.invokeDialogForJumping();
	}

	/**
	 * This method initializes jMenuItem_selectAll
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem_selectAll() {
		if (jMenuItem_selectAll == null) {
			jMenuItem_selectAll = new JMenuItem();
			jMenuItem_selectAll.setText(Messages.getString("Frame1.24")); //$NON-NLS-1$
			jMenuItem_selectAll.setAccelerator(KeyStroke.getKeyStroke('A',
					KeyEvent.CTRL_DOWN_MASK, false));
			jMenuItem_selectAll
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							jMenuItem_selectAll_actionPerformed(e);
						}
					});
		}
		return jMenuItem_selectAll;
	}

	// 検索結果をすべて選択
	void jMenuItem_selectAll_actionPerformed(ActionEvent e) {
		if (selectedTable.getRowCount() == 0) {
			return;
		}
		selectedTable.selectAll();
	}

	/**
	 * This method initializes jMenu2
	 *
	 * @return javax.swing.JMenu
	 */
	private JMenu getJMenuTools() {
		if (jMenuTools == null) {
			jMenuTools = new JMenu();
			jMenuTools.setText(Messages.getString("Frame1.29")); //$NON-NLS-1$
			jMenuTools.add(getJMenuSort());
			jMenuTools.add(getJMenuFilter());
			jMenuTools.add(getJMenuBrowse());
			jMenuTools.add(getJMenuList());
			jMenuTools.add(getJMenuStat());
			jMenuTools.add(getJMenuConstruct());
			jMenuTools.add(getJMenuOption());
		}
		return jMenuTools;
	}



	/**
	 * テキストファイルインポート実行アクション
	 * @param e
	 */
	private void executeImportText(String targetDir){
		try {
			if(userSetting.evaluateOneNode2("/setting/import", false) == null){ //$NON-NLS-1$
				JOptionPane.showMessageDialog(this,
						Messages.getString("Frame1.166")); //$NON-NLS-1$
				return;
			}
		} catch (XPathExpressionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		jDialog_importText = new TextFileImportDialog(this, targetDir);
		jDialog_importText.setModal(true);
		jDialog_importText.setLocationRelativeTo(this);
		jDialog_importText.setVisible(true);
	}


	/**
	 * This method initializes jMenu4
	 *
	 * @return javax.swing.JMenu
	 */
	private JMenu getJMenuSort() {
		if (jMenuSort == null) {
			jMenuSort = new JMenu();
			jMenuSort.setText(Messages.getString("Frame1.57")); //$NON-NLS-1$
			jMenuSort.setDelay(200);
			jMenuSort.add(getJMenuItem_sort_ascending());
			jMenuSort.add(getJMenuItem_sort_descending());
			jMenuSort.add(getJMenuItem_sort_random());
		}
		return jMenuSort;
	}

	/**
	 * This method initializes jMenuItem_sort_ascending
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem_sort_ascending() {
		if (jMenuItem_sort_ascending == null) {
			jMenuItem_sort_ascending = new JMenuItem();
			jMenuItem_sort_ascending.setToolTipText(Messages
					.getString("Frame1.58")); //$NON-NLS-1$
			jMenuItem_sort_ascending.setText(Messages.getString("Frame1.59")); //$NON-NLS-1$
			jMenuItem_sort_ascending
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							jMenuItem_sort_ascending_actionPerformed(e);
						}
					});
		}
		return jMenuItem_sort_ascending;
	}

	// ソート(昇順)
	void jMenuItem_sort_ascending_actionPerformed(ActionEvent e) {
		int columnIndex = selectedTable.getSelectedColumn();
		if (columnIndex != -1) { // 表中のセルを選択中
			selectedTable.sortDataKeepingSelectedPosition(columnIndex); // ソート(昇順)
		} else {
			statusBar.setText(Messages.getString("Frame1.450")); //$NON-NLS-1$
		}
	}

	/**
	 * This method initializes jMenuItem_sort_descending
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem_sort_descending() {
		if (jMenuItem_sort_descending == null) {
			jMenuItem_sort_descending = new JMenuItem();
			jMenuItem_sort_descending.setToolTipText(Messages
					.getString("Frame1.60")); //$NON-NLS-1$
			jMenuItem_sort_descending.setText(Messages.getString("Frame1.61")); //$NON-NLS-1$
			jMenuItem_sort_descending
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							jMenuItem_sort_descending_actionPerformed(e);
						}
					});
		}
		return jMenuItem_sort_descending;
	}

	// ソート(降順)
	void jMenuItem_sort_descending_actionPerformed(ActionEvent e) {
		int columnIndex = selectedTable.getSelectedColumn();
		if (columnIndex != -1) { // 表中のセルを選択中
			selectedTable.sortDataKeepingSelectedPosition(columnIndex, false); // ソート(降順)
		} else {
			statusBar.setText(Messages.getString("Frame1.451")); //$NON-NLS-1$
		}
	}

	/**
	 * This method initializes jMenuItem_sort_random
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem_sort_random() {
		if (jMenuItem_sort_random == null) {
			jMenuItem_sort_random = new JMenuItem();
			jMenuItem_sort_random.setText(Messages.getString("Frame1.62")); //$NON-NLS-1$
			jMenuItem_sort_random
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							jMenuItem_sort_random_actionPerformed(e);
						}
					});
		}
		return jMenuItem_sort_random;
	}

	// ソート(ランダム)
	void jMenuItem_sort_random_actionPerformed(ActionEvent e) {
		((ResultTableModel) selectedTable.getModel()).sortDataRandamly();
	}

	/**
	 * This method initializes jMenu_filter
	 *
	 * @return javax.swing.JMenu
	 */
	private JMenu getJMenuFilter() {
		if (jMenu_filter == null) {
			jMenu_filter = new JMenu();
			jMenu_filter.setText(Messages.getString("Frame1.63")); //$NON-NLS-1$
			jMenu_filter.add(getJMenuItem_filter_execute());
			jMenu_filter.add(getJMenuItem_filter_randam());
			jMenu_filter.add(getJMenuItem_filter_release());
		}
		return jMenu_filter;
	}

	/**
	 * This method initializes jMenuItem_filter_execute
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem_filter_execute() {
		if (jMenuItem_filter_execute == null) {
			jMenuItem_filter_execute = new JMenuItem();
			jMenuItem_filter_execute.setText(Messages.getString("Frame1.64")); //$NON-NLS-1$
			jMenuItem_filter_execute
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							jMenuItem_filter_execute_actionPerformed(e);
						}
					});
		}
		return jMenuItem_filter_execute;
	}

	// 選択中の列の中での絞り込み
	void jMenuItem_filter_execute_actionPerformed(ActionEvent e) {
		int columnIndex = selectedTable.getSelectedColumn();
		if (columnIndex != -1) { // 表中のセルを選択中
			selectedHeader = selectedTable.getTableHeader();
			String headerName = (String) selectedHeader.getColumnModel()
					.getColumn(columnIndex).getHeaderValue();

			// ポップアップメニューが表示される場所を計算する
			// x: 現在選択中の列のヘッダの中間位置
			// y: 現在選択中の列のヘッダの中間位置
			int x = 0;
			for (int i = 0; i < columnIndex; i++) {
				x += selectedHeader.getColumnModel().getColumn(i).getWidth();
			}
			x += selectedHeader.getColumnModel().getColumn(columnIndex)
					.getWidth() / 2;
			selectedTable.invokeDialogForFiltering(headerName, selectedTable,
					x, selectedHeader.getY() - selectedHeader.getHeight() / 2);
		} else {
			statusBar.setText(Messages.getString("Frame1.452")); //$NON-NLS-1$
		}
	}

	/**
	 * This method initializes jMenuItem_filter_randam
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem_filter_randam() {
		if (jMenuItem_filter_randam == null) {
			jMenuItem_filter_randam = new JMenuItem();
			jMenuItem_filter_randam.setText(Messages.getString("Frame1.79")); //$NON-NLS-1$
			jMenuItem_filter_randam.setVisible(false);
			jMenuItem_filter_randam
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							jMenuItem_filter_randam_actionPerformed(e);
						}
					});
		}
		return jMenuItem_filter_randam;
	}

	// 絞込み(ランダム)
	void jMenuItem_filter_randam_actionPerformed(ActionEvent e) {

	}

	/**
	 * This method initializes jMenuItem_filter_release
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem_filter_release() {
		if (jMenuItem_filter_release == null) {
			jMenuItem_filter_release = new JMenuItem();
			jMenuItem_filter_release.setText(Messages.getString("Frame1.65")); //$NON-NLS-1$
			jMenuItem_filter_release
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							jMenuItem_filter_release_actionPerformed(e);
						}
					});
		}
		return jMenuItem_filter_release;
	}

	// 絞込み解除
	void jMenuItem_filter_release_actionPerformed(ActionEvent e) {
		((ResultTableModel) selectedTable.getModel()).initFilter();
	}

	/**
	 * This method initializes jMenu_browse
	 *
	 * @return javax.swing.JMenu
	 */
	private JMenu getJMenuBrowse() {
		if (jMenu_browse == null) {
			jMenu_browse = new JMenu();
		}
		jMenu_browse.removeAll();
		jMenu_browse.setText(Messages.getString("Frame1.76")); //$NON-NLS-1$
		jMenu_browse.add(getJMenuItem_browse_default());
		for(JMenuItem item : getJMenuItem_browse_six()){
			jMenu_browse.add(item);
		}
		
		return jMenu_browse;
	}

	/**
	 * This method initializes jMenuItem_browse_default
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem_browse_default() {
		if (jMenuItem_browse_default == null) {
			jMenuItem_browse_default = new JMenuItem();
			jMenuItem_browse_default.setText(Messages.getString("Frame1.77")); //$NON-NLS-1$
			jMenuItem_browse_default.setAccelerator(KeyStroke.getKeyStroke('B',
					KeyEvent.CTRL_DOWN_MASK, false));
			jMenuItem_browse_default
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							jMenuItem_browse_default_actionPerformed(e);
						}
					});
		}
		return jMenuItem_browse_default;
	}

	// 閲覧(default)
	void jMenuItem_browse_default_actionPerformed(ActionEvent e) {
		int selectedRow = selectedTable.getSelectedRow();
		if (selectedRow != -1) { // 表中のセルを選択中
			ResultRecord selectedRecord = ((ResultTableModel) selectedTable
					.getModel()).getRecordAt(selectedRow);
			int corpusNumber = selectedRecord.getResourceID();
			int index = selectedRecord.getPosition();
			// 検索語
			String target = (String) selectedRecord.get(searchEngineWorker
					.getStoredFieldName());

			CorpusBrowser corpusBrowser = new CorpusBrowser(userSetting, searchEngineWorker, Frame1.this);
			corpusBrowser.browse(corpusNumber, index, Util.strlenSP(target), iSelectedBrowser, iSelectedXSL);
		}
	}

	
	private ArrayList<JMenuItem> getJMenuItem_browse_six() {
		HashSet<String> elementCheck = new HashSet<String>();
		jMenuItem_browse_six_list = new ArrayList<JMenuItem>();
		
		for(String key : dicFarm.keySet()){
			// remove [] (ex. dic[-2])
			final String elementName = key.replaceFirst("(.+)\\[-?\\d+\\]$", "$1"); //$NON-NLS-1$ //$NON-NLS-2$
			JMenuItem jMenuItem_browse_six = new JMenuItem();
			if(!elementCheck.contains(elementName)){
				elementCheck.add(elementName);
				jMenuItem_browse_six = new JMenuItem();
				jMenuItem_browse_six.setText(elementName);
				jMenuItem_browse_six.setAccelerator(KeyStroke.getKeyStroke('L',
						KeyEvent.CTRL_DOWN_MASK, false));
				jMenuItem_browse_six
						.addActionListener(new java.awt.event.ActionListener() {
							public void actionPerformed(ActionEvent e) {
								int selectedRow = selectedTable.getSelectedRow();
								if (selectedRow != -1 && dicFarm.size() > 0) { // 表中のセルを選択中
									ResultRecord selectedRecord = ((ResultTableModel) selectedTable
											.getModel()).getRecordAt(selectedRow);
									int corpusNumber = selectedRecord.getResourceID();
									int index = selectedRecord.getPosition();
									browseSIX(corpusNumber, index, elementName);
								}
							}
						});
				jMenuItem_browse_six_list.add(jMenuItem_browse_six);
			}
		}
		
		return jMenuItem_browse_six_list;
	}

	
	public void browseSIX(int iCorpus, int index, final String elementName) {
		FieldInfo newFieldInfo = FieldInfo.readFieldDiscription(getDic(elementName), true);
		final SearchResultFrame sf = new SearchResultFrame(this);
		resultFrames.add(sf);
		sf.setFieldInfo(newFieldInfo);
		ArrayList<ResultRecord> lexItemList = null;

		try {
			searchEngineWorker.setFilter(createFilter());
			lexItemList  = searchEngineWorker.getSIXElement(iCorpus, elementName, index, newFieldInfo);
			sf.invokeStatisticsFrame(lexItemList, fontsize);
			sf.setTitle(Messages.getString("Frame1.380")); //$NON-NLS-1$
			sf.setSize(sf.getWidth(), 500);
			System.err.println("lx:" + lexItemList.size()); //$NON-NLS-1$
		} catch (Exception e) {
			e.printStackTrace();
			Util.showErrorMessages(this, e);
			return;
		}

		int selectedLine = 0;
		for(ResultRecord r : lexItemList){
			if(r.getPosition() >= index){
				if(r.getPosition() > index && selectedLine >0){
					selectedLine--;
				}
				sf.getTable().changeSelection(selectedLine, 0, false, false);
				break;
			}
			selectedLine++;
		}

		
		sf.getTable().addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
					int selectedRow = sf.getTable().getSelectedRow();
					ResultRecord selectedRecord = ((ResultTableModel) sf.getTable()
							.getModel()).getRecordAt(selectedRow);
					int corpusNumber = selectedRecord.getResourceID();
					int index = selectedRecord.getPosition();
					if(index == -1){
						return;
					}
					
					String targetText = (String) selectedRecord.get(elementName + "\t" + DBController.FIELD_ANNOTATION_SEARCH_KEY); //$NON-NLS-1$
					int textLen = 1;
					if(targetText != null && !targetText.isEmpty()){
						textLen = Util.strlenSP(targetText);
					}
					CorpusBrowser corpusBrowser = new CorpusBrowser(userSetting, searchEngineWorker, Frame1.this);
					corpusBrowser.browse(corpusNumber, index, textLen, iSelectedBrowser, iSelectedXSL);
				}
			}
		});
	}
	
	
	public SearchResultFrame getSearchResultFrame() {
		final SearchResultFrame sf = new SearchResultFrame(this);
		resultFrames.add(sf);
		sf.getTable().addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
					int selectedRow = sf.getTable().getSelectedRow();
					ResultRecord selectedRecord = ((ResultTableModel) sf.getTable()
							.getModel()).getRecordAt(selectedRow);
					int corpusNumber = selectedRecord.getResourceID();
					int index = selectedRecord.getPosition();
					if(index == -1){
						return;
					}
					
					CorpusBrowser corpusBrowser = new CorpusBrowser(userSetting, searchEngineWorker, Frame1.this);
					corpusBrowser.browse(corpusNumber, index, 1, iSelectedBrowser, iSelectedXSL);
				}
			}
		});
		
		return sf;
	}

	
	// データアクセスコマンドの実行
	void accessExternalData(String command, String argument, String os) {
		final String soundPlayerCommandLabel = "[[soundplayer]]"; //$NON-NLS-1$
		final String xdb1CommandLabel = "[[xdb1]]"; // Xdb //$NON-NLS-1$
		final String xdb2CommandLabel = "[[xdb2]]"; // Xdb //$NON-NLS-1$
		final String browserCommandLabel = "[[browser]]"; //$NON-NLS-1$
		
		argument = argument.replaceAll("\n", ""); //$NON-NLS-1$ //$NON-NLS-2$

		int selectedRow = selectedTable.getSelectedRow();
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(this,
					Messages.getString("Frame1.283"), //$NON-NLS-1$
					Messages.getString("Frame1.284"), //$NON-NLS-1$
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		ResultRecord selectedRecord = ((ResultTableModel) selectedTable
				.getModel()).getRecordAt(selectedRow);

		// キー中の文字クラスを展開
		Pattern p = Pattern.compile("\\(\\(([^\\)]+)\\)\\)"); //$NON-NLS-1$
		Matcher m = p.matcher(argument);

		String modifiedArgument = argument; //$NON-NLS-1$
		while (m.find()) {
			String fieldname = m.group(1);
			String key = (String) selectedRecord.get(fieldname);
			modifiedArgument = m.replaceFirst(key);
			m = p.matcher(modifiedArgument);
		}

		if (command.equals(soundPlayerCommandLabel)) { // SoundPlayer.class
			String args[] = modifiedArgument.split(" "); //$NON-NLS-1$
			String soundFileName = args[0];
			float startTime = Float.parseFloat(args[1]);
			float endTime = Float.parseFloat(args[2]);

			try {
				SoundPlayerFrame spf = new SoundPlayerFrame(soundFileName,
						startTime, endTime);
				spf.setVisible(true);
				spf.start();
			} catch (Exception ex1) {
				JOptionPane.showMessageDialog(this,
						Messages.getString("Frame1.288"), //$NON-NLS-1$
						Messages.getString("Frame1.289"), //$NON-NLS-1$
						JOptionPane.ERROR_MESSAGE);
				return;
			}
		} else if (command.equals(xdb1CommandLabel) || command.equals(xdb2CommandLabel)) {
			Xdb extDB = command.equals(xdb1CommandLabel) ? extDB1 : extDB2;
			String args[] = modifiedArgument.split(" "); //$NON-NLS-1$
			String key = args[0];
			String recordSummary = extDB.getRecordSummary(modifiedArgument);

			if (args.length > 1) {
				recordSummary = extDB.getRecordSummary(key, args[1]);
			} else {
				recordSummary = extDB.getRecordSummary(key);
			}

			if (recordSummary != null) {
				Util.showJTextPaneMessageDialog(this, recordSummary, extDB.getName());
			} else {
				JOptionPane.showMessageDialog(this,
						modifiedArgument + Messages.getString("Frame1.291"), //$NON-NLS-1$
						extDB.getName(), JOptionPane.ERROR_MESSAGE);
			}
		} else if (command.equals(browserCommandLabel)) { // Xdb.class
			CorpusBrowser corpusBrowser = new CorpusBrowser(userSetting, searchEngineWorker, Frame1.this);
			try {
				corpusBrowser.execute(modifiedArgument, iSelectedBrowser);
			} catch (IOException e) {
				corpusBrowser.showBrowserError(iSelectedBrowser);
			}
		} else if (externalApplicationPool.contains(command, os)) {
			try {
				externalApplicationPool.execute(command, os, modifiedArgument.split("_/_")); //$NON-NLS-1$
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, Messages.getString("Frame1.116") + e);  //$NON-NLS-1$
			}
		} else {
			JOptionPane.showMessageDialog(this, Messages.getString("Frame1.124") + command);  //$NON-NLS-1$
		}
	}

	/**
	 * This method initializes jMenuList
	 *
	 * @return javax.swing.JMenu
	 */
	private JMenu getJMenuList() {
		if (jMenuListingElements == null) {
			jMenuListingElements = new JMenu();
			jMenuListingElements.setText(Messages.getString("Frame1.66")); //$NON-NLS-1$
			jMenuListingElements.add(getJMenuItem_listing_corpus());
			jMenuListingElements.add(getJMenuItem_listing_browsed());
			jMenuListingElements.add(getJMenuListingItemUserInput());
			jMenuListingElements.add(getJMenuUserDefinedElement());
			jMenuListingElements.add(getJMenuItem_external_database1());
			jMenuListingElements.add(getJMenuItem_external_database2());
		}
		return jMenuListingElements;
	}

	/**
	 * This method initializes jMenuItem_listing_corpus
	 * [ツール]-->[一覧]-->[コーパス]
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem_listing_corpus() {
		if (jMenuItem_listing_corpus == null) {
			jMenuItem_listing_corpus = new JMenuItem();
			jMenuItem_listing_corpus.setText(Messages.getString("Frame1.68")); //$NON-NLS-1$
			jMenuItem_listing_corpus.setAccelerator(KeyStroke.getKeyStroke('C',
					KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK, false));
			jMenuItem_listing_corpus
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							SearchResultFrame resultFrame = new SearchResultFrame(Frame1.this);
							resultFrames.add(resultFrame);
							FieldInfo fieldInfo = FieldInfo.readFieldDiscription(userSetting, "corpus_fields"); //$NON-NLS-1$

							try {
								searchEngineWorker.setFilter(new Filter()); // use a empty filter
								resultFrame.setFieldInfo(fieldInfo);
								resultFrame.invokeStatisticsFrame(searchEngineWorker.getCorpusList(fieldInfo), fontsize);
								resultFrame.setTitle(Messages.getString("Frame1.380")); //$NON-NLS-1$
							} catch (Exception ex) {
								ex.printStackTrace();
								JOptionPane.showConfirmDialog(Frame1.this, ex.getMessage(), Messages.getString("Frame1.191"), //$NON-NLS-1$
										JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
							}
						}
					});
		}
		return jMenuItem_listing_corpus;
	}


	/**
	 * This method initializes jMenuItem_listing_browsed
	 * [ツール]-->[一覧]-->[閲覧要素]
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem_listing_browsed() {
		if (jMenuItem_listing_browsed == null) {
			jMenuItem_listing_browsed = new JMenuItem();
			jMenuItem_listing_browsed.setText(Messages.getString("Frame1.71")); //$NON-NLS-1$
			jMenuItem_listing_browsed.setAccelerator(KeyStroke.getKeyStroke('B',
					KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK, false));
			jMenuItem_listing_browsed.setActionCommand(Messages
					.getString("Frame1.70")); //$NON-NLS-1$
			jMenuItem_listing_browsed
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							SearchResultFrame resultFrame = new SearchResultFrame(Frame1.this);
							resultFrames.add(resultFrame);
							FieldInfo fieldInfo = FieldInfo.readFieldDiscription(userSetting, "unit_fields"); //$NON-NLS-1$

							final ResultTable resultTable = resultFrame.getTable();
							resultTable.addMouseListener(new java.awt.event.MouseAdapter() {
								public void mouseClicked(MouseEvent e) {
									if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
										int selectedRow = resultTable.getSelectedRow();
										ResultRecord selectedRecord = ((ResultTableModel) resultTable
												.getModel()).getRecordAt(selectedRow);
										int corpusNumber = selectedRecord.getResourceID();
										int index = selectedRecord.getPosition();
										CorpusBrowser corpusBrowser = new CorpusBrowser(userSetting, searchEngineWorker, Frame1.this);
										corpusBrowser.browse(corpusNumber, index, 0, iSelectedBrowser, iSelectedXSL); //$NON-NLS-1$
									}
								}
							});
							
							try {
								searchEngineWorker.setFilter(new Filter()); // use a empty filter
								resultFrame.setFieldInfo(fieldInfo);
								resultFrame.invokeStatisticsFrame(searchEngineWorker.getSelectedElementList(fieldInfo), fontsize);
								resultFrame.setTitle(Messages.getString("Frame1.384")); //$NON-NLS-1$
							} catch (Exception ex) {
								ex.printStackTrace();
								JOptionPane.showConfirmDialog(Frame1.this, ex.getMessage(), Messages.getString("Frame1.191"), //$NON-NLS-1$
										JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
							}
						}
					});
		}
		return jMenuItem_listing_browsed;
	}

	
	private JMenuItem getJMenuListingItemUserInput(){
		if (jMenuListingItemUserInput == null) {
			jMenuListingItemUserInput = new JMenuItem();
			jMenuListingItemUserInput.setText(Messages.getString("Frame1.138")); //$NON-NLS-1$
			jMenuListingItemUserInput.setAccelerator(KeyStroke.getKeyStroke('U',
					KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK, false));
		}
		return jMenuListingItemUserInput;
		
	}
	
	private JMenu getJMenuUserDefinedElement() {
		if (jMenuUserDefinedLists == null) {
			jMenuUserDefinedLists = new JMenu(Messages.getString("Frame1.123")); //$NON-NLS-1$
		}
		return jMenuUserDefinedLists;
	}
	
	
	/**
	 * This method initializes jMenuItem_external_database1
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem_external_database1() {
		if (jMenuItem_external_database1 == null) {
			jMenuItem_external_database1 = new JMenuItem();
			jMenuItem_external_database1.setActionCommand(Messages
					.getString("Frame1.72")); //$NON-NLS-1$
			jMenuItem_external_database1.setText(Messages
					.getString("Frame1.73")); //$NON-NLS-1$
			jMenuItem_external_database1
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							listingExternalData(extDB1);
						}
					});
		}
		return jMenuItem_external_database1;
	}


	/**
	 * This method initializes jMenuItem_external_database2
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem_external_database2() {
		if (jMenuItem_external_database2 == null) {
			jMenuItem_external_database2 = new JMenuItem();
			jMenuItem_external_database2.setDebugGraphicsOptions(0);
			jMenuItem_external_database2.setText(Messages
					.getString("Frame1.87")); //$NON-NLS-1$
			jMenuItem_external_database2.setActionCommand(Messages
					.getString("Frame1.86")); //$NON-NLS-1$
			jMenuItem_external_database2
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							listingExternalData(extDB2);
						}
					});
		}
		return jMenuItem_external_database2;
	}


	/**
	 * 外部データベースの取得
	 *
	 * @param xdb
	 */
	void listingExternalData(Xdb xdb) {
		SearchResultFrame resultFrame = new SearchResultFrame(Frame1.this);
		resultFrames.add(resultFrame);
		resultFrame.setFieldInfo(xdb.getFieldInfo());
		resultFrame.invokeStatisticsFrame(new ArrayList<ResultRecord>(xdb.values()), fontsize);
		resultFrame.setTitle(xdb.getName());
	}

	/**
	 * This method initializes jMenu3
	 *
	 * @return javax.swing.JMenu
	 */
	private JMenu getJMenuStat() {
		if (jMenuStat == null) {
			jMenuStat = new JMenu();
			jMenuStat.setText(Messages.getString("Frame1.53")); //$NON-NLS-1$
			jMenuStat.add(getJMenuItem_stat_1());
			jMenuStat.add(getJMenuItem_stat_2());
			jMenuStat.add(getJMenuItem_stat_3());
			jMenuStat.add(getJMenuItem_stat_user_defined());
		}
		return jMenuStat;
	}

	
	/**
	 * This method initializes jMenuItem_stat_1
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem_stat_1() {
		if (jMenuItem_stat_1 == null) {
			jMenuItem_stat_1 = new JMenuItem();
			jMenuItem_stat_1.setText(Messages.getString("Frame1.54")); //$NON-NLS-1$
			jMenuItem_stat_1
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							String statName = "stat_fields_1"; //$NON-NLS-1$
							String frameTitle = userSetting.getAttribute(statName, "label"); //$NON-NLS-1$
							FieldInfo newFieldInfo = FieldInfo.readFieldDiscription(userSetting, statName);
							SearchResultFrame sf = invokeSelectedStatFrame(selectedTable, newFieldInfo);
							sf.setTitle(frameTitle);
						}
					});
		}
		return jMenuItem_stat_1;
	}

	
	/**
	 * This method initializes jMenuItem_stat_2
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem_stat_2() {
		if (jMenuItem_stat_2 == null) {
			jMenuItem_stat_2 = new JMenuItem();
			jMenuItem_stat_2.setText(Messages.getString("Frame1.55")); //$NON-NLS-1$
			jMenuItem_stat_2
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							String statName = "stat_fields_2"; //$NON-NLS-1$
							String frameTitle = userSetting.getAttribute(statName, "label"); //$NON-NLS-1$
							FieldInfo newFieldInfo = FieldInfo.readFieldDiscription(userSetting, statName);
							SearchResultFrame sf = invokeSelectedStatFrame(selectedTable, newFieldInfo);
							sf.setTitle(frameTitle);
						}
					});
		}
		return jMenuItem_stat_2;
	}


	/**
	 * This method initializes jMenuItem_stat_3
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem_stat_3() {
		if (jMenuItem_stat_3 == null) {
			jMenuItem_stat_3 = new JMenuItem();
			jMenuItem_stat_3.setText(Messages.getString("Frame1.81")); //$NON-NLS-1$
			jMenuItem_stat_3
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							String statName = "stat_fields_3"; //$NON-NLS-1$
							String frameTitle = userSetting.getAttribute(statName, "label"); //$NON-NLS-1$
							FieldInfo newFieldInfo = FieldInfo.readFieldDiscription(userSetting, statName);
							SearchResultFrame sf = invokeSelectedStatFrame(selectedTable, newFieldInfo);
							sf.setTitle(frameTitle);
						}
					});
		}
		return jMenuItem_stat_3;
	}

	
	/**
	 * This method initializes jMenuItem_stat_user_defined
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem_stat_user_defined() {
		if (jMenuItem_stat_user_defined == null) {
			jMenuItem_stat_user_defined = new JMenuItem();
			jMenuItem_stat_user_defined.setText(Messages.getString("Frame1.81")); //$NON-NLS-1$
			jMenuItem_stat_user_defined
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							invokeSelectedStatFrame(selectedTable, selectedTable.getSelectedFieldInfo());
						}
					});
		}
		return jMenuItem_stat_user_defined;
	}

	
	/**
	 * This method initializes jMenu_Option
	 *
	 * @return javax.swing.JMenu
	 */
	private JMenu getJMenuOption() {
		if (jMenu_Option == null) {
			jMenu_Option = new JMenu();
			jMenu_Option.setText(Messages.getString("Frame1.30")); //$NON-NLS-1$
			jMenu_Option.add(getJMenuItem_font_size());
			jMenu_Option.add(getJMenuItem_browser());
//			jMenu_Option.add(getJMenuItem_browsedElement());
			jMenu_Option.add(getJMenuItem_xsl());
			jMenu_Option.add(getJMenuItem_saveSetting());
			jMenu_Option.add(getJMenuItemOptionAnnotation());
			jMenu_Option.add(getJMenuItem_language());
		}
		return jMenu_Option;
	}

	
	/**
	 * This method initializes jMenuItem_font_size
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem_font_size() {
		if (jMenuItem_font_size == null) {
			jMenuItem_font_size = new JMenuItem();
			jMenuItem_font_size.setText(Messages.getString("Frame1.31")); //$NON-NLS-1$
			jMenuItem_font_size.setAccelerator(KeyStroke.getKeyStroke('F',
					KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, false));
			jMenuItem_font_size
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							jMenuItem_FontSize_actionPerformed(e);
						}
					});
		}
		return jMenuItem_font_size;
	}

	/**
	 * [オプション] -> [フォントサイズ]
	 *
	 * @param e
	 *            ActionEvent
	 */
	void jMenuItem_FontSize_actionPerformed(ActionEvent e) {
		Integer selectedValue = (Integer) JOptionPane
				.showInputDialog(
						this,
						Messages.getString("Frame1.399"), Messages.getString("Frame1.400"), //$NON-NLS-1$ //$NON-NLS-2$
						JOptionPane.INFORMATION_MESSAGE, null, availableFontSizes,
						availableFontSizes[fontsize - 7]);
		if (selectedValue != null) {
			fontsize = selectedValue;
			setFontSize(fontsize);
		}
	}

	/**
	 * フォントサイズの変更
	 *
	 * @param fontsize
	 *            フォントサイズ
	 */
	void setFontSize(int fontsize) {
		for(Component c : jTabbedPane_ResultTables.getComponents()){
			ResultTable rTable = ((ResultTable)((JScrollPane)c).getViewport().getView());
			rTable.setFontSize(fontsize);
			rTable.setColumnProperty(fieldInfo);
			rTable.setRowHeight(rTable.getFontMetrics(rTable.getFont()).getHeight());
		}
		jTextField_Key.setFont(new java.awt.Font("Dialog", 0, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
		jTextField_KeyPrev.setFont(new java.awt.Font("Dialog", 0, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
		jTextField_KeyFol.setFont(new java.awt.Font("Dialog", 0, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
		jTextField_SelectedValue.setFont(new java.awt.Font(
				"Dialog", 0, fontsize)); //$NON-NLS-1$
		jTextField_Length_of_Context.setFont(new java.awt.Font(
				"Dialog", 0, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
		jTextField_SearchRange
				.setFont(new java.awt.Font("Dialog", 0, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
		
		jLabel_FolContext.setFont(new java.awt.Font("Dialog", 0, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
		jLabel_PrevContext.setFont(new java.awt.Font("Dialog", 0, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
		jLabel1.setFont(new java.awt.Font("Dialog", 0, fontsize)); //$NON-NLS-1$
		jLabelContextLength.setFont(new java.awt.Font("Dialog", 0, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
		jLabelNumberOfCharacters.setFont(new java.awt.Font("Dialog", 0, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
		jLabelSearchRange.setFont(new java.awt.Font("Dialog", 0, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
		jLabelNumberOfCharacters2.setFont(new java.awt.Font("Dialog", 0, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
		jLabelKeyRange.setFont(new java.awt.Font("Dialog", 0, DEFAULT_FONT_SIZE)); //$NON-NLS-1$

		for (int i = 0; i < nFilter; i++) {
			if (jTextField_filter[i] == null) { // 起動時の対策
				continue;
			}
			jTextField_filter[i].setFont(new java.awt.Font(
					"Dialog", 0, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
			jComboBox_filterCondition[i].setFont(new java.awt.Font(
					"Dialog", 0, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
			// フォントの自動調整
			jComboBox_filterCondition[i].adjustFontAuto();

			jComboBox_filterTarget[i].setFont(new java.awt.Font(
					"Dialog", 0, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
			// フォントの自動調整
			jComboBox_filterTarget[i].adjustFontAuto();
		}
		statusBar.setFont(new Font("Dialog", 0, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
		jLabel_save_db.setFont(new java.awt.Font("Dialog", 0, fontsize)); //$NON-NLS-1$
		jButton_save_db.setFont(new java.awt.Font("Dialog", 0, fontsize)); //$NON-NLS-1$
		jButton_revert_db.setFont(new java.awt.Font("Dialog", 0, fontsize)); //$NON-NLS-1$
		
		// ボタン
		jButton_ClearSearchKey
				.setFont(new java.awt.Font("Dialog", 0, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
		jButton_Search.setFont(new java.awt.Font("Dialog", 0, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
		jButton_Trans2OldChar.setFont(new java.awt.Font("Dialog", 0, DEFAULT_FONT_SIZE)); //$NON-NLS-1$

		// 検索文字列タブの JCombobox
		if (jComboBox_KeyFol != null && jComboBox_KeyPrev != null
				&& jComboBox_searchTarget != null) { // 起動時の対策

			jComboBox_KeyFol.setFont(new java.awt.Font("Dialog", 0, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
			// フォントの自動調整
			jComboBox_KeyFol.adjustFontAuto();

			jComboBox_KeyPrev.setFont(new java.awt.Font("Dialog", 0, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
			// フォントの自動調整
			jComboBox_KeyPrev.adjustFontAuto();

			jComboBox_searchTarget.setFont(new java.awt.Font(
					"Dialog", 0, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
			// フォントの自動調整
			jComboBox_searchTarget.adjustFontAuto();

		}

		// タブ
		jTabbedPane_Search_Keys
				.setFont(new java.awt.Font("Dialog", 0, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
		jTabbedPaneSearchOptions.setFont(new java.awt.Font("Dialog", 0, DEFAULT_FONT_SIZE)); //$NON-NLS-1$

		jCheckBox_Additional.setFont(new java.awt.Font("Dialog", 0, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
		jCheckBox_Equivalent.setFont(new java.awt.Font("Dialog", 0, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
		jCheckBox_Itself.setFont(new java.awt.Font("Dialog", 0, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
		jCheckBox_KeyRangeFol.setFont(new java.awt.Font("Dialog", 0, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
		jCheckBox_KeyRangePrev
				.setFont(new java.awt.Font("Dialog", 0, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
		jCheckBox_Not_Used.setFont(new java.awt.Font("Dialog", 0, DEFAULT_FONT_SIZE)); //$NON-NLS-1$

	}

	/**
	 * This method initializes jMenuItem_browser
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem_browser() {
		if (jMenuItem_browser == null) {
			jMenuItem_browser = new JMenuItem();
			jMenuItem_browser.setText(Messages.getString("Frame1.37")); //$NON-NLS-1$
			jMenuItem_browser.setAccelerator(KeyStroke.getKeyStroke('B',
					KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, false));
			jMenuItem_browser
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							jMenuItem_browser_actionPerformed(e);
						}
					});
		}
		return jMenuItem_browser;
	}

	void jMenuItem_browser_actionPerformed(ActionEvent e) {
		String[] possibleValues = userSetting.evaluateAtributeList("/setting/browsers/li", "name", true); //$NON-NLS-1$ //$NON-NLS-2$
//		String[] possibleValues = userSetting.getAttributeList("browsers", "name"); //$NON-NLS-1$ //$NON-NLS-2$
		String selectedValue = (String) JOptionPane
				.showInputDialog(
						this,
						Messages.getString("Frame1.441"), Messages.getString("Frame1.442"), //$NON-NLS-1$ //$NON-NLS-2$
						JOptionPane.INFORMATION_MESSAGE, null, possibleValues,
						possibleValues[iSelectedBrowser]);

		if (selectedValue != null) {
			for (int i = 0; i < possibleValues.length; i++) {
				if (possibleValues[i].compareTo(selectedValue) == 0) {
					iSelectedBrowser = i;
				}
			}
		}
	}

	/**
	 * This method initializes jMenuItem_xsl
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem_xsl() {
		if (jMenuItem_xsl == null) {
			jMenuItem_xsl = new JMenuItem();
			jMenuItem_xsl.setActionCommand(Messages.getString("Frame1.38")); //$NON-NLS-1$
			jMenuItem_xsl.setAccelerator(KeyStroke.getKeyStroke('S',
					KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, false));
			jMenuItem_xsl.setText(Messages.getString("Frame1.39")); //$NON-NLS-1$
			jMenuItem_xsl
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							jMenuItem_xsl_actionPerformed(e);
						}
					});
		}
		return jMenuItem_xsl;
	}

	void jMenuItem_xsl_actionPerformed(ActionEvent e) {
		String[] possibleValues = userSetting.getAttributeList(
				"xsl_files", "label"); //$NON-NLS-1$ //$NON-NLS-2$
		String selectedValue = (String) JOptionPane
				.showInputDialog(
						this,
						Messages.getString("Frame1.445"), Messages.getString("Frame1.446"), //$NON-NLS-1$ //$NON-NLS-2$
						JOptionPane.INFORMATION_MESSAGE, null, possibleValues,
						possibleValues[iSelectedXSL]);

		if (selectedValue != null) {
			for (int i = 0; i < possibleValues.length; i++) {
				if (possibleValues[i].compareTo(selectedValue) == 0) {
					iSelectedXSL = i;
				}
			}
		}
	}


	private JMenuItem getJMenuItemOptionAnnotation() {
		if (jMenuItemOptionAnnotation == null) {
			jMenuItemOptionAnnotation = new JMenuItem();
			jMenuItemOptionAnnotation.setAccelerator(KeyStroke.getKeyStroke('A',
					KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, false));
			jMenuItemOptionAnnotation.setText(Messages.getString("Frame1.11")); //$NON-NLS-1$
			jMenuItemOptionAnnotation
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if(annotatorNames == null || annotatorNames.length == 0){
								JOptionPane.showMessageDialog(Frame1.this, Messages.getString("Frame1.140")); //$NON-NLS-1$
								return;
							}

							JPanel optionPanel = new JPanel();
							JPanel targetElementPanel = new JPanel();
							targetElementPanel.setLayout(new FlowLayout());
							JLabel jLabelMessage1 = new JLabel(Messages.getString("Frame1.69")); //$NON-NLS-1$
							JLabel jLabelMessage2 = new JLabel(Messages.getString("Frame1.108")); //$NON-NLS-1$
							JComboBox<String> jComboBoxAnnotators = new JComboBox<String>(annotatorNames);
							JComponent vGrue = (JComponent) Box.createRigidArea(new Dimension(10, 10));

							// alinment
							jLabelMessage1.setAlignmentX(Component.LEFT_ALIGNMENT);
							jLabelMessage2.setAlignmentX(Component.LEFT_ALIGNMENT);
							targetElementPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
							jComboBoxAnnotators.setAlignmentX(Component.LEFT_ALIGNMENT);
							vGrue.setAlignmentX(Component.LEFT_ALIGNMENT);
							
							// set current values to UI components
							String annotatorTargetElement = ""; //$NON-NLS-1$
							String annotatorTargetAttribute = ""; //$NON-NLS-1$
							String annotatorTargetAttributeValue = ""; //$NON-NLS-1$
							try {
								List<Node> annotatorNodes = userSetting.evaluateNodes("/setting/annotator/li[@name='" + cAnnotatorName + "']", true); //$NON-NLS-1$ //$NON-NLS-2$
								Element extractElement = (Element) ((Element) annotatorNodes.get(0)).getElementsByTagName("extract").item(0); //$NON-NLS-1$
								annotatorTargetElement = extractElement.getAttribute("element"); //$NON-NLS-1$
								annotatorTargetAttribute = extractElement.getAttribute("attribute"); //$NON-NLS-1$
								annotatorTargetAttributeValue = extractElement.getAttribute("value"); //$NON-NLS-1$
							} catch (Exception e2) {
								e2.printStackTrace();
							}
							for(int i = 0; i < annotatorNames.length; i++){
								if(annotatorNames[i].equals(cAnnotatorName)){
									jComboBoxAnnotators.setSelectedIndex(i);
								}
							}
							
							JTextField jTextFieldTargetElement = new JTextField(annotatorTargetElement, 10);
							JTextField jTextFieldTargetAttribute = new JTextField(annotatorTargetAttribute, 10);
							JTextField jTextFieldTargetAttributeValue = new JTextField(annotatorTargetAttributeValue, 10);
							targetElementPanel.add(jTextFieldTargetElement);
							targetElementPanel.add(jTextFieldTargetAttribute);
							targetElementPanel.add(jTextFieldTargetAttributeValue);
							optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
							optionPanel.add(vGrue);
							optionPanel.add(jLabelMessage1);
							optionPanel.add(jComboBoxAnnotators);
							optionPanel.add(vGrue);
							optionPanel.add(jLabelMessage2);
							optionPanel.add(targetElementPanel);
							
							int optionValue = JOptionPane.showConfirmDialog(Frame1.this, optionPanel, Messages.getString("Frame1.141"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
							if(optionValue == JOptionPane.OK_OPTION){
								try {
									cAnnotatorName = (String)jComboBoxAnnotators.getSelectedItem();
									userSetting.setAttribute("/setting/annotator/li[@name='" + cAnnotatorName + "']", "extract", "element", jTextFieldTargetElement.getText()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
									userSetting.setAttribute("/setting/annotator/li[@name='" + cAnnotatorName + "']", "extract", "attribute", jTextFieldTargetAttribute.getText()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
									userSetting.setAttribute("/setting/annotator/li[@name='" + cAnnotatorName + "']", "extract", "value", jTextFieldTargetAttributeValue.getText()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

								} catch (XPathExpressionException e1) {
									e1.printStackTrace();
								}
							}
						}
					});
		}
		return jMenuItemOptionAnnotation;
	}


	private JMenu getJMenuConstruct() {
		if (jMenuConstruct == null) {
			jMenuConstruct = new JMenu();
			jMenuConstruct.setText(Messages.getString("Frame1.34")); //$NON-NLS-1$
			jMenuConstruct.add(getJMenuGenarateIndex());
			jMenuConstruct.add(getJMenuItemConstructAnalyzeCorpus());
		}
		return jMenuConstruct;
	}

	
	/**
	 * This method initializes jMenuGenarateIndex
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuGenarateIndex() {
		if (jMenuGenarateIndex == null) {
			jMenuGenarateIndex = new JMenuItem();
			jMenuGenarateIndex.setText(Messages.getString("Frame1.36")); //$NON-NLS-1$
			jMenuGenarateIndex
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							jMenuGenarateIndex_actionPerformed();
						}
					});
		}
		return jMenuGenarateIndex;
	}

	/**
	 * [ファイル | インデックスの生成]
	 *
	 * @param e
	 *            ActionEvent
	 */
	private void jMenuGenarateIndex_actionPerformed() {
		genarateIndex(this, true, cAnnotatorName);
	}

	public boolean genarateIndex(Component comp, boolean showConfirm, String annotatorName){
		statusBar.setText(Messages.getString("Frame1.334")); //$NON-NLS-1$
		Object[] options = {
				Messages.getString("Frame1.335"), Messages.getString("Frame1.336") }; //$NON-NLS-1$ //$NON-NLS-2$
		JCheckBox checkOverWrite = new JCheckBox(Messages.getString("Frame1.226")); //$NON-NLS-1$
		int n = 0;
		if(showConfirm){
			JPanel panel = new JPanel();
			BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
			panel.setLayout(layout);
			panel.add(new JLabel(Messages.getString("Frame1.337"))); //$NON-NLS-1$
			checkOverWrite.setSelected(true);
			panel.add(checkOverWrite);
			
			n = JOptionPane.showOptionDialog(
					comp,
					panel,
					Messages.getString("Frame1.338"), //$NON-NLS-1$
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
					options, options[1]);
		}

		
		if (n == 0) {
			searchEngineWorker.init(userSetting);
			if(checkOverWrite.isSelected()){
				CorpusFile corpusFiles[] = searchEngineWorker.getCorpus();
				for(CorpusFile corpusFile: corpusFiles){
					corpusFile.deleteIndexes();
				}
			}
			String messages[] = searchEngineWorker.generateIndex(annotatorName).split(SearchEngineWorker.MESSAGE_DELIMITER); //$NON-NLS-1$

			statusBar.setText(messages[1]);
			if (messages[0].compareTo(Messages.getString("Frame1.340")) == 0) { //$NON-NLS-1$
				JOptionPane.showConfirmDialog(comp,
						messages[2] + "\n" + messages[3], //$NON-NLS-1$
						Messages.getString("Frame1.341"), //$NON-NLS-1$
						JOptionPane.DEFAULT_OPTION,
						JOptionPane.WARNING_MESSAGE);
				return false;
			} else {
				if(showConfirm){
					JOptionPane.showConfirmDialog(comp, messages[2] + "\n" + messages[3], //$NON-NLS-1$
							Messages.getString("Frame1.342"), //$NON-NLS-1$
							JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);
				}
			}
		} else {
			JOptionPane.showConfirmDialog(comp,
					Messages.getString("Frame1.343"), //$NON-NLS-1$
					Messages.getString("Frame1.344"), //$NON-NLS-1$
					JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
			statusBar.setText(Messages.getString("Frame1.345")); //$NON-NLS-1$
			return false;
		}
		
		return true;
	}

	/**
	 * This method initializes jMenuHelp
	 *
	 * @return javax.swing.JMenu
	 */
	private JMenu getJMenuHelp() {
		if (jMenuHelp == null) {
			jMenuHelp = new JMenu();
			jMenuHelp.setActionCommand(Messages.getString("Frame1.15")); //$NON-NLS-1$
			jMenuHelp.setText(Messages.getString("Frame1.16")); //$NON-NLS-1$
		}
		return jMenuHelp;
	}

	/**
	 * This method initializes jMenuItem_help_man
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem_help_man() {
		if (jMenuItem_help_man == null) {
			jMenuItem_help_man = new JMenuItem();
			jMenuItem_help_man
					.setActionCommand(Messages.getString("Frame1.32")); //$NON-NLS-1$
			jMenuItem_help_man.setText(Messages.getString("Frame1.33")); //$NON-NLS-1$
			jMenuItem_help_man
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							jMenuItem_help_man_actionPerformed(e);
						}
					});
		}
		return jMenuItem_help_man;
	}

	/**
	 * helpの表示
	 *
	 * @param e
	 */
	void jMenuItem_help_man_actionPerformed(ActionEvent e) {
		String url = userSetting.getAttribute("manual", "url"); //$NON-NLS-1$ //$NON-NLS-2$
		if (!url.startsWith("https:") && !url.startsWith("http:") && !url.startsWith("file:")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			File currentDir = new File("./"); //$NON-NLS-1$
			url = "file://" + currentDir.getAbsolutePath().replace("\\", "/") + "/" + url; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}

		CorpusBrowser corpusBrowser = new CorpusBrowser(userSetting, searchEngineWorker, Frame1.this);
		try {
			corpusBrowser.execute(url, iSelectedBrowser);
		} catch (Exception ex) {
			corpusBrowser.showBrowserError(iSelectedBrowser);
		}
	}

	/**
	 * This method initializes jMenuItem_help_hp
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem_help_hp() {
		if (jMenuItem_help_hp == null) {
			jMenuItem_help_hp = new JMenuItem();
			jMenuItem_help_hp.setActionCommand(Messages.getString("Frame1.82")); //$NON-NLS-1$
			jMenuItem_help_hp.setText(Messages.getString("Frame1.83")); //$NON-NLS-1$
			jMenuItem_help_hp
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							jMenuItem_help_hp_actionPerformed(e);
						}
					});

		}
		return jMenuItem_help_hp;
	}

	/**
	 * helpをホームページで表示
	 *
	 * @param e
	 */
	void jMenuItem_help_hp_actionPerformed(ActionEvent e) {
		String url = userSetting.getAttribute("hp", "url"); //$NON-NLS-1$ //$NON-NLS-2$

		CorpusBrowser corpusBrowser = new CorpusBrowser(userSetting, searchEngineWorker, Frame1.this);
		try {
			corpusBrowser.execute(url, iSelectedBrowser);
		} catch (IOException ex) {
			corpusBrowser.showBrowserError(iSelectedBrowser);
		}
	}

	
	private JMenuItem getJMenuItem_help_package_man(String url) {
		if (jMenuItem_help_package_man == null) {
			jMenuItem_help_package_man = new JMenuItem();
			jMenuItem_help_package_man.setText(Messages.getString("Frame1.133")); //$NON-NLS-1$
			jMenuItem_help_package_man
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							CorpusBrowser corpusBrowser = new CorpusBrowser(userSetting, searchEngineWorker, Frame1.this);
							try {
								corpusBrowser.execute(url, iSelectedBrowser);
							} catch (IOException ex) {
								corpusBrowser.showBrowserError(iSelectedBrowser);
							}
						}
					});
		}
		return jMenuItem_help_package_man;
	}

	private JMenuItem getJMenuItem_help_package_hp(String url) {
		if (jMenuItem_help_package_hp == null) {
			jMenuItem_help_package_hp = new JMenuItem();
			jMenuItem_help_package_hp.setText(Messages.getString("Frame1.135")); //$NON-NLS-1$
			jMenuItem_help_package_hp
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							String url = userSetting.getAttribute("package_hp", "url"); //$NON-NLS-1$ //$NON-NLS-2$

							CorpusBrowser corpusBrowser = new CorpusBrowser(userSetting, searchEngineWorker, Frame1.this);
							try {
								corpusBrowser.execute(url, iSelectedBrowser);
							} catch (IOException ex) {
								corpusBrowser.showBrowserError(iSelectedBrowser);
							}
						}
					});
		}
		return jMenuItem_help_package_hp;
	}
	
	

	/**
	 * This method initializes jMenuItem_help_about
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem_help_about() {
		if (jMenuItem_help_about == null) {
			jMenuItem_help_about = new JMenuItem();
			jMenuItem_help_about.setText(Messages.getString("Frame1.17")); //$NON-NLS-1$
			jMenuItem_help_about.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					jMenuItem_help_about_actionPerformed(e);
				}
			});
		}
		return jMenuItem_help_about;
	}

	/**
	 * [ヘルプ|バージョン情報]
	 *
	 * @param e
	 *            ActionEvent
	 */
	public void jMenuItem_help_about_actionPerformed(ActionEvent e) {
		Frame1_AboutBox dlg = new Frame1_AboutBox(this);
		Dimension dlgSize = dlg.getPreferredSize();
		Dimension frmSize = getSize();
		Point loc = getLocation();
		dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,
				(frmSize.height - dlgSize.height) / 2 + loc.y);
		dlg.setModal(true);
		dlg.imageLabel
				.setIcon(new ImageIcon(
						Frame1_AboutBox.class
								.getResource("/jp/ac/ninjal/himawari/images/" + picfiles[nHelpCalled % picfiles.length]))); //$NON-NLS-1$
		dlg.setVisible(true);
		nHelpCalled = (int) (Math.random() * picfiles.length);
	}

	/**
	 * This method initializes jPanel4
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel4() {
		if (jPanel4 == null) {
			jPanel4 = new JPanel();
			jPanel4.setLayout(new GridBagLayout());
			jPanel4.add(getJPanel_Search_Conditions(), new GridBagConstraints(
					0, 1, 2, 1, 1.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.NONE, new Insets(0, 2, 2, 2), 0, 0));
			jPanel4.add(getJPanel_ResultTableAndSelectedValue(), new GridBagConstraints(0, 2, 2, 1, 1.0,
					1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(12, 2, 4, 12), 323, -87));
		}
		return jPanel4;
	}

	/**
	 * This method initializes jPanel_Search_Conditions
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel_Search_Conditions() {
		if (jPanel_Search_Conditions == null) {
			BorderLayout borderLayout6 = new BorderLayout();
			borderLayout6.setHgap(20);
			jPanel_Search_Conditions = new JPanel();
			jPanel_Search_Conditions.setLayout(borderLayout6);
			jPanel_Search_Conditions.add(getJPanel_Search_Keys(),
					java.awt.BorderLayout.WEST);
			jPanel_Search_Conditions.add(getJPanel_Buttons(),
					java.awt.BorderLayout.CENTER);
			jPanel_Search_Conditions.add(getJPanel14(),
					java.awt.BorderLayout.EAST);
		}
		return jPanel_Search_Conditions;
	}

	/**
	 * This method initializes jPanel_Search_Keys
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel_Search_Keys() {
		if (jPanel_Search_Keys == null) {
			GridLayout gridLayout1 = new GridLayout();
			gridLayout1.setColumns(3);
			jPanel_Search_Keys = new JPanel();
			jPanel_Search_Keys.setMaximumSize(new Dimension(570, 143));
			jPanel_Search_Keys.setMinimumSize(new Dimension(570, 143));
			jPanel_Search_Keys.setPreferredSize(new Dimension(570, 143));
			jPanel_Search_Keys.setLayout(gridLayout1);
			jPanel_Search_Keys.add(getJTabbedPane_Search_Keys(), null);
		}
		return jPanel_Search_Keys;
	}

	/**
	 * This method initializes jTabbedPane_Search_Keys
	 *
	 * @return javax.swing.JTabbedPane
	 */
	private JTabbedPane getJTabbedPane_Search_Keys() {
		if (jTabbedPane_Search_Keys == null) {
			jTabbedPane_Search_Keys = new JTabbedPane();
			jTabbedPane_Search_Keys.setMaximumSize(new Dimension(550, 144));
			jTabbedPane_Search_Keys.setPreferredSize(new Dimension(550, 144));
			jTabbedPane_Search_Keys.setMinimumSize(new Dimension(550, 144));

			jTabbedPane_Search_Keys.addTab(Messages.getString("Frame1.99"), //$NON-NLS-1$
					null, getJPanel_Target_Words(), null);
			jTabbedPane_Search_Keys.addTab(Messages.getString("Frame1.100"), //$NON-NLS-1$
					null, getJPanel_Biblio(), null);
			jTabbedPane_Search_Keys.addTab(Messages.getString("Frame1.104"), //$NON-NLS-1$
					null, getJPanel24(), null);
			jTabbedPane_Search_Keys.addTab(Messages.getString("Frame1.105"), //$NON-NLS-1$
					null, getJPanel1_Option(), null);
			if(System.getProperty("os.name").toLowerCase().startsWith("mac")){ //$NON-NLS-1$ //$NON-NLS-2$
				jTabbedPane_Search_Keys.setBorder(new EmptyBorder(0, 0, -10, 0));
			}
	}
		return jTabbedPane_Search_Keys;
	}

	
	// 前文脈キー用 JTextField を選択
	private void jTextField_KeyFol_mouseClicked(MouseEvent e) {
		selectedJTextField = jTextField_KeyFol;
	}

	void jTextField_Key_keyPressed(KeyEvent e) {
		if (e.getKeyChar() == '\n') {
			// 検索ボタンがクリックされた時と同様の処理を実行
			actionSearch.actionPerformed(null);

			// キーの履歴を保存
			/**
			 * jTextField_Key.registerKeyHistory();
			 * jTextField_KeyPrev.registerKeyHistory();
			 * jTextField_KeyFol.registerKeyHistory();
			 *
			 * statusBar.setText(Messages.getString("Frame1.386"));
			 * //$NON-NLS-1$ // SwingUtilities.invokeLater(new ExecuteSearch());
			 **/
			// testrun();
		}
	}

	// キー用 JTextField を選択
	private void jTextField_Key_mouseClicked(MouseEvent e) {
		selectedJTextField = jTextField_Key;
	}

	void jComboBox_searchTarget_itemStateChanged(ItemEvent e) {
		searchConditionLabelChange();
	}

	/**
	 * db項目の検索
	 * 検索条件のラベルを変更する
	 */
	void searchConditionLabelChange() {
		if (jComboBox_searchTarget.getItemCount() == 0) {
			return;
		}

		String[] cixElementType = userSetting.getAttributeList(
				"index_cix", "type"); //$NON-NLS-1$ //$NON-NLS-2$
		String[] aixElementType = userSetting.getAttributeList(
				"index_aix", "type"); //$NON-NLS-1$ //$NON-NLS-2$

		try {

			for (int i = 0; i < searchTarget.length; i++) {
				if (((String) jComboBox_searchTarget.getSelectedItem())
						.compareTo(searchTarget[i]) == 0) {
					if (cixElementType.length <= i) {
						isRegexKey = aixElementType[i - cixElementType.length].equals("dic"); //$NON-NLS-1$
					} else if (cixElementType[i].compareTo("null") == 0) { //$NON-NLS-1$
						isRegexKey = true;
					} else {
						isRegexKey = false;
					}
					if (i < cixElementType.length
							&& cixElementType[i].compareTo("record_based") == 0) { //$NON-NLS-1$
						jComboBox_KeyFol.setSelectedIndex(4); // 正規表現に設定
						jComboBox_KeyPrev.setSelectedIndex(4); // 正規表現に設定
						jComboBox_KeyFol.setEnabled(false);
						jComboBox_KeyPrev.setEnabled(false);
						jLabel_PrevContext.setText(Messages
								.getString("Frame1.495")); //$NON-NLS-1$
						jLabel_FolContext.setText(Messages
								.getString("Frame1.496")); //$NON-NLS-1$
						isRecordBased = true;
						return;
					} else if (i >= cixElementType.length
							&& aixElementType[i - cixElementType.length]
									.compareTo("record_based") == 0) { //$NON-NLS-1$
						jComboBox_KeyFol.setSelectedIndex(4); // 正規表現に設定
						jComboBox_KeyPrev.setSelectedIndex(4); // 正規表現に設定
						jComboBox_KeyFol.setEnabled(false);
						jComboBox_KeyPrev.setEnabled(false);
						jLabel_PrevContext.setText(Messages
								.getString("Frame1.498")); //$NON-NLS-1$
						jLabel_FolContext.setText(Messages
								.getString("Frame1.499")); //$NON-NLS-1$
						isRecordBased = true;
						return;
					} else {
						jComboBox_KeyFol.setSelectedIndex(0); // デフォルト値に設定
						jComboBox_KeyPrev.setSelectedIndex(2); // デフォルト値に設定
						jComboBox_KeyFol.setEnabled(true);
						jComboBox_KeyPrev.setEnabled(true);
						jLabel_PrevContext.setText(Messages
								.getString("Frame1.500")); //$NON-NLS-1$
						jLabel_FolContext.setText(Messages
								.getString("Frame1.501")); //$NON-NLS-1$
						isRecordBased = false;
						return;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	// 後文脈キー用 JTextField を選択
	private void jTextField_KeyPrev_mouseClicked(MouseEvent e) {
		selectedJTextField = jTextField_KeyPrev;
	}

	/**
	 * This method initializes jPanel_Biblio
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel_Biblio() {
		if (jPanel_Biblio == null) {
			GridLayout gridLayout2 = new GridLayout();
			gridLayout2.setRows(3);
			gridLayout2.setHgap(0);
			gridLayout2.setVgap(5);
			gridLayout2.setColumns(1);
			jPanel_Biblio = new JPanel();
			jPanel_Biblio.setMinimumSize(new Dimension(70, 71));
			jPanel_Biblio.setPreferredSize(new Dimension(70, 71));
			jPanel_Biblio
					.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			jPanel_Biblio.setLayout(gridLayout2);

			for (int i = 0; i < nFilter; i++) {
				jTextField_filter[i] = new SearchTextField();
				jTextField_filter[i].setText(""); //$NON-NLS-1$
				jTextField_filter[i].addKeyListener(new java.awt.event.KeyAdapter() {
					public void keyPressed(KeyEvent e) {
						jTextField_Key_keyPressed(e);
					}
				});

				jComboBox_filterTarget[i] = new HimawariJComboBox();
				jComboBox_filterTarget[i]
						.setMaximumSize(new Dimension(170, 20));
				jComboBox_filterCondition[i] = new HimawariJComboBox();
				jTextField_filter[i].setJComboBox(jComboBox_filterCondition[i],
						0);
				jComboBox_filterCondition[i].setMaximumSize(new Dimension(230,
						20));
			}

			jPanel_Biblio.add(getJPanel7(), null);
			jPanel_Biblio.add(getJPanel8(), null);
			jPanel_Biblio.add(getJPanel9(), null);

		}
		return jPanel_Biblio;
	}

	/**
	 * This method initializes jPanel7
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel7() {
		if (jPanel7 == null) {
			BorderLayout borderLayout3 = new BorderLayout();
			borderLayout3.setHgap(5);
			jPanel7 = new JPanel();
			jPanel7.setLayout(borderLayout3);

			jPanel7.add(jComboBox_filterTarget[0], BorderLayout.WEST);
			jPanel7.add(jTextField_filter[0], BorderLayout.CENTER);
			jPanel7.add(jComboBox_filterCondition[0], BorderLayout.EAST);
		}
		return jPanel7;
	}

	/**
	 * This method initializes jPanel8
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel8() {
		if (jPanel8 == null) {
			BorderLayout borderLayout4 = new BorderLayout();
			borderLayout4.setHgap(5);
			jPanel8 = new JPanel();
			jPanel8.setLayout(borderLayout4);

			jPanel8.add(jComboBox_filterTarget[1], BorderLayout.WEST);
			jPanel8.add(jTextField_filter[1], BorderLayout.CENTER);
			jPanel8.add(jComboBox_filterCondition[1], BorderLayout.EAST);
		}
		return jPanel8;
	}

	/**
	 * This method initializes jPanel9
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel9() {
		if (jPanel9 == null) {
			BorderLayout borderLayout5 = new BorderLayout();
			borderLayout5.setHgap(5);
			jPanel9 = new JPanel();
			jPanel9.setLayout(borderLayout5);
			jPanel9.add(jComboBox_filterTarget[2], BorderLayout.WEST);
			jPanel9.add(jTextField_filter[2], BorderLayout.CENTER);
			jPanel9.add(jComboBox_filterCondition[2], BorderLayout.EAST);
		}
		return jPanel9;
	}

	/**
	 * This method initializes jPanel24
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel24() {
		if (jPanel24 == null) {
			GridLayout gridLayout9 = new GridLayout();
			gridLayout9.setColumns(3);
			jPanel24 = new JPanel();
			jPanel24.setLayout(gridLayout9);
			jPanel24.add(getJPanel_TargetSubCorpusList(), null);
			jPanel24.add(getJPanel_SubCorpusSelectorButtons(), null);
			jPanel24.add(getJPanel_NonTargetSubCorpusList(), null);
		}
		return jPanel24;
	}

	/**
	 * This method initializes jPanel_TargetSubCorpusList
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel_TargetSubCorpusList() {
		if (jPanel_TargetSubCorpusList == null) {
			jLabel10 = new JLabel();
			jLabel10.setText(Messages.getString("Frame1.98")); //$NON-NLS-1$
			jPanel_TargetSubCorpusList = new JPanel();
			jPanel_TargetSubCorpusList.setLayout(new BorderLayout());
			jPanel_TargetSubCorpusList.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			jPanel_TargetSubCorpusList.add(jLabel10, java.awt.BorderLayout.NORTH);
			jPanel_TargetSubCorpusList.add(getJScrollPane_TargetSubCorpusList(), java.awt.BorderLayout.CENTER);
		}
		return jPanel_TargetSubCorpusList;
	}

	/**
	 * This method initializes jScrollPane_TargetSubCorpusList
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane_TargetSubCorpusList() {
		if (jScrollPane_TargetSubCorpusList == null) {
			jScrollPane_TargetSubCorpusList = new JScrollPane();
			jScrollPane_TargetSubCorpusList
					.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			jScrollPane_TargetSubCorpusList.setViewportView(getJList_targetCorpus());
		}
		return jScrollPane_TargetSubCorpusList;
	}

	private JList<CorpusFile> getJList_targetCorpus() {
		if (jList_targetCorpus == null) {
			jList_targetCorpus = new JList<CorpusFile>();
			jList_targetCorpus.setBorder(null);
		}
		return jList_targetCorpus;
	}

	/**
	 * This method initializes jPanel_SubCorpusSelectorButtons
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel_SubCorpusSelectorButtons() {
		if (jPanelSubCorpusSelectorButtons == null) {
			GridLayout gridLayout10 = new GridLayout();
			gridLayout10.setRows(2);
			gridLayout10.setHgap(0);
			gridLayout10.setVgap(15);
			gridLayout10.setColumns(1);
			jPanelSubCorpusSelectorButtons = new JPanel();
			jPanelSubCorpusSelectorButtons.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
			jPanelSubCorpusSelectorButtons.setLayout(gridLayout10);
			jPanelSubCorpusSelectorButtons.add(getJButton_addCorpus(), null);
			jPanelSubCorpusSelectorButtons.add(getJButton_deleteCorpus(), null);
		}
		return jPanelSubCorpusSelectorButtons;
	}

	/**
	 * This method initializes jButton_addCorpus
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButton_addCorpus() {
		if (jButton_addCorpus == null) {
			jButton_addCorpus = new JButton();
			jButton_addCorpus
					.setBorder(BorderFactory.createRaisedBevelBorder());
			jButton_addCorpus.setText(Messages.getString("Frame1.94")); //$NON-NLS-1$
			jButton_addCorpus
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							jButton_addCorpus_actionPerformed(e);
						}
					});
		}
		return jButton_addCorpus;
	}

	void jButton_addCorpus_actionPerformed(ActionEvent e) {
		int[] selectedIndices = jList_nonTargetCorpus.getSelectedIndices();
		DefaultListModel<CorpusFile> listModel;
		boolean isInserted = false;

		for (int i = 0; i < selectedIndices.length; i++) {
			CorpusFile selectedCorpus = (CorpusFile) ((DefaultListModel<CorpusFile>) jList_nonTargetCorpus
					.getModel()).remove(selectedIndices[i] - i);
			selectedCorpus.select(true);
			isInserted = false;

			// insert the corpus name into the list so that the list is sorted
			// by name
			listModel = (DefaultListModel<CorpusFile>) jList_targetCorpus.getModel();
			for (int j = 0; j < listModel.size(); j++) {
				if (listModel.getElementAt(j).toString()
						.compareTo(selectedCorpus.toString()) > 0) {
					listModel.insertElementAt(selectedCorpus, j);
					isInserted = true;
					break;
				}
			}
			if (!isInserted) {
				listModel.addElement(selectedCorpus);
			}
		}
	}

	/**
	 * This method initializes jButton_deleteCorpus
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButton_deleteCorpus() {
		if (jButton_deleteCorpus == null) {
			jButton_deleteCorpus = new JButton();
			jButton_deleteCorpus.setSize(new Dimension(108, 33));
			jButton_deleteCorpus.setText(Messages.getString("Frame1.95")); //$NON-NLS-1$
			jButton_deleteCorpus.setBorder(BorderFactory
					.createRaisedBevelBorder());
			jButton_deleteCorpus
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							jButton_deleteCorpus_actionPerformed(e);
						}
					});
		}
		return jButton_deleteCorpus;
	}

	void jButton_deleteCorpus_actionPerformed(ActionEvent e) {
		int[] selectedIndices = jList_targetCorpus.getSelectedIndices();
		DefaultListModel<CorpusFile> listModel;
		boolean isInserted = false;

		for (int i = 0; i < selectedIndices.length; i++) {
			CorpusFile selectedCorpus = (CorpusFile) ((DefaultListModel<CorpusFile>) jList_targetCorpus
					.getModel()).remove(selectedIndices[i] - i);
			selectedCorpus.select(false);
			isInserted = false;

			// insert the corpus name into the list so that the list is sorted
			// by name
			listModel = (DefaultListModel<CorpusFile>) jList_nonTargetCorpus.getModel();
			for (int j = 0; j < listModel.size(); j++) {
				if (listModel.getElementAt(j).toString()
						.compareTo(selectedCorpus.toString()) > 0) {
					listModel.insertElementAt(selectedCorpus, j);
					isInserted = true;
					break;
				}
			}
			if (!isInserted) {
				listModel.addElement(selectedCorpus);
			}
		}
	}

	/**
	 * This method initializes jPanel_NonTargetSubCorpusList
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel_NonTargetSubCorpusList() {
		if (jPanel_NonTargetSubCorpusList == null) {
			jLabel9 = new JLabel();
			jLabel9.setText(Messages.getString("Frame1.97")); //$NON-NLS-1$
			jPanel_NonTargetSubCorpusList = new JPanel();
			jPanel_NonTargetSubCorpusList.setLayout(new BorderLayout());
			jPanel_NonTargetSubCorpusList.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			jPanel_NonTargetSubCorpusList.add(getJScrollPane_NonTargetSubCorpusList(), java.awt.BorderLayout.CENTER);
			jPanel_NonTargetSubCorpusList.add(jLabel9, java.awt.BorderLayout.NORTH);
		}
		return jPanel_NonTargetSubCorpusList;
	}

	/**
	 * This method initializes jScrollPane_NonTargetSubCorpusList
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane_NonTargetSubCorpusList() {
		if (jScrollPane_NonTargetSubCorpusList == null) {
			jScrollPane_NonTargetSubCorpusList = new JScrollPane();
			jScrollPane_NonTargetSubCorpusList
					.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			jScrollPane_NonTargetSubCorpusList.setViewportView(getJList_nonTargetCorpus());
		}
		return jScrollPane_NonTargetSubCorpusList;
	}

	private JList<CorpusFile> getJList_nonTargetCorpus() {

		if (jList_nonTargetCorpus == null) {
			jList_nonTargetCorpus = new JList<CorpusFile>();
			jList_nonTargetCorpus.setEnabled(true);
			jList_nonTargetCorpus.setBorder(null);
			jList_nonTargetCorpus.setDebugGraphicsOptions(0);
			jList_nonTargetCorpus.setVerifyInputWhenFocusTarget(true);
			jList_nonTargetCorpus.setPrototypeCellValue(null);
			jList_nonTargetCorpus
					.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		}

		return jList_nonTargetCorpus;
	}

	/**
	 * This method initializes jPanel1_Option
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel1_Option() {
		if (jPanel1_Option == null) {
			jPanel1_Option = new JPanel();
			jPanel1_Option.setLayout(new GridLayout());
			jPanel1_Option.add(getJTabbedPane1(), null);
		}
		return jPanel1_Option;
	}

	/**
	 * This method initializes jTabbedPane1
	 *
	 * @return javax.swing.JTabbedPane
	 */
	private JTabbedPane getJTabbedPane1() {
		if (jTabbedPaneSearchOptions == null) {
			jTabbedPaneSearchOptions = new JTabbedPane();
			jTabbedPaneSearchOptions.addTab(Messages.getString("Frame1.102"), null, //$NON-NLS-1$
					getJPanel18(), null);
			jTabbedPaneSearchOptions.addTab(Messages.getString("Frame1.103"), null, //$NON-NLS-1$
					getJPanel_extraction_options(), null);
			jTabbedPaneSearchOptions.addTab(Messages.getString("Frame1.101"), null, //$NON-NLS-1$
					getJPanel_Jitai(), null);
			if(System.getProperty("os.name").toLowerCase().startsWith("mac")){ //$NON-NLS-1$ //$NON-NLS-2$
				jTabbedPaneSearchOptions.setBorder(new EmptyBorder(0, 0, -10, 0));
			}
		}
		return jTabbedPaneSearchOptions;
	}

	/**
	 * This method initializes jPanel_Jitai
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel_Jitai() {
		if (jPanel_Jitai == null) {
			GridLayout gridLayout13 = new GridLayout();
			gridLayout13.setRows(3);
			gridLayout13.setColumns(2);
			jPanel_Jitai = new JPanel();
			jPanel_Jitai.setPreferredSize(new Dimension(176, 69));
			jPanel_Jitai.setLayout(gridLayout13);
			jPanel_Jitai.setEnabled(false);
//			jPanel_Jitai.setFont(new Font("Dialog", 0, 12)); //$NON-NLS-1$
			jPanel_Jitai.add(getJCheckBox_Equivalent(), null);
			jPanel_Jitai.add(getJCheckBox_Additional(), null);
			jPanel_Jitai.add(getJCheckBox_Itself(), null);
			jPanel_Jitai.add(getJCheckBox_Not_Used(), null);
			jPanel_Jitai.setVisible(false);
		}
		return jPanel_Jitai;
	}

	/**
	 * This method initializes jCheckBox_Equivalent
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJCheckBox_Equivalent() {
		if (jCheckBox_Equivalent == null) {
			jCheckBox_Equivalent = new JCheckBox();
			jCheckBox_Equivalent.setSelected(true);
			jCheckBox_Equivalent.setText(Messages.getString("Frame1.45")); //$NON-NLS-1$
		}
		return jCheckBox_Equivalent;
	}

	/**
	 * This method initializes jCheckBox_Additional
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJCheckBox_Additional() {
		if (jCheckBox_Additional == null) {
			jCheckBox_Additional = new JCheckBox();
			jCheckBox_Additional.setText(Messages.getString("Frame1.40")); //$NON-NLS-1$
		}
		return jCheckBox_Additional;
	}

	/**
	 * This method initializes jCheckBox_Itself
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJCheckBox_Itself() {
		if (jCheckBox_Itself == null) {
			jCheckBox_Itself = new JCheckBox();
			jCheckBox_Itself.setEnabled(false);
			jCheckBox_Itself.setOpaque(false);
			jCheckBox_Itself.setText(Messages.getString("Frame1.42")); //$NON-NLS-1$
			jCheckBox_Itself.setVisible(false);
		}
		return jCheckBox_Itself;
	}

	/**
	 * This method initializes jCheckBox_Not_Used
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJCheckBox_Not_Used() {
		if (jCheckBox_Not_Used == null) {
			jCheckBox_Not_Used = new JCheckBox();
			jCheckBox_Not_Used.setOpaque(true);
			jCheckBox_Not_Used.setText(Messages.getString("Frame1.43")); //$NON-NLS-1$
			jCheckBox_Not_Used.setVisible(false);
		}
		return jCheckBox_Not_Used;
	}

	/**
	 * This method initializes jPanel18
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel18() {
		if (jPanel18 == null) {
			GridLayout gridLayout14 = new GridLayout();
			gridLayout14.setRows(3);
			gridLayout14.setVgap(2);
			gridLayout14.setColumns(1);
			jPanel18 = new JPanel();
			jPanel18.setLayout(gridLayout14);
			jPanel18.add(getJPanel13(), null);
			jPanel18.add(getJPanel10(), null);
			jPanel18.add(getJPanel11(), null);
		}
		return jPanel18;
	}

	/**
	 * This method initializes jPanel13
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel13() {
		if (jPanel13 == null) {
			BorderLayout borderLayout8 = new BorderLayout();
			borderLayout8.setHgap(50);
			borderLayout8.setVgap(0);
			jPanel13 = new JPanel();
			jPanel13.setLayout(borderLayout8);
			jPanel13.add(getJPanel19(), java.awt.BorderLayout.WEST);
			jPanel13.add(getJPanel21(), java.awt.BorderLayout.CENTER);
		}
		return jPanel13;
	}

	/**
	 * This method initializes jPanel19
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel19() {
		if (jPanel19 == null) {
			jLabelKeyRange = new JLabel();
			jLabelKeyRange.setText(Messages.getString("Frame1.44")); //$NON-NLS-1$
			jPanel19 = new JPanel();
			jPanel19.add(jLabelKeyRange, null);
		}
		return jPanel19;
	}

	/**
	 * This method initializes jPanel21
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel21() {
		if (jPanel21 == null) {
			GridLayout gridLayout15 = new GridLayout();
			gridLayout15.setColumns(2);
			jPanel21 = new JPanel();
			jPanel21.setLayout(gridLayout15);
			jPanel21.add(getJCheckBox_KeyRangePrev(), null);
			jPanel21.add(getJCheckBox_KeyRangeFol(), null);
		}
		return jPanel21;
	}

	/**
	 * This method initializes jCheckBox_KeyRangePrev
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJCheckBox_KeyRangePrev() {
		if (jCheckBox_KeyRangePrev == null) {
			jCheckBox_KeyRangePrev = new JCheckBox();
			jCheckBox_KeyRangePrev.setText(Messages.getString("Frame1.52")); //$NON-NLS-1$
		}
		return jCheckBox_KeyRangePrev;
	}

	/**
	 * This method initializes jCheckBox_KeyRangeFol
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJCheckBox_KeyRangeFol() {
		if (jCheckBox_KeyRangeFol == null) {
			jCheckBox_KeyRangeFol = new JCheckBox();
			jCheckBox_KeyRangeFol.setText(Messages.getString("Frame1.41")); //$NON-NLS-1$
		}
		return jCheckBox_KeyRangeFol;
	}

	/**
	 * This method initializes jPanel10
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel10() {
		if (jPanel10 == null) {
			BorderLayout borderLayout14 = new BorderLayout();
			borderLayout14.setHgap(10);
			jPanel10 = new JPanel();
			jPanel10.setLayout(borderLayout14);
			jPanel10.add(getJPanel22(), java.awt.BorderLayout.WEST);
			jPanel10.add(getJPanel15(), java.awt.BorderLayout.CENTER);
		}
		return jPanel10;
	}

	/**
	 * This method initializes jPanel22
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel22() {
		if (jPanel22 == null) {
			jLabelContextLength = new JLabel();
			jLabelContextLength.setMaximumSize(new Dimension(120, 18));
			jLabelContextLength.setPreferredSize(new Dimension(120, 18));
			jLabelContextLength.setText(Messages.getString("Frame1.51")); //$NON-NLS-1$
			jLabelContextLength.setMinimumSize(new Dimension(120, 18));
			jPanel22 = new JPanel();
			jPanel22.setPreferredSize(new Dimension(125, 28));
			jPanel22.add(jLabelContextLength, null);
		}
		return jPanel22;
	}

	/**
	 * This method initializes jPanel15
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel15() {
		if (jPanel15 == null) {
			jLabelNumberOfCharacters = new JLabel();
			jLabelNumberOfCharacters.setText(Messages.getString("Frame1.49")); //$NON-NLS-1$
			BorderLayout borderLayout15 = new BorderLayout();
			borderLayout15.setHgap(20);
			jPanel15 = new JPanel();
			jPanel15.setLayout(borderLayout15);
			jPanel15.add(jLabelNumberOfCharacters, java.awt.BorderLayout.CENTER);
			jPanel15.add(getJTextField_Length_of_Context(),
					java.awt.BorderLayout.WEST);
		}
		return jPanel15;
	}

	/**
	 * This method initializes jTextField_Length_of_Context
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextField_Length_of_Context() {
		if (jTextField_Length_of_Context == null) {
			jTextField_Length_of_Context = new JTextField();
			jTextField_Length_of_Context.setMaximumSize(new Dimension(80, 22));
			jTextField_Length_of_Context
					.setPreferredSize(new Dimension(80, 22));
			jTextField_Length_of_Context
					.setHorizontalAlignment(SwingConstants.RIGHT);
			jTextField_Length_of_Context.setMinimumSize(new Dimension(80, 22));
		}
		return jTextField_Length_of_Context;
	}

	/**
	 * This method initializes jPanel11
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel11() {
		if (jPanel11 == null) {
			BorderLayout borderLayout9 = new BorderLayout();
			borderLayout9.setHgap(10);
			jPanel11 = new JPanel();
			jPanel11.setLayout(borderLayout9);
			jPanel11.add(getJPanel3(), java.awt.BorderLayout.WEST);
			jPanel11.add(getJPanel5(), java.awt.BorderLayout.CENTER);
		}
		return jPanel11;
	}

	/**
	 * This method initializes jPanel3
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel3() {
		if (jPanel3 == null) {
			jLabelSearchRange = new JLabel();
			jLabelSearchRange.setMinimumSize(new Dimension(120, 18));
			jLabelSearchRange.setPreferredSize(new Dimension(120, 18));
			jLabelSearchRange.setText(Messages.getString("Frame1.48")); //$NON-NLS-1$
			jLabelSearchRange.setMaximumSize(new Dimension(120, 18));
			jPanel3 = new JPanel();
			jPanel3.setPreferredSize(new Dimension(125, 28));
			jPanel3.add(jLabelSearchRange, null);
		}
		return jPanel3;
	}

	/**
	 * This method initializes jPanel5
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel5() {
		if (jPanel5 == null) {
			jLabelNumberOfCharacters2 = new JLabel();
			jLabelNumberOfCharacters2.setMaximumSize(new Dimension(20, 16));
			jLabelNumberOfCharacters2.setPreferredSize(new Dimension(20, 16));
			jLabelNumberOfCharacters2.setText(Messages.getString("Frame1.47")); //$NON-NLS-1$
			jLabelNumberOfCharacters2.setVerticalTextPosition(SwingConstants.CENTER);
			jLabelNumberOfCharacters2.setMinimumSize(new Dimension(20, 16));
			BorderLayout borderLayout12 = new BorderLayout();
			borderLayout12.setHgap(20);
			jPanel5 = new JPanel();
			jPanel5.setLayout(borderLayout12);
			jPanel5.add(getJTextField_SearchRange(), java.awt.BorderLayout.WEST);
			jPanel5.add(jLabelNumberOfCharacters2, java.awt.BorderLayout.CENTER);
		}
		return jPanel5;
	}

	/**
	 * This method initializes jTextField_SearchRange
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextField_SearchRange() {
		if (jTextField_SearchRange == null) {
			jTextField_SearchRange = new JTextField();
			jTextField_SearchRange.setMaximumSize(new Dimension(80, 22));
			jTextField_SearchRange.setPreferredSize(new Dimension(80, 22));
			jTextField_SearchRange.setHorizontalAlignment(SwingConstants.RIGHT);
			jTextField_SearchRange.setMinimumSize(new Dimension(80, 22));
		}
		return jTextField_SearchRange;
	}


	/**
	 * This method initializes jPanel17
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel_extraction_options() {
		if (jPanel_extraction_options == null) {
			GridBagLayout gridBagLayout = new GridBagLayout();
			jPanel_extraction_options = new JPanel();
			jPanel_extraction_options.setLayout(gridBagLayout);
			GridBagConstraints gbc = new GridBagConstraints();

			// column 1: radio button
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.insets = new Insets(0, 5, 0, 0);
			gbc.anchor = GridBagConstraints.WEST;
			gridBagLayout.setConstraints(getJRadioButton_extraction_all(),  gbc);
			gbc.gridx = 0;
			gbc.gridy = 1;
			gridBagLayout.setConstraints(getJRadioButton_extraction_random(),  gbc);
			gbc.gridx = 0;
			gbc.gridy = 2;
			gridBagLayout.setConstraints(getJRadioButton_extraction_countonly(),  gbc);

			jPanel_extraction_options.add(jRadioButton_extraction_all);
			jPanel_extraction_options.add(jRadioButton_extraction_random);
			jPanel_extraction_options.add(jRadioButton_extraction_countonly);

			ButtonGroup buttonGroup1 = new ButtonGroup();
			buttonGroup1.add(jRadioButton_extraction_all);
			buttonGroup1.add(jRadioButton_extraction_random);
			buttonGroup1.add(jRadioButton_extraction_countonly);

			// column 2: label
			JLabel jLabel_extraction_all_option = new JLabel(Messages.getString("Frame1.90")); //$NON-NLS-1$
			JLabel jLabel_extraction_random_option = new JLabel(Messages.getString("Frame1.89")); //$NON-NLS-1$
			JLabel jLabel_extraction_countonly_option = new JLabel(Messages.getString("Frame1.126")); //$NON-NLS-1$
			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.insets = new Insets(0, 40, 0, 0);
			gridBagLayout.setConstraints(jLabel_extraction_all_option,  gbc);
			gbc.gridx = 1;
			gbc.gridy = 1;
			gridBagLayout.setConstraints(jLabel_extraction_random_option,  gbc);
			gbc.gridx = 1;
			gbc.gridy = 2;
			gridBagLayout.setConstraints(jLabel_extraction_countonly_option,  gbc);
			
			jPanel_extraction_options.add(jLabel_extraction_all_option);
			jPanel_extraction_options.add(jLabel_extraction_random_option);
			jPanel_extraction_options.add(jLabel_extraction_countonly_option);
		

			// column 3, 4: options
			gbc.gridx = 2;
			gbc.gridy = 0;
			gbc.gridwidth = 2;
			gbc.insets = new Insets(0, 5, 0, 0);
			gridBagLayout.setConstraints(getJTextField_nExtraction(),  gbc);
			gbc.gridx = 2;
			gbc.gridy = 1;
			gbc.gridwidth = 2;
			gridBagLayout.setConstraints(getJTextField_nSample(),  gbc);
			gbc.gridx = 2;
			gbc.gridy = 2;
			gbc.gridwidth = 1;
			jRadioButton_countonly_option2 = new JRadioButton(Messages.getString("Frame1.128")); //$NON-NLS-1$
			gridBagLayout.setConstraints(jRadioButton_countonly_option2,  gbc);
			gbc.gridx = 3;
			gbc.gridy = 2;
			jRadioButton_countonly_option1 = new JRadioButton(Messages.getString("Frame1.129")); //$NON-NLS-1$
			gridBagLayout.setConstraints(jRadioButton_countonly_option1,  gbc);
			
			jPanel_extraction_options.add(jTextField_nExtraction);
			jPanel_extraction_options.add(jTextField_nSample);
			jPanel_extraction_options.add(jRadioButton_countonly_option1);
			jPanel_extraction_options.add(jRadioButton_countonly_option2);

			jRadioButton_countonly_option2.setSelected(true);
			ButtonGroup buttonGroup2 = new ButtonGroup();
			buttonGroup2.add(jRadioButton_countonly_option1);
			buttonGroup2.add(jRadioButton_countonly_option2);
			

			// column 5: glue
			gbc.gridx = 4;
			gbc.gridy = 0;
			gbc.gridheight = 3;
			gbc.weightx = 1;
			Component glue2 = Box.createGlue();
			gridBagLayout.setConstraints(glue2,  gbc);
			jPanel_extraction_options.add(glue2);
			
		}
		return jPanel_extraction_options;
	}


	/**
	 * This method initializes jRadioButton_extraction_all
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJRadioButton_extraction_all() {
		if (jRadioButton_extraction_all == null) {
			jRadioButton_extraction_all = new JRadioButton();
			jRadioButton_extraction_all
					.setText(Messages.getString("Frame1.93")); //$NON-NLS-1$
			jRadioButton_extraction_all.setSelected(true);
		}
		return jRadioButton_extraction_all;
	}

	/**
	 * This method initializes jRadioButton_extraction_random
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJRadioButton_extraction_random() {
		if (jRadioButton_extraction_random == null) {
			jRadioButton_extraction_random = new JRadioButton();
			jRadioButton_extraction_random.setText(Messages
					.getString("Frame1.91")); //$NON-NLS-1$
		}
		return jRadioButton_extraction_random;
	}

	/**
	 * This method initializes jRadioButton_extraction_countonly
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJRadioButton_extraction_countonly() {
		if (jRadioButton_extraction_countonly == null) {
			jRadioButton_extraction_countonly = new JRadioButton();
			jRadioButton_extraction_countonly.setText(Messages
					.getString("Frame1.92")); //$NON-NLS-1$
		}
		return jRadioButton_extraction_countonly;
	}



	/**
	 * This method initializes jTextField_nExtraction
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextField_nExtraction() {
		if (jTextField_nExtraction == null) {
			jTextField_nExtraction = new JTextField(20);
			jTextField_nExtraction.setHorizontalAlignment(SwingConstants.RIGHT);
			jTextField_nExtraction.setMinimumSize(jTextField_nExtraction.getPreferredSize());
		}
		return jTextField_nExtraction;
	}


	/**
	 * This method initializes jTextField_nSample
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextField_nSample() {
		if (jTextField_nSample == null) {
			jTextField_nSample = new JTextField(20);
			jTextField_nSample.setHorizontalAlignment(SwingConstants.RIGHT);
			jTextField_nSample.setMinimumSize(jTextField_nSample.getPreferredSize());
		}
		return jTextField_nSample;
	}

	
	/**
	 * This method initializes jPanel_Buttons
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel_Buttons() {
		if (jPanel_Buttons == null) {
			GridLayout gridLayout4 = new GridLayout();
			gridLayout4.setRows(3);
			gridLayout4.setHgap(5);
			gridLayout4.setVgap(5);
			gridLayout4.setColumns(1);
			jPanel_Buttons = new JPanel();
			jPanel_Buttons.setMaximumSize(new Dimension(100, 144));
			// jPanel_Buttons.setMinimumSize(new Dimension(180, 144));
			jPanel_Buttons.setPreferredSize(new Dimension(200, 144));
			jPanel_Buttons.setBorder(BorderFactory.createEmptyBorder(50, 0, 0,
					0));
			jPanel_Buttons.setLayout(gridLayout4);
			jPanel_Buttons.add(getJButton_Search(), null);
			jPanel_Buttons.add(getJButton_Trans2OldChar(), null);
			jPanel_Buttons.add(getJButton_ClearSearchKey(), null);
		}
		return jPanel_Buttons;
	}

	/**
	 * This method initializes jButton_Search
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButton_Search() {
		if (jButton_Search == null) {
			jButton_Search = new JButton();
			jButton_Search.setText(Messages.getString("Frame1.35")); //$NON-NLS-1$
			searchEngineWorker = new SearchEngineWorker(this);

			jButton_Search.addActionListener(actionSearch);
		}
		return jButton_Search;
	}

	
	class ActionListenerListingElements implements ActionListener {

		UserSettings userSettings;
		Element node;
		
		public ActionListenerListingElements(UserSettings userSettings, Element node){
			super();
			this.userSettings = userSettings;
			this.node = node;
		}
		
		public void actionPerformed(ActionEvent arg0) {
			FieldInfo listFieldInfo = FieldInfo.readFieldDiscription(userSettings, node);
			final SearchResultFrame sf = new SearchResultFrame(Frame1.this);
			resultFrames.add(sf);
			sf.setFieldInfo(listFieldInfo);

			Filter filter = createFilter();
			Iterator<String> it = filter.keySet().iterator();
			while(it.hasNext()){
				String key = it.next();
				if(!listFieldInfo.containsKey(key)){
					it.remove();
				}
			}
			
			try {
				searchEngineWorker.setFilter(filter);
				sf.invokeStatisticsFrame(searchEngineWorker.getElementList(node.getAttribute("name"), listFieldInfo), fontsize); //$NON-NLS-1$
				sf.setTitle(Messages.getString("Frame1.380")); //$NON-NLS-1$
			} catch (Exception e) {
				JOptionPane.showConfirmDialog(null, e.getMessage(), Messages.getString("Frame1.191"), //$NON-NLS-1$
						JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}

			sf.getTable().addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
						int selectedRow = sf.getTable().getSelectedRow();
						ResultRecord selectedRecord = ((ResultTableModel) sf.getTable()
								.getModel()).getRecordAt(selectedRow);
						int corpusNumber = selectedRecord.getResourceID();
						int index = selectedRecord.getPosition();
						if(index == -1){
							return;
						}
						CorpusBrowser corpusBrowser = new CorpusBrowser(userSetting, searchEngineWorker, Frame1.this);
						corpusBrowser.browse(corpusNumber, index, 1, iSelectedBrowser, iSelectedXSL); // put an anchor tag on the first character
					}
				}
			});
		}
	}

	
	
	class ActionListenerAnnotation implements ActionListener {
		String label;
		String annotation;
		
		public ActionListenerAnnotation(String label, String annotation){
			super();
			this.label = label;
			this.annotation = annotation;
		}
		
		public void actionPerformed(ActionEvent arg0) {
			// 実行の確認
			int n = JOptionPane.showConfirmDialog(null,
					Messages.getString("Frame1.322"), "", //$NON-NLS-1$ //$NON-NLS-2$
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if(n == JOptionPane.CANCEL_OPTION){
				return;
			}

			CorpusFile[] corpusFiles = searchEngineWorker.getCorpus();
			for(int i = 0; i < corpusFiles.length; i++){
				try {
					corpusFiles[i].init();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				try {
					DatabaseElementIndex di = new DatabaseElementIndex(annotation, corpusFiles[i]);
					di.mkdb(label, userSetting);
				} catch (Exception e) {
					String message = e.getMessage();
					if(message == null || message.isEmpty()){
						message = Messages.getString("Frame1.324"); //$NON-NLS-1$
					}
					JPanel messagePanel = new JPanel();
					messagePanel.setPreferredSize(new Dimension(500, 300));
					messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
					JTextArea messageArea = new JTextArea(message);
					JScrollPane scrollPane = new JScrollPane(messageArea);
					scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
					scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
					messagePanel.add(scrollPane);
					
					JOptionPane.showMessageDialog(null,
							messagePanel,
							Messages.getString("Frame1.325"), //$NON-NLS-1$
							JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
					return;
				}
			}
			System.err.println("finished!"); //$NON-NLS-1$
			JOptionPane.showMessageDialog(null,
					Messages.getString("Frame1.327"), //$NON-NLS-1$
					Messages.getString("Frame1.328"), //$NON-NLS-1$
					JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	/**
	 * 検索ボタンのアクションクラス
	 *
	 * @author osada
	 *
	 */
	class ActionSearch extends AbstractAction {

		private static final long serialVersionUID = -1957142151045956309L;

		public ActionSearch() {
			super("search"); //$NON-NLS-1$
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// "検索"ボタンのアクション
			if (searchEngineWorker.getStatus() == SearchEngineWorker.SEARCH_OK) {

				// 検索の実行
				jButton_Search_actionPerformed();

			}

			// "中止"ボタンのアクション(検索が実行中の場合)
			else {

				if (searchEngineWorker != null && !searchEngineWorker.isDone()) {
					// キャンセルの実行
					searchEngineWorker.cancel(true);
				}

				// 検索フラグの変更
				searchEngineWorker.setStatus(SearchEngineWorker.SEARCH_OK);

				// ボタンの文字列を変更
				jButton_Search.setText(Messages.getString("Frame1.35")); //$NON-NLS-1$

				// ステータスバーの文言を変更
				statusBar.setText(Messages.getString("Frame1.511")); //$NON-NLS-1$
			}
		}

	}

	/**
	 * [検索]
	 *
	 */
	void jButton_Search_actionPerformed() {

		//編集内容が保存されているかどうか確認する
		if(isTableEditted()){
			int option = JOptionPane.showConfirmDialog(this,
					Messages.getString("Frame1.610"), Messages.getString("Frame1.3"), //$NON-NLS-1$ //$NON-NLS-2$
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			//yesの場合
			if(option == JOptionPane.YES_OPTION){
				doSaveDb();
			}
			//noの場合
			else if(option == JOptionPane.NO_OPTION){
				resetDB_actionPeformed(true);
			}else{
				return;
			}
		}


		// キーの履歴を保存
		jTextField_Key.registerKeyHistory();
		jTextField_KeyPrev.registerKeyHistory();
		jTextField_KeyFol.registerKeyHistory();

		// 「検索中…」を表示
		statusBar.setText(Messages.getString("Frame1.207")); //$NON-NLS-1$

		// new Thread(new ExecuteSearch()).start();
		try {
			// SwingUtilities.invokeLater(new ExecuteSearch());
			// testrun();
			// testrunに失敗の場合ボタンラベルをもとに戻す
			if (!testrun()) {
				// 検索フラグの変更
				searchEngineWorker.setStatus(SearchEngineWorker.SEARCH_OK);

				// ボタンの文字列を変更
				jButton_Search.setText(Messages.getString("Frame1.35")); //$NON-NLS-1$

				// ボタンを使用可能に戻す
				setEnabledAll(true);
			}

			if (isErrorOccur) {
				resultRecordSetFarm = new ArrayList<ArrayList<ResultRecord>>();
				System.gc();
				JOptionPane.showMessageDialog(null,
						Messages.getString("Frame1.208"), //$NON-NLS-1$
						Messages.getString("Frame1.209"), //$NON-NLS-1$
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (Error er) {
			er.printStackTrace();
			resultRecordSetFarm.clear();
			JOptionPane.showMessageDialog(null,
					Messages.getString("Frame1.208"), //$NON-NLS-1$
					Messages.getString("Frame1.209"), //$NON-NLS-1$
					JOptionPane.ERROR_MESSAGE);
		} catch (Exception e2) {
			e2.printStackTrace();
			JOptionPane.showMessageDialog(null,
					Messages.getString("Frame1.208"), //$NON-NLS-1$
					Messages.getString("Frame1.209"), //$NON-NLS-1$
					JOptionPane.ERROR_MESSAGE);

		}
		jTextField_SelectedValue.setText(""); //$NON-NLS-1$
	}

	/**
	 * 検索終了時のアクション
	 */
	public void doneSearchAction() {

		// ボタンテキストの変更
		jButton_Search.setText(Messages.getString("Frame1.35")); //$NON-NLS-1$

		// disabledになっているコンポーネントをenabledに変更
		setEnabledAll(true);

		// TriAx bug 2011-04-26, removed by masaya yamaguchi
		// // 検索条件ラベルの変更
		// searchConditionLabelChange();

		// 字体辞書がない場合は"字体変換ボタン"をdisabled
		if (userSetting.getAttribute("jitaidic", "url") == null) { // jitaidic //$NON-NLS-1$ //$NON-NLS-2$
			jButton_Trans2OldChar.setEnabled(false);
		}
	}

	/**
	 * 表示している全てのコンポーネントのdisabledを切り替える
	 *
	 * @param b
	 *            setenabledのパラメタ
	 */
	private void setEnabledAll(boolean b) {

		// 検索文字列タブ
		jTabbedPane_Search_Keys.setEnabled(b);
		jComboBox_searchTarget.setEnabled(b);
		jTextField_Key.setEditable(b);
		jTextField_KeyPrev.setEnabled(b);
		jComboBox_KeyPrev.setEnabled(b);
		jTextField_KeyFol.setEnabled(b);
		jComboBox_KeyFol.setEnabled(b);
		if(isRecordBased){
			jComboBox_KeyPrev.setEnabled(false);
			jComboBox_KeyFol.setEnabled(false);
		}
		
		// フィルタタブ
		for (int i = 0; i < 3; i++) {
			jComboBox_filterTarget[i].setEnabled(b);
			jTextField_filter[i].setEnabled(b);
			jComboBox_filterCondition[i].setEnabled(b);
		}

		// コーパスタブ
		jScrollPane_TargetSubCorpusList.setEnabled(b);
		jScrollPane_NonTargetSubCorpusList.setEnabled(b);
		jButton_addCorpus.setEnabled(b);
		jButton_deleteCorpus.setEnabled(b);

		// 検索オプションタブ
		jTabbedPaneSearchOptions.setEnabled(b);
		jCheckBox_KeyRangePrev.setEnabled(b);
		jCheckBox_KeyRangeFol.setEnabled(b);
		jTextField_Length_of_Context.setEnabled(b);
		jTextField_SearchRange.setEnabled(b);

		jRadioButton_extraction_all.setEnabled(b);
		jRadioButton_extraction_random.setEnabled(b);
		jRadioButton_extraction_countonly.setEnabled(b);
		jTextField_nExtraction.setEnabled(b);
		jTextField_nSample.setEnabled(b);
		jRadioButton_countonly_option1.setEnabled(b);
		jRadioButton_countonly_option2.setEnabled(b);
		
		jCheckBox_Equivalent.setEnabled(b);
		jCheckBox_Additional.setEnabled(b);

		// 右のボタンエリア
		jButton_Trans2OldChar.setEnabled(b);
		jButton_ClearSearchKey.setEnabled(b);

		// 結果テーブル
		jTabbedPane_ResultTables.setEnabled(b);
		selectedTable.setEnabled(b);
		jTextField_SelectedValue.setEnabled(b);
		
//		searchConditionLabelChange();
	}

	/**
	 * This method initializes jButton_Trans2OldChar
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButton_Trans2OldChar() {
		if (jButton_Trans2OldChar == null) {
			jButton_Trans2OldChar = new JButton();
			jButton_Trans2OldChar.setText(Messages.getString("Frame1.85")); //$NON-NLS-1$
			jButton_Trans2OldChar
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							jButton_Trans2OldChar_actionPerformed(e);
						}
					});
		}
		return jButton_Trans2OldChar;
	}

	/**
	 * 字体変換
	 *
	 * @param e
	 *            ActionEvent
	 */
	void jButton_Trans2OldChar_actionPerformed(ActionEvent e) {

		// 字体変換オプション用のフラグを設定
		kt.setFlag(jCheckBox_Itself.isSelected(),
				jCheckBox_Equivalent.isSelected(),
				jCheckBox_Additional.isSelected(),
				jCheckBox_Not_Used.isSelected());

		// 変換前の文字列を履歴に追加
		selectedJTextField.registerKeyHistory();
		// 変換実行
		String result = kt.trans(selectedJTextField.getText());
		selectedJTextField.setText(result);
	}

	/**
	 * This method initializes jButton_ClearSearchKey
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButton_ClearSearchKey() {
		if (jButton_ClearSearchKey == null) {
			jButton_ClearSearchKey = new JButton();
			jButton_ClearSearchKey.setText(Messages.getString("Frame1.88")); //$NON-NLS-1$
			jButton_ClearSearchKey
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							jButton_ClearSearchKey_actionPerformed(e);
						}
					});
		}
		return jButton_ClearSearchKey;
	}

	/**
	 * 検索キークリア([クリア])
	 *
	 * @param e
	 *            ActionEvent
	 */
	void jButton_ClearSearchKey_actionPerformed(ActionEvent e) {
		jTextField_Key.setText(""); //$NON-NLS-1$
		jTextField_KeyPrev.setText(""); //$NON-NLS-1$
		jTextField_KeyFol.setText(""); //$NON-NLS-1$
		for (int i = 0; i < nFilter; i++) {
			jTextField_filter[i].setText(""); //$NON-NLS-1$
		}
	}

	/**
	 * This method initializes jPanel14
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel14() {
		if (jPanel14 == null) {
			jLabel1 = new JLabel();
			jPanel14 = new JPanel();
			jPanel14.setLayout(new BorderLayout());
			jPanel14.add(jLabel1, java.awt.BorderLayout.SOUTH);
		}
		return jPanel14;
	}

	/**
	 * This method initializes jPanel2
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel_ResultTableAndSelectedValue() {
		if (jPanel2 == null) {
			jPanel2 = new JPanel();
			jPanel2.setLayout(new BorderLayout());
			jPanel2.setAutoscrolls(true);
			jPanel2.add(getJPanel_SelectedValue(), java.awt.BorderLayout.SOUTH);
			jPanel2.add(getJTabbedPane_ResultTables(), BorderLayout.CENTER);
		}
		return jPanel2;
	}

	/**
	 * This method initializes jPanel1
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel_SelectedValue() {
		if (jPanel1 == null) {
			jPanel1 = new JPanel();
			jPanel1.setLayout(new BoxLayout(jPanel1, BoxLayout.Y_AXIS));
			jPanel1.setBorder(new TitledBorder("")); //$NON-NLS-1$

			jPanel1.add(getJTextField_SelectedValue()); // text
			jPanel1.add(getJComboBox_SelectedValue()); // items (db)
		}
		return jPanel1;
	}

	/**
	 * jcomboボックス設定アクション
	 * @return
	 */
	private Component getJComboBox_SelectedValue() {
		//TODO 値変更時のアクション
		if(jComboBox_SelectedValue == null){
			jComboBox_SelectedValue = new JComboBox<String>();
			jComboBox_SelectedValue.setPreferredSize(new Dimension(200, 30));
			jComboBox_SelectedValue.setVisible(false);
			jComboBox_SelectedValue.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					reflectFromFieldToSelectedCell(jComboBox_SelectedValue.getSelectedItem());
				}

			});
		}
		return jComboBox_SelectedValue;
	}

	/**
	 * 選択データ表示フィールドの種別を変更する
	 * @param isText
	 */
	public void changeSelectedValueInput(boolean isText){
		if(isText){
			jComboBox_SelectedValue.setVisible(false);
			jTextField_SelectedValue.setVisible(true);
		}else{
			jComboBox_SelectedValue.setVisible(true);
			jTextField_SelectedValue.setVisible(false);
		}
	}

	/**
	 * This method initializes jTextField_SelectedValue
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextField_SelectedValue() {
		if (jTextField_SelectedValue == null) {
			jTextField_SelectedValue = new JTextField();

			//text field処理
			jTextField_SelectedValue.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyReleased(KeyEvent e) {
					int col = selectedTable.getSelectedColumn();
					int row = selectedTable.getSelectedRow();
					int maxcol = selectedTable.getModel().getColumnCount();
					int maxrow = selectedTable.getModel().getRowCount();

					// Enterキーなら下へ移動
					if (e.getKeyCode() == KeyEvent.VK_ENTER){
						reflectFromFieldToSelectedCell(jTextField_SelectedValue.getText());
						if(row + 1 < maxrow ){
							selectedTable.changeSelection(row + 1, col, false, false);
							jTextField_SelectedValue.setText((String)selectedTable.getValueAt(row + 1, col));
						}
					}
					// Tabキーなら右に移動
					if (e.getKeyCode() == KeyEvent.VK_TAB){
						reflectFromFieldToSelectedCell(jTextField_SelectedValue.getText());
						if(col + 1 < maxcol ){
							selectedTable.changeSelection(row, col + 1, false, false);
							jTextField_SelectedValue.setText((String)selectedTable.getValueAt(row, col + 1));
						}
					}
				}
			});
		}
		return jTextField_SelectedValue;
	}

	private void reflectFromFieldToSelectedCell(Object value){
        int[] cols = selectedTable.getSelectedColumns();
        int[] rows = selectedTable.getSelectedRows();

        selectedTableModel = (ResultTableModel) selectedTable.getModel();
        for(int col: cols){
        	int fieldNo = selectedTable.convertColumnIndexToModel(col);
        	if(!fieldInfo.isEditable(fieldNo)){continue;}
        	for(int row:rows){
        		selectedTableModel.setValueAt(value, row, fieldNo);
        		if(fieldInfo.getEditType(fieldNo).equals("select")){ //$NON-NLS-1$
            		@SuppressWarnings("unchecked")
					JComboBox<Object> combo = (JComboBox<Object>)selectedTable.getCellEditor(row, col).getTableCellEditorComponent(selectedTable, value, true, row, col);
            		combo.setSelectedItem(value);
            		selectedTable.getCellEditor(row, col).stopCellEditing();
        		}
        	}
        }
        selectedTable.updateUI();
	}

	void showResult() {
		ResultTableModel resultTableModel = (ResultTableModel) selectedTable
				.getModel();
		int allDataSize = resultTableModel.dataSize();
		int filteredDataSize = resultTableModel.filteredDataSize();
		if (allDataSize != filteredDataSize) {
			statusBar.setText(Messages.getString("Frame1.507") + //$NON-NLS-1$
					allDataSize + ", " + filteredDataSize); //$NON-NLS-1$
		} else {
			statusBar.setText(Messages.getString("Frame1.509") + allDataSize); //$NON-NLS-1$
		}
	}

	/**
	 * This method initializes jTabbedPane2
	 *
	 * @return javax.swing.JTabbedPane
	 */
	private JTabbedPane getJTabbedPane_ResultTables() {
		if (jTabbedPane_ResultTables == null) {
			jTabbedPane_ResultTables = new JTabbedPane();
			jTabbedPane_ResultTables.setTabPlacement(JTabbedPane.BOTTOM);
			jTabbedPane_ResultTables.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (e.isMetaDown()) {
						JPopupMenu popupMenu = new JPopupMenu();
						JMenuItem menuItem;

						menuItem = new JMenuItem(Messages.getString("Frame1.502")); //$NON-NLS-1$
						menuItem.addMouseListener(new addTab_mouseAdapter(menuItem));
						popupMenu.add(menuItem);

						if (jTabbedPane_ResultTables.getTabCount() > 1) {
							menuItem = new JMenuItem(Messages.getString("Frame1.503")); //$NON-NLS-1$
							menuItem.addMouseListener(new removeTab_mouseAdapter(menuItem));
							popupMenu.add(menuItem);
						}

						popupMenu.show(e.getComponent(), e.getX(), e.getY());
					}
					selectedTable = (ResultTable) ((JScrollPane) jTabbedPane_ResultTables
							.getSelectedComponent()).getViewport().getComponent(0);
					showResult();
				}
			});
		}
		return jTabbedPane_ResultTables;
	}


	/**
	 * ウィンドウが閉じられたときにプログラムを終了させる
	 *
	 * @param e
	 *            WindowEvent
	 */
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			jMenuFileExit_actionPerformed(null);
		}
	}

	/**
	 * 検索の実行
	 *
	 * @return true:成功、false:失敗
	 */
	public boolean testrun() {
		try {
			// キーが指定されていない場合
			if (jTextField_Key.getText().compareTo("") == 0) { //$NON-NLS-1$
				JOptionPane.showMessageDialog(null,
						Messages.getString("Frame1.228"), //$NON-NLS-1$
						Messages.getString("Frame1.229"), //$NON-NLS-1$
						JOptionPane.ERROR_MESSAGE);
				statusBar.setText(Messages.getString("Frame1.230")); //$NON-NLS-1$
				return false;
			}

			ResultTableModel selectedTableModel = (ResultTableModel) selectedTable
					.getModel();
			//tableModelにDBコントロールを追加
			selectedTableModel.setDbController(dbController);

			// need to create a new SearchEngineWorker object every search
			searchEngineWorker = new SearchEngineWorker(this);

			// 検索フラグの変更
			searchEngineWorker.setStatus(SearchEngineWorker.SEARCH_NG);

			// ボタンの文字列を変更
			jButton_Search.setText(Messages.getString("Frame1.510")); //$NON-NLS-1$

			// ボタンを実行不可にする
			setEnabledAll(false);
			
			// レコードのフィールド設定
			searchEngineWorker.setFieldInfo(fieldInfo);
			searchEngineWorker.init(userSetting);
			dbController.setCorpora(searchEngineWorker.getCorpus());
			
			
			// データモデルのセット
			searchEngineWorker.setSelectedTable(selectedTable);
			// ステータスバーのセット
			searchEngineWorker.setStatusBar(statusBar);

			ArrayList<ResultRecord> resultRecordSet = resultRecordSetFarm
					.get(jTabbedPane_ResultTables.getSelectedIndex());
			resultRecordSet.clear();
			selectedTableModel.getFieldInfo().resetFieldStatus();
			
			ArrayList<String> keys;
			isErrorOccur = false;

			if (isRegexKey) {
				keys = new ArrayList<String>();
				keys.add(jTextField_Key.getText());
			} else {
				keys = jTextField_Key.expandKey();
			}

			// コーパスの有効・無効の設定
			ListModel<CorpusFile> model = getJList_nonTargetCorpus().getModel();
			CorpusFile[] nonTargetCorpus = new CorpusFile[model.getSize()];
			for (int i = 0; i < model.getSize(); i++) {
				nonTargetCorpus[i] = (CorpusFile) model.getElementAt(i);
			}

			// update corpus selections on both corpus and jList_*Corpus
			updateCorpusSelection();

			int length_context_kwic = 10;
			int length_context_search = 10;


			// 前後文脈長の取得
			try {
				length_context_kwic = Integer
						.parseInt(jTextField_Length_of_Context.getText());
			} catch (NumberFormatException ex1) {
				JOptionPane.showMessageDialog(null,
						Messages.getString("Frame1.231"), //$NON-NLS-1$
						Messages.getString("Frame1.232"), //$NON-NLS-1$
						JOptionPane.ERROR_MESSAGE);
				statusBar.setText(Messages.getString("Frame1.233")); //$NON-NLS-1$
				return false;
			}

			// 検索範囲の取得
			try {
				length_context_search = Integer.parseInt(jTextField_SearchRange
						.getText());
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(null,
						Messages.getString("Frame1.234"), //$NON-NLS-1$
						Messages.getString("Frame1.235"), //$NON-NLS-1$
						JOptionPane.ERROR_MESSAGE);
				statusBar.setText(Messages.getString("Frame1.236")); //$NON-NLS-1$
				return false;
			}

			// フィルターの設定
			Filter filter = createFilter();

			// 検索パラメタのセット
			searchEngineWorker.setKeys(keys);
			searchEngineWorker.setKeyHead(jTextField_KeyPrev
					.getModifiedSearchKey());
			searchEngineWorker.setKeyTail(jTextField_KeyFol
					.getModifiedSearchKey());
			searchEngineWorker.setLength_context_kwic(length_context_kwic);
			searchEngineWorker.setLength_context_search(length_context_search);
			searchEngineWorker.setFilter(filter);
			searchEngineWorker.setCatPreviousContext(jCheckBox_KeyRangePrev
					.isSelected());
			searchEngineWorker.setCatFollowingContext(jCheckBox_KeyRangeFol
					.isSelected());
			searchEngineWorker
					.setSearchTargetName((String) jComboBox_searchTarget
							.getSelectedItem());
			searchEngineWorker.setResultRecordSet(resultRecordSet);
			searchEngineWorker.setIsRecordBased(isRecordBased);

			/**
			 * レコードベース
			 */
			if (isRecordBased) {

				/**
				 * 抽出：全数
				 */
				if (jRadioButton_extraction_all.isSelected()) {

					searchEngineWorker.setMode(SearchEngineWorker.MODE_NORMAL);

					// 上限数指定なし
					if (jTextField_nExtraction.getText().compareTo("") == 0) { //$NON-NLS-1$
						// 上限なしの場合は-1をセット
						searchEngineWorker.setLimit(-1);
					}
					// 上限数指定あり
					else {
						int nLimit = -1;
						// 上限の取得
						try {
							nLimit = Integer.parseInt(jTextField_nExtraction
									.getText());
							if (nLimit < 1) {
								new NumberFormatException();
							}
						}
						// 数値以外の場合はエラーを表示し、終了
						catch (NumberFormatException ex2) {
							statusBar.setText(Messages.getString("Frame1.239")); //$NON-NLS-1$
							JOptionPane.showMessageDialog(null,
									Messages.getString("Frame1.240"), //$NON-NLS-1$
									Messages.getString("Frame1.241"), //$NON-NLS-1$
									JOptionPane.ERROR_MESSAGE);
							return false;
						}
						searchEngineWorker.setLimit(nLimit);
					}
				}
				/**
				 * 抽出：ランダム
				 */
				else if (jRadioButton_extraction_random.isSelected()) {

					searchEngineWorker.setMode(SearchEngineWorker.MODE_RAMDOM);

					int nSample = 0;

					// サンプル数が未入力または非数値の場合はエラー
					try {
						nSample = Integer
								.parseInt(jTextField_nSample.getText());
					} catch (NumberFormatException ex2) {
						statusBar.setText(Messages.getString("Frame1.243")); //$NON-NLS-1$
						JOptionPane.showMessageDialog(null,
								Messages.getString("Frame1.244"), //$NON-NLS-1$
								Messages.getString("Frame1.245"), //$NON-NLS-1$
								JOptionPane.ERROR_MESSAGE);
						return false;
					}
					searchEngineWorker.setSample(nSample);
				}
				/**
				 * 抽出：頻度計測のみ
				 */
				else if (jRadioButton_extraction_countonly.isSelected()) {
					if(jRadioButton_countonly_option1.isSelected()){
						searchEngineWorker.setMode(SearchEngineWorker.MODE_COUNT);
					} else {
						searchEngineWorker.setMode(SearchEngineWorker.MODE_SUMMARY);
					}
				}
			}
			// not RecordBased
			else {
				// 前文脈が空欄でない場合
				if (jTextField_KeyPrev.getModifiedSearchKey().compareTo("") != 0) { //$NON-NLS-1$
					filter.put(ResultRecord.PRECEDING_CONTEXT_FIELD,
							Pattern.compile(jTextField_KeyPrev
									.getModifiedSearchKey()),
							jTextField_KeyPrev.isNot());
				}
				// 後文脈が空欄でない場合
				if (jTextField_KeyFol.getModifiedSearchKey().compareTo("") != 0) { //$NON-NLS-1$
					filter.put(ResultRecord.FOLLOWING_CONTEXT_FIELD, Pattern
							.compile(jTextField_KeyFol.getModifiedSearchKey()),
							jTextField_KeyFol.isNot());
				}
				// filterのセット
				searchEngineWorker.setFilter(filter);

				/**
				 * 抽出：全数
				 */
				if (jRadioButton_extraction_all.isSelected()) {
					searchEngineWorker.setMode(SearchEngineWorker.MODE_NORMAL);

					// 抽出上限数が空欄の場合
					if (jTextField_nExtraction.getText().compareTo("") == 0) { //$NON-NLS-1$
						searchEngineWorker.setLimit(-1);
					}
					// 抽出上限数が空欄以外の場合
					else {
						int nLimit = -1;
						try {
							nLimit = Integer.parseInt(jTextField_nExtraction
									.getText());
							if (nLimit < 1) {
								new NumberFormatException();
							}
						} catch (NumberFormatException ex2) {
							statusBar.setText(Messages.getString("Frame1.256")); //$NON-NLS-1$
							JOptionPane.showMessageDialog(null,
									Messages.getString("Frame1.257"), //$NON-NLS-1$
									Messages.getString("Frame1.258"), //$NON-NLS-1$
									JOptionPane.ERROR_MESSAGE);
							return false;
						}
						searchEngineWorker.setLimit(nLimit);
					}
				}
				/**
				 * 抽出：ランダム
				 */
				else if (jRadioButton_extraction_random.isSelected()) {
					searchEngineWorker.setMode(SearchEngineWorker.MODE_RAMDOM);

					int nSample = 0;
					try {
						nSample = Integer
								.parseInt(jTextField_nSample.getText());
					} catch (NumberFormatException ex2) {
						statusBar.setText(Messages.getString("Frame1.260")); //$NON-NLS-1$
						JOptionPane.showMessageDialog(null,
								Messages.getString("Frame1.261"), //$NON-NLS-1$
								Messages.getString("Frame1.262"), //$NON-NLS-1$
								JOptionPane.ERROR_MESSAGE);
						return false;
					}
					searchEngineWorker.setSample(nSample);

				}
				/**
				 * 抽出：頻度計測のみ
				 */
				else if (jRadioButton_extraction_countonly.isSelected()) {
					if(jRadioButton_countonly_option1.isSelected()){
						searchEngineWorker.setMode(SearchEngineWorker.MODE_COUNT);
					} else {
						searchEngineWorker.setMode(SearchEngineWorker.MODE_SUMMARY);
					}
				}
			}
			// 検索の実行（swingWorkerによる非同期処理の実行）
			searchEngineWorker.execute();

		} catch (OutOfMemoryError er) {
			isErrorOccur = true;
			er.printStackTrace();
			JOptionPane.showMessageDialog(null, er.getMessage(),
					Messages.getString("Frame1.275"), //$NON-NLS-1$
					JOptionPane.ERROR_MESSAGE);
			return false;
		} catch (Exception e3) {
			e3.printStackTrace();
			System.out.println(Messages.getString("Frame1.358")); //$NON-NLS-1$
			JOptionPane.showMessageDialog(null, e3.getMessage(),
					Messages.getString("Frame1.275"), //$NON-NLS-1$
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	
	public Filter createFilter(){
		Filter filter = new Filter();

		for (int i = 0; i < nFilter; i++) {
			if (jTextField_filter[i].getText().compareTo("") != 0) { //$NON-NLS-1$
				filter.put(fieldInfo
						.getFieldID((String) jComboBox_filterTarget[i]
								.getSelectedItem()), Pattern
						.compile(jTextField_filter[i]
								.getModifiedSearchKey()),
						jTextField_filter[i].isNot());
				jTextField_filter[i].registerKeyHistory();
			}
		}
		
		return filter;
	}
	
	
	
	public void updateCorpusSelection() {
		DefaultListModel<CorpusFile> targetModel = (DefaultListModel<CorpusFile>)jList_targetCorpus.getModel();
		DefaultListModel<CorpusFile> nonTargetModel = (DefaultListModel<CorpusFile>)jList_nonTargetCorpus.getModel();
		CorpusFile[] corpus = searchEngineWorker.getCorpus();
		
		for (int i = 0; i < corpus.length; i++) {
			boolean updateFlag = false;
			for(int j = 0; j < targetModel.getSize(); j++){
				CorpusFile corpusFile = targetModel.getElementAt(j);
				if (corpus[i].getCorpusName().equals(corpusFile.getCorpusName())){
					corpus[i].select(true);
					targetModel.set(j, corpus[i]);
					updateFlag = true;
					break;
				}
			}
			if(updateFlag) continue;
			
			for(int j = 0; j < nonTargetModel.getSize(); j++){
				CorpusFile corpusFile = nonTargetModel.getElementAt(j);
				if (corpus[i].getCorpusName().equals(corpusFile.getCorpusName())){
					corpus[i].select(false);
					nonTargetModel.set(j, corpus[i]);
					updateFlag = true;
					break;
				}
			}

			if(!updateFlag){
				System.err.println("Error(Frame1): updateCorpusSelection failed, " + corpus[i].getCorpusName()); //$NON-NLS-1$
			}
		}
	}
	

	void addTable2Tab() {
		ResultTableModel resultTableModel = new ResultTableModel();
		ResultTable jTable = new ResultTable();
		jTable.setParentFrame(this);
		if (jTabbedPane_ResultTables.getTabCount() == 0) {
			selectedTable = jTable;
		}
		
		ArrayList<ResultRecord> resultRecordSet = new ArrayList<ResultRecord>();
		resultRecordSetFarm.add(resultRecordSet);
		resultTableModel.setData(resultRecordSet);
		jTable.setModel(resultTableModel);
		jTable.getModel().addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				showResult();
			}
		});
		// 結果テーブルのヘッダ変更
		resultTableModel.setFieldInfo(fieldInfo);
		jTable.setColumnProperty(fieldInfo);

		jTable.setFontSize(fontsize);
		jTable.setRowHeight(jTable.getFontMetrics(jTable.getFont()).getHeight());

		jTable.setAutoCreateColumnsFromModel(true);
		jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		jTable.setColumnSelectionAllowed(false);
		jTable.setRowSelectionAllowed(true);
		jTable.setCellSelectionEnabled(true);

		//セルエディターの設定
		jTable.setCellEditor();


		//コンボボックスの選択内容と選択項目表示エリアの内容を連動させる
		jTable.addFocusListener(new FocusAdapter(){
			public void focusGained(FocusEvent e){
				jTable_focusGained(e);
			}
			public void focusLost(FocusEvent e){
				jTable_focusGained(e);
			}

		});

		jTable.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				jTable_mouseClicked(e);
			}
		});

		jTable.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
		        Component source = (Component) e.getSource();
		        MouseEvent parentEvent = SwingUtilities.convertMouseEvent(source, e, source.getParent());
				if(e.isControlDown()){
			        source.getParent().dispatchEvent(parentEvent);
				} else {
			        source.dispatchEvent(parentEvent);
				}
			}
		});
		
		jTable.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				jTable1_keyReleased(e);
			}
		});

		JScrollPane jScrollPane = new JScrollPane();
		jScrollPane.setFont(new java.awt.Font("Dialog", 0, fontsize)); //$NON-NLS-1$
		jScrollPane.setAutoscrolls(true);
		jScrollPane.setViewportView(jTable);
		jTabbedPane_ResultTables.add(jScrollPane, String.valueOf(iTab++));
	}

	/**
	 * セルにフォーカスが入った時のアクション
	 * @param e
	 */
	private void jTable_focusGained(FocusEvent e) {
		int row = selectedTable.getSelectedRow();
		int col = selectedTable.getSelectedColumn();

		if(col >= 0 && row >= 0){
			switchInputTypeOfSelectedValue(row,col);
		}
		return;
	}

	private void switchInputTypeOfSelectedValue(int row, int col){
		String selectedVal = (String) selectedTable.getValueAt(row, col);

		//セル種別（text or select）の取得
		String fType = fieldInfo.getEditType(selectedTable.convertColumnIndexToModel(col));

		// 左シングルクリック
		// 選択セルの値を行表示欄に表示

		//combo boxの場合
		if("select".equals(fType)){ //$NON-NLS-1$
			changeSelectedValueInput(false);

			//選択項目のセット
			resetComboSelectVal(row, col, selectedVal);
		}
		//textの場合
		else{
			changeSelectedValueInput(true);
			jTextField_SelectedValue.setText(selectedVal);
		}

	}

	private void resetComboSelectVal(int row, int col, Object value){
		//選択項目のセット
		List<String> optList = new ArrayList<String>();
		optList.add(ResultTable.BLANK_STR);//空文字の挿入
		String[] options = fieldInfo.getEditOption(selectedTable.convertColumnIndexToModel(col)).split(","); //$NON-NLS-1$
		for(String opt:options){optList.add(opt);}

		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>((String[])optList.toArray(new String[0]));
		model.setSelectedItem(value);
		jComboBox_SelectedValue.setModel(model);
	}

	/**
	 * 検索結果表内のカーソルの移動
	 *
	 * @param e
	 *            KeyEvent
	 */
	void jTable1_keyReleased(KeyEvent e) {
		if (selectedTable.getSelectedRow() >= 0) {
			jTextField_SelectedValue.setText((String) selectedTable.getValueAt(
					selectedTable.getSelectedRow(),
					selectedTable.getSelectedColumn()));
		}
		return;
	}

	/**
	 * フィールド値，著者データベース，記事の表記
	 *
	 * @param e
	 *            MouseEvent
	 */
	void jTable_mouseClicked(MouseEvent e) {
		final Pattern regexField = Pattern.compile("/(.*)/"); //$NON-NLS-1$
		
		int col = selectedTable.getSelectedColumn();
		int row = selectedTable.getSelectedRow();
		
		if(col == -1 || row == -1){
			return;
		}
		
		switchInputTypeOfSelectedValue(row,col);

		if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
			String selectedColumnName = selectedTable
					.getColumnName(selectedTable.getSelectedColumn());

			List<Node> externalTools = null;
			try {
				externalTools = userSetting.evaluateNodes("/setting/external_tools/li", true); //$NON-NLS-1$
			} catch (XPathExpressionException e1) {
			}

			for(Node externalTool : externalTools){
				String field = ((Element)externalTool).getAttribute("field"); //$NON-NLS-1$
				String command = ((Element)externalTool).getAttribute("path"); //$NON-NLS-1$
				String arguments = ((Element)externalTool).getAttribute("argument"); //$NON-NLS-1$
				String os = ((Element)externalTool).getAttribute("os"); //$NON-NLS-1$
				
				Matcher m = regexField.matcher(field);
				if(((field == null || field.isEmpty()) && arguments.equals("((" + selectedColumnName + "))")) // for compatibility to ver.1.2 //$NON-NLS-1$ //$NON-NLS-2$
						|| ( m.find() && selectedColumnName.matches(m.group(1))) // regular expression
						|| field.equals(selectedColumnName)){ // normal string
					accessExternalData(command, arguments, os);
					return;
				}
			}

			// 左ボタン，double click
			// 閲覧１
			int selectedRow = selectedTable.getSelectedRow();
			ResultRecord selectedRecord = ((ResultTableModel) selectedTable
					.getModel()).getRecordAt(selectedRow);
			int corpusNumber = selectedRecord.getResourceID();
			int index = selectedRecord.getPosition();
			
			if(e.isShiftDown()){
				for(String elementName : dicFarm.keySet()){
					browseSIX(corpusNumber, index, elementName.replaceFirst("(.+)\\[-?\\d+\\]$", "$1")); //$NON-NLS-1$ //$NON-NLS-2$
					break; // always select the first dic
				}
			} else {
				// 検索語
				String target = (String) selectedRecord.get(searchEngineWorker
						.getStoredFieldName());
				CorpusBrowser corpusBrowser = new CorpusBrowser(userSetting, searchEngineWorker, Frame1.this);
				corpusBrowser.browse(corpusNumber, index, Util.strlenSP(target), iSelectedBrowser, iSelectedXSL);
			}
		}
	}

	class addTab_mouseAdapter extends java.awt.event.MouseAdapter {
		JMenuItem adaptee;

		addTab_mouseAdapter(JMenuItem adaptee) {
			this.adaptee = adaptee;
		}

		public void mouseReleased(MouseEvent e) {
			addTable2Tab();
		}
	}

	class removeTab_mouseAdapter extends java.awt.event.MouseAdapter {
		JMenuItem adaptee;

		removeTab_mouseAdapter(JMenuItem adaptee) {
			this.adaptee = adaptee;
		}

		public void mouseReleased(MouseEvent e) {
			if (jTabbedPane_ResultTables.getTabCount() > 1) {
				int iSelectedTab = jTabbedPane_ResultTables.getSelectedIndex();
				jTabbedPane_ResultTables.remove(iSelectedTab);
				resultRecordSetFarm.remove(iSelectedTab);
				selectedTable = (ResultTable) ((JScrollPane) jTabbedPane_ResultTables
						.getSelectedComponent()).getViewport().getComponent(0);
			}
		}
	}

	/**
	 * This method initializes jPanel_Target_Words
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel_Target_Words() {
		if (jPanel_Target_Words == null) {
			GridLayout gridLayout8 = new GridLayout();
			gridLayout8.setRows(3);
			gridLayout8.setHgap(0);
			gridLayout8.setVgap(4);
			gridLayout8.setColumns(1);
			jPanel_Target_Words = new JPanel();
			jPanel_Target_Words.setMinimumSize(new Dimension(70, 71));
			jPanel_Target_Words.setPreferredSize(new Dimension(70, 71));
			jPanel_Target_Words.setBorder(BorderFactory.createEmptyBorder(0, 2,
					0, 2));
			jPanel_Target_Words.setLayout(gridLayout8);
			jPanel_Target_Words.setName(""); //$NON-NLS-1$

			jPanel_Target_Words.add(getJPanel20(), null);
			jPanel_Target_Words.add(getJPanel6(), null);
			jPanel_Target_Words.add(getJPanel12(), null);
		}
		return jPanel_Target_Words;
	}

	/**
	 * This method initializes jPanel20
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel20() {
		if (jPanel20 == null) {

			BorderLayout borderLayout13 = new BorderLayout();
			borderLayout13.setHgap(5);
			jPanel20 = new JPanel();
			jPanel20.setLayout(borderLayout13);
			jPanel20.add(getJComboBox_searchTarget(), BorderLayout.WEST);
			jPanel20.add(getJTextField_Key(), java.awt.BorderLayout.CENTER);

		}
		return jPanel20;
	}

	/**
	 * This method initializes jTextField_Key
	 *
	 * @return himawari.SearchTextField
	 */
	private SearchTextField getJTextField_Key() {
		if (jTextField_Key == null) {
			jTextField_Key = new SearchTextField();
			jTextField_Key.setMargin(new Insets(0, 2, 0, 0));
			jTextField_Key.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					jTextField_Key_keyPressed(e);
				}
			});

			jTextField_Key.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					jTextField_Key_mouseClicked(e);
				}
			});
		}
		return jTextField_Key;
	}

	/**
	 * This method initializes jPanel6
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel6() {
		if (jPanel6 == null) {
			jLabel_PrevContext = new JLabel();
			jLabel_PrevContext.setBorder(BorderFactory.createEmptyBorder());
			jLabel_PrevContext.setMinimumSize(new Dimension(150, 18));
			jLabel_PrevContext.setPreferredSize(new Dimension(150, 18));
			jLabel_PrevContext.setText(Messages.getString("Frame1.27")); //$NON-NLS-1$
			jLabel_PrevContext.setMaximumSize(new Dimension(110, 18));
			BorderLayout borderLayout1 = new BorderLayout();
			borderLayout1.setHgap(5);
			jPanel6 = new JPanel();
			jPanel6.setLayout(borderLayout1);
			jPanel6.add(jLabel_PrevContext, java.awt.BorderLayout.WEST);
			jPanel6.add(getJComboBox_KeyPrev(), java.awt.BorderLayout.EAST);
			jPanel6.add(getJTextField_KeyPrev(), java.awt.BorderLayout.CENTER);

		}
		return jPanel6;
	}

	/**
	 * This method initializes jTextField_KeyPrev
	 *
	 * @return himawari.SearchTextField
	 */
	private SearchTextField getJTextField_KeyPrev() {
		if (jTextField_KeyPrev == null) {
			jTextField_KeyPrev = new SearchTextField();
			jTextField_KeyPrev.setAlignmentX((float) 0.5);
			jTextField_KeyPrev.setMargin(new Insets(0, 2, 0, 0));
			jTextField_KeyPrev.setJComboBox(jComboBox_KeyPrev, 2);
			jTextField_KeyPrev.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					jTextField_Key_keyPressed(e);
				}
			});
			jTextField_KeyPrev
					.addMouseListener(new java.awt.event.MouseAdapter() {
						public void mouseClicked(MouseEvent e) {
							jTextField_KeyPrev_mouseClicked(e);
						}
					});
		}
		return jTextField_KeyPrev;
	}

	/**
	 * This method initializes jComboBox_KeyPrev
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox<String> getJComboBox_KeyPrev() {
		if (jComboBox_KeyPrev == null) {
			jComboBox_KeyPrev = new HimawariJComboBox();
			// jComboBox_KeyPrev.setMinimumSize(new Dimension(32, 27));
			// jComboBox_KeyPrev.setPreferredSize(new Dimension(200, 27));
			jComboBox_KeyPrev.setMaximumSize(new Dimension(230, 27));
		}
		return jComboBox_KeyPrev;
	}

	/**
	 * This method initializes jPanel12
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel12() {
		if (jPanel12 == null) {
			jLabel_FolContext = new JLabel();
			jLabel_FolContext.setBorder(BorderFactory.createEmptyBorder());
			jLabel_FolContext.setMinimumSize(new Dimension(150, 18));
			jLabel_FolContext.setPreferredSize(new Dimension(150, 18));
			jLabel_FolContext.setText(Messages.getString("Frame1.28")); //$NON-NLS-1$
			jLabel_FolContext.setMaximumSize(new Dimension(110, 18));
			BorderLayout borderLayout2 = new BorderLayout();
			borderLayout2.setHgap(5);
			jPanel12 = new JPanel();
			jPanel12.setLayout(borderLayout2);
			jPanel12.add(jLabel_FolContext, java.awt.BorderLayout.WEST);
			jPanel12.add(getJComboBox_KeyFol(), java.awt.BorderLayout.EAST);
			jPanel12.add(getJTextField_KeyFol(), java.awt.BorderLayout.CENTER);

		}
		return jPanel12;
	}

	/**
	 * This method initializes jTextField_KeyFol
	 *
	 * @return himawari.SearchTextField
	 */
	private SearchTextField getJTextField_KeyFol() {
		if (jTextField_KeyFol == null) {
			jTextField_KeyFol = new SearchTextField();
			jTextField_KeyFol.setAlignmentX((float) 0.5);
			jTextField_KeyFol.setMargin(new Insets(0, 2, 0, 0));
			jTextField_KeyFol.setJComboBox(jComboBox_KeyFol, 0);
			jTextField_KeyFol.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					jTextField_Key_keyPressed(e);
				}
			});
			jTextField_KeyFol
					.addMouseListener(new java.awt.event.MouseAdapter() {
						public void mouseClicked(MouseEvent e) {
							jTextField_KeyFol_mouseClicked(e);
						}
					});
		}
		return jTextField_KeyFol;
	}

	/**
	 * This method initializes jComboBox_KeyFol
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox<String> getJComboBox_KeyFol() {
		if (jComboBox_KeyFol == null) {
			jComboBox_KeyFol = new HimawariJComboBox();
			// jComboBox_KeyFol.setMinimumSize(new Dimension(32, 27));
			// jComboBox_KeyFol.setPreferredSize(new Dimension(200, 27));
			jComboBox_KeyFol.setMaximumSize(new Dimension(230, 27));
		}
		return jComboBox_KeyFol;
	}

	/**
	 * This method initializes jComboBox_searchTarget
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox<String> getJComboBox_searchTarget() {
		if (jComboBox_searchTarget == null) {
			jComboBox_searchTarget = new HimawariJComboBox();
			jComboBox_searchTarget.setMaximumSize(new Dimension(150, 27));
			jComboBox_searchTarget.setMinimumSize(new Dimension(150, 27));
			jComboBox_searchTarget.setPreferredSize(new Dimension(150, 27));
			jComboBox_searchTarget.setBorder(BorderFactory
					.createLoweredBevelBorder());

			jComboBox_searchTarget
					.addItemListener(new java.awt.event.ItemListener() {
						public void itemStateChanged(ItemEvent e) {
							jComboBox_searchTarget_itemStateChanged(e);
						}
					});
		}
		return jComboBox_searchTarget;
	}

	/**
	 * This method initializes jMenuItem_saveSetting
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem_saveSetting() {
		if (jMenuItem_saveSetting == null) {
			jMenuItem_saveSetting = new JMenuItem();
			jMenuItem_saveSetting.setText(Messages.getString("Frame1.516")); //$NON-NLS-1$

			jMenuItem_saveSetting.setAccelerator(KeyStroke.getKeyStroke('U',
					KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, false));
			jMenuItem_saveSetting
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							jMenuItem_saveSetting_actionPerformed(e);
						}
					});
		}
		return jMenuItem_saveSetting;
	}

	/**
	 * ユーザ設定保存機能の実行
	 *
	 * @param e
	 *            アクションイベント
	 */
	protected void jMenuItem_saveSetting_actionPerformed(ActionEvent e) {

		String selectedValue = (String) JOptionPane.showInputDialog(
				this,
				Messages.getString("Frame1.517"), //$NON-NLS-1$
				Messages.getString("Frame1.516"), //$NON-NLS-1$ //$NON-NLS-2$
				JOptionPane.INFORMATION_MESSAGE, null, saveSettingLabel,
				saveSettingLabel[saveSetting]);

		// 選択した値をプロパティに反映する
		if (selectedValue != null) {

			if (selectedValue == saveSettingLabel[VALID_FLAG]) {
				saveSetting = VALID_FLAG;
			} else {
				saveSetting = INVALID_FLAG;
			}
		}
	}
	
	
	private JMenuItem getJMenuItem_language() {
		if (jMenuItem_language == null) {
			jMenuItem_language = new JMenuItem();
			jMenuItem_language.setText(Messages.getString("Frame1.115")); //$NON-NLS-1$

			jMenuItem_language
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							String beforeDisplayLanguage = LocaleUtil.getLocale().getDisplayLanguage();
							String selectedDisplayLanguage = (String) JOptionPane.showInputDialog(
									Frame1.this,
									Messages.getString("Frame1.125"), //$NON-NLS-1$
									Messages.getString("Frame1.131"), //$NON-NLS-1$
									JOptionPane.INFORMATION_MESSAGE,
									null,
									LocaleUtil.getDisplayLanguages(),
									LocaleUtil.getLocale().getDisplayLanguage());
							
							if (selectedDisplayLanguage != null && !beforeDisplayLanguage.equals(selectedDisplayLanguage)) {
								localeUtil.setNextStartupLanguage(LocaleUtil.getLanguage(selectedDisplayLanguage));
								JOptionPane.showMessageDialog(Frame1.this, Messages.getString("Frame1.132")); //$NON-NLS-1$
							}
						}
					});
		}
		return jMenuItem_language;
	}


	
	private JMenuItem getJMenuItemConstructAnalyzeCorpus() {
		if (jMenuItemOptionAnalyze == null) {
			jMenuItemOptionAnalyze = new JMenuItem();
			jMenuItemOptionAnalyze.setText(Messages.getString("Frame1.151")); //$NON-NLS-1$

			jMenuItemOptionAnalyze
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							searchEngineWorker.analyze();
							
							for(Entry<String, CorpusElementInfo> element : elementMap.entrySet()) {
							    System.out.println(element.getKey() + ":" + element.getValue()); //$NON-NLS-1$
							}
							
							JOptionPane.showMessageDialog(Frame1.this, Messages.getString("Frame1.153")); //$NON-NLS-1$
							try {
								myInit();
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					});
		}
		return jMenuItemOptionAnalyze;
	}
	
	/**
	 * [ツール|データ変換]
	 *
	 * @param e
	 *            ActionEvent
	 */
	public void jMenuConvertData_actionPerformed(ActionEvent e) {
		Frame1_AboutBox dlg = new Frame1_AboutBox(this);
		Dimension dlgSize = dlg.getPreferredSize();
		Dimension frmSize = getSize();
		Point loc = getLocation();
		dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,
				(frmSize.height - dlgSize.height) / 2 + loc.y);
		dlg.setModal(true);
		dlg.imageLabel
				.setIcon(new ImageIcon(
						Frame1_AboutBox.class
								.getResource("/jp/ac/ninjal/himawari/images/" + picfiles[nHelpCalled % picfiles.length]))); //$NON-NLS-1$
		dlg.setVisible(true);
		nHelpCalled = (int) (Math.random() * picfiles.length);
	}

	public void setEnableButtonSaveDb(boolean bool){
		jButton_save_db.setEnabled(bool);
	}

	public void setEnableButtonReverDb(boolean bool){
		jButton_revert_db.setEnabled(bool);
	}
	

	public SearchResultFrame invokeSelectedStatFrame(ResultTable resultTable, FieldInfo newFieldInfo){
		return invokeSelectedStatFrame(resultTable, newFieldInfo, true);
	}

	
	public SearchResultFrame invokeSelectedStatFrame(ResultTable resultTable, FieldInfo newFieldInfo, boolean isConfirmed){
		if(newFieldInfo.size() < 2){
			return null;
		}
		
		final SearchResultFrame sf = new SearchResultFrame(this);
		resultFrames.add(sf);
		sf.setFieldInfo(newFieldInfo);
		resultTable.setParentFrame(resultTable.getParentFrame());

		sf.invokeStatisticsFrame(resultTable, fontsize, isConfirmed);
		sf.getTable().addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
					int selectedRow = sf.getTable().getSelectedRow();
					ResultRecord selectedRecord = ((ResultTableModel) sf.getTable().getModel()).getRecordAt(selectedRow);
					int corpusNumber = selectedRecord.getResourceID();
					int index = selectedRecord.getPosition();
					if(index == -1){
						return;
					}
					
					String targetText = (String) selectedRecord.get(searchEngineWorker.getStoredFieldName());
					int textLen = 1;
					if(targetText != null && !targetText.isEmpty()){
						textLen = Util.strlenSP(targetText);
					}
					CorpusBrowser corpusBrowser = new CorpusBrowser(userSetting, searchEngineWorker, Frame1.this);
					corpusBrowser.browse(corpusNumber, index, textLen, iSelectedBrowser, iSelectedXSL);
				}
			}
		});
		
		return sf;
	}


	/**
	 * This method initializes jButton_save_db
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButton_save_db() {
		if (jButton_save_db == null) {
			jButton_save_db = new JButton(Messages.getString("Frame1.531")); //$NON-NLS-1$
			jButton_save_db.setFont(new Font("Dialog", Font.PLAIN, 10)); //$NON-NLS-1$
			jButton_save_db.setEnabled(false);
			jButton_save_db.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					saveDb_actionPerformed();
				}
			});
		}
		return jButton_save_db;
	}

	/**
	 * db保存の実行
	 * @param e
	 */
	private void saveDb_actionPerformed() {
		//確認ダイアログ
		int selopt = JOptionPane.showConfirmDialog(this, Messages.getString("Frame1.615"), Messages.getString("Frame1.5"),JOptionPane.YES_NO_OPTION); //$NON-NLS-1$ //$NON-NLS-2$

		if(selopt == JOptionPane.YES_OPTION){
			//データの保存
			doSaveDb();
		}


	}

	/**
	 * テーブルの編集されたデータを保存する
	 */
	private void doSaveDb(){

		ResultTableModel selectedTableModel = (ResultTableModel) selectedTable.getModel();
		selectedTableModel.saveEdittedData();

		//保存が完了したらボタンをdisabledに戻す
		jButton_save_db.setEnabled(false);
		jButton_revert_db.setEnabled(false);
	}

	/**
	 * This method initializes jButton_revert_db
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButton_revert_db() {
		if (jButton_revert_db == null) {
			jButton_revert_db = new JButton(Messages.getString("Frame1.532")); //$NON-NLS-1$
			jButton_revert_db.setFont(new Font("Dialog", Font.BOLD, 10)); //$NON-NLS-1$
			jButton_revert_db.setEnabled(false);
			jButton_revert_db.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					revertDb_actionPerformed(e);
				}

			});
		}
		return jButton_revert_db;
	}


	/**
	 * This method initializes jPanel_save
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel_status() {
		if (jPanel_status == null) {

			jPanel_status = new JPanel();
						jPanel_status.setLayout(new BorderLayout(0, 0));
						jPanel_status.add(getJPanel_message(), BorderLayout.WEST);
			jPanel_status.add(getJPanel_save(), BorderLayout.EAST);
		}
		return jPanel_status;
	}

	/**
	 * 「元に戻す」実行時のアクション
	 * @param e
	 */
	private void revertDb_actionPerformed(ActionEvent e) {
		// 全てのデータか選択範囲のみか選択させるダイアログを表示
		JDialog confirmDialog = getJDialog_confirmRevert();
		confirmDialog.setVisible(true);

		selectedTable.updateUI();
	}

	/**
	 * This method initializes jDialog
	 *
	 * @return javax.swing.JDialog
	 */
	private JDialog getJDialog_confirmRevert() {
		if (jDialog_resetDb == null) {
			jDialog_resetDb = new JDialog();
			jDialog_resetDb.setModal(true);
			jDialog_resetDb.setTitle(Messages.getString("Frame1.533")); //$NON-NLS-1$
			jDialog_resetDb.setContentPane(getJContentPanel_confirmRevert());
			jDialog_resetDb.setSize(new Dimension(320, 160));
			jDialog_resetDb.setLocationRelativeTo(this);
		}
		return jDialog_resetDb;
	}

	/**
	 * リセット確認ウインドウのコンテンツ
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPanel_confirmRevert() {
		if (jContentPane11 == null) {
			GridLayout gridLayout = new GridLayout(3, 1);
			jContentPane11 = new JPanel();
			JLabel text = new JLabel();
			text.setText(Messages.getString("Frame1.534")); //$NON-NLS-1$
			jContentPane11.setLayout(gridLayout);
			jContentPane11.add(text);

			JPanel pr = new JPanel();

			//ラジオボタン（全て or 選択したもののみ）
			final JRadioButton radio_all = new JRadioButton(Messages.getString("Frame1.535"), true); //$NON-NLS-1$
			final JRadioButton radio_selected = new JRadioButton(Messages.getString("Frame1.536"), false); //$NON-NLS-1$
			pr.add(radio_all);
			pr.add(radio_selected);

			ButtonGroup group = new ButtonGroup();
			group.add(radio_all);
			group.add(radio_selected);

			jContentPane11.add(pr);

			JPanel pb = new JPanel();

			//resetボタン
			JButton btnReset = new JButton(Messages.getString("Frame1.537")); //$NON-NLS-1$
			btnReset.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					boolean isAll = radio_all.isSelected();
					resetDB_actionPeformed(isAll);
				}
			});

			//キャンセルボタン
			JButton btnCancel = new JButton(Messages.getString("Frame1.538")); //$NON-NLS-1$
			btnCancel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					jDialog_resetDb.setVisible(false);
				}
			});
			pb.add(btnReset);
			pb.add(btnCancel);

			jContentPane11.add(pb);

			jContentPane11.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		}
		return jContentPane11;
	}

	/**
	 * dbリセット時のアクション
	 * @param isAll
	 */
	private void resetDB_actionPeformed(boolean isAll){
		ResultTableModel selectedTableModel = (ResultTableModel) selectedTable.getModel();

		// reset all
		if(isAll){
			selectedTableModel.revertEdittedData();
		}
		// reset selected items
		else{
			int selectedFields[] = selectedTable.getSelectedColumns();
			for(int i = 0; i < selectedFields.length; i++){
				selectedFields[i] = selectedTable.convertColumnIndexToModel(selectedFields[i]);
			}
			selectedTableModel.revertEdittedData(selectedFields, selectedTable.getSelectedRows());
		}
		if(jDialog_resetDb != null){
			jDialog_resetDb.setVisible(false);
		}
	}


	/**
	 * groupLayoutを使用したデフォルトの設定を適用する
	 * @param compos
	 * @param panel
	 */
	public void setGroupLayoutUtil(Component[][] compos, JPanel panel){
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		int ny = compos.length;
		int nx = compos[0].length;

		// See https://www.ne.jp/asahi/hishidama/home/tech/java/swing/layout.html#h_GroupLayout_sample2
		{
			SequentialGroup hg = layout.createSequentialGroup();
			for (int x = 0; x < nx; x++) {
				ParallelGroup pg = layout.createParallelGroup();
				for (int y = 0; y < ny; y++) {
					JComponent c = (JComponent) compos[y][x];
					if (c != null) {
						pg.addComponent(c);
					}
				}
				hg.addGroup(pg);
			}
			layout.setHorizontalGroup(hg);
		}

		{
			SequentialGroup vg = layout.createSequentialGroup();
			for (int y = 0; y < ny; y++) {
				ParallelGroup pg = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
				for (int x = 0; x < nx; x++) {
					JComponent c = (JComponent) compos[y][x];
					if (c != null) {
						pg.addComponent(c);
					}
				}
				vg.addGroup(pg);
			}
			layout.setVerticalGroup(vg);
		}

	}


	/**
	 * ステータスバーに表示されているテキストを文字列として取得する。
	 * @return
	 */
	public String getStatusMsg(){
		return statusBar.getText();
	}

	/**
	 * テーブルが編集されたか調べる
	 * @return
	 */
	private boolean isTableEditted(){
		ResultTableModel model = (ResultTableModel)selectedTable.getModel();
		return model.isEditted();
	}

	public void setValueToCombo_selectedValue(int row, int col, Object value){
		resetComboSelectVal(row, col, value);
	}

	private JPanel getJPanel_save() {
		if (jPanel_save == null) {
			jPanel_save = new JPanel();
			jPanel_save.setLayout(new BorderLayout(0, 0));
			jLabel_save_db = new JLabel();
//			jLabel_save_db.setFont(new Font("Dialog", Font.PLAIN, 14));
			jPanel_save.add(jLabel_save_db, BorderLayout.WEST);
			jLabel_save_db.setText(Messages.getString("Frame1.530")); //$NON-NLS-1$
			jPanel_save.add(getJButton_save_db(), BorderLayout.CENTER);
			jPanel_save.add(getJButton_revert_db(), BorderLayout.EAST);
		}
		return jPanel_save;
	}
	private JPanel getJPanel_message() {
		if (jPanel_message == null) {
			jPanel_message = new JPanel();
						jPanel_message.setLayout(new BorderLayout(0, 0));
			
						statusBar = new JLabel();
//						statusBar.setVerticalAlignment(SwingConstants.BOTTOM);
						jPanel_message.add(statusBar);
						statusBar.setText("　"); // dummy wide-width space //$NON-NLS-1$
		}
		return jPanel_message;
	}
	
	
	class DropFileAdapter extends DropTargetAdapter {

		@Override
		public void drop(DropTargetDropEvent dtde) {
			Transferable t = dtde.getTransferable();
			dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

			if(!dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
				System.err.println("Warning(MainFrame): unsupported data"); //$NON-NLS-1$
				return;
			}
			try {
				@SuppressWarnings("unchecked")
				List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);

				if(files.size() > 0){
					File target = files.get(0);
					if(target.getName().endsWith(".zip")){ //$NON-NLS-1$
						int result = JOptionPane.showConfirmDialog(
								Frame1.this, target + Messages.getString("Frame1.7"), //$NON-NLS-1$
								Messages.getString("Frame1.9"), //$NON-NLS-1$
								JOptionPane.OK_CANCEL_OPTION);

						if (result != JOptionPane.YES_OPTION) {
							return;
						}
						installPackage(target.toPath());
					} else if(isHimawariDir(target) ||
							(Util.getOSNameLowerCase().startsWith("mac") && isMacApplication(target))){ //$NON-NLS-1$
						final String MAC_APP_HIMARIDIR_1_6 = "Contents/Java"; //$NON-NLS-1$
						final String MAC_APP_HIMARIDIR_1_7LATER = "Contents/Resources"; //$NON-NLS-1$
						JCheckBox cb = new JCheckBox(Messages.getString("Frame1.110")); //$NON-NLS-1$
						JTextArea ta = new JTextArea(String.format(Messages.getString("Frame1.148"), target)); //$NON-NLS-1$
						ta.setOpaque(false);
						JPanel panel = new JPanel();
						panel.setLayout(new GridLayout(2, 1));
						panel.add(ta);
						panel.add(cb);
						int result = JOptionPane.showConfirmDialog(
								Frame1.this, panel,
								Messages.getString("Frame1.9"), //$NON-NLS-1$
								JOptionPane.YES_NO_OPTION);

						if (result != JOptionPane.YES_OPTION) {
							return;
						}
						
						Path targetPath = target.toPath();
						if(Util.getOSNameLowerCase().startsWith("mac") && isMacApplication(target)) { //$NON-NLS-1$
							if(targetPath.resolve(MAC_APP_HIMARIDIR_1_6).toFile().exists()) {
								targetPath = targetPath.resolve(MAC_APP_HIMARIDIR_1_6);
							} else {
								targetPath = targetPath.resolve(MAC_APP_HIMARIDIR_1_7LATER);
							}
						}
						
						boolean flagMove = false;
						if(cb.isSelected()) {
							flagMove = true;
						}
						
						CorpusManager corpusManager = new CorpusManager(flagMove);
						corpusManager.copyAll(targetPath, new File("./").toPath()); //$NON-NLS-1$
						corpusManager.showLog(Frame1.this);
					} else if(target.isDirectory()){
						File childFiles[] = target.listFiles();
						// look into the lower directory, if only one directory exists.
						if(childFiles.length == 1 && childFiles[0].isDirectory()){
							target = childFiles[0];
							childFiles = target.listFiles();
						}
						
						boolean flagPackageInstall = false;
						
						for(File file: childFiles){
							if(file.getName().equals(PackageInstaller.PACKAGE_INFO_FILE)){
								flagPackageInstall = true;
								break;
							}
						}

						String message = ""; //$NON-NLS-1$
						if(flagPackageInstall){
							message = target + Messages.getString("Frame1.7"); //$NON-NLS-1$
						} else {
							message = target + Messages.getString("Frame1.8"); //$NON-NLS-1$
						}

						int result = JOptionPane.showConfirmDialog(
								Frame1.this, message,
								Messages.getString("Frame1.9"), //$NON-NLS-1$
								JOptionPane.YES_NO_OPTION);

						if (result != JOptionPane.YES_OPTION) {
							return;
						}

						if(flagPackageInstall){
							installPackage(target.toPath());
						} else {
							executeImportText(target.getCanonicalPath());
						}

					} else if(target.getName().matches("^config.*\\.xml$")){ //$NON-NLS-1$
						// in the case of config files
						updateUserWorkSetting();
						openConfigFile(target.getCanonicalPath());
						readUserSetting2();
					} else {
						JOptionPane.showMessageDialog(Frame1.this, target.getName() + Messages.getString("Frame1.10")); //$NON-NLS-1$
					}
				}
			} catch (UnsupportedFlavorException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		private boolean isHimawariDir(File dir) {
			String hintFilenames[] = {"himawari.exe", "Corpora", "config.xml", "resources"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			
			if(!dir.isDirectory()) {
				return false;
			}
			
			for(String hintFilename : hintFilenames) {
				if(!dir.toPath().resolve(hintFilename).toFile().exists()) {
					return false;
				}
			}
			
			return true;
		}

		
		private boolean isMacApplication(File dir) {
			String hintFilenames[] = {"Contents"}; //$NON-NLS-1$
			
			if(!dir.isDirectory()) {
				return false;
			}
			
			for(String hintFilename : hintFilenames) {
				if(!dir.toPath().resolve(hintFilename).toFile().exists()) {
					return false;
				}
			}
			
			return true;
		}
	}
}

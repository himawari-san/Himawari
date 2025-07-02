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

import java.io.*;
import javax.swing.JOptionPane;
import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.*;

/**
 * <p>タイトル: UserWorkSetting</p>
 * <p>説明:ユーザ設定保存機能の制御を行う</p>
 * <p>著作権: Copyright (c) 2011</p>
 * <p>会社名: トライアックス（株）</p>
 *
 * @author Hajime Osada
 * @version 1.0
 */
public class UserWorkSetting {

	/***************
	 * クラス定数
	 ***************/
	/*--------------
	 *  ROOTノード
	 ---------------*/
	//ユーザ設定
	private static final String ROOT = "user_setting"; //$NON-NLS-1$


	/*--------------
	 *  第1階層
	 ---------------*/
	// ROOT
	private static final String OPTION = "option"; //$NON-NLS-1$
	// History
	private static final String HISTORY = "history"; //$NON-NLS-1$
	// Config
	private static final String CONFIG = "config"; //$NON-NLS-1$

	/*--------------
	 *  第2階層
	 ---------------*/
	// ツール
	private static final String TOOL = "tool"; //$NON-NLS-1$
	// 検索
	private static final String SEARCH = "search"; //$NON-NLS-1$
	// 検索キー
	private static final String KEY = "key"; //$NON-NLS-1$
	// フィルター
	private static final String FILTER = "filter"; //$NON-NLS-1$
	// ファイルパス
	private static final String FILE_PATH = "file_path"; //$NON-NLS-1$

	/*--------------
	 *  第3階層
	 ---------------*/
	// フォントサイズ
	private static final String FONT_SIZE = "font_size"; //$NON-NLS-1$
	// ブラウザー
	private static final String BROWSER = "browser"; //$NON-NLS-1$
	// 閲覧表示スタイル
	private static final String XSL = "xsl"; //$NON-NLS-1$
	// ユーザ設定保存
	private static final String SAVE_SETTING = "save_setting"; //$NON-NLS-1$
	// 前後文脈長
	private static final String LENGTH_OF_CONTEXT = "length_of_context"; //$NON-NLS-1$
	// 検索文字列履歴
	private static final String KEY_STRING = "key_string"; //$NON-NLS-1$
	// 前文脈履歴
	private static final String KEY_PREV = "key_prev"; //$NON-NLS-1$
	// 後文脈履歴
	private static final String KEY_FOL = "key_fol"; //$NON-NLS-1$
	// フィルタ上段履歴
	private static final String FILTER_TOP = "filter_top"; //$NON-NLS-1$
	// フィルタ中段履歴
	private static final String FILTER_MIDDLE = "filter_middle"; //$NON-NLS-1$
	// フィルタ下段履歴
	private static final String FILTER_BOTTOM = "filter_bottom"; //$NON-NLS-1$
	// 言語
	private static final String LANGUAGE = "lang";


	/*--------------
	 * その他定数
	 ---------------*/
	//xmlファイル出力スタイル定義ファイルパス
	private static final String XSL_PATH = "/jp/ac/ninjal/himawari/xsl/style.xsl"; //$NON-NLS-1$

	//int型プロパティーのエラー値
	public static final int INT_ERROR = -1;

	//String型プロパティーのエラー値
	public static final String STR_ERROR = ""; //$NON-NLS-1$

	//配列型プロパティーのエラー値
	public static final String[] ARRAY_ERROR = null;

	// user setting file name (default)
	public final static String USER_WORK_SETTING_FILENAME = "user_setting.xml"; //$NON-NLS-1$


	/***************
	 * プロパティー
	 ***************/

	//ユーザ設定ファイルの存在可否
	public boolean existSettingFile = false;

	//DOM ドキュメント
	private Document doc;

	//ファイル
	private File file;

	// フォントサイズ
	private int font_size;

	// ブラウザー
	private String browser;

	// 閲覧表示スタイル
	private String xsl ;

	// ユーザ設定保存
	private int save_setting;

	// 前後文脈長
	private String length_of_context;

	// 検索文字列履歴
	private String[] key_string;

	// 前文脈履歴
	private String[] key_prev;

	// 後文脈履歴
	private String[] key_fol;

	// フィルタ上段履歴
	private String[] filter_top;

	// フィルタ中段履歴
	private String[] filter_middle;

	// フィルタ下段履歴
	private String[] filter_bottom;

	// configファイルパス
	private String file_path;

	// language in Locale 
	private String language;

	/*****************
	 * public methods
	 * @throws Exception 
	 ******************/

	public UserWorkSetting() throws Exception {
		this(USER_WORK_SETTING_FILENAME);
	}

	/**
	 * コンストラクタ
	 * @param filepath ユーザ設定ファイルパス
	 * @throws Exception 
	 */
	public UserWorkSetting(String filepath) throws Exception {

		// DOMオブジェクトの作成
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();

		//ファイルの作成
		file = new File(filepath);
		if (file.exists()){
			//パース
			doc = db.parse(file);
			//値のセット
			setProperty();
			existSettingFile = true;
		}
	}

	/**
	 * ユーザ設定を保存する
	 * @return
	 */
	public int save() {

		int ret = JOptionPane.YES_OPTION;

		try{
			//domドキュメントのvalueを初期化する
			initSettingDom();

			//DOMに値をセット
			setAllValueDom();

			//インデントをつけるスタイルシート
			InputStream is = getClass().getResourceAsStream(XSL_PATH);

	        //DOMオブジェクトを文字列として出力
	        TransformerFactory tfactory = TransformerFactory.newInstance();
	        Source xslSource = new StreamSource(is);
	        Transformer transformer = tfactory.newTransformer(xslSource);

	        //encoding="UTF-8"を指定
	        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$

	        //ファイルに出力
	        transformer.transform(new DOMSource(doc), new StreamResult(file));

		}catch(Exception e){
			e.printStackTrace();
			//エラー時は警告ダイアログを表示する
			ret = JOptionPane.showConfirmDialog(
					null,//親オブジェクト
					Messages.getString("Frame1.515"),//メッセージ //$NON-NLS-1$
					Messages.getString("Frame1.369"),//タイトル //$NON-NLS-1$
					JOptionPane.YES_NO_OPTION);
		}
		return ret;
	}



	/*****************
	 * private methods
	 ******************/

	/**
	 * DOMにこのオブジェクトのプロパティの値をセットする
	 */
	private void setAllValueDom() {

		setValueDom(FONT_SIZE,TOOL, font_size);
		setValueDom(BROWSER,TOOL, browser);
		setValueDom(XSL, TOOL, xsl);
		setValueDom(SAVE_SETTING, TOOL, save_setting);
		setValueDom(LANGUAGE, TOOL, language == null ? LocaleUtil.getLanguage() : language);
		setValueDom(LENGTH_OF_CONTEXT, SEARCH, length_of_context);
		setValueDom(KEY_STRING, KEY, key_string);
		setValueDom(KEY_PREV, KEY, key_prev);
		setValueDom(KEY_FOL, KEY, key_fol);
		setValueDom(FILTER_TOP, FILTER, filter_top);
		setValueDom(FILTER_MIDDLE, FILTER, filter_middle);
		setValueDom(FILTER_BOTTOM, FILTER, filter_bottom);
		setValueDom(FILE_PATH, CONFIG, file_path);

	}

	/**
	 * DomにString型のデータをセットする
	 * @param target 追加するノード名
	 * @param parent 親ノード名
	 * @param value 追加する値
	 */
	private void setValueDom(String target, String parent, String value){

		//タグの生成
		Element element = doc.createElement(target);

		//値の追加
		Text data = doc.createTextNode(value);
		element.appendChild(data);

		//親に要素を追加
		doc.getElementsByTagName(parent).item(0).appendChild(element);
	}


	/**
	 * Domにint型のデータをセットする
	 * @param target 追加するノード名
	 * @param parent 親ノード名
	 * @param value 追加する値
	 */
	private void setValueDom(String target, String parent, int value) {
		setValueDom(target,parent,String.valueOf(value));
	}


	/**
	 * DomにString型配列のデータをセットする
	 * @param target 追加するノード名
	 * @param parent 親ノード名
	 * @param value 追加する値
	 */
	private void setValueDom(String target, String parent, String[] value){
		if (value == null) return;
		for(int i = 0; i < value.length; i++){
			setValueDom(target,parent,value[i]);
		}
	}


	/**
	 * dom設定の初期化
	 * @throws ParserConfigurationException
	 */
	private void initSettingDom() throws ParserConfigurationException {

		// DOMオブジェクトの作成
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();

		// docの初期化
		doc = db.newDocument();

		//domオブジェクトの枠組みを作成
		createDomFrame();
	}


	/**
	 * ユーザ設定保存ファイルの枠組みを生成
	 */
	private void createDomFrame() {

		/**
		 * 要素の作成
		 */
		Element root = doc.createElement(ROOT);
		Element option = doc.createElement(OPTION);
		Element history = doc.createElement(HISTORY);
		Element config = doc.createElement(CONFIG);
		Element tool = doc.createElement(TOOL);
		Element search = doc.createElement(SEARCH);
		Element key = doc.createElement(KEY);
		Element filter = doc.createElement(FILTER);

		/**
		 * 要素の追加
		 */
		doc.appendChild(root);
		root.appendChild(option);
		root.appendChild(history);
		root.appendChild(config);
		option.appendChild(tool);
		option.appendChild(search);
		history.appendChild(key);
		history.appendChild(filter);
	}



	/**
	 * Documentから値を取得し、プロパティーにセットする
	 * @throws Exception
	 */
	public void setProperty() throws Exception {

		/**
		 * 値を自分にセットする（取得できない場合はエラー値）
		 */

		font_size = getInt(FONT_SIZE) != 0 ? getInt(FONT_SIZE): INT_ERROR ;
		browser = getString(BROWSER) != null ? getString(BROWSER): STR_ERROR;
		xsl = getString(XSL) != null ? getString(XSL): STR_ERROR;
		save_setting = getInt(SAVE_SETTING) != 0 ? getInt(SAVE_SETTING):INT_ERROR ;
		language = getString(LANGUAGE) != null ? getString(LANGUAGE): STR_ERROR;
		length_of_context = getString(LENGTH_OF_CONTEXT) != null ? getString(LENGTH_OF_CONTEXT): STR_ERROR;
		key_string = getStringArray(KEY_STRING) != null ? getStringArray(KEY_STRING):ARRAY_ERROR ;
		key_prev = getStringArray(KEY_PREV) != null ? getStringArray(KEY_PREV):ARRAY_ERROR;
		key_fol = getStringArray(KEY_FOL) != null ? getStringArray(KEY_FOL) :ARRAY_ERROR ;
		filter_top = getStringArray(FILTER_TOP) != null ? getStringArray(FILTER_TOP): ARRAY_ERROR;
		filter_middle = getStringArray(FILTER_MIDDLE) != null ? getStringArray(FILTER_MIDDLE): ARRAY_ERROR;
		filter_bottom = getStringArray(FILTER_BOTTOM) != null ? getStringArray(FILTER_BOTTOM): ARRAY_ERROR;
		file_path = getString(FILE_PATH) != null ? getString(FILE_PATH): STR_ERROR;

	}


	/**
	 * DOMから指定されたkeyの数値（int型）を取得する
	 * @param key
	 * @return 取得したデータ：値が取得できない場合0
	 */
	private int getInt(String key) {
		int ret = 0;
		try{
			ret = getString(key) != null ? Integer.parseInt(getString(key)) : 0;
		}catch(NumberFormatException e){
			ret = 0;
		}
		return ret;
	}


	/**
	 * DOMから指定されたkeyの文字列を取得する
	 *
	 * @param key キー
	 * @return 取得したデータ：値が取得できない場合null
	 */
	private String getString(String key) {
		String ret = null;
		try {
			ret = doc.getElementsByTagName(key).item(0).getTextContent();
		} catch (NullPointerException ex) {
			return null;
		}
		return ret;
	}


	/**
	 * DOMから指定されたkeyの配列を取得する
	 * @param key
	 * @return 取得したデータ：値が取得できない場合null
	 */
	private String[] getStringArray(String key) throws DOMException{

		NodeList list = doc.getElementsByTagName(key);
		String ret[] = new String[list.getLength()];

		try{
			for(int i = 0; i < list.getLength(); i++){
				ret[i] = list.item(i).getTextContent();
			}
		}catch(DOMException de){
			throw de;
		}
		return ret;

	}



	/***********************
	 * Getter & Setters
	 ***********************/

	/**
	 * @return font_size
	 */
	public int getFont_size() {
		return font_size;
	}




	/**
	 * @param fontSize セットする font_size
	 */
	public void setFont_size(int fontSize) {
		font_size = fontSize;
	}




	/**
	 * @return xsl
	 */
	public String getXsl() {
		return xsl;
	}




	/**
	 * @param xsl セットする xsl
	 */
	public void setXsl(String xsl) {
		this.xsl = xsl;
	}




	/**
	 * @return save_setting
	 */
	public int getSave_setting() {
		return save_setting;
	}


	/**
	 * @return language
	 */
	public String getLanguage() {
		return language;
	}

	
	/**
	 * @param language セットする language
	 */
	public void setLanguage(String language) {
		this.language = language;
	}


	/**
	 * @param saveSetting セットする save_setting
	 */
	public void setSave_setting(int saveSetting) {
		save_setting = saveSetting;
	}




	/**
	 * @return length_of_context
	 */
	public String getLength_of_context() {
		return length_of_context;
	}




	/**
	 * @param lengthOfContext セットする length_of_context
	 */
	public void setLength_of_context(String lengthOfContext) {
		length_of_context = lengthOfContext;
	}




	/**
	 * @return key_string
	 */
	public String[] getKey_string() {
		return key_string;
	}




	/**
	 * @param keyString セットする key_string
	 */
	public void setKey_string(String[] keyString) {
		key_string = keyString;
	}




	/**
	 * @return key_prev
	 */
	public String[] getKey_prev() {
		return key_prev;
	}




	/**
	 * @param keyPrev セットする key_prev
	 */
	public void setKey_prev(String[] keyPrev) {
		key_prev = keyPrev;
	}




	/**
	 * @return key_fol
	 */
	public String[] getKey_fol() {
		return key_fol;
	}




	/**
	 * @param keyFol セットする key_fol
	 */
	public void setKey_fol(String[] keyFol) {
		key_fol = keyFol;
	}




	/**
	 * @return filter_top
	 */
	public String[] getFilter_top() {
		return filter_top;
	}




	/**
	 * @param filterTop セットする filter_top
	 */
	public void setFilter_top(String[] filterTop) {
		filter_top = filterTop;
	}




	/**
	 * @return filter_middle
	 */
	public String[] getFilter_middle() {
		return filter_middle;
	}




	/**
	 * @param filterMiddle セットする filter_middle
	 */
	public void setFilter_middle(String[] filterMiddle) {
		filter_middle = filterMiddle;
	}




	/**
	 * @return filter_bottom
	 */
	public String[] getFilter_bottom() {
		return filter_bottom;
	}




	/**
	 * @param filterBottom セットする filter_bottom
	 */
	public void setFilter_bottom(String[] filterBottom) {
		filter_bottom = filterBottom;
	}




	/**
	 * @param config_file_path セットする config_file_path
	 */
	public void setConfig_file_path(String file_path) {
		this.file_path = file_path;
	}




	/**
	 * @return config_file_path
	 */
	public String getConfig_file_path() {
		return file_path;
	}

	/**
	 * @param browser セットする browser
	 */
	public void setBrowser(String browser) {
		this.browser = browser;
	}

	/**
	 * @return browser
	 */
	public String getBrowser() {
		return browser;
	}




}
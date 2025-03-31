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

/**
 * @(#)SearchEngine.java	ver.1.0, 2005-06-05
 *
 * Copyright 2003-2005
 * National Institute for Japanese Language All rights reserved.
 */

package jp.ac.ninjal.himawari;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.io.*;
import java.nio.BufferUnderflowException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;


/**
 *
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003-2005
 * </p>
 * <p>
 * 会社名:
 * </p>
 *
 * @author Masaya YAMAGUCHI
 * @version 0.9.4
 */

public class SearchEngineWorker extends SwingWorker<Object, Object> {

	/***********************
	 * ステータス定数
	 ***********************/
	// 検索OK
	public static final int SEARCH_OK = 1;

	// 検索NG
	public static final int SEARCH_NG = 2;
	
	public static final String MESSAGE_DELIMITER = "\t"; //$NON-NLS-1$

	/***********************
	 * 検索モード定数
	 ***********************/
	// 通常検索
	public static final int MODE_NORMAL = 1;

	// ランダム検索
	public static final int MODE_RAMDOM = 2;

	// 頻度計測のみ(総計)
	public static final int MODE_COUNT = 3;
	// 頻度計測のみ(一覧)
	public static final int MODE_SUMMARY = 4;

	/*************************
	 * プロパティ
	 *************************/

	private CorpusFile corpus[];
	private CorpusFile nonTargetCorpus[];
	private int browsedElementNumber;

	// index of the array of ElementIndex for corpus element
	// not defined: -1
	private int iCorpusElemenet;
	private String corpusElementName;

	private ArrayList<ResultRecord> tempRecordSet;
	// private Vector resultRecordSet;

	// private FieldInfo fieldInfo;

	private String searchTarget[];
	private ArrayList<String> storedFieldNames;
	private String storedFieldName = null;

	// 検索ステータスフラグ
	private int status = SEARCH_OK;

	// 検索モード
	private int mode;

	// 結果テーブル
	private ResultTable selectedTable;

	// ステータスバー
	private JLabel statusBar;

	// レコードベースフラグ
	private Boolean isRecordBased;

	/***********************
	 * Search Conditions
	 ***********************/

	private ArrayList<String> keys;
	private String keyHead;
	private String keyTail;
	private int length_context_kwic;
	private int length_context_search;
	private Filter filter;
	private boolean isCatPreviousContext;
	private boolean isCatFollowingContext;
	private FieldInfo fieldInfo;
	private String searchTargetName;
	private ArrayList<ResultRecord> resultRecordSet;
	// 検索上限
	private int limit = -1;
	// サンプル数
	private int sample = 0;
	// 検索ヒット件数
//	private int intResult = 0;
	private RecordStatistics rs = null;
	
	private Frame1 frame;
	private String errorMessage = ""; //$NON-NLS-1$

	private UserSettings config;
	

	public SearchEngineWorker(Frame1 frame){
		tempRecordSet = new ArrayList<ResultRecord>();
		this.frame = frame;
	}

	/**
	 * 非同期に行われる処理
	 */
	@Override
	public Object doInBackground() {
		try {

			// 開始時間取得
			long start = System.currentTimeMillis();

			// 検索実行
			doSearch();

			/**
			 * 終了メッセージ
			 */
			// エラーが存在する場合
			if (errorMessage.length() > 0) {
				JOptionPane.showMessageDialog(null, errorMessage, Messages
						.getString("Frame1.272"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
			}
			// 結果データセットがnullの場合
			else if (resultRecordSet == null) {
				statusBar.setText(Messages.getString("Frame1.270")); //$NON-NLS-1$
				JOptionPane.showMessageDialog(null, Messages
						.getString("Frame1.271"), //$NON-NLS-1$
						Messages.getString("Frame1.272"), //$NON-NLS-1$
						JOptionPane.ERROR_MESSAGE);
			}

			// 正常終了
			else {

				long end = System.currentTimeMillis();
				System.err.println("time[milisec]: " + (end - start)); //$NON-NLS-1$
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
			System.err.println("search processing was canceled"); //$NON-NLS-1$
		} catch (Exception e) {
			e.printStackTrace();
			ErrorMsg error = new ErrorMsg(e);
			JOptionPane.showMessageDialog(null, error.getMessage(), Messages
					.getString("Frame1.272"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
			statusBar.setText(""); //$NON-NLS-1$
		} finally{
			//終了後にメモリの開放
			tempRecordSet.clear();
		}
		return null;
	}

	/**
	 * 非同期処理後に実行
	 */
	@Override
	public void done() {

		//検索後のアクションを実行
		frame.doneSearchAction();

		// 検索フラグの変更
		status = SEARCH_OK;

	}

	public void init(UserSettings config) {
		this.config = config;
		iCorpusElemenet = -1; // default
		corpusElementName = ""; // default //$NON-NLS-1$

		// コーパス関連の設定
		String[] corpusNames = config.getAttributeList("corpora", "name"); //$NON-NLS-1$ //$NON-NLS-2$
		String[] pathForCorpus = config.getAttributeList("corpora", "path"); //$NON-NLS-1$ //$NON-NLS-2$
		String[] isSelectedCorpus = config.getAttributeList(
				"corpora", "isSelected"); //$NON-NLS-1$ //$NON-NLS-2$

		// cix 関連の設定
		String[] cixElementNames = config.getAttributeList("index_cix", "name"); //$NON-NLS-1$ //$NON-NLS-2$
		String[] cixMiddleNames = config.getAttributeList(
				"index_cix", "middle_name"); //$NON-NLS-1$ //$NON-NLS-2$
		String[] cixName = config.getAttributeList("index_cix", "label"); //$NON-NLS-1$ //$NON-NLS-2$
		String[] cixElementType = config.getAttributeList("index_cix", "type"); //$NON-NLS-1$ //$NON-NLS-2$
		String[] stopElement = config.getAttributeList(
				"index_cix", "stop_element"); //$NON-NLS-1$ //$NON-NLS-2$
		String[] cixFieldNames = config.getAttributeList(
				"index_cix", "field_name"); //$NON-NLS-1$ //$NON-NLS-2$

		// eix 関連の設定
		String[] eixElementNames = config.getAttributeList("index_eix", "name"); //$NON-NLS-1$ //$NON-NLS-2$
		String[] eixMiddleNames = config.getAttributeList(
				"index_eix", "middle_name"); //$NON-NLS-1$ //$NON-NLS-2$
		String[] eixIsEmpty = config.getAttributeList("index_eix", "is_empty"); //$NON-NLS-1$ //$NON-NLS-2$
		String[] eixIsBrowsed = config.getAttributeList(
				"index_eix", "isBrowsed"); //$NON-NLS-1$ //$NON-NLS-2$
		String[] eixTop = config.getAttributeList("index_eix", "top"); //$NON-NLS-1$ //$NON-NLS-2$

		// aix 関連の設定
		String[] aixElementNames = config.getAttributeList("index_aix", "name"); //$NON-NLS-1$ //$NON-NLS-2$
		String[] aixMiddleNames = config.getAttributeList(
				"index_aix", "middle_name"); //$NON-NLS-1$ //$NON-NLS-2$
		String[] aixArgument = config.getAttributeList("index_aix", "argument"); //$NON-NLS-1$ //$NON-NLS-2$
		String[] aixName = config.getAttributeList("index_aix", "label"); //$NON-NLS-1$ //$NON-NLS-2$
		String[] aixElementType = config.getAttributeList("index_aix", "type"); //$NON-NLS-1$ //$NON-NLS-2$
		String[] aixFieldNames = config.getAttributeList(
				"index_aix", "field_name"); //$NON-NLS-1$ //$NON-NLS-2$
		String[] aixIsCompleteMatchs = config.getAttributeList(
				"index_aix", "isCompleteMatch"); //$NON-NLS-1$ //$NON-NLS-2$

		// 検索キー
		searchTarget = new String[aixElementNames.length
				+ cixElementNames.length];
		storedFieldNames = new ArrayList<String>();
		for (int i = 0; i < cixElementNames.length; i++) {
			searchTarget[i] = cixName[i];
			storedFieldNames.add(cixFieldNames[i]);
		}
		for (int i = 0; i < aixElementNames.length; i++) {
			searchTarget[i + cixElementNames.length] = aixName[i];
			storedFieldNames.add(aixFieldNames[i]);
		}

		// 検索対象のテーブル
		HashSet<String> requiredDBTableName = new HashSet<String>();
		for(int i = 0; i < fieldInfo.size(); i++){
			if(fieldInfo.getType(i).equalsIgnoreCase(FieldInfo.TYPE_DB)){
				requiredDBTableName.add(fieldInfo.getElementName(i));
			}
		}

//		ArrayList<String> dicElements = new ArrayList<String>;
		
		
		
		
		// 登録コーパス数
		int nCorpus = corpusNames.length;
		corpus = new CorpusFile[nCorpus];

		for (int i = 0; i < nCorpus; i++) {
			corpus[i] = new CorpusFile(pathForCorpus[i], corpusNames[i]);

			if (isSelectedCorpus[i].equals("false")) { //$NON-NLS-1$
				corpus[i].select(false);
			} else {
				corpus[i].select(true);
			}

			// 各種インデックス設定
			// cix
			for (int j = 0; j < cixElementNames.length; j++) {
				if (cixElementType[j].equals("record_based")) { //$NON-NLS-1$
					if (stopElement[j].isEmpty()) {
						corpus[i].setCixRecordBased(cixElementNames[j],
								cixMiddleNames[j]);
					} else {
						corpus[i].setCixRecordBased(cixElementNames[j],
								cixMiddleNames[j], stopElement[j]);
					}
				} else if (cixElementType[j].equals("null")){ //$NON-NLS-1$
					corpus[i].setCixNull(cixElementNames[j]);
				} else if (cixElementType[j].equals("db")){ //$NON-NLS-1$
					corpus[i].setCixDB(cixElementNames[j], cixMiddleNames[j]);
				} else {
					if (stopElement[j].isEmpty()) {
						corpus[i].setCix(cixElementNames[j], cixMiddleNames[j]);
					} else {
						corpus[i].setCix(cixElementNames[j], cixMiddleNames[j],
								stopElement[j]);
					}
				}
			}
			// eix
			for (int j = 0; j < eixElementNames.length; j++) {
				corpus[i].setEix(eixElementNames[j], eixMiddleNames[j],
						eixIsEmpty[j]);
				String valueIsBrowsed = eixIsBrowsed[j];
				String valueTop = eixTop[j];
				if (valueIsBrowsed.equals("true")) { //$NON-NLS-1$
					browsedElementNumber = j;
				}
				if (valueTop.equals("true")) { //$NON-NLS-1$
					iCorpusElemenet = j;
					corpusElementName = eixElementNames[j];
				}
			}
			

			// aix
			for (int j = 0; j < aixElementNames.length; j++) {
				if (aixElementType[j].equals("record_based")) { //$NON-NLS-1$
					corpus[i].setAixRecordBased((String) aixElementNames[j],
							aixMiddleNames[j], aixArgument[j], Boolean.valueOf(aixIsCompleteMatchs[j]));
				}
				//DB検索
				else if(aixElementType[j].equalsIgnoreCase("db")){ //$NON-NLS-1$
					corpus[i].setAixDB((String) aixElementNames[j], aixArgument[j],
							Boolean.valueOf(aixIsCompleteMatchs[j]));
				}
				// six 検索
				else if(aixElementType[j].equalsIgnoreCase("dic")){ //$NON-NLS-1$
					corpus[i].setAixDIC((String) aixElementNames[j], aixArgument[j],
							Boolean.valueOf(aixIsCompleteMatchs[j]), frame.getDic(aixElementNames[j]));
				} else {
					corpus[i].setAix((String) aixElementNames[j],
							aixMiddleNames[j], aixArgument[j], Boolean.valueOf(
									aixIsCompleteMatchs[j]).booleanValue());
				}
			}
			
			// DB
			Iterator<String> it = requiredDBTableName.iterator();
			while(it.hasNext()){
				String tableName = it.next();
				corpus[i].setEixDB(tableName);
			}
			
			// dic
			HashSet<String> t = new HashSet<String>(); 
			for(int j = 0; j < fieldInfo.size(); j++){
				if(fieldInfo.getType(j).equals("dic")){ //$NON-NLS-1$
					String elementNamePS = fieldInfo.getElementName(j);
					String elementName = elementNamePS.replaceFirst("(.+)\\[-?\\d+\\]$", "$1"); //$NON-NLS-1$ //$NON-NLS-2$
					if(!t.contains(elementName)){
						t.add(elementName);
						corpus[i].setEixDIC(elementName, frame.getDic(elementName));
					}
				}
			}
		}
	}

	public CorpusFile[] getCorpus() {
		return corpus;
	}

	public String[] getSearchTarget() {
		return searchTarget;
	}

	public String getStoredFieldName() {
		return storedFieldName;
	}

	public void setError(String message) {
		errorMessage = message;
	}

	/**
	 * 検索の実行
	 *
	 * @return 検索総数
	 */
	public int doSearch() throws InterruptedException, Exception {

		System.err.println("doSearch:start"); //$NON-NLS-1$

		KWICMaker kwicMaker;
		resultRecordSet.clear();
		ArrayList<ContentsIndex> cixVector;
		ArrayList<ArgumentIndex> aixVector;
		ContentsIndex cix = null;
		ArgumentIndex aix = null;
		Index index = null;
		String targetIndexName = null;
		int c = 0;
		HashMap<Integer, Integer> sampleMap = new HashMap<Integer, Integer>();

		try {
			/**
			 * ランダム検索
			 */
			if (mode == MODE_RAMDOM) {

				rs = executeCountOnly();
				RecordStatistics rs = executeCountOnly();
				
				if (rs == null)
					return 0;

				if (sample > rs.size()) {
					sample = rs.size();
				}

				for (int i = 0; i < sample; i++) {
					int rnd = (int) (Math.random() * (rs.size() - i));
					int p = 0;
					Integer marker = null;
					while (rnd >= 0) {
						marker = Integer.valueOf(p);
						if (sampleMap.get(marker) == null) {
							rnd--;
						}
						p++;
					}
					sampleMap.put(marker, marker);
				}
			}

			/**
			 * 件数のみ取得
			 */
			if (mode == MODE_COUNT || mode == MODE_SUMMARY) {
				rs = executeCountOnly();
				statusBar.setText(getStatusMessage());

				if(rs.hasTable() || mode == MODE_SUMMARY){
					SearchResultFrame sf = frame.getSearchResultFrame();
					sf.setFieldInfo(rs.getFieldInfo());
					sf.invokeStatisticsFrame(rs.getResults(), selectedTable.getFont().getSize());
				} else {
					JOptionPane.showMessageDialog(null, Messages
							.getString("Frame1.250") //$NON-NLS-1$
							+ rs.size() + Messages.getString("Frame1.251"), //$NON-NLS-1$ //$NON-NLS-2$
							Messages.getString("Frame1.252"), //$NON-NLS-1$
							JOptionPane.INFORMATION_MESSAGE);
				}

				return rs.size();
			}

			// キーが指定されていない場合
			if (keys == null || keys.size() < 1) {
				ErrorMsg error = new ErrorMsg(new Exception(),
						ErrorMsg.ERR_GUI_INPUT_NULL, null);
				setError(error.getMessage());

				return -1;
			}

			if (length_context_kwic < 0) {
				length_context_kwic = 10;
			}
			if (length_context_search < 0) {
				length_context_search = 10;
			}

			long start = System.currentTimeMillis();

			for (int i = 0; i < corpus.length; i++) {
				if (isCancelled()){
					throw new InterruptedException();
				}


				if (!corpus[i].isSelected()) {
					continue;
				}

				// コーパス初期化
				try {
					corpus[i].init();
				} catch (IOException e) {
					e.printStackTrace();
					ErrorMsg error = new ErrorMsg(e, ErrorMsg.ERR_COURPUS_OPEN,
							corpus[i].getIOFilename());
					setError(error.getMessage());

					return -1;
				}

				// Get indexes
				cixVector = corpus[i].getCixVector();
				aixVector = corpus[i].getAixVector();

				// 検索キーの選択
				for (int l = 0; l < searchTarget.length; l++) {
					if (isCancelled()){
						throw new InterruptedException();
					}

					if (searchTargetName.equals(searchTarget[l])) {
						storedFieldName = (String) storedFieldNames.get(l);
						if (l < cixVector.size()) {
							cix = cixVector.get(l);
							if (cix.getClass().getSimpleName().equals("RecordBasedContentsIndex")) { //$NON-NLS-1$
								((RecordBasedContentsIndex) cix).setRetrieveCondition(fieldInfo, storedFieldName, keyHead, keyTail);
							} else {
								cix.setRetrieveCondition(fieldInfo,	storedFieldName);
							}
							index = cix;
							targetIndexName = cix.getElementName();
						} else {
							aix = aixVector.get(l - cixVector.size());
							if (aix.getClass().getSimpleName().equals("RecordBasedArgumentIndex")) { //$NON-NLS-1$
								((RecordBasedArgumentIndex) aix).setRetrieveCondition(fieldInfo, storedFieldName, keyHead, keyTail);
							} else {
								aix.setRetrieveCondition(fieldInfo, storedFieldName);
							}
							index = aix;
							targetIndexName = aix.getElementName();
						}
						index.setFilter(filter);
						break;
					}
				}


				// get iNeedList which stores indexes of fieldInfo used later 
				ArrayList<Integer> iNeededList = new ArrayList<Integer>(); 
				HashSet<String> elementNameList = new HashSet<String>();
				for (int k = 0; k < fieldInfo.size(); k++) {
					String elementName = fieldInfo.getElementName(k);
					String fieldType = fieldInfo.getType(k);

					if(elementName.startsWith("_") || elementName.isEmpty() || elementName.equals(index.getProcessedElementName())){ //$NON-NLS-1$
						continue;
					}
					
					if (fieldType.equals("relative") || fieldType.equals("sibling")){ //$NON-NLS-1$ //$NON-NLS-2$
						iNeededList.add(k);
					} else {
						if(elementNameList.contains(elementName)){
							continue;
						} else {
							elementNameList.add(elementName);
						}
						iNeededList.add(k);
					}
				}

				// set values to arrays
				String elementNames[] = new String[iNeededList.size()];
				String fieldTypes[] = new String[iNeededList.size()];
				String attributeNames[] = new String[iNeededList.size()];
				int elementOffsets[] = new int[iNeededList.size()];
				ElementIndex elementIndexes[] = new ElementIndex[iNeededList.size()];
				elementNameList.clear();
				int ne = 0;
				for (int k : iNeededList) {
					elementNames[ne] = fieldInfo.getElementName(k);
					fieldTypes[ne] = fieldInfo.getType(k);
					attributeNames[ne] = fieldInfo.getAttributeName(k);
					if(elementNames[ne].endsWith("]")){ //$NON-NLS-1$
						elementOffsets[ne] = Integer.parseInt(elementNames[ne].substring(elementNames[ne].indexOf('[') + 1, elementNames[ne].length() - 1));
						elementNames[ne] = elementNames[ne].replaceFirst("\\[.+?\\]$", ""); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						elementOffsets[ne] = 0;
					}
					if(!elementNames[ne].isEmpty() && !elementNameList.contains(elementNames[ne])){
						elementNameList.add(elementNames[ne]);
					}
					ne++;
				}

				// set elementIndexes
				for(int k = 0; k < ne; k++){
					if(fieldTypes[k].equals("relative") || fieldTypes[k].equals("sibling")){ //$NON-NLS-1$ //$NON-NLS-2$
						elementIndexes[k] = corpus[i].getEix(targetIndexName);
					} else {
						elementIndexes[k] = corpus[i].getEix(elementNames[k]);
					}
				}
				
				try {
					index.open();
				} catch (IOException ex) {
					ex.printStackTrace();
					ErrorMsg error = new ErrorMsg(ex,
							ErrorMsg.ERR_INDEX_OPEN_OTHER, index
									.getIOFilename());
					setError(error.getMessage());
					return -1;
				}

				try {
					System.err.println("open eix"); //$NON-NLS-1$
					corpus[i].openEix(elementNameList.toArray(new String[0]));
				} catch (IOException e) {
					e.printStackTrace();
					ErrorMsg error = new ErrorMsg(e,
							ErrorMsg.ERR_INDEX_OPEN_EIX, corpus[i]
									.getIOFilename());
					setError(error.getMessage());

					return -1;
				}
				
				kwicMaker = new KWICMaker(filter, length_context_kwic,
						length_context_search, isCatPreviousContext,
						isCatFollowingContext, storedFieldName);
				kwicMaker.setCorpus(corpus[i]);

				ResultRecord resultRecord = null;

				for (int j = 0; j < keys.size(); j++) {

					if (isCancelled()){
						throw new InterruptedException();
					}

					tempRecordSet.clear();

					// Retrieve the key from the specified contents of XML
					// element
					resultRecord = index.retrieveFirst(keys.get(j));


					while (resultRecord != null) {
						if (isCancelled()){
							throw new InterruptedException();
						}

						resultRecord = kwicMaker.makeUnderConstraint(resultRecord);
						
						// 属性, next, sibling 検索
						for (int k = 0; k < ne && resultRecord != null; k++) {
							if (isCancelled()){
								throw new InterruptedException();
							}
							
							String fieldType = fieldTypes[k];
							String elementName = elementNames[k];
							String attributeName = attributeNames[k];
							int elementOffset = elementOffsets[k];
							ElementIndex eix = elementIndexes[k];

							if (fieldType.equals("argument") || //$NON-NLS-1$
									((fieldType.equals("db")||fieldType.equals("dic")) && elementOffset == 0)) { //$NON-NLS-1$ //$NON-NLS-2$
								resultRecord = eix.addAttribute(resultRecord, filter);
							} else if(fieldType.equals("dic") && elementOffset != 0){ //$NON-NLS-1$
								resultRecord = eix.resistNextElement(resultRecord, elementOffset, attributeName, filter);
							} else if(fieldType.equals("db") && elementOffset != 0){ //$NON-NLS-1$
								resultRecord = eix.resistNextElement(resultRecord, elementOffset, attributeName, filter);
							} else if (fieldType.equals("relative")) { //$NON-NLS-1$
								resultRecord = eix.resistNextElement(resultRecord, elementOffset, attributeName, filter);
							} else if (fieldType.equals("sibling")) { //$NON-NLS-1$
								resultRecord = eix.resistSiblingElement(resultRecord, elementName, attributeName, filter);
							}
						}

						if (resultRecord != null) {
							if (mode == MODE_NORMAL) {
								if (c >= limit && limit != -1) {
									j = Integer.MAX_VALUE - 1; // exit from
									// the for
									// loop
									break;
								}
								if (resultRecord != null) {
									// Register the corpus number to each
									// ResultRecords
									resultRecord.setResourceID(i);
									resultRecord.setResourceName(corpus[i].getCorpusName());
									tempRecordSet.add(resultRecord);
								}
								c++;
							} else if (mode == MODE_RAMDOM) {
								// Register the corpus number to each
								// ResultRecords
								if (sampleMap.containsKey(c)) {
									resultRecord.setResourceID(i);
									resultRecord.setResourceName(corpus[i].getCorpusName());
									tempRecordSet.add(resultRecord);
								}
								c++;
							}
						}
						resultRecord = index.retrieveNext();
					}
					resultRecordSet.addAll(tempRecordSet);
					System.err.println("n:" + resultRecordSet.size()); //$NON-NLS-1$
				}

				// Close corpusFile
				try {
					if (corpus[i] != null)
						corpus[i].close();
					
				} catch (IOException e) {
					e.printStackTrace();

					ErrorMsg error = new ErrorMsg(e,
							ErrorMsg.ERR_COURPUS_CLOSE, corpus[i]
									.getIOFilename());
					setError(error.getMessage());

					return -1;
				}
				try {
					if (index != null)
						index.close();
				} catch (IOException e) {

					e.printStackTrace();

					ErrorMsg error = new ErrorMsg(e, ErrorMsg.ERR_INDEX_CLOSE,
							index.getIOFilename());
					setError(error.getMessage());
					return -1;
				}
			}
			
			// 結果テーブルにデータをセット
			((ResultTableModel)selectedTable.getModel()).setData(resultRecordSet);
			// 結果テーブルをソート
			((ResultTableModel)selectedTable.getModel()).sortDataWithMultiColumn();

			// ステータスバーに結果を表示
			statusBar.setText(getStatusMessage());

			if (resultRecordSet == null) {
				//setError(Messages.getString("SearchEngine.71")); //$NON-NLS-1$
				ErrorMsg error = new ErrorMsg(new Exception(),
						ErrorMsg.ERR_GUI_INPUT_INVALID, null);
				setError(error.getMessage());

				return -1;
			} else {
				long end = System.currentTimeMillis();
				System.err.println("time[milisec]: " + (end - start)); //$NON-NLS-1$
				// return 0;
				return c;
			}
		} catch (OutOfMemoryError e) {

			//メモリを占有しているオブジェクトをクリア
			resultRecordSet.clear();
			tempRecordSet.clear();

			e.printStackTrace();

			//エラーメッセージのセット
			setError(Messages.getString("SearchEngine.139")); //$NON-NLS-1$
			statusBar.setText(""); //$NON-NLS-1$

			return -1;
		}

	}

	/**
	 * ステータスバーに表示するメッセージを取得する
	 *
	 * @return ステータスメッセージ
	 */
	private String getStatusMessage() {
		String ret = ""; //$NON-NLS-1$

		// record based
		if (isRecordBased) {
			if (mode == MODE_NORMAL) {
				ret = Messages.getString("Frame1.242") //$NON-NLS-1$
						+ String.valueOf(resultRecordSet.size());
			} else if (mode == MODE_RAMDOM) {
				ret = Messages.getString("Frame1.246") //$NON-NLS-1$
						+ String.valueOf(resultRecordSet.size())
						+ //$NON-NLS-1$
						", " + Messages.getString("Frame1.248") //$NON-NLS-1$ //$NON-NLS-2$
						+ String.valueOf(rs.size()); //$NON-NLS-1$ //$NON-NLS-2$
			} else if (mode == MODE_COUNT || mode == MODE_SUMMARY) {
				ret = Messages.getString("Frame1.249") + rs.size(); //$NON-NLS-1$
			}
			// not record based
		} else {
			if (mode == MODE_NORMAL) {
				ret = Messages.getString("Frame1.259") //$NON-NLS-1$
						+ String.valueOf(resultRecordSet.size()); //$NON-NLS-1$
			} else if (mode == MODE_RAMDOM) {
				ret = Messages.getString("Frame1.263") //$NON-NLS-1$
						+ String.valueOf(resultRecordSet.size())
						+ //$NON-NLS-1$
						", " + Messages.getString("Frame1.265") //$NON-NLS-1$ //$NON-NLS-2$
						+ String.valueOf(rs.size()); //$NON-NLS-1$ //$NON-NLS-2$
			} else if (mode == MODE_COUNT || mode == MODE_SUMMARY) {
				ret = Messages.getString("Frame1.266") + rs.size(); //$NON-NLS-1$
			}

		}
		return ret;
	}

	/**
	 * 検索条件にあうレコード数を取得する
	 *
	 * @return 取得件数
	 * @throws InterruptedException
	 */
	public RecordStatistics executeCountOnly() throws InterruptedException {

		KWICMaker kwicMaker;
		RecordStatistics rs = new RecordStatistics(selectedTable.getSelectedFieldInfo());
		resultRecordSet.clear();
		ArrayList<ContentsIndex> cixVector;
		ArrayList<ArgumentIndex> aixVector;
		ContentsIndex cix = null;
		ArgumentIndex aix = null;
		Index index = null;
		String targetIndexName = null;

		// キーが指定されていない場合
		if (keys == null || keys.size() < 1) {
			ErrorMsg error = new ErrorMsg(new Exception(),
					ErrorMsg.ERR_GUI_INPUT_NULL, null);
			setError(error.getMessage());

			return null;
		}

		if (length_context_kwic < 0) {
			length_context_kwic = 10;
		}
		if (length_context_search < 0) {
			length_context_search = 10;
		}

		long start = System.currentTimeMillis();

		for (int i = 0; i < corpus.length; i++) {
			if (isCancelled()){
				throw new InterruptedException();
			}


			if (!corpus[i].isSelected()) {
				continue;
			}

			// コーパス初期化
			try {
				corpus[i].init();
			} catch (IOException e) {
				e.printStackTrace();
				ErrorMsg error = new ErrorMsg(e, ErrorMsg.ERR_COURPUS_OPEN,
						corpus[i].getIOFilename());
				setError(error.getMessage());

				return null;
			}

			// Get indexes
			cixVector = corpus[i].getCixVector();
			aixVector = corpus[i].getAixVector();

			// 検索キーの選択
			for (int l = 0; l < searchTarget.length; l++) {
				if (isCancelled()){
					throw new InterruptedException();
				}

				if (searchTargetName.equals(searchTarget[l])) {
					storedFieldName = (String) storedFieldNames.get(l);
					if (l < cixVector.size()) {
						cix = cixVector.get(l);
						if (cix.getClass().getSimpleName().equals("RecordBasedContentsIndex")) { //$NON-NLS-1$
							((RecordBasedContentsIndex) cix).setRetrieveCondition(fieldInfo, storedFieldName, keyHead, keyTail);
						} else {
							cix.setRetrieveCondition(fieldInfo,	storedFieldName);
						}
						index = cix;
						targetIndexName = cix.getElementName();
					} else {
						aix = aixVector.get(l - cixVector.size());
						if (aix.getClass().getSimpleName().equals("RecordBasedArgumentIndex")) { //$NON-NLS-1$
							((RecordBasedArgumentIndex) aix).setRetrieveCondition(fieldInfo, storedFieldName, keyHead, keyTail);
						} else {
							aix.setRetrieveCondition(fieldInfo, storedFieldName);
						}
						index = aix;
						targetIndexName = aix.getElementName();
					}
					index.setFilter(filter);
					break;
				}
			}


			// get iNeedList which stores indexes of fieldInfo used later 
			ArrayList<Integer> iNeededList = new ArrayList<Integer>(); 
			HashSet<String> elementNameList = new HashSet<String>();
			for (int k = 0; k < fieldInfo.size(); k++) {
				String elementName = fieldInfo.getElementName(k);
				String fieldType = fieldInfo.getType(k);

				if(elementName.startsWith("_") || elementName.isEmpty() || elementName.equals(index.getProcessedElementName())){ //$NON-NLS-1$
					continue;
				}
				
				if (fieldType.equals("relative") || fieldType.equals("sibling")){ //$NON-NLS-1$ //$NON-NLS-2$
					iNeededList.add(k);
				} else {
					if(elementNameList.contains(elementName)){
						continue;
					} else {
						elementNameList.add(elementName);
					}
					iNeededList.add(k);
				}
			}

			// set values to arrays
			String elementNames[] = new String[iNeededList.size()];
			String fieldTypes[] = new String[iNeededList.size()];
			String attributeNames[] = new String[iNeededList.size()];
			int elementOffsets[] = new int[iNeededList.size()];
			ElementIndex elementIndexes[] = new ElementIndex[iNeededList.size()];
			elementNameList.clear();
			int ne = 0;
			for (int k : iNeededList) {
				elementNames[ne] = fieldInfo.getElementName(k);
				fieldTypes[ne] = fieldInfo.getType(k);
				attributeNames[ne] = fieldInfo.getAttributeName(k);
				if(elementNames[ne].endsWith("]")){ //$NON-NLS-1$
					elementOffsets[ne] = Integer.parseInt(elementNames[ne].substring(elementNames[ne].indexOf('[') + 1, elementNames[ne].length() - 1));
					elementNames[ne] = elementNames[ne].replaceFirst("\\[.+?\\]$", ""); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					elementOffsets[ne] = 0;
				}
				if(!elementNames[ne].isEmpty() && !elementNameList.contains(elementNames[ne])){
					elementNameList.add(elementNames[ne]);
				}
				ne++;
			}

			// set elementIndexes
			for(int k = 0; k < ne; k++){
				if(fieldTypes[k].equals("relative") || fieldTypes[k].equals("sibling")){ //$NON-NLS-1$ //$NON-NLS-2$
					elementIndexes[k] = corpus[i].getEix(targetIndexName);
				} else {
					elementIndexes[k] = corpus[i].getEix(elementNames[k]);
				}
			}
			
			try {
				index.open();
			} catch (IOException ex) {
				ex.printStackTrace();
				ErrorMsg error = new ErrorMsg(ex,
						ErrorMsg.ERR_INDEX_OPEN_OTHER, index
								.getIOFilename());
				setError(error.getMessage());
				return null;
			}

			try {
				System.err.println("open eix"); //$NON-NLS-1$
				corpus[i].openEix(elementNameList.toArray(new String[0]));
			} catch (IOException e) {
				e.printStackTrace();
				ErrorMsg error = new ErrorMsg(e,
						ErrorMsg.ERR_INDEX_OPEN_EIX, corpus[i]
								.getIOFilename());
				setError(error.getMessage());

				return null;
			}

			kwicMaker = new KWICMaker(filter, length_context_kwic,
					length_context_search, isCatPreviousContext,
					isCatFollowingContext, storedFieldName);
			kwicMaker.setCorpus(corpus[i]);

			ResultRecord resultRecord = null;

			for (int j = 0; j < keys.size(); j++) {

				if (isCancelled()){
					throw new InterruptedException();
				}

				tempRecordSet.clear();

				// Retrieve the key from the specified contents of XML
				// element
				resultRecord = index.retrieveFirst(keys.get(j));


				while (resultRecord != null) {
					if (isCancelled()){
						throw new InterruptedException();
					}

					resultRecord = kwicMaker.makeUnderConstraint(resultRecord);
					
					// 属性, next, sibling 検索
					for (int k = 0; k < ne && resultRecord != null; k++) {
						if (isCancelled()){
							throw new InterruptedException();
						}
						
						String fieldType = fieldTypes[k];
						String elementName = elementNames[k];
						String attributeName = attributeNames[k];
						int elementOffset = elementOffsets[k];
						ElementIndex eix = elementIndexes[k];

						if (fieldType.equals("argument") || //$NON-NLS-1$
								((fieldType.equals("db")||fieldType.equals("dic")) && elementOffset == 0)) { //$NON-NLS-1$ //$NON-NLS-2$
							resultRecord = eix.addAttribute(resultRecord, filter);
						} else if(fieldType.equals("dic") && elementOffset != 0){ //$NON-NLS-1$
							resultRecord = eix.resistNextElement(resultRecord, elementOffset, attributeName, filter);
						} else if(fieldType.equals("db") && elementOffset != 0){ //$NON-NLS-1$
							resultRecord = eix.resistNextElement(resultRecord, elementOffset, attributeName, filter);
						} else if (fieldType.equals("relative")) { //$NON-NLS-1$
							resultRecord = eix.resistNextElement(resultRecord, elementOffset, attributeName, filter);
						} else if (fieldType.equals("sibling")) { //$NON-NLS-1$
							resultRecord = eix.resistSiblingElement(resultRecord, elementName, attributeName, filter);
						}
					}
					if (resultRecord != null) {
						if(mode == MODE_COUNT){
							rs.increment();
						} else {
							resultRecord.setResourceID(i);
							resultRecord.setResourceName(corpus[i].getCorpusName());

							rs.add(resultRecord);
						}
					}
					resultRecord = index.retrieveNext();
				}
			}
			
			// Close corpusFile
			try {
				if (corpus[i] != null)
					corpus[i].close();
			} catch (IOException e) {
				e.printStackTrace();

				ErrorMsg error = new ErrorMsg(e, ErrorMsg.ERR_INDEX_CLOSE,
						corpus[i].getIOFilename());
				setError(error.getMessage());

				return null;
			}
			// Close corpusFile
			try {
				if (index != null)
					index.close();
			} catch (IOException e) {
				e.printStackTrace();
				ErrorMsg error = new ErrorMsg(e, ErrorMsg.ERR_INDEX_CLOSE,
						index.getIOFilename());
				setError(error.getMessage());

				return null;
			}
		}

		if (resultRecordSet == null) {
			ErrorMsg error = new ErrorMsg(new Exception(),
					ErrorMsg.ERR_GUI_INPUT_INVALID, null);
			setError(error.getMessage());
			return null;
		} else {
			long end = System.currentTimeMillis();
			System.err.println("time[milisec]: " + (end - start)); //$NON-NLS-1$
			return rs;
		}
	}

	ArrayList<ResultRecord> getCorpusList(FieldInfo fieldInfo) throws Exception {
		ArrayList<ResultRecord> resultList = new ArrayList<ResultRecord>();
		for (int i = 0; i < corpus.length; i++) {
			if (!corpus[i].isSelected()) {
				continue;
			}

			ElementIndex corpusElementIndex = corpus[i].getEixVector().get(iCorpusElemenet);
			try {
				corpus[i].init();
				corpusElementIndex.open();
			} catch (IOException ex) {
				ex.printStackTrace();
				ErrorMsg error = new ErrorMsg(ex, ErrorMsg.ERR_COURPUS_OPEN,
						corpus[i].getIOFilename());
				setError(error.getMessage());
			}

			resultList.addAll(corpusElementIndex.listElement(fieldInfo, filter));

			try {
				corpus[i].close();
			} catch (IOException ex) {
				ex.printStackTrace();
				ErrorMsg error = new ErrorMsg(ex, ErrorMsg.ERR_COURPUS_CLOSE,
						corpus[i].getIOFilename());
				setError(error.getMessage());
			}
		}
		return resultList;
	}

	ArrayList<ResultRecord> getSelectedElementList(FieldInfo fieldInfo) throws Exception {
		ArrayList<ResultRecord> resultList = new ArrayList<ResultRecord>();
		ArrayList<ResultRecord> tmpResultVector;

		for (int i = 0; i < corpus.length; i++) {
			if (!corpus[i].isSelected()) {
				continue;
			}

			ElementIndex topElementIndex = corpus[i].getEixVector().get(iCorpusElemenet);
			ElementIndex browsedElementIndex = corpus[i].getEixVector().get(browsedElementNumber);

			// initialize and open CorpusFile
			try {
				corpus[i].init();
				topElementIndex.open();
				browsedElementIndex.open();
			} catch (IOException e2) {
				e2.printStackTrace();
				ErrorMsg error = new ErrorMsg(e2, ErrorMsg.ERR_COURPUS_OPEN,
						corpus[i].getIOFilename());
				setError(error.getMessage());
			}
			tmpResultVector = null;
			tmpResultVector = browsedElementIndex.listElement(fieldInfo, filter);
			// register corpus numbers to each records
			for (ResultRecord r : tmpResultVector) {
				topElementIndex.addAttribute(r, null);
				r.setResourceID(i);
				r.setResourceName(corpus[i].getCorpusName());
			}
			try {
				if (topElementIndex != null)
					topElementIndex.close();
				if (browsedElementIndex != null)
					browsedElementIndex.close();
				if (corpus[i] != null)
					corpus[i].close();
			} catch (IOException ex) {
				ex.printStackTrace();
				ErrorMsg error = new ErrorMsg(ex, ErrorMsg.ERR_COURPUS_CLOSE,
						corpus[i].getIOFilename());
				setError(error.getMessage());
			}
			resultList.addAll(tmpResultVector);
		}
		return resultList;
	}

	
	ArrayList<ResultRecord> getElementList(String elementName, FieldInfo fieldInfo) throws Exception {
		RecordStatistics rs = new RecordStatistics(fieldInfo, true);
		ArrayList<ResultRecord> tmpResultVector;
		
		for(int i = 0; i < corpus.length; i++){
			CorpusFile targetCorpus = corpus[i];
			String corpusName = targetCorpus.getCorpusName();
			
			if (!targetCorpus.isSelected()) {
				continue;
			}
		
			ElementIndex eix = targetCorpus.getEix(elementName);
			HashMap<String, Boolean> finish = new HashMap<String, Boolean>();

			// initialize and open CorpusFile
			try {
				targetCorpus.init();
				eix.open();
			} catch (IOException e2) {
				e2.printStackTrace();
				ErrorMsg error = new ErrorMsg(e2, ErrorMsg.ERR_COURPUS_OPEN,
						targetCorpus.getIOFilename());
				setError(error.getMessage());
			}

			tmpResultVector = eix.listElement(fieldInfo, filter);
			finish.put(elementName, true);
			
			for (ResultRecord r : tmpResultVector) {
				r.setResourceID(i);
				r.setResourceName(corpusName);
			}

			for(int j = 0; j < fieldInfo.size(); j++){
				String otherElementName = fieldInfo.getElementName(j);
				if(finish.containsKey(otherElementName)
						|| otherElementName.equals(FieldInfo.ELEMENT_SYSTEM)
						|| otherElementName.isEmpty()){
					continue;
				}
				finish.put(otherElementName, true);
				ElementIndex otherEix = targetCorpus.getEix(otherElementName);
				otherEix.open();
				
				Iterator<ResultRecord> it = tmpResultVector.iterator();
				while(it.hasNext()){
					ResultRecord r = it.next();
					if(otherEix.addAttribute(r, filter) == null){
						it.remove();
					}
				}
				otherEix.close();
			}
			
			
			try {
				if (eix != null)
					eix.close();
				if (targetCorpus != null)
					targetCorpus.close();
			} catch (IOException ex) {
				ex.printStackTrace();
				ErrorMsg error = new ErrorMsg(ex, ErrorMsg.ERR_COURPUS_CLOSE,
						targetCorpus.getIOFilename());
				setError(error.getMessage());
			}
			rs.add(tmpResultVector);
		}
		return rs.getResults();
	}
	
	RecordStatistics getElementList2(FieldInfo fieldInfo, String[] order, int contextLength) throws Exception {
		int nTargetIndex = order.length - 1;
		String targetElementName = order[nTargetIndex];
		RecordStatistics rs = new RecordStatistics(fieldInfo);
		IndexState idxStates[] = new IndexState[nTargetIndex];
		boolean includeContents = false;
		boolean includeLength = false;
		String contentsFieldName = null;
		String lengthFieldName = null;
		Pattern tagPattern = Pattern.compile("<.+?>|\\s"); //$NON-NLS-1$
		LinkedList<ResultRecord> contextQueue = new LinkedList<ResultRecord>();
		ArrayList<Integer> iTargetElementFields = new ArrayList<Integer>();

		// remove unused entries in the fieldInfo from the filter
		Filter newFilter = new Filter();
		for(Entry<String, Pattern> a : filter.entrySet()) {
			if(fieldInfo.containsKey(a.getKey())) {
				newFilter.put(a.getKey(), a.getValue(), filter.isNot(a.getKey()));
			}
		}
		
		// check OPTION_CONTENTS and OPTION_LENGTH
		for(int j = 0; j < fieldInfo.size(); j++){
			String elementName2 = fieldInfo.getElementName(j);
			if(elementName2.equals(targetElementName)){
				iTargetElementFields.add(j);
				String attribute2 = fieldInfo.getAttributeName(j);
				if(attribute2.equals(FieldInfo.ATTRIBUTE_CONTENTS)){
					contentsFieldName = fieldInfo.getName(j);
					includeContents = true;
				} else if(attribute2.equals(FieldInfo.ATTRIBUTE_LENGTH)){
					lengthFieldName = fieldInfo.getName(j);
					includeLength = true;
				}
			}
		}
		
		
		for(int i = 0; i < corpus.length; i++){
			CorpusFile targetCorpus = corpus[i];
			String corpusName = targetCorpus.getCorpusName();
			
			if (!targetCorpus.isSelected()) {
				continue;
			}
		
			ElementIndex eix = targetCorpus.getEix(targetElementName);

			// initialize and open CorpusFile
			try {
				targetCorpus.init();
				eix.open();
			} catch (IOException e2) {
				e2.printStackTrace();
				ErrorMsg error = new ErrorMsg(e2, ErrorMsg.ERR_COURPUS_OPEN,
						targetCorpus.getIOFilename());
				setError(error.getMessage());
			}
			
			for(int j = 0; j < nTargetIndex; j++){
				idxStates[j] = new IndexState();
				idxStates[j].eix = targetCorpus.getEix(order[j]);
				idxStates[j].eix.open();
				idxStates[j].endPoint = -1; // initialize
				ArrayList<Integer> iFields = new ArrayList<Integer>();
				for(int k = 0; k < fieldInfo.size(); k++){
					if(fieldInfo.getElementName(k).equals(order[j])){
						iFields.add(k);
					}
				}
				idxStates[j].iFields = iFields.toArray(new Integer[0]);
				idxStates[j].fieldValues = new String[iFields.size()];
			}
			
			ArrayList<Integer> indexList = eix.listIndex(fieldInfo);
			int ps[] = new int[indexList.size()];
			for(int p = 0; p < indexList.size(); p++){
				ps[p] = indexList.get(p);
			}

			for(int p = 0; p < ps.length; p++){
				int index = ps[p];
				ResultRecord r = new ResultRecord(fieldInfo);
				r.setPosition(index);
				
				boolean flagNextLoop = false;
				for(int k = 0; k < nTargetIndex; k++){
					IndexState idxState = idxStates[k];
					if(idxState.endPoint < index){
						try {
							Range range = idxState.eix.searchRange(index);
							if (range == null) {
								flagNextLoop = true;
								break;
							}
							idxState.endPoint = range.getEnd();
						} catch(BufferUnderflowException e) {
							flagNextLoop = true; 
							break;
						}
						if(idxState.eix.addAttribute(r, newFilter) == null){
							while(++p < ps.length && ps[p] < idxState.endPoint){}
							p--;
							flagNextLoop = true; 
							break;
						}
						
						for(int l = 0; l < idxState.iFields.length; l++){
							idxState.fieldValues[l] = (String)r.get(idxState.iFields[l]);
						}
					} else {
						for(int l = 0; l < idxState.iFields.length; l++){
							r.set(idxState.iFields[l], idxState.fieldValues[l]);
						}
					}
				}
				if(flagNextLoop){
					continue;
				}
				if(eix.addAttribute(r, newFilter) == null){
					continue;
				}
				
				if(includeContents || includeLength){
					String contents;
					if(eix instanceof StandoffElementIndex){
						contents = ((StandoffElementIndex)eix).getText(index);
					} else {
						contents = targetCorpus.getContent(targetElementName, index);
					}
					if(includeContents){
						r.set(contentsFieldName, contents);
					}
					if(includeLength){
						if(eix.isEmpty()){
							r.set(lengthFieldName, 0);
						} else {
							String contentsTagRemoved = tagPattern.matcher(contents).replaceAll(""); //$NON-NLS-1$
							r.set(lengthFieldName, contentsTagRemoved.codePointCount(0, contentsTagRemoved.length())); //$NON-NLS-1$
						}
					}
				}
				
				r.setResourceID(i);
				r.setResourceName(corpusName);
				
				
				if(contextLength > 0){
					contextQueue.add(r);
					int len = iTargetElementFields.size();
					int iContextQueue = contextQueue.size() - 1;
					for(ResultRecord rPrev : contextQueue){
						if(iContextQueue == 0){
							break;
						}
						for(int iField : iTargetElementFields){
							rPrev.set(iField + len*iContextQueue, r.get(iField));
						}
						iContextQueue--;
					}
					if(contextQueue.size() > contextLength){
						r = contextQueue.poll();
					} else {
						continue;
					}
				}
				
				rs.add(r);
			}
			
			while(!contextQueue.isEmpty()){
				rs.add(contextQueue.poll());
			}
			
			
			try {
				eix.close();
				for(IndexState idxState : idxStates){
					idxState.eix.close();
				}
				targetCorpus.close();
			} catch (IOException ex) {
				ex.printStackTrace();
				ErrorMsg error = new ErrorMsg(ex, ErrorMsg.ERR_COURPUS_CLOSE,
						targetCorpus.getIOFilename());
				setError(error.getMessage());
			}
		}
		
		return rs;
	}


	class IndexState {
		ElementIndex eix;
		int endPoint;
		Integer[] iFields;
		String fieldValues[];
	}
	
	
	String getBrowsedElement(int iCorpus, int index, int targetLengthSP,
			String anchorName, String anchorID) throws Exception {
		CorpusFile selectedCorpus = corpus[iCorpus];
		ElementIndex corpusElementIndex;
		ElementIndex browsedElementIndex = selectedCorpus.getEixVector().get(browsedElementNumber);
		
		
		try {
			selectedCorpus.init();
			browsedElementIndex.open();
		} catch (IOException e) {
			e.printStackTrace();
			ErrorMsg error = new ErrorMsg(e, ErrorMsg.ERR_COURPUS_OPEN,
					selectedCorpus.getIOFilename());
			setError(error.getMessage());
		}

		// top 要素の開始タグ
		String corpusElementStartTag;
		String corpusElementEndTag;

		if (iCorpusElemenet == -1 || iCorpusElemenet == browsedElementNumber) {
			corpusElementIndex = null;
			corpusElementStartTag = ""; //$NON-NLS-1$
			corpusElementEndTag = ""; //$NON-NLS-1$
		} else {
			corpusElementIndex = selectedCorpus.getEixVector().get(iCorpusElemenet);
			corpusElementIndex.open();
			try {
				corpusElementStartTag = corpusElementIndex.searchArg(index);
			} catch (Exception ex) {
				throw ex;
			}
			corpusElementEndTag = "</" + corpusElementName + ">"; //$NON-NLS-1$ //$NON-NLS-2$
		}

		// 閲覧対象要素の範囲
		Range browsedElementRange;
		try {
			browsedElementRange = browsedElementIndex.searchRange(index);
		} catch (Exception ex) {
			throw ex;
		}

		// 閲覧要素
		String source = selectedCorpus.getSource(browsedElementRange);

		// 検索語よりも前の文字列
		String contentsHead = source.substring(0, index
				- browsedElementRange.getStart());
		selectedCorpus.readStrNSP(targetLengthSP, index); // 読み込み用のポインタを検索語の直後に移動
		// XML 中の検索語（検索語中のタグを含む）
		String realTarget = source.substring(index
				- browsedElementRange.getStart(),
				(selectedCorpus.getPosition() / 2)
						- browsedElementRange.getStart());
		// 検索語の後の文字列
		String contentsTail = source.substring(index
				- browsedElementRange.getStart() + realTarget.length());

		// 一文字ずつマーク用のタグ(<tg>)をつける
		StringBuilder markedTarget = new StringBuilder();
		
		if(realTarget.length() == 0){
			if(contentsHead.length() != 0){ // avoid to generate invalid html documents
				markedTarget.append("<" + anchorName + " id=\"" + anchorID + "\"></" + anchorName + ">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			}
		} else {
			boolean tagFlag = false;
			boolean markFlag = false;
			for (int i = 0; i < realTarget.length(); i++) {
				char c = realTarget.charAt(i);
				if (c == '<') {
					tagFlag = true;
					markedTarget.append(c);
				} else if (c == '>') {
					tagFlag = false;
					markedTarget.append(c);
				} else if (tagFlag) {
					markedTarget.append(c);
					continue;
				} else {
					StringBuffer targetCharBuf = new StringBuffer();
					targetCharBuf.append(c);
					if(Character.isSurrogate(c)) {
						targetCharBuf.append(realTarget.charAt(++i));
					}
					if (markFlag) {
						markedTarget
								.append("<" + anchorName + ">" + targetCharBuf + "</" + anchorName + ">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

					} else {
						// put id attribute on the first anchor
						markedTarget
								.append("<" + anchorName + " id=\"" + anchorID + "\">" + targetCharBuf + "</" + anchorName + ">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
						markFlag = true;
					}
				}
			}
		}		

		if (selectedCorpus != null)
			selectedCorpus.close();

		return corpusElementStartTag + contentsHead + markedTarget.toString()
				+ contentsTail + corpusElementEndTag;
	}
	
	
	ArrayList<ResultRecord> getSIXElement(int iCorpus, String elementName, int index, FieldInfo fieldInfo) throws Exception {
		ArrayList<ResultRecord> result = new ArrayList<ResultRecord>();
		CorpusFile selectedCorpus = corpus[iCorpus];
		ElementIndex browsedElementIndex = selectedCorpus.getEixVector().get(browsedElementNumber);
		ElementIndex sixElementIndex = selectedCorpus.getEix(elementName);
		
		try {
			selectedCorpus.init();
			browsedElementIndex.open();
			sixElementIndex.open();
		} catch (IOException e) {
			e.printStackTrace();
			ErrorMsg error = new ErrorMsg(e, ErrorMsg.ERR_COURPUS_OPEN,
					selectedCorpus.getIOFilename());
			setError(error.getMessage());
		}

		// 閲覧対象要素の範囲
		Range browsedElementRange;
		try {
			browsedElementRange = browsedElementIndex.searchRange(index);
			int p = browsedElementRange.getStart();
			int pEnd = browsedElementRange.getEnd();
			int c = 0;

			while(p < pEnd){
				ResultRecord resultRecord = new ResultRecord(fieldInfo);
				resultRecord.setPosition(p);
				try {
					if (sixElementIndex.addAttribute(resultRecord, null) == null) {
						p++;
						continue;
					} else {
						String text = (String) resultRecord.get(elementName
								+ "\t" //$NON-NLS-1$
								+ DBController.FIELD_ANNOTATION_SEARCH_KEY);
						if (text == null || text.isEmpty()) {
							p++;
						} else {
							selectedCorpus.setPosition(p * 2);
							selectedCorpus.skip(text.length());
							p = selectedCorpus.getPosition() / 2;
							resultRecord.setResourceID(iCorpus);
							resultRecord.set(0, String.format("%08d", ++c)); // 0 => FieldInfo.ELEMENT_SYSTEM + "\t" + FieldInfo.ATTRIBUTE_SERIAL_NUMBER //$NON-NLS-1$
							result.add(resultRecord);
						}
					}
				} catch (Exception e) {
					p++;
					continue;
				}
			}
		} catch (Exception ex) {
			throw ex;
		}


		if (selectedCorpus != null)
			selectedCorpus.close();

		return result;
	}
	
	

	public String generateIndex(String annotatorName) {
		for (int i = 0; i < corpus.length; i++) {
			HashMap<String, CorpusElementInfo> elementMap = new HashMap<String, CorpusElementInfo>();

			System.err.println("* " + corpus[i].getBasename()); //$NON-NLS-1$
			if (!corpus[i].exists()) {
				System.err
						.println(Messages.getString("SearchEngine.110") + corpus[i].getBasename() + //$NON-NLS-1$
								Messages.getString("SearchEngine.111") + //$NON-NLS-1$
								corpus[i].getBasename() + ".xml"); //$NON-NLS-1$
				return Messages.getString("SearchEngine.113") + //$NON-NLS-1$
						MESSAGE_DELIMITER +
						Messages.getString("SearchEngine.114") + //$NON-NLS-1$
						MESSAGE_DELIMITER +
						Messages.getString("SearchEngine.115") + //$NON-NLS-1$
						corpus[i].getBasename()
						+ Messages.getString("SearchEngine.116") + //$NON-NLS-1$
						MESSAGE_DELIMITER +
						""; //$NON-NLS-1$
			}
			try {
				corpus[i].init();
				ArrayList<ContentsIndex> cixVector = corpus[i].getCixVector();
				// cix
				for (int j = 0; j < cixVector.size(); j++) {
					ContentsIndex targetCix = cixVector.get(j);
					if (targetCix.exists()) {
						System.err
								.println(Messages.getString("SearchEngine.117") + targetCix.getFilename() + //$NON-NLS-1$
										Messages.getString("SearchEngine.118")); //$NON-NLS-1$
						continue;
					}
					System.err
							.println(Messages.getString("SearchEngine.119") + targetCix.getFilename()); //$NON-NLS-1$
					targetCix.mkcix(targetCix.getElementName());
					targetCix.close();
				}

				// eix
				ArrayList<ElementIndex> eixVector = corpus[i].getEixVector();
				for (int j = 0; j < eixVector.size(); j++) {
					if(eixVector.get(j).isDB){
						continue;
					}
					if(eixVector.get(j).getClass().getCanonicalName().contains("StandoffElementIndex")){ //$NON-NLS-1$
						StandoffElementIndex targetEix = (StandoffElementIndex) eixVector.get(j);
						if (targetEix.exists()) {
							System.err
									.println(Messages.getString("SearchEngine.120") + targetEix.getIOFilename() + //$NON-NLS-1$
											Messages.getString("SearchEngine.121")); //$NON-NLS-1$
							continue;
						}
						// TODO 形態素解析器名のハードコーディング
						targetEix.mksix(annotatorName, config);
						targetEix.close();
						continue;
					}
					
					XmlElementIndex targetEix = (XmlElementIndex) eixVector.get(j);
					if (targetEix.exists()) {
						System.err
								.println(Messages.getString("SearchEngine.120") + targetEix.getFilename() + //$NON-NLS-1$
										Messages.getString("SearchEngine.121")); //$NON-NLS-1$
						continue;
					}
					System.err
							.println(Messages.getString("SearchEngine.122") + targetEix.getFilename()); //$NON-NLS-1$
					targetEix.mkeix();
					targetEix.close();
				}
//				targetEix.getClass().getSimpleName()

				// aix
				ArrayList<ArgumentIndex> aixVector = corpus[i].getAixVector();
				for (int j = 0; j < aixVector.size(); j++) {
					ArgumentIndex targetAix = aixVector.get(j);
					if(targetAix.isDB){
						continue;
					}
					if (targetAix.exists()) {
						System.err
								.println(Messages.getString("SearchEngine.123") + targetAix.getFilename() + //$NON-NLS-1$
										Messages.getString("SearchEngine.124")); //$NON-NLS-1$
						continue;
					}
					System.err
							.println(Messages.getString("SearchEngine.125") + targetAix.getFilename()); //$NON-NLS-1$
					if (targetAix.isCompleteMatch()) {
						targetAix.mkaix(true);
					} else {
						targetAix.mkaix(false);
					}
					if (targetAix != null)
						targetAix.close();
				}
				
				corpus[i].analyze(elementMap);
				corpus[i].saveStructure(elementMap);
				
				if (corpus[i] != null){
					corpus[i].close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
				return Messages.getString("SearchEngine.126") + //$NON-NLS-1$
						MESSAGE_DELIMITER +
						Messages.getString("SearchEngine.127") + //$NON-NLS-1$
						MESSAGE_DELIMITER +
						Messages.getString("SearchEngine.128") + //$NON-NLS-1$
						MESSAGE_DELIMITER +
						e2.getLocalizedMessage();
			}
		}
		System.err.println(Messages.getString("SearchEngine.129")); //$NON-NLS-1$
		return Messages.getString("SearchEngine.130") + //$NON-NLS-1$
				MESSAGE_DELIMITER +
				Messages.getString("SearchEngine.131") + //$NON-NLS-1$
				MESSAGE_DELIMITER +
				Messages.getString("SearchEngine.132") + //$NON-NLS-1$
				MESSAGE_DELIMITER +
				" "; //$NON-NLS-1$
	}
	
	
	public HashMap<String, CorpusElementInfo> analyze(){
		
		HashMap<String, CorpusElementInfo> elementMap = new HashMap<String, CorpusElementInfo>();

		for(CorpusFile corpus : getCorpus()){
			if(corpus.getFile() == null){
				try {
					corpus.init();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			corpus.analyze(elementMap);
			try {
				corpus.saveStructure(elementMap);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return elementMap;
	}


	
	public void loadStructure(HashMap<String, CorpusElementInfo> elementMap) throws IOException {

		for (CorpusFile c : corpus) {
			c.init();
			c.loadStructure(elementMap);
			c.close();
		}
	}	

	
	public void setStatus(int status) {
		this.status = status;
	}

	public int getStatus() {
		return status;
	}

	public ArrayList<ResultRecord> getTempRecordSet() {
		return tempRecordSet;
	}

	public void setTempRecordSet(ArrayList<ResultRecord> tempRecordSet) {
		this.tempRecordSet = tempRecordSet;
	}

	public ArrayList<String> getStoredFieldNames() {
		return storedFieldNames;
	}

	public void setStoredFieldNames(ArrayList<String> storedFieldNames) {
		this.storedFieldNames = storedFieldNames;
	}

	public ArrayList<String> getKeys() {
		return keys;
	}

	public void setKeys(ArrayList<String> keys) {
		this.keys = keys;
	}

	public String getKeyHead() {
		return keyHead;
	}

	public void setKeyHead(String keyHead) {
		this.keyHead = keyHead;
	}

	public String getKeyTail() {
		return keyTail;
	}

	public void setKeyTail(String keyTail) {
		this.keyTail = keyTail;
	}

	public int getLength_context_kwic() {
		return length_context_kwic;
	}

	public void setLength_context_kwic(int lengthContextKwic) {
		length_context_kwic = lengthContextKwic;
	}

	public int getLength_context_search() {
		return length_context_search;
	}

	public void setLength_context_search(int lengthContextSearch) {
		length_context_search = lengthContextSearch;
	}

	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public boolean isCatPreviousContext() {
		return isCatPreviousContext;
	}

	public void setCatPreviousContext(boolean isCatPreviousContext) {
		this.isCatPreviousContext = isCatPreviousContext;
	}

	public boolean isCatFollowingContext() {
		return isCatFollowingContext;
	}

	public void setCatFollowingContext(boolean isCatFollowingContext) {
		this.isCatFollowingContext = isCatFollowingContext;
	}

	public FieldInfo getFieldInfo() {
		return fieldInfo;
	}

	public void setFieldInfo(FieldInfo fieldInfo) {
		this.fieldInfo = fieldInfo;
	}

	public String getSearchTargetName() {
		return searchTargetName;
	}

	public void setSearchTargetName(String searchTargetName) {
		this.searchTargetName = searchTargetName;
	}

	public ArrayList<ResultRecord> getResultRecordSet() {
		return resultRecordSet;
	}

	public void setResultRecordSet(ArrayList<ResultRecord> resultRecordSet) {
		this.resultRecordSet = resultRecordSet;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public void setStoredFieldName(String storedFieldName) {
		this.storedFieldName = storedFieldName;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public int getMode() {
		return mode;
	}

	public void setSample(int sample) {
		this.sample = sample;
	}

	public int getSample() {
		return sample;
	}


	public void setSelectedTable(ResultTable selectedTable) {
		this.selectedTable = selectedTable;
	}

	public ResultTable getSelectedTable() {
		return selectedTable;
	}

	public JLabel getStatusBar() {
		return statusBar;
	}

	public void setStatusBar(JLabel statusBar) {
		this.statusBar = statusBar;
	}

	public void setIsRecordBased(Boolean isRecordBased) {
		this.isRecordBased = isRecordBased;
	}

	public Boolean getIsRecordBased() {
		return isRecordBased;
	}

	
	/**
	 * @return nonTargetCorpus
	 */
	public CorpusFile[] getNonTargetCorpus() {
		return nonTargetCorpus;
	}

	public int getBrowsedElementNumber() {
		return browsedElementNumber;
	}

	public void setBrowsedElementNumber(int value) {
		browsedElementNumber = value;
	}
}

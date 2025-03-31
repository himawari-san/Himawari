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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;


public class DatabaseElementIndex extends ElementIndex {

	private final static String COLUMN_NAME_GLUE = "\t"; //$NON-NLS-1$
	private final String tmpFilenameBody = "_himawari_db"; //$NON-NLS-1$
	private final String tmpFilenameSuffix = ".tmp"; //$NON-NLS-1$


	private final static int HEADER_LINE = 1;
	private final static int MAX_ERROR_COUNT = 10;
	private Map<Character, Integer> charWarnings = new HashMap<Character, Integer>();

	
	private DBController dbController;
	private String tableName;
	private CorpusFile corpus;
	private PreparedStatement preparedStatementSelect;
	private PreparedStatement preparedStatementSelectNext;
	private PreparedStatement preparedStatementSelectAll;
	private String elementNamePsTab;
	
	
	public DatabaseElementIndex(String tableName, CorpusFile corpus){
		super(tableName);
		isDB = true;
		this.corpus = corpus;
		this.preparedStatementSelect = null;
		this.preparedStatementSelectNext = null;
		this.preparedStatementSelectAll = null;
		elementNamePsTab = elementName + COLUMN_NAME_GLUE;
		charWarnings = new HashMap<Character, Integer>();
	}

	
	@Override
	public void open() {
		dbController = corpus.getDBController();
		tableName = dbController.getTableName(corpus.getCorpusName(), elementName);
		String t1 = tableName.replaceFirst("\\[-?\\d+\\]", ""); //$NON-NLS-1$ //$NON-NLS-2$
		String t2 = elementName.replaceFirst("\\[-?\\d+\\]", ""); //$NON-NLS-1$ //$NON-NLS-2$
		
		String sql = "SELECT m._ID, d.* FROM " + //$NON-NLS-1$
						t1 + " as m, " + t2 + DBController.SUFFIX_TABLE_DIC + " as d " + //$NON-NLS-1$ //$NON-NLS-2$
						" WHERE d." + DBController.FIELD_ANNOTATION_SEARCH_KEY + " = ? and " + //$NON-NLS-1$ //$NON-NLS-2$
						"m." + DBController.FIELD_ANNOTATION_START + "= ? and " + //$NON-NLS-1$ //$NON-NLS-2$
						"m." + DBController.FIELD_ANNOTATION_DIC_ID + "= d." + DBController.FIELD_DIC_ITEM_ID; //$NON-NLS-1$ //$NON-NLS-2$


		String sqlNext = "SELECT d.*, m2.* FROM " + //$NON-NLS-1$
				t1 + " as m1, " + t1 + " as m2, " + t2 + DBController.SUFFIX_TABLE_DIC + " as d " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				" WHERE " + //$NON-NLS-1$
				"m1." + DBController.FIELD_ANNOTATION_START + "= ? and " + //$NON-NLS-1$ //$NON-NLS-2$
				"m2." + DBController.FIELD_ANNOTATION_ID + " = " + "m1." + DBController.FIELD_ANNOTATION_ID + " + ? and " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"m2." + DBController.FIELD_ANNOTATION_DIC_ID + "= d." + DBController.FIELD_DIC_ITEM_ID; //$NON-NLS-1$ //$NON-NLS-2$

		String sqlAll = "SELECT m.*, d.* FROM " + //$NON-NLS-1$
				t1 + " as m, " + t2 + DBController.SUFFIX_TABLE_DIC + " as d " + //$NON-NLS-1$ //$NON-NLS-2$
				" WHERE m." + DBController.FIELD_ANNOTATION_DIC_ID + "= d." + DBController.FIELD_DIC_ITEM_ID; //$NON-NLS-1$ //$NON-NLS-2$
		
		try {
			preparedStatementSelect = dbController.getConn().prepareStatement(sql);
			preparedStatementSelectNext = dbController.getConn().prepareStatement(sqlNext);
			preparedStatementSelectAll = dbController.getConn().prepareStatement(sqlAll);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	void close() {
		// do nothing
	}

	@Override
	public boolean isOpen() {
		return dbController.isFlagConnected();
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean exists() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String searchArg(int place) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Range searchRange(int place) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Range searchRange(int place, int n) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<ResultRecord> listElement(FieldInfo fieldInfo, Filter filter)	throws Exception {

		ArrayList<ResultRecord> resultRecords = new ArrayList<ResultRecord>();

		try {

			ResultSet rs = preparedStatementSelectAll.executeQuery();
			int size = rs.getMetaData().getColumnCount();
			ResultSetMetaData rsm = rs.getMetaData();
			ArrayList<Integer> targetIndex = new ArrayList<Integer>();
			for (int i = 1; i <= size; i++) {
				if(fieldInfo.containsKey(elementNamePsTab + rsm.getColumnName(i))){
					targetIndex.add(i);
				}
			}
			
			if (filter == null || filter.size() == 0) { // no filter
				while(rs.next()){
					ResultRecord resultRecord = new ResultRecord(fieldInfo);
					// ResultRecord.setResourceID will be executed later by the caller 
					resultRecord.setPosition(rs.getInt(2));
					for (int i : targetIndex) {
						resultRecord
								.set(elementNamePsTab + rsm.getColumnName(i), rs.getString(i)); //$NON-NLS-1$
					}
					resultRecords.add(resultRecord);
				}
			} else {
				while(rs.next()){
					ResultRecord resultRecord = new ResultRecord(fieldInfo);
					boolean flag = true;
					for (int i : targetIndex) {
						String columnName = rsm.getColumnName(i);
						String columnValue = rs.getString(i);
						if (columnValue == null) {
							columnValue = ""; //$NON-NLS-1$
						}
						Pattern filterValue = filter.get(columnName);

						if (filterValue != null) {
							if (filter.isNot(columnName)) { // 否定
								if (filterValue.matcher(columnValue).find()) {
									flag = false;
									break;
								}
							} else {
								if (!filterValue.matcher(columnValue).find()) {
									flag = false;
									break;
								}
							}
						}
						resultRecord
						.set(elementNamePsTab + rsm.getColumnName(i), rs.getString(i)); //$NON-NLS-1$
					}
					
					if(flag){
						resultRecord.setPosition(rs.getInt(2));
						resultRecords.add(resultRecord);
					}
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return resultRecords;
	}

	
	public ArrayList<Integer> listIndex(FieldInfo fieldInfo) throws Exception {
		return null;
	}
	

	@Override
	public ArrayList<ResultRecord> listContents(FieldInfo fieldInfo)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public ResultRecord addAttribute(ResultRecord resultRecord, Filter filter) {

		int txtPos = resultRecord.getPosition();
		String firstChar = (String)resultRecord.get(ResultRecord.PRIMARY_KEY_FIELD);

		try {
			preparedStatementSelect.setString(1, firstChar);
			preparedStatementSelect.setInt(2, txtPos);
		
			ResultSet rs = preparedStatementSelect.executeQuery();

			if (filter == null || filter.size() == 0) { // フィルタが指定されていない場合
				if (rs.next()) {
					for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
						resultRecord
								.set(elementNamePsTab + rs.getMetaData().getColumnName(i), rs.getString(i)); //$NON-NLS-1$
					}
				}
			} else {
				if (rs.next()) {
					for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
						String columnName = elementNamePsTab + rs.getMetaData().getColumnName(i);
						String columnValue = rs.getString(i);
						if (columnValue == null) {
							columnValue = ""; //$NON-NLS-1$
						}
						Pattern filterValue = filter.get(columnName);

						if (filterValue != null) {
							if (filter.isNot(columnName)) { // 否定
								if (filterValue.matcher(columnValue).find()) {
									return null;
								} else {
									resultRecord.set(columnName, columnValue);
								}
							} else {
								if (filterValue.matcher(columnValue).find()) {
									resultRecord.set(columnName, columnValue);
								} else {
									return null;
								}
							}
						} else {
							resultRecord.set(columnName, columnValue);
						}
					}
				} else {
					// 属性値がなく，フィルタが指定されている場合の処理
					// この要素に対するフィルタだけ選択する
					Set<String> filterColumns = filter.keySet();
					Iterator<String> itFilterColumns = filterColumns.iterator();
					while (itFilterColumns.hasNext()) {
						String columnName = itFilterColumns.next();
						if (columnName.startsWith(elementNamePsTab)) { //$NON-NLS-1$
							if (filter.get(columnName) != null) {
								if ((resultRecord.get(columnName) == null || ((String) resultRecord
										.get(columnName)).compareTo("") == 0) //$NON-NLS-1$
										&& !filter.isNot(columnName)) {
									// フィルタ設定あり，値なし，否定条件ではない
									return null;
								}
								// 否定条件の場合は，そのまま
								// 値ありの場合は，上でチェックしている
							}

						}
					}
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return resultRecord;
	}

	@Override
	public ResultRecord resistNextElement(ResultRecord resultRecord,
			int relativeElementIndex, String targetAttributeName, Filter filter) {

		int txtPos = resultRecord.getPosition();
		String relativeIndexElementName = elementName + "[" + relativeElementIndex + "]\t"; //$NON-NLS-1$ //$NON-NLS-2$

		try {
			preparedStatementSelectNext.setInt(1, txtPos);
			preparedStatementSelectNext.setInt(2, relativeElementIndex);
			
			ResultSet rs = preparedStatementSelectNext.executeQuery();

			if (filter == null || filter.size() == 0) { // フィルタが指定されていない場合
				if (rs.next()) {
					for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
						resultRecord
								.set(relativeIndexElementName + rs.getMetaData().getColumnName(i), rs.getString(i)); //$NON-NLS-1$
					}
				}
			} else {
				if (rs.next()) {
					for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
						String columnName = relativeIndexElementName + rs.getMetaData().getColumnName(i);
						String columnValue = rs.getString(i);
						if (columnValue == null) {
							columnValue = ""; //$NON-NLS-1$
						}
						Pattern filterValue = filter.get(columnName);

						if (filterValue != null) {
							if (filter.isNot(columnName)) { // 否定
								if (filterValue.matcher(columnValue).find()) {
									return null;
								} else {
									resultRecord.set(columnName, columnValue);
								}
							} else {
								if (filterValue.matcher(columnValue).find()) {
									resultRecord.set(columnName, columnValue);
								} else {
									return null;
								}
							}
						} else {
							resultRecord.set(columnName, columnValue);
						}
					}
				} else {
					// 属性値がなく，フィルタが指定されている場合の処理
					// この要素に対するフィルタだけ選択する
					Set<String> filterColumns = filter.keySet();
					Iterator<String> itFilterColumns = filterColumns.iterator();
					while (itFilterColumns.hasNext()) {
						String columnName = itFilterColumns.next();
						if (columnName.startsWith(relativeIndexElementName)) { //$NON-NLS-1$
							if (filter.get(columnName) != null) {
								if ((resultRecord.get(columnName) == null || ((String) resultRecord
										.get(columnName)).compareTo("") == 0) //$NON-NLS-1$
										&& !filter.isNot(columnName)) {
									// フィルタ設定あり，値なし，否定条件ではない
									return null;
								}
								// 否定条件の場合は，そのまま
								// 値ありの場合は，上でチェックしている
							}

						}
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return resultRecord;
	}

	@Override
	public ResultRecord resistSiblingElement(ResultRecord resultRecord,
			String targetElementName, String targetAttributeName, Filter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIOFilename() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public void mkdb(String annotatorName, UserSettings userSettings) throws Exception {
		if(dbController == null){
			dbController = corpus.getDBController();
		}
		dbController.openConnection();
		dbController.createCorpusIdMap();


		ExternalAnnotator ea = new ExternalAnnotator(annotatorName, elementName);
		ea.init(userSettings);
		String targetElementName = ea.getTargetElementName();
		String targetAttributeName = ea.getTargetAttributeName();
		String targetValue = ea.getTargetAttributeValue();
		File resultFile = null;
		ArrayList<String> indexedFields = ea.getIndexedFields();
		

		resultFile = File.createTempFile(tmpFilenameBody, tmpFilenameSuffix);
		ea.execute(corpus, resultFile);

		// corpus initialization
		corpus.init();
		
		// test
		System.err.println("cheking data before storing"); //$NON-NLS-1$
		storeData(resultFile, false, targetElementName, targetAttributeName, targetValue);

		
		// corpus initialization
		corpus.init();

		
		// insert
		dbController.clearCache();
		dbController.executeSQL("DROP TABLE IF EXISTS " + elementName + DBController.SUFFIX_TABLE_DIC); //$NON-NLS-1$
			
		for (String field : indexedFields) {
			String indexName = "idx_" + field + "_" + elementName; //$NON-NLS-1$ //$NON-NLS-2$
			dbController.executeSQL("DROP INDEX IF EXISTS " + indexName); //$NON-NLS-1$
		}

			
		dbController.getConn().setAutoCommit(true);

		System.err.println("storing data"); //$NON-NLS-1$
		storeData(resultFile, true, targetElementName, targetAttributeName, targetValue);	

		// create indexes
		System.err.println("creating indexes"); //$NON-NLS-1$
		String tableName = elementName + "_" + dbController.getCorpusId(corpus.getCorpusName()); //$NON-NLS-1$
		String indexNameStart = "idx_START_" + tableName; //$NON-NLS-1$
		String indexNameDicId = "idx_DIC_ID_" + tableName; //$NON-NLS-1$
		dbController.executeSQL("DROP INDEX IF EXISTS " + indexNameStart); //$NON-NLS-1$
		dbController.executeSQL("DROP INDEX IF EXISTS " + indexNameDicId); //$NON-NLS-1$
		dbController.addIndex(tableName, indexNameStart, "(" + DBController.FIELD_ANNOTATION_START + ")", true, true); //$NON-NLS-1$ //$NON-NLS-2$
		dbController.addIndex(tableName, indexNameDicId, "(" + DBController.FIELD_ANNOTATION_DIC_ID + ")", false, false); //$NON-NLS-1$ //$NON-NLS-2$

		for (String field : indexedFields) {
			String indexName = "idx_" + field + "_" + elementName; //$NON-NLS-1$ //$NON-NLS-2$
			dbController.addIndex(elementName + DBController.SUFFIX_TABLE_DIC, indexName, "(" + field + ")", false, false); //$NON-NLS-1$ //$NON-NLS-2$
			System.err.println("index: " + field); //$NON-NLS-1$
		}

		resultFile.delete();
	}

	
	public void storeData(File annotationDataFile, boolean isStored,
			String targetElementName, String attributeName, String value) throws Exception {
		
		FileInputStream is = new FileInputStream(annotationDataFile);
		String encode = EncodeDetector.detect(annotationDataFile);
		BufferedReader br;
		// すでに encode の値は使っていない。様子を見て，削除
		if(encode == null){
			br = new BufferedReader(new InputStreamReader(is));
		} else {
			System.err.println("ec:" + encode); //$NON-NLS-1$
			br = new BufferedReader(new InputStreamReader(is));
//			br = new BufferedReader(new InputStreamReader(is, encode));
		}
		
		String line;
		int lineNum = 1;
		String[] fieldNames = null;
		int nf = 0;
		int fnStart = -1;
		int fnEnd = -1;
		int fnText = -1;
		int currentPosition = 0;
		int corpusId = -1;
		
		int cError = 0; // 

		Map<String, Object> mapMain = new HashMap<String, Object>();
		Map<String, Object> mapValue = new HashMap<String, Object>();

		int targetEnd = corpus.gotoElement(targetElementName, attributeName, value);
		String initErrorMessage = Messages.getString("Annotator.0") + "\n";  //$NON-NLS-1$ //$NON-NLS-2$
		String errorMessage = initErrorMessage;

		corpusId = dbController.getCorpusId(corpus.getCorpusName());
//		String tableName = annotationName + "_" + corpusId;
		
		int nInsert = 0;
		
		while ((line = br.readLine()) != null) {
			if (line.length() == 0) {
				continue;
			}

			String[] data = line.split("\t"); //$NON-NLS-1$
			
			// ヘッダー行
			if (lineNum == HEADER_LINE) {
				fieldNames = new String[data.length]; 
				nf = data.length;

				for (int i = 0; i < nf; i++) {
					fieldNames[i] = data[i];
					if (fieldNames[i].equals(DBController.FIELD_ANNOTATION_START)) {
						fnStart = i;
						mapMain.put(fieldNames[i], null);
					} else if (fieldNames[i].equals(DBController.FIELD_ANNOTATION_END)) {
						fnEnd = i;
//						mapMain.put(fieldNames[i], null);
					} else {
						if (fieldNames[i].equals(DBController.FIELD_ANNOTATION_SEARCH_KEY)) {
							fnText = i;
						}
						mapValue.put(fieldNames[i], null);
					}
				}

				
		
				if(isStored){
					// corpusID をセット
//					mapMain.put(DBController.FIELD_ANNOTATION_CORPUS_ID, corpusId);

					// テーブル初期化（アノテーション用のテーブルとメモ用のテーブルを作成）
					// アノテーション用テーブル名: annotationName + "_" + corpusId
					// メモ用テーブル名: note + "_" + corpusId
					dbController.initTable(elementName, corpusId);
					
					// 列追加
					for (String fieldName : mapValue.keySet()){
						dbController.addColumn(elementName, fieldName);
					}
					dbController.setPreparedStatementForAnnotation(elementName, corpusId, mapMain, mapValue);
				}

				if (fnStart == -1 || fnEnd == -1 || fnText == -1) {
					errorMessage += Messages.getString("Annotator.2") + line //$NON-NLS-1$
							+ "\n"; //$NON-NLS-1$
					br.close();
					throw new Exception();
				}

			}
			// 本文行
			else {
				int start = Integer.parseInt(data[fnStart]) - 1;
				int end = Integer.parseInt(data[fnEnd]) - 1;
				String targetText = data[fnText];

				int nSkip = start - currentPosition;
				currentPosition = end + 1;

				// 空白，改行などを読み飛ばす
				corpus.setPositionToContents(targetElementName, attributeName, value);
				

				
				if (nSkip > 0) {
					corpus.skip(nSkip);
					System.err.println("skip!!: " + nSkip + ", " + start + ", " + end); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				} else if(nSkip < 0){
					System.err.println("error in annotationimport: " + nSkip + ", " + start + ", " + end); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}

				
				if(targetEnd != -1 && targetEnd <= corpus.getPosition()){
					targetEnd = corpus.gotoElement(targetElementName, attributeName, value);
					corpus.setPositionToContents(targetElementName, attributeName, value);

					if (nSkip > 0) {
						corpus.skip(nSkip);
						System.err.println("skip!!: " + nSkip + ", " + start + ", " + end); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					} else if(nSkip < 0){
						System.err.println("error in annotationimport: " + nSkip + ", " + start + ", " + end); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
				}
			
				
				//  _start と _end を取得
				data[fnStart] = String.valueOf(corpus.getPosition() / 2);
				String corpusText = corpus.readStrN(targetText.length());
				data[fnEnd] = String.valueOf(corpus.getPosition() / 2 - 1); // 読み込み後なので，１文字引く

				
				if (!targetText.equals(corpusText) && (!corpusText.equals(",") || !targetText.equals("，"))){ //$NON-NLS-1$ //$NON-NLS-2$
					if(Annotator.charCheck(corpusText, charWarnings)){
						cError = 0;
					} else {
						errorMessage += Messages.getString("Annotator.29") + targetText + Messages.getString("Annotator.30") //$NON-NLS-1$ //$NON-NLS-2$
								+ corpusText + Messages.getString("Annotator.31") + line + "\n"; //$NON-NLS-1$ //$NON-NLS-2$

						cError++;
						System.err.println("diff(" + cError + "): line=" + line + ", pos=" + corpus.getPosition()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						System.err.println("targetText:" + targetText); //$NON-NLS-1$
						System.err.println("corpusText:" + corpusText); //$NON-NLS-1$

						if (cError > MAX_ERROR_COUNT) {
							errorMessage += MAX_ERROR_COUNT + Messages.getString("Annotator.33") //$NON-NLS-1$
									+ Messages.getString("Annotator.34"); //$NON-NLS-1$
							br.close();
							throw new IllegalStateException(errorMessage);
						}
					}
				} else if(start > end){
					errorMessage += Messages.getString("Annotator.35") + line + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
							Messages.getString("Annotator.37"); //$NON-NLS-1$
					br.close();
					throw new IllegalStateException(errorMessage);
				} else {
					cError = 0;
				}

				
				if(isStored){
					//データインサート
					for(int i = 0; i < nf; i++){
						if(i == fnStart){
							mapMain.put(fieldNames[i], data[i]);
						} else if(i == fnEnd){
							// skip
						} else {
							if(data[i] == null){
								data[i] = ""; //$NON-NLS-1$
							}
							mapValue.put(fieldNames[i], data[i]);
						}
					}
					
					dbController.insert(mapMain, mapValue);
					
					nInsert++;
					if(nInsert % 10000 == 0){
						dbController.getConn().commit();
						System.err.println("n: " + nInsert); //$NON-NLS-1$
//						nInsert = 0;
					}
				}
			}
			lineNum++;
		}

		if(isStored){
			// 残りを commit
			dbController.getConn().commit();
		}

		if(charWarnings.size() > 0){
			errorMessage += "* Warning(the number of skipped characters)\n"; //$NON-NLS-1$
		}
		for ( Character charCode : charWarnings.keySet() ) {
			int c = charWarnings.get( charCode );
			errorMessage += "  " + String.format("0x%04x", (int)charCode) + " : " + c + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}

		if(!errorMessage.equals(initErrorMessage)){
			System.err.println(errorMessage);
		}
		
		br.close();
		is.close();
		
		return;
	}
}

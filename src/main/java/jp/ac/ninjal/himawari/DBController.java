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


import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;

public class DBController {
	private static final int ERRCODE_DBUSING = 90020;

	private static final String DB_DRIVER = "org.h2.Driver"; //$NON-NLS-1$
	private static final String DB_PROTOCOL = "jdbc:h2:"; //$NON-NLS-1$
	private static final String DB_USER = "sa"; //$NON-NLS-1$
	private static final String DB_NAME = "himawari"; //$NON-NLS-1$
	private static final String DB_CACHE = ";CACHE_SIZE=500000"; // 500MB //$NON-NLS-1$
	private static final String PREFIX_INDEX = "idx_"; //$NON-NLS-1$
	
	
	public static final String SUFFIX_TABLE_DIC = "_DIC"; //$NON-NLS-1$
	public static final String TABLE_CORPUS = "_CORPUS"; // コーパス用のテーブル //$NON-NLS-1$
	// 自動アノテーション用テーブル
	public static final String TABLE_ANNOTATION = "_ANNOTATION"; //$NON-NLS-1$
	public static final String TABLE_ANNOTATION_DIC = TABLE_ANNOTATION + SUFFIX_TABLE_DIC;
	// 手動アノテーション用のテーブル
	public static final String TABLE_NOTE = "_NOTE"; //$NON-NLS-1$
	
	public static final String FIELD_CORPUS_ID = "_ID"; //$NON-NLS-1$
	public static final String FIELD_CORPUS_NAME = "_NAME"; //$NON-NLS-1$
	public static final String FIELD_ANNOTATION_ID = "_ID"; //$NON-NLS-1$
	public static final String FIELD_ANNOTATION_SEARCH_KEY = "_TEXT"; //$NON-NLS-1$
	public static final String FIELD_ANNOTATION_START = "_START"; //$NON-NLS-1$
	public static final String FIELD_ANNOTATION_END = "_END"; //$NON-NLS-1$
	public static final String FIELD_ANNOTATION_DIC_ID = "_DIC_ID"; //$NON-NLS-1$
	public static final String FIELD_DIC_ITEM_ID = "_ID"; //$NON-NLS-1$
	
	private Connection conn = null;
	private CorpusFile corpora[];
	private String sqlCreateCorpusTable =
			"CREATE TABLE IF NOT EXISTS " + TABLE_CORPUS + //$NON-NLS-1$
			" (" + FIELD_CORPUS_ID + " INT PRIMARY KEY AUTO_INCREMENT, " + FIELD_CORPUS_NAME + " VARCHAR(255));"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	private Properties props = new Properties();
	private String rootPath = ""; //$NON-NLS-1$
	private Map<String, Integer> corpusIdMap = new HashMap<String, Integer>();
	private int corpusResourceMap[]; // 引数：resourceID(corpora の引数の値)，値： corpusId
	private String dbPath;

	public String getDbPath() {return dbPath;}

	private Frame1 frame1;
	private boolean connected = true;

	private PreparedStatement preparedStatementInsert = null;
	private PreparedStatement preparedStatementUpdate = null;
	private PreparedStatement preparedStatementCheck = null;

	private PreparedStatement preparedStatementInsertDic = null;
	private PreparedStatement preparedStatementCheckDic = null;

	private PreparedStatement preparedStatementCheckCorpus = null;
	private PreparedStatement preparedStatementInsertCorpus = null;

	private Map<Map<String, Object>, Object> cacheMap = new HashMap<Map<String, Object>, Object>();

	
	/**
	 * コンストラク
	 * @param frame1
	 * @param path　dbファイルのパス
	 */
	public DBController(Frame1 frame1, String path) {
		props = new Properties();
		props.put("user", DB_USER); //$NON-NLS-1$
		String currentPath = System.getProperty("user.dir"); //$NON-NLS-1$
		this.rootPath = currentPath + "/" + path; //$NON-NLS-1$
		this.frame1 = frame1;
	}

	
	public void setCorpora(CorpusFile[] corpora){
		this.corpora = corpora;
		for(CorpusFile corpus : this.corpora){
			corpus.setDBController(this);
		}
	}
	
	/**
	 * db_init データベースへの接続と初期化を行う
	 */
	public void openConnection() {
		if(conn != null){
			return;
		}
		
		loadDriver();
		dbPath = rootPath + "//" + DB_NAME; //$NON-NLS-1$
		System.err.println("dbPath: " + dbPath); //$NON-NLS-1$
		try {
			conn = DriverManager.getConnection(DB_PROTOCOL + dbPath + DB_CACHE, props);
			conn.setAutoCommit(true);
			
			Statement statement = conn.createStatement();
			// コーパス用のテーブル
			statement.execute(sqlCreateCorpusTable);
			System.err.println(sqlCreateCorpusTable);
			
			// 手動アノテーション用のテーブル
//			statement.execute(sqlCreateManualAnnotationTable);
			conn.commit();
		} catch (SQLException e) {
			showDbError(e);
			System.exit(-1);
		}
	}

	private void loadDriver() {
		try {
			Class.forName(DB_DRIVER).getDeclaredConstructor().newInstance();
			System.out.println("Loaded the appropriate driver"); //$NON-NLS-1$
		} catch (ClassNotFoundException cnfe) {
			System.err.println("\nUnable to load the JDBC driver " + DB_DRIVER); //$NON-NLS-1$
			System.err.println("Please check your CLASSPATH."); //$NON-NLS-1$
			cnfe.printStackTrace(System.err);
		} catch (InstantiationException ie) {
			System.err.println("\nUnable to instantiate the JDBC driver " //$NON-NLS-1$
					+ DB_DRIVER);
			ie.printStackTrace(System.err);
		} catch (IllegalAccessException iae) {
			System.err.println("\nNot allowed to access the JDBC driver " //$NON-NLS-1$
					+ DB_DRIVER);
			iae.printStackTrace(System.err);
		} catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			System.err.println("\nUnable to instantiate the JDBC driver " //$NON-NLS-1$
					+ DB_DRIVER);
			e.printStackTrace(System.err);
		}	
	}


	public void closeConnection(){
		try{
			if (conn != null){
				conn.close();
			}
		}catch(SQLException e){
			showDbError(e);
		}

	}

	
	public void initManualAnnotationTable(CorpusFile corpora[], FieldInfo fieldInfo){

		for(int i = 0; i < fieldInfo.size(); i++){
			String fieldType = fieldInfo.getType(i);
			String fieldElementName = fieldInfo.getElementName(i);
			if(fieldType != null && fieldType.equalsIgnoreCase(FieldInfo.TYPE_DB) &&
					fieldElementName != null && fieldElementName.equalsIgnoreCase(DBController.TABLE_NOTE)){

				for(CorpusFile corpus: corpora){
					int corpusId = getCorpusId(corpus.getCorpusName());
					
					String noteTableName = TABLE_NOTE + "_" + corpusId; //$NON-NLS-1$
					String noteDicTableName = TABLE_NOTE + SUFFIX_TABLE_DIC;

					String sqlCreateManualAnnotationTable = 
							"CREATE TABLE IF NOT EXISTS " + noteTableName + //$NON-NLS-1$
							" (" + FIELD_ANNOTATION_ID + " INT PRIMARY KEY AUTO_INCREMENT, " + //$NON-NLS-1$ //$NON-NLS-2$
							FIELD_ANNOTATION_START + " INT, " + //$NON-NLS-1$
							FIELD_ANNOTATION_DIC_ID + " INT);" + //$NON-NLS-1$
							"CREATE TABLE IF NOT EXISTS " + noteDicTableName + //$NON-NLS-1$
							" (" + FIELD_DIC_ITEM_ID + " INT PRIMARY KEY AUTO_INCREMENT, " + //$NON-NLS-1$ //$NON-NLS-2$
							FIELD_ANNOTATION_SEARCH_KEY + " VARCHAR(255));"; //$NON-NLS-1$
					try {
						Statement statement = conn.createStatement();
						statement.execute(sqlCreateManualAnnotationTable);
						addIndex(noteTableName, PREFIX_INDEX + FIELD_ANNOTATION_START + "_" + DBController.TABLE_NOTE, "(" + FIELD_ANNOTATION_START + ")", false, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						addIndex(noteTableName, PREFIX_INDEX + FIELD_ANNOTATION_DIC_ID + "_" + DBController.TABLE_NOTE, "(" + FIELD_ANNOTATION_DIC_ID + ")", false, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						conn.commit();
						String fieldName = fieldInfo.getName(i);

						if(fieldName != null){
							addColumn(DBController.TABLE_NOTE, fieldName);
							String indexName = PREFIX_INDEX + fieldName + "_" + DBController.TABLE_NOTE; //$NON-NLS-1$ //$NON-NLS-2$
							addIndex(noteDicTableName, indexName, "(" + fieldName + ")", false, false); //$NON-NLS-1$ //$NON-NLS-2$
							conn.commit();
						}

					} catch (SQLException e) {
						e.printStackTrace();
						showDbError(e);
					}
				}
			}
		}
	}
	
	/**
	 * DB関連エラーをウインドウで表示する
	 * @param e
	 */
	public void showDbError(SQLException e) {

		if(e.getErrorCode() == ERRCODE_DBUSING){
			showErrorDialog(Messages.getString("Frame1.608")); //$NON-NLS-1$
			setFlagConnected(false);
		}else{
			showErrorDialog(e.getErrorCode()+":"+e.getMessage()); //$NON-NLS-1$
		}
	}

	/**
	 * エラーダイアログの表示
	 * @param msg
	 */
	public void showErrorDialog(String msg){
		JOptionPane.showMessageDialog(
				frame1, msg, Messages.getString("Frame1.609"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
	}

	
	public void setFlagConnected(boolean connected) {
		this.connected = connected;
	}

	
	public Connection getConn() {
		return conn;
	}

	public boolean isConnected() {
		if(conn != null){
			return true;
		}
		return false;
	}


	public boolean isFlagConnected() {
		return connected;
	}

	
	public boolean isFieldExist(String tableName, String fieldName){
		try {
			System.err.println("fieldname: " + fieldName); //$NON-NLS-1$
			ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, fieldName);
			if(rs.next()){ // exist?
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			showDbError(e);
		}
		return false;
	}

	
	// テーブルに新たに column を追加する
	// テーブル名は annotationName + SUFFIX_TABLE_DIC
	public void addColumn(String annotationName, String columnName){
		Statement statement;
		try {
			statement = conn.createStatement();
			// 列を追加するのは，dic テーブルのみ
			statement.execute("ALTER TABLE " + annotationName + SUFFIX_TABLE_DIC + " ADD COLUMN IF NOT EXISTS " + columnName + " VARCHAR(64)"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} catch (SQLException e) {
			e.printStackTrace();
			showDbError(e);
		}
		
	}
	
	
	/**
	 * corpusNameに紐付くコーパスIDを取得する
	 * @param corpusName
	 * @return コーパスID
	 */
	public int getCorpusId(String corpusName){
		if(corpusIdMap == null || corpusIdMap.size() == 0){
			createCorpusIdMap();
		}
		System.err.println("getcorpusid: " + corpusName); //$NON-NLS-1$
		if(corpusIdMap == null){
			System.err.println("null desu"); //$NON-NLS-1$
			System.exit(-1);
		} 
		return corpusIdMap.get(corpusName);
	}

	public int getCorpusId(int resourceId){
		return corpusResourceMap[resourceId];
	}

	
	/**
	 * コーパスの情報を登録し，
	 * dbの情報からコーパスidのハッシュマップを生成する
	 */
	public void createCorpusIdMap() {
		// コーパス登録用 sql 準備
		Map<String, Object> mapCorpus = new HashMap<String, Object>();
		mapCorpus.put(DBController.FIELD_CORPUS_NAME, null);
		setPreparedStatementForCorpus(mapCorpus);
		
		// コーパス名重複チェック用
		HashMap<String, Integer> corpusArrayMap = new HashMap<String, Integer>();

		for(int i = 0; i < corpora.length; i++){
			String corpusName = corpora[i].getCorpusName();
			if(corpusArrayMap.containsKey(corpusName)){
				showErrorDialog(Messages.getString("DBController.61")); //$NON-NLS-1$
				System.err.println(Messages.getString("DBController.62")); //$NON-NLS-1$
				System.exit(-1);
			} else {
				corpusArrayMap.put(corpusName,  i);
			}
			mapCorpus.put(DBController.FIELD_CORPUS_NAME, corpusName);
			insertCorpus(mapCorpus);
			System.err.println(corpusName);
		}
		corpusResourceMap = new int[corpora.length];
		
		ResultSet rs = select("select " + FIELD_CORPUS_ID + "," + FIELD_CORPUS_NAME + " from " + DBController.TABLE_CORPUS + ";"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		try {
			while(rs.next()){
				String corpusName = rs.getString(FIELD_CORPUS_NAME);
				int corpusId = rs.getInt(FIELD_CORPUS_ID);
				if(corpusArrayMap.containsKey(corpusName)){
					corpusIdMap.put(corpusName, corpusId);
					corpusResourceMap[corpusArrayMap.get(corpusName)] = corpusId;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	
	public ResultSet select(String sql){
		Statement stmt;
		ResultSet rs = null;
		
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return rs;
	}


	
	public void insert(Map<String, Object> mapMain, Map<String, Object> mapValue) {

		int id = -1;

		try {
			
			Integer ido = (Integer)cacheMap.get(mapValue);
			if(ido != null){
				id = ido.intValue();
			} else {
				preparedStatementInsertDic.clearParameters();
				preparedStatementInsertDic = setParameterToPreparedStatement(preparedStatementInsertDic, mapValue);
				preparedStatementInsertDic.executeUpdate();
				ResultSet rs = preparedStatementInsertDic.getGeneratedKeys();
				if(rs.next()){
					id = rs.getInt(1);
				}
				HashMap<String, Object> copyMap = new HashMap<String, Object>();
				copyMap.putAll(mapValue);
				cacheMap.put(copyMap, id);
			}
			
			mapMain.put(FIELD_ANNOTATION_DIC_ID, id);
			preparedStatementInsert.clearParameters();
			preparedStatementInsert = setParameterToPreparedStatement(preparedStatementInsert, mapMain);
			preparedStatementInsert.executeUpdate();
			mapMain.put(FIELD_ANNOTATION_DIC_ID, null);
		} catch (SQLException e) {
			e.printStackTrace();
			showDbError(e);
		}
	}

	
	
	
	public void interactiveUpdate(Map<String, Object> mapMain, Map<String, Object> mapValue) {

		try {
			// dic に登録されているか
			preparedStatementCheckDic.clearParameters();
			preparedStatementCheckDic = setParameterToPreparedStatement(preparedStatementCheckDic, mapValue);
			ResultSet rsDic = preparedStatementCheckDic.executeQuery();
			
			int dicItemId = -1;
			// dic に登録されている場合
			if(rsDic.next()){
				dicItemId = rsDic.getInt(FIELD_DIC_ITEM_ID);
			}
			// dic に登録されていない場合
			else {
				preparedStatementInsertDic.clearParameters();
				preparedStatementInsertDic = setParameterToPreparedStatement(preparedStatementInsertDic, mapValue);
				preparedStatementInsertDic.executeUpdate();
				ResultSet rs = preparedStatementInsertDic.getGeneratedKeys();
				if(rs.next()){
					dicItemId = rs.getInt(1);
				}
			}

			preparedStatementCheck.clearParameters();
			preparedStatementCheck = setParameterToPreparedStatement(preparedStatementCheck, mapMain);
			ResultSet rsMain = preparedStatementCheck.executeQuery();

			int mainId = -1;
			int mainDicItemId = -1;
			// main に登録されている場合
			if(rsMain.next()){
				mainId = rsMain.getInt(FIELD_ANNOTATION_ID);
				mainDicItemId = rsMain.getInt(FIELD_ANNOTATION_DIC_ID);
				
				if(mainDicItemId != dicItemId){
					preparedStatementUpdate.clearParameters();
					preparedStatementUpdate.setInt(1, dicItemId);
					preparedStatementUpdate.setInt(2, mainId);
					preparedStatementUpdate.executeUpdate();
				}
			}
			// main に登録されていない場合
			else {
				Map<String, Object> mapNewMain = new HashMap<String, Object>(mapMain);
				mapNewMain.put(FIELD_ANNOTATION_DIC_ID, dicItemId);
				preparedStatementInsert.clearParameters();
				preparedStatementInsert = setParameterToPreparedStatement(preparedStatementInsert, mapNewMain);
				preparedStatementInsert.executeUpdate();
//				mapMain.put(FIELD_ANNOTATION_DIC_ID, null); // 初期化
			}
		} catch (SQLException e) {
			e.printStackTrace();
			showDbError(e);
		}
	}

	public void clearCache(){
		cacheMap.clear();
	}
	

	public void setPreparedStatementForCorpus(Map<String, Object> mapCorpus) {

		String sql = ""; //$NON-NLS-1$

		try {
			// count
			sql = "SELECT " + FIELD_CORPUS_ID + //$NON-NLS-1$
					" FROM " + TABLE_CORPUS +  //$NON-NLS-1$
					" WHERE " + getDummyParameters(mapCorpus, "=?", " and "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			preparedStatementCheckCorpus = conn.prepareStatement(sql);

			// insert
			sql = "INSERT INTO " + TABLE_CORPUS +  //$NON-NLS-1$
					" (" + getDummyParameters(mapCorpus, "", ",") + ") " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					"VALUES (" + getDummyValues(mapCorpus) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			preparedStatementInsertCorpus = conn.prepareStatement(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println(sql);
			System.exit(-1);
			showDbError(e);
		}
	}


	public void insertCorpus(Map<String, Object> mapCorpus) {

		try {
			preparedStatementCheckCorpus.clearParameters();
			preparedStatementCheckCorpus = setParameterToPreparedStatement(preparedStatementCheckCorpus, mapCorpus);
			ResultSet rs = preparedStatementCheckCorpus.executeQuery();

			if(!rs.next()){
				preparedStatementInsertCorpus.clearParameters();
				preparedStatementInsertCorpus = setParameterToPreparedStatement(preparedStatementInsertCorpus, mapCorpus);
				preparedStatementInsertCorpus.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			showDbError(e);
		}
	}


	public void setPreparedStatementForAnnotation(String annotationName, int corpusId, Map<String, Object> mapMain, Map<String, Object> mapValue) {

		String sql = ""; //$NON-NLS-1$
		String tableName = annotationName + "_" + corpusId; //$NON-NLS-1$

		try {
			// insert(main)
			Map<String, Object> mapNewMain = new HashMap<String, Object>(mapMain);
			mapNewMain.put(FIELD_ANNOTATION_DIC_ID, null);
			sql = "INSERT INTO " + tableName + //$NON-NLS-1$
					" (" + getDummyParameters(mapNewMain, "", ",") + ") " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					+ "VALUES (" + getDummyValues(mapNewMain) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			preparedStatementInsert = conn.prepareStatement(sql);


			// count (dic)
			sql = "SELECT " + FIELD_DIC_ITEM_ID + //$NON-NLS-1$
					" FROM " + annotationName + SUFFIX_TABLE_DIC + //$NON-NLS-1$
					" WHERE " + getDummyParameters(mapValue, "=?", " and "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			preparedStatementCheckDic = conn.prepareStatement(sql);

			// insert (dic)
			sql = "INSERT INTO " + annotationName + SUFFIX_TABLE_DIC + //$NON-NLS-1$
					" (" + getDummyParameters(mapValue, "", ",") + ") " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					+ "VALUES (" + getDummyValues(mapValue) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			preparedStatementInsertDic = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			
		} catch (SQLException e) {
			e.printStackTrace();
			showDbError(e);
		}
	}

	

	public void setPreparedStatementInteractiveAnnotation(String annotataionName, int corpusId, Map<String, Object> mapMain, Map<String, Object> mapValue) {

		String sql = ""; //$NON-NLS-1$

		try {
			// check (main)
			sql = "SELECT " + FIELD_ANNOTATION_ID + ", " + FIELD_ANNOTATION_DIC_ID + //$NON-NLS-1$ //$NON-NLS-2$
					" FROM " + annotataionName + "_" + corpusId + //$NON-NLS-1$ //$NON-NLS-2$
					" WHERE " + getDummyParameters(mapMain, "=?", " and "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			preparedStatementCheck = conn.prepareStatement(sql);

			// update(main)
			sql = "UPDATE " + annotataionName + "_" + corpusId + //$NON-NLS-1$ //$NON-NLS-2$
					" SET " + FIELD_ANNOTATION_DIC_ID + "=?" + //$NON-NLS-1$ //$NON-NLS-2$
					" WHERE " + FIELD_ANNOTATION_ID + "=?"; //$NON-NLS-1$ //$NON-NLS-2$
			preparedStatementUpdate = conn.prepareStatement(sql);

			// insert(main)
			//// insert時は，すべてのフィールドを使うので，FIELD_ANNOTATION_DIC_IDを追加
			Map<String, Object> mapNewMain = new HashMap<String, Object>(mapMain);
			mapNewMain.put(FIELD_ANNOTATION_DIC_ID, null);
			sql = "INSERT INTO " + annotataionName + "_" + corpusId + //$NON-NLS-1$ //$NON-NLS-2$
					" (" + getDummyParameters(mapNewMain, "", ",") + ") " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					+ "VALUES (" + getDummyValues(mapNewMain) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			preparedStatementInsert = conn.prepareStatement(sql);
			

			// check (dic)
			sql = "SELECT " + FIELD_ANNOTATION_ID +  //$NON-NLS-1$
					" FROM " + annotataionName + SUFFIX_TABLE_DIC + //$NON-NLS-1$
					" WHERE " + getDummyParameters(mapValue, "=?", " and "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			preparedStatementCheckDic = conn.prepareStatement(sql);

			// insert (dic)
			sql = "INSERT INTO " + annotataionName + SUFFIX_TABLE_DIC + //$NON-NLS-1$
					" (" + getDummyParameters(mapValue, "", ",") + ") " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					+ "VALUES (" + getDummyValues(mapValue) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			preparedStatementInsertDic = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			
		} catch (SQLException e) {
			e.printStackTrace();
			showDbError(e);
		}
	}


	public String getDummyParameters(Map<String, Object> map, String dummyValue, String delimiter){
		int n = map.keySet().size();
		
		if(n < 1){
			return ""; //$NON-NLS-1$
		} else {
			return StringUtils.join(map.keySet(), dummyValue + delimiter) + dummyValue;
		}
	}

	public String getDummyValues(Map<String, Object> map){
		int n = map.keySet().size();
		
		StringBuilder result = new StringBuilder("?"); //$NON-NLS-1$
		for (int i = 0; i < n-1; i++) {
			result.append(", ?"); //$NON-NLS-1$
		}
	
		return result.toString();
	}
	
	
	

	

	public PreparedStatement setParameterToPreparedStatement(
			PreparedStatement preparedStatement, Map<String, Object> map) throws SQLException{

		int i = 1;
		
		for(Object value : map.values()){
			if(value == null){
//				preparedStatement.setString(i++, null);
			} else if(value.getClass().getSimpleName().compareTo("Integer") == 0){ //$NON-NLS-1$
				preparedStatement.setInt(i++, (Integer) value);
			} else {
				preparedStatement.setString(i++, (String) value);
			}
		}

		return preparedStatement;
	}


	
	public void commit() {
		try {
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			showDbError(e);
		}
	}

	
	public void initTable(String annotationName, int corpusId){
		// mainTableName = annotationName  + "_" + corpusId

		Statement statement;
		String mainTableName = annotationName + "_" + corpusId; //$NON-NLS-1$
		String mainDicTableName = annotationName + SUFFIX_TABLE_DIC;

		// コーパスと対応付けられたデータ
		String sqlCreateAnnotationTable = 
				"CREATE TABLE " + mainTableName +  //$NON-NLS-1$
				" (" + FIELD_ANNOTATION_ID + " INT PRIMARY KEY AUTO_INCREMENT, " + //$NON-NLS-1$ //$NON-NLS-2$
				FIELD_ANNOTATION_START + " INT, " + //$NON-NLS-1$
				FIELD_ANNOTATION_DIC_ID  + " INT);"; //$NON-NLS-1$

		String sqlCreateAnnotationDicTable = 
				"CREATE TABLE IF NOT EXISTS " + mainDicTableName + //$NON-NLS-1$
				" (" + FIELD_DIC_ITEM_ID + " INT PRIMARY KEY HASH AUTO_INCREMENT, " + //$NON-NLS-1$ //$NON-NLS-2$
							FIELD_ANNOTATION_SEARCH_KEY + " VARCHAR(64));"; //$NON-NLS-1$

		try {
			statement = conn.createStatement();
			// コーパス用のテーブル
			statement.execute("DROP TABLE IF EXISTS " + mainTableName); //$NON-NLS-1$
			conn.commit();
			statement.execute(sqlCreateAnnotationTable);
			statement.execute(sqlCreateAnnotationDicTable);
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			showDbError(e);
		}
	}
	

	public void executeSQL(String sql){
		Statement statement;
		try {
			statement = conn.createStatement();
			// 列を追加
			statement.execute(sql);
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			showDbError(e);
		}
	}
	
	
	public String getTableName(String corpusName, String elementName){
		return elementName + "_" + getCorpusId(corpusName); //$NON-NLS-1$
	}

	
	public void addIndex(String targetTable, String indexName, String targetFields, boolean isUnique, boolean isHash){
		StringBuffer command = new StringBuffer("CREATE "); //$NON-NLS-1$
		
		executeSQL("DROP index IF EXISTS " + indexName); //$NON-NLS-1$

		if(isUnique){
			command.append("UNIQUE "); //$NON-NLS-1$
		}
		if(isHash){
			command.append("HASH "); //$NON-NLS-1$
		}
		executeSQL(command + "index " + indexName + " ON " + targetTable + " " + targetFields); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		System.err.println("add: " + command + "index " + indexName + " ON " + targetTable + " " + targetFields); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
}

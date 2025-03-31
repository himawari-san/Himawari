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

import java.util.*;
import java.util.regex.*;
import javax.swing.table.*;


/**
 * <p>
 * タイトル:
 * </p>
 * <p>
 * 説明:
 * </p>
 * <p>
 * 著作権: Copyright (c) 2003
 * </p>
 * <p>
 * 会社名:
 * </p>
 *
 * @author Masaya YAMAGUCHI
 * @version 0.9.4
 */

/**
 * @author masaya
 *
 */
public class ResultTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
//	private static final int WAIT_COMMIT_UNTIL = 5000;
	private String[] header;
	private ArrayList<ResultRecord> data;
	private ArrayList<ResultRecord> filteredData;
	private HashMap<String, ArrayList<String>> itemLists = new HashMap<String, ArrayList<String>>();
	private FieldInfo fieldinfo;
	// private String strPcontextFieldName = "前文脈";
	// private String fieldNameWithNumeric = "頻度";

	// 編集前の ResultRecord のリスト
	private HashMap<String, ResultRecord> originalData;
	// 編集後の ResultRecord のリスト
	private HashMap<String, ResultRecord> edittedData;
	
	private DBController dbController;

	private Frame1 parentFrame;


	public ResultTableModel() {
		data = new ArrayList<ResultRecord>();
		filteredData = new ArrayList<ResultRecord>();
		header = new String[1];
		originalData = new HashMap<String, ResultRecord>();
		edittedData = new HashMap<String, ResultRecord>();
	}

	public int getColumnCount() {
		if (header != null) {
			return header.length;
		} else {
			return 0;
		}
	}

	public Object getValueAt(int parm1, int parm2) {
		Object ret = null;
		if(filteredData == null){
			return null;
		}

		try {
			ret = filteredData.get(parm1).get(parm2);
		} catch (IndexOutOfBoundsException e) {
			// e.printStackTrace();
			// 検索の実行後にスレッドで発生することが時々ある。
			// 検索結果やアプリの挙動に対して影響が無いため、
			// 握りつぶしておく。
			ret = new String(""); //$NON-NLS-1$
		}
		return ret;
	}

	public String getColumnName(int column) {
		return header[column];
	}

	public int getRowCount() {
		return filteredData.size();
	}

	public boolean isCellEditable(int row, int column) {
		if (fieldinfo.isEditable(column)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void setValueAt(Object value, int row, int column) {

		ResultRecord currentRecord = filteredData.get(row);

		// 前の値の取得
		Object before = getValueAt(row, column);

		//null処理
		if(value == null){
			value = ""; //$NON-NLS-1$
		}

		// 前の値と比較し変更があればデータを編集済みリストに登録
		if (!value.equals(before)) {
			String keyOriginal =
					currentRecord.getPosition() + "\t" + //$NON-NLS-1$
					currentRecord.getResourceID() + "\t" + //$NON-NLS-1$
					currentRecord.get(ResultRecord.PRIMARY_KEY_FIELD) + "\t" + //$NON-NLS-1$
					column;
			if(!originalData.containsKey(keyOriginal)){
				// バックアップ用の ResultRecord の copy (一部のみ)
				ResultRecord copyRecord = new ResultRecord(currentRecord.getFieldInfo());
				copyRecord.setPosition(currentRecord.getPosition());
				copyRecord.setResourceID(currentRecord.getResourceID());
				copyRecord.set(column, currentRecord.get(column));
				copyRecord.set(ResultRecord.PRIMARY_KEY_FIELD, currentRecord.get(ResultRecord.PRIMARY_KEY_FIELD));
				// 登録
				originalData.put(keyOriginal, copyRecord);
			}
			checkEditted();
		}

		
		// 値の変更
		try{
			filteredData.get(row).set(column, value);
		}catch(ArrayIndexOutOfBoundsException e){
			System.err.println("filteredData size="+filteredData.size()); //$NON-NLS-1$
			System.err.println(String.format("row=$d, col=%d", row, column)); //$NON-NLS-1$

			return;
		}

		// save するレコードを edittedData に登録
		String keyEditted =
				currentRecord.getPosition() + "\t" + //$NON-NLS-1$
				currentRecord.getResourceID() + "\t" + //$NON-NLS-1$
				currentRecord.get(ResultRecord.PRIMARY_KEY_FIELD);
		edittedData.put(keyEditted, currentRecord);
	}

	
	
	
	public void setData(ArrayList<ResultRecord> data) {
		this.data = data;
		initFilter();
	}

	public void updateData() {
		itemLists.clear();
		itemLists = new HashMap<String, ArrayList<String>>();
		fireTableDataChanged();
	}

	public ArrayList<ResultRecord> getData() {
		return data;
	}

	public ArrayList<ResultRecord> getFilteredData() {
		return filteredData;
	}

	/*
	 * public void setHeader(String[] header){ this.header = header;
	 * this.fireTableStructureChanged(); // this.fireTableDataChanged(); }
	 */

	public void setFieldInfo(FieldInfo fieldinfo) {
		this.fieldinfo = fieldinfo;
		header = fieldinfo.getNames();
		this.fireTableStructureChanged();
	}

	public FieldInfo getFieldInfo() {
		return fieldinfo;
	}

	public ResultRecord getRecordAt(int row) {
		return filteredData.get(row);
	}

	/**
	 * フィルタ済データと itemLists を初期化する
	 */
	public void initFilter() {
		// データをコピー
		filteredData = new ArrayList<ResultRecord>();
		filteredData.addAll(data);
		updateData();
		// this.fireTableDataChanged();
	}

	/**
	 * データをフィルタリングする
	 *
	 * @param filter
	 *            フィルタ
	 */
	public void filterData(HashMap<String, String> filter) {

		String filterKeys[] = filter.keySet().toArray(new String[0]);
		ArrayList<ResultRecord> newResults = new ArrayList<ResultRecord>();
		
		boolean flagMatch;
		for(ResultRecord resultRecord : filteredData) {
			flagMatch = true;
			for (String filterKey : filterKeys) {
				String fieldValue = resultRecord.get(filterKey) == null ? "" : resultRecord.get(filterKey).toString(); //$NON-NLS-1$
				if (!filter.get(filterKey).equals(fieldValue)) {
					flagMatch = false;
					break;
				}
			}
			if(flagMatch){
				newResults.add(resultRecord);
			}
		}
		filteredData.clear();
		filteredData.addAll(newResults);
		
		updateData();
	}


	/**
	 * データをフィルタリングする(正規表現指定)
	 *
	 * @param filter
	 *            フィルタ
	 */
	public void filterData_with_regex(HashMap<String, String> filter) {

		ArrayList<ResultRecord> newResults = new ArrayList<ResultRecord>();
		String filterKeys[] = filter.keySet().toArray(new String[0]);

		// Patterns for keys
		Pattern keyPattern[] = new Pattern[filterKeys.length];
		for (int i = 0; i < filterKeys.length; i++) {
			keyPattern[i] = Pattern.compile((String) (filter.get(filterKeys[i])));
		}

		boolean flagMatch;
		for(ResultRecord resultRecord : filteredData) {
			flagMatch = true;
			for(int j = 0; j < filterKeys.length; j++) {
				String fieldValue =  resultRecord.get(filterKeys[j]) == null ? "" : resultRecord.get(filterKeys[j]).toString(); //$NON-NLS-1$
				if(fieldValue == null){
					fieldValue = ""; //$NON-NLS-1$
				}
				Matcher keyMatcher = keyPattern[j].matcher(fieldValue);
				if (keyMatcher != null && !keyMatcher.find()) {
					flagMatch = false;
					break;
				}
			}
			if(flagMatch){
				newResults.add(resultRecord);
			}
		}
		filteredData.clear();
		filteredData.addAll(newResults);
		updateData();
	}

	/**
	 * Sort data of this table
	 *
	 * @param headerName
	 *            ソートする列名
	 * @param isAscendingOrder
	 *            並び順(昇順のとき true)
	 */
	public void sortData(String headerName, boolean isAscendingOrder) {
		Comparator<ResultRecord> pc;
		String sortType = fieldinfo.getSortType(fieldinfo.get(headerName));
		int sortDirection = fieldinfo.getSortDirection(fieldinfo
				.get(headerName));

		if (sortDirection == FieldInfo.SORT_DIRECTION_R_L) {
			pc = new PrevStringComparator(headerName, isAscendingOrder);
		} else if (sortType.compareTo(FieldInfo.SORT_TYPE_NUMERIC) == 0) {
			pc = new NumericComparator(headerName, isAscendingOrder);
		} else if (sortType.compareTo(FieldInfo.SORT_TYPE_VECTOR) == 0) {
			pc = new NumericComparator(headerName, isAscendingOrder);
		} else if (headerName.equals(FieldInfo.FIELDNAME_FREQ)) {
			pc = new NumericComparator(headerName, isAscendingOrder);
		} else {
			pc = new StringComparator(headerName, isAscendingOrder);
		}

		fieldinfo.resetFieldStatus();
		if(isAscendingOrder){
			fieldinfo.setFieldStatus(fieldinfo.get(headerName), FieldInfo.STATUS_SORT_ASCENDING);
		} else {
			fieldinfo.setFieldStatus(fieldinfo.get(headerName), FieldInfo.STATUS_SORT_DESCENDING);
		}

		
		Collections.sort(filteredData, pc);
/*
		filteredData = new ArrayList<ResultRecord>(obj.length);
		for (int i = 0; i < obj.length; i++) {
			filteredData.add(obj[i]);
		}
*/
		updateData();
	}

	/**
	 * データをランダムな順番で並びかえる
	 *
	 */
	public void sortDataRandamly() {
		int size = filteredDataSize();

		for (int i = 0; i < size; i++) {
			int rand = (int) (Math.random() * (size - i));
			ResultRecord rr = filteredData.remove(rand + i);
			filteredData.add(0, rr);
		}
		fieldinfo.resetFieldStatus();
		updateData();
	}

	/**
	 * データから，列 headername で，セルの値が target のレコードを検索する
	 *
	 * @param target
	 * @param headerName
	 * @param offset
	 * @return
	 */
	public ResultRecord findData(String target, String headerName, int offset) {

		for (ResultRecord resultRecord : filteredData) {
			String value = (String) resultRecord.get(headerName);
			if (value.equals(target)) {
				return resultRecord;
			}
		}
		return null;
	}

	
	public int replaceData(String headerName, String regxFrom, String regxTo) {
		int n = 0;
		
		Pattern pFrom = Pattern.compile(regxFrom);
		Matcher mFrom;
		
		for(ResultRecord resultRecord : filteredData){
			String value = (String) resultRecord.get(headerName);
			if(value == null){
				value = ""; //$NON-NLS-1$
			}
			mFrom = pFrom.matcher(value);
			if(mFrom.find()){
				resultRecord.set(headerName, mFrom.replaceAll(regxTo));
				n++;
			}
		}
		
		updateData();
		return n;
	}
	
	
	
	
	/**
	 * データから，列 headername でセルの値が target のレコードの行番号を返す
	 *
	 * @param target
	 * @param headerName
	 * @param offset
	 * @return レコードの行番号(先頭は 0，見つからなければ -1)
	 */
	public int getDataIndex(Object target, String headerName, int offset) {
		int size = filteredDataSize();

		for (int i = offset; i < size; i++) {
			ResultRecord resultRecord = filteredData.get(i);
			Object value = resultRecord.get(headerName);
			if (value.equals(target)) {
				return i;
			}
		}
		return -1;
	}

	public int getDataIndex(int resourceID, int position) {
		int size = filteredDataSize();

		for (int i = 0; i < size; i++) {
			ResultRecord resultRecord = filteredData.get(i);
			if (resultRecord.getPosition() == position
					&& resultRecord.getResourceID() == resourceID) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * データから，列 headername でセルの値が target のレコードの行番号を返す
	 *
	 * @param target
	 * @param headerName
	 * @param offset
	 * @return レコードの行番号(先頭は 0，見つからなければ -1)
	 */
	public int getDataIndexWithRegex(String regex, String headerName, int offset) {
		int size = filteredDataSize();

		for (int i = offset; i < size; i++) {
			ResultRecord resultRecord = filteredData.get(i);
			Object value = resultRecord.get(headerName);
			if (value == null){
				// do nothing
			} else if(value.getClass().getName().compareTo("java.lang.String") != 0) { //$NON-NLS-1$
				return -1;
			} else {
				Pattern p = Pattern.compile(regex);
				Matcher m = p.matcher((String) value);
				if (m.find()) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * 設定されているデータサイズを返す
	 *
	 * @return データサイズ
	 */
	public int dataSize() {
		if (data == null) {
			return 0;
		} else {
			return data.size();
		}
	}

	public int filteredDataSize() {
		if (filteredData == null) {
			return 0;
		} else {
			return filteredData.size();
		}
	}

	public HashMap<String, ArrayList<String>> getItemLists() {
		return itemLists;
	}

	/**
	 * 列の項目の異なりを求める
	 *
	 * @param fieldName
	 *            フィールド名
	 * @return 異なりの Vector
	 */
	public ArrayList<String> makeItemListFromData(String fieldName) {
		HashSet<String> itemList = new HashSet<String>();
		String item;
		Object v;

		for (ResultRecord r : filteredData) {
			v = r.get(fieldName);
			if(v == null){
				item = ""; //$NON-NLS-1$
			} else {
				item = v.toString();
			}

			if (!itemList.contains(item)) {
				itemList.add(item);
			}
		}
		return new ArrayList<String>(itemList);
	}

	void sortDataWithMultiColumn() {
		int currentOrder = Integer.MAX_VALUE;
		int targetFieldNo = 0;
		int fieldSize = fieldinfo.size();

		for (int i = 0; i < fieldSize; i++) {
			int maxOrder = Integer.MIN_VALUE;
			for (int j = 0; j < fieldSize; j++) {
				int valueOrder = fieldinfo.getSortOrder(j);
				if (maxOrder < valueOrder && currentOrder > valueOrder) {
					maxOrder = valueOrder;
					targetFieldNo = j;
				}
			}
			if (currentOrder < 1) {
				break;
			}
			currentOrder = maxOrder;
			int sortOrder = FieldInfo.STATUS_SORT_ASCENDING;
			if((fieldinfo.getElementName(targetFieldNo).equals(FieldInfo.ELEMENT_SYSTEM)
					&& fieldinfo.getAttributeName(targetFieldNo).equals(FieldInfo.ATTRIBUTE_FREQ))
					|| fieldinfo.getName(targetFieldNo).equals(FieldInfo.FIELDNAME_FREQ)){
				sortData(fieldinfo.getName(targetFieldNo), false);
				sortOrder = FieldInfo.STATUS_SORT_DESCENDING;
			} else {
				sortData(fieldinfo.getName(targetFieldNo), true);
			}

			if (currentOrder == 1) {
				fieldinfo.setFieldStatus(targetFieldNo, sortOrder);
				break;
			}
		}
	}

	/**
	 * 編集されているか？
	 * @return true:編集されている false:未編集
	 */
	public boolean isEditted() {
		return originalData.size() > 0 ? true : false;
	}

	/**
	 * db管理クラスをセットする
	 * @param dbController
	 */
	public void setDbController(DBController dbController) {
		this.dbController = dbController;
	}

	/**
	 * 編集したデータを保存する
	 */
	public void saveEdittedData(){

		ArrayList<String> dbFields = new ArrayList<String>();
		for(int i = 0; i < fieldinfo.size(); i++){
			if(!fieldinfo.isDbEditable(i)){
				continue;
			}
//			System.err.println("fi:" + fieldinfo.getName(i));
			dbFields.add(fieldinfo.getName(i));
		}

		// map initialization
		//// main 側
		//// where で使われるもののみ
		//// FIELD_ANNOTATION_DIC_IDは，setPreparedStatementInteractiveAnnotation中で追加
		Map<String, Object> mapMain = new HashMap<String, Object>();
		mapMain.put(DBController.FIELD_ANNOTATION_START,  null);

		//// dic 側
		Map<String, Object> mapValue = new HashMap<String, Object>();
		for(String fieldName: dbFields){
			mapValue.put(fieldName, ""); //$NON-NLS-1$
		}
		mapValue.put(DBController.FIELD_ANNOTATION_SEARCH_KEY,  null);
		
//		System.err.println("update sizee: " + edittedData.size());
		while (!edittedData.isEmpty()) {
			int currentResourceId = -1;
			Iterator<Map.Entry<String, ResultRecord>> it = edittedData.entrySet().iterator();
			while(it.hasNext()){
				Map.Entry<String, ResultRecord> entry = it.next();
				ResultRecord record = entry.getValue();

				int resourceId = record.getResourceID();
				if (currentResourceId == -1) {
					currentResourceId = resourceId;
//					System.err.println("a:" + mapMain.size());
					dbController.setPreparedStatementInteractiveAnnotation(DBController.TABLE_NOTE, dbController.getCorpusId(resourceId), mapMain, mapValue);
				} else if (currentResourceId != resourceId) {
					continue;
				}
				it.remove();
				
				for (String fieldName : dbFields) {
					String value = (String) record.get(fieldName);
					if (value == null) {
						value = ""; //$NON-NLS-1$
					}
					mapValue.put(fieldName, value);
				}

				// start
				int start = record.getPosition();
				// 検索キー
				String searchKey = (String) record
						.get(ResultRecord.PRIMARY_KEY_FIELD);

				// 検索条件の作成
				System.err.println("cid: " + resourceId + " = " + start); //$NON-NLS-1$ //$NON-NLS-2$
				mapMain.put(DBController.FIELD_ANNOTATION_START, start);
				mapValue.put(DBController.FIELD_ANNOTATION_SEARCH_KEY,
						searchKey);
				// 何らかの文字列があれば更新または保存
				dbController.interactiveUpdate(mapMain, mapValue);
			}
		}
		// 残りをcommit
		dbController.commit();

		//edittedDataのリセット
		originalData.clear();
//		edittedData.clear();
		
//		getAnnotationList(current_search_key, "search_key");
		System.err.println("annotation saved"); //$NON-NLS-1$
	}

	/**
	 * 編集したデータをすべてもとに戻す(全て)
	 */
	public void revertEdittedData(){
		revertEdittedData(null, null);
	}

	
	
	/**
	 * 編集したデータをすべてもとに戻す(選択したもののみ)
	 * @param selectedFields
	 * @param selectedRows
	 */
	public void revertEdittedData(int[] selectedFields, int[] selectedRows) {

		if(selectedRows == null){
			ArrayList<Integer> dbFields = new ArrayList<Integer>();
			for(int i = 0; i < fieldinfo.size(); i++){
				if(!fieldinfo.getType(i).equalsIgnoreCase(FieldInfo.TYPE_DB)){
					continue;
				}
				dbFields.add(i);
			}

			Iterator<ResultRecord> itFilteredData = filteredData.iterator();

			while(itFilteredData.hasNext()){
				ResultRecord currentRecord = itFilteredData.next();
				Iterator<Integer> itDBField = dbFields.iterator();
				while(itDBField.hasNext()){
					int fieldNo = itDBField.next();
					String key =
							currentRecord.getPosition() + "\t" + //$NON-NLS-1$
							currentRecord.getResourceID() + "\t" + //$NON-NLS-1$
							currentRecord.get(ResultRecord.PRIMARY_KEY_FIELD) + "\t" + //$NON-NLS-1$
							fieldNo;
					if(originalData.containsKey(key)){
						ResultRecord originalRecord = originalData.remove(key);
						currentRecord.set(fieldNo,  originalRecord.get(fieldNo));
					}
				}
			}
		} else {
			for (int row = 0; row < selectedRows.length; row++) {
				ResultRecord currentRecord = getRecordAt(selectedRows[row]);
				for (int fieldNo = 0; fieldNo < selectedFields.length; fieldNo++) {
					String key =
							currentRecord.getPosition() + "\t" + //$NON-NLS-1$
							currentRecord.getResourceID() + "\t" + //$NON-NLS-1$
							currentRecord.get(ResultRecord.PRIMARY_KEY_FIELD) + "\t" + //$NON-NLS-1$
							selectedFields[fieldNo];
					
					if(originalData.containsKey(key)){
						ResultRecord originalRecord = originalData.remove(key);
						currentRecord.set(selectedFields[fieldNo],  originalRecord.get(selectedFields[fieldNo]));
					}
				}
			}
		}

		checkEditted();
		System.err.println("annotation reverted"); //$NON-NLS-1$

	}

	private void checkEditted(){
		//編集済みデータが無ければ保存・元に戻すボタンをdisableにする
		if(originalData.size()== 0){
			parentFrame.setEnableButtonSaveDb(false);
			parentFrame.setEnableButtonReverDb(false);
		}else{
			parentFrame.setEnableButtonSaveDb(true);
			parentFrame.setEnableButtonReverDb(true);
		}
	}



	public void setParentFrame(Frame1 parentFrame) {
		this.parentFrame = parentFrame;
	}

	public Frame1 getParentFrame() {
		return parentFrame;
	}


	public int sumFreq(){
		return sumFreq(getData());
	}

	public int sumFreqFilteredData(){
		return sumFreq(filteredData);
	}

	
	public int sumFreq(ArrayList<ResultRecord> data){
		int sum = 0;
		int iFreq = fieldinfo.getIndexFreq();
		
		if(iFreq != -1){ // a frequency field exists
			for(ResultRecord r : data){
				sum += (int)r.get(iFreq);
			}
		} else {
			sum = data.size();
		}
		
		return sum;
	}
}

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

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class ArgumentDatabaseIndex extends ArgumentIndex {

	private DBController dbController;

	private PreparedStatement preparedStatementSelect;
	private ArrayList<String> requiredColumns = new ArrayList<String>();

	private final static String COLUMN_NAME_GLUE = "\t"; //$NON-NLS-1$
	private String fieldName; // for storing the key string
	private FieldInfo fieldInfo;
	private ResultSet rs;
	private String tableName;
	private CorpusFile corpus;
	
	public ArgumentDatabaseIndex(String tableName, String fieldName, boolean isCompleteMatch, CorpusFile corpus) {
		elementName = tableName;
		argumentName = fieldName;
		isDB = true;
		this.isCompleteMatch = isCompleteMatch;
		this.corpus = corpus;
	}
	

	public void setRetrieveCondition(FieldInfo fieldInfo, String fieldName) {
		this.fieldInfo = fieldInfo;
		this.fieldName = fieldName;
	}


	public void mkaix(boolean isCompleteMatch){
		System.err.println("Warning(ArgumentDatabaseIndex): No update for the database by this process"); //$NON-NLS-1$
	}
	
	public boolean isOpen() {
		return true;
	}

	
	@Override
	public boolean exists() {
		return true;
	}
	
	@Override
	public void close() throws IOException {
		// DB連携では特にやることなし
		return;
	}

	@Override
	public String getIOFilename() {
		if(dbController == null){
			System.err.println("db null"); //$NON-NLS-1$
			return null;
		}
		return dbController.getDbPath();
	}


	public String getFilename() {
		return dbController.getDbPath();
	}

	
	@Override
	public void open() throws IOException {
		dbController = corpus.getDBController();
		tableName = dbController.getTableName(corpus.getCorpusName(), elementName);

		// この要素はすでにDBで検索済みだが，フィルターを通していないことを宣言
		processedElement = elementName;

		requiredColumns = new ArrayList<String>();
		requiredColumns.add(DBController.FIELD_ANNOTATION_START);
		requiredColumns.add(DBController.FIELD_ANNOTATION_SEARCH_KEY);

		for (int i = 0; i < fieldInfo.size(); i++) {
			String name = fieldInfo.getElementName(i);
			if (name.compareTo(elementName) == 0) {
				String attr = fieldInfo.getAttributeName(i);
				requiredColumns.add(attr);
			}
		}

		// PreparedStatement
		String sql;
		if(isCompleteMatch) {
			sql = "SELECT " + StringUtils.join(requiredColumns, ",") + //$NON-NLS-1$ //$NON-NLS-2$
					" FROM " + tableName + " as m, " + elementName + DBController.SUFFIX_TABLE_DIC + " as d " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					" WHERE " + "m." + DBController.FIELD_ANNOTATION_DIC_ID + "= d." + DBController.FIELD_DIC_ITEM_ID + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					" AND " + argumentName + " = ?"; //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			sql = "SELECT " + StringUtils.join(requiredColumns, ",") +  //$NON-NLS-1$ //$NON-NLS-2$
					" FROM " + tableName + " as m, " + elementName + DBController.SUFFIX_TABLE_DIC + " as d " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					" WHERE " + argumentName + " LIKE ? AND " + //$NON-NLS-1$ //$NON-NLS-2$
					"m." + DBController.FIELD_ANNOTATION_DIC_ID + "= d." + DBController.FIELD_DIC_ITEM_ID; //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		try {
			preparedStatementSelect = dbController.getConn().prepareStatement(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return;
	}
	
	

	

	/* (非 Javadoc)
	 * @see himawari.ArgumentIndex#retrieveFirst(java.lang.String)
	 */
	@Override
	public ResultRecord retrieveFirst(String target) {
		ResultRecord resultRecord = new ResultRecord(fieldInfo);
		
		try {
			// set the target string to the PreparedStatement
			preparedStatementSelect.setString(1, target);
			// query execution
			rs = preparedStatementSelect.executeQuery();

			if(filter == null || filter.size() == 0){
				if(rs.next()){
					resultRecord.setPosition(rs.getInt(DBController.FIELD_ANNOTATION_START));
					resultRecord.set(fieldName, rs.getString(DBController.FIELD_ANNOTATION_SEARCH_KEY));

					Iterator<String> it = requiredColumns.iterator();
					while(it.hasNext()) {
						String columnName = it.next();
						String columnValue = rs.getString(columnName);
						columnName = elementName + COLUMN_NAME_GLUE + columnName;
						resultRecord.set(columnName, columnValue);
					}
				} else {
					return null;
				}
			} else if(rs.next()){
				resultRecord.setPosition(rs.getInt(DBController.FIELD_ANNOTATION_START));
				resultRecord.set(fieldName, rs.getString(DBController.FIELD_ANNOTATION_SEARCH_KEY));

				Iterator<String> it = requiredColumns.iterator();
				while(it.hasNext()) {
					String columnName = it.next();
					String columnValue = rs.getString(columnName);
					columnName = elementName + COLUMN_NAME_GLUE + columnName;
					
					Pattern filterValue = filter.get(columnName);

					if (filterValue != null) {
						if (filter.isNot(columnName)) { // 否定
							if (filterValue.matcher(columnValue).find()) {
								return retrieveNext();
							} else {
								resultRecord.set(columnName, columnValue);
							}
						} else {
							if (filterValue.matcher(columnValue).find()) {
								resultRecord.set(columnName, columnValue);
							} else {
								return retrieveNext();
							}
						}
					} else {
						resultRecord.set(columnName, columnValue);
					}
				}
				
			} else {
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return resultRecord;
	}

	

	/* (非 Javadoc)
	 * @see himawari.ArgumentIndex#retrieveNext()
	 */
	@Override
	public ResultRecord retrieveNext() {
		ResultRecord resultRecord = null;
		try{
			resultRecord = new ResultRecord(fieldInfo);
		} catch (Exception e){
			e.printStackTrace();
		}
		
		try {
			if(filter == null || filter.size() == 0){
				if(rs.next()){
					resultRecord.setPosition(rs.getInt(DBController.FIELD_ANNOTATION_START));
					resultRecord.set(fieldName, rs.getString(DBController.FIELD_ANNOTATION_SEARCH_KEY));

					Iterator<String> it = requiredColumns.iterator();
					while(it.hasNext()) {
						String columnName = it.next();
						String columnValue = rs.getString(columnName);
						columnName = elementName + COLUMN_NAME_GLUE + columnName;
						resultRecord.set(columnName, columnValue);
					}
				} else {
					return null;
				}
			} else 	if(rs.next()){
				resultRecord.setPosition(rs.getInt(DBController.FIELD_ANNOTATION_START));
				resultRecord.set(fieldName, rs.getString(DBController.FIELD_ANNOTATION_SEARCH_KEY));

				Iterator<String> it = requiredColumns.iterator();
				while(it.hasNext()) {
					String columnName = it.next();
					String columnValue = rs.getString(columnName);
					columnName = elementName + COLUMN_NAME_GLUE + columnName;
					
					Pattern filterValue = filter.get(columnName);

					if (filterValue != null) {
						if (filter.isNot(columnName)) { // 否定
							if (filterValue.matcher(columnValue).find()) {
								// 大量の再帰は，stack がなくなるためルーブに変更
								if(!rs.next()) return null;
								resultRecord = new ResultRecord(fieldInfo);
								resultRecord.setPosition(rs.getInt(DBController.FIELD_ANNOTATION_START));
								resultRecord.set(fieldName, rs.getString(DBController.FIELD_ANNOTATION_SEARCH_KEY));
								it = requiredColumns.iterator();
								continue;
							} else {
								resultRecord.set(columnName, columnValue);
							}
						} else {
							if (filterValue.matcher(columnValue).find()) {
								resultRecord.set(columnName, columnValue);
							} else {
								// 大量の再帰は，stack がなくなるためルーブに変更
								if(!rs.next()) return null;
								resultRecord = new ResultRecord(fieldInfo);
								resultRecord.setPosition(rs.getInt(DBController.FIELD_ANNOTATION_START));
								resultRecord.set(fieldName, rs.getString(DBController.FIELD_ANNOTATION_SEARCH_KEY));
								it = requiredColumns.iterator();
								continue;
							}
						}
					} else {
						resultRecord.set(columnName, columnValue);
					}
				}
				
			} else {
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return resultRecord;
	}
}

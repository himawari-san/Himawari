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


public class ContentsDatabaseIndex extends ContentsIndex {

	private final static String COLUMN_NAME_GLUE = "\t"; //$NON-NLS-1$

	private DBController dbController;
	private String targetFieldName;
	private FieldInfo fieldInfo;
	private String fieldName;  // for storing the key string
	private ResultSet rs;
	private PreparedStatement preparedStatementSelect;
	private ArrayList<String> requiredColumns = new ArrayList<String>();
	private String tableName;
	
	public ContentsDatabaseIndex(String tableName, String dbFieldName, CorpusFile corpus) {
		super.elementName = tableName;
		this.targetFieldName = dbFieldName;
		this.corpus = corpus;
	}

	@Override
	boolean exists() {
		return true;
	}

	@Override
	String getFilename() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	void setRetrieveCondition(FieldInfo fieldInfo, String fieldName) {
		this.fieldInfo = fieldInfo;
		this.fieldName = fieldName;
	}

	@Override
	int mkcix(String elementName) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	void open() throws IOException {
		dbController = corpus.getDBController();
		tableName = elementName + "_" + dbController.getCorpusId(corpus.getCorpusName()); //$NON-NLS-1$
		
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
		String sql =
				"SELECT " + StringUtils.join(requiredColumns, ",") + //$NON-NLS-1$ //$NON-NLS-2$
				" from " + tableName + " where " + targetFieldName + " = ?"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		try {
			preparedStatementSelect = dbController.getConn().prepareStatement(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	void close() throws IOException {
		// TODO Auto-generated method stub

	}

	
	@Override
	ResultRecord retrieveFirst(String target) {
		ResultRecord resultRecord = new ResultRecord(fieldInfo);
		
		try {
			// set the target string to the PreparedStatement
			preparedStatementSelect.setString(1, target);
			// query execution
			rs = preparedStatementSelect.executeQuery();

			if(filter == null || filter.size() == 0){
				if(rs.next()){
					resultRecord.setPosition(rs.getInt(1));
					resultRecord.set(fieldName, rs.getString(2));

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
				resultRecord.setPosition(rs.getInt(1));
				resultRecord.set(fieldName, rs.getString(2));

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultRecord;
	}

	
	@Override
	ResultRecord retrieveNext() {
		ResultRecord resultRecord = new ResultRecord(fieldInfo);

		try {
			if(filter == null || filter.size() == 0){
				if(rs.next()){
					resultRecord.setPosition(rs.getInt(1));
					resultRecord.set(fieldName, rs.getString(2));

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
				resultRecord.setPosition(rs.getInt(1));
				resultRecord.set(fieldName, rs.getString(2));

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultRecord;
	}

	
	@Override
	String getIOFilename() {
		// TODO Auto-generated method stub
		return dbController.getDbPath();
	}
}

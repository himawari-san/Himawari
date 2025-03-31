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
 * @(#)ResultRecord.java	0.9.4 2003-10-20
 *
 * Copyright 2003
 * National Institute for Japanese Language All rights reserved.
 *
 */
package jp.ac.ninjal.himawari;

/**
 * レコードを表現する
 * 
 * @author Masaya YAMAGUCHI
 * @version 0.9.4
 */
public class ResultRecord {
	static final String PRIMARY_KEY_FIELD = "_sys\t_key"; //$NON-NLS-1$
	static final String PRECEDING_CONTEXT_FIELD = "_sys\t_preceding_context"; //$NON-NLS-1$
	static final String FOLLOWING_CONTEXT_FIELD = "_sys\t_following_context"; //$NON-NLS-1$

	private FieldInfo fieldInfo = null;
	private Object field[];
	private int resourceID = -1; // このレコードに関連するリソース(コーパス番号など)
	private String resouceName = ""; // このレコードに関連するリソース名 //$NON-NLS-1$
	private int position = -1; // リソース中の位置情報

	/**
	 * ResultRecord を生成する
	 * 
	 * @param fieldInfo
	 *            フィールド情報
	 */
	public ResultRecord(FieldInfo fieldInfo) {
		this.fieldInfo = fieldInfo;
		field = new Object[fieldInfo.size()];
	}

	/**
	 * フィールド<code>fieldName</code>に値<code>value</code>を設定する
	 * 
	 * @param fieldName
	 *            フィールド名
	 * @param value
	 *            フィールド値
	 * @return true .. 設定に成功した場合 false .. フィールドが存在せず，設定に失敗した場合
	 */
	public boolean set(String fieldName, Object value) {
		if (fieldInfo.get(fieldName) != -1) {
			field[fieldInfo.get(fieldName)] = value;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * <code>fieldNum</code>番目のフィールドに値<code>value</code>を設定する
	 * 
	 * @param fieldNum
	 * @param value
	 * @return true .. 設定に成功した場合 false .. フィールドが存在せず，設定に失敗した場合
	 */
	public boolean set(int fieldNum, Object value) {
		if (field.length > fieldNum) {
			field[fieldNum] = value;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * フィールド<code>fieldName</code>の値を取得する
	 * 
	 * @param fieldName
	 *            フィールド名
	 * @return フィールド値 フィールドが存在しない場合は，null
	 */
	public Object get(String fieldName) {
		if (fieldInfo.get(fieldName) != -1) {
			return field[fieldInfo.get(fieldName)];
		} else {
			return null;
		}
	}

	/**
	 * フィールド<code>fieldName</code>の値を取得する
	 * 
	 * @param fieldNumber
	 *            フィールド番号
	 * @return フィールド値 フィールドが存在しない場合は，null
	 */
	public Object get(int fieldNumber) {
		if (field.length > fieldNumber) {
			return field[fieldNumber];
		} else {
			return null;
		}
	}

	public void setResourceID(int id) {
		resourceID = id;
	}

	public int getResourceID() {
		return resourceID;
	}

	public void setPosition(int p) {
		position = p;
	}

	public int getPosition() {
		return position;
	}

	public FieldInfo getFieldInfo() {
		return fieldInfo;
	}

	/**
	 * フィールド数を返す
	 * 
	 * @return フィールド数
	 */
	public int length() {
		return field.length;
	}

	public void setResourceName(String resouceName) {
		this.resouceName = resouceName;
	}

	public String getResourceName() {
		return resouceName;
	}
	
	public ResultRecord copyTo(ResultRecord src){
		for(int i = 0; i < fieldInfo.size(); i++){
			src.set(fieldInfo.getName(i), get(i));
		}
		src.setPosition(getPosition());
		src.setResourceID(getResourceID());
		src.setResourceName(getResourceName());
		
		return src;
	}
}

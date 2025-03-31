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
 * @(#)FieldInfo.java	0.9.4 2003-10-03
 *
 * Copyright 2003
 * National Institute for Japanese Language All rights reserved.
 */
package jp.ac.ninjal.himawari;

import java.util.*;

import javax.swing.table.DefaultTableCellRenderer;

import org.w3c.dom.Node;

/**
 * The FieldInfo class describes the information of fields of result record.
 *
 * @author Masaya YAMAGUCHI
 * @version 0.9.4
 */

public class FieldInfo extends HashMap<String, Object> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2602626032797068798L;
	// ソート時の文字列比較の方向
	static final int SORT_DIRECTION_L_R = 0; // L->R
	static final int SORT_DIRECTION_R_L = 1; // R->L
	static final String SORT_TYPE_STRING = "string"; //$NON-NLS-1$
	static final String SORT_TYPE_NUMERIC = "numeric"; //$NON-NLS-1$
	static final String SORT_TYPE_VECTOR = "vector"; //$NON-NLS-1$
	static final String EDIT_TYPE_TEXT = "text"; //$NON-NLS-1$
	static final String EDIT_TYPE_SELECT = "select"; //$NON-NLS-1$
	static final String TYPE_DB = "db"; //$NON-NLS-1$
	static final String TYPE_INDEX = "index"; //$NON-NLS-1$

	static final int DEFAULT_WIDTH = 100;
	static final int DEFAULT_ALIGN = DefaultTableCellRenderer.LEFT;
	static final int DEFAULT_SORT_DIRECTION = SORT_DIRECTION_L_R;
	static final String DEFAULT_SORT_TYPE = SORT_TYPE_STRING;
	static final int DEFAULT_SORT_ORDER = 0;
	static final boolean DEFAULT_EDITABLE = false;
	static final String DEFAULT_EDIT_TYPE = null;
	static final String DEFAULT_EDIT_OPTION = null;
	
	static final int OPTION_CONTENTS = 1;
	static final int OPTION_LENGTH = 2;
	static final int OPTION_FREQ = 4;
	static final String LABEL_CONTENTS = Messages.getString("FieldInfo.0"); //$NON-NLS-1$
	static final String LABEL_LENGTH = Messages.getString("FieldInfo.1"); //$NON-NLS-1$
	static final String LABEL_FREQ = Messages.getString("FieldInfo.2"); //$NON-NLS-1$
	static final String LABEL_SERIAL_NUMBER = Messages.getString("FieldInfo.3"); //$NON-NLS-1$
	
	static final String LABEL_SEP_ATTRIBUTE = "/@"; //$NON-NLS-1$
	static final String LABEL_SEP_OF = "%"; //$NON-NLS-1$
	
	static String FIELDNAME_FREQ = Messages.getString("Frame1.433"); //$NON-NLS-1$
	static String ATTRIBUTE_CONTENTS = "_contents"; //$NON-NLS-1$
	static String ATTRIBUTE_LENGTH = "_length"; //$NON-NLS-1$
	static String ATTRIBUTE_FREQ = "_freq"; //$NON-NLS-1$
	static String ATTRIBUTE_SERIAL_NUMBER = "_sn"; //$NON-NLS-1$
	static String ELEMENT_SYSTEM = "_sys"; //$NON-NLS-1$

	static final int STATUS_INIT = 0;
	static final int STATUS_SORT_ASCENDING = 1;
	static final int STATUS_SORT_DESCENDING = 2;
	
	
	
	private String fieldNames[]; // field name
	private String typeArray[]; // field type (index, preceding_context, following_context, argument, db)
	private String elementArray[]; // element name
	private String attributeArray[]; // attribute name
	private int widthArray[];
	private int alignArray[];
	private int sortDirectionArray[];
	private int sortOrderArray[];
	private String sortTypeArray[];
	private boolean editableArray[];
//	private boolean dbEditableArray[];
	private String editTypeArray[];
	private String editOptionArray[];
	private int fieldStatus[];

	/**
	 * Constructor
	 *
	 * @param n
	 *            int the number of field
	 */
	public FieldInfo(int n) {
		fieldNames = new String[n];
		typeArray = new String[n];
		elementArray = new String[n];
		attributeArray = new String[n];
		widthArray = new int[n];
		alignArray = new int[n];
		sortDirectionArray = new int[n];
		sortOrderArray = new int[n];
		sortTypeArray = new String[n];
		editableArray = new boolean[n];
//		dbEditableArray = new boolean[n];
		editTypeArray = new String[n];
		editOptionArray = new String[n];
		fieldStatus = new int[n];
	}

	/**
	 * フィールドに各種属性をセットする
	 *
	 * @param fieldName
	 *            String フィールド名
	 * @param no
	 *            int フィールド番号
	 * @param type
	 *            String フィールドタイプ
	 * @param element
	 *            String 要素名
	 * @param attribute
	 *            String 属性名
	 * @param width
	 *            int フィールド幅
	 * @param align
	 *            int 文字揃え
	 * @param sortDirection
	 *            int ソート方向(0: 文字列左から右, 1:文字列右から左)
	 * @param sortOrder
	 *            int ソート順(1 以上, フィールド数以下)
	 * @param sortType
	 *            String ソートタイプ(string, numeric)
	 */
	public void set(String fieldName, int no, String type, String element,
			String attribute, int width, int align, int sortDirection,
			int sortOrder, String sortType, boolean editable, String editType,
			String editOption) {
		fieldNames[no] = fieldName;
		put(fieldName, no); // fieldName 登録
		System.err.println("info: " + element + "\t" + attribute); //$NON-NLS-1$ //$NON-NLS-2$
		put(element + "\t" + attribute, no); // fieldID 登録 //$NON-NLS-1$
		typeArray[no] = type;
		elementArray[no] = element;
		attributeArray[no] = attribute;
		widthArray[no] = width;
		alignArray[no] = align;
		sortDirectionArray[no] = sortDirection;
		sortOrderArray[no] = sortOrder;
		sortTypeArray[no] = sortType;
		editableArray[no] = editable;
		fieldStatus[no] = STATUS_INIT; // initialize

		// db編集可能フラグ
		if (type.equalsIgnoreCase("db") && editable) { //$NON-NLS-1$
			editTypeArray[no] = editType == null ? EDIT_TYPE_TEXT : editType;
			editOptionArray[no] = editOption;
		} else {
			editTypeArray[no] = null;
			editOptionArray[no] = null;
		}
	}

	public void set(String fieldName, int no, String type, String element,
			String attribute, int width, int align, int sortDirection,
			int sortOrder, String sortType, boolean editable) {
		set(fieldName, no, type, element, attribute, width, align,
				sortDirection, sortOrder, sortType, editable, null, null);
	}

	/**
	 * フィールドID を取得
	 *
	 * @param fieldName
	 *            String フィールド名
	 * @return String フィールドID
	 */
	public String getFieldID(String fieldName) {
		int i = get(fieldName);
		return getElementName(i) + "\t" + getAttributeName(i); //$NON-NLS-1$
	}

	/**
	 * フィールド番号を取得する
	 *
	 * @param element
	 *            String 要素名
	 * @param attribute
	 *            String 属性名
	 * @return int フィールド番号
	 */
	public int get(String element, String attribute) {
		String key = element + "\t" + attribute; //$NON-NLS-1$
		if (super.get(key) != null) {
			return ((Integer) super.get(key)).intValue();
		} else {
			return -1;
		}
	}

	/**
	 * フィールドタイプを取得
	 *
	 * @param no
	 *            int フィールド番号
	 * @return String フィールドタイプ
	 */
	public String getType(int no) {
		return typeArray[no];
	}

	/**
	 * 要素名を取得
	 *
	 * @param no
	 *            int フィールド番号
	 * @return String 要素名
	 */
	public String getElementName(int no) {
		return elementArray[no];
	}

	/**
	 * 属性名の取得
	 *
	 * @param no
	 *            int フィールド番号
	 * @return String 属性名
	 */
	public String getAttributeName(int no) {
		return attributeArray[no];
	}

	/**
	 * フィールド幅の取得
	 *
	 * @param no
	 *            int フィールド番号
	 * @return int フィールド幅
	 */
	public int getWidth(int no) {
		return widthArray[no];
	}

	/**
	 * 文字揃えの取得
	 *
	 * @param no
	 *            int フィールド番号
	 * @return int 文字揃え
	 */
	public int getAlign(int no) {
		return alignArray[no];
	}

	/**
	 * ソート方向の取得
	 *
	 * @param no
	 *            int フィールド番号
	 * @return int ソート方向
	 */
	public int getSortDirection(int no) {
		return sortDirectionArray[no];
	}

	/**
	 * ソート順の取得
	 *
	 * @param no
	 *            int フィールド番号
	 * @return int ソート順
	 */
	public int getSortOrder(int no) {
		return sortOrderArray[no];
	}

	/**
	 * ソートタイプの取得
	 *
	 * @param no
	 *            int フィールド番号
	 * @return String ソートタイプ
	 */
	public String getSortType(int no) {
		return sortTypeArray[no];
	}

	/**
	 * フィールド名の取得
	 *
	 * @param no
	 *            int フィールド番号
	 * @return String フィールド名
	 */
	public String getName(int no) {
		return fieldNames[no];
	}

	/**
	 * フィールド名リストの取得
	 *
	 * @return フィールド名のリスト
	 */
	public String[] getNames() {
		return fieldNames;
	}

	
	public String[] getDbEditableNames(){
		HashSet<String> names = new HashSet<String>();
		
		for(int i = 0; i < fieldNames.length; i++){
			if(elementArray[i].equalsIgnoreCase(DBController.TABLE_NOTE)){
				names.add(fieldNames[i]);
			}
		}
		return names.toArray(new String[0]);
	}
	
	
	/**
	 * フィールド番号の取得
	 *
	 * @param fieldName
	 *            フィールド名
	 * @return フィールド値 フィールドが存在しない場合は，-1
	 */
	public int get(String fieldName) {
		if (super.get(fieldName) != null) {
			return ((Integer) super.get(fieldName)).intValue();
		} else {
			return -1;
		}
	}

	/**
	 * フィールド数の取得
	 *
	 * @return int フィールド数
	 */
	public int size() {
		return fieldNames.length;
	}

	public boolean isEditable(int no) {
		return editableArray[no];
	}

	public boolean isDbEditable(int no) {
		if(typeArray[no].equalsIgnoreCase("db") && editableArray[no]){ //$NON-NLS-1$
			return true;
		} else {
			return false;
		}
	}

	public boolean isDbReferred(){
		for(String type : typeArray){
			if(type.equalsIgnoreCase("db")){ //$NON-NLS-1$
				return true;
			}
		}
		return false;
	}


	public boolean isDbEditable(){
		for(int i = 0; i < size(); i++){
			if(isDbEditable(i)){
				return true;
			}
		}
		return false;
	}

	
	public boolean isDicRefferred(){
		for(String type : typeArray){
			if(type.equalsIgnoreCase("dic")){ //$NON-NLS-1$
				return true;
			}
		}
		return false;
	}
	
	
	public int getIndexFreq(){
		int idx = get(ELEMENT_SYSTEM, ATTRIBUTE_FREQ);
		if(idx != -1){
			return idx;
		} else {
			return get(LABEL_FREQ);
		}
	}
	
	
	public String getEditType(int no) {
		return editTypeArray[no];
	}

	public String getEditOption(int no) {
		return editOptionArray[no];
	}

	
	public int getFieldStatus(int no) {
		return fieldStatus[no];
	}

	
	public void resetFieldStatus(){
		for(int i = 0; i < fieldStatus.length; i++){
			fieldStatus[i] = STATUS_INIT;
		}
	}
	
	
	public void setFieldStatus(int no, int status) {
		fieldStatus[no] = status;
	}

	
	/**
	 * プライマリキーを取得する
	 * @return
	 */
	public String getPrimaryKeyName() {
		String ret = ""; //$NON-NLS-1$
		for (int i = 0; i < fieldNames.length; i++) {
			if (typeArray[i].equalsIgnoreCase("key")) { //$NON-NLS-1$
				ret = fieldNames[i];
			}
		}
		return ret;
	}


	static public FieldInfo readFieldDiscription(UserSettings userSetting, String settingName) {
		return readFieldDiscription(userSetting, userSetting.doc.getElementsByTagName(settingName).item(0));
	}
	
	
	static public FieldInfo readFieldDiscription(UserSettings userSetting, Node node) {
		String fieldName[] = userSetting.getAttributeList(node, "name"); //$NON-NLS-1$
		String typeArray[] = userSetting.getAttributeList(node, "type"); //$NON-NLS-1$
		String elementArray[] = userSetting
				.getAttributeList(node, "element"); //$NON-NLS-1$
		String attributeArray[] = userSetting.getAttributeList(node,
				"attribute"); //$NON-NLS-1$
		String widthArray[] = userSetting.getAttributeList(node, "width"); //$NON-NLS-1$
		String alignArray[] = userSetting.getAttributeList(node, "align"); //$NON-NLS-1$
		String sortDirectionArray[] = userSetting.getAttributeList(node,
				"sort_direction"); //$NON-NLS-1$
		String sortOrderArray[] = userSetting.getAttributeList(node,
				"sort_order"); //$NON-NLS-1$
		String sortTypeArray[] = userSetting.getAttributeList(node,
				"sort_type"); //$NON-NLS-1$
		String editableArray[] = userSetting.getAttributeList(node,
				"isEditable"); //$NON-NLS-1$
		String editTypeArray[] = userSetting.getAttributeList(node,
		"edit_type"); //$NON-NLS-1$
		String editOptionArray[] = userSetting.getAttributeList(node,
		"edit_option"); //$NON-NLS-1$

		// レコードのフィールド設定
		FieldInfo fieldInfo = new FieldInfo(fieldName.length);
		
//		boolean isUseDatabase = false;

		for (int i = 0; i < fieldName.length; i++) {
			try {
				int sortDirection = DEFAULT_SORT_DIRECTION;
				int sortOrder = 0;
				int align = 0;
				int width = DEFAULT_WIDTH; // default
				boolean isEditable = false; // default

				if (alignArray[i].equalsIgnoreCase("CENTER")) { //$NON-NLS-1$
					align = DefaultTableCellRenderer.CENTER;
				} else if (alignArray[i].equalsIgnoreCase("RIGHT")) { //$NON-NLS-1$
					align = DefaultTableCellRenderer.RIGHT;
				} else { // デフォルト
					align = DEFAULT_ALIGN;
				}

				if (sortDirectionArray[i].equalsIgnoreCase("R")) { //$NON-NLS-1$
					// ソート時の文字列比較の方向 右(末尾)から左
					sortDirection = SORT_DIRECTION_R_L;
				} else {
					// 左から右(デフォルト)
					sortDirection = SORT_DIRECTION_L_R;
				}

				if (!sortTypeArray[i].equalsIgnoreCase("numeric")) { //$NON-NLS-1$
					// デフォルト
					sortTypeArray[i] = DEFAULT_SORT_TYPE;
				}

				// ソート順
				try {
					sortOrder = Integer.parseInt(sortOrderArray[i]);
				} catch (NumberFormatException ex) {
					// 未指定 or 解析不能文字列の場合
					sortOrder = DEFAULT_SORT_ORDER;
				}

				// フィールド幅
				try {
					width = Integer.parseInt(widthArray[i]);
				} catch (NumberFormatException ex) {
					width = DEFAULT_WIDTH;
				}

				if (editableArray[i].equalsIgnoreCase("true")) { //$NON-NLS-1$
					isEditable = true;
				}


				if(typeArray[i].equalsIgnoreCase("db")){ //$NON-NLS-1$
//					dbController.openConnection();
//					dbController.addColumn(DBController.TABLE_EDIT, fieldName[i]);
				}
				
				fieldInfo.set(fieldName[i], i, typeArray[i], elementArray[i],
						attributeArray[i], width, align, sortDirection,
						sortOrder, sortTypeArray[i], isEditable,
						editTypeArray[i], editOptionArray[i]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return fieldInfo;
	}


	static public FieldInfo readFieldDiscription(HashMap<String, CorpusElementInfo> elementMap, ArrayList<String> order, int options, int contextLength) {
		String targetElementName = order.get(order.size()-1);
		int nField = 0;
		
		if((options & OPTION_FREQ) != 0){
			nField++;
		}
		if((options & OPTION_CONTENTS) != 0){
			nField += contextLength + 1;
		}
		if((options & OPTION_LENGTH) != 0){
			nField += contextLength + 1;
		}
		
		for(String elementName : order){
			CorpusElementInfo corpusElementInfo = elementMap.get(elementName);
			for(String attributeName : corpusElementInfo.keySet()){
				if(corpusElementInfo.isSelected(attributeName)){ // selected attribute?
					nField++;
					if(elementName.equals(targetElementName)){
						nField += contextLength;
					}
				}
			}
		}

		FieldInfo fieldInfo = new FieldInfo(nField);
		
		int i = 0;
		for(String elementName : order){
			CorpusElementInfo corpusElementInfo = elementMap.get(elementName);
			for(String attributeName : corpusElementInfo.keySet()){
				if(!corpusElementInfo.isSelected(attributeName)){
					continue;
				}
				String fieldName = elementName + "/@" + corpusElementInfo.getLabel(attributeName); //$NON-NLS-1$
				fieldInfo.set(fieldName, i++, "", elementName, //$NON-NLS-1$
						attributeName, DEFAULT_WIDTH, DEFAULT_ALIGN, DEFAULT_SORT_DIRECTION,
						DEFAULT_SORT_ORDER, DEFAULT_SORT_TYPE, DEFAULT_EDITABLE,
						DEFAULT_EDIT_TYPE, DEFAULT_EDIT_OPTION);
			}
		}
		if((options & OPTION_CONTENTS) != 0){
			fieldInfo.set(targetElementName + "%" + LABEL_CONTENTS, i++, "", targetElementName, //$NON-NLS-1$ //$NON-NLS-2$
					FieldInfo.ATTRIBUTE_CONTENTS, DEFAULT_WIDTH, DEFAULT_ALIGN, DEFAULT_SORT_DIRECTION,
					DEFAULT_SORT_ORDER, DEFAULT_SORT_TYPE, DEFAULT_EDITABLE,
					DEFAULT_EDIT_TYPE, DEFAULT_EDIT_OPTION);
		}
		if((options & OPTION_LENGTH) != 0){
			fieldInfo.set(targetElementName + "%" + LABEL_LENGTH, i++, "", targetElementName, //$NON-NLS-1$ //$NON-NLS-2$
					FieldInfo.ATTRIBUTE_LENGTH, DEFAULT_WIDTH, DefaultTableCellRenderer.RIGHT, DEFAULT_SORT_DIRECTION,
					DEFAULT_SORT_ORDER, SORT_TYPE_NUMERIC, DEFAULT_EDITABLE,
					DEFAULT_EDIT_TYPE, DEFAULT_EDIT_OPTION);
		}

		for(int j = 0; j < contextLength; j++){
			CorpusElementInfo corpusElementInfo = elementMap.get(targetElementName);
			String indexedElementName = targetElementName + "[" + (j+1) + "]"; //$NON-NLS-1$ //$NON-NLS-2$
			for(String attributeName : corpusElementInfo.keySet()){
				if(!corpusElementInfo.isSelected(attributeName)){
					continue;
				}
				fieldInfo.set(indexedElementName + "/@" + corpusElementInfo.getLabel(attributeName), //$NON-NLS-1$
						i++, "", //$NON-NLS-1$
						indexedElementName,
						attributeName, DEFAULT_WIDTH, DEFAULT_ALIGN, DEFAULT_SORT_DIRECTION,
						DEFAULT_SORT_ORDER, DEFAULT_SORT_TYPE, DEFAULT_EDITABLE,
						DEFAULT_EDIT_TYPE, DEFAULT_EDIT_OPTION);
			}
			if((options & OPTION_CONTENTS) != 0){
				fieldInfo.set(indexedElementName + "%" + LABEL_CONTENTS, i++, "", indexedElementName, //$NON-NLS-1$ //$NON-NLS-2$
						FieldInfo.ATTRIBUTE_CONTENTS, DEFAULT_WIDTH, DEFAULT_ALIGN, DEFAULT_SORT_DIRECTION,
						DEFAULT_SORT_ORDER, DEFAULT_SORT_TYPE, DEFAULT_EDITABLE,
						DEFAULT_EDIT_TYPE, DEFAULT_EDIT_OPTION);
			}
			if((options & OPTION_LENGTH) != 0){
				fieldInfo.set(indexedElementName + "%" + LABEL_LENGTH, i++, "", indexedElementName, //$NON-NLS-1$ //$NON-NLS-2$
						FieldInfo.ATTRIBUTE_LENGTH, DEFAULT_WIDTH, DefaultTableCellRenderer.RIGHT, DEFAULT_SORT_DIRECTION,
						DEFAULT_SORT_ORDER, SORT_TYPE_NUMERIC, DEFAULT_EDITABLE,
						DEFAULT_EDIT_TYPE, DEFAULT_EDIT_OPTION);
			}
		}
		
		if((options & OPTION_FREQ) != 0){
			fieldInfo.set(LABEL_FREQ, i++, "", FieldInfo.ELEMENT_SYSTEM, //$NON-NLS-1$
					FieldInfo.ATTRIBUTE_FREQ, DEFAULT_WIDTH, DefaultTableCellRenderer.RIGHT, DEFAULT_SORT_DIRECTION,
					DEFAULT_SORT_ORDER, SORT_TYPE_NUMERIC, DEFAULT_EDITABLE,
					DEFAULT_EDIT_TYPE, DEFAULT_EDIT_OPTION);
		}

		
		return fieldInfo;
	}
	
	
	static public FieldInfo readFieldDiscription(SIXDic dic, boolean hasSerialNumber) {
		int nField = dic.fieldNames.size();
		String elementName = dic.getName();
		if(hasSerialNumber){
			nField++;
		}
		
		FieldInfo fieldInfo = new FieldInfo(nField);
		
		int i = 0;
		
		if(hasSerialNumber){
			fieldInfo.set(LABEL_SERIAL_NUMBER, i++, "", ELEMENT_SYSTEM, //$NON-NLS-1$
					ATTRIBUTE_SERIAL_NUMBER, DEFAULT_WIDTH, DEFAULT_ALIGN, DEFAULT_SORT_DIRECTION,
					DEFAULT_SORT_ORDER, DEFAULT_SORT_TYPE, DEFAULT_EDITABLE,
					DEFAULT_EDIT_TYPE, DEFAULT_EDIT_OPTION);
		}
		for(String fieldName : dic.fieldNames){
			fieldInfo.set(fieldName, i++, "", elementName, //$NON-NLS-1$
					fieldName, DEFAULT_WIDTH, DEFAULT_ALIGN, DEFAULT_SORT_DIRECTION,
					DEFAULT_SORT_ORDER, DEFAULT_SORT_TYPE, DEFAULT_EDITABLE,
					DEFAULT_EDIT_TYPE, DEFAULT_EDIT_OPTION);
		}
		
		return fieldInfo;
	}
}

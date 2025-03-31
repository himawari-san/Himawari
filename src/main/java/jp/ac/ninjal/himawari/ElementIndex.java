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
 * @(#)ElementIndex.java	ver.1.1, 2005-06-05
 *
 * Copyright 2003-2005
 * National Institute for Japanese Language All rights reserved.
 */

package jp.ac.ninjal.himawari;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.commons.io.FileUtils;

/**
 * Index for XML element. The index is used by the binary search method.
 *
 * @author Masaya YAMAGUCHI
 * @version 1.1
 */
public abstract class ElementIndex {
	protected final int ELEMENT_TYPE_NOT_EMPTY = 0;
	protected final int ELEMENT_TYPE_EMPTY_SEARCH_BACKWARD = 1;
	protected final int ELEMENT_TYPE_EMPTY_SEARCH_FORWARD = 2;

	protected String elementName;
	protected boolean isDB = false;

	public ElementIndex() {
	}

	public ElementIndex(String elementName) {
		this.elementName = elementName;
	}

//	public ElementIndex(String elementName, String middleName, String elementType) {
//		this.elementName = elementName;
//		if (elementType.compareTo("false") == 0) { //$NON-NLS-1$
//			this.elementType = ELEMENT_TYPE_NOT_EMPTY;
//		} else if (elementType.compareTo("true") == 0) { //$NON-NLS-1$
//			this.elementType = ELEMENT_TYPE_EMPTY_SEARCH_BACKWARD;
//		} else if (elementType.compareTo("empty_backward") == 0) { //$NON-NLS-1$
//			this.elementType = ELEMENT_TYPE_EMPTY_SEARCH_BACKWARD;
//		} else if (elementType.compareTo("empty_forward") == 0) { //$NON-NLS-1$
//			this.elementType = ELEMENT_TYPE_EMPTY_SEARCH_FORWARD;
//		}
//	}

	/**
	 * Open the index file
	 */
	abstract public void open();

	/**
	 * Close the index file
	 */
	abstract void close();

	abstract public boolean isOpen();

	abstract public boolean isEmpty();

	abstract public boolean exists();

	public String getElementName()	{
		return elementName;
	}

	/**
	 * Search argument by place
	 *
	 * @param place
	 * @return a tag in <code>place</code>
	 */
	abstract public String searchArg(int place);

	/**
	 * Search a range of an element by place
	 *
	 * @param place
	 * @return a range
	 */
	abstract public Range searchRange(int place);

	abstract public Range searchRange(int place, int n);

	/**
	 * 要素の属性を列挙する
	 *
	 * @param fieldNames
	 *            結果に含める属性名
	 * @return 属性を格納した Vector
	 */
	abstract public ArrayList<ResultRecord> listElement(FieldInfo fieldInfo, Filter filter) throws Exception;
	
	abstract public ArrayList<Integer> listIndex(FieldInfo fieldInfo) throws Exception;


	/**
	 * 要素の属性と要素内容を列挙する
	 *
	 * @param fieldNames
	 *            結果に含める属性名
	 * @return 要素内容と属性を格納した Vector
	 */
	abstract public ArrayList<ResultRecord> listContents(FieldInfo fieldInfo) throws Exception;

	/**
	 * <code>args</code>に含まれる属性を<code>ResultRecord</code>に登録する
	 *
	 * @param resultRecord
	 *            登録対象の <code>ResultRecord</code>
	 * @param args
	 *            属性を含む文字列
	 * @return <code>ResultRecord</code>
	 */
//	abstract public ResultRecord resistAttribute(ResultRecord resultRecord, String args);


	/**
	 * <code>args</code>に含まれる属性を<code>ResultRecord</code>に登録する (フィルター付き)
	 *
	 * @param resultRecord
	 *            登録対象の <code>ResultRecord</code>
	 * @param args
	 *            属性を含む文字列
	 * @return <code>ResultRecord</code>
	 */
//	abstract public ResultRecord resistAttribute(ResultRecord resultRecord, String args, Filter filter);

	abstract public ResultRecord addAttribute(ResultRecord resultRecord, Filter filter);
	
	abstract public ResultRecord resistNextElement(ResultRecord resultRecord,
			int relativeElementIndex, String targetAttributeName, Filter filter);

	abstract public ResultRecord resistSiblingElement(ResultRecord resultRecord,
			String targetElementName, String targetAttributeName, Filter filter);

	abstract public String getIOFilename();
	
	
	public void deleteIndex() throws IOException{
		String indexFilename = getIOFilename();
		if(indexFilename == null || indexFilename.isEmpty()){
			return;
		}
		close();
		FileUtils.forceDelete(new File(getIOFilename()));
		System.err.println("Message(ElementIndex): delete the index file, " + getIOFilename()); //$NON-NLS-1$
	}

}

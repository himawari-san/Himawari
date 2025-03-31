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

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.regex.*;

/**
 * Index for XML element. The index is used by the binary search method.
 * 
 * @author Masaya YAMAGUCHI
 * @version 1.1
 */
public class XmlElementIndex extends ElementIndex {
	public static final String SUFFIX = ".eix"; //$NON-NLS-1$

	private String baseFilename;
	String eixFilename;
	String elementName;
	FileInputStream fisEix;
	IntBuffer ibEix;
	CorpusFile corpus;
	MappedByteBuffer xmlBuf;
	// String isEmpty;
	private Pattern argPattern;
	private FileChannel fc;
	private int elementType;
	private String elementNamePsTab;
	
	/**
	 * Constructor of ElementIndex Class
	 * 
	 * @param elementName
	 *            String target element name
	 * @param middleName
	 *            String middle name for index file
	 * @param corpus
	 *            CorpusFile instance of CorpusFile Class
	 * @param isEmpty
	 *            boolean flag for specifying whether the element is an empty
	 *            element or not (true: if empty element)
	 * 
	 */
	public XmlElementIndex(String elementName, String middleName,
			CorpusFile corpus, String elementType) {
		this.corpus = corpus;
		this.baseFilename = corpus.getBasename();
		this.eixFilename = baseFilename + "." + middleName + SUFFIX; //$NON-NLS-1$
		this.elementName = elementName;
		this.xmlBuf = corpus.getBuf();
		// this.isEmpty = isEmpty;
		if (elementType.compareTo("false") == 0) { //$NON-NLS-1$
			this.elementType = ELEMENT_TYPE_NOT_EMPTY;
		} else if (elementType.compareTo("true") == 0) { //$NON-NLS-1$
//			this.elementType = ELEMENT_TYPE_EMPTY_SEARCH_BACKWARD;
			this.elementType = ELEMENT_TYPE_EMPTY_SEARCH_FORWARD;
		} else if (elementType.compareTo("empty_backward") == 0) { //$NON-NLS-1$
			this.elementType = ELEMENT_TYPE_EMPTY_SEARCH_BACKWARD;
		} else if (elementType.compareTo("empty_forward") == 0) { //$NON-NLS-1$
			this.elementType = ELEMENT_TYPE_EMPTY_SEARCH_FORWARD;
		}
		argPattern = Pattern.compile("([^\\s]+)\\s*=\\s*\"([^\"]*)\""); //$NON-NLS-1$
		elementNamePsTab = elementName + "\t"; //$NON-NLS-1$
	}

	/**
	 * Open the index file
	 */
	public void open() {
		try {
			File eixFile = new File(eixFilename);
			fisEix = new FileInputStream(eixFile);
			fc = fisEix.getChannel();
			ibEix = fc.map(FileChannel.MapMode.READ_ONLY, 0, eixFile.length())
					.asIntBuffer();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Close the index file
	 */
	public void close() {
		try {
			if (fc != null) {
				fc.close();

			}
			if (fisEix != null) {
				fisEix.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		fisEix = null;
		ibEix = null;
		fc = null;
	}

	public boolean isOpen() {
		if (fc != null) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isEmpty() {
		if (elementType == ELEMENT_TYPE_NOT_EMPTY) {
			return false;
		} else {
			return true;
		}
	}

	public boolean exists() {
		File eixFile = new File(eixFilename);
		return eixFile.exists();
	}

	public String getElementName() {
		return elementName;
	}

	public String getFilename() {
		return eixFilename;
	}

	/**
	 * Make an index
	 */
	public void mkeix() {
		if (isEmpty()) {
			// when the element has contents
			mkeix4EmptyElement();
		} else {
			// when no contents
			mkeix4Element();
		}
	}

	/**
	 * Make an index (when the element has contents)
	 * 
	 * <name> ^ p1 <name> ^ p2
	 * 
	 * </name> ^ p3
	 * 
	 * </name> ^ p4
	 * 
	 * order in the index file: p1, p2, p1, p4, p2, p3, p2, p3, p3, p4, p1, p4
	 * This sequence consist of three subsets.
	 * The first set p1, p2, p1, p4 means that characters from p1 to p2
	 * are marked up with a start tag at p1 and an end tag end at p4 
	 */
	public void mkeix4Element() {
		File xmlFile = corpus.getFile();
		File eixFile = new File(eixFilename);
		BufferedReader brXML;
		DataOutputStream dosEIX;
		String line;
		int iXml = 1; // for BOM
		Stack<Integer> startTagStack = new Stack<Integer>(); // stack for stack
																// tag
		Stack<Integer> resultQueue = new Stack<Integer>(); // queue for the
															// result
		Integer tmpObject;
		HashMap<Integer, Integer> rangeMap = new HashMap<Integer, Integer>();

		try {
			brXML = new BufferedReader(new InputStreamReader(
					new FileInputStream(xmlFile), "UnicodeLittle")); //$NON-NLS-1$
			eixFile.createNewFile();
			dosEIX = new DataOutputStream(new FileOutputStream(eixFile));

			String tmpLine = ""; //$NON-NLS-1$
			while ((line = brXML.readLine()) != null) {

				// concatinate the next line if there is a \n in a tag
				if (tmpLine.compareTo("") != 0) { //$NON-NLS-1$
					tmpLine += " " + line; //$NON-NLS-1$
					line = tmpLine;
					System.err.println("Warning(There is \n in tag): " + line); //$NON-NLS-1$
				}
				if (!corpus.isTagFinished(line)) {
					tmpLine = line;
					continue;
				} else {
					tmpLine = ""; //$NON-NLS-1$
				}

				int iLine = 0;
				while (iLine < line.length()) {
					if (line.startsWith("<" + elementName + ">", iLine)) { //$NON-NLS-1$ //$NON-NLS-2$
						// start tag with no argument
						if (startTagStack.size() == 0) {
							tmpObject = null;
						} else {
							tmpObject = startTagStack.peek();
						}
						startTagStack.push(iXml + iLine);
						resultQueue.add(iXml + iLine);
						if (tmpObject != null) {
							resultQueue.add(tmpObject);
							resultQueue.add(iXml + iLine);
						}

						iLine += elementName.length() + 2;
					} else if (line.startsWith("<" + elementName + " ", iLine)) { //$NON-NLS-1$ //$NON-NLS-2$
						int iTmp = iLine + 2 + elementName.length();
						// skip white space
						while (!line.startsWith("/>", iTmp) && !line.startsWith(">", iTmp)) {iTmp++;} //$NON-NLS-1$ //$NON-NLS-2$
						if (line.startsWith(">", iTmp)) { //$NON-NLS-1$
							// start tag with argument
							if (startTagStack.size() == 0) {
								tmpObject = null;
							} else {
								tmpObject = startTagStack.peek();
							}
							startTagStack.push(iXml + iLine);
							resultQueue.add(iXml + iLine); // p1
							if (tmpObject != null) {
								resultQueue.add(tmpObject); // p2
								resultQueue.add(iXml + iLine); // p3
							}
							iTmp++;
						}
						iLine = iTmp;
					} else if (line.startsWith("</" + elementName + ">", iLine)) { //$NON-NLS-1$ //$NON-NLS-2$
						if (startTagStack.size() == 0) {
							System.err
									.println("Error(ElementIndex, mkeix4Element): start tag stack empty"); //$NON-NLS-1$
						} else {
							iLine += elementName.length() + 3;
							resultQueue.add(iXml + iLine);
							resultQueue.add(startTagStack.peek());
							rangeMap.put(startTagStack.pop(), iXml + iLine);
							if (startTagStack.size() == 0) {
								// flush all data from resultQueue
								int queueSize = resultQueue.size();
								for (int i = 0; i < queueSize; i += 3) {
									dosEIX.writeInt(((Integer) resultQueue
											.get(i)).intValue());
									dosEIX.writeInt(((Integer) resultQueue
											.get(i + 1)).intValue());
									tmpObject = resultQueue.get(i + 2);
									dosEIX.writeInt(((Integer) tmpObject)
											.intValue());
									dosEIX.writeInt(((Integer) rangeMap
											.get(tmpObject)).intValue());
								}
								resultQueue.removeAllElements();
							} else {
								resultQueue.add(iXml + iLine);
							}
						}
					} else {
						iLine++;
					}
				}
				iXml += line.length() + 1; // for includeing \n
			}

			dosEIX.flush();
			dosEIX.close();
		} catch (IOException e) {
			System.err.println("Error(mkeix): " + e); //$NON-NLS-1$
		}
	}

	/**
	 * 
	 * Make an index (when empty element)
	 * 
	 * <name /> ^ p1, p3
	 * 
	 * <name /> ^p2, p4
	 * 
	 * order in the index file: p1, p2, p3, p4
	 * 
	 */
	public void mkeix4EmptyElement() {
		File xmlFile = corpus.getFile();
		File eixFile = new File(eixFilename);
		BufferedReader brXML;
		DataOutputStream dosEIX;
		String line;
		int iXml = 1; // for BOM
		int iTagStart = 1; // for BOM
		int iTagTmp;

		try {
			brXML = new BufferedReader(new InputStreamReader(
					new FileInputStream(xmlFile), "UnicodeLittle")); //$NON-NLS-1$
			eixFile.createNewFile();
			dosEIX = new DataOutputStream(new FileOutputStream(eixFile));

			String tmpLine = ""; //$NON-NLS-1$
			while ((line = brXML.readLine()) != null) {

				// タグ内部で改行している場合は，次の行を連結
				if (tmpLine.compareTo("") != 0) { //$NON-NLS-1$
					tmpLine += " " + line; //$NON-NLS-1$
					line = tmpLine;
				}
				if (!corpus.isTagFinished(line)) {
					tmpLine = line;
					continue;
				} else {
					tmpLine = ""; //$NON-NLS-1$
				}

				int iLine = 0;
				while (iLine < line.length()) {
					if (line.startsWith("<" + elementName + "/>", iLine)) { //$NON-NLS-1$ //$NON-NLS-2$
						// 空要素タグ（属性なし）
						dosEIX.writeInt(iTagStart); // p1
						iTagTmp = iTagStart;
						iLine += elementName.length() + 3;
						iTagStart = iXml + iLine; // 空要素タグの終わりを次要素の開始位置に設定
						dosEIX.writeInt(iTagStart); // p2
						dosEIX.writeInt(iTagTmp); // p3
						dosEIX.writeInt(iTagStart); // p4
					} else if (line.startsWith("<" + elementName + " ", iLine)) { //$NON-NLS-1$ //$NON-NLS-2$
						int iTmp = iLine + 2 + elementName.length();
						while (!line.startsWith("/>", iTmp) && !line.startsWith(">", iTmp)) {iTmp++;} // 空白をスキップ //$NON-NLS-1$ //$NON-NLS-2$
						if (line.startsWith("/>", iTmp)) { //$NON-NLS-1$
							// 空要素タグ
							dosEIX.writeInt(iTagStart); // p1
							iTagTmp = iTagStart;
							iTmp += 2;
							iTagStart = iXml + iTmp; // 空要素タグの終わりを次要素の開始位置に設定
							dosEIX.writeInt(iTagStart); // p2
							dosEIX.writeInt(iTagTmp); // p3
							dosEIX.writeInt(iTagStart); // p4
						}
						iLine = iTmp;
					} else {
						iLine++;
					}
				}
				iXml += line.length() + 1; // \n を含めて
			}
			if(elementType == ELEMENT_TYPE_EMPTY_SEARCH_BACKWARD){
				dosEIX.writeInt(iTagStart); // p1
				dosEIX.writeInt(iXml); // p2
				dosEIX.writeInt(iTagStart); // p3
				dosEIX.writeInt(iXml); // p4
				
			}
			dosEIX.flush();
			dosEIX.close();
		} catch (IOException e) {
			System.err.println("Error(mkeix): " + e); //$NON-NLS-1$
		}
	}

	/**
	 * Search argument by place
	 * 
	 * @param place
	 * @return a tag in <code>place</code>
	 */
	public String searchArg(int place) {
		int start = 0;
		int end = 0;
		int curEixPos;
		int elementStartPos;
		int elementEndPos;

		end = ibEix.limit();

		while (start <= end) {
			curEixPos = (start + end) / 2 - (((start + end) / 2) % 4);
			try {
				// read a position of the text from the array
				ibEix.position(curEixPos);
				elementStartPos = ibEix.get();
				elementEndPos = ibEix.get();

				if (elementStartPos <= place && elementEndPos > place) {
					if (!isEmpty()) {
						elementStartPos = ibEix.get();
						return corpus.getTag(elementStartPos);
					} else if (elementType == ELEMENT_TYPE_EMPTY_SEARCH_FORWARD) {
						ibEix.get(); // skip
						elementEndPos = ibEix.get();
						return corpus.getEmptyTag(elementEndPos);
					} else { // when ELEMENT_TYPE_EMPTY_SEARCH_BACKWARD
						elementEndPos = ibEix.get();
						return corpus.getEmptyTag(elementEndPos);
					}
				} else if (place < elementStartPos) {
					end = curEixPos - 4;
				} else {
					start = curEixPos + 4;
				}
			} catch (Exception e) {
				if (start != end) {
					// not found if start=end
					System.err
							.println("Error(ElementIndex, searchArg): " + elementName + //$NON-NLS-1$
									", " + place //$NON-NLS-1$
									+ ", " + curEixPos + ", " + start + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
									", " + end + ", " + e); //$NON-NLS-1$ //$NON-NLS-2$
				}
				return ""; //$NON-NLS-1$
			}
		}
		return "nothing"; // for the tail of text //$NON-NLS-1$
	}

	/**
	 * Search a range of an element by place
	 * 
	 * @param place
	 * @return a range
	 */
	public Range searchRange(int place) {
		int start = 0;
		int end = 0;
		int curEixPos;
		int elementStartPos;
		int elementEndPos;

		end = ibEix.limit();
		while (start <= end) {
			curEixPos = (start + end) / 2 - ((start + end) / 2) % 4;

			// read a position of the text from the array
			ibEix.position(curEixPos);
			elementStartPos = ibEix.get();
			elementEndPos = ibEix.get();

			if (elementStartPos <= place && elementEndPos > place) {
				elementStartPos = ibEix.get();
				elementEndPos = ibEix.get();
				return new Range(elementStartPos, elementEndPos);
			} else if (place < elementStartPos) {
				end = curEixPos - 4;
			} else {
				start = curEixPos + 4;
			}
		}
		return null;
	}

	public Range searchRange(int place, int n) {
		int start = 0;
		int end = 0;
		int curEixPos;
		int elementStartPos;
		int elementEndPos;

		end = ibEix.limit();

		while (start <= end) {
			curEixPos = (start + end) / 2 - ((start + end) / 2) % 4;

			// read a position of the text from the array
			ibEix.position(curEixPos);
			elementStartPos = ibEix.get();
			elementEndPos = ibEix.get();

			if (elementStartPos <= place && elementEndPos > place) {
				try {
					if (n != 0) {
						ibEix.position(curEixPos + n * 4 + 2);
						elementStartPos = ibEix.get();
						elementEndPos = ibEix.get();
					}
				} catch (Exception e) {
					break;
				}
				return new Range(elementStartPos, elementEndPos);
			} else if (place < elementStartPos) {
				end = curEixPos - 4;
			} else {
				start = curEixPos + 4;
			}
		}
		return null;
	}

	/**
	 * 要素の属性を列挙する
	 * 
	 * @param fieldNames
	 *            結果に含める属性名
	 * @return 属性を格納した Vector
	 */
	public ArrayList<ResultRecord> listElement(FieldInfo fieldInfo, Filter filter)
			throws Exception {
		int end = 0;
		int curEixPos;
		int elementPos; // start or end position (depend on isEmpty)
		ArrayList<ResultRecord> resultRecords = new ArrayList<ResultRecord>();
		boolean includeContents = false;
		boolean includeLength = false;
		String contentsFieldName = null;
		String lengthFieldName = null;
		HashSet<Long> indexHash = new HashSet<Long>(); 
		Pattern tagPattern = Pattern.compile("<.+?>|\\s"); //$NON-NLS-1$
		
		for(int i = 0; i < fieldInfo.size(); i++){
			if(fieldInfo.getElementName(i).equals(elementName)){
				if(fieldInfo.getAttributeName(i).equals(FieldInfo.ATTRIBUTE_CONTENTS)){
					contentsFieldName = fieldInfo.getName(i);
					includeContents = true;
				} else if(fieldInfo.getAttributeName(i).equals(FieldInfo.ATTRIBUTE_LENGTH)){
					lengthFieldName = fieldInfo.getName(i);
					includeLength = true;
				}
			}
		}

		end = ibEix.limit();

		for (curEixPos = 0; curEixPos < end; curEixPos += 4) {

			try {
				/* ary からテキスト位置の読込み */
				ibEix.position(curEixPos + 2);
				String tagStr;
				long indexHashKey;
				if (!isEmpty()) {
					elementPos = ibEix.get();
					tagStr = corpus.getTag(elementPos);
					indexHashKey = ((long)elementPos << 32) + ibEix.get();
				} else if (elementType == ELEMENT_TYPE_EMPTY_SEARCH_FORWARD) {
					indexHashKey = (long)ibEix.get() << 32; // skip
					elementPos = ibEix.get();
					indexHashKey +=  elementPos;
					tagStr = corpus.getEmptyTag(elementPos);
				} else { // when ELEMENT_TYPE_EMPTY_SEARCH_BACKWARD
					elementPos = ibEix.get();
					tagStr = corpus.getEmptyTag(elementPos);
					indexHashKey = ((long)elementPos << 32) + ibEix.get();
				}
				
				// hash for avoiding duplication in the list
				// do not put on the list when an index pair (startpos, endpos) is already on the list
				// 32 = int (4bytes) x 8bits
				if(indexHash.contains(indexHashKey)){
					continue;
				} else {
					indexHash.add(indexHashKey);
				}
				
				ResultRecord resultRecord = new ResultRecord(fieldInfo);
				if(resistAttribute(resultRecord, tagStr, filter) == null){
					continue;
				}
				if(includeContents || includeLength){
					String contents = corpus.getContent(elementName, elementPos);
					if(includeContents){
						resultRecord.set(contentsFieldName, contents);
					}
					if(includeLength){
						if(isEmpty()){
							resultRecord.set(lengthFieldName, 0);
						} else {
							resultRecord.set(lengthFieldName, tagPattern.matcher(contents).replaceAll("").length()); //$NON-NLS-1$
						}
					}
				}
				resultRecord.setPosition(elementPos);
				resultRecords.add(resultRecord);
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Error(ElementIndex, search[2]): " + e); //$NON-NLS-1$
				throw e;
			}
		}
		return resultRecords;
	}

	
	public ArrayList<Integer> listIndex(FieldInfo fieldInfo) throws Exception {
		int end = ibEix.limit();
		int curEixPos;
		int elementPos; // start or end position (depend on isEmpty)
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		
		for (curEixPos = 0; curEixPos < end; curEixPos += 4) {

			try {
				/* ary からテキスト位置の読込み */
				ibEix.position(curEixPos + 2);
				if (!isEmpty()) {
					elementPos = ibEix.get();
				} else if (elementType == ELEMENT_TYPE_EMPTY_SEARCH_FORWARD) {
					ibEix.get(); // skip
					elementPos = ibEix.get();
					if(elementPos != 0){ // move the position into the empty tag  <a /^here>
						elementPos--;
					}
				} else { // when ELEMENT_TYPE_EMPTY_SEARCH_BACKWARD
					elementPos = ibEix.get();
					if(elementPos == 1){ // skip the first index
						continue;
					}
				}
				indexes.add(elementPos);
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Error(ElementIndex, search[2]): " + e); //$NON-NLS-1$
				throw e;
			}
		}
		return indexes;
	}
	
	
	/**
	 * 要素の属性と要素内容を列挙する
	 * 
	 * @param fieldNames
	 *            結果に含める属性名
	 * @return 要素内容と属性を格納した Vector
	 */
	public ArrayList<ResultRecord> listContents(FieldInfo fieldInfo)
			throws Exception {
		int end = 0;
		int curEixPos;
		int elementStartPos;
		ArrayList<ResultRecord> resultRecords = new ArrayList<ResultRecord>();

		end = ibEix.limit();

		for (curEixPos = 0; curEixPos < end; curEixPos += 4) {

			try {
				/* ary からテキスト位置の読込み */
				ibEix.position(curEixPos + 2);
				elementStartPos = ibEix.get();
				ResultRecord resultRecord = new ResultRecord(fieldInfo);
				resistAttribute(resultRecord, corpus.getTag(elementStartPos));
				resultRecord
						.set("_contents_", corpus.getContent(elementName, elementStartPos)); //$NON-NLS-1$
				resultRecord.setPosition(elementStartPos);
				resultRecords.add(resultRecord);
			} catch (Exception e) {
				System.err.println("Error(ElementIndex, search[2]): " + e); //$NON-NLS-1$
				throw e;
			}
		}
		return resultRecords;
	}

	/**
	 * <code>args</code>に含まれる属性を<code>ResultRecord</code>に登録する
	 * 
	 * @param resultRecord
	 *            登録対象の <code>ResultRecord</code>
	 * @param args
	 *            属性を含む文字列
	 * @return <code>ResultRecord</code>
	 */
	public ResultRecord resistAttribute(ResultRecord resultRecord, String args) {
		Matcher argMatcher = argPattern.matcher(args);

		while (argMatcher.find()) {
			resultRecord
					.set(elementNamePsTab + argMatcher.group(1), argMatcher.group(2));
		}
		return resultRecord;
	}

	/**
	 * <code>args</code>に含まれる属性を<code>ResultRecord</code>に登録する (フィルター付き)
	 * 
	 * @param resultRecord
	 *            登録対象の <code>ResultRecord</code>
	 * @param args
	 *            属性を含む文字列
	 * @return <code>ResultRecord</code>
	 */
	public ResultRecord resistAttribute(ResultRecord resultRecord, String args,
			Filter filter) {
		Matcher argMatcher = argPattern.matcher(args);
		String argName;
		String argValue;
		Object hashValue;
		// boolean isNot;

		while (argMatcher.find()) {
			argName = elementNamePsTab + argMatcher.group(1);
			argValue = argMatcher.group(2);
			hashValue = filter.get(argName);
			if (hashValue != null) {
				if (filter.isNot(argName)) { // 否定
					if (((Pattern) hashValue).matcher(argValue).find()) {
						return null;
					} else {
						resultRecord.set(argName, argValue);
					}
				} else {
					if (((Pattern) hashValue).matcher(argValue).find()) {
						resultRecord.set(argName, argValue);
					} else {
						return null;
					}
				}
			} else {
				resultRecord.set(argName, argValue);
			}
		}

		// 属性値がなく，フィルタが指定されている場合の処理
		for (String filterEntry : filter.keySet()) {
			if(!filterEntry.startsWith(elementNamePsTab)){
				continue;
			}
			if (filter.get(filterEntry) != null) {
				if ((resultRecord.get(filterEntry) == null ||
						((String) resultRecord.get(filterEntry)).compareTo("") == 0) //$NON-NLS-1$
						&& !filter.isNot(filterEntry)) {
					// フィルタ設定あり，値なし，否定条件ではない
					return null;
				}
				// 否定条件の場合は，そのまま
				// 値ありの場合は，上でチェックしている
			}
		}
		return resultRecord;
	}

	public ResultRecord addAttribute(ResultRecord resultRecord, Filter filter) {
		int txtPos;
		Matcher argMatcher;

		String argName;
		String argValue;
		Object hashValue;

		if (filter == null || filter.size() == 0) { // no fileter
			txtPos = resultRecord.getPosition();
			argMatcher = argPattern.matcher(searchArg(txtPos));
			while (argMatcher.find()) {
				resultRecord
						.set(elementNamePsTab + argMatcher.group(1), argMatcher.group(2));
			}
		} else { // using the filter
			txtPos = resultRecord.getPosition();

			argMatcher = argPattern.matcher(searchArg(txtPos));
			while (argMatcher.find()) {
				argName = elementNamePsTab + argMatcher.group(1);
				argValue = argMatcher.group(2);
				hashValue = filter.get(argName);

				if (hashValue != null) {
					if (filter.isNot(argName)) { // 否定
						if (((Pattern) hashValue).matcher(argValue).find()) {
							return null;
						} else {
							resultRecord.set(argName, argValue);
						}
					} else {
						if (((Pattern) hashValue).matcher(argValue).find()) {
							resultRecord.set(argName, argValue);
						} else {
							return null;
						}
					}
				} else {
					resultRecord.set(argName, argValue);
				}
			}
			// 属性値がなく，フィルタが指定されている場合の処理
			for (String filterEntry : filter.keySet()) {
				if(!filterEntry.startsWith(elementNamePsTab)){
					continue;
				}
				hashValue = filter.get(filterEntry);
				if (hashValue != null) {
					if ((resultRecord.get(filterEntry) == null) //$NON-NLS-1$
							&& !filter.isNot(filterEntry)
							&& !((Pattern) hashValue).matcher("").find()) { //$NON-NLS-1$
//					if ((resultRecord.get(filterEntry) == null ||
//							((String) resultRecord.get(filterEntry)).compareTo("") == 0) //$NON-NLS-1$
//							&& !filter.isNot(filterEntry)) {
						// フィルタ設定あり，値なし，否定条件ではない
						return null;
					}
					// 否定条件の場合は，そのまま
					// 値ありの場合は，上でチェックしている
				}
			}
		}

		return resultRecord;
	}

	
	public ResultRecord resistNextElement(ResultRecord resultRecord,
			int relativeElementIndex, String targetAttributeName, Filter filter) {

		String fieldID = "[" + relativeElementIndex + "]\t"; //$NON-NLS-1$ //$NON-NLS-2$
		if (targetAttributeName.compareTo("") == 0) { //$NON-NLS-1$
			boolean flagIsNot = filter.isNot(fieldID);
			// 属性値の指定なし
			Pattern filterValue = (Pattern) filter.get(fieldID);

			int position = resultRecord.getPosition();
			Range range = searchRange(position, relativeElementIndex);
			if (range == null) {
				if (filterValue == null) {
					return resultRecord;
				} else if (filterValue.matcher("").find()) {
					if (!flagIsNot) {
						return resultRecord;
					} else { // filtered
						return null;
					}
				} else {
					if (flagIsNot) {
						return resultRecord;
					} else { // filtered
						return null;
					}
				}
			}
			String targetElement = corpus.getSource(range);
			targetElement = targetElement.replaceAll("<[^>]+>", ""); // remove tag //$NON-NLS-1$ //$NON-NLS-2$
			// filtering
			if (filterValue == null) {
				resultRecord.set(fieldID, targetElement);
			} else if (filterValue.matcher(targetElement).find()) {
				if (!flagIsNot) {
					resultRecord.set(fieldID, targetElement);
				} else { // filtered
					return null;
				}
			} else {
				if (flagIsNot) {
					resultRecord.set(fieldID, targetElement);
				} else { // filtered
					return null;
				}
			}
		} else if (true) {
			String trueFieldID = fieldID + targetAttributeName;
			boolean flagIsNot = filter.isNot(trueFieldID);
			Pattern filterValue = (Pattern) filter.get(trueFieldID);
			Pattern attributePattern = Pattern.compile(" " + targetAttributeName
					+ "\\s*=\\s*\"([^\"]*)\""); //$NON-NLS-1$
			// 属性値の指定あり
			int position = resultRecord.getPosition();
			Range range = searchRange(position, relativeElementIndex);
			if (range == null) {
				if (filterValue == null) {
					return resultRecord;
				} else if (filterValue.matcher("").find()) {
					if (!flagIsNot) {
						return resultRecord;
					} else { // filtered
						return null;
					}
				} else {
					if (flagIsNot) {
						return resultRecord;
					} else { // filtered
						return null;
					}
				}
			}
			String targetElement = corpus.getSource(range);
			String targetTag = targetElement.substring(0,
					targetElement.indexOf('>'));
			Matcher attributeMatcher = attributePattern.matcher(targetTag);
			if (attributeMatcher.find()) {
				String attributeValue = attributeMatcher.group(1);
				// filtering
				if (filterValue == null) {
					resultRecord.set(trueFieldID, attributeValue);
				} else if (filterValue.matcher(attributeValue).find()) {
					if (!flagIsNot) {
						resultRecord.set(trueFieldID, attributeValue);
					} else { // filtered
						return null;
					}
				} else {
					if (flagIsNot) {
						resultRecord.set(trueFieldID, attributeValue);
					} else { // filtered
						return null;

					}
				}
			} else if (filterValue != null && !flagIsNot) {
				return null;
			}
		}
		return resultRecord;
	}

	public ResultRecord resistSiblingElement(ResultRecord resultRecord,
			String targetElementName, String targetAttributeName, Filter filter) {

		String fieldID = targetElementName + "\t" + targetAttributeName; //$NON-NLS-1$
		boolean flagIsNot = filter.isNot(fieldID);
		Pattern filterValue = (Pattern) filter.get(fieldID);

		if (targetAttributeName.compareTo("") == 0) { // 兄弟要素を検索する場合 //$NON-NLS-1$
			int position = resultRecord.getPosition();
			Range primaryElementRange = searchRange(position);
			int primaryElementStartIndex = primaryElementRange.getStart();
			int primaryElementEndIndex = primaryElementRange.getEnd();
			String nextElement = corpus.getNextElement(primaryElementEndIndex,
					targetElementName);
			if (nextElement != null) { // 後方を検索
				// タグ&先頭の空白文字削除
				nextElement = nextElement.replaceAll("<[^>]+>*", ""); //$NON-NLS-1$ //$NON-NLS-2$
				nextElement = nextElement.replaceAll("^\\s+", ""); //$NON-NLS-1$ //$NON-NLS-2$
				if (filterValue == null) { // フィルタの指定なし
					resultRecord.set(fieldID, nextElement);
				} else if (filterValue.matcher(nextElement).find()) { // フィルタの指定にマッチ
					if (!flagIsNot) { // 否定ではない
						resultRecord.set(fieldID, nextElement);
					} else {
						return null;
					}
				} else { // フィルタの指定にマッチしない
					if (flagIsNot) { // 否定
						resultRecord.set(fieldID, nextElement);
					} else {
						return null;
					}
				}
			} else {
				// 後方になかったら，前方を検索
				String prevElement = corpus.getPreviousElement(
						primaryElementStartIndex, targetElementName);
				if (prevElement != null) {
					// タグ&先頭の空白文字削除
					prevElement = prevElement.replaceAll("<[^>]+>", ""); //$NON-NLS-1$ //$NON-NLS-2$
					prevElement = prevElement.replaceAll("^\\s+", ""); //$NON-NLS-1$ //$NON-NLS-2$

					if (filterValue == null) { // フィルタの指定なし
						resultRecord.set(fieldID, prevElement);
					} else if (filterValue.matcher(prevElement).find()) { // フィルタの指定にマッチ
						if (!flagIsNot) { // 否定ではない
							resultRecord.set(fieldID, prevElement);
						} else {
							return null;
						}
					} else { // フィルタの指定にマッチしない
						if (flagIsNot) { // 否定
							resultRecord.set(fieldID, prevElement);
						} else {
							return null;
						}
					}
				} else if (filterValue != null && !flagIsNot) { // 要素が存在しない
					return null;
				}
			}
		} else { // 属性を検索する場合
			Pattern attributePattern = Pattern.compile(targetAttributeName
					+ "\\s*=\\s*\"([^\"]*)\""); //$NON-NLS-1$
			int position = resultRecord.getPosition();
			Range primaryElementRange = searchRange(position);
			int primaryElementStartIndex = primaryElementRange.getStart();
			int primaryElementEndIndex = primaryElementRange.getEnd();
			String nextElement = corpus.getNextElement(primaryElementEndIndex,
					targetElementName);
			if (nextElement != null) { // 後方を検索
				String targetTag = nextElement.substring(0,
						nextElement.indexOf('>'));
				Matcher attributeMatcher = attributePattern.matcher(targetTag);
				if (attributeMatcher.find()) {
					if (filterValue == null) { // フィルタの指定なし
						resultRecord.set(fieldID, attributeMatcher.group(1));
					} else if (filterValue.matcher(attributeMatcher.group(1))
							.find()) { // フィルタの指定にマッチ
						if (!flagIsNot) { // 否定ではない
							resultRecord
									.set(fieldID, attributeMatcher.group(1));
						} else {
							return null;
						}
					} else { // フィルタの指定にマッチしない
						if (flagIsNot) { // 否定
							resultRecord
									.set(fieldID, attributeMatcher.group(1));
						} else {
							return null;
						}
					}
				} else if (filterValue != null && !flagIsNot) { // 属性が存在しない場合
					return null;
				}
			} else {
				// 後方になかったら，前方を検索
				String prevElement = corpus.getPreviousElement(
						primaryElementStartIndex, targetElementName);
				if (prevElement != null) {
					String targetTag = prevElement.substring(0,
							prevElement.indexOf('>'));
					Matcher attributeMatcher = attributePattern
							.matcher(targetTag);
					if (attributeMatcher.find()) {
						if (filterValue == null) { // フィルタの指定なし
							resultRecord
									.set(fieldID, attributeMatcher.group(1));
						} else if (filterValue.matcher(
								attributeMatcher.group(1)).find()) { // フィルタの指定にマッチ
							if (!flagIsNot) { // 否定ではない
								resultRecord.set(fieldID,
										attributeMatcher.group(1));
							} else {
								return null;
							}
						} else { // フィルタの指定にマッチしない
							if (flagIsNot) { // 否定
								resultRecord.set(fieldID,
										attributeMatcher.group(1));
							} else {
								return null;
							}
						}
					} else if (filterValue != null && !flagIsNot) { // 属性が存在しない場合
						return null;
					}
				} else if (filterValue != null && !flagIsNot) { // 要素が存在しない場合
					return null;
				}
			}
		}
		return resultRecord;
	}

	public String getIOFilename() {
		return eixFilename;
	}

}

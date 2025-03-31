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
 * @(#)CorpusFile.java	2003-10-03
 *
 * Copyright 2003-2005
 * National Institute for Japanese Language All rights reserved.
 */

package jp.ac.ninjal.himawari;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 *
 * The CorpusFile class expresses a corpus itself and the indexes and provides
 * some methods to manage the files.
 *
 * @author Masaya YAMAGUCHI
 * @version 1.0
 */
public class CorpusFile {
	public CorpusFile() {
	}

	public final static char KEY_HEAD_MARKER = '\ufffd'; //$NON-NLS-1$
	private final static String SUFFIX_XML = ".xml"; //$NON-NLS-1$
	private final static String SUFFIX_DOS = ".dos"; //$NON-NLS-1$
	private final static String DEFAULT_DOS_ENCODING = "utf-8"; //$NON-NLS-1$
	private final static String DOS_FIELD_SEPARATOR = "\t"; //$NON-NLS-1$

	private String basename;
	private String corpusname;
	private File xmlFile;
	protected MappedByteBuffer xmlBuf;
	private FileInputStream fisXml;
	private byte b2[] = new byte[2];
	private ArrayList<ContentsIndex> cixVector = new ArrayList<ContentsIndex>();
	private ArrayList<ElementIndex> eixVector = new ArrayList<ElementIndex>();
	private ArrayList<ArgumentIndex> aixVector = new ArrayList<ArgumentIndex>();
	private HashMap<String, Object> dixMap = new HashMap<String, Object>();
	protected StringBuilder strBuf = new StringBuilder(512);
	private FileChannel fc;
	private boolean isSelected = true;
	private ContentsReader cr = new ContentsReaderDefault(this); // default
	private DBController dbController = null;
	private HashMap<String, CorpusElementInfo> elementMap = new HashMap<String, CorpusElementInfo>();
	
	/**
	 * Constructor
	 *
	 * @param basename
	 *            String the body of filename (ex. "corpus" in the case of
	 *            "corpus.xml")
	 * @param corpusname
	 *            String the name of this corpus
	 */
	public CorpusFile(String basename, String corpusname) {
		this.basename = basename;
		this.corpusname = corpusname;
	}

	/**
	 * Initialization
	 *
	 * @throws IOException
	 */
	public void init() throws IOException {
		xmlFile = new File(basename + SUFFIX_XML);
		System.err.println("corpusname " + corpusname); //$NON-NLS-1$
		fisXml = new FileInputStream(xmlFile);
		fc = fisXml.getChannel();
		xmlBuf = fc.map(FileChannel.MapMode.READ_ONLY, 0, xmlFile.length());
		isSelected = true;
	}

	public void close() throws IOException {
		closeIndex();
		if (fc != null)
			fc.close();
		if (fisXml != null)
			fisXml.close();
		fisXml = null;
		xmlBuf = null;
		fc = null;
//		System.gc();
//		System.runFinalization();
		System.err
				.println("info(available memory): " + Runtime.getRuntime().freeMemory()); //$NON-NLS-1$
	}

	public void openEix() throws IOException {
		for (int i = 0; i < eixVector.size(); i++) {
			eixVector.get(i).open();
		}
	}

	public void openEix(String[] elementNames) throws IOException {
		for(String elementName : elementNames){
			System.err.println("eix:" + elementName); //$NON-NLS-1$
			getEix(elementName).open();
		}
	}
	
	public void closeIndex() throws IOException {
		// cix
		for (int i = 0; i < cixVector.size(); i++) {
			if (cixVector.get(i) != null) {
				cixVector.get(i).close();
			}
		}
		// eix
		for (int i = 0; i < eixVector.size(); i++) {
			if (eixVector.get(i) != null) {
				eixVector.get(i).close();
			}
		}
		// aix
		for (int i = 0; i < aixVector.size(); i++) {
			if (aixVector.get(i).isOpen()) {
				aixVector.get(i).close();
			}
		}
	}

	public boolean exists() {
		File f = new File(basename + SUFFIX_XML);
		return f.exists();
	}

	public void setCix(String elementName, String middleName) {
		cixVector.add(new ContentsSuffixArray(elementName, middleName, this));
	}

	public void setCix(String elementName, String middleName, String stopElement) {
		cixVector.add(new ContentsSuffixArray(elementName, middleName, this,
				stopElement));
	}

	public void setCixRecordBased(String elementName, String middleName) {
		cixVector.add(new RecordBasedContentsIndex(elementName, middleName,
				this));
	}

	public void setCixRecordBased(String elementName, String middleName,
			String stopElement) {
		cixVector.add(new RecordBasedContentsIndex(elementName, middleName,
				this, stopElement));
	}

	public void setCixNull(String elementName) {
		cixVector.add(new ContentsNullIndex(elementName, this));
	}

	public void setCixDB(String elementName, String middleName) {
		cixVector.add(new ContentsDatabaseIndex(elementName, middleName, this));
	}
	
	public void setContentsReader(ContentsReader cr) {
		this.cr = cr;
	}

	public void setEix(String elementName, String middleName, String isEmpty) {
		eixVector.add(new XmlElementIndex(elementName, middleName, this, isEmpty));
	}

	public void setEixDB(String elementName) {
		eixVector.add(new DatabaseElementIndex(elementName, this));
	}

	
	public void setEixDIC(String elementName, SIXDic dic) {
		eixVector.add(new StandoffElementIndex(elementName, this, dic));
	}

	
	public void setAix(String elementName, String middleName,
			String argumentName, boolean isCompleteMatch) {
		aixVector.add(new XmlArgumentIndex(elementName, middleName, argumentName,
				isCompleteMatch, this));
	}

	public void setAixRecordBased(String elementName, String middleName,
			String argumentName, boolean isCompleteMatch) {
		aixVector.add(new RecordBasedArgumentIndex(elementName, middleName,
				argumentName, isCompleteMatch, this));
	}

	
	public void setAixDB(String elementName, String argumentName, boolean isCompleteMatch) {
		aixVector.add(new ArgumentDatabaseIndex(elementName, argumentName, isCompleteMatch, this));
	}


	public void setAixDIC(String elementName, String argumentName, boolean isCompleteMatch, SIXDic dic) {
		aixVector.add(new StandoffArgumentIndex(elementName, argumentName, isCompleteMatch, this, dic));
	}
	
	public ArrayList<ContentsIndex> getCixVector() {
		return cixVector;
	}

	public ArrayList<ElementIndex> getEixVector() {
		return eixVector;
	}

	public ArrayList<ArgumentIndex> getAixVector() {
		return aixVector;
	}


	/**
	 * コーパス関連ファイルの basename を返す
	 *
	 * @return basename
	 */
	public String getBasename() {
		return basename;
	}

	public String getCorpusName() {
		return corpusname;
	}

	public String toString() {
		return corpusname;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void select(boolean flag) {
		isSelected = flag;
	}

	/**
	 * コーパス本体の File を返す
	 *
	 * @return コーパス本体の File
	 */
	public File getFile() {
		return xmlFile;
	}

	/**
	 *
	 * @return
	 */
	public MappedByteBuffer getBuf() {
		return xmlBuf;
	}

	/**
	 * byte 型の配列を char に変換する UTF-16(Little Endian) 専用
	 *
	 * @param byteBuffer
	 *            変換対象の ByteBuffer
	 * @return 変換された文字
	 */
	protected char getDecodedChar() {
		try {
			xmlBuf.get(b2);
			return (char) (((0xff & b2[1]) << 8) | (0xff & b2[0]));
		} catch (Exception e) {
			return '\0';
		}
	}

	/**
	 * XML ファイルの Contents から n 文字取得
	 *
	 * @param length
	 *            取得する文字数
	 * @param index
	 *            コーパスへのインデックス
	 * @return 取得された文字 （index 位置が Contents かどうかのチェックはしていない）
	 */
	public String readStrN(int length, int index) {
		return cr.readStrN(length, index);
	}

	/**
	 * XML ファイルの Contents から逆方向へ n 文字取得
	 *
	 * @param length
	 *            取得する文字数
	 * @param index
	 *            コーパスへのインデックス
	 * @return 取得された文字 （index 位置が Contents かどうかのチェックはしていない）
	 */
	public String readBackStrN(int length, int index) {
		return cr.readBackStrN(length, index);
	}

	
	public String readBackStrNSP(int length, int index) {
		return cr.readBackStrNSP(length, index);
	}

	// n 文字分戻る
	public int readBackN(int length, int index) {
		return cr.readBackN(length, index);
	}

	
	// 現在位置から，n 文字読み飛ばす
	public int skip(int n) {
		return cr.skip(n);
	}


	// 現在位置から，n 文字読み込む
	public String readStrN(int n) {
		return cr.readStrN(n);
	}

	
	public String readStrNSP(int length, int index) {
		return cr.readStrNSP(length, index);
	}

	
	/**
	 * タグの属性値から N 文字取得
	 *
	 * @param length
	 *            取得する文字数
	 * @param index
	 *            コーパスへのインデックス
	 * @return 取得された文字 （index 位置が 属性値かどうかのチェックはしていない）
	 */
	public String readArgumentN(int length, int index) {
		// StringBuilder strBuf = new StringBuilder(length);
		strBuf.delete(0, strBuf.length());

		char c;

		try {
			xmlBuf.position(index * 2);
			while (strBuf.length() < length) {
				c = getDecodedChar();
				if (c == '"') {
					break;
				}
				// System.out.print(c);
				strBuf.append(c);
			}
		} catch (Exception e) {
			System.err.println("Error(CorpusFile, readArgumentN): " + e); //$NON-NLS-1$
		}
		return strBuf.toString();
	}

	/**
	 * Read an argument from XML Element. <code>index</code> is assumed to point
	 * a part of the argument.
	 *
	 * @param index
	 *            int
	 * @return String
	 */
	public String readArgument(int index) {
		// StringBuilder strBuf = new StringBuilder();
		strBuf.delete(0, strBuf.length());

		char c;

		try {
			xmlBuf.position(index * 2);
			while ((c = getDecodedChar()) != '"') {
				strBuf.append(c);
			}
		} catch (Exception e) {
			System.err.println("Error(CorpusFile, readArgument): " + e); //$NON-NLS-1$
		}
		return strBuf.toString();
	}

	/**
	 * index を含む引数全体を取得する
	 *
	 * @param index
	 * @return
	 */
	public String readArgumentAll(int index) {
		int index_tmp = index;
		strBuf.delete(0, strBuf.length());
		//
		// insert KEY_HEAD_MARKER to mark the position of the index (the head of the key string) 
		// example
		//    tag <x arg="ABCDE">, key = "BC"
		//    result "A" + KEY_HEAD_MARKER + "BCDE"
		strBuf.append(KEY_HEAD_MARKER);

		char c;

		try {
			xmlBuf.position(index * 2);

			while (index_tmp > 0) {
				index_tmp--;
				xmlBuf.position(index_tmp * 2);
				c = getDecodedChar();
				if (c != '"') {
					strBuf.append(c);
				} else {
					strBuf.reverse();
					break;
				}
			}

			xmlBuf.position(index * 2);
			while ((c = getDecodedChar()) != '"') {
				strBuf.append(c);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strBuf.toString();
	}

	/**
	 * index の位置にある content を取得する
	 *
	 * @param elementName
	 *            取得する content の要素名
	 * @param index
	 *            取得する content の要素開始タグへのインデックス
	 * @return 取得した content
	 *
	 *         入れ子には対応していない
	 */
	public String getContent(String elementName, int index_byte) {
		char c;
		boolean tagFlag = false;
		StringBuilder strBuf = new StringBuilder();
		StringBuilder tagBuf = new StringBuilder();

		try {
			xmlBuf.position(index_byte * 2);
			// タグ内部の文字を読み飛ばす
			/*
			 * while((c = getDecodedChar(xmlBuf)) != '\0'){ if(c == '>'){ break;
			 * } }
			 */
			// content を取得
			while ((c = getDecodedChar()) != '\0') {
				if (c == '<') {
					tagFlag = true;
					tagBuf.append(c);
				} else if (tagFlag) {
					tagBuf.append(c);
					if (c == '>') {
						tagFlag = false;
						if (tagBuf.toString().compareTo(
								"</" + elementName + ">") == 0) { //$NON-NLS-1$ //$NON-NLS-2$
							return strBuf.toString();
						}
						tagBuf = new StringBuilder();
					}
				} else {
					strBuf.append(c);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * index の位置にある content を取得する
	 *
	 * @param elementName
	 *            取得する content の要素名
	 * @param index
	 *            取得する content 中のインデックス
	 * @return 取得した content。なお，終了時にcontentの先頭に移動する。
	 */
	public String getContent2(String elementName, int index_byte) {
		char c;
		boolean tagFlag = false;
		int index_tmp = index_byte;
		int pHeadofContent = index_byte;
		StringBuilder strBuf = new StringBuilder();
		StringBuilder tagBuf = new StringBuilder();
		String reversedElementName = new StringBuilder(elementName).reverse()
				.toString();

		try {
			xmlBuf.position(index_byte * 2);

			// index よりも前方の content を取得
			while (index_tmp > 0) {
				index_tmp--;
				xmlBuf.position(index_tmp * 2);
				c = getDecodedChar();
				if (c == '>') {
					pHeadofContent = xmlBuf.position();
					tagFlag = true;
				} else if (c == '<') {
					tagFlag = false;
					if (tagBuf.toString().compareTo(reversedElementName) == 0
							|| tagBuf.toString().endsWith(
									" " + reversedElementName)) { //$NON-NLS-1$
						strBuf.reverse();
						tagBuf.setLength(0);
						break;
					}
					tagBuf.setLength(0);
				} else if (!tagFlag) {
					strBuf.append(c);
				} else {
					tagBuf.append(c);
				}
			}

			// index よりも後方の content を取得
			xmlBuf.position(index_byte * 2);
			tagBuf.setLength(0);
			while (true) {
				c = getDecodedChar();
				if (c == '<') {
					tagFlag = true;
				} else if (c == '>') {
					tagFlag = false;
					if (tagBuf.toString().compareTo("/" + elementName) == 0) { //$NON-NLS-1$
						xmlBuf.position(pHeadofContent); // content の先頭に移動
						return strBuf.toString();
					}
					tagBuf.setLength(0);
				} else if (!tagFlag) {
					strBuf.append(c);
				} else {
					tagBuf.append(c);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * 次の要素を取得する ただし，次の要素がテキスト要素の場合は，null
	 *
	 * @param indexOfTailOfElement
	 * @return
	 */
	public String getNextElement(int indexOfTailOfElement) {
		String tag = getTag(indexOfTailOfElement);

		if (tag == null || tag.startsWith("</") || !tag.startsWith("<")) { //$NON-NLS-1$ //$NON-NLS-2$
			// 次の要素が兄弟要素でない場合
			// テキスト要素の場合も含む
			return null;
		}

		try {
			String elementName = getElementName(tag);
			int currentPosition = getPosition() / 2;
			return tag + getContent(elementName, currentPosition)
					+ "</" + elementName + ">"; //$NON-NLS-1$ //$NON-NLS-2$
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * 次の要素を取得する ただし，次の要素がテキスト要素の場合は，null
	 *
	 * @param indexOfTailOfElement
	 * @return
	 */
	@SuppressWarnings("unused")
	public String getNextElement(int indexOfTailOfElement, String elementName) {
		String tag;
		String extractedElementName;

		while (true) {
			tag = getTag(indexOfTailOfElement);
			if (tag == null) {
				return null;
			}
			extractedElementName = getElementName(tag);
			if (extractedElementName.compareTo(elementName) == 0) {
				return tag + getContent(elementName, getPosition() / 2)
						+ "</" + elementName + ">"; //$NON-NLS-1$ //$NON-NLS-2$
			} else if (extractedElementName != null) {
				// read the rest through
				getContent(extractedElementName, getPosition() / 2);
				indexOfTailOfElement = getPosition() / 2;
			} else {
				return null;
			}
		}
	}

	/**
	 * 一つ前の要素を取得する。 ただし，テキスト要素の場合は，null
	 *
	 * @param indexOfHeadOfElement
	 * @return
	 */
	public String getPreviousElement(int indexOfHeadOfElement,
			String elementName) {
		StringBuilder tagBuf = new StringBuilder();
		StringBuilder elementBuf = new StringBuilder();
		String extractedElementName;
		String endTag = ""; //$NON-NLS-1$
		char c;
		boolean tagFlag;

		try {
			do {
				tagFlag = false;

				// 空白文字を読み飛ばす
				do {
					indexOfHeadOfElement--;
					xmlBuf.position(indexOfHeadOfElement * 2);
					c = getDecodedChar();
				} while (c == ' ' || c == '\n' || c == '\t');

				// 前要素の終了タグ取得
				while (true) {
					if (c == '>') {
						tagFlag = true;
						tagBuf.append(c);
					} else if (c == '<') {
						tagBuf.append(c);
						elementBuf.append(tagBuf);
						tagBuf.reverse();
						System.out.println("close: " + tagBuf.toString()); //$NON-NLS-1$
						if (tagBuf.toString().startsWith("</")) { //$NON-NLS-1$
							// 終了タグの場合
							endTag = tagBuf.toString();
							extractedElementName = getElementName(endTag);
							break;
						} else {
							// 開始タグの場合は，兄弟要素でない
							return null;
						}
					} else if (tagFlag) {
						tagBuf.append(c);
					}
					indexOfHeadOfElement--;
					xmlBuf.position(indexOfHeadOfElement * 2);
					c = getDecodedChar();
				}

				// 前要素の開始タグを取得
				tagBuf.setLength(0);
				while (indexOfHeadOfElement > 0) {
					indexOfHeadOfElement--;
					xmlBuf.position(indexOfHeadOfElement * 2);
					c = getDecodedChar();
					elementBuf.append(c);
					if (c == '>') {
						tagFlag = true;
						tagBuf.append(c);
					} else if (c == '<') {
						tagFlag = false;
						tagBuf.append(c);
						String tempTag = tagBuf.reverse().toString();
						if (tempTag
								.startsWith("<" + extractedElementName + ">") || //$NON-NLS-1$ //$NON-NLS-2$
								tempTag
										.startsWith("<" + extractedElementName + " ")) { //$NON-NLS-1$ //$NON-NLS-2$
							break;
						}
						tagBuf.setLength(0);
					} else if (tagFlag) {
						tagBuf.append(c);
					}
				}
				if (elementName.compareTo(extractedElementName) == 0) {
					return elementBuf.reverse().toString();
				} else {
					elementBuf.setLength(0);
					tagBuf.setLength(0);
				}
			} while (true);
		} catch (Exception ex) {
			return null;
		}
	}

	public String getElementName(String tag) {
		int len = tag.length();
		if (!tag.startsWith("<") || !tag.endsWith(">")) { //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}

		int p = tag.indexOf(" "); //$NON-NLS-1$
		if (tag.startsWith("</")) { //$NON-NLS-1$
			// 終了タグの場合
			if (p != -1) {
				// 属性あり
				return tag.substring(2, p);
			} else {
				// 属性なし
				return tag.substring(2, len - 1);
			}
		} else {
			// 開始タグの場合
			if (p != -1) {
				// 属性あり
				return tag.substring(1, p);
			} else {
				// 属性なし
				return tag.substring(1, len - 1);
			}
		}
	}

	public String getAttribute(String tag, String attributeName){
		Pattern p = Pattern.compile(attributeName + " *= *\"(.*?)\""); //$NON-NLS-1$
		Matcher m = p.matcher(tag);
		if(m.find()){
			return m.group(1);
		} else {
			return ""; //$NON-NLS-1$
		}
	}

	
	
	/**
	 *
	 * @param index
	 * @return
	 */
	public int passByTag(int index) {
		char c;

		try {
			xmlBuf.position(index * 2);
			// タグ内部の文字を読み飛ばす
			while ((c = getDecodedChar()) != '\0') {
				index++;
				if (c == '>') {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return index;
	}

	/**
	 * 位置<code>index</code>から始まるタグを取得する
	 *
	 * @param index
	 *            取得するタグの開始位置
	 * @return タグ
	 */
	public String getTag(int index) {
		strBuf.delete(0, strBuf.length());

		char c;

		xmlBuf.position(index * 2);
		// 空白文字を読み飛ばす
		do {
			c = getDecodedChar();
		} while (c == ' ' || c == '\n' || c == '\t');

		// 先頭の文字がタグ開始記号か
		// さもなければ，null 文字を返す
		if (c != '<') {
			return null;
		} else {
			strBuf.append(c);
		}

		while (true) {
			c = getDecodedChar();
			strBuf.append(c);
			if (c == '>') {
				break;
			}
		}
		return strBuf.toString();
	}

	/**
	 *
	 * @param index
	 *            空タグの終了位置
	 * @return
	 */
	public String getEmptyTag(int index) {
		strBuf.delete(0, strBuf.length());

		char c;

		index--;
		xmlBuf.position(index * 2);
		// 先頭の文字がタグ開始記号か
		// さもなければ，空文字を返す
		c = getDecodedChar();
		if (c != '>') {
			return ""; //$NON-NLS-1$
		} else {
			strBuf.append(c);
		}

		while (true) {
			index--;
			xmlBuf.position(index * 2);
			c = getDecodedChar();
			strBuf.append(c);
			if (c == '<') {
				break;
			}
		}
		return strBuf.reverse().toString();
	}

	public String getSource(Range r) {
		int rangeValue = r.getValue();
		byte source[] = new byte[rangeValue * 2];

		try {
			xmlBuf.position(r.getStart() * 2);
			xmlBuf.get(source);

			return new String(source, "UnicodeLittle"); //$NON-NLS-1$
		} catch (Exception e) {
			System.err.println("error: " + e); //$NON-NLS-1$
			return ""; //$NON-NLS-1$
		}
	}

	
	public int gotoElement(String elementName, String attributeName, String value){
		char c;
		boolean flagTag = false;
		boolean flagTarget = false; // 閉じタグだけでは，別属性だったときなどに判断できないため，対象の要素かどうかを判定する
		int tagDepth = 0;
		int initPosition = xmlBuf.position();
		StringBuilder tagBuf = new StringBuilder();
		
		if(elementName.isEmpty()){
			return -1;
		}
		

		do {
			c = getDecodedChar();
			if (c == '>') {
				tagBuf.append(c);
				String tag = tagBuf.toString();
				String tagName = getElementName(tag);
				tagBuf = new StringBuilder();
				flagTag = false;
				
				
				// empty tag
				if(tag.endsWith("/>")){ //$NON-NLS-1$
					continue;
				} // close
				else if(tag.startsWith("</")){ //$NON-NLS-1$
					if(tagName.equals(elementName)){
						tagDepth--;
						if(tagDepth == -1){
							System.exit(-1);
						}

						
						if(tagDepth == 0 && flagTarget){
							int cPosition = xmlBuf.position();
							xmlBuf.position(initPosition);
							return cPosition;
						}
					}
				} // open
				else {
					if(tagName.equals(elementName)){
						if(tagDepth == 0){
							if(!attributeName.isEmpty() && !getAttribute(tag, attributeName).equals(value)){
								tagDepth++;
								continue;
							}
							initPosition = xmlBuf.position();
							flagTarget = true;
						}
						tagDepth++;
					}
				}
			} else if (c == '<') {
				flagTag = true;
				tagBuf.append(c);
			} else if(flagTag){
				tagBuf.append(c);
			}
		} while (c != '\0');

		return -1;
	}
	
	
	public boolean isTagFinished(String line) {
		int n = 0;
		char c;

		for (int i = 0; i < line.length(); i++) {
			c = line.charAt(i);
			if (c == '<') {
				n++;
			} else if (c == '>') {
				n--;
			}
		}
		if (n == 0) {
			return true;
		} else {
			return false;
		}
	}

	public int getPosition() {
		return xmlBuf.position();
	}

	public void setPosition(int index) throws IOException {
		// xmlBufがnullの場合は初期化しなおす処理を追加
		if (xmlBuf == null)
			init();

		xmlBuf.position(index);
	}
	
	
	public void setPositionToContents() throws IOException{
		boolean flagTag = false;
		char c;
		
		while ((c = getDecodedChar()) != '\0') {
			if (c == '\n' || c == '\t' || c == ' ' || c == 0xfeff) {
			} else if (c == '<') {
				flagTag = true;
			} else if (c == '>') {
				flagTag = false;
			} else if (flagTag) {
			} else {
				setPosition(getPosition()-2);
				return;
			}
		}
		return;
	}


	public void setPositionToContents(String elementName, String attributeName, String value) throws IOException{
		boolean flagTag = false;
		char c;
		int cp = -1;
		StringBuffer tagBuf = new StringBuffer();
		
		while ((c = getDecodedChar()) != '\0') {
			if (c == '<') {
				flagTag = true;
				cp = getPosition();
				tagBuf.append(c);
			} else if (c == '>') {
				flagTag = false;
				tagBuf.append(c);
				String tag = tagBuf.toString();
				if(!tag.startsWith("</") && getElementName(tag).equals(elementName) && (attributeName.isEmpty() || getAttribute(tag, attributeName).equals(value))){ //$NON-NLS-1$
					setPosition(cp-2);
					return;
				}
				tagBuf.setLength(0);
			} else if (flagTag) {
				tagBuf.append(c);
			} else if (c == '\n' || c == '\t' || c == '\r' || c == ' ' || c == 0xfeff) {

			} else {
				setPosition(getPosition()-2);
				return;
			}
		}
		return;
	}

	
	public ElementIndex getEix(String elementName) {
		ElementIndex eix;
		for (int i = 0; i < eixVector.size(); i++) {
			eix = eixVector.get(i);
			if (eix.getElementName().compareTo(elementName) == 0) {
				return eix;
			}
		}
		return null;
	}


	public String getIOFilename() {
		return basename + SUFFIX_XML;
	}

	
	public boolean ExistsInDbLinks(String FieldName){
		return dixMap.containsKey(FieldName);
	}
	
	
	public DBController getDBController(){
		return dbController;
	}

	
	public void setDBController(DBController dbController){
		this.dbController = dbController;
	}

	
	
	public void deleteIndexes(){
		for(ContentsIndex contentsIndex : getCixVector()){
			try {
				contentsIndex.deleteIndex();
			} catch (IOException e) {
				System.err.println("Warning(CorpusFile): Failed to delete index file, " + contentsIndex.getIOFilename()); //$NON-NLS-1$
			}
		}

		for(ElementIndex elementIndex : getEixVector()){
			if(elementIndex.getIOFilename() != null &&
					(elementIndex.getIOFilename().endsWith(XmlElementIndex.SUFFIX) || elementIndex.getIOFilename().endsWith(StandoffElementIndex.SUFFIX))){
				try {
					elementIndex.deleteIndex();
				} catch (IOException e) {
					System.err.println("Warning(CorpusFile): Failed to delete index file, " + elementIndex.getIOFilename()); //$NON-NLS-1$
				}
			}
		}

		for(ArgumentIndex argumentIndex : getAixVector()){
			if(argumentIndex.isDB){
				continue;
			}
			if(argumentIndex.getIOFilename() != null && argumentIndex.getIOFilename().endsWith(XmlArgumentIndex.SUFFIX)){
				try {
					argumentIndex.deleteIndex();
				} catch (IOException e) {
					System.err.println("Warning(CorpusFile): Failed to delete index file, " + argumentIndex.getIOFilename()); //$NON-NLS-1$
				}
			}
		}
	}
	
	
	public HashMap<String, CorpusElementInfo> analyze(HashMap<String, CorpusElementInfo> initElementsInfo){

		elementMap = initElementsInfo;

		SAXParserFactory spfactory = SAXParserFactory.newInstance();
		SAXParser parser;
		try {
			parser = spfactory.newSAXParser();
			CorpusHandler corpusHandler = new CorpusHandler(elementMap);
			parser.parse(getFile(), corpusHandler);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
		
		for(ElementIndex elementIndex : eixVector){
			if (elementIndex instanceof StandoffElementIndex) {
				SIXDic dic = ((StandoffElementIndex)elementIndex).getDic();
				String elementName = elementIndex.getElementName();

				if(!elementMap.containsKey(elementName)){
					elementMap.put(elementName, new CorpusElementInfo(elementName));
				}
				
				CorpusElementInfo corpusElementInfo = elementMap.get(elementName);
				for(String attributeName : dic.getFieldNames()){
					if(!corpusElementInfo.containsKey(attributeName)){
						corpusElementInfo.setSelected(attributeName, false);
					}
				}
			}
		}

		return elementMap;
	}
	
	
	public boolean loadStructure(HashMap<String, CorpusElementInfo> elementMap) throws IOException {

		File dosFile = new File(getBasename() + SUFFIX_DOS);

		if (dosFile.exists()) {
			BufferedReader br;
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					dosFile), DEFAULT_DOS_ENCODING));

			String line;
			CorpusElementInfo corpusElementInfo;

			while ((line = br.readLine()) != null) {
				// 0: elementName, 1: attributeName
				String[] data = line.split(DOS_FIELD_SEPARATOR);

				if (!elementMap.containsKey(data[0])) {
					corpusElementInfo = new CorpusElementInfo(data[0]);
					elementMap.put(data[0], corpusElementInfo);
				} else {
					corpusElementInfo = elementMap.get(data[0]);
				}

				if(data.length < 2){
					// no attributes
				} else if (!corpusElementInfo.containsKey(data[1])) {
					corpusElementInfo.setSelected(data[1], false);
				} else {
					// duplicate attributes
				}
			}
			
			br.close();
		} else {
			analyze(elementMap);
			saveStructure(elementMap);
		}

		return true;
	}	

	
	public void saveStructure(HashMap<String, CorpusElementInfo> elementMap) throws IOException {
		File dosFile = new File(getBasename() + SUFFIX_DOS);

		PrintWriter pw;
		pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
				dosFile), DEFAULT_DOS_ENCODING));

		for (String elementName : elementMap.keySet()) {
			CorpusElementInfo corpusElementInfo = elementMap.get(elementName);
			for (String attribute : corpusElementInfo.keySet()) {
				pw.println(elementName + DOS_FIELD_SEPARATOR + attribute);
			}
		}

		pw.close();
		System.err
				.println("Message(CorpusFile): wrote a document structure file, " //$NON-NLS-1$
						+ dosFile.toString());
	}

	class CorpusHandler extends DefaultHandler {

		HashMap<String, CorpusElementInfo> xmlElementMap;
		
		public CorpusHandler(HashMap<String, CorpusElementInfo> xmlElementMap) {
			super();
			this.xmlElementMap = xmlElementMap;
		}


		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if(!xmlElementMap.containsKey(qName)){
				System.err.println("start local:" + localName + ", " + qName ); //$NON-NLS-1$ //$NON-NLS-2$
				CorpusElementInfo xmlElementInfo = new CorpusElementInfo(qName);
				if(attributes.getLength() == 0){
					xmlElementInfo.setSelected("", false); // no attributes //$NON-NLS-1$
				} else {
					for(int i = 0; i < attributes.getLength(); i++){
						xmlElementInfo.setSelected(attributes.getQName(i), false);
					}
				}
				xmlElementMap.put(qName, xmlElementInfo);
			} else {
				CorpusElementInfo xmlElementInfo = xmlElementMap.get(qName);
				if(attributes.getLength() != 0){
					for(int i = 0; i < attributes.getLength(); i++){
						String attribute = attributes.getQName(i);
						if(!xmlElementInfo.containsKey(attribute)){
							xmlElementInfo.setSelected(attribute, false);
						}
					}
					if(xmlElementInfo.containsKey("")){ //$NON-NLS-1$
						xmlElementInfo.remove(""); //$NON-NLS-1$
					}
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if(xmlElementMap.containsKey(qName)){
				xmlElementMap.get(qName).setEmpty(false);
			} else {
				throw new SAXException();
			}
		}
		
		public HashMap<String, CorpusElementInfo> getXMLElementMap(){
			return xmlElementMap;
		}
	}
}

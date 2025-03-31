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
 * @(#)ArgumentIndex.java  ver.1.1, 2005-06-05
 *
 * Copyright 2003-2005
 * National Institute for Japanese Language All rights reserved.
 */

package jp.ac.ninjal.himawari;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;

/**
 * Index for argument of XML document
 *
 * @author Masaya YAMAGUCHI
 * @version 1.1
 *
 */
public class XmlArgumentIndex extends ArgumentIndex {
	public final static String SUFFIX = ".aix"; //$NON-NLS-1$
	final static int SEARCH_FORWARD = 1;
	final static int SEARCH_BACKWORD = -1;
	final static int SEARCH_FINISHED = 0;

	protected CorpusFile corpus;
	private String aixFilename;
	protected RandomAccessFile raCix;
	protected IntBuffer cixBuf;
	private FileChannel fc;
	protected int recCixPos;
	protected String fieldName;
	protected String key;
	protected FieldInfo fieldInfo;
	protected int searchDirection;
	public String type = ""; //$NON-NLS-1$

	/**
	 *
	 * Constructor of ArgumentIndex Class
	 *
	 * @param elementName
	 *            String target element name
	 * @param middleName
	 *            String middle name of index file
	 * @param argumentName
	 *            String target argument name in the element
	 * @param isCompleteMatch
	 *            boolean flag for specifying whether the matching method is
	 *            "complete match" or not. true if the matching method is
	 *            comlete matching.
	 * @param corpus
	 *            CorpusFile instance of CorpusFile Class
	 *
	 */
	public XmlArgumentIndex(String elementName, String middleName,
			String argumentName, boolean isCompleteMatch, CorpusFile corpus) {
		super.elementName = elementName;
		super.argumentName = argumentName;
		super.isCompleteMatch = isCompleteMatch;
		this.corpus = corpus;
		try {
			this.aixFilename = corpus.getBasename() + "." + middleName + "." //$NON-NLS-1$ //$NON-NLS-2$
					+ URLEncoder.encode(argumentName, "utf-8") + SUFFIX; //$NON-NLS-1$
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
		}
	}

	
	/**
	 * Opens index file
	 *
	 * @throws IOException
	 *
	 */
	public void open() throws IOException {
		raCix = new RandomAccessFile(aixFilename, "r"); //$NON-NLS-1$
		fc = raCix.getChannel();
		cixBuf = fc.map(FileChannel.MapMode.READ_ONLY, 0, raCix.length())
				.asIntBuffer();
	}

	public boolean isOpen() {
		if (raCix != null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 *
	 * Closes index file
	 *
	 * @throws IOException
	 *
	 */
	public void close() throws IOException {
		if (fc != null)fc.close();
		if (raCix != null)raCix.close();
		raCix = null;
		cixBuf = null;
	}

	/**
	 *
	 * When the index file specified in the constructor is existed, return true.
	 * Otherwise false.
	 *
	 * @return boolean
	 */
	public boolean exists() {
		File aixFile = new File(aixFilename);
		return aixFile.exists();
	}

	public String getElementName() {
		return elementName;
	}

	public String getFilename() {
		return aixFilename;
	}


	/**
	 * Sets search conditions. Arguments are substituted to the global
	 * variables.
	 *
	 * @param fieldInfo
	 *            FieldInfo field information of the result record
	 * @param fieldName
	 *            String the name of field where the target string is stored
	 */

	public void setRetrieveCondition(FieldInfo fieldInfo, String fieldName) {
		this.fieldInfo = fieldInfo;
		this.fieldName = fieldName;
	}

	/**
	 * Retrieves the first result
	 *
	 * @param String
	 *            key
	 * @return
	 */
	public ResultRecord retrieveFirst(String key) {
		ResultRecord resultRecord = null;
		int suffixNumber;
		String txtLine = null;
		int resCmp = 0;
		int start = 0;
		int end = 0;
		String content = null;
		int curCixPos = 0;
		this.key = key;
		searchDirection = SEARCH_FORWARD;

		try {
			end = (int) raCix.length() / 4;
		} catch (IOException e) {
			return null;
		}

		while (start <= end) {
			curCixPos = (start + end) / 2;
			try {
				/* ary からテキスト位置の読込み */
				cixBuf.position(curCixPos);
				suffixNumber = cixBuf.get();

				/* テキストの読込み */
				txtLine = corpus.readArgument(suffixNumber);
			} catch (Exception e) {
				return null;
				
			}

			/* テキストとの比較 */
			resCmp = txtLine.compareTo(key);
			if (resCmp == 0 || (!isCompleteMatch && txtLine.startsWith(key))) {
				// 残りのタグを読み飛ばす
				suffixNumber = corpus.passByTag(suffixNumber);

				// content を取得
				content = corpus.getContent(elementName, suffixNumber);

				recCixPos = curCixPos - 1;
				resultRecord = new ResultRecord(fieldInfo);
				resultRecord.setPosition(suffixNumber);
				resultRecord.set(fieldName, content);
				return resultRecord;
			} else if (resCmp > 0) {
				end = curCixPos - 1;
			} else {
				start = curCixPos + 1;
			}
		}
		return resultRecord;
	}

	public ResultRecord retrieveNext() {
		int suffixNumber;
		String txtLine = null;
		String content = null;
		ResultRecord resultRecord = null;

		if (searchDirection == SEARCH_FORWARD) {
			try {
				/* ary からテキスト位置の読込み */
				suffixNumber = cixBuf.get();
				/* テキストの読込み */
				txtLine = corpus.readArgument(suffixNumber);
				if (txtLine.compareTo(key) != 0
						&& (isCompleteMatch || !txtLine.startsWith(key))) {
					searchDirection = SEARCH_BACKWORD;
				} else {
					// 残りのタグを読み飛ばす
					suffixNumber = corpus.passByTag(suffixNumber);
					// content を取得
					content = corpus.getContent(elementName, suffixNumber);
					resultRecord = new ResultRecord(fieldInfo);
					resultRecord.setPosition(suffixNumber);
					resultRecord.set(fieldName, content);
					return resultRecord;
				}
			} catch (Exception e) {
				searchDirection = SEARCH_BACKWORD;
			}
		}
		// suffay array 後方方向へ検索
		if (recCixPos > -1) {
			cixBuf.position(recCixPos);
		} else {
			return null;
		}
		/* ary からテキスト位置の読込み */
		suffixNumber = cixBuf.get();
		/* テキストの読込み */
		txtLine = corpus.readArgument(suffixNumber);
		if (txtLine.compareTo(key) != 0
				&& (isCompleteMatch || !txtLine.startsWith(key))) {
			return null;
		}

		// 残りのタグを読み飛ばす
		suffixNumber = corpus.passByTag(suffixNumber);
		// content を取得
		content = corpus.getContent(elementName, suffixNumber);

		recCixPos--;
		resultRecord = new ResultRecord(fieldInfo);
		resultRecord.setPosition(suffixNumber);
		resultRecord.set(fieldName, content);
		return resultRecord;
	}

	// cix ファイルを生成する
	public void mkaix(boolean isCompleteMatch) {
		BufferedReader brXML;
		File xmlFile;
		File aixFile;
		IntBuffer iSuffixBuffer;
		RandomAccessFile raSuf;
		MappedByteBuffer xmlBuf;

		try {
			xmlFile = corpus.getFile();
			aixFile = new File(aixFilename);
			brXML = new BufferedReader(new InputStreamReader(
					new FileInputStream(xmlFile), "UnicodeLittle")); //$NON-NLS-1$
			raSuf = new RandomAccessFile(aixFile, "rw"); //$NON-NLS-1$
			xmlBuf = corpus.getBuf();

			int iXml = 1; // 文字コード識別用コードを読み飛ばす
			int iSuf = 0;

			String line;
			String tmpLine = null;

			// インデックス対象の文字列数(iSuf)を計測
			while ((line = brXML.readLine()) != null) {
				// 前行を append
				if (tmpLine != null) {
					line = tmpLine + "\n" + line; //$NON-NLS-1$
				}
				// タグが１行で完結しているかチェック
				if (line.lastIndexOf('<') > line.lastIndexOf('>')) {
					tmpLine = line;
					continue;
				} else {
					tmpLine = null;
				}

				int iLine = 0;
				while (iLine < line.length()) {
					if (line.startsWith("<" + elementName + " ", iLine)) { //$NON-NLS-1$ //$NON-NLS-2$
						iLine += 1 + elementName.length(); // iLine を空白の位置にする

						// タグの末尾の > を検索
						int iNextBracket = line.indexOf('>', iLine);
						// 属性の位置を検索
						int iArgument = line.indexOf(
								" " + argumentName + "=", iLine); //$NON-NLS-1$ //$NON-NLS-2$
						if (iArgument == -1) {
							iArgument = line.indexOf(
									" " + argumentName + " ", iLine); //$NON-NLS-1$ //$NON-NLS-2$
							if (iArgument == -1) {
								iLine = iNextBracket + 1;
								continue;
							}
						}

						if (iArgument < iNextBracket) {
							iLine = iArgument + argumentName.length() + 2;
							while (line.startsWith(" ", iLine) || //$NON-NLS-1$
									line.startsWith("=", iLine)) {iLine++;} // 空白をスキップ //$NON-NLS-1$
							if (line.startsWith("\"", iLine)) { //$NON-NLS-1$
								iLine++;
								while (!line.startsWith("\"", iLine)) { //$NON-NLS-1$
									iSuf++;
									iLine++;
									if (isCompleteMatch) {
										break;
									}
								}
							}
						}
						iLine = iNextBracket + 1;
					} else {
						iLine++;
					}
				} // while (iLine < line.length()) {
			} // while((line = brXML.readLine()) != null){

			if(brXML != null)brXML.close(); // 一度 close する


			brXML = new BufferedReader(new InputStreamReader(
					new FileInputStream(xmlFile), "UnicodeLittle")); //$NON-NLS-1$
			iSuffixBuffer = raSuf.getChannel().map(
					FileChannel.MapMode.READ_WRITE, 0, iSuf * 4).asIntBuffer();
			System.out
					.println("size: " + iSuffixBuffer.capacity() + ", " + iSuf); //$NON-NLS-1$ //$NON-NLS-2$
			iSuf = 0;

			while ((line = brXML.readLine()) != null) {
				// 前行を append
				if (tmpLine != null) {
					line = tmpLine + "\n" + line; //$NON-NLS-1$
				}
				// タグが１行で完結しているかチェック
				if (line.lastIndexOf('<') > line.lastIndexOf('>')) {
					tmpLine = line;
					continue;
				} else {
					tmpLine = null;
				}

				int iLine = 0;
				while (iLine < line.length()) {
					if (line.startsWith("<" + elementName + " ", iLine)) { //$NON-NLS-1$ //$NON-NLS-2$
						iLine += 1 + elementName.length(); // iLine を空白の位置にする

						// タグの末尾の > を検索
						int iNextBracket = line.indexOf('>', iLine);
						// 属性の位置を検索
						int iArgument = line.indexOf(
								" " + argumentName + "=", iLine); //$NON-NLS-1$ //$NON-NLS-2$
						if (iArgument == -1) {
							iArgument = line.indexOf(
									" " + argumentName + " ", iLine); //$NON-NLS-1$ //$NON-NLS-2$
							if (iArgument == -1) {
								iLine = iNextBracket + 1;
								continue;
							}
						}

						if (iArgument < iNextBracket) {
							iLine = iArgument + argumentName.length() + 2;
							while (line.startsWith(" ", iLine) || //$NON-NLS-1$
									line.startsWith("=", iLine)) {iLine++;} // 空白をスキップ //$NON-NLS-1$
							if (line.startsWith("\"", iLine)) { //$NON-NLS-1$
								iLine++;
								while (!line.startsWith("\"", iLine)) { //$NON-NLS-1$
									iSuffixBuffer.put(iSuf, iXml + iLine);
									iLine++;
									iSuf++;
									if (isCompleteMatch) {
										break;
									}
								}
							}
						}
						iLine = iNextBracket + 1;
					} else {
						iLine++;
					}
				} // while (iLine < line.length()) {
				iXml += line.length() + 1;
				
			} // while((line = brXML.readLine()) != null){

			System.out.println("iSuf: " + iSuf); //$NON-NLS-1$
			qsortBySuffix(iSuffixBuffer, 0, iSuf - 1,
					new ArgumentStrComparator(xmlBuf));
			System.out.println("iSuf: " + iSuf); //$NON-NLS-1$
			if(brXML != null){
				brXML.close();
			}
			if(raSuf != null){
				raSuf.close();
			}
		} catch (IOException e) {
			System.err.println("Error(mkaix): " + e); //$NON-NLS-1$
		}

	}

	public String getIOFilename() {
		return aixFilename;
	}
}

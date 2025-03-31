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
 * @(#)ContentsIndex.java	0.9.4 2003-10-20
 *
 * Copyright 2003
 * National Institute for Japanese Language All rights reserved.
 */
package jp.ac.ninjal.himawari;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;

public class ContentsSuffixArray extends ContentsIndex {
	public final static String suffixCIX = ".cix"; //$NON-NLS-1$

	final static int SEARCH_FORWARD = 1;

	final static int SEARCH_BACKWORD = -1;

	final static int SEARCH_FINISHED = 0;

	private String baseFilename;

	private String cixFilename; // コンテンツのインデックスファイル(cix ファイル）

	// protected String elementName;

	protected RandomAccessFile raCix;

	protected IntBuffer cixBuf;

	// protected CorpusFile corpus;
	protected StringBuilder strBuf = new StringBuilder(512);

	private FileChannel fc;

	private String stopElementName;

	protected int searchDirection;

	protected FieldInfo fieldInfo;

	protected String target;

	protected String fieldName;

	protected int targetLength;

	protected int recCixPos = 0;

	public ContentsSuffixArray(String elementName, String middleName,
			CorpusFile corpus) {
		this.corpus = corpus;
		this.elementName = elementName;
		this.baseFilename = corpus.getBasename();
		this.cixFilename = baseFilename + "." + middleName + suffixCIX; //$NON-NLS-1$
		// this.xmlBuf = corpus.getBuf();
		stopElementName = null;
	}

	public ContentsSuffixArray(String elementName, String middleName,
			CorpusFile corpus, String stopElementName) {
		this.corpus = corpus;
		this.elementName = elementName;
		this.baseFilename = corpus.getBasename();
		this.cixFilename = baseFilename + "." + middleName + suffixCIX; //$NON-NLS-1$
		// this.xmlBuf = corpus.getBuf();
		this.stopElementName = stopElementName;
	}

	/**
	 * Open cix file
	 * 
	 * @throws IOException
	 */
	public void open() throws IOException {
		raCix = new RandomAccessFile(cixFilename, "r"); //$NON-NLS-1$
		fc = raCix.getChannel();
		cixBuf = fc.map(FileChannel.MapMode.READ_ONLY, 0, raCix.length())
				.asIntBuffer();
		if (stopElementName == null || stopElementName.compareTo("") == 0) { //$NON-NLS-1$
			corpus.setContentsReader(new ContentsReaderDefault(corpus));
		} else {
			corpus.setContentsReader(new ContentsReaderConstraint(corpus,
					stopElementName));
//			System.out.println("se: ci, " + stopElementName); //$NON-NLS-1$
		}
	}

	/**
	 * Close cix file
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		if (fc != null)	fc.close();
		fc = null;
		if (raCix != null) raCix.close();
		raCix = null;
		cixBuf = null;
//		System.gc();
//		System.runFinalization();
	}

	public boolean exists() {
		File f = new File(cixFilename);
		return f.exists();
	}

	public String getFilename() {
		return cixFilename;
	}

	public void setStopElement(String stopElementName) {
		this.stopElementName = stopElementName;
	}

	// cix ファイルを生成する
	public int mkcix(String elementName) {
		MappedByteBuffer xmlBuf;
		BufferedReader brXml;
		File xmlFile;
		File cixFile;
		IntBuffer iSuffixBuffer;
		RandomAccessFile raSuf;

		try {
			xmlFile = corpus.getFile();
			cixFile = new File(cixFilename);
			brXml = new BufferedReader(new InputStreamReader(
					new FileInputStream(xmlFile), "UnicodeLittle")); //$NON-NLS-1$
			raSuf = new RandomAccessFile(cixFile, "rw"); //$NON-NLS-1$
			xmlBuf = corpus.getBuf();
			int c;

			int iXml = 1; // 文字コード識別用コードを読み飛ばす
			int iSuf = 0;
			boolean tagFlag = false;
			boolean targetFlag = false;
			StringBuilder sb = new StringBuilder(50);
			// iSuffixBuffer =
			// raSuf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0,
			// xmlFile.length()*4).asIntBuffer();

			// インデックス対象の文字列数(iSuf)を計測
			while ((c = brXml.read()) != -1) {
				if (c == '<') {
					if (tagFlag) {
						System.out
								.println("Error(ContentsIndex): '<' may exist in a tag or " + //$NON-NLS-1$
										"an another '<' may exist in contents, " //$NON-NLS-1$
										+ brXml.readLine());
					}
					// タグの開始
					tagFlag = true;
					sb = new StringBuilder(50);
					sb.append((char) c);
				} else if (c == '>') {
					// タグの終了
					sb.append((char) c);
					if (!tagFlag) {
						System.out
								.println("Error(ContentsIndex): '>' existed in contents, " //$NON-NLS-1$
										+ brXml.readLine());
					}
					if (sb.indexOf("<" + elementName + " ") == 0 || //$NON-NLS-1$ //$NON-NLS-2$
							sb.indexOf("<" + elementName + ">") == 0) { //$NON-NLS-1$ //$NON-NLS-2$
						targetFlag = true;
					} else if (sb.indexOf("</" + elementName + ">") == 0 || //$NON-NLS-1$ //$NON-NLS-2$
							sb.indexOf("</" + elementName + " ") == 0) { //$NON-NLS-1$ //$NON-NLS-2$
						targetFlag = false;
					}
					tagFlag = false;
				} else if (tagFlag) {
					// タグの内部
					sb.append((char) c);
				} else if (c != '\n' && targetFlag) {
					// iSuffixBuffer.put(iSuf, iXml); // byte 数で位置を記録
					iSuf++;
				}
				// iXml++;
			}
			brXml.close(); // 一度 close する
			brXml = new BufferedReader(new InputStreamReader(
					new FileInputStream(xmlFile), "UnicodeLittle")); //$NON-NLS-1$
			iSuffixBuffer = raSuf.getChannel().map(
					FileChannel.MapMode.READ_WRITE, 0, iSuf * 4).asIntBuffer();
			System.out
					.println("size: " + iSuffixBuffer.capacity() + ", " + iSuf); //$NON-NLS-1$ //$NON-NLS-2$
			iSuf = 0;
			tagFlag = false;
			targetFlag = false;

			// indexing 開始
			while ((c = brXml.read()) != -1) {
				if (c == '<') {
					if (tagFlag) {
						System.out
								.println("Error(ContentsIndex): '<' may exist in a tag or " + //$NON-NLS-1$
										"an another '<' may exist in contents, " //$NON-NLS-1$
										+ brXml.readLine());
					}
					// タグの開始
					tagFlag = true;
					sb = new StringBuilder(50);
					sb.append((char) c);
				} else if (c == '>') {
					// タグの終了
					sb.append((char) c);
					if (!tagFlag) {
						System.out
								.println("Error(ContentsIndex): '>' existed in contents, " //$NON-NLS-1$
										+ brXml.readLine());
					}
					if (sb.indexOf("<" + elementName + " ") == 0 || //$NON-NLS-1$ //$NON-NLS-2$
							sb.indexOf("<" + elementName + ">") == 0) { //$NON-NLS-1$ //$NON-NLS-2$
						targetFlag = true;
					} else if (sb.indexOf("</" + elementName + ">") == 0 || //$NON-NLS-1$ //$NON-NLS-2$
							sb.indexOf("</" + elementName + " ") == 0) { //$NON-NLS-1$ //$NON-NLS-2$
						targetFlag = false;
					}
					tagFlag = false;
				} else if (tagFlag) {
					// タグの内部
					sb.append((char) c);
				} else if (c != '\n' && targetFlag) {
					iSuffixBuffer.put(iSuf, iXml); // byte 数で位置を記録
					iSuf++;
				}
				iXml++;
			}

			if (stopElementName == null || stopElementName.compareTo("") == 0) { //$NON-NLS-1$
//				System.out.println("tgcmp"); //$NON-NLS-1$
				qsortBySuffix(iSuffixBuffer, 0, iSuf - 1, new TaggedStrCmp(
						xmlBuf));
			} else {
//				System.out.println("sscmp"); //$NON-NLS-1$
				qsortBySuffix(iSuffixBuffer, 0, iSuf - 1,
						new TaggedStrCmpStopElement(xmlBuf, stopElementName));
			}
			System.out.println("iSuf: " + iSuf); //$NON-NLS-1$
			if (raSuf != null)
				raSuf.close();
			if (brXml != null)
				brXml.close();
		} catch (IOException e) {
			System.out.println("ContentsIndex, mkcix): " + e); //$NON-NLS-1$
			return -1;
		}
		System.out.println("finished!"); //$NON-NLS-1$
		return (int) xmlFile.length();
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
	 * 
	 * Retrieves the target string, and returns the first ResultRecord of the
	 * whole ResultRecord set. This method is the implimentation of the abstract
	 * method in Index class.
	 * 
	 * @param target
	 *            String the target string
	 * @return ResultRecord the result record. Returns null value if the target
	 *         string is not retrieved.
	 * @throws Exception
	 * 
	 */

	public ResultRecord retrieveFirst(String target) {
		int start = 0;
		int end = 0;
		int suffixNumber;
		int resCmp = 0;
		int curCixPos = 0;
		String txtLine = null;
		ResultRecord resultRecord = null;
		searchDirection = SEARCH_FORWARD;
		this.target = target;
		targetLength = target.length();

		try {
			end = (int) raCix.length() / 4;

			while (start <= end) {
				curCixPos = (start + end) / 2;

				// Get a suffix number
				cixBuf.position(curCixPos);
				suffixNumber = cixBuf.get();

				// Read text with <code>cTargetLength</code> characters
				txtLine = corpus.readStrN(targetLength, suffixNumber);

				// Compare the target string to the text
				resCmp = fstrcmp(txtLine, target);
				if (resCmp == 0) {
					recCixPos = curCixPos - 1;
					resultRecord = new ResultRecord(fieldInfo);
					resultRecord.setPosition(suffixNumber);
					resultRecord.set(fieldName, target);
					return resultRecord;
				} else if (resCmp > 0) {
					end = curCixPos - 1;
				} else {
					start = curCixPos + 1;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			// throw e;
		}
		return null;
	}

	/**
	 * 
	 * Retrieves the target string using the arguments given by searchFirst
	 * Method, and returns the next ResultRecord of the whole ResultRecord set.
	 * 
	 * @return ResultRecord the result record. Returns null value if the target
	 *         string is searchded.
	 * 
	 */
	public ResultRecord retrieveNext() {
		int suffixNumber;
		String txtLine = null;
		ResultRecord resultRecord = null;

		try {
			if (searchDirection == SEARCH_FORWARD) {
				/* ary からテキスト位置の読込み */
				suffixNumber = cixBuf.get();
				/* テキストの読込み */
				txtLine = corpus.readStrN(targetLength, suffixNumber);
				if (fstrcmp(txtLine, target) != 0) {
					searchDirection = SEARCH_BACKWORD;
				} else {
					resultRecord = new ResultRecord(fieldInfo);
					resultRecord.setPosition(suffixNumber);
					resultRecord.set(fieldName, target);
					return resultRecord;
				}
			}
		} catch (Exception e) {
			System.out.println("exception"); //$NON-NLS-1$
			searchDirection = SEARCH_BACKWORD;
		}

		// try {
		// suffay array 後方方向へ検索
		if(recCixPos == -1) return null;
//		System.err.println(recCixPos);
		cixBuf.position(recCixPos);
		/* ary からテキスト位置の読込み */
		suffixNumber = cixBuf.get();
		/* テキストの読込み */
		txtLine = corpus.readStrN(targetLength, suffixNumber);
		if (fstrcmp(txtLine, target) != 0) {
		} else {
			resultRecord = new ResultRecord(fieldInfo);
			resultRecord.setPosition(suffixNumber);
			resultRecord.set(fieldName, target);
			recCixPos--;
			return resultRecord;
		}
		// } catch (Exception e) {
		// e.printStackTrace();
		// throw e;
		// }
		return null;
	}

	public String getIOFilename() {
		return cixFilename;
	}
}

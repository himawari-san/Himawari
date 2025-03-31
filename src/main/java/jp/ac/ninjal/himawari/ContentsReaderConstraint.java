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

public class ContentsReaderConstraint extends ContentsReader {
	private StringBuilder tagBuf = new StringBuilder(100);
	private String reversedStopElement_start;
	private String stopElement_end;

	public ContentsReaderConstraint(CorpusFile corpus, String stopElementName) {
		this.corpus = corpus;
		reversedStopElement_start = new StringBuilder(stopElementName).reverse()
				.toString();
		stopElement_end = "/" + stopElementName; //$NON-NLS-1$
	}

	/**
	 * 
	 * @param length
	 *            取得する文字数
	 * @param index
	 *            コーパスへのインデックス
	 * @return 取得された文字 （index 位置が Contents かどうかのチェックはしていない）
	 * @todo この sfs01.ContentsReader メソッドを実装
	 */
	public String readBackStrN(int length, int index) {
		char c;
		boolean tagFlag = false;

		strBuf.setLength(0);
		tagBuf.setLength(0);

		try {
			while (strBuf.length() < length && index > 0) {
				index--;
				corpus.setPosition(index * 2);
				c = corpus.getDecodedChar();
				if (c == '>') {
					tagFlag = true;
				} else if (c == '<') {
					tagFlag = false;
					if (tagBuf.toString().compareTo(reversedStopElement_start) == 0
							|| tagBuf.toString().endsWith(
									" " + reversedStopElement_start)) { //$NON-NLS-1$
						return strBuf.reverse().toString();
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
			System.err.println("index: " + index); //$NON-NLS-1$
		}
		return strBuf.reverse().toString();

	}

	/**
	 * 
	 * @param length
	 *            取得する文字数
	 * @param index
	 *            コーパスへのインデックス
	 * @return 取得された文字 （index 位置が Contents かどうかのチェックはしていない）
	 * @todo この sfs01.ContentsReader メソッドを実装
	 */
	public String readStrN(int length, int index) {
		char c;
		boolean tagFlag = false;

		strBuf.setLength(0);
		tagBuf.setLength(0);
		try {
			corpus.setPosition(index * 2);
			while (strBuf.length() < length) {
				c = corpus.getDecodedChar();
				if (c == '<') {
					tagFlag = true;
				} else if (c == '>') {
					tagFlag = false;
					if (tagBuf.toString().compareTo(stopElement_end) == 0) {
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
		return strBuf.toString();
	}

	// n 文字分戻る
	public int readBackN(int length, int index) {
		char c;
		int nc = 0;
		boolean tagFlag = false;
		tagBuf.setLength(0);

		try {
			while (nc < length) {
				index--;
				corpus.setPosition(index * 2);
				c = corpus.getDecodedChar();
				if (c == '>') {
					tagFlag = true;
				} else if (c == '<') {
					tagFlag = false;
					if (tagBuf.toString().compareTo(reversedStopElement_start) == 0
							|| tagBuf.toString().endsWith(
									" " + reversedStopElement_start)) { //$NON-NLS-1$
						return index;
					}
					tagBuf.setLength(0);
				} else if (!tagFlag) {
					nc++;
				} else {
					tagBuf.append(c);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return index;
	}


	public int skip(int n) {
		char c;
		boolean tagFlag = false;
		int nc = 0;
		
		tagBuf.setLength(0);
		try {
			while (nc < n) {
				c = corpus.getDecodedChar();
				if (c == '<') {
					tagFlag = true;
				} else if (c == '>') {
					tagFlag = false;
					if (tagBuf.toString().compareTo(stopElement_end) == 0) {
						return nc;
					}
					tagBuf.setLength(0);
				} else if (!tagFlag) {
					if (c == '\n' || c == '\t' || c == ' ' || c == 0xFEFF) {
						// do nothing
					} else {
						nc++;
					}
				} else {
					tagBuf.append(c);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nc;
	}


	// 現在位置から length 文字読み込む
	public String readStrN(int length) {
		char c;
		boolean tagFlag = false;

		strBuf.setLength(0);
		tagBuf.setLength(0);
		try {
			while (strBuf.length() < length) {
				c = corpus.getDecodedChar();
				if (c == '<') {
					tagFlag = true;
				} else if (c == '>') {
					tagFlag = false;
					if (tagBuf.toString().compareTo(stopElement_end) == 0) {
						return strBuf.toString();
					}
					tagBuf.setLength(0);
				} else if (!tagFlag) {
					if(c == '\n' || c == '\t' || c == ' ' || c == 0xFEFF) {
						// do nothing
					} else {
						strBuf.append(c);
					}
				} else {
					tagBuf.append(c);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strBuf.toString();
	}


	public String readStrNSP(int lengthSP, int index) {
		char c;
		boolean tagFlag = false;

		strBuf.setLength(0);
		tagBuf.setLength(0);
		try {
			corpus.setPosition(index * 2);
			int len = 0;
			while (len < lengthSP) {
				c = corpus.getDecodedChar();
				if (c == '<') {
					tagFlag = true;
				} else if (c == '>') {
					tagFlag = false;
					if (tagBuf.toString().compareTo(stopElement_end) == 0) {
						return strBuf.toString();
					}
					tagBuf.setLength(0);
				} else if (!tagFlag) {
					strBuf.append(c);
					if(Character.isSurrogate(c)) {
						strBuf.append(corpus.getDecodedChar());
					}
					len++;
				} else {
					tagBuf.append(c);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strBuf.toString();
	}


	public String readBackStrNSP(int lengthSP, int index) {
		char c;
		boolean tagFlag = false;

		strBuf.setLength(0);
		tagBuf.setLength(0);

		try {
			int len = 0;
			while (len < lengthSP && index > 0) {
				index--;
				corpus.setPosition(index * 2);
				c = corpus.getDecodedChar();
				if (c == '>') {
					tagFlag = true;
				} else if (c == '<') {
					tagFlag = false;
					if (tagBuf.toString().compareTo(reversedStopElement_start) == 0
							|| tagBuf.toString().endsWith(
									" " + reversedStopElement_start)) { //$NON-NLS-1$
						return strBuf.reverse().toString();
					}
					tagBuf.setLength(0);
				} else if (!tagFlag) {
					if(Character.isSurrogate(c)) {
						index--;
						corpus.setPosition(index * 2);
						strBuf.append(corpus.getDecodedChar());
						strBuf.append(c);
					} else {
						strBuf.append(c);
					}
					len++;
				} else {
					tagBuf.append(c);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("index: " + index); //$NON-NLS-1$
		}
		return strBuf.reverse().toString();

	}
}

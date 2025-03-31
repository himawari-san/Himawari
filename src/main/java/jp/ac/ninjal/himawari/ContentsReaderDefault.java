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

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2003-2005
 * </p>
 * 
 * <p>
 * 会社名:
 * </p>
 * 
 * @author Masaya YAMAGUCHI
 * @version 0.9.4
 */
public class ContentsReaderDefault extends ContentsReader {

	public ContentsReaderDefault(CorpusFile corpus) {
		this.corpus = corpus;
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
		strBuf.setLength(0);
		char c;
		boolean tagFlag = false;

		try {
			while (strBuf.length() < length && index > 0) {
				index--;
				corpus.setPosition(index * 2);
				c = corpus.getDecodedChar();
				if (c == '>') {
					tagFlag = true;
				} else if (c == '<') {
					tagFlag = false;
				} else if (!tagFlag) {
					strBuf.append(c);
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
		strBuf.setLength(0);
		char c;
		boolean tagFlag = false;

		try {
			corpus.setPosition(index * 2);
			while (strBuf.length() < length) {
				c = corpus.getDecodedChar();
				if (c == '<') {
					tagFlag = true;
				} else if (c == '>') {
					tagFlag = false;
				} else if (!tagFlag) {
					strBuf.append(c);
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

		try {
			while (nc < length) {
				index--;
				corpus.setPosition(index * 2);
				c = corpus.getDecodedChar();
				if (c == '>') {
					tagFlag = true;
				} else if (c == '<') {
					tagFlag = false;
				} else if (!tagFlag) {
					nc++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return index;
	}

	// 要素内容を n 文字分スキップする
	public int skip(int n) {
		int nc = 0;
		char c;

		if (nc >= n) {
			return 0;
		}

		while (nc < n && (c = corpus.getDecodedChar()) != '\0') {
			// skip a tag
			if (c == '<') {
				while ((c = corpus.getDecodedChar()) != '\0') {
					if (c == '>') {
						break;
					}
				}
			} // skip a space character
//			else if (c == 0xFEFF || c == '\n') {
				else if (c == '\n' || c == '\t' || c == ' ' || c == 0xFEFF) {
				// do nothing
			} else {
				nc++;
			}
		}
		return nc;
	}

	// length 分文字を抽出する
	// ファイルポインタは，現時点での値をそのまま使う
	public String readStrN(int length) {
		strBuf.setLength(0);
		char c;
		boolean tagFlag = false;

		try {

			while (strBuf.length() < length) {
				c = corpus.getDecodedChar();
				if (c == '<') {
					tagFlag = true;
				} else if (c == '>') {
					tagFlag = false;
				} else if (!tagFlag) {
					if(c == '\n' || c == '\t' || c == ' '|| c == 0xFEFF) {
						// do nothing
					} else {
						strBuf.append(c);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strBuf.toString();
	}

	public String readStrNSP(int lengthSP, int index) {
		strBuf.setLength(0);
		char c;
		boolean tagFlag = false;

		try {
			corpus.setPosition(index * 2);
			int len = 0;
			while (len < lengthSP) {
				c = corpus.getDecodedChar();
				if (c == '<') {
					tagFlag = true;
				} else if (c == '>') {
					tagFlag = false;
				} else if (!tagFlag) {
					strBuf.append(c);
					if(Character.isSurrogate(c)) {
						strBuf.append(corpus.getDecodedChar());
					}
					len++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strBuf.toString();
	}


	public String readBackStrNSP(int lengthSP, int index) {
		strBuf.setLength(0);
		char c;
		boolean tagFlag = false;

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
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("index: " + index); //$NON-NLS-1$
		}
//		System.err.println("s:" + strBuf);
		return strBuf.reverse().toString();
	}
}

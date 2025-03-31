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


public abstract class ContentsReader {
  protected StringBuilder strBuf = new StringBuilder(512);
  protected CorpusFile corpus;

  public ContentsReader() {}

  /**
   * XML ファイルの Contents から n 文字取得
   *
   * @param length 取得する文字数
   * @param index コーパスへのインデックス
   * @return 取得された文字
   *         （index 位置が Contents かどうかのチェックはしていない）
   */
  public abstract String readStrN(int length, int index);

  // Read "length" characters in Character rather than char
  // to handle surrogate pair characters
  public abstract String readStrNSP(int length, int index);


  /**
   * XML ファイルの Contents から逆方向へ n 文字取得
   *
   * @param length 取得する文字数
   * @param index コーパスへのインデックス
   * @return 取得された文字
   *         （index 位置が Contents かどうかのチェックはしていない）
   */
  public abstract String readBackStrN(int length, int index);
  public abstract String readBackStrNSP(int length, int index);

  public abstract int readBackN(int length, int index);
  
  // 現在の位置から，n 文字スキップする
  // 返り値は，スキップした文字数
  public abstract int skip(int n);


  // 現在の位置から，n 文字読み込む
  // 返り値は，読み込んだ文字列
  public abstract String readStrN(int n);


}

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

import java.io.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.*;


/**
 * KanjiCharTransTable.java
 *
 * 字体変換
 *
 * @author Masaya YAMAGUCHI
 * @version 1.0
 */

public class KanjiCharTransTable {

  HashMap<String, String> tableEquiv = new HashMap<String, String>(); // 等価字体用
  HashMap<String, String> tableOther = new HashMap<String, String>(); // 参考字体用
  HashMap<String, String> tableNotUseInTaiyo = new HashMap<String, String>();  // 不使用字体用
  boolean flagItself = true; // 変換前文字も含む？
  boolean flagEquiv = true; // 等価字体へ変換？
  boolean flagOther = false; // 参考字体用へ変換？
  boolean flagNotUseInTaiyo = false; // 不使用字体用へ変換


  /**
   * コンストラクタ
   *
   * @param transTableFilename 字体変換テーブル用ファイル名
   */
  public KanjiCharTransTable(String transTableFilename) {
    initTransTable(transTableFilename);
  }

  public KanjiCharTransTable() {
  }


  public void load(String url) throws Exception {
    DefaultHandler handler = new JitaidicHandler();
    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser parser = factory.newSAXParser();
      parser.parse(url, handler);
    } catch (SAXException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    }
  }



  /**
   * 字体変換
   *
   * @param oldKanji 変換対象の文字列
   */
  public String trans(String oldKanji){
    String result = ""; // 変換結果 //$NON-NLS-1$
    int bracketCounter = 0; // [] 計測用カウンタ
    StringBuilder candidate; // 変換後の字体候補

    for(int i = 0; i < oldKanji.length(); i++){
      String c = oldKanji.substring(i, i+1); // 変換対象の字体

      candidate = new StringBuilder();
      String strEquiv = (String)tableEquiv.get(c);
      String strOther = (String)tableOther.get(c);
      String strNotUseInTaiyo = (String)tableNotUseInTaiyo.get(c);

      // 変換字体そのものを候補に追加
      if(flagItself){
        if(candidate.indexOf(c) == -1){
          candidate.append(c);
        }
      }

      // 等価字体を候補に追加
      if(flagEquiv && strEquiv != null){
        if(candidate.indexOf(strEquiv) == -1){
          for(int j = 0; j < strEquiv.length(); j++){
            if(candidate.indexOf(strEquiv.substring(j, j+1)) == -1){
              candidate.append(strEquiv.substring(j, j+1));
            }
          }
        }
      }

      // 参考字体を候補に追加
      if(flagOther && strOther != null){
        if(candidate.indexOf(strOther) == -1){
          for(int j = 0; j < strOther.length(); j++){
            if(candidate.indexOf(strOther.substring(j, j+1)) == -1){
              candidate.append(strOther.substring(j, j+1));
            }
          }
        }
      }

      // 不使用字体を候補から削除
      if(flagNotUseInTaiyo && strNotUseInTaiyo != null){
        if(candidate.indexOf(strNotUseInTaiyo) != -1){
          for(int j = 0; j < strNotUseInTaiyo.length(); j++){
            if(candidate.indexOf(strNotUseInTaiyo.substring(j, j+1)) != -1){
              candidate.deleteCharAt(candidate.indexOf(strNotUseInTaiyo.substring(j, j+1)));
            }
          }
        }
      }

      // 文字クラス中の文字の判別
      if(oldKanji.charAt(i) == '['){
        bracketCounter++;
      } else if(oldKanji.charAt(i) == ']'){
        bracketCounter--;
      }

      int nCandidate = candidate.length();

      if(nCandidate != 0){
        if(nCandidate > 1){
          if(bracketCounter == 0){
            // 文字クラス中の文字
            result += "[" + candidate.toString() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
          } else {
            result += candidate.toString();
          }
        } else {
          result += candidate.toString();
        }
      } else {
        // 変換候補が一つもない場合は，変換しない
        result += oldKanji.substring(i, i+1);
      }
    }
    return result;
  }


  /**
   * 各種フラグをオプション設定値に基づき，設定
   *
   * @param flagItself 変換字体
   * @param flagEquiv  等価字体
   * @param flagOther  参考字体
   * @param flagNotUseInTaiyo 不使用字体
   */
  public void setFlag(boolean flagItself, boolean flagEquiv, boolean flagOther, boolean flagNotUseInTaiyo){
    this.flagItself = flagItself;
    this.flagEquiv = flagEquiv;
    this.flagOther = flagOther;
    this.flagNotUseInTaiyo = flagNotUseInTaiyo;
  }


  /**
   * 字体変換テーブルの読み込み
   *
   *
   * @param transTableFilename 字体変換テーブル用ファイル名
   *
   *  字体変換テーブル用ファイルのフォーマット
   *  # 字体辞書データ
   *  #　空フィルド記号（ー）を削除
   *  # 指定字体,等価字体,参考字体,不使用字体
   *  亜,亞,,亜
   *  亞,亞,,亜
   *
   */
  public void initTransTable(String transTableFilename){
    BufferedReader in;
    String line;
    boolean readFlag = false;
    int fp = 0; // field pointer
    String c = ""; //$NON-NLS-1$

    try{
      in = new BufferedReader(
          new InputStreamReader(new FileInputStream(transTableFilename), "Shift_JIS")); //$NON-NLS-1$

      while((line = in.readLine()) != null){
        if(!line.startsWith("#")){ // コメント行は読み飛ばす //$NON-NLS-1$
          // 等価字体，参考字体，不使用自体用のハッシュテーブルの初期化
          fp = 0;
          readFlag = false;
          StringBuilder b1 = new StringBuilder();
          StringBuilder b2 = new StringBuilder();
          StringBuilder b3 = new StringBuilder();

          for(int i = 0; i < line.length(); i++){
            if(line.charAt(i) == ','){
              fp++;
            } else if(fp == 0){
              if(readFlag){
                System.out.println("Error(KanjiCharTransTable): invalid field\n"); //$NON-NLS-1$
              }
              c = line.substring(i, i+1);
              readFlag = true;
            } else if(fp == 1){
              b1.append(line.substring(i, i+1));
            } else if(fp == 2){
              b2.append(line.substring(i, i+1));
            } else if(fp == 3){
              b3.append(line.substring(i, i+1));
            } else {
              System.out.println("Error(KanjiCharTransTable): invalid field\n"); //$NON-NLS-1$
            }
          }
          if(b1.length() != 0){ tableEquiv.put(c, b1.toString()); }
          if(b2.length() != 0){ tableOther.put(c, b2.toString()); }
          if(b3.length() != 0){ tableNotUseInTaiyo.put(c, b3.toString()); }
        }
      }
      in.close();
    } catch(IOException e){
      System.out.println("Error(KanjiCharTransTable): " + e); //$NON-NLS-1$
    }
  }

  class JitaidicHandler extends DefaultHandler {
    private String elementName = ""; //$NON-NLS-1$
    private String targetChar = ""; //$NON-NLS-1$
    private String elementTarget = Messages.getString("KanjiCharTransTable.11"); //$NON-NLS-1$
    private String argEqiv = Messages.getString("KanjiCharTransTable.12"); //$NON-NLS-1$
    private String argOther = Messages.getString("KanjiCharTransTable.13"); //$NON-NLS-1$
    private String argNotUseInTaiyo = Messages.getString("KanjiCharTransTable.14"); //$NON-NLS-1$
    private String valueEquiv = ""; //$NON-NLS-1$
    private String valueOther = ""; //$NON-NLS-1$
    private String valueNotUseInTaiyo = ""; //$NON-NLS-1$


    public JitaidicHandler(){
    }

    public void startDocument() {
    }
    public void endDocument() {
    }

    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes atts) {

      if(qName.compareTo(elementTarget) == 0){
        valueEquiv = atts.getValue(argEqiv);
        valueOther = atts.getValue(argOther);
        valueNotUseInTaiyo = atts.getValue(argNotUseInTaiyo);
        elementName = qName;
      }
    }

    public void endElement(String namespaceURI,
                           String localName,
                           String qName) {
      if(qName.compareTo(elementTarget) == 0){
        tableEquiv.put(targetChar, valueEquiv);
        tableOther.put(targetChar, valueOther);
        tableNotUseInTaiyo.put(targetChar, valueNotUseInTaiyo);

      }
    }

    public void characters(char[] ch, int start, int length) {
      if(elementName.compareTo(elementTarget) == 0){
        targetChar = new String(ch, start, length);
      }
    }
  }
}

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
import java.util.regex.*;


/**
 * <p>タイトル: </p>
 * <p>説明: </p>
 * <p>著作権: Copyright (c) 2003</p>
 * <p>会社名: </p>
 * @author Masaya YAMAGUCHI
 * @version 0.9.4
 */

public class RecordBasedArgumentIndex extends XmlArgumentIndex {
  public RecordBasedArgumentIndex(String elementName, String middleName, String argumentName, boolean isCompleteMatch, CorpusFile corpus) {
    super(elementName, middleName, argumentName, isCompleteMatch, corpus);
  }

  private Pattern rexPattern;
  private int keyLength;
  private String keyHead, keyTail;


  /**
   * Sets search conditions. Arguments are substituted to the global variables.
   *
   * @param fieldInfo FieldInfo   field information of the result record
   * @param fieldName String      the name of field where the target string is stored
   */

  public void setRetrieveCondition(FieldInfo fieldInfo, String fieldName,
                                   String keyHead, String keyTail){
    this.fieldInfo = fieldInfo;
    this.fieldName = fieldName;
    this.keyHead = keyHead;
    this.keyTail = keyTail;
  }



  public ResultRecord retrieveFirst(String key) {
    int curCixPos;
    int suffixNumber;
    String txtLine = null;
    int resCmp = 0;
    int start = 0;
    int end = 0;
    ResultRecord resultRecord = null;
    String readStr = null;
    String content = null;
    Matcher rexMatcher;

    this.key = key;
    keyLength = key.length();
    recCixPos = 0;
    if(isCompleteMatch){
    	keyHead = "^" + keyHead; // recordBased completematch の場合は，keyHead は無視するようにしたほうがよい //$NON-NLS-1$
    	keyTail += "$"; //$NON-NLS-1$
    }
    // Use CorpusFile.KEY_HEAD_MARKER to avoid multiple matching for one argument
    // (ex. key: "meishi", keyHead:"^", target argument: "meishi-futu_meishi") 
    rexPattern = Pattern.compile(keyHead + "(" + CorpusFile.KEY_HEAD_MARKER +"\\Q" + key + "\\E)" + keyTail); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    searchDirection = SEARCH_FORWARD;

    try {
      end = (int) raCix.length() / 4;
    }
    catch (IOException e) {
      e.printStackTrace();
    }

    while (start <= end) {
      curCixPos = (start + end) / 2;

      try {
        /* ary からテキスト位置の読込み */
        cixBuf.position(curCixPos);
        suffixNumber = cixBuf.get();

        /* テキストの読込み */
        txtLine = corpus.readArgumentN(keyLength, suffixNumber);
      }
      catch (Exception e) {
        e.printStackTrace();
        return null;
      }

      /* テキストとの比較 */
      resCmp = txtLine.compareTo(key);
      if (resCmp == 0) {
        recCixPos = curCixPos;
        try {
          // suffay array 前方方向へ検索
          cixBuf.position(curCixPos);
          while (true) {
            /* ary からテキスト位置の読込み */
            suffixNumber = cixBuf.get();
            /* テキストの読込み */
            txtLine = corpus.readArgumentN(keyLength, suffixNumber);
            if (txtLine.compareTo(key) != 0) {
              break;
            }

            // キー末尾比較
            readStr = corpus.readArgumentAll(suffixNumber);
            rexMatcher = rexPattern.matcher(readStr);
            if (!rexMatcher.find()) {
              continue;
            }

            // 残りのタグを読み飛ばす
            suffixNumber = corpus.passByTag(suffixNumber);

            // content を取得
            content = corpus.getContent(elementName, suffixNumber);

            resultRecord = new ResultRecord(fieldInfo);
            resultRecord.setPosition(suffixNumber);
            resultRecord.set(fieldName, content);
            return resultRecord;
          }
        }
        catch (Exception e) {
          e.printStackTrace();
          return null;
        }

        searchDirection = SEARCH_BACKWORD;

        try {
          // suffay array 後方方向へ検索
          while (true) {
            recCixPos--;
            cixBuf.position(recCixPos);
            /* ary からテキスト位置の読込み */
            suffixNumber = cixBuf.get();
            /* テキストの読込み */
            txtLine = corpus.readArgumentN(keyLength, suffixNumber);
            if (txtLine.compareTo(key) != 0) {
              return null;
            }

            // キー末尾比較
            readStr = corpus.readArgumentAll(suffixNumber);
            rexMatcher = rexPattern.matcher(readStr);
            if (!rexMatcher.find()) {
              continue;
            }

            // 残りのタグを読み飛ばす
            suffixNumber = corpus.passByTag(suffixNumber);

            // content を取得
            content = corpus.getContent(elementName, suffixNumber);

            resultRecord = new ResultRecord(fieldInfo);
            resultRecord.setPosition(suffixNumber);
            resultRecord.set(fieldName, content);
            return resultRecord;
          }
        }
        catch (Exception e) {
          e.printStackTrace();
          return null;
        }
      }
      else if (resCmp > 0) {
        end = curCixPos - 1;
      }
      else {
        start = curCixPos + 1;
      }
    }
    return null;
  }



  public ResultRecord retrieveNext() {
    int suffixNumber;
    String txtLine = null;
    ResultRecord resultRecord = null;
    String readStr = null;
    String content = null;
    Matcher rexMatcher;

    if(searchDirection == SEARCH_FORWARD){
      while (true) {
        /* ary からテキスト位置の読込み */
        suffixNumber = cixBuf.get();
        /* テキストの読込み */
        txtLine = corpus.readArgumentN(keyLength, suffixNumber);
        if (txtLine.compareTo(key) != 0) {
          break;
        }

        // キー末尾比較
        readStr = corpus.readArgumentAll(suffixNumber);
        rexMatcher = rexPattern.matcher(readStr);
        if (!rexMatcher.find()) {
          continue;
        }

        // 残りのタグを読み飛ばす
        suffixNumber = corpus.passByTag(suffixNumber);

        // content を取得
        content = corpus.getContent(elementName, suffixNumber);

        resultRecord = new ResultRecord(fieldInfo);
        resultRecord.setPosition(suffixNumber);
        resultRecord.set(fieldName, content);
        return resultRecord;
      }
    }

    searchDirection = SEARCH_BACKWORD;

    try {
      // suffay array 後方方向へ検索
      while (true) {
        recCixPos--;
        cixBuf.position(recCixPos);
        /* ary からテキスト位置の読込み */
        suffixNumber = cixBuf.get();
        /* テキストの読込み */
        txtLine = corpus.readArgumentN(keyLength, suffixNumber);
        if (txtLine.compareTo(key) != 0) {
          return null;
        }

        // キー末尾比較
        readStr = corpus.readArgumentAll(suffixNumber);
        rexMatcher = rexPattern.matcher(readStr);
        if (!rexMatcher.find()) {
          continue;
        }

        // 残りのタグを読み飛ばす
        suffixNumber = corpus.passByTag(suffixNumber);

        // content を取得
        content = corpus.getContent(elementName, suffixNumber);

        resultRecord = new ResultRecord(fieldInfo);
        resultRecord.setPosition(suffixNumber);
        resultRecord.set(fieldName, content);
        return resultRecord;
      }
    }
    catch (Exception e) {
//      e.printStackTrace();
      return null;
    }
  }
}


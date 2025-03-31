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
 * @(#)RecordBasedContentsIndex.java	2005-06-05
 *
 * Copyright 2003-2005
 * National Institute for Japanese Language All rights reserved.
 *
 */

package jp.ac.ninjal.himawari;

import java.io.*;
import java.util.regex.*;

/**
 *
 * <p>タイトル: </p>
 * <p>説明: </p>
 * <p>著作権: Copyright (c) 2003</p>
 * <p>会社名: </p>
 * @author Masaya YAMAGUCHI
 * @version 0.9.4
 */

public class RecordBasedContentsIndex extends ContentsSuffixArray {
  private Pattern rexPattern;
  private int keyLength;
  private String keyHead, keyTail;

  public RecordBasedContentsIndex(String elementName, String middleName, CorpusFile corpus) {
    super(elementName, middleName, corpus);
  }

  public RecordBasedContentsIndex(String elementName, String middleName,
                                  CorpusFile corpus, String stopElementName) {
    super(elementName, middleName, corpus, stopElementName);
  }


  /**
   * Sets search conditions. Arguments are substituted to the global variables.
   *
   * @param fieldInfo FieldInfo   field information of the result record
   * @param fieldName String      the name of field where the target string is stored
   *
   */
  public void setRetrieveCondition(FieldInfo fieldInfo, String fieldName){
    this.fieldInfo = fieldInfo;
    this.fieldName = fieldName;
    this.keyHead = ""; //$NON-NLS-1$
    this.keyTail = ""; //$NON-NLS-1$
  }

  /**
   * Sets search conditions. Arguments are substituted to the global variables.
   *
   * @param fieldInfo FieldInfo   field information of the result record
   * @param fieldName String      the name of field where the target string is stored
   * @param keyHead               the head of key
   * @param keyTail               the tail of key
   *
   */
  public void setRetrieveCondition(FieldInfo fieldInfo, String fieldName,
                                   String keyHead, String keyTail){
    this.fieldInfo = fieldInfo;
    this.fieldName = fieldName;
    this.keyHead = keyHead;
    this.keyTail = keyTail;
  }





  // 正規表現検索
  public ResultRecord retrieveFirst(String key){
    int curCixPos;
    int suffixNumber;
    int resCmp = 0;
    int start = 0;
    int end = 0;
    int tmpSuffixNumber;
    ResultRecord resultRecord = null;
    String readStr = null;
    Matcher rexMatcher;

    keyLength = key.length();
    recCixPos = 0;
    rexPattern = Pattern.compile(keyHead + "(\\Q" + key + "\\E)" + keyTail); //$NON-NLS-1$ //$NON-NLS-2$
    target = key;
    searchDirection = SEARCH_FORWARD;

    try{
      end = (int)raCix.length() / 4;
    } catch(IOException e){
      e.printStackTrace();
    }


    while(start <= end){
      curCixPos = (start + end) / 2;

      try{
        /* ary からテキスト位置の読込み */
        cixBuf.position(curCixPos);
        suffixNumber = cixBuf.get();

        /* テキストの読込み */
        readStr = corpus.readStrN(keyLength, suffixNumber);
      } catch(Exception e){
        e.printStackTrace();
      }

      /* テキストとの比較 */
      resCmp = fstrcmp(readStr, key);
      if(resCmp == 0){
        recCixPos = curCixPos;
        // suffay array 前方方向へ検索
        cixBuf.position(curCixPos);
        while(true){
          /* ary からテキスト位置の読込み */
          suffixNumber = cixBuf.get();
          /* テキストの読込み */
          readStr = corpus.readStrN(keyLength, suffixNumber);

          // キー本体比較
          if(fstrcmp(readStr, key) != 0){
            break;
          }
          // キー末尾比較
          readStr = corpus.getContent2(elementName, suffixNumber);
          rexMatcher = rexPattern.matcher(readStr);
          if(!rexMatcher.find()){
            continue;
          }
//          recCixPos = curCixPos - 1;
          tmpSuffixNumber = corpus.getPosition()/2;
          resultRecord = new ResultRecord(fieldInfo);
          resultRecord.setPosition(tmpSuffixNumber);
          resultRecord.set(fieldName, readStr);
          return resultRecord;
        }

        searchDirection = SEARCH_BACKWORD;
        try{
          // suffay array 後方方向へ検索
          while(true){
            recCixPos--;
            cixBuf.position(recCixPos);
            /* ary からテキスト位置の読込み */
            suffixNumber = cixBuf.get();
            /* テキストの読込み */
            readStr = corpus.readStrN(keyLength, suffixNumber);
            if(fstrcmp(readStr, key) != 0){
              return null;
            }
            // キー末尾比較
            readStr = corpus.getContent2(elementName, suffixNumber);
            rexMatcher = rexPattern.matcher(readStr);
            if(!rexMatcher.find()){
              continue;
            }
            tmpSuffixNumber = corpus.getPosition()/2;
            resultRecord = new ResultRecord(fieldInfo);
            resultRecord.setPosition(tmpSuffixNumber);
            resultRecord.set(fieldName, readStr);
            return resultRecord;
          }
        } catch(Exception e){}
      } else if(resCmp > 0) {
        end = curCixPos - 1;
      } else {
        start = curCixPos + 1;
      }
    }
    return resultRecord;
  }


  // 正規表現検索
  public ResultRecord retrieveNext(){
    int suffixNumber;
    int tmpSuffixNumber;
    ResultRecord resultRecord = null;
    String readStr = null;
    Matcher rexMatcher;


    if(searchDirection == SEARCH_FORWARD){
      // suffay array 前方方向へ検索
//      cixBuf.position(recCixPos);
      while(true){
        /* ary からテキスト位置の読込み */
        suffixNumber = cixBuf.get();
        /* テキストの読込み */
        readStr = corpus.readStrN(keyLength, suffixNumber);

        // キー本体比較
        if(fstrcmp(readStr, target) != 0){
          searchDirection = SEARCH_BACKWORD;
          break;
        }
        // キー末尾比較
        readStr = corpus.getContent2(elementName, suffixNumber);
        rexMatcher = rexPattern.matcher(readStr);
        if(!rexMatcher.find()){
          continue;
        }
        tmpSuffixNumber = corpus.getPosition()/2;
        resultRecord = new ResultRecord(fieldInfo);
        resultRecord.setPosition(tmpSuffixNumber);
        resultRecord.set(fieldName, readStr);
        return resultRecord;
      }
    }

    // suffay array 後方方向へ検索
    while(true){
      recCixPos--;
      cixBuf.position(recCixPos);
      /* ary からテキスト位置の読込み */
      suffixNumber = cixBuf.get();
      /* テキストの読込み */
      readStr = corpus.readStrN(keyLength, suffixNumber);
      if(fstrcmp(readStr, target) != 0){
        return null;
      }
      // キー末尾比較
      readStr = corpus.getContent2(elementName, suffixNumber);
      rexMatcher = rexPattern.matcher(readStr);
      if(!rexMatcher.find()){
         continue;
      }
      tmpSuffixNumber = corpus.getPosition()/2;
      resultRecord = new ResultRecord(fieldInfo);
      resultRecord.setPosition(tmpSuffixNumber);
      resultRecord.set(fieldName, readStr);
      return resultRecord;
    }
  }
}

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
 * @(#)KWICMaker.java   1.1 2005-08-20
 *
 * Copyright 2005
 * National Institute for Japanese Language All rights reserved.
 *
 */
package jp.ac.ninjal.himawari;

import java.util.regex.*;


/**
 * The KWICMaker class constructs KWIC for the key word stored in the input
 * <code>ResultRecord</code>. The result is constrained by the specified
 * condition for the previous and/or following context described in regular
 * expression. In addition, the string matched with the regular expression
 * can be concatenated to the key optionally.
 *
 * @author Masaya YAMAGUCHI
 * @version 1.0
 * @since 1.3
*/

public class KWICMaker {
  private CorpusFile corpus;
  private String primaryFieldName;
  private Pattern rexPatternHead;
  private Pattern rexPatternTail;
  private boolean isNotHead;
  private boolean isNotTail;
  private int lengthOfContext;
  private int searchRange;
  private boolean isAddPrev;
  private boolean isAddFol;

  public KWICMaker(Filter filter,
                   int lengthOfContext, int searchRange,
                   boolean isAddPrev, boolean isAddFol,
                   String primaryFieldName){

    this.lengthOfContext = lengthOfContext;
    this.primaryFieldName = primaryFieldName;

    try{
      this.rexPatternHead = (Pattern) filter.get(ResultRecord.
                                                 PRECEDING_CONTEXT_FIELD);
      this.rexPatternTail = (Pattern) filter.get(ResultRecord.
                                                 FOLLOWING_CONTEXT_FIELD);
    }catch(Exception e){
      e.printStackTrace();
    }
    this.isNotHead = filter.isNot(ResultRecord.PRECEDING_CONTEXT_FIELD);
    this.isNotTail = filter.isNot(ResultRecord.FOLLOWING_CONTEXT_FIELD);
    this.searchRange = searchRange;
    this.isAddPrev = isAddPrev;
    this.isAddFol = isAddFol;
  }


  public void setCorpus(CorpusFile corpus){
    this.corpus = corpus;
  }


  /**
   * Makes the KWIC for the ResultRecord
   *
   * @param resultRecord     a result record as the input of this method
   * @return ResultRecord     the result record as the output of this method
   */
  public ResultRecord makeUnderConstraint(ResultRecord resultRecord) {

    String prevContext, folContext;
    String searchedPrevContext, searchedFolContext;
    Matcher rexMatcherHead, rexMatcherTail;
    int txtPos = resultRecord.getPosition();

    String currentKey = (String)resultRecord.get(primaryFieldName);

    int keyLength = currentKey.length();
    int keyLengthSP = currentKey.codePointCount(0, keyLength);

    // Retrieve the previous context, and then store it to the ResultRecord
    if (rexPatternHead == null) {
      resultRecord.set(ResultRecord.PRECEDING_CONTEXT_FIELD,
                       corpus.readBackStrNSP(lengthOfContext, txtPos));
    } else { // if constrants exist
      if(lengthOfContext == searchRange){
        prevContext = corpus.readBackStrNSP(lengthOfContext, txtPos);
        searchedPrevContext = prevContext;
      } else if(lengthOfContext > searchRange){
        prevContext = corpus.readBackStrNSP(lengthOfContext, txtPos);
        int lenPrevContext = prevContext.length();
        int lenPrevContextSP = prevContext.codePointCount(0, lenPrevContext);
        if(lenPrevContextSP > searchRange){
          searchedPrevContext = getTailStringSP(prevContext, lenPrevContextSP - searchRange);
        } else {
          searchedPrevContext = prevContext;
        }
      } else {
        searchedPrevContext = corpus.readBackStrNSP(searchRange, txtPos);
        int lenSearchedPrevContext = searchedPrevContext.length();
        int lenSearchedPrevContextSP = searchedPrevContext.codePointCount(0, lenSearchedPrevContext);
        if(lenSearchedPrevContextSP > lengthOfContext){
          prevContext = getTailStringSP(searchedPrevContext, lenSearchedPrevContextSP - lengthOfContext);
        } else {
          prevContext = searchedPrevContext;
        }
      }

      rexMatcherHead = rexPatternHead.matcher(searchedPrevContext);

      if(rexMatcherHead.find()) {
        if (isNotHead) {
          return null;
        }
        else if (isAddPrev) {
          currentKey = searchedPrevContext.substring(rexMatcherHead.start()) + currentKey;
          txtPos = corpus.readBackN(searchedPrevContext.length() -rexMatcherHead.start(), txtPos);
          keyLength = currentKey.length();
          keyLengthSP = currentKey.codePointCount(0,  keyLength);
          resultRecord.set(primaryFieldName, currentKey);
          resultRecord.setPosition(txtPos);
          resultRecord.set(ResultRecord.PRECEDING_CONTEXT_FIELD,
                           corpus.readBackStrNSP(lengthOfContext, txtPos));
        }
        else {
          resultRecord.set(ResultRecord.PRECEDING_CONTEXT_FIELD, prevContext);
        }
      }
      else {
        if (isNotHead) {
          resultRecord.set(ResultRecord.PRECEDING_CONTEXT_FIELD, prevContext);
        }
        else {
          return null;
        }
      }
    }


    // Retrieve the following context, and then store it to the ResultRecord
    if (rexPatternTail == null) {
      resultRecord.set(ResultRecord.FOLLOWING_CONTEXT_FIELD,
              corpus.readStrNSP(lengthOfContext+keyLengthSP, txtPos).substring(keyLength));


    } else {
      if(lengthOfContext == searchRange){
        folContext = corpus.readStrNSP(lengthOfContext+keyLengthSP, txtPos).substring(keyLength);
        searchedFolContext = folContext;
      } else if(lengthOfContext > searchRange){
        folContext = corpus.readStrNSP(lengthOfContext+keyLengthSP, txtPos).substring(keyLength);
        int lenFolContext = folContext.length();
        int lenFolContextSP = folContext.codePointCount(0, lenFolContext);
        if(lenFolContextSP > searchRange){
          searchedFolContext = getHeadStringSP(folContext, searchRange);
        } else {
          searchedFolContext = folContext;
        }
      } else {
        searchedFolContext = corpus.readStrNSP(searchRange+keyLengthSP, txtPos).substring(keyLength);
        int lenSearchedFolContext = searchedFolContext.length();
        int lenSearchedFolContextSP = searchedFolContext.codePointCount(0,  lenSearchedFolContext);
        if(lenSearchedFolContextSP > lengthOfContext){
          folContext = getHeadStringSP(searchedFolContext, lengthOfContext);
        } else {
          folContext = searchedFolContext;
        }
      }

      rexMatcherTail = rexPatternTail.matcher(searchedFolContext);

      if (rexMatcherTail.find()) {
        if (isNotTail) {
          return null;
        }
        else if (isAddFol) {
          currentKey = currentKey + searchedFolContext.substring(0, rexMatcherTail.end());
          keyLength = currentKey.length();
          keyLengthSP = currentKey.codePointCount(0, keyLength);
          resultRecord.set(primaryFieldName, currentKey);
          resultRecord.set(ResultRecord.FOLLOWING_CONTEXT_FIELD,
                           corpus.readStrNSP(keyLengthSP+lengthOfContext, txtPos).substring(keyLength));
        }
        else {
          resultRecord.set(ResultRecord.FOLLOWING_CONTEXT_FIELD, folContext);
        }
      }
      else {
        if (isNotTail) {
          resultRecord.set(ResultRecord.FOLLOWING_CONTEXT_FIELD, folContext);
        }
        else {
          return null;
        }
      }
    }
    return resultRecord;
  }

  
  // str.substring(0, n); 
  private String getHeadStringSP(String str, int n) {
	  
	  int strlen = str.length();
	  int c = 0;
	  
	  for(int i = 0; i < strlen; i++) {
		  if(Character.isSurrogate(str.charAt(i))) {
			  c++;
		  } else {
			  c += 2;
		  }
		  
		  if(c == n*2) {
			  return str.substring(0, i+1);
		  }
	  }
	  
	  return str;
  }

  
  // str.substring(n); 
  private String getTailStringSP(String str, int n) {
	  
	  int strlen = str.length();
	  int c = 0;
	  
	  for(int i = 0; i < strlen; i++) {
		  if(Character.isSurrogate(str.charAt(i))) {
			  c++;
		  } else {
			  c += 2;
		  }
		  
		  if(c == n*2) {
			  return str.substring(i+1);
		  }
	  }
	  
	  return "";
  }
}

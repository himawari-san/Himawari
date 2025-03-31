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
 * @(#)PrevStringComparator.java	0.9.7 2003-01-20
 *
 * Copyright 2003
 * National Institute for Japanese Language All rights reserved.
 */

package jp.ac.ninjal.himawari;

import java.util.Comparator;

/**
 * ResultRecord 用文字列比較子
 *   文字列末尾から比較する(前文脈専用)
 *
 * @author Masaya YAMAGUCHI
 * @version 0.9.7
 */
public class PrevStringComparator implements Comparator<ResultRecord> {
  private String strPcontextFieldName = Messages.getString("PrevStringComparator.0"); //$NON-NLS-1$
  private int orderDecider;


  /**
   * PrevStringComparator の生成
   *   フィールド名は「前文脈」を使用
   *
   * @param isAscendingOrder 昇順のとき true
   */
  public PrevStringComparator(boolean isAscendingOrder) {
    if(isAscendingOrder){
      orderDecider = 1;
    } else {
      orderDecider = -1;
    }
  }


  /**
   * PrevStringComparator の生成
   *
   * @param strPcontextFieldName 比較対象のフィールド
   * @param isAscendingOrder 昇順のとき true
   */
  public PrevStringComparator(String strPcontextFieldName, boolean isAscendingOrder) {
    this.strPcontextFieldName = strPcontextFieldName;
    if(isAscendingOrder){
      orderDecider = 1;
    } else {
      orderDecider = -1;
    }
  }


  public int compare(ResultRecord rr1, ResultRecord rr2) {
    String str1, str2;
    char ch1, ch2;

    str1 = (String)rr1.get(strPcontextFieldName);
    str2 = (String)rr2.get(strPcontextFieldName);
    for(int i = 0; i < str1.length() && i < str2.length(); i++){
      ch1 = str1.charAt(str1.length()-i-1);
      ch2 = str2.charAt(str2.length()-i-1);

      if(ch1 != ch2){
        return (ch1 - ch2) * orderDecider;
      }
    }
    if(str1.length() > str2.length()){
      return orderDecider;
    } else if(str1.length() < str2.length()){
      return -orderDecider;
    } else {
      return 0;
    }
  }


  public boolean equals(Object obj) {
    return this.equals(obj);
  }
}
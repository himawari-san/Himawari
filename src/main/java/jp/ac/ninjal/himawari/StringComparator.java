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
 * @(#)StringComparator.java	0.9.7 2003-01-20
 *
 * Copyright 2003
 * National Institute for Japanese Language All rights reserved.
 */

package jp.ac.ninjal.himawari;

import java.util.Comparator;

/**
 * ResultRecord 用文字列比較子
 *
 * @author Masaya YAMAGUCHI
 * @version 0.9.7
 */
public class StringComparator implements Comparator<ResultRecord> {
  String fieldname;
  int orderDecider; // 昇順のとき 1，降順のとき -1

  /**
   * StringComparator の生成
   *
   * @param fieldname 比較対象のフィールド
   * @param isAscendingOrder 昇順のとき true
   */
  public StringComparator(String fieldname, boolean isAscendingOrder) {
    this.fieldname = fieldname;
    if(isAscendingOrder){
      orderDecider = 1;
    } else {
      orderDecider = -1;
    }
  }


  public int compare(ResultRecord rr1, ResultRecord rr2) {
    char ch1, ch2;
    String str1, str2;

    str1 = (String)rr1.get(fieldname);
    str2 = (String)rr2.get(fieldname);

    if(str1 == null){
      str1 = ""; //$NON-NLS-1$
    }
    if(str2 == null){
      str2 = ""; //$NON-NLS-1$
    }
//    if(str1 == null){
//        return -orderDecider;
//      } else if(str2 == null){
//        return orderDecider;
//      }

    
    
    
    for(int i = 0; i < str1.length() && i < str2.length(); i++){
      ch1 = str1.charAt(i);
      ch2 = str2.charAt(i);

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
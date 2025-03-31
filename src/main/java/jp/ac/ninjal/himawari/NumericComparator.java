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
 * @(#)NumericComparator.java	0.9.8 2003-01-28
 *
 * Copyright 2003
 * National Institute for Japanese Language All rights reserved.
 */

package jp.ac.ninjal.himawari;

import java.util.Comparator;

/**
 * ResultRecord 用数値比較子
 *
 * @author Masaya YAMAGUCHI
 * @version 0.9.7
 */
public class NumericComparator implements Comparator<ResultRecord> {
  private int orderDecider;
  private String fieldname;


  /**
   * NumericComparator の生成
   *
   * @param isAscendingOrder 昇順のとき true
   */
  public NumericComparator(String fieldname, boolean isAscendingOrder) {
    this.fieldname = fieldname;
    if(isAscendingOrder){
      orderDecider = 1;
    } else {
      orderDecider = -1;
    }
  }


	public int compare(ResultRecord rr1, ResultRecord rr2) {
		Double n1, n2;

		try {
			Object a1 = rr1.get(fieldname);
			if(a1 instanceof Integer){
				n1 = ((Integer)a1).doubleValue();
			} else {
				n1 = Double.parseDouble((String) a1);
			}
		} catch (ClassCastException ex) {
			n1 = Double.NaN;
		}

		try {
			Object a2 = rr2.get(fieldname);
			if(a2 instanceof Integer){
				n2 = ((Integer)a2).doubleValue();
			} else {
				n2 = Double.parseDouble((String) a2);
			}
		} catch (ClassCastException ex) {
			n2 = Double.NaN;
		}

		if (n1 == Double.NaN && n2 == Double.NaN) {
			return 0;
		} else if (n1 == Double.NaN) {
			return -orderDecider;
		} else if (n2 == Double.NaN) {
			return orderDecider;
		} else {
			return n1.compareTo(n2) * orderDecider;
		}
	}

  public boolean equals(Object obj) {
    return this.equals(obj);
  }
}

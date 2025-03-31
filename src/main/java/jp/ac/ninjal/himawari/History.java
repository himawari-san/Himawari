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
 * @(#)History.java	0.9.4 2003-10-20
 *
 * Copyright 2003
 * National Institute for Japanese Language All rights reserved.
 *
 */
package jp.ac.ninjal.himawari;

import java.util.*;

/**
 * キー入力の履歴を管理する
 * 
 * @author Masaya YAMAGUCHI
 * @version 0.9.4
 */
public class History extends LinkedList<String> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int max = 20; // 履歴の最大数
	int index = 0; // 履歴のインデックス
	boolean flagEnd = true; // show the last item initially 

	public History() {
	}

	/**
	 * 履歴へ<code>value</code>を追加する
	 * 
	 * @param value
	 *            追加する文字列
	 * @return 履歴数
	 */
	public int register(String value) {
		if (value.isEmpty()) {
			return size();
		}
		if(contains(value)){
			remove(value);
		}
		if (size() >= max) {
			remove();
		}
		add(value);
		index = size() - 1;
		flagEnd = true;
		
		System.err.println("history added: " + value); //$NON-NLS-1$
		return size();
	}

	/**
	 * 履歴を進める
	 * 
	 * @return 履歴
	 */
	public String forward() {
		if(size() == 0){
			return ""; //$NON-NLS-1$
		} else if(index < size()-1){
			index++;
		}
		
		return get(index);
	}

	/**
	 * 履歴を戻す
	 * 
	 * @return 履歴
	 */
	public String backward() {
		if(size() == 0){
			return ""; //$NON-NLS-1$
		} else if (index > 0) {
			if(flagEnd){
				flagEnd = false;
			} else {
				index--;
			}
		}
		return get(index);
	}
}

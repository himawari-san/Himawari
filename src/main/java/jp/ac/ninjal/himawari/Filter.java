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

import java.util.HashMap;
import java.util.regex.*;

/**
 * <p>
 * タイトル:
 * </p>
 * <p>
 * 説明:
 * </p>
 * <p>
 * 著作権: Copyright (c) 2003
 * </p>
 * <p>
 * 会社名:
 * </p>
 * 
 * @author Masaya YAMAGUCHI
 * @version 0.9.4
 */

public class Filter extends HashMap<String, Pattern> {
	private static final long serialVersionUID = 1L;
	HashMap<String, Boolean> not = new HashMap<String, Boolean>();

	public Filter() {
		super();
	}

	public void put(String key, Pattern value, boolean isNot) {
		super.put(key, value);
		not.put(key, isNot);
	}

	public Pattern get(String key) {
		Pattern value = super.get(key);
		
		if (value == null) {
			return null;
		} else {
			return value;
		}
	}

	public boolean isNot(Object key) {
		if (not.containsKey(key)) {
			return ((Boolean) not.get(key)).booleanValue();
		} else {
			return false;
		}
	}

	
	public boolean isFiltered(Object key, String value, boolean isNot) {
		Pattern filterValue = get(key);
		if (filterValue == null) {
			return false;
		} else if (filterValue.matcher(value).find()) {
			if (isNot) {
				return false;
			} else {
				return true;
			}
		} else {
			if (isNot) {
				return true;
			} else {
				return false;
			}
		}
	}
}

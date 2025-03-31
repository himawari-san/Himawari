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

import java.nio.*;
import java.util.*;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

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

abstract class Index {
	protected String processedElement = null;
	protected Filter filter = null;
	
	public Index() {
	}

	public void qsortBySuffix(IntBuffer ibuffer, int left, int right,	Comparator<Object> c) {
		int last;

		if (left >= right) {
			return;
		}
		swap(ibuffer, left, (left + right) / 2);
		last = left;
		for (int i = left + 1; i <= right; i++) {
			if (c.compare(ibuffer.get(i), ibuffer.get(left)) < 0) {
				swap(ibuffer, ++last, i);
			}
		}
		swap(ibuffer, left, last);
		qsortBySuffix(ibuffer, left, last - 1, c);
		qsortBySuffix(ibuffer, last + 1, right, c);
	}

	private void swap(IntBuffer ibuffer, int i, int j) {
		int temp;

		temp = ibuffer.get(i);
		ibuffer.put(i, ibuffer.get(j));
		ibuffer.put(j, temp);
	}

	// 文字列比較
	// 返り値：
	// 0 : txt =~ /^target/
	// txt - target: 上記以外
	public int fstrcmp(String txt, String target) {
		int targetLen = target.length();

		if (txt.length() < targetLen) {
			return -1;
		}

		for (int i = 0; i < targetLen; i++) {
			if (txt.charAt(i) != target.charAt(i)) {
				return (int) (txt.charAt(i) - target.charAt(i));
			}
		}
		return 0;
	}


	public String getProcessedElementName() {
		return processedElement;
	}

	public void setFilter(Filter filter){
		this.filter = filter;
	}
	
	
	abstract void open() throws IOException;

	abstract void close() throws IOException;

	abstract ResultRecord retrieveFirst(String target);

	abstract ResultRecord retrieveNext();

	abstract String getIOFilename();
	
	public void deleteIndex() throws IOException{
		String indexFilename = getIOFilename();
		if(indexFilename == null || indexFilename.isEmpty()){
			return;
		}
		close();
		FileUtils.forceDelete(new File(getIOFilename()));
		System.err.println("Message(Index): delete the index file, " + getIOFilename()); //$NON-NLS-1$
	}

}

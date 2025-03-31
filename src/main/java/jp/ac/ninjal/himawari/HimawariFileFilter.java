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

/**
 * <p>タイトル: 太陽検索システム</p>
 * <p>説明: </p>
 * <p>著作権: Copyright (c) 2001</p>
 * <p>会社名: 独立行政法人国立国語研究所</p>
 * @author Masaya YAMAGUCHI
 * @version 0.22
 */

public class HimawariFileFilter extends javax.swing.filechooser.FileFilter {
	String fileExtension;
	String explanation;

	public HimawariFileFilter(String fileExtension, String explanation) {
		this.fileExtension = fileExtension;
		this.explanation = explanation;
	}

	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}

		String extension = getExtension(f);
		if (extension != null) {
			if (extension.equals(fileExtension)) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	private String getExtension(File f) {
		String filename = f.getName();
		int p = filename.lastIndexOf("."); //$NON-NLS-1$

		if (p != -1 && filename.length() > p + 1) {
			return filename.substring(p + 1);
		} else {
			return ""; //$NON-NLS-1$
		}
	}

	@Override
	public String getDescription() {
		return explanation;
	}

}

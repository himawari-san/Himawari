/*
    Copyright (C) 2004-2025 Masaya YAMAGUCHI, Hajime Osada

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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.mozilla.universalchardet.UniversalDetector;

/**
 * エンコードを自動判別するクラス
 *
 * @author osada
 *
 */
public class EncodeDetector {
	/**
	 * ファイルのエンコーディングを自動判別する
	 * @param file
	 * @return 判別できない場合はnullを返却
	 * @throws IOException
	 */
	public static String detect(File file) throws IOException{
		byte[] buf = new byte[4096];
	    FileInputStream fis = new FileInputStream(file);

	    // createDetector
	    UniversalDetector detector = new UniversalDetector(null);

	    // feed file
	    int nread;
	    while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
	      detector.handleData(buf, 0, nread);
	    }
	    // end of data
	    detector.dataEnd();
	    
	    fis.close();

	    // get encode
	    return detector.getDetectedCharset();
	}

}

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

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2003-2005</p>
 *
 * <p>会社名: </p>
 *
 * @author Masaya YAMAGUCHI
 * @version 0.9.4
 */
public abstract class ContentsIndex extends Index {
	protected CorpusFile corpus;
	protected String elementName;

	public ContentsIndex() {
	}

	public String getElementName() {
		return elementName;
	}

	abstract boolean exists();

	abstract String getFilename();

	abstract void setRetrieveCondition(FieldInfo fieldInfo, String fieldName);

	abstract int mkcix(String elementName);

	abstract void open() throws java.io.IOException;

	abstract void close() throws java.io.IOException;
}

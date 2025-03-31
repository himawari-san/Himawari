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


public class CharNormalizerByTable extends CharNormalizer {
	String from = ""; //$NON-NLS-1$
	String to = ""; //$NON-NLS-1$

	public CharNormalizerByTable(String from, String to) {
		setTable(from, to);
		if(from.length() != to.length()){
			System.err.println("Warning: the length of from is not the same as the one of to\n"); //$NON-NLS-1$
			System.err.println("from:" + from + "," + from); //$NON-NLS-1$ //$NON-NLS-2$
			System.err.println("to  :" + from + "," + to); //$NON-NLS-1$ //$NON-NLS-2$
//
//			throw new DataFormatException("'from' and 'to' is unequal in length"); //$NON-NLS-1$
		}
	}
	
	@Override
	public String execute(String text) {
		for(int i = 0; i < from.length(); i++){
			text = text.replace(from.charAt(i), to.charAt(i));
		}
		return text;
	}

	public void setTable(String from, String to){
		this.from = from;
		this.to = to;
	}

}

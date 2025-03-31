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

import java.util.Map;


public class Annotator {
	protected String annotationName;
	protected String errorMessage;
	
	public final static char evilChars[] = {'\u2014', '\u2016', '\u2212', '\u301C', '\uFFFD', '\u00A2', '\u00A3', '\u00AC', '\u00A0',
		'\uFF5E', '\u2225', '\uFF0D', '\uFFE0', '\uFFE1', '\uFFE2'};

	
	
	public Annotator(String annotationName) {
		this.annotationName = annotationName;
	}

	public String getErrorMessage(){
		return errorMessage;
	}
	
	
	public static boolean charCheck(String text, Map<Character, Integer> charWarnings){
		boolean flagError = false;
		
		for(Character evilCode : evilChars){
			if(text.contains(String.valueOf(evilCode))){
				flagError = true;
				if(charWarnings.containsKey(evilCode)){
					charWarnings.put(evilCode, charWarnings.get(evilCode)+1);
				} else {
					charWarnings.put(evilCode, 1);
				}
			}
		}
		return flagError;
	}
}

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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;


public class SIXDic extends ArrayList<ArrayList<String>> {
	
	private static final long serialVersionUID = 1L;

	public static final String suffix = ".sdc"; //$NON-NLS-1$
	public static final String FIELD_DIC_ITEM_ID = "_DIC_ITEM_ID"; //$NON-NLS-1$
	public static final String FIELD_ANNOTATION_SEARCH_KEY = "_TEXT"; //$NON-NLS-1$
	public static final String LABEL_SEARCH_KEY = "出現形"; //$NON-NLS-1$

	HashMap<String, Integer> map = new HashMap<String, Integer>();
	File dic = null;
	ArrayList<String> fieldNames = new ArrayList<String>();
	int fnStart = -1;
	int fnEnd = -1;
	int fnText = -1;
	int fnID = -1;
	String name;
	
	public SIXDic(File dic, String name){
		this.dic = dic;
		this.name = name;
	}


	public void init(){
		clear();
		map.clear();
		fieldNames.clear();
	}

	
	public void setHeader(String[] header) throws IllegalStateException {
		boolean flagEmpty = fieldNames.isEmpty() ? true : false;
		int c = 0;
		
		fnStart = -1;
		fnEnd = -1;
		fnText = -1;
		fnID = -1;
		
		for (int i = 0; i < header.length; i++) {
			if (header[i].equals(DBController.FIELD_ANNOTATION_START)) {
				fnStart = i;
			} else if (header[i].equals(DBController.FIELD_ANNOTATION_END)) {
				fnEnd = i;
			} else if (header[i].equals(FIELD_DIC_ITEM_ID)) {
				fnID = i;
			} else {
				if (header[i].equals(DBController.FIELD_ANNOTATION_SEARCH_KEY)) {
					fnText = i;
				}
				if(flagEmpty){
					fieldNames.add(header[i]);
				} else {
					if(!fieldNames.get(c).endsWith(header[i])){
						throw new IllegalStateException("invalid header: " + fieldNames.get(c) + "," + header[i]); //$NON-NLS-1$ //$NON-NLS-2$
					}
					c++;
				}
			}
		}
	}
	
	
	public ArrayList<String> getFieldNames(){
		return fieldNames;
	}
	
	
	public int getFieldNumberStart(){
		return fnStart;
	}

	
	public int getFieldNumberEnd(){
		return fnEnd;
	}

	// index of header fields
	public int getFieldNumberText(){
		return fnText;
	}

	// index of dicItem
	public int getIndexText(){
		for(int i = 0; i < fieldNames.size(); i++){
			if(fieldNames.get(i).equals(FIELD_ANNOTATION_SEARCH_KEY)){
				return i;
			}
		}
		return -1;
	}
	
	public String getName(){
		return name;
	}
	
	public int addItem(String data[]){
		ArrayList<String> result = new ArrayList<String>(data.length);

		for(int i = 0; i < data.length; i++){
			if(i != fnStart && i != fnEnd && i != fnID){
				result.add(data[i]);
			}
		}
	
		String mapKey = StringUtils.join(result, "\t"); //$NON-NLS-1$
		Integer index = map.get(mapKey);
		if(index == null){
			add(result);
			int idic = size() - 1;
			map.put(mapKey, idic);
			return idic;
		} else {
			return index;
		}
	}
	

	public ArrayList<Integer> search(String argumentName, String value, int iDicLimit){
		ArrayList<Integer> results = new ArrayList<Integer>();
		int iArg = -1;
		for(int i = 0; i < fieldNames.size(); i++){
			if(fieldNames.get(i).equals(argumentName)){
				iArg = i;
				break;
			}
		}
		
		int i = 0;
		for(ArrayList<String> item : this){
			if(item.get(iArg).equals(value)){
				results.add(i);
			}
			i++;
			if(iDicLimit < i){
				break;
			}
		}
		
		return results;
	}

	
	public ArrayList<Integer> searchRegex(String argumentName, String value, int iDicLimit){
		ArrayList<Integer> results = new ArrayList<Integer>();
		Pattern valuePattern = Pattern.compile(value);
		Matcher matcher;

		int iArg = -1;
		for(int i = 0; i < fieldNames.size(); i++){
			if(fieldNames.get(i).equals(argumentName)){
				iArg = i;
				break;
			}
		}
		
		int i = 0;
		for(ArrayList<String> item : this){
			matcher = valuePattern.matcher(item.get(iArg));
			if(matcher.find()){
				results.add(i);
			}
			i++;
			if(iDicLimit < i){
				break;
			}
		}
		
		return results;
	}
	

	public void readDic() throws IllegalStateException, IOException{
		if (dic.exists()) {
			init();
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(dic), "utf-8")); //$NON-NLS-1$

			boolean flag = false;
			String line;
			while ((line = br.readLine()) != null) {
				String[] data = (line+"\n").split("\t"); //$NON-NLS-1$ //$NON-NLS-2$
				data[data.length-1] = data[data.length-1].substring(0, data[data.length-1].length()-1); 
				if (!flag) {
					setHeader(data);
					flag = true;
				} else {
					addItem(data);
				}
			}
			br.close();
			System.err.println("dic found: " + size()); //$NON-NLS-1$
		} else {
			System.err.println("dic not found"); //$NON-NLS-1$
		}
		
	}
	
	
	public void writeDic() throws UnsupportedEncodingException, FileNotFoundException{

		PrintWriter pw = new PrintWriter(new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(dic), "utf-8"))); //$NON-NLS-1$

		// output headers
		pw.print(FIELD_DIC_ITEM_ID + "\t"); //$NON-NLS-1$
		pw.println(StringUtils.join(fieldNames, "\t")); //$NON-NLS-1$

		for (int i = 0; i < size(); i++) {
			pw.print(i);
			pw.print("\t"); //$NON-NLS-1$
			pw.println(StringUtils.join(get(i), "\t")); //$NON-NLS-1$
		}
		pw.close();
	}
	
}

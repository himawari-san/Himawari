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
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


public class StandoffElementIndex extends ElementIndex {
	public static final String SUFFIX = ".six"; //$NON-NLS-1$
	private final static int HEADER_LINE = 1;
	private final static int MAX_ERROR_COUNT = 10;

	private Map<Character, Integer> charWarnings = new HashMap<Character, Integer>();

	private final String tmpFilenameBody = "_himawari_six"; //$NON-NLS-1$
	private final String tmpFilenameSuffix = ".tmp"; //$NON-NLS-1$
	
	private String eixFilename;
	private CorpusFile corpus;
	private String baseFilename;
	private String asxFilename;
	
	private RandomAccessFile fisEix;
	private IntBuffer ibEix;
	private FileChannel fc;


	private String errorMessage = ""; //$NON-NLS-1$
	private SIXDic sixdic;
	private ArrayList<String> dicFieldNames;
	private String elementNamePsTab;
	private String columnNames[];

	private int ibEixEnd;
	
	public StandoffElementIndex(String elementName, CorpusFile corpus, SIXDic sixdic) {

		this.corpus = corpus;
		this.baseFilename = corpus.getBasename();

		this.elementName = elementName;

		this.eixFilename = baseFilename + "." + elementName + SUFFIX; //$NON-NLS-1$
		asxFilename = baseFilename + "." + elementName + StandoffArgumentIndex.suffix; //$NON-NLS-1$
		this.sixdic = sixdic;
		dicFieldNames = sixdic.getFieldNames();
		elementNamePsTab = elementName + "\t"; //$NON-NLS-1$
		columnNames = new String[dicFieldNames.size()];
		for(int i = 0; i < dicFieldNames.size(); i++){
			columnNames[i] = elementNamePsTab + dicFieldNames.get(i);
		}
		
	}
	
	@Override
	public void open() {
		try {
			File eixFile = new File(eixFilename);
			fisEix = new RandomAccessFile(eixFile, "r"); //$NON-NLS-1$
			fc = fisEix.getChannel();
			ibEix = fc.map(FileChannel.MapMode.READ_ONLY, 0, eixFile.length())
					.asIntBuffer();
			ibEixEnd = ibEix.limit();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	void close() {
		try {
			if (fc != null) {
				fc.close();

			}
			if (fisEix != null) {
				fisEix.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		fisEix = null;
		ibEix = null;
		fc = null;
	}

	@Override
	public boolean isOpen() {
		if (fc != null) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean exists() {
		File eixFile = new File(eixFilename);
		return eixFile.exists();
	}

	@Override
	public String searchArg(int place) {
		return null;
	}

	@Override
	public Range searchRange(int place) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Range searchRange(int place, int n) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<ResultRecord> listElement(FieldInfo fieldInfo, Filter filter) throws Exception {

		ArrayList<ResultRecord> resultRecords = new ArrayList<ResultRecord>();

		int p = 0; // idic
		int idic;
		int pStart;
		int i;
		Pattern hashValue;
		String argValue;
		boolean flag;
		
		while(ibEixEnd > p){
			pStart = ibEix.get();
			ibEix.get(); // pEnd
			idic = ibEix.get();
			flag = true;
			p += 3;
			
			ArrayList<String> dicItem = sixdic.get(idic);
			ResultRecord resultRecord = new ResultRecord(fieldInfo);

			if (filter == null || filter.size() == 0) { // no fileter
				i = 0;
				for(String columnName : columnNames){
					resultRecord.set(columnName, dicItem.get(i++));
				}
			} else { // using the filter
				i = 0;
				for(String columnName : columnNames){
					hashValue = filter.get(columnName);
					argValue = dicItem.get(i++);
					if (hashValue != null) {
						if (filter.isNot(columnName)) { // 否定
							if (((Pattern) hashValue).matcher(argValue).find()) {
								flag = false;
								break;
							} else {
								resultRecord.set(columnName, argValue);
							}
						} else {
							if (((Pattern) hashValue).matcher(argValue).find()) {
								resultRecord.set(columnName, argValue);
							} else {
								flag = false;
								break;
							}
						}
					} else {
						resultRecord.set(columnName, argValue);
					}
				}
				// 属性値がない場合は想定しない
			}

			if(flag){
				resultRecord.setPosition(pStart);
				resultRecords.add(resultRecord);
			}
		}

		return resultRecords;
	}
	

	public ArrayList<Integer> listIndex(FieldInfo fieldInfo) throws Exception {

		ArrayList<Integer> indexes = new ArrayList<Integer>();

		int p = 0; // idic
		int pStart;
		
		while(ibEixEnd > p){
			pStart = ibEix.get();
			ibEix.get(); // pEnd
			ibEix.get(); // idic
			p += 3;
			
			indexes.add(pStart);
		}

		return indexes;
	}


	@Override
	public ArrayList<ResultRecord> listContents(FieldInfo fieldInfo)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public ResultRecord addAttribute(ResultRecord resultRecord, Filter filter) {
		int txtPos;

		String argValue;
		Pattern hashValue;
		int i;
		
		txtPos = resultRecord.getPosition();
		int idic = searchDicIndex(txtPos);
		if(idic == -1){
			// return resultRecord without using the filter
			return resultRecord;
		}
		ArrayList<String> dicItem = sixdic.get(idic);

		if (filter == null || filter.size() == 0) { // no fileter
			i = 0;
			for(String columnName : columnNames){
				resultRecord.set(columnName, dicItem.get(i++));
			}
		} else { // using the filter
			i = 0;
			for(String columnName : columnNames){
				hashValue = filter.get(columnName);
				argValue = dicItem.get(i++);
				if (hashValue != null) {
					if (filter.isNot(columnName)) { // 否定
						if (((Pattern) hashValue).matcher(argValue).find()) {
							return null;
						} else {
							resultRecord.set(columnName, argValue);
						}
					} else {
						if (((Pattern) hashValue).matcher(argValue).find()) {
							resultRecord.set(columnName, argValue);
						} else {
							return null;
						}
					}
				} else {
					resultRecord.set(columnName, argValue);
				}
			}
			// 属性値がない場合は想定しない
		}

		return resultRecord;
	}

	@Override
	public ResultRecord resistNextElement(ResultRecord resultRecord,
			int relativeElementIndex, String targetAttributeName, Filter filter) {
		// attention: targetAttributeName is not used

		String fieldID = elementName + "[" + relativeElementIndex + "]\t"; //$NON-NLS-1$ //$NON-NLS-2$
		int txtPos = resultRecord.getPosition();
		int idic = searchDicIndex(txtPos);
		if(idic == -1){
			// return resultRecord without using the filter
			return resultRecord;
		}

		int newP = ibEix.position() + (relativeElementIndex-1)*3 + 2;
	
		if(newP < 0 || newP > ibEixEnd){
			// Get no next element
			for (String fieldName : dicFieldNames) {
				String columnName = fieldID + fieldName;
				Pattern filterValue = filter.get(columnName);

				if (filterValue != null) {
					if (filter.isNot(columnName)) { // 否定
						if (filterValue.matcher("").find()) {
							return null;
						}
					} else {
						if (!filterValue.matcher("").find()) {
							return null;
						}
					}
				}
			}
			return resultRecord;
		}

		ibEix.position(newP);
		idic = ibEix.get();

		ArrayList<String> dicItem = sixdic.get(idic);
		
		// TODO when targetAttributeName is empty
		
		int i;
		if (filter == null || filter.size() == 0) { // フィルタが指定されていない場合
			i = 0;
			for(String fieldName : dicFieldNames){
				resultRecord.set(fieldID + fieldName, dicItem.get(i++));
			}
		} else {
			i = 0;
			for (String fieldName : dicFieldNames) {
				String columnName = fieldID + fieldName;
				String columnValue = dicItem.get(i++);
				if (columnValue == null) {
					columnValue = ""; //$NON-NLS-1$
				}
				Pattern filterValue = filter.get(columnName);

				if (filterValue != null) {
					if (filter.isNot(columnName)) { // 否定
						if (filterValue.matcher(columnValue).find()) {
							return null;
						} else {
							resultRecord.set(columnName, columnValue);
						}
					} else {
						if (filterValue.matcher(columnValue).find()) {
							resultRecord.set(columnName, columnValue);
						} else {
							return null;
						}
					}
				} else {
					resultRecord.set(columnName, columnValue);
				}
			}

		}
		return resultRecord;
	}

	@Override
	public ResultRecord resistSiblingElement(ResultRecord resultRecord,
			String targetElementName, String targetAttributeName, Filter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIOFilename() {
		return eixFilename;
	}

	
	// get the index of dic 
	public int searchDicIndex(int place) {
		int start = 0;
		int end = ibEixEnd;
		int curEixPos;
		int elementStartPos;
		int elementEndPos;
		int idic;
		int t;

		while (start <= end) {
			t = (start + end) / 2;
			curEixPos = t - (t % 3);
			// read a position of the text from the array
			ibEix.position(curEixPos);
			elementStartPos = ibEix.get();
			if (place < elementStartPos) {
				end = curEixPos - 3;
				continue;
			}

			elementEndPos = ibEix.get();
			if (place > elementEndPos) {
				start = curEixPos + 3;
				continue;
			}

			idic = ibEix.get();
			return idic;
		}
		return -1;
	}


	public String getText(int place){
		int idic = searchDicIndex(place);
		ArrayList<String> dicItem = sixdic.get(idic);
		if(dicItem == null){
			return null;
		} else {
			return dicItem.get(sixdic.getIndexText());
		}
	}
	
	
	public void mksix(String annotatorName, UserSettings userSettings) throws Exception {
		ExternalAnnotator ea = new ExternalAnnotator(annotatorName, elementName);
		ea.init(userSettings);
		String targetElementName = ea.getTargetElementName();
		String targetAttributeName = ea.getTargetAttributeName();
		String targetValue = ea.getTargetAttributeValue();

		File resultFile = null;

		resultFile = File.createTempFile(tmpFilenameBody, tmpFilenameSuffix);
		resultFile.deleteOnExit();
		ea.execute(corpus, resultFile);
		
		corpus.init();

		storeData(resultFile, targetElementName, targetAttributeName, targetValue, 1);
		resultFile.delete();

	}
	
	public void storeData(File annotationDataFile, String targetElementName, String attributeName, String value, int mode) throws NumberFormatException, IllegalStateException, IOException {
		HashMap<Integer, ArrayList<Integer>> pMap= new HashMap<Integer, ArrayList<Integer>>(); 
		
		FileInputStream is = new FileInputStream(annotationDataFile);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		String line;
		int lineNum = 1;
		int fnStart = -1;
		int fnEnd = -1;
		int fnText = -1;
		int currentPosition = 0;
		
		int pStart = -1;
		int pEnd = -1;
		
		int cError = 0; // 

		int targetEnd = corpus.gotoElement(targetElementName, attributeName, value);
		
		DataOutputStream dosEIX = null;
		
		if(mode == 1){
			dosEIX = new DataOutputStream(new FileOutputStream(new File(eixFilename)));
		}
		
		while ((line = br.readLine()) != null) {
			if (line.length() == 0) {
				continue;
			}

			String[] data = (line + "\n").split("\t"); //$NON-NLS-1$ //$NON-NLS-2$
			data[data.length-1] = data[data.length-1].substring(0, data[data.length-1].length()-1); 
			
			// ヘッダー行
			if (lineNum++ == HEADER_LINE) {
				sixdic.setHeader(data);
				fnStart = sixdic.getFieldNumberStart();
				fnEnd = sixdic.getFieldNumberEnd();
				fnText = sixdic.getFieldNumberText();
			}
			// 本文行
			else {
				int start = Integer.parseInt(data[fnStart]) - 1;
				int end = Integer.parseInt(data[fnEnd]) - 1;
				String targetText = data[fnText];

				int nSkip = start - currentPosition;
				currentPosition = end + 1;

				// 空白，改行などを読み飛ばす
				corpus.setPositionToContents(targetElementName, attributeName, value);
				
				// go to the next element or skip elements whose contents is empty
				int rc = 0;
				while(targetEnd != -1 && targetEnd <= corpus.getPosition()){
					targetEnd = corpus.gotoElement(targetElementName, attributeName, value);
					corpus.setPositionToContents(targetElementName, attributeName, value);
					if(rc++ > 0){
						System.err.println("Warning(StandoffElementIndex): skip an element, cp:" + corpus.getPosition() + ", end:" + targetEnd); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}

				if (nSkip > 0) {
					corpus.skip(nSkip);
				} else if(nSkip < 0){
					System.err.println("error in annotationimport: " + nSkip + ", " + start + ", " + end); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
				
				//  _start と _end を取得
				pStart = corpus.getPosition() / 2;
				String corpusText = corpus.readStrN(targetText.length());
				pEnd = corpus.getPosition() / 2 - 1; // 読み込み後なので，１文字引く

				
				if (!targetText.equals(corpusText) && (!corpusText.equals(",") || !targetText.equals("，"))){ //$NON-NLS-1$ //$NON-NLS-2$
					if(Annotator.charCheck(corpusText, charWarnings)){
						cError = 0;
					} else {
						errorMessage += Messages.getString("Annotator.29") + targetText + Messages.getString("Annotator.30") //$NON-NLS-1$ //$NON-NLS-2$
								+ corpusText + Messages.getString("Annotator.31") + line + "\n"; //$NON-NLS-1$ //$NON-NLS-2$

						cError++;
						System.err.println("diff(" + cError + "): line=" + line + ", pos=" + corpus.getPosition()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						System.err.println("targetText:" + targetText); //$NON-NLS-1$
						System.err.println("copursText:" + corpusText); //$NON-NLS-1$

						if (cError > MAX_ERROR_COUNT) {
							errorMessage += MAX_ERROR_COUNT + Messages.getString("Annotator.33") //$NON-NLS-1$
									+ Messages.getString("Annotator.34"); //$NON-NLS-1$
							br.close();
							throw new IllegalStateException(errorMessage);
						}
					}
				} else if(start > end){
					errorMessage += Messages.getString("Annotator.35") + line + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
							Messages.getString("Annotator.37"); //$NON-NLS-1$
					br.close();
					throw new IllegalStateException(errorMessage);
				} else {
					cError = 0;
				}

				
				if(mode == 1){
					int iDic = sixdic.addItem(data);
					dosEIX.writeInt(pStart);
					dosEIX.writeInt(pEnd);
					dosEIX.writeInt(iDic);
					
					if(pMap.containsKey(iDic)){
						ArrayList<Integer> p = pMap.get(iDic);
						p.add(pStart);
					} else {
						ArrayList<Integer> p = new ArrayList<Integer>();
						p.add(pStart);
						pMap.put(iDic, p);
					}
				}
			}
			lineNum++;
		}

		sixdic.writeDic();
		
		if(charWarnings.size() > 0){
			errorMessage += "* Warning(the number of skipped characters)\n"; //$NON-NLS-1$
		}
		for ( Character charCode : charWarnings.keySet() ) {
			int c = charWarnings.get( charCode );
			errorMessage += "  " + String.format("0x%04x", (int)charCode) + " : " + c + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}

		br.close();
		is.close();

		// asx format
		//
		// (a) 4byte ...  the max index of dic item (for the target corpus)
		// (b) 4byte x (a)  ... indexes to dic items
		//                      -1 if no dic item
		// (c) ... dic items (indexes to corpus)
		//
		// Example
		// 2 ... (a) (this dic has two items)
		// 3 ... (b) (index to dic item 1)
		// 6 ... (b) (index to dic item 2)
		// 111 114 115 ... (c) (indexes to corpus of dic item 1)
		// 323 431 243 ... (c) (indexes to corpus of dic item 2)
		
		DataOutputStream dosASX = new DataOutputStream(new FileOutputStream(new File(asxFilename)));
		ArrayList<Integer> iDicList = new ArrayList<Integer>(pMap.keySet());
		Collections.sort(iDicList);

		int iDicMax = iDicList.get(iDicList.size()-1);
		System.err.println("dic max:" + iDicMax); //$NON-NLS-1$
		dosASX.writeInt(iDicMax);

		int fp = iDicMax+1+1; // iDicMax+1 = size of dic, +1 is for (a)
		for(int i = 0; i <= iDicMax; i++){
			if(iDicList.contains(i)){
				dosASX.writeInt(fp);
				fp += pMap.get(i).size();
			} else {
				dosASX.writeInt(-1);
			}
		}
		
		for(int i : iDicList){
			ArrayList<Integer> pList = pMap.get(i);
			Collections.sort(pList);
			for(int p : pList){
				dosASX.writeInt(p);
			}
		}
		dosASX.close();
		
		return;

	}
	
	public SIXDic getDic(){
		return sixdic;
	}
}

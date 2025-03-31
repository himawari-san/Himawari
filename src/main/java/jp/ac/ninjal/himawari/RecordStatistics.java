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
 * <p>タイトル: </p>
 * <p>説明: </p>
 * <p>著作権: Copyright (c) 2003</p>
 * <p>会社名: </p>
 * @author Masaya YAMAGUCHI
 * @version 0.9.4
 */

import java.util.*;

import javax.swing.table.DefaultTableCellRenderer;


public class RecordStatistics {
	private FieldInfo statFieldInfo = null;
	private String[] fieldNames = null;
	private HashMap<String, ResultRecord> listFreq = null;
	private HashMap<String, ResultRecord> listAccumulated = null;
	private int c = 0;
    private StringBuilder keyBuffer = new StringBuilder();
    private int iFreqStat; // the field number of FREQUENCY field 
    private boolean useFreq = true; // use the value of Freq field when "add" and "accumulate"
	private String keyDelimiter = "\t"; //$NON-NLS-1$
	private int nErrors = 0;
	
	public RecordStatistics(FieldInfo statFieldInfo, boolean useFreq) {
		this.statFieldInfo = statFieldInfo;
		this.useFreq = useFreq;
		fieldNames = statFieldInfo.getNames();
	    listFreq = new HashMap<String, ResultRecord>();
	    listAccumulated = new HashMap<String, ResultRecord>();
	    iFreqStat = statFieldInfo.getIndexFreq();
	    if(iFreqStat == -1 && useFreq){
	    	useFreq = false;
	    	System.err.println("Warning(RecordStatistics): No Frequency field in FieldInfo."); //$NON-NLS-1$
	    }
	}

	public RecordStatistics(FieldInfo statFieldInfo) {
		this(statFieldInfo, false);
	}

	
  	public void add(List<ResultRecord> records){
  		Iterator<ResultRecord> it = records.iterator();
  		while(it.hasNext()){
  			add(it.next());
  		}
  	}

	
	public void add(ResultRecord resultRecord) {
		int freq = 1;
		keyBuffer.setLength(0);
		
		for (int i = 0; i < fieldNames.length; i++) {
			if (i == iFreqStat) {
				continue;
			}
			Object v = resultRecord.get(fieldNames[i]);
			if (v != null) {
				keyBuffer.append(v.toString());
				keyBuffer.append("\t"); //$NON-NLS-1$
			} else {
				keyBuffer.append("\t"); //$NON-NLS-1$
			}
		}
		if(keyBuffer.length() > 0){
			keyBuffer.deleteCharAt(keyBuffer.length()-1);
		}
		
		if(useFreq){
			Object v = resultRecord.get(FieldInfo.FIELDNAME_FREQ);
			if(v != null){
				freq = (int)v;
			}
		}

		String key = keyBuffer.toString();
		ResultRecord currentResultRecord = listFreq.get(key);
		if (currentResultRecord != null) {
			if(iFreqStat != -1){
				int currentFreq = (int)currentResultRecord.get(iFreqStat);
				currentResultRecord.set(iFreqStat, currentFreq + freq);
			}
		} else {
			ResultRecord newResultRecord = new ResultRecord(statFieldInfo);
			resultRecord.copyTo(newResultRecord);
			if(iFreqStat != -1){
				newResultRecord.set(iFreqStat, freq);
			}
			listFreq.put(key, newResultRecord);
		}
		c++;
	}
  	

  	public FieldInfo accumulate(List<ResultRecord> records, int iAccumulatedHeader){
  		nErrors = 0;
  		FieldInfo newFieldInfo = new FieldInfo(statFieldInfo.size());
		String headerName = statFieldInfo.getName(iAccumulatedHeader);
		listAccumulated.clear();

		// make newFieldInfo
		for(int i = 0; i < statFieldInfo.size(); i++){
			if(i == iAccumulatedHeader){
				newFieldInfo.set(Messages.getString("RecordStatistics.0") + headerName, //$NON-NLS-1$
						i,
						statFieldInfo.getType(i),
						statFieldInfo.getElementName(i),
						statFieldInfo.getAttributeName(i),
						statFieldInfo.getWidth(i),
						DefaultTableCellRenderer.RIGHT,
						statFieldInfo.getSortDirection(i),
						statFieldInfo.getSortOrder(i),
						FieldInfo.SORT_TYPE_NUMERIC,
						statFieldInfo.isEditable(i),
						statFieldInfo.getEditType(i),
						statFieldInfo.getEditOption(i));
			} else {
				newFieldInfo.set(statFieldInfo.getName(i),
						i,
						statFieldInfo.getType(i),
						statFieldInfo.getElementName(i),
						statFieldInfo.getAttributeName(i),
						statFieldInfo.getWidth(i),
						DefaultTableCellRenderer.RIGHT,
						statFieldInfo.getSortDirection(i),
						statFieldInfo.getSortOrder(i),
						statFieldInfo.getSortType(i),
						statFieldInfo.isEditable(i),
						statFieldInfo.getEditType(i),
						statFieldInfo.getEditOption(i));
				
			}
		}

  		for(ResultRecord record : records){
  			if(!accumulate(record, newFieldInfo, iAccumulatedHeader)){
  				nErrors++;
  			}
  		}
  		
  		return newFieldInfo;
  	}

	public boolean accumulate(ResultRecord resultRecord, FieldInfo newFieldInfo, int iAccumulatedHeader) {
		int freq = 1;
		int targetValue = 0;
		
		// make key for listAccumulated
		keyBuffer.setLength(0);
		for (int i = 0; i < fieldNames.length; i++) {
			if (i == iFreqStat) {
				keyBuffer.append(keyDelimiter);
				continue;
			} else if (i == iAccumulatedHeader) {
				try{
					targetValue = Integer.parseInt(resultRecord.get(i).toString());
				} catch(NumberFormatException e){
					return false;
				}
				keyBuffer.append(keyDelimiter);
				continue;
			} else {
				Object v = resultRecord.get(fieldNames[i]);
				if (v != null) {
					keyBuffer.append(v.toString());
					keyBuffer.append(keyDelimiter);
				} else {
					keyBuffer.append(keyDelimiter);
				}
			}
		}

		if(useFreq){
			Object v = resultRecord.get(FieldInfo.FIELDNAME_FREQ);
			if(v != null){
				freq = (int)v;
			}
		}

		String key = keyBuffer.toString();
		ResultRecord currentResultRecord = listAccumulated.get(key);
		if (currentResultRecord != null) {
			int currentAccumulatedValue = (int)currentResultRecord.get(iAccumulatedHeader);
			currentResultRecord.set(iAccumulatedHeader, currentAccumulatedValue + targetValue * freq);
		} else {
			ResultRecord newResultRecord = new ResultRecord(newFieldInfo);
			resultRecord.copyTo(newResultRecord);
			newResultRecord.set(iAccumulatedHeader, targetValue * freq);
			listAccumulated.put(key, newResultRecord);
		}
		c++;
		
		return true;
	}
	
	
	public int getErrors(){
		return nErrors;
	}
	
	
	public ArrayList<ResultRecord> getResults() {
		ArrayList<ResultRecord> result = new ArrayList<ResultRecord>();

		for(ResultRecord resultRecord : listFreq.values()){
			result.add(resultRecord);
		}
		listFreq.clear();

		return result;
	}

	public ArrayList<ResultRecord> getResults(int iAccumulatedHeader) {
		ArrayList<ResultRecord> result = new ArrayList<ResultRecord>();
		Iterator<String> it = listAccumulated.keySet().iterator();
		
		while (it.hasNext()) {
			String enumKey = it.next();
			ResultRecord newResultRecord = listAccumulated.get(enumKey);
			if (iFreqStat != -1) {
				newResultRecord.set(fieldNames[iFreqStat], 1);
			}
			result.add(newResultRecord);
			it.remove();
		}
		return result;
	}  	
  	
  	public int size(){
  		return c;
  	}
  	
  	
  	public void increment(){
  		c++;
  	}
  	
  	public FieldInfo getFieldInfo(){
  		return statFieldInfo;
  	}
  	
  	  	
  	public boolean hasTable(){
  		if(listFreq.isEmpty()){
  			return false;
  		} else {
  			return true	;
  		}
  	}
  	
  	
  	class MapValue {
  		public int freq;
  		public ResultRecord resultRecord;
  		
  		public MapValue(int freq, ResultRecord resultRecord){
  			this.freq = freq;
  			this.resultRecord = resultRecord;
  		}
  	}
}

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

public class CorpusElementInfo extends HashMap<String, HashMap<String, String>> {
	private static final long serialVersionUID = 1L;
	private static final String PROPERTY_SELECTED = "selected"; //$NON-NLS-1$
	private static final String PROPERTY_LABEL = "label"; //$NON-NLS-1$
	private static final String PROPERTY_TYPE = "type"; //$NON-NLS-1$
	
	private static final String PROPERTY_VALUE_TRUE = "true"; //$NON-NLS-1$
	private static final String PROPERTY_VALUE_FALSE = "false"; //$NON-NLS-1$
	
	private String name;
	private boolean flagEmpty = false;
	
	public CorpusElementInfo(String elementName){
		this.name = elementName;
		// key => attributes of the element
		// value => properties of the attributes
	}
	
	public void setEmpty(boolean flag){
		flagEmpty = flag;
	}
	
	public boolean isEmplty(){
		return flagEmpty;
	}
	
	public String getName(){
		return name;
	}
	
	public void setProperty(String attributeName, String propertyName, String value){
		HashMap<String, String> properties;
		
		if(!containsKey(attributeName)){
			properties = new HashMap<String, String>();
			put(attributeName, properties);
		} else {
			properties = get(attributeName);
		}
		
		properties.put(propertyName, value);
	}

	public String getProperty(String attributeName, String propertyName){
		if(!containsKey(attributeName)){
			return null;
		}
		
		return get(attributeName).get(propertyName);
	}

	public void setSelected(String attributeName, boolean isSelected){
		if(isSelected){
			setProperty(attributeName, PROPERTY_SELECTED, PROPERTY_VALUE_TRUE);
		} else {
			setProperty(attributeName, PROPERTY_SELECTED, PROPERTY_VALUE_FALSE);
		}
	}

	public boolean isSelected(String attributeName){
		String v = getProperty(attributeName, PROPERTY_SELECTED);
		if(v == null){
			return false;
		} else if(v.equals(PROPERTY_VALUE_TRUE)){
			return true;
		} else if(v.equals(PROPERTY_VALUE_FALSE)){
			return false;
		}
		return false;
	}
	
	public void setLabel(String attributeName, String label){
		setProperty(attributeName, PROPERTY_LABEL, label);
	}

	public String getLabel(String attributeName){
		return getProperty(attributeName, PROPERTY_LABEL);
	}
	
	public void setType(String attributeName, String type){
		setProperty(attributeName, PROPERTY_TYPE, type);
	}

	public String getType(String attributeName){
		return getProperty(attributeName, PROPERTY_TYPE);
	}
}

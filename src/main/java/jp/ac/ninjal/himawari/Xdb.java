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
import java.util.HashMap;
import javax.xml.parsers.*;
import org.xml.sax.*;


/**
 * <p>タイトル: </p>
 * <p>説明: </p>
 * <p>著作権: Copyright (c) 2003</p>
 * <p>会社名: </p>
 * @author Masaya YAMAGUCHI
 * @version 0.9.4
 */

public class Xdb extends HashMap<String, ResultRecord> {
	private static final long serialVersionUID = -2277142782498329514L;
	private static final String DEFAULT_P_WIDTH = "300px";
	private static final String DEFAULT_P_LINE_HEIGHT = "line-height:1.2;";
	private static final String DEFAULT_P_MARGINE = "margin: 2px 0px;";

	private FieldInfo fieldInfo;
	private String name;

	public Xdb(String name, FieldInfo fieldInfo) {
		this.fieldInfo = fieldInfo;
		this.name = name;
	}

	public String[] getFields() {
		return fieldInfo.getNames();
	}

	public String getName() {
		return name;
	}

	public FieldInfo getFieldInfo() {
		return fieldInfo;
	}


  public String getRecordSummary(String key){
	  return getRecordSummary(key, DEFAULT_P_WIDTH);
  }

  public String getRecordSummary(String key, String width){
    ResultRecord record = (ResultRecord)get(key);
    String fieldnames[] = fieldInfo.getNames();
    StringBuilder res = new StringBuilder();
    String pStartTag = "<p style=\"width:" + width + "; "
    		+ DEFAULT_P_LINE_HEIGHT
    		+ DEFAULT_P_MARGINE
    		+ "\">";
    if(record == null){
      return null;
    }

    res.append("<html>"); //$NON-NLS-1$

    for(String fieldname : fieldnames){
    	res.append(
    			pStartTag
    			+ "<strong>" + fieldname + ": </strong>"
    			+ record.get(fieldname)
    			+ "</p>");
    }
    res.append("</html>"); //$NON-NLS-1$
    return res.toString();
  }


  public void load(String url, String recordElementName, String keyElementName){
    TableDataHandler tableDataHandler =
        new TableDataHandler(this, fieldInfo, recordElementName, keyElementName);

    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser parser = factory.newSAXParser();
      parser.parse(url, tableDataHandler);
    } catch (SAXException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    }
  }
}

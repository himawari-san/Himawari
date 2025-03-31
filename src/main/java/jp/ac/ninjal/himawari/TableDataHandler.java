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

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;


/**
 * <p>タイトル: </p>
 * <p>説明: </p>
 * <p>著作権: Copyright (c) 2003</p>
 * <p>会社名: </p>
 * @author Masaya YAMAGUCHI
 * @version 0.9.4
 */

public class TableDataHandler extends DefaultHandler {
  private Xdb xdb;
  private ResultRecord record;
  private FieldInfo fieldInfo;
  private StringBuilder value = new StringBuilder();
  private String key; // 行を検索する時のキー
  private String currentElementName; // 現在処理中の要素名
  private String recordElementName; // 表の行に対応する要素名
  private String keyElementName; // 行を検索する時のキーとなる要素名
  private int n = 0; // 登録された行数

  public TableDataHandler(Xdb xdb, FieldInfo fieldInfo,
                          String recordElementName, String keyElementName) {
    this.xdb = xdb;
    this.fieldInfo = fieldInfo;
    this.recordElementName = recordElementName;
    this.keyElementName = keyElementName;
  }

  public FieldInfo getFieldInfo(){
    return fieldInfo;
  }

  public void startDocument() {}

  public void endDocument() {}

  public void startElement(String namespaceURI,
                           String localName,
                           String qName,
                           Attributes atts) {

    if(qName.compareTo(recordElementName) == 0){
      record = new ResultRecord(fieldInfo);
      record.setPosition(n++);
      record.setResourceID(0);
    }
    currentElementName = qName;
    value.delete(0, value.length());
  }

  public void endElement(String namespaceURI,
                         String localName,
                         String qName) {


    if(qName.compareTo(recordElementName) == 0){
      xdb.put(key, record);
    } else {
      record.set(currentElementName, value.toString());
    }
    currentElementName = ""; //$NON-NLS-1$
  }

  public void characters(char[] ch, int start, int length) {
    if(currentElementName.compareTo("") == 0){ //$NON-NLS-1$
      return;
    }
    value.append(new String(ch, start, length));
    if(currentElementName.compareTo(keyElementName) == 0){
      key = value.toString();
    }
  }
}


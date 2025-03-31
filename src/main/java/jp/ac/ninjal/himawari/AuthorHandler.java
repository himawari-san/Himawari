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

public class AuthorHandler extends DefaultHandler {

  private Xdb xdb;
  private ResultRecord record;
  private FieldInfo fieldInfo;
  private String authorName;
  private String elementName;
  private StringBuilder value = new StringBuilder();
  private String element_root = Messages.getString("AuthorHandler.0"); //$NON-NLS-1$
  private String element_author = Messages.getString("AuthorHandler.1"); //$NON-NLS-1$
  private String element_name = Messages.getString("AuthorHandler.2"); //$NON-NLS-1$

  public AuthorHandler(Xdb xdb, FieldInfo fieldInfo){
    this.xdb = xdb;
    this.fieldInfo = fieldInfo;
  }

  public void startDocument() {
//    System.out.println("startDocument");
  }

  public void endDocument() {
//    System.out.println("endDocument");
  }

  public void startElement(String namespaceURI,
                           String localName,
                           String qName,
                           Attributes atts) {

    if(qName.compareTo(element_root) == 0){

    } else if(qName.compareTo(element_author) == 0){
      record = new ResultRecord(fieldInfo);
    }
    elementName = qName;
    value.delete(0, value.length());
  }

  public void endElement(String namespaceURI,
                         String localName,
                         String qName) {


    if(qName.compareTo(element_root) == 0){

    } else if(qName.compareTo(element_author) == 0){
      xdb.put(authorName, record);
    } else {
      record.set(elementName, value.toString());
    }
    elementName = ""; //$NON-NLS-1$
  }

  public void characters(char[] ch, int start, int length) {
    if(elementName.compareTo("") == 0){ //$NON-NLS-1$
      return;
    }
    value.append(new String(ch, start, length));
    if(elementName.compareTo(element_name) == 0){
      authorName = value.toString();
    }
  }
}

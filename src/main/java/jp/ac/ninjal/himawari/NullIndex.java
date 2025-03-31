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
import java.util.regex.*;

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
public class NullIndex extends Index {
  protected CorpusFile corpus;
  private BufferedReader br;
  protected String elementName;
  private String line = null;
  private Pattern rexPattern;
  private Matcher rexMatcher;
  private int p = 0;
//  private int offset = 0;
  protected FieldInfo fieldInfo;
  protected String fieldName;


  public NullIndex(String elementName, CorpusFile corpus) {
    this.corpus = corpus;
    this.elementName = elementName;
  }

  /**
   * close
   *
   * @throws IOException
   * @todo この himawari.Index メソッドを実装
   */
  void close() throws IOException {
    if(br != null ) br.close();
  }

  /**
   * open
   *
   * @throws IOException
   * @todo この himawari.Index メソッドを実装
   */
  void open() throws IOException {
    File xmlFile = corpus.getFile();
    br = new BufferedReader(new FileReader(xmlFile));
  }

  /**
   * retrieveFirst
   *
   * @param target String
   * @return ResultRecord
   * @todo この himawari.Index メソッドを実装
   */
  ResultRecord retrieveFirst(String target) {
    ResultRecord resultRecord;
//    offset = 0;
    rexPattern = Pattern.compile(target);
    rexMatcher = null;

    try {
      while((line = br.readLine()) != null){
        rexMatcher = rexPattern.matcher(line);
        if (rexMatcher.find()) {
          String matchedStr = line.substring(rexMatcher.start(), rexMatcher.end());
          resultRecord = new ResultRecord(fieldInfo);
          resultRecord.setPosition(p);
          resultRecord.set(fieldName, matchedStr);
//          offset += rexMatcher.end();
          p += line.length() + 2; // add the length of LF
          return resultRecord;
        }
        p = line.length() + 2; // add the length of LF
      }
    }
    catch (IOException ex) {
    }
    return null;
  }

  /**
   * retrieveNext
   *
   * @return ResultRecord
   * @todo この himawari.Index メソッドを実装
   */
  ResultRecord retrieveNext() {
    return null;
  }

  public void setRetrieveCondition(FieldInfo fieldInfo, String fieldName){
    this.fieldInfo = fieldInfo;
    this.fieldName = fieldName;
  }

  public String getIOFilename()
  {
	  return null;
  }
}

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
public class ContentsNullIndex  extends ContentsIndex {
	public static String NULL_INDEX_FILENAME = "__null_index_"; //$NON-NLS-1$
	
//  protected CorpusFile corpus;
  private BufferedReader br;
//  protected String elementName;
  private String line = null;
  private Pattern rexPattern;
  private Pattern tagPattern;
  private Matcher rexMatcher;
  private int p = 0;
  private int offset = 0;
  private int realOffset = 0;
  protected FieldInfo fieldInfo;
  protected String fieldName;
  private int elementDepth = 0;
  private String taggedText = ""; //$NON-NLS-1$
  private String rawText = ""; //$NON-NLS-1$
  private int rexMatchOffset = 0;
  private Matcher tagMatcher;
  private int targetElementStart = 0;

  public ContentsNullIndex(String elementName, CorpusFile corpus) {
    this.elementName = elementName;
    this.corpus = corpus;
    // start tag and end tag
    tagPattern = Pattern.compile("<" + elementName + ">|<" + elementName + " [^>]+>|</" + elementName + ">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
  }

  /**
   * close
   *
   * @throws IOException
   */
  public void close() throws IOException {
    if(br != null){
      br.close();
    }
  }

  /**
   * open
   *
   * @throws IOException
   */
  public void open() throws IOException {
    File xmlFile = corpus.getFile();
    br = new BufferedReader(new InputStreamReader(new FileInputStream(xmlFile), "UnicodeLittle")); //$NON-NLS-1$
    System.out.println("open"); //$NON-NLS-1$
  }
  /**
   * retrieveFirst
   *
   * @param target String
   * @return ResultRecord
   * @todo この himawari.Index メソッドを実装
   */

  public ResultRecord retrieveFirst(String target) {
    ResultRecord resultRecord;
    p = 1; // length of BOM
    realOffset = 0;
    rexPattern = Pattern.compile(target);
    rexMatcher = null;

    try{
      while ( (line = br.readLine()) != null) { // read a line
        targetElementStart = 0;
        offset = 0;

        // concatenate multiline tag
        while (checkLineFeedInTag(line) > 0) {
          line = line.concat("\n" + br.readLine()); //$NON-NLS-1$
          System.out.println("Message: multiline tag found."); //$NON-NLS-1$
        }

        // try to match the tag of the instance of this class with the line
        tagMatcher = tagPattern.matcher(line);
        while(tagMatcher.find(offset)){
          String matchedTag = line.substring(tagMatcher.start(), tagMatcher.end());
          offset = tagMatcher.end();
          // tag open
          if(matchedTag.startsWith("<" + elementName)){ //$NON-NLS-1$
            elementDepth++;
            if(elementDepth == 1){
              targetElementStart = tagMatcher.start();
            }
          }
          // tag close
          else {
            elementDepth--;
            if(elementDepth == 0){
              taggedText = line.substring(targetElementStart, tagMatcher.end());
              rawText = taggedText.replaceAll("<[^>]+>", ""); //$NON-NLS-1$ //$NON-NLS-2$
              rexMatcher = rexPattern.matcher(rawText);
              if(rexMatcher.find()){
                resultRecord = new ResultRecord(fieldInfo);
                rexMatchOffset = rexMatcher.start();
                realOffset = getPosition(taggedText, updateRexMatchOffset(rexMatcher.start()));
                resultRecord.setPosition(p + targetElementStart + realOffset);
                resultRecord.set(fieldName, rawText.substring(rexMatcher.start(), rexMatcher.end()));
                return resultRecord;
              }
            }
          }
        }

        // when a start tag appears and the close tag does not.
        if(elementDepth > 0){
          offset = line.length();
          taggedText = line.substring(targetElementStart);
          rawText = taggedText.replaceAll("<[^>]+>", ""); //$NON-NLS-1$ //$NON-NLS-2$
          rexMatcher = rexPattern.matcher(rawText);
          if(rexMatcher.find()){
            resultRecord = new ResultRecord(fieldInfo);
            rexMatchOffset = rexMatcher.start();
            realOffset = getPosition(taggedText, updateRexMatchOffset(rexMatcher.start()));
            resultRecord.setPosition(p + targetElementStart + realOffset);
            resultRecord.set(fieldName, rawText.substring(rexMatcher.start(), rexMatcher.end()));
            return resultRecord;
          }
        }
        p += line.length() + 1; // add the length of LF
      }
    } catch (IOException ex) {
      ex.printStackTrace();
      return null;
    }

    try{
      if(br != null) br.close();
    } catch (IOException ex){
      ex.printStackTrace();
    }
    return null;
  }

  public ResultRecord retrieveNext() {
    ResultRecord resultRecord;

    // process for the rest of the current element
    if(rawText.length() >= rexMatchOffset && rexMatcher.find(rexMatchOffset)){
      resultRecord = new ResultRecord(fieldInfo);
      rexMatchOffset = rexMatcher.start();
      realOffset = getPosition(taggedText, updateRexMatchOffset(rexMatcher.start()));
      resultRecord.setPosition(p + targetElementStart + realOffset);
      resultRecord.set(fieldName, rawText.substring(rexMatcher.start(), rexMatcher.end()));
      return resultRecord;
    }

    // process for the rest of the current line
    while(tagMatcher.find(offset)){
     String matchedTag = line.substring(tagMatcher.start(), tagMatcher.end());
     offset = tagMatcher.end();
     // tag open
     if(matchedTag.startsWith("<" + elementName)){ //$NON-NLS-1$
       elementDepth++;
       if(elementDepth == 1){
         targetElementStart = tagMatcher.start();
       }
     }
     // tag close
     else {
       elementDepth--;
       if(elementDepth == 0){
         taggedText = line.substring(targetElementStart, tagMatcher.end());
         rawText = taggedText.replaceAll("<[^>]+>", ""); //$NON-NLS-1$ //$NON-NLS-2$
         rexMatcher = rexPattern.matcher(rawText);
         if(rexMatcher.find()){
           resultRecord = new ResultRecord(fieldInfo);
           rexMatchOffset = rexMatcher.start();
           realOffset = getPosition(taggedText, updateRexMatchOffset(rexMatcher.start()));
           resultRecord.setPosition(p + targetElementStart + realOffset);
           resultRecord.set(fieldName, rawText.substring(rexMatcher.start(), rexMatcher.end()));
           return resultRecord;
         }
       }
     }
   }

   p += line.length() + 1; // add the length of LF

   try{
     while ( (line = br.readLine()) != null) {
       while (checkLineFeedInTag(line) > 0) {
         line = line.concat(" " + br.readLine()); //$NON-NLS-1$
       }
       if (elementDepth > 0) {
         targetElementStart = 0; // head of the line
       }
       // try to match the tag of the instance of this class with the line
       offset = 0;
       tagMatcher = tagPattern.matcher(line);
       while(tagMatcher.find(offset)){
         String matchedTag = line.substring(tagMatcher.start(), tagMatcher.end());
         offset = tagMatcher.end();
         // tag open
         if(matchedTag.startsWith("<" + elementName)){ //$NON-NLS-1$
           elementDepth++;
           if(elementDepth == 1){
             targetElementStart = tagMatcher.start();
           }
         }
         // tag close
         else {
           elementDepth--;
           if(elementDepth == 0){
             taggedText = line.substring(targetElementStart, tagMatcher.end());
             rawText = taggedText.replaceAll("<[^>]+>", ""); //$NON-NLS-1$ //$NON-NLS-2$
             rexMatcher = rexPattern.matcher(rawText);
             if(rexMatcher.find()){
               resultRecord = new ResultRecord(fieldInfo);
               rexMatchOffset = rexMatcher.start();
               realOffset = getPosition(taggedText, updateRexMatchOffset(rexMatcher.start()));
               resultRecord.setPosition(p + targetElementStart + realOffset);
               resultRecord.set(fieldName, rawText.substring(rexMatcher.start(), rexMatcher.end()));
               return resultRecord;
             }
           }
         }
       }

       // when a start tag appears and the close tag does not.
       if(elementDepth > 0){
         offset = line.length();
         taggedText = line.substring(targetElementStart);
         rawText = taggedText.replaceAll("<[^>]+>", ""); //$NON-NLS-1$ //$NON-NLS-2$
         rexMatcher = rexPattern.matcher(rawText);
         if (rexMatcher.find()) {
           resultRecord = new ResultRecord(fieldInfo);
           rexMatchOffset = rexMatcher.start();
           realOffset = getPosition(taggedText, updateRexMatchOffset(rexMatcher.start()));
           resultRecord.setPosition(p + targetElementStart + realOffset);
           resultRecord.set(fieldName,
                            rawText.substring(rexMatcher.start(), rexMatcher.end()));
           return resultRecord;
         }
       }
       p += line.length() + 1; // add the length of LF
     }
   } catch (IOException ex) {
     ex.printStackTrace();
     return null;
   }

   try{
     if(br != null) br.close();
   } catch (IOException ex){
     ex.printStackTrace();
   }
   return null;
 }


  private int getPosition(String taggedText, int positionInRawText){
    boolean insideTagFlag = false;
    int nTarget = taggedText.length();
    int cc = 0; // current position without counting tags

    for(int i = 0; i < nTarget; i++){
      if (taggedText.charAt(i) == '<') {
        insideTagFlag = true;
      } else if (taggedText.charAt(i) == '>') {
        insideTagFlag = false;
      } else if (!insideTagFlag) {
        if(positionInRawText == cc){
          return i;
        } else {
          cc++;
        }
      }
    }
    return nTarget;
  }


  private int checkLineFeedInTag(String line){
    int len = line.length();
    int c = 0;

    for(int i = 0; i < len; i++){
      if(line.charAt(i) == '<'){
        c++;
      } else if(line.charAt(i) == '>'){
        c--;
      }
    }
    return c;
  }

  
  // attention: the return value is the old rexMatchOffset
  private int updateRexMatchOffset(int offset) {
	  int oldRexMatchOffset = rexMatchOffset++;
	  
	  if(Character.isSurrogate(rawText.charAt(offset))) {
		  rexMatchOffset++;
	  }
	  
	  return oldRexMatchOffset;
  }
  
  
  public void setRetrieveCondition(FieldInfo fieldInfo, String fieldName){
    this.fieldInfo = fieldInfo;
    this.fieldName = fieldName;
  }

  public boolean exists(){
    return true;
  }

  public String getFilename(){
    return NULL_INDEX_FILENAME; //$NON-NLS-1$
  }

  public int mkcix(String elementName){
    return 0;
  }

  public String getIOFilename()
  {
	  return null;
  }
}

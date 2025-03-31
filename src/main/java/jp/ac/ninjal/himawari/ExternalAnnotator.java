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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// 文の分割後，形態素解析し，その結果をデータベースに格納する

public class ExternalAnnotator extends Annotator {
	public static final String UNUSED_DIC_FIELDNAME_PREFIX = "_unused"; //$NON-NLS-1$
	
	private final String tmpFilenameBody = "_himawari_annotator"; //$NON-NLS-1$
	private final String tmpFilenameSuffix = ".tmp"; //$NON-NLS-1$
	
	private String command;
	private String commandOption;
	private String annotatorName;
	private Process process = null;
	private Pattern tabPattern = Pattern.compile("^\t+(.*?) "); //$NON-NLS-1$
	private ArrayList<String> headers;
	private int fnText = 0;
	private String targetElementName = ""; //$NON-NLS-1$
	private String targetAttributeName = ""; //$NON-NLS-1$
	private String targetAttributeValue = ""; //$NON-NLS-1$
	private String annotatorResultDelimitor = "[\t ,]"; //$NON-NLS-1$
	private String chunkDelimitor = "[。！？]"; //$NON-NLS-1$
	private int chunkMaxLength = 200;
	private String encodingAnnotator = System.getProperty("file.encoding"); //$NON-NLS-1$
	private ArrayList<String> indexedFields;
	
		
	public ExternalAnnotator(String annotatorName, String annotationName) {
		super(annotationName);
		this.annotatorName = annotatorName;
	}

	
	public void init(UserSettings settings){
		headers = new ArrayList<String>();
		setCommand(settings);
	}

	
	public void setCommand(UserSettings settings){
		try {
			// read settings from UserSettings
			String strXPathAnnotator = "/setting/annotator/li[@name='" + annotatorName + "']"; //$NON-NLS-1$ //$NON-NLS-2$

			List<Node> annotatorList = settings.evaluateNodes(strXPathAnnotator, true);
			if(annotatorList.size() > 1){
				System.err.println("Warning(ExternalAnnotator): duplicate entry (" + annotatorName + ") in the config file"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			Element elementCommand = (Element) annotatorList.get(0);
			command = elementCommand.getAttribute("command"); //$NON-NLS-1$
			System.err.println("command: " + command); //$NON-NLS-1$
			commandOption = elementCommand.getAttribute("option"); //$NON-NLS-1$
			System.err.println("option: " + commandOption); //$NON-NLS-1$
			encodingAnnotator = getAttribute(elementCommand, "encoding", encodingAnnotator); //$NON-NLS-1$

			
			Element elementExtract = getFirstElement(elementCommand, "extract"); //$NON-NLS-1$
			targetElementName = getAttribute(elementExtract, "element", targetElementName); //$NON-NLS-1$
			targetAttributeName = getAttribute(elementExtract, "attribute", targetAttributeName); //$NON-NLS-1$
			targetAttributeValue = getAttribute(elementExtract, "value", targetAttributeValue); //$NON-NLS-1$

			Element elementChunk = getFirstElement(elementCommand, "chunk"); //$NON-NLS-1$
			chunkDelimitor = getAttribute(elementChunk, "delimitor", chunkDelimitor); //$NON-NLS-1$
			if(!elementChunk.getAttribute("maxlength").isEmpty()){ //$NON-NLS-1$
				chunkMaxLength = Integer.parseInt(elementChunk.getAttribute("maxlength")); //$NON-NLS-1$
			}


			Element elementResultFields = getFirstElement(elementCommand, "result_fields"); //$NON-NLS-1$
			annotatorResultDelimitor = elementResultFields.getAttribute("delimitor"); //$NON-NLS-1$

			indexedFields = new ArrayList<String>();
			NodeList fields = elementResultFields.getElementsByTagName("li"); //$NON-NLS-1$
			for(int i = 0; i < fields.getLength(); i++){
				Element element = (Element) fields.item(i);
				String fieldName = element.getAttribute("name"); //$NON-NLS-1$
				String isIndexed = element.getAttribute("isIndexed"); //$NON-NLS-1$
				if(fieldName.equals(DBController.FIELD_ANNOTATION_SEARCH_KEY)){
					fnText = i;
				}
				headers.add(fieldName);
				
				if(isIndexed != null && isIndexed.equalsIgnoreCase("true")){ //$NON-NLS-1$
					indexedFields.add(fieldName);
				}
				
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
	private Element getFirstElement(Element element, String elementName){
		NodeList nodeList = element.getElementsByTagName(elementName);
		if(nodeList.getLength() > 0){
			return (Element) nodeList.item(0);
		} else {
			return null;
		}
	}
	

	private String getAttribute(Element element, String attributeName, String defaultValue){
		if(!element.hasAttribute(attributeName)){
			return defaultValue;
		} else {
			return element.getAttribute(attributeName);
		}
	}

	
	
	
	
	public void execute(CorpusFile corpus, File resultFile) throws IOException, InterruptedException, IllegalStateException {

		File targetXMLFile = new File(corpus.getBasename() + ".xml"); //$NON-NLS-1$

		// 文字コード変換
		// unicodelittle を必ず使う。utf-16, utf-16LE だと BOM の処理がない。
		System.err.println("trans"); //$NON-NLS-1$
		System.err.println("enc: " + System.getProperty("file.encoding")); //$NON-NLS-1$ //$NON-NLS-2$
		File outTrans = translateCharacterCode(targetXMLFile, "unicodelittle"); //$NON-NLS-1$
		System.err.println("extract: " + targetElementName); //$NON-NLS-1$
		File outExtract = extractText(outTrans, targetElementName,
				targetAttributeName, targetAttributeValue);
		outTrans.delete();

		
		// 文分割
		System.err.println("split"); //$NON-NLS-1$
		File outSplit = splitBy(outExtract, chunkDelimitor, chunkMaxLength);
		outExtract.delete();
		String osname = Util.getOSNameLowerCase();
		ProcessBuilder pb = null;
		ArrayList<String> commandArray = new ArrayList<String>();
		
		if (osname.startsWith("windows")) { //$NON-NLS-1$
			command = modifyPathOnWindows(command);
			commandArray.add("cmd"); //$NON-NLS-1$
			commandArray.add("/c"); //$NON-NLS-1$
			commandArray.add(command);
			commandArray.addAll(Arrays.asList(commandOption.split(" "))); //$NON-NLS-1$
			commandArray.add("<"); //$NON-NLS-1$
			commandArray.add(outSplit.getAbsolutePath());
		} else {
			commandArray.add("sh"); //$NON-NLS-1$
			commandArray.add("-c"); //$NON-NLS-1$
			commandArray.add(command + " " + commandOption + " < " + outSplit.getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$
		}

		System.err.println(command + " " + commandOption + " < " + outSplit.getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$
		pb = new ProcessBuilder(commandArray.toArray(new String[0]));

		pb.redirectErrorStream();
		process = pb.start();

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				process.getInputStream(), encodingAnnotator));

		BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile));

		// print the header
		writer.write(DBController.FIELD_ANNOTATION_START + "\t" //$NON-NLS-1$
				+ DBController.FIELD_ANNOTATION_END);
		for (String header : headers) {
			if (!header.startsWith(UNUSED_DIC_FIELDNAME_PREFIX)) {
				writer.write("\t" + header); //$NON-NLS-1$
			}
		}
		writer.write("\n"); //$NON-NLS-1$

		
		String line = null;
		int p = 1;
		while ((line = reader.readLine()) != null) {
			// attention!!!!!!!!!!!!!!!!!!!
			line = line.replaceAll(",", "，"); //$NON-NLS-1$ //$NON-NLS-2$
			int cp = p;
			// EOS
			if (line.equals("EOS")) { //$NON-NLS-1$
				continue;
			} // white space
			else if (line.startsWith("  ")) { //$NON-NLS-1$
				continue;
			} else {
				Matcher m = tabPattern.matcher(line);
				// tab
				if (m.find()) {
					p += m.group(1).length();
					continue;
				} // normal line
				else {
					// add \n for empty strings of the last field
					String[] fields = (line + "\n").split(annotatorResultDelimitor); //$NON-NLS-1$
					String lastField = fields[fields.length-1];
					fields[fields.length-1] = lastField.substring(0, lastField.length()-1); //chop
					StringBuilder sb = new StringBuilder();
					p += fields[fnText].length();
					sb.append(cp + "\t" + (p - 1)); //$NON-NLS-1$
					int i = 0;
					for (String header : headers) {
						if (!header.startsWith(UNUSED_DIC_FIELDNAME_PREFIX)) {
							sb.append("\t" + fields[i]); //$NON-NLS-1$
						}
						i++;
					}
					writer.write(sb.toString());
					writer.write("\n"); //$NON-NLS-1$
				}
			}
		}

		try {
			process.waitFor();
		} catch (InterruptedException e) {
			errorMessage = "Error(ExternalAnnotator): The annotator was terminated abnormally."; //$NON-NLS-1$
			System.err.println(errorMessage); //$NON-NLS-1$
			throw new InterruptedException(errorMessage);
		} finally{
			writer.close();
			reader.close();
		}
		
		int returnValue = process.exitValue();
		if (returnValue != 0) {
			System.err.println("exit status:" + process.exitValue()); //$NON-NLS-1$
			System.err.println("An external program is terminated abnormally. The process is aborted."); //$NON-NLS-1$
			errorMessage = Messages.getString("ExternalAnnotator.73"); //$NON-NLS-1$
			writer.close();
			reader.close();
			throw new IllegalStateException(errorMessage);
		}

		writer.close();
		reader.close();

		outSplit.delete();
	}
	
	
	public String getTargetElementName(){
		return targetElementName;
	}
	
	public String getTargetAttributeName(){
		return targetAttributeName;
	}
	
	public String getTargetAttributeValue(){
		return targetAttributeValue;
	}

	
	public ArrayList<String> getIndexedFields(){
		return indexedFields;
	}
	
	
	public File translateCharacterCode(File inputFile, String from) throws IOException{
		// create a temporary file
		File tmpFile = File.createTempFile(tmpFilenameBody, tmpFilenameSuffix);

		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), from));	
		BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile));

		// for BOM
		if(from.equalsIgnoreCase("utf-16le")){ //$NON-NLS-1$
			
		}
		
		String line = null;
		while ((line = reader.readLine()) != null) {
			writer.write(line);
			writer.write("\n"); //$NON-NLS-1$
		}
		reader.close();
		writer.close();
		
		return tmpFile;
	}
	
	
	/*
	 *  extract text tagged by targetTagName whose class name is targetClassName
	 */
	public File extractText(File inputFile, String targetTagName, String targetClassName, String targetClassValue) throws IOException{
		File tmpFile = File.createTempFile(tmpFilenameBody, tmpFilenameSuffix);

		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile));

		String line = null;
		Pattern patternTagOpen = Pattern.compile("^<" + targetTagName + "(>|\\s+.+?>)", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$ //$NON-NLS-2$
		String strTagClose = "</" + targetTagName + ">"; //$NON-NLS-1$ //$NON-NLS-2$
		int cInner = 0;
		if(targetTagName.isEmpty()){
			cInner = 1;
		}
		
		while ((line = reader.readLine()) != null) {
			boolean flagWrite = false;
			//TODO cat the next line if a partial tag exists
			if(!isTagCompleteStr(line)){
				String line2;
				while ((line2 = reader.readLine()) != null) {
					line += " " + line2; //$NON-NLS-1$
					if(isTagCompleteStr(line)){
						break;
					}
				}
			}

			while (!line.isEmpty()) {
				Matcher m = patternTagOpen.matcher(line);
				if(m.find()){
					line = m.replaceFirst(""); //$NON-NLS-1$
					String matchedTag = m.group();
					if(targetClassName.isEmpty()){
						cInner++;
					} else if(matchedTag.contains(" " + targetClassName + "=\"" + targetClassValue + "\"")){ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						cInner++;
					} else if(cInner != 0){
						cInner++;
					}
				} else if(line.startsWith(strTagClose)){
					line = line.substring(strTagClose.length());
					if(cInner > 0){
						cInner--;
					}
				} else if(line.startsWith("<")){ //$NON-NLS-1$
					int p = line.indexOf('>');
					line = line.substring(p+1);
				} else {
					int p = line.indexOf('<');
					if(p == -1){
						if(cInner > 0){
							writer.write(line);
							flagWrite = true;
						}
						break;
					} else {
						if(cInner > 0){
							writer.write(line.substring(0, p));
							flagWrite = true;
						}
						line = line.substring(p);
					}
				}
			}
			
			if(flagWrite){
				writer.write('\n');
			}
		}
		reader.close();
		writer.close();

		return tmpFile;
	}

	//
	// タグが完結している文字列かどうかを調べる
	//
	private boolean isTagCompleteStr(String str){
		int c = 0;

		for(int i = 0; i < str.length(); i++){
			if(str.charAt(i) == '<'){
				c++;
			} else if(str.charAt(i) == '>'){
				c--;
			}
		}
		
		if(c == 0){
			return true;
		} else {
			return false;
		}
	}

	
	/*
	 *  extract text tagged by targetTagName whose class name is targetClassName
	 */
	public File splitBy(File inputFile, String regexDelimiter, int maxLength) throws IOException{
		File tmpFile = File.createTempFile(tmpFilenameBody, tmpFilenameSuffix);

		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpFile), encodingAnnotator));

		Pattern patternDelimiter = Pattern.compile(regexDelimiter);
		String line = null;

		while ((line = reader.readLine()) != null) {
			Matcher m = patternDelimiter.matcher(line);
			line = m.replaceAll("$0\n").replaceFirst("\n*$", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			for(String fragment : line.split("\n")){ //$NON-NLS-1$
				while(true){
					if(fragment.length() > maxLength){
						writer.write(fragment.substring(0, maxLength));
						writer.write("\n"); //$NON-NLS-1$
						fragment = fragment.substring(maxLength);
					} else {
						writer.write(fragment);
						break;
					}
				}
				writer.write("\n"); //$NON-NLS-1$
			}
		}

		reader.close();
		writer.close();

		return tmpFile;
	}


	public String getErrorMessage(){
		return errorMessage;
	}

	private String modifyPathOnWindows(String path) {
		if(path.contains("\\Program Files\\")){ //$NON-NLS-1$
			File testFile1 = new File(path);
			File testFile2 = new File(path + ".exe"); //$NON-NLS-1$
			if(!testFile1.exists() && !testFile2.exists()){
				String newPath = path.replace("\\Program Files\\", "\\Program Files (x86)\\"); //$NON-NLS-1$ //$NON-NLS-2$
				testFile1 = new File(newPath);
				testFile1 = new File(newPath + ".exe"); //$NON-NLS-1$
				if(testFile1.exists() || testFile2.exists()){
					return newPath;
				}
			}
		}
		return path;
	}
}

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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.text.html.HTML;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import jp.ac.ninjal.himawari.HimawariLogger.HimawariListedLog;
import jp.ac.ninjal.himawari.HimawariLogger.HimawariLog;


public class TextFileImporter {

	static final String FILE_SEP = System.getProperty("file.separator"); //$NON-NLS-1$
	
	// template dir
	public static final String DIR_RESOURCE_TEMPLATE = "resources/template"; //$NON-NLS-1$
	// defalt configuration template file
	public static final String DEFAULT_CONFIG_TEMPLATE = "defaultConfig.xml"; //$NON-NLS-1$
	// template corpus.xml
	private static final String DEFAULT_CORPUS_PATH = DIR_RESOURCE_TEMPLATE + "/defaultCorpus.xml"; //$NON-NLS-1$

	private static final String STR_REPLACER = "%"; //$NON-NLS-1$
	// default css file
	private static final String XSLT_CSS = "kotobun_written.css"; //$NON-NLS-1$
	// default xsl file
	private static final String XSLT_XSL = "kotobun_written.xsl"; //$NON-NLS-1$
	// default xslt directory
	private static final String XSLT_DIR = "xslt"; //$NON-NLS-1$
	
	// status
	public static final int STATUS_OK = 1;
	public static final int STATUS_WARN = 2;
	public static final int STATUS_ERROR = 3;
	public static final int STATUS_CANCEL = 4;

	// default character conversion table (from)
	private static final String DEFALUT_CHAR_CONV_FROM = "<>&"; //$NON-NLS-1$
	// default character conversion table (to)
	private static final String DEFALUT_CHAR_CONV_TO = "＜＞＆"; //$NON-NLS-1$
	
	/**
	 * properties
	 */
	// path to source directory
	private String srcDirPath;
	// selected options
	private String extArr[];
	// text normalization option
	private String optTextNorm;
	// selected text conversion rule
	private String optTxtStyle;
	
	// 選択されたスタイルシートオプション(XHTML)
	private String optXhtmlStyle;
	private boolean optXhtmlIsConvertHtml;
	// 選択されたスタイルシートオプション(XML)
	private String optXmlStyle;
	// 入力された出力コーパス名
	private String outputName;
	// corporaのパス
	private String CorporaRootPath = "./Corpora/"; //$NON-NLS-1$
	// コンフィグファイル名(メモのみ)
	private String configFileName;
	// コンフィグファイル名(DB含む)
	private String configFileNameDB;
	
	// インポート時に複数のコーパスを対象とするか
	private boolean optConstuctIncludeSubcorpora;
	private String annotatorName;
	
	// logs
	private HimawariListedLog successfulFilesLog = new HimawariListedLog(Messages.getString("TextFileImporter.1")); //$NON-NLS-1$
	private HimawariListedLog failedFilesLog = new HimawariListedLog(HimawariLog.MODE_ERROR, Messages.getString("TextFileImporter.2")); //$NON-NLS-1$
	private HimawariListedLog summaryLog = new HimawariListedLog(Messages.getString("TextFileImporter.3")); //$NON-NLS-1$

	// status
	private int status = STATUS_OK;

	public int getStatus() {
		return status;
	}

	// インポート成功ファイル数
	public int importedCount = 0;
	// インポート失敗（スキップ）ファイルすう
	public int failedCount = 0;

	// コーパスDirectory存在フラグ
	private boolean aleadyExistCorpusDir = false;

	// 出力ディレクトリ
	private Path corpusDir;

	// テキスト処理用
	private StringBuilder resultText = new StringBuilder();
	private StringBuilder tempText = new StringBuilder();
	
	// ユーザ設定
	private UserSettings userSettings;
	private String charConvTableFrom; 
	private String charConvTableTo; 

	// インポート設定
	private UserSettings importConfig;
	
	// Template Config file
	private String optTemplateConfigFile;
	
	private JDialog dialog;
	
	/**
	 * コンストラクタ
	 *
	 * @param parent
	 */
	public TextFileImporter(JDialog dialog, UserSettings userSettings) {
		this.userSettings = userSettings;
		this.dialog = dialog;
	}

	
	public HimawariListedLog getSuccessfulFilesLog(){
		return successfulFilesLog;
	}

	
	public HimawariListedLog getFailedFilesLog(){
		return failedFilesLog;
	}

	
	public void setSummaryLog(HimawariListedLog log){
		summaryLog = log;
	}

	public HimawariListedLog getSummaryLog(){
		return summaryLog;
	}

	/**
	 * インポートの実行
	 *
	 * @param srcDir
	 * @param extArr
	 * @param optTextKaigyo
	 * @param optTextFurigana
	 * @param optTextChu
	 * @param optXhtmlStyle
	 * @param optXhtmlIsConvertHtml
	 * @param outputName
	 * @return
	 * @throws Exception
	 */
	public String execute(String srcDir, String[] extArr,
			String optTextNorm, String optTxtStyle, String optXhtmlStyle,
			boolean optXhtmlIsConvertHtml, String optXmlStyle, String outputName,
			boolean optConstuctIncludeSubcorpora,
			String annotatorName,
			UserSettings importConfig,
			String optTemplateConfigFile) throws Exception {
		this.srcDirPath = srcDir;
		this.extArr = extArr;
		this.optTextNorm = optTextNorm;
		this.optTxtStyle = optTxtStyle;
		this.optXhtmlStyle = optXhtmlStyle;
		this.optXhtmlIsConvertHtml = optXhtmlIsConvertHtml;
		this.optXmlStyle = optXmlStyle;
		this.outputName = outputName;
		this.optConstuctIncludeSubcorpora = optConstuctIncludeSubcorpora;
		this.annotatorName = annotatorName;
		this.importConfig = importConfig;
		this.optTemplateConfigFile = optTemplateConfigFile;

		charConvTableFrom = userSettings.evaluateOneNode2("/setting/import/char_convertion_table/@from", false) + DEFALUT_CHAR_CONV_FROM; //$NON-NLS-1$
		charConvTableTo = userSettings.evaluateOneNode2("/setting/import/char_convertion_table/@to", false) + DEFALUT_CHAR_CONV_TO; //$NON-NLS-1$
		if(charConvTableFrom.length() != charConvTableTo.length()){
			System.err.println("Warning: the length of FROM is not the same as the one of TO"); //$NON-NLS-1$
			System.err.println("from:" + charConvTableFrom); //$NON-NLS-1$ //$NON-NLS-2$
			System.err.println("to  :" + charConvTableTo); //$NON-NLS-1$ //$NON-NLS-2$
			throw new Exception("Warning: the length of FROM is not the same as the one of TO"); //$NON-NLS-1$
		}

		return makeCorpus();

	}

	/**
	 * Make a corpus
	 *
	 * @throws Exception
	 */
	public String makeCorpus() throws Exception {
		try {
			// corpora dir
			Path corporaDir = Paths.get(CorporaRootPath);
			if (!Files.exists(corporaDir)) {
				Files.createDirectory(corporaDir);
			}

			// corpus dir
			corpusDir = corporaDir.resolve(outputName); //$NON-NLS-1$
			if (!Files.exists(corpusDir)) {
				Files.createDirectory(corpusDir);
				summaryLog.add(
						Messages.getString("TextFileImporter.4") //$NON-NLS-1$
						+ corpusDir.normalize().toAbsolutePath());
			} else {
				// overwrite ok?
				int choice = JOptionPane.showConfirmDialog(dialog, Messages
						.format(Messages.getString("TextFileImporter.39"), outputName), Messages //$NON-NLS-1$
						.getString(Messages.getString("TextFileImporter.40")), JOptionPane.YES_NO_OPTION); //$NON-NLS-1$
				if (choice == JOptionPane.NO_OPTION) {
					status = STATUS_CANCEL;
					return null;
				} else {
					if(userSettings.includeSDB()){
						System.runFinalization();
						System.gc();
					}
					
					// delete all files
					deleteFiles(corpusDir);
					Files.createDirectories(corpusDir);
				}
				aleadyExistCorpusDir = true;
				summaryLog.add(
						Messages.getString("TextFileImporter.8") //$NON-NLS-1$
						+ corpusDir.normalize().toAbsolutePath());
			}

			// path normalization
			srcDirPath = srcDirPath.replaceAll("\\"+FILE_SEP, "/"); //$NON-NLS-1$ //$NON-NLS-2$
			// read the template corpus file
			String strDefaultCorpus = readFile(DEFAULT_CORPUS_PATH, "UTF-16"); //$NON-NLS-1$

			File srcDir = new File(srcDirPath);
			File[] childDirs = getChildDirs(srcDir);
			List<String> corpusNameList = new ArrayList<String>(); 
			
			if(optConstuctIncludeSubcorpora && childDirs.length > 1){
				for (File dir : childDirs) {
					corpusNameList.add(makeCorpusXML(dir, strDefaultCorpus, false));
				}
			} else {
				corpusNameList.add(makeCorpusXML(srcDir, strDefaultCorpus, true));
			}

			if(importConfig != null) // use user's config files instead of generated ones
			{
				// Copy misc files
				String strMiscDir = importConfig.evaluateOneNode2("/setting/import/source_files/@misc_dir", false); //$NON-NLS-1$
				if(strMiscDir != null){
					Files.copy(
							Paths.get(srcDirPath + "/../" + strMiscDir), //$NON-NLS-1$ 
							Paths.get("./" + corpusDir + "/" + strMiscDir)); //$NON-NLS-1$ //$NON-NLS-2$
				}

				// config files
				String strConfigFile = importConfig.evaluateOneNode2("/setting/import/source_files/@config_file1", false); //$NON-NLS-1$
				String strConfigFile2 = importConfig.evaluateOneNode2("/setting/import/source_files/@config_file2", false); //$NON-NLS-1$
				if(strConfigFile != null && !strConfigFile.isEmpty()){
					configFileName = srcDirPath + "/../" + strConfigFile; //$NON-NLS-1$
					Files.copy(
							Paths.get(configFileName),
							Paths.get("./" + strConfigFile), StandardCopyOption.REPLACE_EXISTING); //$NON-NLS-1$
					configFileName = strConfigFile;
				} else {
					System.err.println("Warning(TextFileImporter): No configuration file was generated"); //$NON-NLS-1$
					// do nothing
				}
				if(strConfigFile2 != null && !strConfigFile2.isEmpty()){
					configFileNameDB = srcDirPath + "/../" + strConfigFile2; //$NON-NLS-1$
					Files.copy(
							Paths.get(configFileNameDB),
							Paths.get("./" + strConfigFile2), StandardCopyOption.REPLACE_EXISTING); //$NON-NLS-1$
				}

				
				String strXsltDir = importConfig.evaluateOneNode2("/setting/import/source_files/@xslt_dir", false); //$NON-NLS-1$
				if(strXsltDir != null){
					Files.copy(
							Paths.get(srcDirPath + "/../" + strXsltDir), //$NON-NLS-1$
							Paths.get("./" + corpusDir + "/xslt")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			} else {
				configFileName = UserSettings.CONFIG_FILE_BODY + outputName + UserSettings.CONFIG_FILE_SUFFIX;
				configFileNameDB  = UserSettings.CONFIG_FILE_BODY + outputName + UserSettings.CONFIG_FILE_SUFFIX_SDB;

				// mkdir xslt dir
				Path xsltDir = corpusDir.resolve("xslt"); //$NON-NLS-1$
				if (!Files.exists(xsltDir)) {
					Files.createDirectory(xsltDir);
				}

				// copy kotobun_written.xsl
				Path xsltXslFilePath = xsltDir.resolve(XSLT_XSL); //$NON-NLS-1$
				Files.copy(Paths.get(DIR_RESOURCE_TEMPLATE + "/" + XSLT_XSL), xsltXslFilePath); //$NON-NLS-1$

				// copy kotobun_written.css
				Path xsltCssFilePath = xsltDir.resolve(XSLT_CSS); //$NON-NLS-1$
				Files.copy(Paths.get(DIR_RESOURCE_TEMPLATE + "/" + XSLT_CSS), xsltCssFilePath); //$NON-NLS-1$

				// make config files
				UserSettings newConfig = new UserSettings();
				newConfig.init(optTemplateConfigFile, false);
		
				Document doc = newConfig.doc;
				
				// set corpus list
				Element corporaList =(Element) doc.getElementsByTagName("corpora").item(0); //$NON-NLS-1$
				corporaList.setAttribute("name", outputName); //$NON-NLS-1$
				corporaList.setAttribute("dppath", "Corpora/" + outputName); //$NON-NLS-1$ //$NON-NLS-2$
				for(String corpusName : corpusNameList){
					Element item = doc.createElement("li"); //$NON-NLS-1$
					item.setAttribute("name", corpusName); //$NON-NLS-1$
					item.setAttribute("path", "Corpora/" + corpusName + "/corpus"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					corporaList.appendChild(item);
				}
				
				// set root_path attribute of xsl_files element
				Element xslFilesElement =(Element) doc.getElementsByTagName("xsl_files").item(0); //$NON-NLS-1$
				xslFilesElement.setAttribute("root_path", "Corpora/" + outputName + "/" + XSLT_DIR); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				
				// output default config.xml
				newConfig.save(configFileName);

				// additional annotation?
				if(annotatorName != TextFileImportDialog.NOT_USE_ANNOTATOR){
					Element fieldSettings =(Element) doc.getElementsByTagName("field_setting").item(0); //$NON-NLS-1$
					Element aixSettings =(Element) doc.getElementsByTagName("index_aix").item(0); //$NON-NLS-1$
					Element resultFields = null;
					String elementName = ""; //$NON-NLS-1$

					// get annotation settings
					List<Node> annotatorList = userSettings.evaluateNodes("//annotator/li[@name=\"" + annotatorName + "\"]", true); //$NON-NLS-1$ //$NON-NLS-2$
					resultFields = (Element)((Element)annotatorList.get(0)).getElementsByTagName("result_fields").item(0); //$NON-NLS-1$
					elementName = ((Element)annotatorList.get(0)).getAttribute("annotation"); //$NON-NLS-1$
					
					if(resultFields == null || elementName.isEmpty()){
						
					} else {
						NodeList fieldNodes = resultFields.getChildNodes();
						HashMap<String, Integer> contextLengthMap = new HashMap<String, Integer>();
						for(int i = 0; i < fieldNodes.getLength(); i++){
							if(fieldNodes.item(i).getNodeType() == Node.ELEMENT_NODE){
								Element fieldElement = (Element)fieldNodes.item(i);
								
								// fieldSetting
								if(!fieldElement.getAttribute("name").equals(SIXDic.FIELD_ANNOTATION_SEARCH_KEY) //$NON-NLS-1$
										&& !fieldElement.getAttribute("name").startsWith(ExternalAnnotator.UNUSED_DIC_FIELDNAME_PREFIX)){ //$NON-NLS-1$
									Element newItem = doc.createElement("li"); //$NON-NLS-1$
									newItem.setAttribute("name", fieldElement.getAttribute("name")); //$NON-NLS-1$ //$NON-NLS-2$
									newItem.setAttribute("type", "dic"); //$NON-NLS-1$ //$NON-NLS-2$
									newItem.setAttribute("element", elementName); //$NON-NLS-1$
									newItem.setAttribute("attribute", fieldElement.getAttribute("name")); //$NON-NLS-1$ //$NON-NLS-2$
									newItem.setAttribute("width", "80"); //$NON-NLS-1$ //$NON-NLS-2$
									fieldSettings.appendChild(newItem);
								}
								
								// contextLength option
								if(fieldElement.hasAttribute("contextLength")){ //$NON-NLS-1$
									contextLengthMap.put(fieldElement.getAttribute("name"), Integer.parseInt(fieldElement.getAttribute("contextLength"))); //$NON-NLS-1$ //$NON-NLS-2$
								}
								
								// aix_index
								if(!fieldElement.hasAttribute("isCompleteMatch")){ //$NON-NLS-1$
									continue;
								} else {
									Element newAixItem = doc.createElement("li"); //$NON-NLS-1$
									if(fieldElement.getAttribute("name").equals(SIXDic.FIELD_ANNOTATION_SEARCH_KEY)){ //$NON-NLS-1$
										newAixItem.setAttribute("label", SIXDic.LABEL_SEARCH_KEY); //$NON-NLS-1$
									} else {
										newAixItem.setAttribute("label", fieldElement.getAttribute("name")); //$NON-NLS-1$ //$NON-NLS-2$
									}
									newAixItem.setAttribute("name", elementName); //$NON-NLS-1$
									newAixItem.setAttribute("middle_name", "dummy"); //$NON-NLS-1$ //$NON-NLS-2$
									newAixItem.setAttribute("argument", fieldElement.getAttribute("name")); //$NON-NLS-1$ //$NON-NLS-2$
									newAixItem.setAttribute("type", "dic"); //$NON-NLS-1$ //$NON-NLS-2$
									newAixItem.setAttribute("field_name", "キー"); //$NON-NLS-1$ //$NON-NLS-2$
									if(fieldElement.getAttribute("isCompleteMatch").equalsIgnoreCase("true")){ //$NON-NLS-1$ //$NON-NLS-2$
										newAixItem.setAttribute("isCompleteMatch", "true"); //$NON-NLS-1$ //$NON-NLS-2$
									} else {
										newAixItem.setAttribute("isCompleteMatch", "false"); //$NON-NLS-1$ //$NON-NLS-2$
									}
									aixSettings.appendChild(newAixItem);
								}
							}
						}
						
						// fieldSetting
						for(Entry<String, Integer> e : contextLengthMap.entrySet()) {
							int contextLength = e.getValue();
							String tAttribute = e.getKey();
							String tLabel = tAttribute.equals(SIXDic.FIELD_ANNOTATION_SEARCH_KEY) ? SIXDic.LABEL_SEARCH_KEY : tAttribute;
							
							for(int i = contextLength * -1; i <= contextLength; i++){
								if(i == 0){
									continue;
								}
								Element newItem = doc.createElement("li"); //$NON-NLS-1$
								newItem.setAttribute("name", tLabel + i); //$NON-NLS-1$
								newItem.setAttribute("type", "dic"); //$NON-NLS-1$ //$NON-NLS-2$
								newItem.setAttribute("element", elementName + "[" + i + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								newItem.setAttribute("attribute", tAttribute); //$NON-NLS-1$
								newItem.setAttribute("width", "80"); //$NON-NLS-1$ //$NON-NLS-2$
								fieldSettings.appendChild(newItem);
							}
						}
					}
					
					newConfig.save(configFileNameDB);
					summaryLog.add(
							configFileName + ", " + configFileNameDB //$NON-NLS-1$
							+ String.format(Messages.getString("TextFileImporter.53"), configFileNameDB)); //$NON-NLS-1$
					configFileName = configFileNameDB;
				} else {
					summaryLog.add(
							configFileName
							+ String.format(Messages.getString("TextFileImporter.53"), configFileName)); //$NON-NLS-1$
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return configFileName;
	}

	
	public String makeCorpusXML(File srcDir, String strDefaultCorpus, boolean isTopDir) throws Exception {
		String subCorpusName = srcDir.getName();
		Path subCorpusDir = corpusDir;
		if(!isTopDir){
			subCorpusDir = corpusDir.resolve(subCorpusName);
		}
		
		// corpus file
		Path outFileCorpus = subCorpusDir.resolve("corpus.xml"); //$NON-NLS-1$
		// temporary corpus file
		Path tmpFile = Files.createTempFile("edamame", "tmp"); //$NON-NLS-1$ //$NON-NLS-2$
		System.err.println("outfile: "  + outFileCorpus.toString()); //$NON-NLS-1$

		if (!Files.exists(subCorpusDir) && !isTopDir) {
			Files.createDirectory(subCorpusDir);
		}

		// corpus name
		String newOutputName = outputName;
		if(!isTopDir){
			newOutputName = outputName + "/" + subCorpusName; //$NON-NLS-1$
		}
		// get the content of the corpus
		String corpusContent = strDefaultCorpus.replaceAll(
				getReplacer("corpus"), //$NON-NLS-1$
				Matcher.quoteReplacement(newOutputName));
		corpusContent = corpusContent.replaceAll(
				getReplacer("srcPath"), //$NON-NLS-1$
				Matcher.quoteReplacement(srcDir.getCanonicalPath().replaceAll("\\"+FILE_SEP, "/"))); //$NON-NLS-1$ //$NON-NLS-2$

		// put the present date on the corpus file
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
		String strDate = sdf.format(new Date());
		corpusContent = corpusContent.replaceAll(getReplacer("date"), //$NON-NLS-1$
				strDate);

		// write the corpus file as tmpFile
		PrintWriter pwCorpus = new PrintWriter(new OutputStreamWriter(Files.newOutputStream(tmpFile), "x-UTF-16LE-BOM")); //$NON-NLS-1$
		int pEndOfHeader = corpusContent.indexOf(getReplacer("contents")); //$NON-NLS-1$
		if(pEndOfHeader != -1){
			pwCorpus.print(corpusContent.substring(0,pEndOfHeader));
			writeCorpusContent(pwCorpus, srcDir.getCanonicalPath());
			pwCorpus.print(corpusContent.substring(pEndOfHeader+getReplacer("contents").length())); //$NON-NLS-1$
		}
		pwCorpus.close();

		// throw an exception if no files were imported
		if (importedCount == 0) {
			throw new Exception(Messages.getString("TextFileImporter.64")); //$NON-NLS-1$
		}

		// copy the generated corpus file to the corpus directory
		Files.move(tmpFile, outFileCorpus, StandardCopyOption.REPLACE_EXISTING);
		
		validate(outFileCorpus.toFile());

		return(newOutputName); 
	}
	
	
	
	/**
	 * Delete created files for import failure
	 */
	public void deleteCreatedFiles() {
		// do nothing if corpus dir has already existed
		if (aleadyExistCorpusDir) {
			return;
		}

		try {
			// delete config files
			Files.deleteIfExists(Paths.get(configFileName));
			Files.deleteIfExists(Paths.get(configFileNameDB));

			// delete outdir
			deleteFiles(corpusDir);
		} catch (IOException e) {
			e.printStackTrace();
		}

		
	}

	/**
	 * Delete files recursively
	 *
	 * @param p
	 * @throws IOException 
	 */
	private void deleteFiles(Path p) throws IOException {
		Files.walkFileTree(p, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file,
					java.nio.file.attribute.BasicFileAttributes attrs)
					throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException e)
					throws IOException {
	             if (e == null) {
	                 Files.delete(dir);
	                 return FileVisitResult.CONTINUE;
	             } else {
	                 // directory iteration failed
	                 throw e;
	             }
			}
		});
	}


	/**
	 * コーパスのコンテンツを追加する
	 *
	 * @param strBuf
	 * @param dirPath
	 * @throws IOException
	 * @throws TransformerException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	private void writeCorpusContent(PrintWriter pw, String dirPath)
			throws IOException, ParserConfigurationException, SAXException,
			TransformerException {
		// 対象ディレクトリ
		File srcdir = new File(dirPath);

		// 対象ファイル拡張子判別用パターン
		Pattern fileNamePattern = Pattern.compile("\\.(" //$NON-NLS-1$
				+ StringUtils.join(extArr, "|") + ")$", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$ //$NON-NLS-2$
		String txtFileSuffixRegex = "(?i)^.*\\.(" + StringUtils.join(TextFileImportDialog.TXT_SUFFIXES, "|") + ")$"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String xhtmlFileSuffixRegex = "(?i)^.*\\.(" + StringUtils.join(TextFileImportDialog.XHTML_SUFFIXES, "|") + ")$"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String xmlFileSuffixRegex = "(?i)^.*\\.(" + StringUtils.join(TextFileImportDialog.XML_SUFFIXES, "|") + ")$"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// 子ファイルの処理
		File[] files = getChildFiles(srcdir, fileNamePattern);
		
		for (File file : files) {
			String defaultTxt = "<記事 タイトル=\"%title%\" 著者=\"\" path=\"%path%\" 備考=\"transDataType:テキスト\">\n" //$NON-NLS-1$
					+ "<テキスト>\n" + "%body%" + "</テキスト>\n</記事>\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			String baseName = file.getName();
			String title = getPreffix(baseName);
			String text = ""; //$NON-NLS-1$
			String relativePath = "/" + getCorpusRelativePath(file) + "/" + baseName; //$NON-NLS-1$ //$NON-NLS-2$

			System.err.println("filename: " + baseName); //$NON-NLS-1$
			
			try {

				// txtファイル
				if (baseName.matches(txtFileSuffixRegex)) {
					
					String converted = txt2doc(file);
					if (converted.length() > 0) {
						text = defaultTxt.replaceAll(getReplacer("title"), Matcher.quoteReplacement(title)); //$NON-NLS-1$
						text = text.replaceAll(getReplacer("path"), Matcher.quoteReplacement(relativePath)); //$NON-NLS-1$
						text = text.replaceAll(getReplacer("body"), Matcher.quoteReplacement(converted)); //$NON-NLS-1$
					}
					if (text.length() > 0) {
						pw.print(text + "\n"); //$NON-NLS-1$
						importedCount++;
					}
				}
				// xmlファイル
				else if (baseName.matches(xmlFileSuffixRegex) || baseName.matches(xhtmlFileSuffixRegex)) {

					if(baseName.matches(xmlFileSuffixRegex)){ //$NON-NLS-1$
						text = openTextFile(file);
						
					} else {
						text = openHtmlFile(file);
					}

					text = regReplace("(?mi)<\\?\\s?xml((?:\n|\r|.)*?)>\r?\n?", //$NON-NLS-1$
							text, ""); //$NON-NLS-1$
					text = regReplace("(?mi)<!DOCTYPE((?:\n|\r|.)*?)>\r?\n?", //$NON-NLS-1$
							text, ""); //$NON-NLS-1$
					// 改行コード統一
					text = text.replaceAll("\r\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
					// remove BOM
					text = text.replaceFirst("^\uFEFF", "").replaceFirst("^\uFFFE", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					

					// htmlファイル
					if (baseName.matches(xhtmlFileSuffixRegex)) {
							text = htm2doc(text);
						if (text.length() > 0) {
							text = regReplace("(?mi)(<記事[^>]+)>", text, //$NON-NLS-1$
									"$1 path=\""
									+ Matcher.quoteReplacement(relativePath) //$NON-NLS-1$
									+ "\">"); //$NON-NLS-1$
						}
					} else 	if (baseName.matches(xmlFileSuffixRegex)) {
						// The following code removes generated xml declaration by xml2doc.
						// Reccommand to use <xsl:output method="xml" omit-xml-declaration = "yes" /> in XSL files
						text = regReplace("(?mi)<\\?\\s?xml((?:\n|\r|.)*?)>\r?\n?", //$NON-NLS-1$
								xml2doc(text), ""); //$NON-NLS-1$
					}
					
					if (text.length() > 0) {
						pw.print(text + "\n"); //$NON-NLS-1$
						importedCount++;
					}
				}
				successfulFilesLog.add(file.getName());
			} catch (SAXParseException se) {
				failedFilesLog.add(
						file.getName()
						+ " (" + se.getLineNumber() + ", " + se.getColumnNumber() + ")" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						+ ", " + StringEscapeUtils.escapeXml(se.getMessage())); //$NON-NLS-1$
				failedCount++;
			} catch (TransformerException te) {
				failedFilesLog.add(
						file.getName()
						+ ", " + StringEscapeUtils.escapeXml(te.getMessageAndLocation())); //$NON-NLS-1$
				failedCount++;
			}
		}
		
		// フォルダ再帰処理
		File[] dirs = getChildDirs(srcdir);
		for (File dir : dirs) {
			writeCorpusContent(pw, dir.getPath());
		}

		return;
	}

	/**
	 * コーパスファイルのルートディレクトリからの相対パスを取得する
	 * @param file
	 * @return
	 */
	private String getCorpusRelativePath(File file) {
		File srcDir = new File(srcDirPath);
		String rootPath = srcDir.getParent();
		String relPath = file.getParent().replace(rootPath+FILE_SEP, ""); //$NON-NLS-1$
		return relPath.replace(FILE_SEP, "/"); //$NON-NLS-1$
	}

	/**
	 * 子ディレクトリの取得
	 *
	 * @param dir
	 * @return
	 */
	private File[] getChildDirs(File dir) {

		List<File> dirList = new ArrayList<File>();

		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				dirList.add(files[i]);
			}
		}
		return (File[]) dirList.toArray(new File[0]);
	}

	/**
	 * 子ファイルの取得
	 *
	 * @param dir
	 * @return
	 */
	private File[] getChildFiles(File dir, Pattern pattern) {
		List<File> fileList = new ArrayList<File>();

		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isFile()) {
				if(file.getName().startsWith(".")){ //$NON-NLS-1$
					System.err.println("Warning(TextFileImporter): Files whose name begins with \".\" are not imported ==> " + file.getName()); //$NON-NLS-1$
					continue;
				}
				Matcher matcher = pattern.matcher(file.getName());
				if (matcher.find()) {
					fileList.add(file);
				}
			}
		}
		return (File[]) fileList.toArray(new File[0]);
	}

	
	/**
	 * テキストファイルのXML化
	 *
	 * @param file
	 * @throws IOException
	 */
	private String txt2doc(File file) throws IOException {
		String text = openTextFile(file);

		// remove BOM
		text = text.replaceFirst("^\uFEFF", "").replaceFirst("^\uFFFE", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		// 改行コード統一
		text = text.replace("\r\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		// EOF除去
		// text = regReplace("\\cZ",text, "");//えだまめのコード
		text = regReplace("\\z", text, "");// ↑が怪しいので修正 //$NON-NLS-1$ //$NON-NLS-2$

		// 文字正規化(plain textの場合だけ，変換前に正規化する)
		text = normalizePlainText(text);

		// テキスト変換
		if(optTxtStyle != null){
			text = textTransformation(text, optTxtStyle);
		}
		
		return text;
	}


	/**
	 *
	 * XHTMLファイルのXML化
	 *
	 * @param file
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws TransformerException
	 */
	private String htm2doc(String text) throws IOException,
			ParserConfigurationException, SAXException, TransformerException {
//		text = regReplace("(?i)<!DOCTYPE((?:\n|\r|.)*?)>\r?\n?", text, ""); //$NON-NLS-1$ //$NON-NLS-2$
//		text = "<?xml version=\"1.0\" encoding=\"utf-8\" xml:space=\"preserve\" ?>" + text;

		text = regReplace("&nbsp;", text, "&#160;"); //$NON-NLS-1$ //$NON-NLS-2$
		text = regReplace("&copy;", text, "&#169;"); //$NON-NLS-1$ //$NON-NLS-2$

		File tmpfile = File.createTempFile("errfile", "tmp"); //$NON-NLS-1$ //$NON-NLS-2$
		tmpfile.deleteOnExit();
		
		// htmをxhtmlに変換
		try {
			xmlParse(text, tmpfile, false);
		} catch (SAXParseException e) {
			System.err.println("Warning(TextFileImporter): found parse error and retry."); //$NON-NLS-1$
			if (optXhtmlIsConvertHtml) {
				text = htm2xhtml(text);
			}
		}

		// 文字正規化
		text = normalizeTaggedText(text);
		
		if (optXhtmlStyle != null) {
			// xmlのパース
			try {
				xmlParse(text, tmpfile);
			} catch (SAXParseException e) {
				e.printStackTrace();
				throw e;
			}

			InputStream xmlIs = new ByteArrayInputStream(text.getBytes("UTF-8"));; //$NON-NLS-1$

			// xsltスタイルシート
			InputStream stylIs = null;
			String xmltext = ""; //$NON-NLS-1$

			try {
				File xslFile = new File(optXhtmlStyle);
				if (xslFile.exists()) {
					stylIs = new FileInputStream(xslFile);
					xmltext = openTextFile(xslFile);
				}
				xmlParse(xmltext, tmpfile, false);
			} catch (SAXParseException e) {
				throw e;
			}

			// XSLTとXML文書をスタイルの変換を行う
			// 出力用一時ファイル
			File tmpResultFile = File.createTempFile("edamame", "tmp"); //$NON-NLS-1$ //$NON-NLS-2$
			tmpResultFile.deleteOnExit();

			xmlStyleCovert(xmlIs, stylIs, tmpResultFile);
			String resultText = openTextFile(tmpResultFile);
			tmpResultFile.delete();
			
			return resultText;
		} else {
			return "<記事 ><テキスト>\n" + text + "</テキスト></記事>\n"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		
	}

	
	private String normalizeTaggedText(String text){
		resultText.setLength(0);
		tempText.setLength(0);
		CharNormalizer cn = null;

		// 文字参照の文字をUnicodeへ
//		text = StringEscapeUtils.unescapeHtml4(text);

		if(optTextNorm.equals(TextFileImportDialog.TEXT_NORMALIZATION_TYPE_TABLE)){
			cn = new CharNormalizerByTable(charConvTableFrom, charConvTableTo);
		} else if(optTextNorm.equals(TextFileImportDialog.TEXT_NORMALIZATION_TYPE_NFKC)){
			cn = new CharNormalizerByNFKC();
		} else {
			// do nothing
			return text;
		}
		
		int cTag = 0;
		for(int i = 0; i < text.length(); i++){
			char c = text.charAt(i);
			if(c == '<'){
				// タグのエラー処理は省略
				cTag++;
				if(tempText.length() > 0){
					// HTML4の実体参照文字を Unicode 文字化 -> XML 実体参照化 -> 全角で文字化
					// NFKC などでは，XML で <>& が ASCII に変換される可能性があるために上記の処理を行う
					// 同じ処理を最後にするので，変更する場合は注意
					resultText.append(StringEscapeUtils.escapeXml(cn.execute(StringEscapeUtils.unescapeHtml4(tempText.toString()))).
							replace("&lt;", "＜").replace("&gt;", "＞").replace("&amp;", "＆")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
					tempText.setLength(0);
				}
				resultText.append(c);
			} else if(c == '>'){
				cTag--;
				resultText.append(c);
			} else if(cTag > 0){
				resultText.append(c);
			} else {
				tempText.append(c);
			}
		}
		
		if(tempText.length() != 0){
			resultText.append(StringEscapeUtils.escapeXml(cn.execute(StringEscapeUtils.unescapeHtml4(tempText.toString()))).
					replace("&lt;", "＜").replace("&gt;", "＞").replace("&amp;", "＆")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		}
		return resultText.toString();
	}

	
	private String normalizePlainText(String text){
		resultText.setLength(0);
		tempText.setLength(0);
		CharNormalizer defaultCharNormalizer = new CharNormalizerByTable(DEFALUT_CHAR_CONV_FROM, DEFALUT_CHAR_CONV_TO);
		CharNormalizer cn = null;
		
		if(optTextNorm.equals(TextFileImportDialog.TEXT_NORMALIZATION_TYPE_TABLE)){
			cn = new CharNormalizerByTable(charConvTableFrom, charConvTableTo);
		} else if(optTextNorm.equals(TextFileImportDialog.TEXT_NORMALIZATION_TYPE_NFKC)){
			cn = new CharNormalizerByNFKC();
		} else {
			return defaultCharNormalizer.execute(text);
		}
		
		return defaultCharNormalizer.execute(cn.execute(text));
	}
	

	
	private String textTransformation(String text, String filename){
		BufferedReader br;

		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					filename), "UTF-8")); //$NON-NLS-1$
			String line;

			while ((line = br.readLine()) != null) {
//				line = line.replace("\r\n", "\n");
				// 削除規則の場合
				if(line.endsWith("\t")){ //$NON-NLS-1$
					line += "\0"; //$NON-NLS-1$
				}
				
				String[] rule = line.split("\t"); //$NON-NLS-1$
				
				
				if(line.startsWith("#") || line.length() == 0){ //$NON-NLS-1$
					// コメントとして，スキップ
					System.err.println("comment: " + line); //$NON-NLS-1$
					continue;
				} else if(rule.length != 2){
					// エラーとして，スキップ
					System.err.println("Warning(TextFileImporter#textTransformation): the invalid rule was not applied : " + line); //$NON-NLS-1$
					continue;
				} else {
					System.err.println("applied: " + line); //$NON-NLS-1$
				}

				if(rule[1].compareTo("\0") == 0){ //$NON-NLS-1$
					rule[1] = ""; //$NON-NLS-1$
				}

				// TODO ２種類の特殊文字しか扱えない
				text = text.replaceAll(rule[0], rule[1].replace("\\n", "\n").replace("\\t", "\t")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
			br.close();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return text;
	}
	
	
	
	
	
	private String xml2doc(String text) throws IOException,
			ParserConfigurationException, SAXException, TransformerException {

		String xmltext = ""; //$NON-NLS-1$
		File tmpfile = File.createTempFile("errfile", "tmp"); //$NON-NLS-1$ //$NON-NLS-2$
		tmpfile.deleteOnExit();

		// 文字正規化
		text = normalizeTaggedText(text);
		
		// xmlのパース
		try {
			xmlParse(text, tmpfile);
		} catch (SAXParseException e) {
			e.printStackTrace();
			throw e;
		}

		// xsltスタイルシート
		if (optXmlStyle != null) {
			InputStream styleIs = null;
			try {
				File xslFile = new File(optXmlStyle);
				if (xslFile.exists()) {
					styleIs = new FileInputStream(xslFile);
					xmltext = openTextFile(xslFile);
				}
				xmlParse(xmltext, tmpfile, false);
			} catch (SAXParseException e) {
				e.printStackTrace();
				throw e;
			}

			// XSLTとXML文書をスタイルの変換を行う
			// 出力用一時ファイル
			File tmpResultFile = File.createTempFile("edamame", "tmp"); //$NON-NLS-1$ //$NON-NLS-2$
			tmpResultFile.deleteOnExit();

			InputStream xmlIs = new ByteArrayInputStream(text.getBytes("UTF-8"));; //$NON-NLS-1$

			xmlStyleCovert(xmlIs, styleIs, tmpResultFile);
			xmltext = openTextFile(tmpResultFile);
			xmlIs.close();
			styleIs.close();
			tmpResultFile.delete();
		} else {
			xmltext = text;
		}
		
		return xmltext;
	}
	
	
	/**
	 * xmlのパースを行う（テキストから）
	 *
	 * @param text
	 *            パース対象文字列
	 * @param file
	 *            エラーを出すときの対象ファイル名
	 * @param addError
	 *            エラーメッセージを追加するかのフラグ
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	private void xmlParse(String text, File file, boolean addError)
			throws SAXException, IOException, ParserConfigurationException {

		// textの内容を一時ファイルに保存（parseのため）
		File tmpFile = File.createTempFile("himawari", ".tmp"); //$NON-NLS-1$ //$NON-NLS-2$
		tmpFile.deleteOnExit();
		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(
				tmpFile)));

		pw.print(text);
		pw.close();
		xmlParse(tmpFile);
		tmpFile.delete();
		return;
	}

	/**
	 * xmlスタイルシートの変換を行う
	 *
	 * @param xml
	 * @param style
	 * @param output
	 * @throws TransformerException
	 * @throws IOException
	 */
	private void xmlStyleCovert(InputStream xml, InputStream style, File output)
			throws TransformerException, IOException {

		StreamSource xmlSource = new StreamSource(xml);
		StreamSource styleSource = new StreamSource(style);


		try {
			StreamResult result = new StreamResult(new FileOutputStream(output));

			TransformerFactory tFactory = TransformerFactory.newInstance();
	        Transformer transformer = tFactory.newTransformer(styleSource);
	        transformer.transform(xmlSource, result);

		} catch (TransformerException e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void xmlParse(String text, File file) throws SAXException,
			IOException, ParserConfigurationException {
		xmlParse(text, file, true);
		return;
	}

	/**
	 * パースを行う（ファイルから）
	 *
	 * @param file
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	private void xmlParse(File file) throws SAXParseException, SAXException, IOException, ParserConfigurationException{

		FileInputStream fis = null;
		Reader reader = null;

		try{
			String charset = EncodeDetector.detect(file);
			if (charset == null) {
				charset = "shift-jis"; //$NON-NLS-1$
			}
			InputSource is = null;

			fis = new FileInputStream(file);
			reader = new InputStreamReader(fis, charset);
			//UTF-16LEの場合BOMを除去する
			if(charset == "UTF-16LE"){ //$NON-NLS-1$
				StringBuilder buffer = new StringBuilder();
				int c;
				c = reader.read();
				if (c != -1) {
				    if (c != 0xFEFF) { buffer.append((char) c); }
				    while ((c = reader.read()) != -1) { buffer.append((char) c); }
				}
				reader = new StringReader(buffer.toString());
			}
			is = new InputSource(reader);
			is.setEncoding(charset);

			SAXParserFactory saxFactory = SAXParserFactory.newInstance();
			saxFactory.setValidating(false);
			saxFactory.setNamespaceAware(true);

			SAXParser saxParser = saxFactory.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			xmlReader.setErrorHandler(new SimpleErrorHandler());
			xmlReader.parse(is);

		}finally{
		    if (reader != null) {
		        reader.close();
		    } else if (fis != null) {
		    	fis.close();
		    }
		}
	}

	
	/**
	 * HTMLファイルのX(HT)ML化
	 *
	 * @param text
	 */
	private String htm2xhtml(String text) {

		// 名前空間
//		if (!regMatch("<html[^>]*xmlns=[^>]*>", text)) { //$NON-NLS-1$
		if (!regMatch("<html[^>]*?xmlns=[^>]*>", text)) { //$NON-NLS-1$
			text = regReplace("(?i)<html([^>]*)>", text, //$NON-NLS-1$
					"<html xmlns=\"http://www.w3.org/1999/xhtml\"$1>"); //$NON-NLS-1$
		}


		// 削除対象
		text = regReplace("(?i)<\\/?input[^>]*>", text, ""); //$NON-NLS-1$ //$NON-NLS-2$
		text = regReplace("<!--(.*?)-->", text, ""); //$NON-NLS-1$ //$NON-NLS-2$
//		text = regReplace("(?m)<!--((?:\n|\r|.)*?)-->", text, ""); //$NON-NLS-1$ //$NON-NLS-2$
//		text = regReplace("(?mi)<script[^>]*>((?:\n|\r|[^<])*?)</script>", text, ""); //$NON-NLS-1$
//		text = regReplace("(?mi)<style[^>]*>((?:\n|\r||[^<])*?)<\\/style>", text, ""); //$NON-NLS-1$

		// タグのパターン
		Pattern patternTag = Pattern.compile("((</?([^\\s>\"]+)).*?>)"); //$NON-NLS-1$
		Matcher mTag = patternTag.matcher(text);
		// 空タグ変換対象のパターン
		Pattern patternEmptyTag = Pattern.compile("<(br|hr|img|meta|link|frame)(\\s*|\\s.*[^/])>"); //$NON-NLS-1$
		// 引用符がついていない属性値のパターン
		Pattern patternArg = Pattern.compile("( [^\\s\"]+?=)([^\\s>\"]+)(?=[\\s>])"); //$NON-NLS-1$
	
		StringBuffer sb = new StringBuffer();

		while(mTag.find()){
			String tag = mTag.group();
			String tagFront = mTag.group(2);
			String tagName = mTag.group(3);
			
			if (HTML.getTag(tagName) != null) {
				// タグ名の小文字化
				tag = tag.replaceFirst(tagFront, tagFront.toLowerCase());

				// 属性値の引用符
				Matcher mArg = patternArg.matcher(tag);
				tag = mArg.replaceAll("$1\"$2\""); //$NON-NLS-1$

				// tag = tag.replaceAll("( [^\\s\"]+?=)([^\\s>\"]+)(?=[\\s>])",
				// "$1\"$2\"");
				// tag = tag.replaceAll("( [^\\s\"]+?=)([^\\s>\"]+)(?=[\\s>])",
				// "$1\"$2\"");

				// 空要素化
				Matcher mEmptyTag = patternEmptyTag.matcher(tag);
				if (mEmptyTag.find()) {
					tag = tag.replaceFirst(">", " />"); //$NON-NLS-1$ //$NON-NLS-2$
				}

				// 属性内の &
				tag = tag.replaceAll("&", "&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
				tag = cleanArgument(tag);
			} else {
				tag = ""; //$NON-NLS-1$
			}
			
			mTag.appendReplacement(sb, tag);
		}

		mTag.appendTail(sb);
		text = sb.toString();
		
		return text;

	}


	/**
	 * plain テキストを開く
	 *
	 * @param file
	 * @throws IOException
	 */
	private String openHtmlFile(File file, String encode) throws IOException {
		FileInputStream is = new FileInputStream(file);
		BufferedReader br = new BufferedReader(
				new InputStreamReader(is, encode));
		String line;
		StringBuilder sb = new StringBuilder();
		int ntChar = 0;
		
		while ((line = br.readLine()) != null) {
			int t = checkTagString(line, ntChar);
			if(t <= 0){
				sb.append(line + "\n"); //$NON-NLS-1$
				ntChar = 0;
			} else if(t > 0){
				sb.append(line + " "); //$NON-NLS-1$
				ntChar = t;
			}
		}

		br.close();
		is.close();

		sb = removeElement(sb, "script"); //$NON-NLS-1$
		sb = removeElement(sb, "SCRIPT"); //$NON-NLS-1$
		sb = removeElement(sb, "style"); //$NON-NLS-1$
		sb = removeElement(sb, "STYLE"); //$NON-NLS-1$
		return sb.toString();
	}

	private String openHtmlFile(File file) throws IOException {
		String charset = EncodeDetector.detect(file);
		if (charset == null) {
			charset = "UTF-8"; //$NON-NLS-1$
		}
		return openHtmlFile(file, charset);
	}

	
	private int checkTagString(String line, int n){
		int len = line.length();

		for(int i = 0; i < len; i++){
			char c = line.charAt(i);
			if(c == '<'){
				n++;
			} else if(c == '>'){
				n--;
			}
		}
		
		return n;
	}


	private StringBuilder removeElement(StringBuilder sb, String elementName){

		int p = 0;

		while(true){
			int cp = sb.indexOf("<" + elementName + " ", p); //$NON-NLS-1$ //$NON-NLS-2$
			if(cp == -1){
				cp = sb.indexOf("<" + elementName + ">", p); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			if(cp != -1){
				int ep = sb.indexOf("</" + elementName + ">", cp); //$NON-NLS-1$ //$NON-NLS-2$
				sb.delete(cp, ep + elementName.length() + 3);
			} else {
				break;
			}
		}
		return sb;
	}
	
	
	private String cleanArgument(String tag){
		if(tag.startsWith("</") || tag.matches("^<[^\\s>]+>$")){ //$NON-NLS-1$ //$NON-NLS-2$
			return tag;
		} else {
			Pattern patternTag = Pattern.compile("(<.+?\\s+?)(.*?)(/?)>"); //$NON-NLS-1$
			Matcher mTag = patternTag.matcher(tag);
			if(mTag.find()){
				StringBuilder sb = new StringBuilder(mTag.group(1));
				String tagRest = mTag.group(2);
				Pattern patternArg = Pattern.compile("([^\"]+?=\".*?\"\\s*)"); //$NON-NLS-1$
				Matcher mArg = patternArg.matcher(tagRest);
				int ep = tagRest.length(); // 末尾の文字の後のoffset
				int cp = 0;
				while(mArg.find()){
					sb.append(mArg.group());
					cp = mArg.end();
				}
				if(ep != cp){
					System.err.println("Warning(TextFileInporter(invalid argument in a tag): " + tag + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				return sb.toString() + mTag.group(3) + ">"; //$NON-NLS-1$
			} else {
				
			}
		}
		return null;
	}
	
	/**
	 * テキストファイルを開く
	 *
	 * @param file
	 * @throws IOException
	 */
	private String openTextFile(File file, String encode) throws IOException {
		FileInputStream is = new FileInputStream(file);
		BufferedReader br = new BufferedReader(
				new InputStreamReader(is, encode));
		String line;
		StringBuilder sb = new StringBuilder();
		while ((line = br.readLine()) != null) {
			sb.append(line + "\n"); //$NON-NLS-1$
		}
		br.close();
		is.close();

		return sb.toString();
	}

	private String openTextFile(File file) throws IOException {
		String charset = EncodeDetector.detect(file);
		if (charset == null) {
			charset = "UTF-8"; //$NON-NLS-1$
		}
		return openTextFile(file, charset);
	}

	/**
	 * Validate a XML file
	 *
	 * @param file
	 * @throws Exception
	 */
	private void validate(File file) throws Exception {
		try {
			xmlParse(file);
		} catch (SAXParseException e) {
			throw new Exception(
					String.format("%s (%d, %d) %s<br/>%s",  //$NON-NLS-1$
							file.getName(),
							e.getLineNumber(), e.getColumnNumber(), 
							Messages.getString("TextFileImporter.194"), //$NON-NLS-1$
							StringEscapeUtils.escapeXml(e.getMessage())));
		}

	}

	/**
	 * ファイル名からプレフィックスを取得
	 *
	 * @param fileName
	 * @return
	 */
	private String getPreffix(String fileName) {
		if (fileName == null)
			return null;
		int point = fileName.lastIndexOf("."); //$NON-NLS-1$
		if (point != -1) {
			return fileName.substring(0, point);
		}
		return fileName;
	}


	private String getReplacer(String target) {
		return STR_REPLACER + target + STR_REPLACER;
	}

	/**
	 * リソースからファイルを読み込む
	 *
	 * @param path
	 * @return
	 */
	private String readFile(String path, String encode) {
		StringBuffer text = new StringBuffer(); //$NON-NLS-1$
		try {
			FileInputStream fis = new FileInputStream(path);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis,
					encode));

			while (br.ready()) {
				text.append(br.readLine() + "\n"); //$NON-NLS-1$
			}
			br.close();
			fis.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return text.toString();
	}

	/**
	 * 正規表現による文字列置換を行う
	 *
	 * @param reg
	 * @param target
	 * @param replace
	 * @return
	 */
	private String regReplace(String reg, String target, String replace) {
		Pattern p = Pattern.compile(reg);
		return p.matcher(target).replaceAll(replace);
	}


	/**
	 * 正規表現マッチを行う
	 * @param reg 正規表現
	 * @param target マッチ対象文字列
	 * @return　マッチする場合はtrue, しない場合はfalse
	 */
	private boolean regMatch(String reg, String target) {
		Pattern p = Pattern.compile(reg);
		return p.matcher(target).find();
	}
}

class SimpleErrorHandler implements ErrorHandler {
    public void warning(SAXParseException e) throws SAXException {
        System.out.println(e.getMessage());
    }

    public void error(SAXParseException e) throws SAXException {
        System.out.println(e.getMessage());
    }

    public void fatalError(SAXParseException e) throws SAXException {
        System.out.println(e.getMessage());
    }
}


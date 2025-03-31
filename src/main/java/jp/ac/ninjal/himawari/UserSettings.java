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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UserSettings {
	public static final String CONFIG_FILE_SYS = ".himawari_sys_config.xml"; //$NON-NLS-1$
	private static String externalElementNames[] = {"annotator", "import", "browsers", "hp", "manual"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	public final static String DEFAULT_CONFIG_FILE = "config.xml"; //$NON-NLS-1$
	public final static String CONFIG_FILE_BODY = "config_"; //$NON-NLS-1$
	public final static String CONFIG_FILE_SUFFIX = ".xml"; //$NON-NLS-1$
	public final static String CONFIG_FILE_SUFFIX_SDB = ".sd.xml"; //$NON-NLS-1$
	
	Document doc;
	XPath xpath;
	String filename;
	String osName = Util.getOSNameLowerCase();

	public UserSettings() {
	}

	
	public void init(String filename, boolean loadDefault) throws Exception {
		this.filename = filename;
		
		try {
			// DOMオブジェクトの作成
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(new File(filename));
			
			XPathFactory xpfactory = XPathFactory.newInstance();
			xpath = xpfactory.newXPath();
			
		} catch (OutOfMemoryError ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, Messages
					.getString("Frame1.522"), Messages.getString("Frame1.121"), //$NON-NLS-1$ //$NON-NLS-2$
					JOptionPane.ERROR_MESSAGE);

			// configの初期化に失敗した場合別のconfigを選択させる
			JFileChooser fileChooser = new JFileChooser("."); //$NON-NLS-1$
			HimawariFileFilter hff = new HimawariFileFilter("xml", Messages.getString("Frame1.117")); //$NON-NLS-1$ //$NON-NLS-2$
			fileChooser.addChoosableFileFilter(hff);
			fileChooser.setFileFilter(hff);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			if (fileChooser.showDialog(null, Messages.getString("Frame1.118")) == JFileChooser.APPROVE_OPTION) { //$NON-NLS-1$
				filename = fileChooser.getSelectedFile().getAbsolutePath();
				init(filename, false);
			} else {
				System.exit(-1);
			}
			throw new IOException();

		}

		// import the default settings for annotator and import
		String allowOverRide = getAttribute("setting", "allowOverRide"); // true if allowOverRide is null //$NON-NLS-1$ //$NON-NLS-2$
		if(!filename.startsWith(".") && loadDefault) { // avoid recursive import //$NON-NLS-1$
			importConfig(CONFIG_FILE_SYS, allowOverRide);
		}

		if(loadDefault){
			conformOldElements();
		}
	}

	
	private void conformOldElements(){
		// external_tools
		Element externalToolsElement = null;
		NodeList nodeList = doc.getElementsByTagName("external_tools"); //$NON-NLS-1$
		if(nodeList.getLength() > 0){
			externalToolsElement = (Element) nodeList.item(0);
		} else {
			externalToolsElement = doc.createElement("external_tools"); //$NON-NLS-1$
			doc.getDocumentElement().appendChild(externalToolsElement);
		}
		
		// access_command[1,2,3] -> external_tools/li
		try {
			nodeList = (NodeList) xpath.evaluate("//access_command1| //access_command2| //access_command3", doc, XPathConstants.NODESET); //$NON-NLS-1$

			for(int i = 0; i < nodeList.getLength(); i++){
				Element target = (Element)nodeList.item(i);
				
				// rename the element name
				doc.renameNode(target, target.getNamespaceURI(), "li"); //$NON-NLS-1$
				
				// copy label attributes to name attributes
				if(!target.hasAttribute("name")){ //$NON-NLS-1$
					target.setAttribute("name", target.getAttribute("label")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				externalToolsElement.appendChild(target);
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}
	
	
	private void importConfig(String configFilename, String allowOverRide) throws Exception {
		
		if(! new File(configFilename).exists()){
			return;
		}
		
		UserSettings annotatorConfig = new UserSettings();
		annotatorConfig.init(configFilename, false);

		for (String elementName : externalElementNames) {
			NodeList annotatorNodes = doc.getElementsByTagName(elementName);
			if(allowOverRide == null || annotatorNodes.getLength() == 0 || allowOverRide.equalsIgnoreCase("true")) { //$NON-NLS-1$
				NodeList importedAnnotatorNodes = annotatorConfig.doc.getElementsByTagName(elementName);
				if(importedAnnotatorNodes.getLength() == 0) {
					return;
				}
				Node cloneAnnotatorNode = doc.importNode(importedAnnotatorNodes.item(0), true);
				if (annotatorNodes.getLength() == 0) {
					doc.getDocumentElement().appendChild(cloneAnnotatorNode);
				} else {
					doc.getDocumentElement().replaceChild(cloneAnnotatorNode, annotatorNodes.item(0));
				}
			}
		}
	}
	

	public String getAttribute(String nodeName, String attributeName) {
		try {
			Node targetNode = doc.getElementsByTagName(nodeName).item(0);
			return targetNode.getAttributes().getNamedItem(attributeName)
					.getNodeValue();
		} catch (NullPointerException e) {
			// System.err.println("Warning(UserSetting, getAttribute): ノード " +
			// nodeName +
			// " の属性 " + attributeName +
			// " は，定義ファイルに存在しません。null 文字を返します。");
			return null;
		}
	}


	public String[] getAttributeList(Node node, String attributeName) {
		ArrayList<String> res = new ArrayList<String>();
		String resStr[];
		Node item = node.getFirstChild();
		while (item != null) {
			if (item.getNodeName().compareTo("li") == 0) { //$NON-NLS-1$
				if (item.getAttributes().getNamedItem(attributeName) != null) {
					res.add(item.getAttributes().getNamedItem(attributeName)
							.getNodeValue());
				} else {
					res.add(new String("")); //$NON-NLS-1$
				}
			}
			item = item.getNextSibling();
		}

		resStr = new String[res.size()];
		for (int i = 0; i < res.size(); i++) {
			resStr[i] = res.get(i);
		}
		return resStr;
	}


	public String[] getAttributeList(String nodeName, String attributeName) {
		return getAttributeList(doc.getElementsByTagName(nodeName).item(0), attributeName);
	}


	public List<Node> evaluateNodes(String xpathStr, boolean useOSFilter) throws XPathExpressionException {
		NodeList nodeList = (NodeList) xpath.evaluate(xpathStr, doc, XPathConstants.NODESET);
		List<Node> resultNodes = new ArrayList<Node>();
		// use name attributes to identify nodes
		HashMap<String, Integer> nodeMap = new HashMap<String, Integer>();

		if(!useOSFilter){
			for(int i = 0; i < nodeList.getLength(); i++){
				resultNodes.add(nodeList.item(i));
			}
		} else {
			for(int i = 0; i < nodeList.getLength(); i++){
				Node node = nodeList.item(i);

				if(node.getNodeType() == Node.ATTRIBUTE_NODE){
					node = ((Attr) node).getOwnerElement();
				}
				
				if(node != null && node.getNodeType() == Node.ELEMENT_NODE){
					Element element = (Element) node;
					String name = element.getAttribute("name"); //$NON-NLS-1$
					String nodeOSName = element.getAttribute("os").toLowerCase(); //$NON-NLS-1$

					
					if(nodeOSName.isEmpty()){
						if(!nodeMap.containsKey(name)){
							nodeMap.put(name, i);
						}
					} else if(osName.startsWith(nodeOSName)){
						nodeMap.put(name, i);
					}
//				} else {
//					nodeMap.put(String.valueOf(i), i);
//					continue;
				}
			}
			
			for(int i = 0; i < nodeList.getLength(); i++){
				if(nodeMap.containsValue(i)){
					resultNodes.add(nodeList.item(i));
				}
			}
		}

		return resultNodes;
	}


	public String[] evaluateAtributeList(String xpath, String attributeName, boolean useOSFilter) {
		
		List<Node> nodeList = null;
		try {
			nodeList = evaluateNodes(xpath, useOSFilter);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		String resultStr[] = new String[nodeList.size()];
		for(int i = 0; i < nodeList.size(); i++){
			Element element = (Element) nodeList.get(i);
			resultStr[i] = element.getAttribute(attributeName);
		}

		return resultStr;
		
	}
	
	
	public String evaluateOneNode2(String xpathStr, boolean useOSFilter) throws XPathExpressionException {

		List<Node> nodeList = evaluateNodes(xpathStr, useOSFilter);
		if(nodeList.size() > 0){
			return nodeList.get(0).getTextContent();
		} else {
			return null;
		}
	}
	
	
	public void getElementInfo(HashMap<String, CorpusElementInfo> map){
		NodeList liNodes = null;
		
		// read from field_setting
		try {
			liNodes = (NodeList) xpath.evaluate("//corpus_fields/li|//unit_fields/li|//field_setting/li", doc, XPathConstants.NODESET); //$NON-NLS-1$
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(int i = 0; i < liNodes.getLength(); i++){
			Element li = (Element)liNodes.item(i);
			String elementName = li.getAttribute("element"); //$NON-NLS-1$
			if(elementName == null || elementName.isEmpty()){
				continue;
			}
			
			if(map.containsKey(elementName)){
				CorpusElementInfo corpusElementInfo = map.get(elementName);
				String attributeName = li.getAttribute("attribute"); //$NON-NLS-1$
				String type = li.getAttribute("sort_type"); //$NON-NLS-1$
				String label = li.getAttribute("name"); //$NON-NLS-1$
				if(type.isEmpty() || type.equalsIgnoreCase(FieldInfo.SORT_TYPE_STRING)){
					corpusElementInfo.setType(attributeName, FieldInfo.SORT_TYPE_STRING);
				} else {
					corpusElementInfo.setType(attributeName, FieldInfo.SORT_TYPE_NUMERIC);
				}
				
				if(label.isEmpty()){
					label = attributeName;
				}
				corpusElementInfo.setLabel(attributeName, label);
			}
		}
		
		// use attributes as labels if no entry
		for(Entry<String, CorpusElementInfo> element : map.entrySet()){
			CorpusElementInfo corpusElementInfo = element.getValue();
			for(String attributeName : corpusElementInfo.keySet()){
				if(corpusElementInfo.getLabel(attributeName) == null){
					corpusElementInfo.setLabel(attributeName, "{" + attributeName + "}"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			
		}
		
		
	}
	
	public void save(String filename) throws IOException, TransformerException {
		
		File configFile = new File(filename);
		if (configFile.exists()) {
			String backupFilename = filename + ".bak"; //$NON-NLS-1$
			File backupFile = new File(backupFilename);
			Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        
        transformer.setOutputProperty("indent", "yes"); //$NON-NLS-1$ //$NON-NLS-2$
        transformer.setOutputProperty("encoding", "utf-8"); //$NON-NLS-1$ //$NON-NLS-2$

        // XMLファイルの作成
        transformer.transform(new DOMSource(doc), new StreamResult(configFile));
	}
	
	
	public boolean setAttribute(String pathToBaseElement, String targetElementName, String attribute, String value) throws XPathExpressionException{
		List<Node> baseElementList = evaluateNodes(pathToBaseElement, true);
		if(baseElementList.size() == 0){
			return false;
		}
		
		NodeList nodeList = ((Element) baseElementList.get(0)).getElementsByTagName(targetElementName); //$NON-NLS-1$
		if(nodeList.getLength() == 0){
			return false;
		}

		((Element) nodeList.item(0)).setAttribute(attribute, value);
		
		return true;
	}

	public boolean includeSDB() {
		return filename.endsWith(CONFIG_FILE_SUFFIX_SDB);
	}

	
	public static boolean isConfigurationFile(String filename) {
		if(filename.equals(DEFAULT_CONFIG_FILE)
				|| (filename.startsWith(CONFIG_FILE_BODY) && filename.endsWith(CONFIG_FILE_SUFFIX))) {
			return true;
		} else {
			return false;
		}
	}
	
	
	public static List<Path> findConfig(Path dirPath) {
		try {
			return Files.walk(dirPath, 1).filter(path -> {
				return isConfigurationFile(path.getFileName().toString());
			}).sorted().collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
			return new ArrayList<Path>();
		}
	}
}

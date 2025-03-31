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

import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javax.swing.JOptionPane;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class CorpusBrowser {
	// tag name for marking up target characters
	public final static String ANCHOR_NAME = "tg"; //$NON-NLS-1$ 
	// id in anchor element
	public final static String ANCHOR_ID = "himawari"; //$NON-NLS-1$
	// file encoding
	final static String FILE_ENCODING = "utf-16"; //$NON-NLS-1$
	// file delimiter
	final static String PATH_DELIMITER = "/"; //$NON-NLS-1$
	
	final static String DEFAULT_BROWSER = "[[default_browser]]"; //$NON-NLS-1$

	final static String TEMPDIR_ROOT = "himawari"; //$NON-NLS-1$
	
	static Path tempDir;

	private ExternalApplicationPool externalApplicationPool = new ExternalApplicationPool();
	
	UserSettings userSettings;
	SearchEngineWorker searchEngineWorker;
	String browserNames[];
	String browserCommands[];
	String xslFilenames[];
	String browserOptions[];
	Component parent;
	
	public CorpusBrowser(UserSettings userSettings, SearchEngineWorker searchEngineWorker, Component parent) {
		this.userSettings = userSettings;
		this.searchEngineWorker = searchEngineWorker;
		this.parent = parent;
		
		browserNames = userSettings.evaluateAtributeList("/setting/browsers/li", "name", true); //$NON-NLS-1$ //$NON-NLS-2$
		browserCommands = userSettings.evaluateAtributeList("/setting/browsers/li", "path", true); //$NON-NLS-1$ //$NON-NLS-2$
		browserOptions = userSettings.evaluateAtributeList("/setting/browsers/li", "option", true); //$NON-NLS-1$ //$NON-NLS-2$

		xslFilenames = userSettings.getAttributeList("xsl_files", "name"); //$NON-NLS-1$ //$NON-NLS-2$

		// Copy the xslt directory to a temporary directory
		// Note: CorpusBrowser.init() must be invoked to be tempDir == null after loading a new configuration file
		try {
			if(tempDir == null) {
				Path tempDirRootPath = new File(System.getProperty("java.io.tmpdir")).toPath().resolve(TEMPDIR_ROOT); //$NON-NLS-1$

				// e.g. create /tmp/himawari if linux
				if(tempDirRootPath.toFile().exists()) {
					tempDir = tempDirRootPath;
				} else {
					tempDir = Files.createDirectory(tempDirRootPath);
				}
				
				// /tmp/himawari/_CREATED_TEMPORARY_DIRECTORY
				tempDir = Files.createTempDirectory(tempDir, null);
				Util.copyFile(
						new File(userSettings.getAttribute("xsl_files", "root_path")).toPath(), //$NON-NLS-1$ //$NON-NLS-2$
						tempDir,
						new CopyOption[] { StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING });
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void init() {
		tempDir = null;
	}
	
	public void browse(int iCorpus, int index, int targetLengthSP, int iSelectedBrowser, int iSelectedXSL) {
		Path styleSheetPath = tempDir.resolve(xslFilenames[iSelectedXSL]); 
		String xmlHeader =
				"<?xml version=\"1.0\"?>\n" //$NON-NLS-1$ //$NON-NLS-2$
				+ "<?xml-stylesheet href=\"" //$NON-NLS-1$
				+ styleSheetPath.toAbsolutePath() 
				+ "\" type=\"text/xsl\" ?>\n"; //$NON-NLS-1$ //$NON-NLS-2$
		
		try {
			Path tmpfile4browsing = Files.createTempFile(tempDir, null, ".html"); //$NON-NLS-1$

			xmlTransform(
					xmlHeader + searchEngineWorker.getBrowsedElement(iCorpus, index, targetLengthSP, ANCHOR_NAME, ANCHOR_ID),
					styleSheetPath,
					tmpfile4browsing);

			String url = "file://" + tmpfile4browsing.toAbsolutePath() + "#" + ANCHOR_ID; //$NON-NLS-1$ //$NON-NLS-2$
			
			if(System.getProperty("os.name").toLowerCase().startsWith("windows")) { //$NON-NLS-1$ //$NON-NLS-2$
				url = url.replaceFirst("^file://", "file:///").replaceAll("\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
			
			// trick for space characters (%20) and non-ascii characters
			url = new URI(url.replaceAll(" ", "%20")).toASCIIString(); //$NON-NLS-1$ //$NON-NLS-2$

			execute(url, iSelectedBrowser);
			
		} catch (Exception e1) {
			e1.printStackTrace();
			showBrowserError(iSelectedBrowser);
		}
	}

	
	private void xmlTransform(String inputString, Path styleSheetPath, Path outputPath)
			throws TransformerException, IOException {

		StreamSource xmlSource = new StreamSource(new StringReader(inputString));
		StreamSource styleSource = new StreamSource(new FileInputStream(styleSheetPath.toFile()));

		StreamResult result = new StreamResult(new FileOutputStream(outputPath.toFile()));

		TransformerFactory tFactory = TransformerFactory.newInstance();
		// change base URI
		tFactory.setURIResolver(new URIResolver() {
			@Override
			public Source resolve(String href, String base) throws TransformerException {
				return new StreamSource(tempDir.resolve(href).toFile());
			}
		});
		
		Transformer transformer = tFactory.newTemplates(styleSource).newTransformer();
		transformer.transform(xmlSource, result);
	}

	
	public void execute(String url, int iSelectedBrowser) throws IOException {
		String browser = Util.modifyPathOnWindows(browserCommands[iSelectedBrowser]);
		String browserOption = browserOptions[iSelectedBrowser];
		String osName = Util.getCannonicalOSName();

		if(browser.equals(DEFAULT_BROWSER)){
			if(!browserOption.isEmpty()){
				System.err.println("Warning(CorpusBrowser): Ignore the browser options, " + browserOption); //$NON-NLS-1$
			}
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.browse(new URI(url));
			} catch (Exception e) {
				e.printStackTrace();
				// assume that the setting of default_browser is the first on the setting list
				int nCandidate = browserCommands.length;
				if(iSelectedBrowser < nCandidate){
					System.err.println("Warning(CorpusBrowser): Next browser, " + iSelectedBrowser); //$NON-NLS-1$
					iSelectedBrowser++;
					execute(url, iSelectedBrowser);
					return;
				}
				e.printStackTrace();
				throw new IOException();
			}
		} else if(externalApplicationPool.contains(browser, osName)){
			externalApplicationPool.execute(browser, osName, new String[]{url});
		} else {
			throw new IOException("No application information for " + browser); //$NON-NLS-1$
		}
	}
	
	public void showBrowserError(int iSelectedBrowser){
		String browser = browserNames[iSelectedBrowser];
		JOptionPane.showConfirmDialog(parent,
				Messages.getString("Frame1.485") + //$NON-NLS-1$
						browser + Messages.getString("Frame1.486") + //$NON-NLS-1$
						Messages.getString("Frame1.487"), //$NON-NLS-1$
				Messages.getString("Frame1.488"), //$NON-NLS-1$
				JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
	}
}

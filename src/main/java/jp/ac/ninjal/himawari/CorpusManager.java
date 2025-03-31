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

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import jp.ac.ninjal.himawari.HimawariLogger.HimawariListedLog;
import jp.ac.ninjal.himawari.HimawariLogger.HimawariLog;
import jp.ac.ninjal.himawari.HimawariLogger.HimawariResultLog;

public class CorpusManager {

	public final static String RESOURCES_DIR = "resources"; //$NON-NLS-1$

	// logs
	private HimawariListedLog successfulFilesLog = new HimawariListedLog(Messages.getString("CorpusManager.0")); //$NON-NLS-1$
	private HimawariListedLog failedFilesLog = new HimawariListedLog(HimawariLog.MODE_ERROR, Messages.getString("CorpusManager.1")); //$NON-NLS-1$
	private HimawariListedLog summaryLog = new HimawariListedLog(Messages.getString("CorpusManager.2")); //$NON-NLS-1$

	private boolean flagMove = false;
	
	public CorpusManager() {
	}

	public CorpusManager(boolean flagMove) {
		this.flagMove = flagMove;
	}

	
	public void copyAll(Path fromDirPath, Path toDirPath) {
		
		// quit when fromDirPath is the same as toDirPath
		if(fromDirPath.toAbsolutePath().normalize().equals(toDirPath.toAbsolutePath().normalize())) {
			failedFilesLog.add(Messages.getString("CorpusManager.3")); //$NON-NLS-1$
			return;
		}
		
		// all configuration files
		List<Path> configsFrom = UserSettings.findConfig(fromDirPath);
		ArrayList<Path> configsFrom2 = new ArrayList<Path>(); // to be copied
		
		// find configuration files to copy
		for(Path configFrom : configsFrom) {
			Path configFileFrom = configFrom.getFileName();
			
			if(toDirPath.resolve(configFileFrom).toFile().exists()) {
				String corpusName = getCorpusName(configFrom);
				failedFilesLog.add("Already exist: " + corpusName + " (" + configFileFrom.toString() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				continue;
			}
			
			// get corpus directories in Corpora directory
			ArrayList<String> fromPaths = getSubCorporaDirectories(configFrom, toDirPath, true);
			if(fromPaths != null) {
				configsFrom2.add(configFrom);
			}
		}

		// copy configuration files, corpus files and xslt files
		for(Path configFrom : configsFrom2) {
			String corpusName = getCorpusName(configFrom);

			try {
				copy(configFrom, toDirPath);
				successfulFilesLog.add(corpusName + " (" + configFrom.toFile().getName() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (IOException e) {
				e.printStackTrace();
				failedFilesLog.add("IO error: " + corpusName + " (" + configFrom.toFile().getName() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		
		// copy files in resources directory
		try {
			if(copyResources(fromDirPath, toDirPath) != null) {
				successfulFilesLog.add(fromDirPath.resolve(RESOURCES_DIR).toString());
			};
		} catch (IOException e) {
			e.printStackTrace();
			failedFilesLog.add("IO error: " + fromDirPath.resolve(RESOURCES_DIR).toString());
		}
	}

	
	public void copy(Path configFrom, Path toDirPath) throws IOException {
		Path fromDirPath = configFrom.getParent();
		
		// get corpus directories in Corpora directory
		ArrayList<String> fromPaths = getSubCorporaDirectories(configFrom, toDirPath, false);

		if(fromPaths != null) {
			// copy corpus directories
			for(String strFromPath : fromPaths) {
				Path relativePath = Paths.get(strFromPath);
				if(!relativePath.getParent().toFile().exists()) {
					Files.createDirectories(relativePath.getParent());
				}
				if(fromDirPath.resolve(relativePath).toFile().exists()
						&& !toDirPath.resolve(relativePath).toFile().exists()) {
					fileCopy(fromDirPath.resolve(relativePath), toDirPath.resolve(relativePath));
				}
			}
		}

		// copy xslt directory
		copyXsltFiles(configFrom, toDirPath);
		// copy jitaidic
		copyJitaidic(configFrom, toDirPath);
		// copy sixdic
		copySIXDic(configFrom, toDirPath);
		// copy ext_db1, ext_db2
		copyExternalDbFiles(configFrom, toDirPath);
		
		// copy the configuration file
		fileCopy(configFrom, toDirPath.resolve(configFrom.getFileName()));
	}
	
	
	public boolean delete(File configFrom) {
		UserSettings userSettings = new UserSettings();
		
		try {
			userSettings.init(configFrom.getCanonicalPath(), true);
			String strDeletePaths[] = userSettings.evaluateAtributeList("/setting/corpora/li", "path", false); //$NON-NLS-1$ //$NON-NLS-2$
			
			for(String strDeletePath : strDeletePaths) {
				strDeletePath = strDeletePath.replaceFirst("/[^/]+$", ""); //$NON-NLS-1$ //$NON-NLS-2$
				
				if(!strDeletePath.startsWith("Corpora/")) { //$NON-NLS-1$
					continue;
				}
				
				Path pathDelete = Paths.get(strDeletePath);
				if(pathDelete.toFile().exists()) {
					Files.walk(pathDelete, Integer.MAX_VALUE).sorted(Comparator.reverseOrder()).forEach(path -> {
						try {
							Files.delete(path);
							System.err.println("Delete:" + path); //$NON-NLS-1$
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
				}
			}

			// delete the configuration file
			Files.delete(configFrom.toPath());
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	
	public void showLog(Frame1 parent) {
		HimawariLogger logger = new HimawariLogger();

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JTextPane pane = new JTextPane();
		pane.setContentType("text/html"); //$NON-NLS-1$
		JScrollPane scrollPane = new JScrollPane();
		pane.setPreferredSize(new Dimension(600,250));
		scrollPane.setViewportView(pane);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		panel.add(scrollPane);

		summaryLog.add(successfulFilesLog.getSize() + Messages.getString("CorpusManager.13")); //$NON-NLS-1$
		
		if(failedFilesLog.getSize() == 0 && successfulFilesLog.getSize() != 0){
			// success with no error
			logger.add(new HimawariResultLog(Messages.getString("CorpusManager.14"))); //$NON-NLS-1$
			logger.add(summaryLog);
			logger.add(successfulFilesLog);
			pane.setText(logger.write());
			pane.setCaretPosition(0);
			JOptionPane.showMessageDialog(parent, panel);
		} else if(successfulFilesLog.getSize() == 0){
			logger.add(new HimawariResultLog(Messages.getString("CorpusManager.15"))); //$NON-NLS-1$
			logger.add(summaryLog);
			logger.add(failedFilesLog);
			pane.setText(logger.write());
			pane.setCaretPosition(0);
			JOptionPane.showMessageDialog(parent, panel);
		} else{
			// success with some errors
			summaryLog.add(failedFilesLog.getSize() + Messages.getString("CorpusManager.16")); //$NON-NLS-1$
			logger.add(new HimawariResultLog(Messages.getString("CorpusManager.17"))); //$NON-NLS-1$
			logger.add(summaryLog);
			logger.add(failedFilesLog);
			logger.add(successfulFilesLog);
			pane.setText(logger.write());
			pane.setCaretPosition(0);
			JOptionPane.showMessageDialog(parent, panel, Messages.getString("CorpusManager.18"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
		}
	}
	
	private Path copyXsltFiles(Path configFrom, Path toDirPath) {
		UserSettings userSettings = new UserSettings();
		
		try {
			userSettings.init(configFrom.toFile().getCanonicalPath(), true);
			String strXsltPath = userSettings.evaluateOneNode2("/setting/xsl_files/@root_path", false); //$NON-NLS-1$
			if(strXsltPath == null) {
				System.err.println("Warning(CorpusManager): Not found the xslt direcotry described in " + configFrom.toString()); //$NON-NLS-1$
				return null;
			}
			
			Path pathXsltFrom = configFrom.getParent().resolve(strXsltPath);
			
			if(pathXsltFrom.toFile().exists() 
					&& !toDirPath.resolve(strXsltPath).toFile().exists()) {
				fileCopy(pathXsltFrom, toDirPath.resolve(strXsltPath));
				return pathXsltFrom;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	
	private Path copyJitaidic(Path configFrom, Path toDirPath) {
		UserSettings userSettings = new UserSettings();
		
		try {
			userSettings.init(configFrom.toFile().getCanonicalPath(), true);
			String strJitaidicPath = userSettings.evaluateOneNode2("/setting/jitaidic/@url", false); //$NON-NLS-1$
			if(strJitaidicPath == null) {
				System.err.println("Warning(CorpusManager): Not found the jitaidic described in " + configFrom.toString()); //$NON-NLS-1$
				return null;
			}
			
			Path pathJitaidicFrom = configFrom.getParent().resolve(strJitaidicPath);
			
			if(pathJitaidicFrom.toFile().exists() 
					&& !toDirPath.resolve(strJitaidicPath).toFile().exists()) {
				fileCopy(pathJitaidicFrom, toDirPath.resolve(strJitaidicPath));
				return pathJitaidicFrom;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	
	private Path copySIXDic(Path configFrom, Path toDirPath) {
		UserSettings userSettings = new UserSettings();
		
		try {
			userSettings.init(configFrom.toFile().getCanonicalPath(), true);
			String sixdicPath = userSettings.evaluateOneNode2("/setting/corpora/@dbpath", false); //$NON-NLS-1$
			if(sixdicPath == null) {
				return null;
			}
			
			Path pathSIXDicFrom = configFrom.getParent().resolve(sixdicPath);
			
			for (Path pathSIXDic : Files.list(pathSIXDicFrom).toArray(Path[]::new)) {
				Path toPath = toDirPath.resolve(sixdicPath).resolve(pathSIXDic.getFileName());
				
				if(pathSIXDic.toString().endsWith(SIXDic.suffix)
						&& pathSIXDic.toFile().isFile()
						&& !toPath.toFile().exists()) {
					fileCopy(pathSIXDic, toPath);
				}
			}
			
			return pathSIXDicFrom;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	private void copyExternalDbFiles(Path configFrom, Path toDirPath) {
		UserSettings userSettings = new UserSettings();
		
		try {
			userSettings.init(configFrom.toFile().getCanonicalPath(), true);
		} catch (Exception e1) {
			e1.printStackTrace();
			return;
		}
		
		String strDbPaths[] = userSettings.evaluateAtributeList("/setting/ext_db1 | /setting/ext_db2", "url", false); //$NON-NLS-1$
		if(strDbPaths == null) {
			return;
		}
		
		for(String strDbPath : strDbPaths) {
			Path pathFrom = configFrom.getParent().resolve(strDbPath);
			
			if(pathFrom.toFile().exists() 
					&& !toDirPath.resolve(strDbPath).toFile().exists()) {
				try {
					fileCopy(pathFrom, toDirPath.resolve(strDbPath));
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
			}
		}
		
		return;
	}

	
	private Path copyResources(Path fromDirPath, Path toDirPath) throws IOException {
		Path toResourcesPath = toDirPath.resolve(RESOURCES_DIR);
		
		
		if(!toResourcesPath.toFile().exists()) {
			Files.createDirectory(toResourcesPath);
		}
		
		Path fromTopPath = fromDirPath.resolve(RESOURCES_DIR);

		long n = Files.walk(fromTopPath, Integer.MAX_VALUE).filter(path -> {
			if(path.compareTo(fromTopPath) == 0){
				return false; // = continue
			}
			
			Path relativePath = fromTopPath.relativize(path);
			Path toFilePath = toResourcesPath.resolve(relativePath);

			try {
				if(!toFilePath.toFile().exists()) {
					if(path.toFile().isDirectory()) {
						Files.createDirectory(toFilePath);
					} else {
						fileCopy(path, toFilePath);
						return true;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			return false;
		}).count();
		
		if(n != 0) {
			return toResourcesPath;
		} else {
			return null;
		}
	}
	
	
	private void fileCopy(Path fromFilePath, Path toFilePath) throws IOException {
		if(flagMove) {
			Files.move(fromFilePath, toFilePath);
			System.err.println("Move From:" + fromFilePath + " To " + toFilePath); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			Util.copyFile(fromFilePath, toFilePath, new CopyOption[] { StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING });
			System.err.println("Copy From:" + fromFilePath + " To " + toFilePath); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
	}
	
	
	// return a list of /setting/corpora/li/@path to be copied
	private ArrayList<String> getSubCorporaDirectories(Path configFrom, Path toDirPath, boolean failThenReturn) {
		ArrayList<String> results = new ArrayList<String>();
		UserSettings userSettings = new UserSettings();
		
		try {
			userSettings.init(configFrom.toFile().getCanonicalPath(), true);
			String pathsFrom[] = userSettings.evaluateAtributeList("/setting/corpora/li", "path", false); //$NON-NLS-1$ //$NON-NLS-2$
			
			for(String strPathFrom : pathsFrom) {
				if(!strPathFrom.startsWith("Corpora/")) { //$NON-NLS-1$
					// only permitted relative paths
					System.err.println("rejected not corpora:" + strPathFrom); //$NON-NLS-1$
					if(failThenReturn) {
						return null;
					}
				}
				
				strPathFrom = strPathFrom.replaceFirst("/[^/]+$", ""); //$NON-NLS-1$ //$NON-NLS-2$
				Path pathTo = toDirPath.resolve(strPathFrom);
				Path pathFrom = configFrom.getParent().resolve(strPathFrom);
				if(!pathFrom.toFile().exists() || pathTo.toFile().exists()) {
					if(failThenReturn) {
						return null;
					}
				} else if(!results.contains(strPathFrom)) {
					results.add(strPathFrom);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return results;
	}
	

	private String getCorpusName(Path configPath) {
		String corpusName = "no name"; //$NON-NLS-1$
		UserSettings userSettings = new UserSettings();
		try {
			userSettings.init(configPath.toFile().getAbsolutePath(), false);
			corpusName = userSettings.evaluateOneNode2("/setting/corpora/@name", false); //$NON-NLS-1$
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		return corpusName;
	}
}

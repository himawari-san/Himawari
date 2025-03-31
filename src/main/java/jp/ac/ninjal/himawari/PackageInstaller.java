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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.CopyOption;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.StringUtils;

public class PackageInstaller {
	public final static String PACKAGE_INFO_FILE = ".himawari_package_info"; //$NON-NLS-1$
	private Component parent;
	private String newConfigFile = null;

	
	public PackageInstaller(Component parent) {
		this.parent = parent;
	}

	public String install(Path packagePath){
		List<Path> packageInfoPathes = new ArrayList<Path>();
		Path packageInfoPath = null;
		FileSystem packageFS = null;
		FileSystem targetFS = FileSystems.getDefault();
		long totalFileSize = 0;
		
		try {
			Path rootPath = null;
			if(Files.isDirectory(packagePath)){
				rootPath = packagePath;
				packageFS = FileSystems.getDefault();
			} else {
				packageFS = FileSystems.newFileSystem(packagePath, ClassLoader.getSystemClassLoader());

				for(Path path: packageFS.getRootDirectories()){
					rootPath = path;
					break;
				}
			}
					
			Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>(){
		         @Override
		         public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
		             throws IOException
		         {
		        	 if(file.getFileName().toString().equals(PACKAGE_INFO_FILE)){
		        		 packageInfoPathes.add(file);
			             return FileVisitResult.TERMINATE;
		        	 } else {
			             return FileVisitResult.CONTINUE;
		        	 }
		         }
		         @Override
		         public FileVisitResult postVisitDirectory(Path dir, IOException e)
		             throws IOException
		         {
	                 return FileVisitResult.CONTINUE;
		         }
			});
			
			if(packageInfoPathes.size() == 0){
				JOptionPane.showMessageDialog(parent,
				Messages.getString("Frame1.130")); //$NON-NLS-1$
				System.err.println("Error(Frame): " + PACKAGE_INFO_FILE + " is not found"); //$NON-NLS-1$ //$NON-NLS-2$
				return null;
			}
			packageInfoPath = packageInfoPathes.get(0);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(parent, Messages.getString("Frame1.143") + e); //$NON-NLS-1$
			e.printStackTrace();
		}

		BufferedReader br;
		ArrayList<String> targetFiles = new ArrayList<String>();
		ArrayList<String> invalidFiles = new ArrayList<String>();
		ArrayList<String> duplicateFiles = new ArrayList<String>();

		
		try {
			br = Files.newBufferedReader(packageInfoPath, Charset.forName("utf-8")); //$NON-NLS-1$
			String line;

			while ((line = br.readLine()) != null) {
				if (line.isEmpty()) {
					continue;
				}
				String targetFile = checkInstallationFilePath(line);
				if (targetFile == null) {
					invalidFiles.add(line);
				} else {
					if (Files.exists(targetFS.getPath(line))) {
						duplicateFiles.add(line);
					}

					if (newConfigFile == null
							&& line.matches("^config[^/]*\\.xml$")) { //$NON-NLS-1$
						newConfigFile = line;
						targetFiles.add(0, line); // Add a configuration file at the front for canceling installation 
					} else {
						targetFiles.add(line);
					}
				}
			}
			br.close();
			
			if (invalidFiles.size() > 0) {
				JOptionPane.showMessageDialog(parent,
						Messages.getString("Frame1.134") + //$NON-NLS-1$
								StringUtils.join(invalidFiles, "\n")); //$NON-NLS-1$
				return null;
			}
			
			if (duplicateFiles.size() > 0) {
				int option = JOptionPane.showConfirmDialog(
								parent,
								Messages.getString("Frame1.136") + StringUtils.join(duplicateFiles, "\n"), //$NON-NLS-1$ //$NON-NLS-2$
								Messages.getString("Frame1.139"), //$NON-NLS-1$
								JOptionPane.OK_CANCEL_OPTION);

				if (option == JOptionPane.CANCEL_OPTION) {
					return null;
				}
			}

			for (String filePath : targetFiles) {
				totalFileSize += Util.getFileSize(packageInfoPath.resolveSibling(filePath));
			}

			
			// check the free disk space
			File himawariDir = new File("./"); // directory where Himawari is installed //$NON-NLS-1$
			if(totalFileSize > himawariDir.getFreeSpace()) {
				int option = JOptionPane.showConfirmDialog(parent, Messages.getString("PackageInstaller.1") + "\n" //$NON-NLS-1$ //$NON-NLS-2$
						+ Messages.getString("PackageInstaller.5") + himawariDir.getFreeSpace() + " byte\n" //$NON-NLS-1$ //$NON-NLS-2$
						+ Messages.getString("PackageInstaller.7") + totalFileSize + " byte\n\n" //$NON-NLS-1$ //$NON-NLS-2$
						+ Messages.getString("PackageInstaller.9"), Messages.getString("PackageInstaller.10"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$ //$NON-NLS-2$
				
				if(option == JOptionPane.CANCEL_OPTION) {
					return null;
				}
			}
			
			
			final long finalTotalFileSize = totalFileSize;
			final Path finalPackageInfoPath = packageInfoPath;

			InstallProgressDialog d = new InstallProgressDialog();
			d.setLocationRelativeTo(parent);

			var f = Executors.newSingleThreadExecutor().submit(new Callable<Void>() {
				@Override
				public Void call() throws IOException {
					long totalCopySize = 0;

					for (String filePath : targetFiles) {
						final long finalTotalCopySize = totalCopySize;
						Path from = finalPackageInfoPath.resolveSibling(filePath);

						Util.copyFile(from,
								targetFS.getPath(filePath),
								new CopyOption[] { StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING },
								(size) -> {
									SwingUtilities.invokeLater(new Runnable() {
										public void run() {
											d.setValue((int)Math.floor((finalTotalCopySize + size) / (double)finalTotalFileSize * 100));
										}
									});
								}
						);
						totalCopySize += Util.getFileSize(targetFS.getPath(filePath));

					}

					d.setText(Messages.getString("Frame1.144") + StringUtils.join(targetFiles, "\n")); //$NON-NLS-1$ //$NON-NLS-2$
					d.setStatus(InstallProgressDialog.STATUS_OK);
					
					return null;
				}
			});

			d.show(f);
			System.gc();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(parent,
					Messages.getString("Frame1.143") + e); //$NON-NLS-1$
			e.printStackTrace();
			return null;
		}

		return newConfigFile;
	}

	
	private String checkInstallationFilePath(String str){
		if(str.contains("..") || str.contains("//") || str.contains("~")){ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return null;
		} else if(str.matches("^Corpora/.+") || str.matches("^resources/.+") || str.matches("^config[^/]*\\.xml$")){ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return str;
		} else {
			return null;
		}
	}
	

	class InstallProgressDialog extends JDialog {

		private static final long serialVersionUID = 1L;
		private static final int STATUS_OK = 0;
		private static final int STATUS_CANCEL = 1;
		private static final String STATUS_OK_LABEL = "OK"; //$NON-NLS-1$
		private static final String STATUS_CANCEL_LABEL = "Cancel"; //$NON-NLS-1$
		
		private JProgressBar progressBar = new JProgressBar();
		private JTextArea textArea = new JTextArea(5, 30);
		private JButton button = new JButton(STATUS_CANCEL_LABEL);
		private int status = STATUS_CANCEL;
		private Future<Void> future = null;

		public InstallProgressDialog() {
			JPanel progressBarPanel = new JPanel();
			progressBarPanel.setLayout(new BorderLayout());
			progressBarPanel.setBorder(new EmptyBorder(10, 20, 5, 20));
			progressBar.setStringPainted(true);
			progressBar.setBorderPainted(true);
			progressBar.setPreferredSize(new Dimension(250, 30));
			progressBarPanel.add(progressBar, BorderLayout.CENTER);
			
			add(progressBarPanel, BorderLayout.NORTH);

			JPanel messagePanel = new JPanel();
			messagePanel.setLayout(new BorderLayout());
			messagePanel.setBorder(new EmptyBorder(5, 20, 5, 20));
			JScrollPane scrollPane = new JScrollPane();
			textArea.setText(Messages.getString("PackageInstaller.2")); //$NON-NLS-1$
			textArea.setEditable(false);
			scrollPane.setViewportView(textArea);
			messagePanel.add(scrollPane);
			add(messagePanel, BorderLayout.CENTER);
			
			JPanel buttonPanel = new JPanel();
			buttonPanel.add(button);
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(status == STATUS_CANCEL) {
						cancel();
					} else {
						dispose();
					}
				}
			});
			add(buttonPanel, BorderLayout.SOUTH);

			setModal(true);
			setTitle(Messages.getString("PackageInstaller.3")); //$NON-NLS-1$
			setSize(400, 200);
		}
		
		
		public void setValue(int n) {
			progressBar.setValue(n);
		}
		
		
		public void setText(String text) {
			textArea.setText(text);
		}
		
		
		public void setStatus(int status) {
			this.status = status;
			button.setText(status == STATUS_OK ? STATUS_OK_LABEL : STATUS_CANCEL_LABEL);
		}
		
		public void show(Future<Void> future) {
			this.future = future;
			setVisible(true);
		}
		
		public void cancel() {
			future.cancel(true);
			CorpusManager manager = new CorpusManager();
			manager.delete(new File(newConfigFile));
			newConfigFile = UserSettings.DEFAULT_CONFIG_FILE;
			setText(Messages.getString("PackageInstaller.8") //$NON-NLS-1$
					+ Messages.getString("PackageInstaller.11")); //$NON-NLS-1$
			setStatus(STATUS_OK);
		}
	}
}

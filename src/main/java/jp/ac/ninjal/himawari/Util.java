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
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

public class Util {
	static final String OS_MACOS = "mac"; 
	static final String OS_WINDOWS = "windows"; 
	static final String OS_LINUX = "linux"; 
	
    static void copyFile(Path source, Path target, CopyOption[] options) throws IOException {
    	final Path source2 = source;
    	final Path target2 = target;
    	final CopyOption[] options2 = options;

    	Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
				Path newdir = target2.resolve(source2.relativize(dir).toString());
				try {
					Files.copy(dir, newdir, options2);
				} catch (IOException x) {
					System.err.format("Warning(Util.copyFile): %s is overwritten\n", newdir); //$NON-NLS-1$
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file,	BasicFileAttributes attrs) throws IOException {
				Path newFile = target2.resolve(source2.relativize(file).toString());
				Files.copy(file, newFile, options2);

				return FileVisitResult.CONTINUE;
			}
		});
    }
    
    
    static void copyFile(Path source, Path target, CopyOption[] options, Consumer<Long> totalCopySize) throws IOException {
    	final Path source2 = source;
    	final Path target2 = target;
    	final CopyOption[] options2 = options;
    	final AtomicLong size = new AtomicLong(0);

    	Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
				Path newdir = target2.resolve(source2.relativize(dir).toString());
				try {
					Files.copy(dir, newdir, options2);
				} catch (IOException x) {
					System.err.format("Warning(Util.copyFile): %s is overwritten\n", newdir); //$NON-NLS-1$
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file,	BasicFileAttributes attrs) throws IOException {
				Path newFile = target2.resolve(source2.relativize(file).toString());
				Files.copy(file, newFile, options2);
				size.addAndGet(attrs.size());
				totalCopySize.accept(size.get());

				return FileVisitResult.CONTINUE;
			}
		});
    }
    
    
    static long getFileSize(Path path) throws IOException {
    	AtomicLong fileSize = new AtomicLong(0);
    	
    	Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				fileSize.addAndGet(attrs.size());
				return FileVisitResult.CONTINUE;
			}
			
	         @Override
	         public FileVisitResult postVisitDirectory(Path dir, IOException e)
	             throws IOException
	         {
                return FileVisitResult.CONTINUE;
	         }
		});
    	
    	return fileSize.get();
    }
    
    
    
	public static <T extends Comparable<? super T>> List<T> asSortedList(
			Collection<T> c) {
		List<T> list = new ArrayList<T>(c);
		java.util.Collections.sort(list);
		return list;
	}    
    
    // quote from http://www.ne.jp/asahi/hishidama/home/tech/java/swing/layout.html
    public static JPanel makeDialogPanel(JComponent[][] jc, GroupLayout.Alignment align){
    	JPanel panel = new JPanel();
    	GroupLayout layout = new GroupLayout(panel);
    	panel.setLayout(layout);

    	layout.setAutoCreateContainerGaps(true);
    	layout.setAutoCreateGaps(true);
    	
		int ny = jc.length;
		int nx = jc[0].length;

		SequentialGroup hg = layout.createSequentialGroup();
		for (int x = 0; x < nx; x++) {
			ParallelGroup pg = layout.createParallelGroup();
			for (int y = 0; y < ny; y++) {
				JComponent c = jc[y][x];
				if (c != null) {
					pg.addComponent(c);
				}
			}
			hg.addGroup(pg);
		}
		layout.setHorizontalGroup(hg);

		SequentialGroup vg = layout.createSequentialGroup();
		for (int y = 0; y < ny; y++) {
			ParallelGroup pg = layout
					.createParallelGroup(align);
			for (int x = 0; x < nx; x++) {
				JComponent c = jc[y][x];
				if (c != null) {
					pg.addComponent(c);
				}
			}
			vg.addGroup(pg);
		}
		layout.setVerticalGroup(vg);
		
		return panel;
	}
    
    
    static void showJTextPaneMessageDialog(Component parent, String message, String title){
    	// use JTextPane to allow users to copy the message
    	
		JTextPane jtp = new JTextPane();
		jtp.setContentType("text/html");
		jtp.setText(message);
		jtp.setFont(new Font(Font.DIALOG, Font.PLAIN, jtp.getFont().getSize()));
		
		jtp.setEditable(false);
		jtp.setOpaque(false);
		jtp.setBorder(null); 
		
		JOptionPane.showMessageDialog(parent, jtp, title, JOptionPane.INFORMATION_MESSAGE);
    }

    
    static void showErrorMessages(Component parent, Exception e){
    	final int MAX_LINE_NUMBER = 5;
    	int i = 0;
    	StringBuffer sb = new StringBuffer();
    	sb.append(e.getLocalizedMessage() + "\n\n"); //$NON-NLS-1$
    	for(StackTraceElement s : e.getStackTrace()){
    		sb.append(s.toString());
    		sb.append("\n"); //$NON-NLS-1$
    		if(++i > MAX_LINE_NUMBER){
    			break;
    		}
    	}
    	
		JOptionPane.showConfirmDialog(parent, sb.toString(), Messages.getString("Frame1.191"), //$NON-NLS-1$
				JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
    }


    /**
     * Get an executable path of 32bit commands on 64bit Windows 
     * 
     * @param pathAndCommand
     * @return
     *   pathAndCommand if pathAndCommand exists
     *   pathAndCommand.replace("\\Program Files\\", "\\Program Files (x86)\\") if the replaced pathAndCommand exists 
     */
    public static String modifyPathOnWindows(String pathAndCommand) {
		if(pathAndCommand.contains("\\Program Files\\")){ //$NON-NLS-1$
			File testFile1 = new File(pathAndCommand);
			File testFile2 = new File(pathAndCommand + ".exe"); //$NON-NLS-1$
			if(!testFile1.exists() && !testFile2.exists()){
				String newPathAndCommand = pathAndCommand.replace("\\Program Files\\", "\\Program Files (x86)\\"); //$NON-NLS-1$ //$NON-NLS-2$
				testFile1 = new File(newPathAndCommand);
				testFile2 = new File(newPathAndCommand + ".exe"); //$NON-NLS-1$
				if(testFile1.exists() || testFile2.exists()){
					return newPathAndCommand;
				}
			}
		}
		return pathAndCommand;
	}
    
    
    public static void prettyPrintXML(Node doc){
    	// https://stackoverflow.com/questions/139076/how-to-pretty-print-xml-from-java
    	// answered by Lorenzo Boccaccia
		Transformer transformer;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2"); //$NON-NLS-1$ //$NON-NLS-2$
			//initialize StreamResult with File object to save to file
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);
			String xmlString = result.getWriter().toString();
			System.err.println(xmlString);
		} catch (TransformerFactoryConfigurationError | TransformerException e1) {
			e1.printStackTrace();
		}
    }
    
    
    public static String formatTime(float timeSec) {
		int hour = (int) Math.floor(timeSec / 3600);
		timeSec -= hour * 3600;
		int minute = (int) Math.floor(timeSec / 60);
		int sec = (int) Math.floor(timeSec - minute * 60);
			
    	return String.format("%02d:%02d:%02d", hour, minute, sec); //$NON-NLS-1$
    }
    
    
    public static int strlenSP(String str) {
    	return str.codePointCount(0, str.length());
    }
    
    
    public static String getOSNameLowerCase() {
    	return System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
    }
    
    
    public static String getCannonicalOSName() {
    	String osName = getOSNameLowerCase();
    	
    	if(osName.startsWith(OS_WINDOWS)) {
    		return OS_WINDOWS;
    	} else if(osName.startsWith(OS_MACOS)){
    		return OS_MACOS;
    	} else if(osName.startsWith(OS_LINUX)){
    		return OS_LINUX;
    	} else {
    		return osName;
    	}
    }
    
    
}

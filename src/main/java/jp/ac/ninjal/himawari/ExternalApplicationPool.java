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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;


public class ExternalApplicationPool {
	final static private String KEY_SEPARATOR = "/"; //$NON-NLS-1$
	final static private String MACOS_HIMAWARI_JAR_DIR = "Himawari.app/Contents/Resources"; //$NON-NLS-1$
	final static private String COMMAND_ARGUMENT_PWD = "{{PWD}}"; //$NON-NLS-1$
	
	
	private HashMap<String , ApplicationInfo> map = new HashMap<String, ExternalApplicationPool.ApplicationInfo>();

	
	public ExternalApplicationPool() {
		addDefaultApplications();
	}

	public void addApplication(String name, String os, String[] command) {
		String key = getApplicationKey(name, os);
		if(map.containsKey(key)) {
			System.err.println("Warning: Overwrite application infomation, " + name + "," + os); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		map.put(key, new ApplicationInfo(name, os, command));
	}
	
	
	public ApplicationInfo getApplication(String name, String os) {
		
		String key = getApplicationKey(name, os);
		return map.get(key);
	}
	
	
	public void execute(String name, String os, String[] arguments) throws IOException {
		ApplicationInfo ai = getApplication(name, os);
		
		if(ai == null) {
			return;
		}
		
		ArrayList<String> command = new ArrayList<String>(Arrays.asList(ai.getCommand()));
		
		String pwd = "";
		File himawariJarDir = new File("./"); //$NON-NLS-1$
		if(os.equalsIgnoreCase(Util.OS_MACOS)) {
			pwd = himawariJarDir.getAbsolutePath().replaceFirst("/" + MACOS_HIMAWARI_JAR_DIR + "/\\.$", ""); // directory where Himawari.app is placed //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} else {
			pwd = himawariJarDir.getAbsolutePath();
		}
		for(String argument : arguments) {
			command.add(argument.replaceAll(Pattern.quote(COMMAND_ARGUMENT_PWD), pwd));
		}
		
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.start();
	}
	
	
	public boolean contains(String name, String os) {
		return map.containsKey(getApplicationKey(name, os));
	}
	
	
	private void addDefaultApplications() {
		// VLC
		addApplication("[[VLC]]", Util.OS_WINDOWS, new String[] {"cmd", "/c", "start", "vlc"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		addApplication("[[VLC]]", Util.OS_MACOS, new String[] {"open", "-n", "-a", "VLC", "--args"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		addApplication("[[VLC]]", Util.OS_LINUX, new String[] {"vlc"}); //$NON-NLS-1$ //$NON-NLS-2$
		
		// FishWatchr: arg1-> media file, arg2-> start time
		addApplication("[[FishWatchr_BUILT-IN]]", Util.OS_WINDOWS, new String[] {"resources\\FishWatchr\\FishWatchr.exe"}); //$NON-NLS-1$ //$NON-NLS-2$
		addApplication("[[FishWatchr]]", Util.OS_MACOS, new String[] {"open", "-n", "-a", "FishWatchr", "--args"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		addApplication("[[FishWatchr_BUILT-IN]]", "", new String[] {"java", "-jar", "resources/FishWatchr/fishwatchr.jar"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		
		// Firefox: arg1-> url
		addApplication("[[Firefox]]", Util.OS_WINDOWS, new String[] {"cmd", "/c", "start", "firefox"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		addApplication("[[Firefox]]", Util.OS_MACOS, new String[] {"open", "-a", "firefox"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		addApplication("[[Firefox]]", Util.OS_LINUX, new String[] {"firefox"}); //$NON-NLS-1$ //$NON-NLS-2$
		
		// Chrome: arg1-> url
		addApplication("[[Chrome]]", Util.OS_WINDOWS, new String[] {"cmd", "/c", "start", "chrome"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		addApplication("[[Chrome]]", Util.OS_MACOS, new String[] {"open", "-n", "-b", "com.google.Chrome"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		addApplication("[[Chrome]]", Util.OS_LINUX, new String[] {"chrome"}); //$NON-NLS-1$ //$NON-NLS-2$

		// Safari: arg1-> url
		addApplication("[[Safari]]", Util.OS_MACOS, new String[] {"open", "-a", "safari"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		
		// Microsoft Edge: arg1-> url
		addApplication("[[Edge]]", Util.OS_WINDOWS, new String[] {"cmd", "/c", "start", "msedge"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}
	
	private String getApplicationKey(String name, String os) {
		return name + KEY_SEPARATOR + os.toLowerCase();
	}
	
	
	class ApplicationInfo {
		private String name;
		private String[] command;
		private String os;
		
		public ApplicationInfo(String name, String os, String[] command) {
			this.name = name;
			this.command = command;
			this.os = os;
		}

		public String getName() {
			return name;
		}

		public String[] getCommand() {
			return command;
		}

		public String getArgument() {
			return os;
		}
	}
}

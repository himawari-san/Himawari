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

import java.util.ArrayList;

public class HimawariLogger {

	private ArrayList<HimawariLog> logList = new ArrayList<HimawariLog>();
	
	public HimawariLogger() {
	}

	public void add(String message){
		logList.add(new HimawariPlainLog(message));
	}

	public void add(int mode, String message){
		logList.add(new HimawariPlainLog(mode, message));
	}
	
	public void add(HimawariLog log){
		logList.add(log);
	}

	
	public String write(){
		StringBuilder result = new StringBuilder();
		for(HimawariLog log : logList){
			result.append(log.write());
		}
		return result.toString();
	}
	
	static abstract class HimawariLog {
		static final String BR = "\n"; //$NON-NLS-1$
		static final int MODE_NORMAL = 0;
		static final int MODE_ERROR = 1;

		int mode = MODE_NORMAL;

		abstract String write();
		

		public void setMode(int mode){
			this.mode = mode;
		}
		
		public int getMode(){
			return mode;
		}
	}
	
	static class HimawariPlainLog extends HimawariLog {
		String message;
		
		public HimawariPlainLog(String message){
			this.message = message;
		}

		public HimawariPlainLog(int mode, String message){
			this.message = message;
			this.mode = mode;
		}
		
		public String write(){
			if(mode == HimawariLog.MODE_ERROR){
				return "<p style=\"color:red;\">" + message + "</p>" + BR; //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				return "<p>" + message + "</p>" + BR; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	static class HimawariResultLog extends HimawariLog {
		String message;
		
		public HimawariResultLog(String message){
			this.message = message;
		}
		
		public String write(){
			return "<h1 style=\"text-align:center;\">" + message + "</h1>" + BR; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	static class HimawariListedLog extends HimawariLog {
		String title;
		ArrayList<String> messageList = new ArrayList<String>();
		
		public HimawariListedLog(String title){
			this.title = title;
		}

		public HimawariListedLog(int mode, String title){
			this.mode = mode;
			this.title = title;
		}

		public void add(String message){
			messageList.add(message);
		}
		
		public String write(){
			if(messageList.size() == 0){
				return ""; //$NON-NLS-1$
			}
			
			StringBuilder result = new StringBuilder();
			result.append("<h2>" + title + "</h2>" + BR); //$NON-NLS-1$ //$NON-NLS-2$
			result.append("<ul>" + BR); //$NON-NLS-1$
			for(String message : messageList){
				result.append("<li>" + message + "</li>" + BR); //$NON-NLS-1$ //$NON-NLS-2$
			}
			result.append("</ul>" + BR); //$NON-NLS-1$
			
			return result.toString();
		}
		
		public int getSize(){
			return messageList.size();
		}
	}
}

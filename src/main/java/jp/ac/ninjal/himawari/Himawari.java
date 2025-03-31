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

/**
JTable * @(#)Application1.java	0.9.4 2003-10-20
 *
 * Copyright 2003
 * National Institute for Japanese Language All rights reserved.
 *
 */
package jp.ac.ninjal.himawari;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import java.awt.*;
import java.lang.Thread.UncaughtExceptionHandler;

/**
 * 全文検索システム「ひまわり」
 *
 * @author Masaya YAMAGUCHI
 * @version 0.9.4
 */
public class Himawari {
	boolean packFrame = false;

	/**
	 * Application1 を生成する
	 */
	public Himawari() {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {


                // EDTに例外ハンドラーを設定し、ErrorとRuntimeException を受け取る
                Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                    public void uncaughtException(Thread t, Throwable e) {
                    	//補足できないエラーが発生した場合は、ダイアログでエラーを表示
                    	ErrorMsg errorMsg = new ErrorMsg(e);
                    	errorMsg.showErrorDialog();
                    }
                });

				Frame1 frame = new Frame1();

				// Frame3 frame = new Frame3();
				// validate() はサイズを調整する
				// pack() は有効なサイズ情報をレイアウトなどから取得する
				if (packFrame) {
					frame.pack();
				} else {
					frame.validate();
				}
				// ウィンドウを中央に配置
				Dimension screenSize = Toolkit.getDefaultToolkit()
						.getScreenSize();
				Dimension frameSize = frame.getSize();
				if (frameSize.height > screenSize.height) {
					frameSize.height = screenSize.height;
				}
				if (frameSize.width > screenSize.width) {
					frameSize.width = screenSize.width;
				}
				frame.setLocation((screenSize.width - frameSize.width) / 2,
						(screenSize.height - frameSize.height) / 2);
				frame.setVisible(true);

			}
		});
	}

	/**
	 * main 関数
	 *
	 * @param args
	 *            実行時引数
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		} catch (Exception e) {
			e.printStackTrace();
		}
		new Himawari();
	}
}

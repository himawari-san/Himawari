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

package jp.ac.ninjal.soundplayer;

import java.awt.*;
import javax.swing.*;

/**
 * <p>
 * タイトル:
 * </p>
 * <p>
 * 説明:
 * </p>
 * <p>
 * 著作権: Copyright (c) 2004
 * </p>
 * <p>
 * 会社名:
 * </p>
 * 
 * @author 未入力
 * @version 1.0
 */

public class Application1 {
	boolean packFrame = false;

	// アプリケーションのビルド
	public Application1(String[] args) {
		SoundPlayerFrame frame = new SoundPlayerFrame(args[0], Float.valueOf(args[1]), Float.valueOf(args[2]));
		// validate() はサイズを調整する
		// pack() は有効なサイズ情報をレイアウトなどから取得する
		if (packFrame) {
			frame.pack();
		} else {
			frame.validate();
		}
		// ウィンドウを中央に配置
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
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

	// Main メソッド
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (args.length != 3) {
			System.err
					.println("Usage: java -jar soundplayer.jar SOUND_FILENAME START_TIME END_TIME"); //$NON-NLS-1$
		}
		new Application1(args);
	}
}
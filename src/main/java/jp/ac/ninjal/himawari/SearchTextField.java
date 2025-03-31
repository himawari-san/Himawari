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
 * @(#)SearchTextField.java	0.9.4 2003-10-20
 *
 * Copyright 2003
 * National Institute for Japanese Language All rights reserved.
 *
 */
package jp.ac.ninjal.himawari;

import java.util.*;
import java.util.regex.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * 検索キー用の JTextField
 * 
 * @author Masaya YAMAGUCHI
 * @version 0.9.4
 */
public class SearchTextField extends JTextField {

	private static final long serialVersionUID = 6920016891751494808L;

	final private String REPLACED = Messages.getString("SearchTextField.0"); // 検索文字列で置き換わる //$NON-NLS-1$
	// 検索条件指定
	// メニューで表示する文字列, 検索文字列の修正，否定
	final private String searchExpression[][] = {
			{ Messages.getString("SearchTextField.1"), "^" + REPLACED, "" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			{ Messages.getString("SearchTextField.4"), "^" + REPLACED + "$", "" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			{ Messages.getString("SearchTextField.8"), REPLACED + "$", "" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			{ Messages.getString("SearchTextField.11"), REPLACED, "" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ Messages.getString("SearchTextField.13"), REPLACED, "" }, //$NON-NLS-1$ //$NON-NLS-2$
			{
					Messages.getString("SearchTextField.15"), "^" + REPLACED + "$", "not" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			{ Messages.getString("SearchTextField.19"), "^" + REPLACED, "not" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			{ Messages.getString("SearchTextField.22"), REPLACED + "$", "not" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			{ Messages.getString("SearchTextField.25"), REPLACED, "not" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ Messages.getString("SearchTextField.27"), REPLACED, "not" } //$NON-NLS-1$ //$NON-NLS-2$
	};
	final private int F_TYPE = 0; // メニューで表示する文字列
	final private int F_TEMPL = 1; // 検索文字列の修正
	final private int F_IS_NOT = 2; // 否定

//	private JPopupMenu popupMenu = new JPopupMenu();
	private History history = new History();
	private boolean isEnableRegularExpression = false;
	private int iSearchExpression = 0; // 選択中のメニュー項目番号
	private JComboBox<String> jComboBox_ItemList;

	/**
	 * SearchTextField を生成する 通常の文字列の場合
	 */
	public SearchTextField() {
		jComboBox_ItemList = null;

		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * コンポーネントを初期化する
	 * 
	 * @throws java.lang.Exception
	 */
	private void jbInit() throws Exception {
		this.addMouseListener(new SearchTextField_this_mouseAdapter(this));
		this.addKeyListener(new SearchTextField_this_keyAdapter(this));
	}

	public void setJComboBox(JComboBox<String> jComboBox_ItemList, int iSearchExpression) {
		this.jComboBox_ItemList = jComboBox_ItemList;
		this.iSearchExpression = iSearchExpression;

		for (int i = 0; i < searchExpression.length; i++) {
			jComboBox_ItemList.addItem(searchExpression[i][F_TYPE]);
		}
		jComboBox_ItemList.setSelectedIndex(iSearchExpression);

	}

	public String getModifiedSearchKey() {
		if (jComboBox_ItemList == null) {
			return getText();
		} else {
			String text = getText();
			if (text.compareTo("") == 0) { //$NON-NLS-1$
				return ""; //$NON-NLS-1$
			} else {
				iSearchExpression = jComboBox_ItemList.getSelectedIndex();
				int p = searchExpression[iSearchExpression][F_TEMPL]
						.indexOf(REPLACED);
				// REPLACED を置き換え
				return searchExpression[iSearchExpression][F_TEMPL].substring(
						0, p)
						+ text
						+ searchExpression[iSearchExpression][F_TEMPL]
								.substring(p + REPLACED.length());
				// return
				// searchExpression[iSearchExpression][1].replaceFirst(REPLACED,
				// text);
			}
		}
	}

	public ArrayList<String> expandKey() {
		ArrayList<ArrayList<String>> keyFlagments = new ArrayList<ArrayList<String>>();
		String sourceKey = getText();

		// キー中の文字クラスを展開
		Pattern p = Pattern.compile("(.*?)\\[(.+?)]"); //$NON-NLS-1$
		Matcher m = p.matcher(sourceKey);
		int cp = 0;
		while (m.find()) {
			if (m.group(1).compareTo("") != 0) { //$NON-NLS-1$
				ArrayList<String> oneElemnetVector = new ArrayList<String>();
				oneElemnetVector.add(m.group(1));
				keyFlagments.add(oneElemnetVector);
			}
			String chars = m.group(2);
			ArrayList<String> charList = new ArrayList<String>(chars.length());
			for (int i = 0; i < chars.length(); i++) {
				charList.add(chars.substring(i, i + 1));
			}
			keyFlagments.add(charList);
			cp = m.end();
		}
		if (cp < sourceKey.length()) {
			ArrayList<String> oneElemnetVector = new ArrayList<String>();
			oneElemnetVector.add(sourceKey.substring(cp));
			keyFlagments.add(oneElemnetVector);
		}

		return makeKeyCombination(keyFlagments);
	}

	public boolean isNot() {
		if (searchExpression[iSearchExpression][F_IS_NOT].compareTo("not") == 0) { //$NON-NLS-1$
			return true;
		} else {
			return false;
		}
	}

	private ArrayList<String> makeKeyCombination(ArrayList<ArrayList<String>> keyFlagments) {
		int n = keyFlagments.size();
		ArrayList<String> resSet = new ArrayList<String>();

		if (n == 0) {
			return null;
		} else if (n == 1) {
			return keyFlagments.get(0);
		} else {
			ArrayList<String> flagmentsHead = keyFlagments.remove(0);
			ArrayList<String> flagmentsTail = makeKeyCombination(keyFlagments);

			for (int i = 0; i < flagmentsHead.size(); i++) {
				for (int j = 0; j < flagmentsTail.size(); j++) {
					String key = flagmentsHead.get(i) + flagmentsTail.get(j);
					if(!resSet.contains(key)){
						resSet.add(key);
					}
				}
			}
		}
		return resSet;
	}

	public void registerKeyHistory() {
		history.register(this.getText());
	}

	public void registerKeyHistory(String value) {

		// nullの場合は何もせず終了
		if (value == null)
			return;

		history.register(value);
	}

	public void registerKeyHistory(String[] array) {

		for (int i = 0; i < array.length; i++) {
			// nullの場合はスキップ
			if (array[i] == null)
				continue;

			// historyの登録
			history.register(array[i]);
		}
	}

	public void setSearchExpression(int num) {
		if (num >= 0) {
			iSearchExpression = num;
			isEnableRegularExpression = true;
			setTip();
		} else {
			isEnableRegularExpression = false;
			setTip();
		}
	}

	private void setTip() {
		if (isEnableRegularExpression) {
			setToolTipText(searchExpression[iSearchExpression][F_TYPE]);
		} else {
			setToolTipText(Messages.getString("SearchTextField.34")); //$NON-NLS-1$
		}
	}

	void this_keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();

		if (keyCode == KeyEvent.VK_UP) {
			setText(history.forward());
		} else if (keyCode == KeyEvent.VK_DOWN) {
			setText(history.backward());
		}
	}

	class SearchTextField_this_keyAdapter extends java.awt.event.KeyAdapter {
		SearchTextField adaptee;

		SearchTextField_this_keyAdapter(SearchTextField adaptee) {
			this.adaptee = adaptee;
		}

		public void keyPressed(KeyEvent e) {
			adaptee.this_keyPressed(e);
		}
	}

	void this_mouseClicked(MouseEvent e) {

		if (e.isMetaDown()) {
			JPopupMenu popupMenu = new JPopupMenu();
			HashSet<String> hs = new HashSet<String>();
			JMenuItem menuItem;

			for (int i = history.size()-1; i >= 0; i--) {
				hs.add(history.get(i));
				menuItem = new JMenuItem((String) history.get(i));
				menuItem.addMouseListener(new this_popup_mouseAdapter(this));
				popupMenu.add(menuItem);
			}
			popupMenu.addSeparator();

			menuItem = new JMenuItem(Messages.getString("SearchTextField.37")); //$NON-NLS-1$
			menuItem.addMouseListener(new this_cut_mouseAdapter(this));
			popupMenu.add(menuItem);

			menuItem = new JMenuItem(Messages.getString("SearchTextField.38")); //$NON-NLS-1$
			menuItem.addMouseListener(new this_copy_mouseAdapter(this));
			popupMenu.add(menuItem);

			menuItem = new JMenuItem(Messages.getString("SearchTextField.39")); //$NON-NLS-1$
			menuItem.addMouseListener(new this_paste_mouseAdapter(this));
			popupMenu.add(menuItem);

			popupMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	class this_paste_mouseAdapter extends java.awt.event.MouseAdapter {
		SearchTextField adaptee;

		this_paste_mouseAdapter(SearchTextField adaptee) {
			this.adaptee = adaptee;
		}

		public void mouseReleased(MouseEvent e) {
			adaptee.paste();
		}
	}

	class this_cut_mouseAdapter extends java.awt.event.MouseAdapter {
		SearchTextField adaptee;

		this_cut_mouseAdapter(SearchTextField adaptee) {
			this.adaptee = adaptee;
		}

		public void mouseReleased(MouseEvent e) {
			adaptee.cut();
		}
	}

	class this_copy_mouseAdapter extends java.awt.event.MouseAdapter {
		SearchTextField adaptee;

		this_copy_mouseAdapter(SearchTextField adaptee) {
			this.adaptee = adaptee;
		}

		public void mouseReleased(MouseEvent e) {
			adaptee.copy();
		}
	}

	class this_popup_mouseAdapter extends java.awt.event.MouseAdapter {
		SearchTextField adaptee;

		this_popup_mouseAdapter(SearchTextField adaptee) {
			this.adaptee = adaptee;
		}

		public void mouseReleased(MouseEvent e) {
			adaptee.setText(((JMenuItem) (e.getSource())).getText());
		}
	}

	class SearchTextField_this_mouseAdapter extends java.awt.event.MouseAdapter {
		SearchTextField adaptee;

		SearchTextField_this_mouseAdapter(SearchTextField adaptee) {
			this.adaptee = adaptee;
		}

		public void mouseClicked(MouseEvent e) {
			adaptee.this_mouseClicked(e);
		}
	}

	/**
	 * @return history
	 */
	public History getHistory() {
		return history;
	}

	/**
	 * @param history
	 *            セットする history
	 */
	public void setHistory(History history) {
		this.history = history;
	}
}

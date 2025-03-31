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
 * @(#)SearchResultFrame.java	0.9.7 2004-01-20
 *
 * Copyright 2003
 * National Institute for Japanese Language All rights reserved.
 */

package jp.ac.ninjal.himawari;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.JTableHeader;

import org.apache.commons.lang3.StringUtils;

/**
 * 検索結果用の JFrame
 * 
 * @author Masaya YAMAGUCHI
 * @version 0.9.7
 */
public class SearchResultFrame extends JFrame {
	private static final long serialVersionUID = 1344462942127918449L;

	final static int FRAME_HEIGHT = 400; // frame の高さ

	JMenuBar jMenuBar = new JMenuBar();

	JScrollPane jScrollPane1 = new JScrollPane();

	JPanel jPanel2 = new JPanel();

	ResultTable jTable = new ResultTable();

	GridLayout gridLayout1 = new GridLayout();

	JPanel jPanel3 = new JPanel();

	GridLayout gridLayout2 = new GridLayout();

	JLabel jLabel_Statusbar = new JLabel();

	TitledBorder titledBorder1;

	JPanel jPanel1 = new JPanel();

	JTextField jTextField_SelectedValue = new JTextField();

	GridLayout gridLayout3 = new GridLayout();

	GridBagLayout gridBagLayout1 = new GridBagLayout();

	ResultTableModel resultTableModel1 = new ResultTableModel();

	JMenu jMenuFile = new JMenu();
	JMenuItem jMenuItemExit = new JMenuItem();

	JMenu jMenuEdit = new JMenu();
	JMenuItem jMenuItemSearch = new JMenuItem();
	JMenuItem jMenuItemCopy = new JMenuItem();
	JMenuItem jMenuItemCopyWithHeader = new JMenuItem();
	JMenuItem jMenuItemJoin = new JMenuItem();
	JMenuItem jMenuItemAccumulate = new JMenuItem();
	JMenuItem jMenuItemReplace = new JMenuItem();

	JMenu jMenuTool = new JMenu();
	JMenu jMenuItemSort = new JMenu();
	JMenuItem jMenuItemSortAscend = new JMenuItem();
	JMenuItem jMenuItemSortDescend = new JMenuItem();
	JMenuItem jMenuItemSortRandom = new JMenuItem();
	JMenu jMenuItemFilter = new JMenu();
	JMenuItem jMenuItemFilterExecute = new JMenuItem();
	JMenuItem jMenuItemFilterClear = new JMenuItem();
	
	Frame1 parent;
	String targetFieldNames[];
	String frameTitle;
	FieldInfo fieldInfo;

	static ResultTable copiedTable;
	static int[] srcSelectedColumns;
	static String myClipboard = ""; //$NON-NLS-1$
	
	static int serial = 0;
	
	public SearchResultFrame(Frame1 parent) throws HeadlessException {
		this.parent = parent;
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		serial++;
	}

	private void jbInit() throws Exception {
		// set maximum frame size
		setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
		
		jTable.setParentFrame(parent);
		titledBorder1 = new TitledBorder(""); //$NON-NLS-1$
		this.getContentPane().setLayout(gridBagLayout1);
		jPanel2.setLayout(gridLayout1);
		gridLayout1.setColumns(1);
		this.setLocale(java.util.Locale.getDefault());
		this.setSize(new Dimension(300, 500));
		jPanel3.setAlignmentX((float) 0.5);
		jPanel3.setLayout(gridLayout2);
		jLabel_Statusbar.setBorder(BorderFactory.createEtchedBorder());
		jLabel_Statusbar.setText(""); //$NON-NLS-1$
		jScrollPane1
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jScrollPane1
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		jTable.addMouseListener(new SearchResultFrame_jTable1_mouseAdapter(
				this));
		jPanel1.setBorder(BorderFactory.createLoweredBevelBorder());
		jPanel1.setLayout(gridLayout3);
		jTextField_SelectedValue.setText(""); //$NON-NLS-1$
		jTable.addKeyListener(new SearchResultFrame_jTable1_keyAdapter(this));

		jMenuFile.setText(Messages.getString("SearchResultFrame.3")); //$NON-NLS-1$
		jMenuItemExit.setText(Messages.getString("SearchResultFrame.4")); //$NON-NLS-1$
		jMenuItemExit.setAccelerator(KeyStroke.getKeyStroke('Q',
				KeyEvent.CTRL_DOWN_MASK, false));
		jMenuItemExit
				.addActionListener(new SearchResultFrame_jMenuItem_exit_actionAdapter(
						this));

		jMenuEdit.setText(Messages.getString("SearchResultFrame.5")); //$NON-NLS-1$
		jMenuItemSearch.setText(Messages.getString("SearchResultFrame.6")); //$NON-NLS-1$
		jMenuItemSearch.setAccelerator(KeyStroke.getKeyStroke('F',
				KeyEvent.CTRL_DOWN_MASK, false));
		jMenuItemSearch.addActionListener(
				new SearchResultFrame_jMenuItem_search_actionAdapter(this));

		jMenuItemReplace.setText(Messages.getString("SearchResultFrame.13")); //$NON-NLS-1$
		jMenuItemReplace.setAccelerator(KeyStroke.getKeyStroke('R',
				KeyEvent.CTRL_DOWN_MASK, false));
		jMenuItemReplace.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				jTable.invokeReplace();
			}
		});
		
		jMenuItemCopy.setText(Messages.getString("SearchResultFrame.2")); //$NON-NLS-1$
		jMenuItemCopy.setAccelerator(KeyStroke.getKeyStroke('C',
				KeyEvent.CTRL_DOWN_MASK, false));
		jMenuItemCopyWithHeader.setText(Messages.getString("SearchResultFrame.8")); //$NON-NLS-1$
		jMenuItemCopyWithHeader.setAccelerator(KeyStroke.getKeyStroke('C',
				KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, false));
		jMenuItemJoin.setText(Messages.getString("SearchResultFrame.10")); //$NON-NLS-1$
		jMenuItemJoin.setAccelerator(KeyStroke.getKeyStroke('J',
				KeyEvent.CTRL_DOWN_MASK, false));
		jMenuItemAccumulate.setText(Messages.getString("SearchResultFrame.12")); //$NON-NLS-1$
		jMenuItemAccumulate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				jTable.invokeAccumulate();
			}
		});
		
		jMenuItemCopy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				copyDataToClipboard();
			}
		});
		jMenuItemCopyWithHeader.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				copyDataWithHeaderToClipboard();
			}
		});
		jMenuItemJoin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					jTable.invokeJoin(copiedTable, srcSelectedColumns);
					myClipboard = ""; //$NON-NLS-1$
				} catch (IllegalArgumentException e) {
					JOptionPane.showMessageDialog(SearchResultFrame.this, e.getMessage());
					e.printStackTrace();
				}
			}
		});
		
		
		jMenuTool.setText(Messages.getString("Frame1.29")); //$NON-NLS-1$
		jMenuTool.add(jMenuItemSort);
		jMenuTool.add(jMenuItemFilter);
		jMenuItemSort.setText(Messages.getString("Frame1.57")); //$NON-NLS-1$
		jMenuItemSort.add(jMenuItemSortAscend);
		jMenuItemSort.add(jMenuItemSortDescend);
		jMenuItemSort.add(jMenuItemSortRandom);
		jMenuItemSortAscend.setText(Messages.getString("Frame1.59")); //$NON-NLS-1$
		jMenuItemSortAscend.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int columnIndex = jTable.getSelectedColumn();
				if (columnIndex != -1) {
					jTable.sortDataKeepingSelectedPosition(columnIndex, true);
				} else {
					JOptionPane.showMessageDialog(SearchResultFrame.this, Messages.getString("Frame1.450")); //$NON-NLS-1$
				}
			}
		});
		jMenuItemSortDescend.setText(Messages.getString("Frame1.61")); //$NON-NLS-1$
		jMenuItemSortDescend.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int columnIndex = jTable.getSelectedColumn();
				if (columnIndex != -1) {
					jTable.sortDataKeepingSelectedPosition(columnIndex, false);
				} else {
					JOptionPane.showMessageDialog(SearchResultFrame.this, Messages.getString("Frame1.450")); //$NON-NLS-1$
				}
			}
		});
		jMenuItemSortRandom.setText(Messages.getString("Frame1.62")); //$NON-NLS-1$
		jMenuItemSortRandom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				((ResultTableModel) jTable.getModel()).sortDataRandamly();
			}
		});

		jMenuItemFilter.setText(Messages.getString("Frame1.63")); //$NON-NLS-1$
		jMenuItemFilter.add(jMenuItemFilterExecute);
		jMenuItemFilter.add(jMenuItemFilterClear);
		jMenuItemFilterExecute.setText(Messages.getString("Frame1.64")); //$NON-NLS-1$
		jMenuItemFilterExecute.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int columnIndex = jTable.getSelectedColumn();
				if (columnIndex != -1) {
					JTableHeader selectedHeader = jTable.getTableHeader();
					String headerName = (String) selectedHeader.getColumnModel()
							.getColumn(columnIndex).getHeaderValue();

					// calculate the place of a popup menu
					int x = 0;
					for (int i = 0; i < columnIndex; i++) {
						x += selectedHeader.getColumnModel().getColumn(i).getWidth();
					}
					x += selectedHeader.getColumnModel().getColumn(columnIndex)
							.getWidth() / 2;
					jTable.invokeDialogForFiltering(headerName, jTable,
							x, selectedHeader.getY() - selectedHeader.getHeight() / 2);
				} else {
					JOptionPane.showMessageDialog(SearchResultFrame.this, Messages.getString("Frame1.452")); //$NON-NLS-1$
				}
			}
		});
		jMenuItemFilterClear.setText(Messages.getString("Frame1.65")); //$NON-NLS-1$
		jMenuItemFilterClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				((ResultTableModel) jTable.getModel()).initFilter();
			}
		});

		
		this.getContentPane().add(
				jPanel2,
				new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(5, 0, 0, 0), 347, 29));
		jPanel2.add(jScrollPane1, null);
		jScrollPane1.getViewport().add(jTable, null);
		this.getContentPane().add(
				jPanel3,
				new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0,
						GridBagConstraints.SOUTH,
						GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0),
						797, 1));
		jPanel3.add(jLabel_Statusbar, null);
		this.getContentPane().add(
				jPanel1,
				new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
						GridBagConstraints.CENTER,
						GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0),
						793, 4));
		jPanel1.add(jTextField_SelectedValue, null);
		jMenuBar.add(jMenuFile);
		jMenuBar.add(jMenuEdit);
		jMenuBar.add(jMenuTool);
		
		jMenuFile.add(jMenuItemExit);

		jMenuEdit.add(jMenuItemSearch);
		jMenuEdit.add(jMenuItemReplace);
		jMenuEdit.add(jMenuItemCopy);
		jMenuEdit.add(jMenuItemCopyWithHeader);
		jMenuEdit.add(jMenuItemJoin);
		jMenuEdit.add(jMenuItemAccumulate);
		jMenuEdit.addMenuListener(new MenuListener() {
			
			@Override
			public void menuSelected(MenuEvent arg0) {
				int selectedColumns[] = jTable.getSelectedColumns();
				

				jMenuItemJoin.setEnabled(true);
				jMenuItemAccumulate.setEnabled(true);

				if(selectedColumns.length == 0){
					jMenuItemJoin.setEnabled(false);
					jMenuItemAccumulate.setEnabled(false);
					return;
				}

				if(myClipboard.isEmpty() || !myClipboard.contains("\n") || srcSelectedColumns == null || copiedTable == null){ //$NON-NLS-1$
					jMenuItemJoin.setEnabled(false);
				}

				if(selectedColumns.length > 1 
						|| ((ResultTableModel)jTable.getModel()).getFieldInfo().getIndexFreq() == jTable.convertColumnIndexToModel(selectedColumns[0])){
					jMenuItemAccumulate.setEnabled(false);
				}
			}
			
			@Override
			public void menuDeselected(MenuEvent arg0) {
			}
			
			@Override
			public void menuCanceled(MenuEvent arg0) {
			}
		});

		setJMenuBar(jMenuBar);

		resultTableModel1.addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				setStatusBar();
			}
		});
		jTable.setModel(resultTableModel1);
	}

	/**
	 * テーブルを返す
	 * 
	 * @return テーブル
	 */
	public ResultTable getTable() {
		return jTable;
	}

	/**
	 * status bar に文字列を出力する
	 * 
	 * @param message
	 *            出力する文字列
	 */
	public void setStatusBar(String message) {
		jLabel_Statusbar.setText(message);
	}

	
	public void setStatusBar() {
		String message = ""; //$NON-NLS-1$

		if (resultTableModel1.dataSize() != resultTableModel1.filteredDataSize()) {
			message = Messages.getString("SearchResultFrame.11") + resultTableModel1.sumFreq() + ", " + resultTableModel1.sumFreqFilteredData(); //$NON-NLS-1$ //$NON-NLS-2$
			if(resultTableModel1.getFieldInfo().getIndexFreq() != -1){
				message += " / " + Messages.getString("SearchResultFrame.14") + resultTableModel1.dataSize() + ", " + resultTableModel1.filteredDataSize(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		} else {
			message = Messages.getString("Frame1.436") + resultTableModel1.sumFreq(); //$NON-NLS-1$
			if(resultTableModel1.getFieldInfo().getIndexFreq() != -1){
				message += ", " + Messages.getString("Frame1.437") + resultTableModel1.dataSize(); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		setStatusBar(message);
	}

	
	public void setFontSize(int fontsize) {
		jTable.setFontSize(fontsize);
		jTable.setRowHeight(jTable.getFontMetrics(jTable.getFont()).getHeight());
		jLabel_Statusbar.setFont(new java.awt.Font("Dialog", 0, Frame1.DEFAULT_FONT_SIZE)); //$NON-NLS-1$
		jTextField_SelectedValue.setFont(new java.awt.Font(
				"Dialog", 0, fontsize)); //$NON-NLS-1$
	}

	public void setViewPoint() {
		// System.out.println(jScrollPane1.getViewport().getViewPosition().getY());
	}
	
	@Override
	public void setTitle(String title) {
		// TODO Auto-generated method stub
		super.setTitle("[" + serial + "] " + title); //$NON-NLS-1$ //$NON-NLS-2$
	}


	public void copyDataToClipboard(){
		srcSelectedColumns = null;
		copiedTable = null;
		jTable.copySelectedDataToClipboard(false);
	}

	
	public void copyDataWithHeaderToClipboard(){
		jTable.copySelectedDataToClipboard(true);

		srcSelectedColumns = jTable.getSelectedColumns();
		copiedTable = jTable;
		try {
			myClipboard = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
		} catch (HeadlessException | UnsupportedFlavorException
				| IOException e) {
			e.printStackTrace();
		}
	}
	
	

	/**
	 * マウスで選択している項目を jTextField_selectedValue に出力する
	 * 
	 * @param e
	 *            MouseEvent
	 */
	void jTable1_mouseClicked(MouseEvent e) {
		// 右ボタン，シングルクリック
		if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
			Object selectedValue = jTable.getValueAt(jTable.getSelectedRow(), jTable.getSelectedColumn());
			if(selectedValue == null){
				selectedValue = ""; //$NON-NLS-1$
			}
			jTextField_SelectedValue.setText(selectedValue.toString());
			return;
		}
	}

	/**
	 * カーソル移動時に選択している項目を jTextField_selectedValue に出力する
	 * 
	 * @param e
	 *            KeyEvent
	 */
	void jTable1_keyReleased(KeyEvent e) {
		if (jTable.getSelectedRow() >= 0) {
			Object selectedValue = jTable.getValueAt(jTable.getSelectedRow(), jTable.getSelectedColumn());
			if(selectedValue == null){
				selectedValue = ""; //$NON-NLS-1$
			}
			jTextField_SelectedValue.setText(selectedValue.toString());
		}
		return;
	}

	void jMenuItem_exit_actionPerformed(ActionEvent e) {
		this.dispose();
	}

	void jMenuItem_search_actionPerformed(ActionEvent e) {
		int rowIndex = jTable.getSelectedRow();
		jTable.invokeDialogForFind(rowIndex);
	}
	
	public void setFieldInfo(UserSettings userSetting, String statName){
		targetFieldNames = userSetting.getAttributeList(statName, "name"); //$NON-NLS-1$
		frameTitle = userSetting.getAttribute(statName, "label"); //$NON-NLS-1$
		fieldInfo = FieldInfo.readFieldDiscription(userSetting, statName);
	}

	public void setFieldInfo(FieldInfo fieldInfo){
		this.fieldInfo = fieldInfo;
		targetFieldNames = fieldInfo.getNames();
		frameTitle = StringUtils.join(fieldInfo.getNames(), ","); //$NON-NLS-1$
	}

	
	public void invokeStatisticsFrame(ResultTable selectedTable, int fontsize, boolean isConfirmed) {
		ResultTableModel selectedTableModel = (ResultTableModel) selectedTable
				.getModel();

		RecordStatistics rs;
		int iFreq = selectedTableModel.getFieldInfo().getIndexFreq();
		if (iFreq != -1 && isConfirmed) {
			JOptionPane jpane = new JOptionPane(
					Messages.getString("SearchResultFrame.0"), //$NON-NLS-1$
					JOptionPane.QUESTION_MESSAGE,
					JOptionPane.YES_NO_CANCEL_OPTION);
			JDialog dialog = jpane.createDialog(Messages
					.getString("SearchResultFrame.1")); //$NON-NLS-1$
			dialog.setLocationByPlatform(true);
			dialog.setVisible(true);
			
			if(jpane.getValue() == null){
				return; // closed by the close button
			}
			int option = (Integer) jpane.getValue();

			if (option == JOptionPane.YES_OPTION) {
				rs = new RecordStatistics(fieldInfo, true);
			} else if (option == JOptionPane.NO_OPTION) {
				rs = new RecordStatistics(fieldInfo, false);
			} else {
				return;
			}
		} else {
			rs = new RecordStatistics(fieldInfo); // uniq
		}
		rs.add(selectedTableModel.getFilteredData());
		invokeStatisticsFrame(rs.getResults(), fontsize);
	}
	
	
	public void invokeStatisticsFrame(ArrayList<ResultRecord> resultRecords, int fontsize) {
		ResultTableModel tableModel = (ResultTableModel) jTable.getModel();
		tableModel.setFieldInfo(fieldInfo);
		tableModel.setData(resultRecords);
		tableModel.sortDataWithMultiColumn();

		jTable.setFontSize(fontsize);
		jTable.setColumnProperty(fieldInfo);
		setStatusBar();
		setTitle(Messages.getString("Frame1.438") + frameTitle); //$NON-NLS-1$
		setFontSize(fontsize);

		// frame の横幅を決定
		int xFrameSize = 0;
		for (int i = 0; i < fieldInfo.size(); i++) {
			xFrameSize += jTable.getTableHeader().getColumnModel().getColumn(i).getWidth();
		}
		xFrameSize += 30; // すこし余裕を持たせる
		setSize(xFrameSize, FRAME_HEIGHT);
		setLocationByPlatform(true);
		setVisible(true);
	}

	
}

class SearchResultFrame_jTable1_mouseAdapter extends
		java.awt.event.MouseAdapter {
	SearchResultFrame adaptee;

	SearchResultFrame_jTable1_mouseAdapter(SearchResultFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void mouseClicked(MouseEvent e) {
		adaptee.jTable1_mouseClicked(e);
	}

	
}

class SearchResultFrame_jTable1_keyAdapter extends java.awt.event.KeyAdapter {
	SearchResultFrame adaptee;

	SearchResultFrame_jTable1_keyAdapter(SearchResultFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void keyReleased(KeyEvent e) {
		adaptee.jTable1_keyReleased(e);
	}
}

class SearchResultFrame_jMenuItem_exit_actionAdapter implements
		java.awt.event.ActionListener {
	SearchResultFrame adaptee;

	SearchResultFrame_jMenuItem_exit_actionAdapter(SearchResultFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.jMenuItem_exit_actionPerformed(e);
	}
}

class SearchResultFrame_jMenuItem_search_actionAdapter implements
		java.awt.event.ActionListener {
	SearchResultFrame adaptee;

	SearchResultFrame_jMenuItem_search_actionAdapter(SearchResultFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.jMenuItem_search_actionPerformed(e);
	}
}

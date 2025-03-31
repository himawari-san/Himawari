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
 * @(#)ResultTable.java 	0.9.4 2003-10-20
 *
 * Copyright 2003
 * National Institute for Japanese Language All rights reserved.
 */
package jp.ac.ninjal.himawari;

import java.util.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.*;


/**
 * 結果表示用の JTable
 *
 * @author Masaya YAMAGUCHI
 * @version 0.9.4
 */
public class ResultTable extends JTable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final static int BASE_FONT_SIZE = 14;
	final static int DEFAULT_FREQ_COLUMN_WIDTH = 100;

	final String LABEL_FILTER_CANCEL = Messages.getString("ResultTable.0"); //$NON-NLS-1$

	final String LABEL_KEY_SET = Messages.getString("ResultTable.1"); //$NON-NLS-1$
	
	final String LABEL_KEY_REPLACE = Messages.getString("ResultTable.5"); //$NON-NLS-1$

	final String LABEL_KEY_EMPTY = Messages.getString("ResultTable.15"); //$NON-NLS-1$

	final String LABEL_KEY_ACCUMULATE = Messages.getString("ResultTable.30"); //$NON-NLS-1$

	final int MAX_POPUP_ITEMS = 20;

	private ResultTableModel tableModel = new ResultTableModel();

	private ArrayList<DefaultTableCellRenderer> renderers = new ArrayList<DefaultTableCellRenderer>();

	//save 可能フラグ
	private boolean saveEnabled = false;

	private Frame1 parentFrame;

	public final static String BLANK_STR = ""; //$NON-NLS-1$
	public final static char REPLACED_CONTROL_CODE = ' '; //$NON-NLS-1$

	private ResultTable adaptee = this; // アダプタのための参照用

	private String preservedFindKey = ""; //$NON-NLS-1$
	private String preservedReplaceKey = ""; //$NON-NLS-1$
	
	/**
	 * ResultTable を生成する
	 */
	public ResultTable() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * テーブルモデルを設定する
	 *
	 * @param tm
	 *            テーブルモデル
	 */
	public void setModel(ResultTableModel tm) {

		if(parentFrame != null){
			tm.setParentFrame(parentFrame);
		}

		super.setModel(tm);
		tableModel = tm;
	}


	public class MyTableHeaderRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;
		private DefaultTableCellRenderer defaultRenderer;
		final Icon ascendingSortIcon = UIManager.getIcon("Table.ascendingSortIcon"); //$NON-NLS-1$
		final Icon descendingSortIcon = UIManager.getIcon("Table.descendingSortIcon"); //$NON-NLS-1$
		final JTableHeader header = ResultTable.this.getTableHeader();
		
		public MyTableHeaderRenderer(DefaultTableCellRenderer defaultRenderer) {
			this.defaultRenderer = defaultRenderer;
		}
		
		
		@Override
	    public Component getTableCellRendererComponent(
	            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	    	DefaultTableCellRenderer rendererComponent = (DefaultTableCellRenderer) defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

	    	FieldInfo fieldInfo = tableModel.getFieldInfo();
	    	int fieldNo = fieldInfo.get(rendererComponent.getText());

	    	rendererComponent.setHorizontalTextPosition(SwingConstants.LEFT);
	    	
	    	if(fieldNo != -1 && !fieldInfo.getType(fieldNo).equals(FieldInfo.TYPE_INDEX)){
	    		if(fieldInfo.getFieldStatus(fieldNo) == FieldInfo.STATUS_SORT_ASCENDING){
			    	rendererComponent.setIcon(ascendingSortIcon);
	    		} else if(fieldInfo.getFieldStatus(fieldNo) == FieldInfo.STATUS_SORT_DESCENDING){
			    	rendererComponent.setIcon(descendingSortIcon);
	    		} else {
			    	rendererComponent.setIcon(null);
	    		}
	    	}
	    	
	        return rendererComponent;
	    }
	}

	
	
	/**
	 * コンポーネントを初期化する
	 *
	 * @throws java.lang.Exception
	 */
	private void jbInit() throws Exception {
		this.setGridColor(Color.GRAY);
		this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.setCellSelectionEnabled(true);
		this.addKeyListener(new ResultTable_this_keyAdapter(this));
		this.addMouseListener(new ResultTable_cell_mouseAdapter(this));
		this.getTableHeader().addMouseListener(
				new ResultTable_header_mouseAdapter(this));
		this.getTableHeader().addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				int iColumn = columnAtPoint(e.getPoint());
				if(iColumn >= 0){
					((JTableHeader)e.getSource()).setToolTipText(getColumnName(iColumn));
				}
			}
		});
		
		//フォーカスアダプタの設定
        DefaultCellEditor dce = (DefaultCellEditor)this.getDefaultEditor(Object.class);
        dce.getComponent().addFocusListener(new ResultTable_focusAdapter(this));

    	DefaultTableCellRenderer defaultRenderer = (DefaultTableCellRenderer)getTableHeader().getDefaultRenderer();
       
    	// renderer for showing arrows of the sort order
        this.getTableHeader().setDefaultRenderer(new MyTableHeaderRenderer(defaultRenderer));

	}

	/**
	 * フィルターを設定する
	 *
	 * @param headerName
	 *            ヘッダ名
	 * @param selectedValue
	 *            設定の種類(解除，文字列指定)
	 */
	private void callFilter(String headerName, String selectedValue) {
		if (selectedValue == null || selectedValue.equals(LABEL_KEY_EMPTY)) {
			selectedValue = ""; //$NON-NLS-1$
		}

		if (selectedValue.compareTo(LABEL_FILTER_CANCEL) == 0) {
			// フィルタ解除
			tableModel.initFilter();
		} else if (selectedValue.compareTo(LABEL_KEY_SET) == 0) {
			// 文字列指定
			selectedValue = (String) JOptionPane
					.showInputDialog(
							null,
							Messages.getString("ResultTable.2"), Messages.getString("ResultTable.3"), //$NON-NLS-1$ //$NON-NLS-2$
							JOptionPane.INFORMATION_MESSAGE, null, null, ""); //$NON-NLS-1$
			if (selectedValue != null) {
				HashMap<String, String> filter = new HashMap<String, String>();
				filter.put(headerName, selectedValue);
				tableModel.filterData_with_regex(filter);
			}
		} else if (selectedValue.equals(LABEL_KEY_ACCUMULATE)) {
			FieldInfo thisFieldInfo = tableModel.getFieldInfo();
			int iAccumulatedHeader = thisFieldInfo.get(headerName);
			
			RecordStatistics rs = new RecordStatistics(thisFieldInfo, true);
			FieldInfo newFieldInfo = rs.accumulate(tableModel.getFilteredData(), iAccumulatedHeader);
			if(rs.getErrors() > 0){
				JOptionPane.showMessageDialog(ResultTable.this,
						rs.getErrors() + Messages.getString("ResultTable.7"), Messages.getString("ResultTable.14"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				ResultTable newTable = new ResultTable();
				ResultTableModel newTableModel = new ResultTableModel();
				newTableModel.setFieldInfo(newFieldInfo);
				newTableModel.setData(rs.getResults(iAccumulatedHeader));
				newTable.setModel(newTableModel);
				parentFrame.invokeSelectedStatFrame(newTable, newFieldInfo, false);
			}
			
		} else {
			HashMap<String, String> filter = new HashMap<String, String>();
			filter.put(headerName, selectedValue);
			tableModel.filterData(filter);
		}
	}

	
	class MyDialog extends JDialog implements ActionListener {
		private static final long serialVersionUID = 1L;
		private String strFrom = ""; //$NON-NLS-1$
		private String strTo = ""; //$NON-NLS-1$
		private int returnValue = JOptionPane.CANCEL_OPTION;

		private JButton btnOk = new JButton(Messages.getString("ResultTable.8")); //$NON-NLS-1$
		private JButton btnCancel = new JButton(Messages.getString("ResultTable.9")); //$NON-NLS-1$
		private JTextField jtfFrom = new JTextField();
		private JTextField jtfTo = new JTextField();
		
		public MyDialog(Frame owner){
			super(owner);
			
			Container c = getContentPane();

			JPanel panelForm = new JPanel();
			GroupLayout gLayout = new GroupLayout(panelForm);
			panelForm.setLayout(gLayout);
			
			JPanel panelButton = new JPanel();
			panelButton.setLayout(new FlowLayout());

			c.add(BorderLayout.CENTER, panelForm);
			c.add(BorderLayout.SOUTH, panelButton);
			
			
			GroupLayout.SequentialGroup hGroup = gLayout.createSequentialGroup();
	        GroupLayout.SequentialGroup vGroup = gLayout.createSequentialGroup();
	        gLayout.setAutoCreateGaps(true);
	        gLayout.setAutoCreateContainerGaps(true);

			JLabel labelFrom = new JLabel(Messages.getString("ResultTable.11")); //$NON-NLS-1$
			JLabel labelTo = new JLabel(Messages.getString("ResultTable.12")); //$NON-NLS-1$
			
			btnOk.addActionListener(this);
			btnCancel.addActionListener(this);
			
	        hGroup.addGroup(gLayout.createParallelGroup()
                    .addComponent(labelFrom)
                    .addComponent(labelTo));
	        hGroup.addGroup(gLayout.createParallelGroup()
                    .addComponent(jtfFrom)
                    .addComponent(jtfTo));
	        gLayout.setHorizontalGroup(hGroup);

	        vGroup.addGroup(gLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(labelFrom)
                    .addComponent(jtfFrom));
	        vGroup.addGroup(gLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(labelTo)
                    .addComponent(jtfTo));
	        gLayout.setVerticalGroup(vGroup);
	        
	        
	        panelButton.add(btnOk);
	        panelButton.add(btnCancel);
	        
			setTitle(Messages.getString("ResultTable.13")); //$NON-NLS-1$
		}

		public MyDialog(Frame owner, String input1, String input2){
			this(owner);

			jtfFrom.setText(input1);
			jtfTo.setText(input2);
		}
		
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == btnOk){
				strFrom = jtfFrom.getText();
				strTo = jtfTo.getText();
				returnValue = JOptionPane.OK_OPTION;
			} else if(e.getSource() == btnCancel){
				strFrom = null;
				strTo = null;
				returnValue = JOptionPane.CANCEL_OPTION;
			}

			setVisible(false);
		}
		
		public String getResultFrom(){
			return strFrom;
		}

		public String getResultTo(){
			return strTo;
		}
		
		public int getValue(){
			return returnValue;
		}
	}
	
	

	// 選択範囲の先頭の値をすべてのセルにコピー
	public void copyHeadValueAndPaste() {
		int selectedColumns[];
		int selectedRows[];
		int firstFieldNo;
		String headValue = ""; //$NON-NLS-1$


		selectedColumns = getSelectedColumns();
		selectedRows = getSelectedRows();
		firstFieldNo = convertColumnIndexToModel(selectedColumns[0]);
		
		FieldInfo fieldInfo = tableModel.getFieldInfo();
		if(fieldInfo == null || !fieldInfo.isEditable(firstFieldNo)){
			System.err.println("no effect"); //$NON-NLS-1$
			
			return;
		}

		boolean isSelectiveEdit;
		if(fieldInfo.getEditType(firstFieldNo) == null){
			isSelectiveEdit = false;
		} else if(fieldInfo.getEditType(firstFieldNo).equals(FieldInfo.EDIT_TYPE_SELECT)){
			isSelectiveEdit = true;
		} else {
			isSelectiveEdit = false;
		}
	
		
		if(selectedColumns.length != 0 && selectedRows.length != 0){
			headValue = (String)getValueAt(selectedRows[0], selectedColumns[0]);
		}

		if(selectedColumns.length == 1 && selectedRows.length == 1){
			for (int i = 0; i < getRowCount(); i++) {
				if(i == selectedRows[0]){
					continue;
				} else {
					Object currentValue = getValueAt(i, selectedColumns[0]);
					if (currentValue == null || ((String)currentValue).isEmpty()||
							headValue == null || ((String)headValue).isEmpty() || isSelectiveEdit ) {
						setValueAt(headValue, i, selectedColumns[0]);
					} else {
						setValueAt((String) headValue, i, selectedColumns[0]);
					}
				}
			}
		} else {
			for (int i = 1; i < selectedRows.length; i++) {
				Object currentValue = getValueAt(selectedRows[i], selectedColumns[0]);
				if(currentValue == null || isSelectiveEdit || ((String)currentValue).isEmpty()){
					setValueAt(headValue, selectedRows[i], selectedColumns[0]);
				} else {
					setValueAt(headValue, selectedRows[i], selectedColumns[0]);
				}
			}
		}
        updateUI();

	}

	
	
	// 選択範囲をクリップボードへ（タブ区切り形式）
	public void copySelectedDataToClipboard(boolean withHeader) {
		int selectedColumns[];
		int selectedRows[];
		StringBuilder transferedData = new StringBuilder();
		StringBuilder temp = new StringBuilder();
		StringSelection stringSelection;

		selectedColumns = getSelectedColumns();
		selectedRows = getSelectedRows();
		
		if(withHeader){
			for(int i: selectedColumns){
				temp.append(getColumnName(i) + "\t"); //$NON-NLS-1$
			}
			transferedData.append(temp.toString().replaceFirst("\t$",  "\n")); //$NON-NLS-1$ //$NON-NLS-2$
		}

		FieldInfo fieldInfo = ((ResultTableModel)getModel()).getFieldInfo();
		for(int i: selectedRows){
			temp.setLength(0);
			for(int j: selectedColumns){
				Object v;
				if(fieldInfo.getType(fieldInfo.get(getColumnName(j))).equals(FieldInfo.TYPE_INDEX)){
					v = i + 1;
				} else {
					v = getValueAt(i,  j);
				}
				
				if(v == null) {
					temp.append("\t"); //$NON-NLS-1$
				} else {
					temp.append(v.toString()
							.replace('\n', REPLACED_CONTROL_CODE)
							.replace('\t', REPLACED_CONTROL_CODE) + "\t"); //$NON-NLS-1$
				}
			}
			transferedData.append(temp.toString().replaceFirst("\t$",  "\n")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		// delete '\n' if the number of selectedRows is 1
		if(!withHeader && selectedRows.length == 1 && transferedData.charAt(transferedData.length()-1) == '\n'){
			transferedData.deleteCharAt(transferedData.length()-1);
		}
		
		stringSelection = new StringSelection(transferedData.toString());
		this.getToolkit().getSystemClipboard().setContents(stringSelection,
				stringSelection);
	}

	public void setFont(Font font) {
		super.setFont(font);

		// render を独自に設定しているものについては，個別にフォントを設定
		for (int i = 0; renderers != null && i < renderers.size(); i++) {
			((DefaultTableCellRenderer) renderers.get(i)).setFont(font);
		}
	}

	public void setAlignment(int column, int alignment) {
		String renderClassName;

		if (getColumnModel().getColumn(column).getCellRenderer() != null) {
			renderClassName = getColumnModel().getColumn(column)
					.getCellRenderer().getClass().getName();
		} else {
			renderClassName = ""; //$NON-NLS-1$
		}

		if (renderClassName.endsWith("IndexNoRenderer")) { //$NON-NLS-1$
			((DefaultTableCellRenderer) (getColumnModel().getColumn(column)
					.getCellRenderer())).setHorizontalAlignment(alignment);
		} else {
			HorizontalAlignmentRenderer renderer = new HorizontalAlignmentRenderer(
					alignment);
			renderer.setFont(getFont());
			getColumnModel().getColumn(column).setCellRenderer(renderer);
			renderers.add(renderer);
		}
	}

	public void setWidth(int column, int width) {
		setAutoResizeMode(AUTO_RESIZE_OFF);
		getColumnModel().getColumn(column).setMinWidth(0);
		getColumnModel().getColumn(column).setWidth(width);
		sizeColumnsToFit(column);
	}

	public int getWidth(int column) {
		return getColumnModel().getColumn(column).getWidth();
	}
	
	public void setIndexField(int column) {
		IndexNoRenderer renderer = new IndexNoRenderer();
		renderer.setFont(getFont());
		getColumnModel().getColumn(column).setCellRenderer(renderer);
		renderers.add(renderer);
	}

	public void setColumnProperty(FieldInfo fieldInfo) {
		FontMetrics defaultFm = getFontMetrics(new Font(getFont().getName(), getFont().getStyle(), BASE_FONT_SIZE)); //$NON-NLS-1$
		FontMetrics fm = getFontMetrics(getFont());
		double widthScaleFactor = (double)fm.charWidth('あ') / (double)defaultFm.charWidth('あ');
		renderers.clear();
				
		for (int i = 0; i < fieldInfo.size(); i++) {
			// 行番号フィールドの設定
			if (fieldInfo.getType(i).equals(FieldInfo.TYPE_INDEX)) { //$NON-NLS-1$
				setIndexField(i);
			}

			// セル中の文字の align を設定
			if (fieldInfo.getAlign(i) == DefaultTableCellRenderer.CENTER) {
				setAlignment(i, DefaultTableCellRenderer.CENTER);
			} else if (fieldInfo.getAlign(i) == DefaultTableCellRenderer.RIGHT) {
				setAlignment(i, DefaultTableCellRenderer.RIGHT);
			} else {
				// default
				setAlignment(i, DefaultTableCellRenderer.LEFT);
			}
			// 列幅の設定
			setWidth(i, (int)(fieldInfo.getWidth(i) * widthScaleFactor));
		}
	}


	public void setEnabledTable(boolean flag){
		setEnabled(flag);
		if(flag){
			setBackground(Color.WHITE);
			setForeground(Color.BLACK);


		}else{
			setBackground(Color.LIGHT_GRAY);
			setForeground(Color.GRAY);

		}
	}


	public void joinData(ResultTable srcTable, ResultTable toTable, int[] iSrcColumns) throws IllegalArgumentException {
		FieldInfo thisFieldInfo = tableModel.getFieldInfo();
		FieldInfo srcFieldInfo = ((ResultTableModel)srcTable.getModel()).getFieldInfo();

		HashMap<String, Integer> srcMap = new HashMap<String, Integer>();
		int[] iToSelectedColumns = toTable.getSelectedColumns();
		
		if(iSrcColumns.length < 2){
			throw new IllegalArgumentException(Messages.getString("ResultTable.39")); //$NON-NLS-1$
		}
		if(iToSelectedColumns.length == 0){
			throw new IllegalArgumentException(Messages.getString("ResultTable.40")); //$NON-NLS-1$
		}
		
		// find iKeyColumns
		HashMap<Integer, Integer> iKeyFields = new HashMap<Integer, Integer>(); // iThisColumn->iSrcColumn
		for(int iSelectedColumn : iToSelectedColumns){
			int iSelectedField = toTable.convertColumnIndexToModel(iSelectedColumn);
			String thisElement = thisFieldInfo.getElementName(iSelectedField);
			String thisAttribute = thisFieldInfo.getAttributeName(iSelectedField);
			String thisLabelName = thisFieldInfo.getName(iSelectedField);
			
			int iTarget1 = srcFieldInfo.get(thisLabelName);
			int iTarget2 = srcFieldInfo.get(thisElement, thisAttribute);
			
			if(iTarget1 != -1){ // prefer labelName
				iKeyFields.put(iSelectedField, iTarget1);
			} else if(iTarget2 != -1){
				iKeyFields.put(iSelectedField, iTarget2);
			}
		}
		if(iKeyFields.isEmpty()){
			throw new IllegalArgumentException(Messages.getString("ResultTable.41")); //$NON-NLS-1$
		}
		
		// find iAddedColumns
		ArrayList<Integer> iSrcAddedFields = new ArrayList<Integer>();
		Collection<Integer> iSrcKeyFields = iKeyFields.values();
		int iSrcFreq = srcFieldInfo.getIndexFreq();
		String newSrcFreqName = ""; //$NON-NLS-1$
		for(int iSrcColumn : iSrcColumns){
			int iSrc = srcTable.convertColumnIndexToModel(iSrcColumn);
			if(iSrcKeyFields.contains(iSrc)){
				continue;
			}
			
			String srcLabelName = srcFieldInfo.getName(iSrc);
			
			if(thisFieldInfo.get(srcLabelName) == -1
					&& iSrcFreq != iSrc){ // always generate newSrcFreqName for srcTable's freq column 
				iSrcAddedFields.add(iSrc);
			} else if(iSrcFreq == iSrc){
				// src freq name will be replaced with newSrcFreqName later
				newSrcFreqName = srcFieldInfo.getName(iSrc);

				ArrayList<String> srcNames = new ArrayList<String>();
				for(int iSrcColumn2 : iSrcColumns){
					int iSrc2 = srcTable.convertColumnIndexToModel(iSrcColumn2);
					if(iSrc2 == iSrcFreq){
						continue;
					}
					srcNames.add(srcFieldInfo.getName(iSrc2));
				}
				newSrcFreqName += ":" + String.join(",", srcNames); //$NON-NLS-1$ //$NON-NLS-2$

				if(thisFieldInfo.get(newSrcFreqName) == -1){
					iSrcAddedFields.add(iSrc);
				}
			}
		}
		if(iSrcAddedFields.isEmpty()){
			throw new IllegalArgumentException(Messages.getString("ResultTable.42")); //$NON-NLS-1$
		}

		// make srcMap
		int iResultRecord = 0;
		char keyDelimiter = '\t';
		ArrayList<ResultRecord> srcData = ((ResultTableModel)srcTable.getModel()).getFilteredData();
		for(ResultRecord r : srcData){
			
			StringBuilder key = new StringBuilder();
			for(int iKeySrc : iKeyFields.keySet()){
				key.append(r.get(iKeyFields.get(iKeySrc)));
				key.append(keyDelimiter);
			}
			
			String keyValue = key.toString();
			if(srcMap.containsKey(keyValue)){
				throw new IllegalArgumentException(Messages.getString("ResultTable.45") + "\n" + keyValue); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				srcMap.put(keyValue, iResultRecord);
			}
			iResultRecord++;
		}

		// make a new fieldInfo
		FieldInfo newFieldInfo = new FieldInfo(thisFieldInfo.size() + iSrcAddedFields.size());
		int c = 0;
		int iStartAdd = 0;
		int iFreq = thisFieldInfo.getIndexFreq();
		
		//// copy thisFieldInfo to newFieldInfo
		for(int i = 0; i < thisFieldInfo.size(); i++){
			if(i == iFreq){
				continue; // will be added to the end of newFieldInfo
			}
			newFieldInfo.set(thisFieldInfo.getName(i),
					c,
					thisFieldInfo.getType(i),
					thisFieldInfo.getElementName(i),
					thisFieldInfo.getAttributeName(i),
					thisFieldInfo.getWidth(i),
					thisFieldInfo.getAlign(i),
					thisFieldInfo.getSortDirection(i),
					Integer.MAX_VALUE,
					thisFieldInfo.getSortType(i),
					false);
			c++;
		}

		//// copy copiedFieldInfo to newFieldInfo
		iStartAdd = c;
		for(int iSrcAddedField : iSrcAddedFields){
			String newName = srcFieldInfo.getName(iSrcAddedField);
			if(iSrcAddedField == iSrcFreq){
				newName = newSrcFreqName;
			}
			
			newFieldInfo.set(newName,
					c,
					srcFieldInfo.getType(iSrcAddedField),
					srcFieldInfo.getElementName(iSrcAddedField),
					newName,
					srcFieldInfo.getWidth(iSrcAddedField),
					srcFieldInfo.getAlign(iSrcAddedField),
					srcFieldInfo.getSortDirection(iSrcAddedField),
					Integer.MAX_VALUE,
					srcFieldInfo.getSortType(iSrcAddedField),
					false);
			c++;
		}
		
		if(iFreq != -1){
			newFieldInfo.set(FieldInfo.FIELDNAME_FREQ,
					c,
					"", //$NON-NLS-1$
					FieldInfo.ELEMENT_SYSTEM,
					FieldInfo.ATTRIBUTE_FREQ,
					60,
					DefaultTableCellRenderer.RIGHT,
					FieldInfo.SORT_DIRECTION_L_R,
					1, 
					FieldInfo.SORT_TYPE_NUMERIC,
					false);
		}
		
		ArrayList<ResultRecord> currentResultRecords = tableModel.getData();
		tableModel.setData(new ArrayList<ResultRecord>());
		tableModel.setFieldInfo(newFieldInfo);
		setColumnProperty(newFieldInfo);
		
		
		ArrayList<ResultRecord> newResultRecords = new ArrayList<ResultRecord>();
		String headerNames[] = thisFieldInfo.getNames();
		for(ResultRecord resultRecord : currentResultRecords){
			ResultRecord newResultRecord = new ResultRecord(newFieldInfo);
			for(String headerName: headerNames){
				newResultRecord.set(headerName, resultRecord.get(headerName));
			}
			
			StringBuilder key = new StringBuilder();
			for(int iKeyThisField : iKeyFields.keySet()){
				key.append(resultRecord.get(iKeyThisField));
				key.append(keyDelimiter);
			}
			String keyValue = key.toString();

			if(srcMap.containsKey(keyValue)){
				int j = 0;
				ResultRecord srcRecord = srcData.get(srcMap.get(keyValue));
				for(int iSrcAddedField : iSrcAddedFields){
					newResultRecord.set(iStartAdd+j, srcRecord.get(iSrcAddedField));
					j++;
				}
			}
			newResultRecord.setPosition(resultRecord.getPosition());
			newResultRecord.setResourceID(resultRecord.getResourceID());
			newResultRecord.setResourceName(resultRecord.getResourceName());
			newResultRecords.add(newResultRecord);
		}
		tableModel.setData(newResultRecords);
	}
	
	
	// alignment を考慮した renderer
	class HorizontalAlignmentRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		private int alignment;

		public HorizontalAlignmentRenderer(int alignment) {
			super();
			this.alignment = alignment;
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			FontMetrics fm = getFontMetrics(getFont());
			int width = -getX() -1;

			String str;
			if(value == null){
				str = ""; //$NON-NLS-1$
			} else if(value instanceof String){
				str = ((String)value)
						.replace('\n', REPLACED_CONTROL_CODE)
						.replace('\t', REPLACED_CONTROL_CODE);
			} else {
				str = value.toString();
			}

			
			if (alignment == SwingConstants.RIGHT) {
				int len = str.length();
				for(int i = 0; i < len; i++){
					String newStr = str.substring(i);
					if(fm.stringWidth(newStr) < width){
						str = newStr;
						break;
					}
				}
			}
			setText(str);
			setHorizontalAlignment(alignment);
			return this;
		}
	}

	class IndexNoRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			setBackground(Color.lightGray);
			setText(Integer.toString(row + 1));
			return this;
		}
	}

	void setFontSize(int fontsize) {
		setFont(new Font("Dialog", 0, fontsize)); //$NON-NLS-1$
		getTableHeader().setFont(new Font("Dialog", 0, fontsize)); //$NON-NLS-1$
		// render を独自に設定しているものについては，個別にフォントを設定
		for(DefaultTableCellRenderer renderer : renderers){
			renderer.setFont(new Font("Dialog", 0, fontsize)); //$NON-NLS-1$
		}
	}

	void invokeDialogForFiltering(String headerName, Component component,
			int x, int y) {
		JPopupMenu popupMenu = new JPopupMenu();


		// itemLists に登録していないフィールドはリストを作成
		HashMap<String, ArrayList<String>> itemLists = tableModel.getItemLists();
		ArrayList<String> itemList = itemLists.get(headerName);
		ArrayList<String> specialItems = new ArrayList<String>();
		
		if (itemList == null) {
			itemList = tableModel.makeItemListFromData(headerName);
			itemLists.put(headerName, itemList);
		}

		// フィルター解除用item追加
		if (tableModel.dataSize() != tableModel.filteredDataSize()) {
			specialItems.add(LABEL_FILTER_CANCEL);
		}
		if (itemList.size() != 0) {
			// 文字列検索用item追加
			specialItems.add(LABEL_KEY_SET);
			FieldInfo currentFieldInfo = tableModel.getFieldInfo();
			if(SwingUtilities.getAncestorOfClass(Frame1.class, this) == null
					&& !headerName.equals(FieldInfo.LABEL_FREQ)
					&& currentFieldInfo.getSortType(currentFieldInfo.get(headerName)) == FieldInfo.SORT_TYPE_NUMERIC){
				specialItems.add(LABEL_KEY_ACCUMULATE);
			}
		}
		boolean hasEmpty = false;
		Iterator<String> it = itemList.iterator();
		while(it.hasNext()){
			String item = it.next();
			if(item == null || item.isEmpty()){
				hasEmpty = true;
				it.remove();
			}
		}
		if(hasEmpty){
			specialItems.add(LABEL_KEY_EMPTY);
		}

		// item のソート
		Collections.sort(itemList);
		specialItems.addAll(itemList);
		String[] allItems = specialItems.toArray(new String[0]);

		String selectedValue = null;
		if (allItems.length <= MAX_POPUP_ITEMS) {
			// ポップアップメニューの場合
			for (String item : allItems) {
				JMenuItem menuItem = new JMenuItem(item);
				menuItem.addMouseListener(new ResultTable_popup_mouseAdapter(
						this, headerName));
				popupMenu.add(menuItem);
			}
			
			popupMenu.setPreferredSize(new Dimension(Math.min(400, popupMenu.getPreferredSize().width), popupMenu.getPreferredSize().height));
			popupMenu.show(component, x, y);
		} else {
			JOptionPane op = new JOptionPane();
			op.setMessageType(JOptionPane.INFORMATION_MESSAGE);
			op.setOptionType(JOptionPane.OK_CANCEL_OPTION);
			op.setSelectionValues(allItems);
			op.setInitialSelectionValue(allItems[0]);
			op.setWantsInput(true);
			op.setIcon(null);
			JDialog jd = op.createDialog(component, Messages.getString("ResultTable.17")); //$NON-NLS-1$
			jd.setSize(new Dimension(Math.min(400, jd.getPreferredSize().width), jd.getPreferredSize().height));
			jd.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			jd.setVisible(true);
			selectedValue = (String) op.getInputValue();
			if(selectedValue == null || selectedValue.equals(JOptionPane.UNINITIALIZED_VALUE)){
				return;
			} else {
				callFilter(headerName, selectedValue);
			}
		}
	}
	

	void invokeDialogForJumping() {
		if (tableModel.getData().size() == 0) {
			JOptionPane.showConfirmDialog(null, Messages
					.getString("ResultTable.18"), //$NON-NLS-1$
					Messages.getString("ResultTable.19"), //$NON-NLS-1$
					JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
			return;
		}

		String inputValue = (String) JOptionPane
				.showInputDialog(
						null,
						Messages.getString("ResultTable.20"), Messages.getString("ResultTable.21"), //$NON-NLS-1$ //$NON-NLS-2$
						JOptionPane.INFORMATION_MESSAGE, null, null, ""); //$NON-NLS-1$
		if (inputValue != null) {
			try {
				int inputNumber = Integer.parseInt(inputValue) - 1;
				if (inputNumber < 0
						|| inputNumber >= tableModel.getData().size()) {
					JOptionPane
							.showConfirmDialog(
									null,
									Messages.getString("ResultTable.23") + tableModel.getData().size() + //$NON-NLS-1$
											Messages
													.getString("ResultTable.24"), //$NON-NLS-1$
									Messages.getString("ResultTable.25"), //$NON-NLS-1$
									JOptionPane.DEFAULT_OPTION,
									JOptionPane.WARNING_MESSAGE);
					return;
				}
				changeSelection(inputNumber, 0, false, false);
			} catch (NumberFormatException ex) {

			}
		}
	}

	void invokeDialogForFind(int rowIndex) {
		int columnIndex = getSelectedColumn();
		if (columnIndex == -1) {
			JOptionPane.showConfirmDialog(null, Messages
					.getString("ResultTable.26"), //$NON-NLS-1$
					Messages.getString("ResultTable.27"), //$NON-NLS-1$
					JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
		} else {
			String inputValue = (String) JOptionPane
					.showInputDialog(
							null,
							Messages.getString("ResultTable.28"), Messages.getString("ResultTable.29"), //$NON-NLS-1$ //$NON-NLS-2$
							JOptionPane.INFORMATION_MESSAGE, null, null, preservedFindKey); //$NON-NLS-1$
			if (inputValue == null) {
				return; // cancel
			}
			preservedFindKey = inputValue;
			String headerName = (String) getTableHeader().getColumnModel()
					.getColumn(columnIndex).getHeaderValue();
			rowIndex = tableModel.getDataIndexWithRegex(inputValue, headerName,
					rowIndex);
			if (rowIndex == -1) {
				int inputValue2 = JOptionPane
						.showConfirmDialog(null,
								Messages.getString("ResultTable.31"), //$NON-NLS-1$
								Messages.getString("ResultTable.32"), //$NON-NLS-1$
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE);
				if (inputValue2 == JOptionPane.YES_OPTION) {
					rowIndex = tableModel.getDataIndexWithRegex(inputValue,
							headerName, 0);
				} else {
					return;
				}
			}
			if (rowIndex == -1) {
				JOptionPane
						.showConfirmDialog(null,
								Messages.getString("ResultTable.33"), //$NON-NLS-1$
								Messages.getString("ResultTable.34"), //$NON-NLS-1$
								JOptionPane.DEFAULT_OPTION,
								JOptionPane.WARNING_MESSAGE);
				return;
			}
			changeSelection(rowIndex, columnIndex, false, false);
		}
	}

	
	public void invokeAccumulate(){
		String headerName = (String) getTableHeader().getColumnModel()
				.getColumn(getSelectedColumn()).getHeaderValue();
		callFilter(headerName, LABEL_KEY_ACCUMULATE);
	}
	

	public void invokeReplace(){
		int selectedColumn = getSelectedColumn();
		
		if(selectedColumn == -1){
			return;
		}
		
		MyDialog dialog = new MyDialog(parentFrame, preservedFindKey, preservedReplaceKey);
		dialog.setModal(true);
		dialog.setLocationRelativeTo(null);
		dialog.setSize(300,140);
		dialog.setVisible(true);
		
		if(dialog.getValue() == JOptionPane.CANCEL_OPTION){
			return;
		}
		
		preservedFindKey = dialog.getResultFrom();
		preservedReplaceKey = dialog.getResultTo();

		ResultTable newTable = new ResultTable();
		ResultTableModel newTableModel = new ResultTableModel();
		FieldInfo newFieldInfo = tableModel.getFieldInfo();
		ArrayList<ResultRecord> newData = new ArrayList<ResultRecord>();
		for(ResultRecord r : tableModel.getData()){
			newData.add(r.copyTo(new ResultRecord(newFieldInfo)));
		}
		newTableModel.setFieldInfo(tableModel.getFieldInfo());
		newTableModel.setData(newData);
		newTableModel.replaceData(
				(String)getTableHeader().getColumnModel().getColumn(getSelectedColumn()).getHeaderValue(),
				dialog.getResultFrom(), dialog.getResultTo());

		newTable.setModel(newTableModel);
		parentFrame.invokeSelectedStatFrame(newTable, newFieldInfo, true);
	}


	public void invokeJoin(ResultTable srcTable, int[] iSrcColumns){
		ResultTable newTable = new ResultTable();
		ResultTableModel newTableModel = new ResultTableModel();
		FieldInfo newFieldInfo = tableModel.getFieldInfo();
		ArrayList<ResultRecord> newData = new ArrayList<ResultRecord>();
		for(ResultRecord r : tableModel.getData()){
			newData.add(r.copyTo(new ResultRecord(newFieldInfo)));
		}
		newTableModel.setFieldInfo(tableModel.getFieldInfo());
		newTableModel.setData(newData);
		newTable.setModel(newTableModel);
		newTable.joinData(srcTable, this, iSrcColumns);

		parentFrame.invokeSelectedStatFrame(newTable, ((ResultTableModel)newTable.getModel()).getFieldInfo(), true);
	}

	
	/**
	 * Sort the selected record keeping the view in the window.
	 *
	 * @param columnIndex
	 *            int index of the target column
	 * @param isAscending
	 *            boolean flag of sort order (true: ascending, false:
	 *            descending)
	 */
	void sortDataKeepingSelectedPosition(int columnIndex, boolean isAscending) {
		int rowIndex = getSelectedRow();
		int selectedColumn = getSelectedColumn();

		// move the view to the top of the table if there is no selected cell.
		if (selectedColumn == -1) {
			if (((ResultTableModel) this.getModel()).dataSize() > 0) {
				selectedColumn = 0;
			} else { // if no data
				return;
			}
		}

		// get the name of the target row
		String headerName = (String) getTableHeader().getColumnModel()
				.getColumn(columnIndex).getHeaderValue();

		int corpusID = -1;
		int position = -1;

		ResultRecord record;
		if (rowIndex != -1) {
			record = (ResultRecord) tableModel.getRecordAt(rowIndex);
		} else {
			record = (ResultRecord) tableModel.getRecordAt(0);
		}
		corpusID = record.getResourceID();
		position = record.getPosition();

		tableModel.sortData(headerName, isAscending);
		
		// move the view to the record selected before sorting
		int targetRecordNumber = tableModel.getDataIndex(corpusID, position);
		if (targetRecordNumber == -1) {
			return;
		} else {
			changeSelection(targetRecordNumber, columnIndex, false, false);
			requestFocusInWindow();

		}
	}

	
	void sortDataKeepingSelectedPosition(int columnIndex) {
		FieldInfo fieldInfo = tableModel.getFieldInfo();
		int fieldStatus = fieldInfo.getFieldStatus(fieldInfo.get(getColumnName(columnIndex)));

		if(fieldStatus == FieldInfo.STATUS_INIT || fieldStatus == FieldInfo.STATUS_SORT_DESCENDING){
			sortDataKeepingSelectedPosition(columnIndex, true);
		} else {
			sortDataKeepingSelectedPosition(columnIndex, false);
		}
	}

	
	
	public void setViewCenterByIndex(int row){		
		Rectangle targetRect = getCellRect(row, 0, true);
		Rectangle viewRect = getVisibleRect();
		int dy = viewRect.height / 2;
		viewRect.setLocation(targetRect.x, targetRect.y	- dy);
		scrollRectToVisible(viewRect);
	}

	
	
	
	/**
	 * (左クリック) ソート (右クリック) フィルタの設定
	 *
	 * @param e
	 *            MouseEvent
	 */
	void header_mouseClicked(MouseEvent e) {
		// 左ボタンクリック(ソート)
		int columnIndex = getTableHeader().columnAtPoint(e.getPoint());
		// int rowIndex = getSelectedRow();

		// ポイントしているヘッダの名前を取得
		String headerName = (String) getTableHeader().getColumnModel()
				.getColumn(columnIndex).getHeaderValue();

		if (e.isMetaDown()) {
			invokeDialogForFiltering(headerName, e.getComponent(), e.getX(), e
					.getY());
		} else if (e.getButton() == MouseEvent.BUTTON1) {
			if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0) { // 左ボタンクリック
				sortDataKeepingSelectedPosition(columnIndex, false); // ソート(逆順)
			} else {
				sortDataKeepingSelectedPosition(columnIndex); // ソート(正順)
			}
		}
	}

	void cell_mouseClicked(MouseEvent e) {
		if (e.isMetaDown()) {
			JPopupMenu popupMenu = new JPopupMenu();

			int selectedColumn = getSelectedColumn();
			int selectedRow = getSelectedRow();
			
			
			// copy
			JMenuItem menuItem = new JMenuItem(Messages
					.getString("ResultTable.35")); //$NON-NLS-1$
			menuItem.addMouseListener(new MouseAdapter(){
				public void mouseReleased(MouseEvent e) {
					SearchResultFrame parentSearchResultFrame = (SearchResultFrame)SwingUtilities.getAncestorOfClass(SearchResultFrame.class, ResultTable.this);
					if(parentSearchResultFrame != null){
						// update information for join
						parentSearchResultFrame.copyDataToClipboard();
					} else {
						copySelectedDataToClipboard(false);
					}
				}
			});
			popupMenu.add(menuItem);

			// copy_with_header
			menuItem = new JMenuItem(Messages.getString("ResultTable.10")); //$NON-NLS-1$
			menuItem.addMouseListener(new MouseAdapter(){
				public void mouseReleased(MouseEvent e) {
					SearchResultFrame parentSearchResultFrame = (SearchResultFrame)SwingUtilities.getAncestorOfClass(SearchResultFrame.class, ResultTable.this);
					if(parentSearchResultFrame != null){
						// update information for join
						parentSearchResultFrame.copyDataWithHeaderToClipboard();
					} else {
						copySelectedDataToClipboard(true);
					}
				}
			});
			popupMenu.add(menuItem);

			
			
			// select_all
			menuItem = new JMenuItem(Messages.getString("ResultTable.36")); //$NON-NLS-1$
			menuItem.addMouseListener(new this_selectAll_mouseAdapter(this));
			popupMenu.add(menuItem);

			// clear filter
			if(tableModel.dataSize() != tableModel.filteredDataSize()){
				menuItem = new JMenuItem(Messages.getString("ResultTable.43")); //$NON-NLS-1$
				menuItem.addMouseListener(new MouseAdapter(){
					public void mouseReleased(MouseEvent e) {
						tableModel.initFilter();
					}
				});
				popupMenu.add(menuItem);
			}

			
			if(selectedColumn != -1 && selectedRow != -1){
				menuItem = new JMenuItem(Messages.getString("ResultTable.46")); //$NON-NLS-1$
				menuItem.addMouseListener(new java.awt.event.MouseAdapter() {
					public void mouseReleased(MouseEvent e) {
						invokeReplace();
					}
				});
				popupMenu.add(menuItem);
				
				// filter
				menuItem = new JMenuItem(Messages.getString("ResultTable.38")); //$NON-NLS-1$
				menuItem.addMouseListener(new java.awt.event.MouseAdapter() {
					public void mouseReleased(MouseEvent e) {
						int selectedColumn = -1;
						int selectedRow = -1;
						selectedColumn = adaptee.getSelectedColumn();
						selectedRow = adaptee.getSelectedRow();

						String headerName = (String) adaptee.getTableHeader()
								.getColumnModel().getColumn(selectedColumn)
								.getHeaderValue();
						callFilter(headerName, adaptee.getValueAt(selectedRow, selectedColumn) == null ? "" //$NON-NLS-1$
								: adaptee.getValueAt(selectedRow, selectedColumn).toString());
					}
				});
				popupMenu.add(menuItem);

				
				// accumulate
//				int iFreq = tableModel.getFieldInfo().getIndexFreq();
//				if(iFreq != -1 	&& iFreq != getSelectedColumn()){
//					// has freq field
//					menuItem = new JMenuItem(Messages.getString("ResultTable.6")); //$NON-NLS-1$
//					menuItem.addMouseListener(new java.awt.event.MouseAdapter() {
//						public void mouseReleased(MouseEvent e) {
//							RecordStatistics rs = new RecordStatistics(tableModel.getFieldInfo(), true);
//							int nErrors = rs.accumulate(tableModel.getFilteredData(), adaptee.getSelectedColumn());
//							if(nErrors > 0){
//								JOptionPane.showMessageDialog(ResultTable.this, nErrors + Messages.getString("ResultTable.7"), Messages.getString("ResultTable.14"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
//							} else {
//								SearchResultFrame sf = new SearchResultFrame();
//								sf.setFieldInfo(tableModel.getFieldInfo());
//								sf.invokeStatisticsFrame(rs.getResults(adaptee.getSelectedColumn()), fontsize);
//							}
//						}
//					});
//					popupMenu.add(menuItem);
//				}

				
				// statistics
				menuItem = new JMenuItem(Messages.getString("ResultTable.4")); //$NON-NLS-1$
				menuItem.addMouseListener(new java.awt.event.MouseAdapter() {
					public void mouseReleased(MouseEvent e) {
						parentFrame.invokeSelectedStatFrame(ResultTable.this, ResultTable.this.getSelectedFieldInfo());
					}
				});
				popupMenu.add(menuItem);

				
				// mark
				if(tableModel.getFieldInfo().isDbEditable()){
					menuItem = new JMenuItem(Messages.getString("ResultTable.37")); //$NON-NLS-1$
					menuItem.addMouseListener(new this_copyHeadValueAndPaste_mouseAdapter(
							this));
					popupMenu.add(menuItem);
				}
			}
			popupMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}
	
	
	FieldInfo getSelectedFieldInfo(){
		int selectedColumns[] = getSelectedColumns();
		FieldInfo selectedTableInfo = ((ResultTableModel)getModel()).getFieldInfo();
		int iFreqSelectedTable = selectedTableInfo.getIndexFreq();
		int nf = 0;

		for(int selectedColumn : selectedColumns){
			int selectedFieldNo = convertColumnIndexToModel(selectedColumn);
			if(selectedFieldNo == iFreqSelectedTable){
				continue;
			}
			nf++;
		}

		FieldInfo newTableInfo = new FieldInfo(nf+1);
		int c = 0;
		for(int selectedColumn : selectedColumns){
			int selectedFieldNo = convertColumnIndexToModel(selectedColumn);
			if(selectedFieldNo == iFreqSelectedTable){
				continue;
			}
			newTableInfo.set(selectedTableInfo.getName(selectedFieldNo),
					c++,
					selectedTableInfo.getType(selectedFieldNo),
					selectedTableInfo.getElementName(selectedFieldNo),
					selectedTableInfo.getAttributeName(selectedFieldNo),
					selectedTableInfo.getWidth(selectedFieldNo),
					selectedTableInfo.getAlign(selectedFieldNo),
					selectedTableInfo.getSortDirection(selectedFieldNo),
					Integer.MAX_VALUE,
					selectedTableInfo.getSortType(selectedFieldNo),
					false);
		}
		newTableInfo.set(FieldInfo.FIELDNAME_FREQ,
				nf,
				"", //$NON-NLS-1$
				FieldInfo.ELEMENT_SYSTEM,
				FieldInfo.ATTRIBUTE_FREQ,
				DEFAULT_FREQ_COLUMN_WIDTH,
				DefaultTableCellRenderer.RIGHT,
				FieldInfo.SORT_DIRECTION_L_R,
				1, 
				FieldInfo.SORT_TYPE_NUMERIC,
				false);
		return newTableInfo;
	}


	class this_selectAll_mouseAdapter extends java.awt.event.MouseAdapter {
		ResultTable adaptee;

		this_selectAll_mouseAdapter(ResultTable adaptee) {
			this.adaptee = adaptee;
		}

		public void mouseReleased(MouseEvent e) {
			adaptee.selectAll();
		}
	}

	class this_copyHeadValueAndPaste_mouseAdapter extends java.awt.event.MouseAdapter {
		ResultTable adaptee;

		this_copyHeadValueAndPaste_mouseAdapter(ResultTable adaptee) {
			this.adaptee = adaptee;
		}

		public void mouseReleased(MouseEvent e) {
			adaptee.copyHeadValueAndPaste();
		}
	}

	
	/**
	 * popup 動作時の<code>MouseAdapter</code>
	 */
	class ResultTable_popup_mouseAdapter extends java.awt.event.MouseAdapter {
		ResultTable adaptee;

		String headerName;

		ResultTable_popup_mouseAdapter(ResultTable adaptee, String headerName) {
			this.adaptee = adaptee;
//			this.data = data;
			this.headerName = headerName;
		}

		public void mouseReleased(MouseEvent e) {
			String selectedValue = ((JMenuItem) e.getSource()).getText();
			callFilter(headerName, selectedValue);
		}
	}

	/**
	 * ヘッダクリック時の<code>MouseAdapter</code>
	 */
	class ResultTable_header_mouseAdapter extends java.awt.event.MouseAdapter {
		ResultTable adaptee;

		ResultTable_header_mouseAdapter(ResultTable adaptee) {
			this.adaptee = adaptee;
		}

		public void mouseClicked(MouseEvent e) {
			adaptee.header_mouseClicked(e);
		}
	}

	/**
	 * セルクリック時の<code>MouseAdapter</code>
	 */
	class ResultTable_cell_mouseAdapter extends java.awt.event.MouseAdapter {
		ResultTable adaptee;

		ResultTable_cell_mouseAdapter(ResultTable adaptee) {
			this.adaptee = adaptee;
		}

		public void mouseClicked(MouseEvent e) {
			adaptee.cell_mouseClicked(e);
		}
	}


	public void setSaveEnabled(boolean saveEnabled) {
		this.saveEnabled = saveEnabled;
	}


	public boolean isSaveEnabled() {
		return saveEnabled;
	}

	public void setCellEditor(){
		FieldInfo fi = tableModel.getFieldInfo();
		if(fi == null){return;}

		for(int i=0; i<fi.size();i++){
			String editType = fi.getEditType(i);
			if (editType == null){ continue;}

			//コンボボックスの設定
			if(editType.equalsIgnoreCase("select")){ //$NON-NLS-1$
				if(fi.getEditOption(i) == null){continue;}
				String[] brankOpt = {BLANK_STR};
				String[] valueOpt = fi.getEditOption(i).split(","); //$NON-NLS-1$
				String[] options = new String[brankOpt.length + valueOpt.length];
				System.arraycopy(brankOpt, 0, options, 0, brankOpt.length);
				System.arraycopy(valueOpt, 0, options, brankOpt.length, valueOpt.length);

				setComboBox(i, options);
			}
		}
	}

	/**
	 * col行目をコンボボックスにする
	 * @param col
	 * @param items
	 */
	private void setComboBox(int col, Object[] items){
		getColumn(getColumnName(col)).setCellEditor(new ComboEditor(this, items));
	}


	public void setParentFrame(Frame1 parentFrame) {
		this.parentFrame = parentFrame;
	}


	public Frame1 getParentFrame() {
		return parentFrame;
	}

}

class ResultTable_this_keyAdapter extends java.awt.event.KeyAdapter {
	ResultTable adaptee;

	ResultTable_this_keyAdapter(ResultTable adaptee) {
		this.adaptee = adaptee;
	}

	public void keyPressed(KeyEvent e) {
		if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
			adaptee.getParent().dispatchEvent(e);
		}
	}
}


/**
 * 結果テーブル用フォーカスアダプタ
 * @author osada
 *
 */
class ResultTable_focusAdapter extends FocusAdapter{
	ResultTable adaptee;
	String before = ""; //$NON-NLS-1$

	ResultTable_focusAdapter(ResultTable adaptee) {
		this.adaptee = adaptee;
	}

    @Override
    public void focusLost(FocusEvent e) {
    	JTextField tf = (JTextField)e.getSource();
    	String after = tf.getText();
    	if(!after.equals(before)){
    		Frame1 frame1 = (Frame1) adaptee.getParentFrame();
    		frame1.setEnableButtonSaveDb(true);
    		frame1.setEnableButtonReverDb(true);
    		adaptee.setSaveEnabled(true);
    	}

        if(adaptee.isEditing()) {
        	adaptee.getCellEditor().stopCellEditing();
        }
    }
    @Override
	public void focusGained(FocusEvent e) {
//	     System.out.println("focus gained");
	}
}

/*
 * コンボボックス用のエディター
 */
class ComboEditor extends AbstractCellEditor implements TableCellEditor {
	private static final long serialVersionUID = 1L;
	ResultTable adaptee;
	JComboBox<Object> comboBox = new JComboBox<Object>();
	Object[] items;

	public ComboEditor(ResultTable adaptee, Object[] items) {
		super();
		this.items = items;
		this.adaptee = adaptee;
	}

	public Component getTableCellEditorComponent(JTable table,
			Object value, boolean isSelected, final int row, final int column) {

		comboBox.removeAllItems();
		for (int i = 0; i < items.length; i++) {
			comboBox.addItem(items[i]);
		}
		comboBox.setSelectedItem(value);

		comboBox.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent arg0) {
			}
			@Override
			public void focusLost(FocusEvent arg0) {
				changeValue(comboBox.getSelectedItem(), row, column);
			}
		});

		comboBox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent actionevent) {
				changeValue(comboBox.getSelectedItem(), row, column);
			}

		});
		return comboBox;
	}

	/**
	 * セルの値を変更
	 * @param val
	 * @param row
	 * @param col
	 */
	private void changeValue(Object val, int row, int col){
		Frame1 f1 = adaptee.getParentFrame();

		f1.setValueToCombo_selectedValue(row, col, val);
		fireEditingStopped();
	}

	/*
	 * コンボボックスの選択された値を返します。
	 *
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	public Object getCellEditorValue() {
		return comboBox.getSelectedItem();
	}

}


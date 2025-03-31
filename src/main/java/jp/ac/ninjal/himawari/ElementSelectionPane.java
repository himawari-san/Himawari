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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ElementSelectionPane extends JOptionPane {
	private static final long serialVersionUID = 1L;
	public static final String LabelNotSelected = Messages.getString("ElementSelectionPane.0"); //$NON-NLS-1$
	public static final String STATUS_ALL_SELECTED = Messages.getString("ElementSelectionPane.6"); //$NON-NLS-1$
	public static final String STATUS_NOT_SELECTED = Messages.getString("ElementSelectionPane.7"); //$NON-NLS-1$
	public static final String STATUS_PARTLY_SELECTED = Messages.getString("ElementSelectionPane.8"); //$NON-NLS-1$
	
	private MyComboBox jComboBoxElementsLV1;
	private MyComboBox jComboBoxElementsLV2;
	private MyComboBox jComboBoxElementsLV3;
	
	private MyButton jButtonAttributesLV1;
	private MyButton jButtonAttributesLV2;
	private MyButton jButtonAttributesLV3;
	
	private JCheckBox jCheckBoxFreq;
	private JCheckBox jCheckBoxContents;
	private JCheckBox jCheckBoxLength;

	private JComboBox<Integer> jComboBoxContextLength;
	
	public ElementSelectionPane(HashMap<String, CorpusElementInfo> elementMap) {
		super();
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		jButtonAttributesLV1 = new MyButton();
		jButtonAttributesLV2 = new MyButton();
		jButtonAttributesLV3 = new MyButton();
		
		jComboBoxElementsLV1 = new MyComboBox(jButtonAttributesLV1, new HashMap<String, CorpusElementInfo>(elementMap));
		jComboBoxElementsLV2 = new MyComboBox(jButtonAttributesLV2, new HashMap<String, CorpusElementInfo>(elementMap)); 
		jComboBoxElementsLV3 = new MyComboBox(jButtonAttributesLV3, new HashMap<String, CorpusElementInfo>(elementMap)); 

		jCheckBoxFreq = new JCheckBox(Messages.getString("ElementSelectionPane.3")); //$NON-NLS-1$
		jCheckBoxContents = new JCheckBox(Messages.getString("ElementSelectionPane.5")); //$NON-NLS-1$
		jCheckBoxLength = new JCheckBox(Messages.getString("ElementSelectionPane.4")); //$NON-NLS-1$
		
		jComboBoxContextLength = new JComboBox<Integer>(new Integer[] {0, 1, 2, 3, 4, 5});
		jComboBoxContextLength.setPrototypeDisplayValue(10);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		JComponent[][] elementComponents = {
				{ new JLabel(Messages.getString("ElementSelectionPane.1")), jComboBoxElementsLV1, jButtonAttributesLV1}, //$NON-NLS-1$
				{ new JLabel(Messages.getString("ElementSelectionPane.2")), jComboBoxElementsLV2, jButtonAttributesLV2}, //$NON-NLS-1$
				{ new JLabel(Messages.getString("ElementSelectionPane.13")), jComboBoxElementsLV3, jButtonAttributesLV3}, //$NON-NLS-1$
		};
		JPanel elementPanel = Util.makeDialogPanel(elementComponents, GroupLayout.Alignment.CENTER);
		
		JPanel optionPanel = new JPanel();
		optionPanel.setLayout(new FlowLayout());
		optionPanel.add(jCheckBoxFreq);
		optionPanel.add(jCheckBoxLength);
		optionPanel.add(jCheckBoxContents);
		optionPanel.add(Box.createHorizontalGlue());
		optionPanel.add(new JLabel(Messages.getString("ElementSelectionPane.18"))); //$NON-NLS-1$
		optionPanel.add(jComboBoxContextLength);

		mainPanel.add(elementPanel);
		mainPanel.add(optionPanel);
		
		setMessage(mainPanel);
		setOptionType(JOptionPane.OK_CANCEL_OPTION);
		setMessageType(JOptionPane.QUESTION_MESSAGE);
	}
	

	public String getSelectedElement(int iElement){
		if(iElement == 0){
			return (String)jComboBoxElementsLV1.getSelectedItem();
		} else if(iElement == 1){
			return (String)jComboBoxElementsLV2.getSelectedItem();
		} else {
			return (String)jComboBoxElementsLV3.getSelectedItem();
		}
	}
	
	
	public int getOptionalFields(){
		int option = 0;
		
		if(jCheckBoxContents.isSelected()){
			option += FieldInfo.OPTION_CONTENTS;
		}
		if(jCheckBoxLength.isSelected()){
			option += FieldInfo.OPTION_LENGTH;
		}
		if(jCheckBoxFreq.isSelected()){
			option += FieldInfo.OPTION_FREQ;
		}
		
		return option;
	}
	
	
	public int getContextLength(){
		return (Integer)jComboBoxContextLength.getSelectedItem();
	}
	

	class MyComboBox extends JComboBox<String> {
		private static final long serialVersionUID = 1L;

		private MyButton button;
		
		public MyComboBox(MyButton button, final HashMap<String, CorpusElementInfo> elementMap) {
			super(new DefaultComboBoxModel<String>());
			this.button = button;
			
			addItem(LabelNotSelected);
			for(String item : elementMap.keySet()){
				addItem(item);
			}
			button.setMap(elementMap.get(getSelectedItem()));
			
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					MyComboBox.this.button.setMap(elementMap.get(getSelectedItem()));
				}
			});
		}
		
		public boolean isSelected(){
			if(((String)getSelectedItem()).equals(LabelNotSelected)){
				return false;
			} else {
				return true;
			}
		}
	}

	
	class MyButton extends JButton {
		private static final long serialVersionUID = 1L;
		private static final int PANEL_MAX_WIDTH = 350;
		private static final int PANEL_MAX_HEIGHT = 500;
		private static final int PANEL_MERGIN = 10;

		private CorpusElementInfo corpusElementInfo;
		
		public MyButton() {
			super();
			
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					final JComponent[][] components = new JComponent[corpusElementInfo.size()][1];
					final MyAllSelectionCheckBox allSelectionCheckBox = new MyAllSelectionCheckBox(corpusElementInfo);
					allSelectionCheckBox.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							boolean newValue = allSelectionCheckBox.isSelected();
							
							for(int i = 0; i < components.length; i++){
								((JCheckBox)components[i][0]).setSelected(newValue);
							}
							for(String attributeName : corpusElementInfo.keySet()){
								corpusElementInfo.setSelected(attributeName, newValue);
							}
						}
					});
					
					int i = 0;
					for(final String attributeName : Util.asSortedList(corpusElementInfo.keySet())){
						final JCheckBox cb = new JCheckBox(corpusElementInfo.getLabel(attributeName));
						components[i][0] = cb;
						cb.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent arg0) {
								corpusElementInfo.setSelected(attributeName, cb.isSelected());
								allSelectionCheckBox.updateStatus();
							}
						});
						if(corpusElementInfo.isSelected(attributeName)){
							cb.setSelected(true);
						}
						i++;
					}
					allSelectionCheckBox.updateStatus(); // update because of components initialization
					
					JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
					JPanel cbPanel = Util.makeDialogPanel(components, GroupLayout.Alignment.CENTER);
					JScrollPane jsp = new JScrollPane(cbPanel);
					panel.add(jsp);
					panel.add(allSelectionCheckBox);
					allSelectionCheckBox.setAlignmentX(LEFT_ALIGNMENT);
					jsp.setAlignmentX(LEFT_ALIGNMENT);
					
					int xmax = cbPanel.getPreferredSize().width < PANEL_MAX_WIDTH ? cbPanel.getPreferredSize().width + PANEL_MERGIN : PANEL_MAX_WIDTH; 
					int ymax = cbPanel.getPreferredSize().height < PANEL_MAX_HEIGHT ? cbPanel.getPreferredSize().height + PANEL_MERGIN : PANEL_MAX_HEIGHT; 
					jsp.setPreferredSize(new Dimension(xmax, ymax));
					
					int result = JOptionPane.showConfirmDialog(
						ElementSelectionPane.this,
						panel,
						Messages.getString("ElementSelectionPane.9"), //$NON-NLS-1$
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE);
					
					if(result == JOptionPane.YES_OPTION){
						int nSelected = 0;
						for(String attributeName : corpusElementInfo.keySet()){
							if(corpusElementInfo.isSelected(attributeName)){
								nSelected++;
							}
						}
						if(nSelected == 0){
							setText(Messages.getString("ElementSelectionPane.10")); //$NON-NLS-1$
						} else if(nSelected == corpusElementInfo.size()){
							setText(Messages.getString("ElementSelectionPane.11")); //$NON-NLS-1$
						} else {
							setText(Messages.getString("ElementSelectionPane.8")); //$NON-NLS-1$
						}
					}
				}
			});
		}

		
		class MyAllSelectionCheckBox extends JCheckBox {
			private static final long serialVersionUID = 1L;
			public final int PARTIALLY_SELECTED = 3;
			public final int ALL_SELECTED = 1;
			public final int ALL_NON_SELECTED = 2;
			private int status = ALL_SELECTED;
			CorpusElementInfo corpusElementInfo;
			
			public MyAllSelectionCheckBox(CorpusElementInfo corpusElementInfo) {
				super(Messages.getString("ElementSelectionPane.14")); //$NON-NLS-1$
				this.corpusElementInfo = corpusElementInfo;
			}
			
			public int updateStatus(){
				boolean isAllSelected = true;
				boolean isAllNonSelected = true;
				
				for(String attributeName : corpusElementInfo.keySet()){
					boolean isSelected = corpusElementInfo.isSelected(attributeName);
					if(isSelected){
						isAllNonSelected = false;
					} else {
						isAllSelected = false;
					}
				}
				
				if(isAllSelected){
					status = ALL_SELECTED;
					setSelected(true);
				} else if(isAllNonSelected){
					status = ALL_NON_SELECTED; 
					setSelected(false);
				} else {
					status = PARTIALLY_SELECTED;
					setSelected(false);
				}
				
				return status;
			}
		}


		
		public void setMap(CorpusElementInfo corpusElementInfo){
			this.corpusElementInfo = corpusElementInfo;
			if(corpusElementInfo == null || corpusElementInfo.size() == 0){
				MyButton.this.setEnabled(false);
				setText(STATUS_NOT_SELECTED);
				return;
			} else {
				MyButton.this.setEnabled(true);
			}
						
			int c = 0;
			for(String attributeName : corpusElementInfo.keySet()){
				if(corpusElementInfo.isSelected(attributeName)){
					c++;
				}
			}
			
			if(c == 0){
				setText(STATUS_NOT_SELECTED);
			} else if(c == corpusElementInfo.size()){
				setText(STATUS_ALL_SELECTED);
			} else {
				setText(STATUS_PARTLY_SELECTED);
			}
		}
	}
	
}

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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;


public class CorpusChooserPane extends JOptionPane {
	private static final long serialVersionUID = 1L;
	
	private static final int FN_CORPUSNAME = 0;
	private static final int FN_AUX_TYPE = 1;
	private static final int FN_CONFIG = 2;
	private static final int FN_DELETE = 3;
	private static final String AUX_TYPE_NONE = Messages.getString("CorpusChooserPane.0"); //$NON-NLS-1$
	private static final String AUX_TYPE_SD = Messages.getString("CorpusChooserPane.1"); //$NON-NLS-1$
	private static final String AUX_TYPE_DB = Messages.getString("CorpusChooserPane.2"); //$NON-NLS-1$
	

	private String[] headerNames = {Messages.getString("CorpusChooserPane.4"), Messages.getString("CorpusChooserPane.5"), Messages.getString("CorpusChooserPane.6"), "-"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private int columnWidth[] = {250, 100, 170, 60};
	private String[] auxTypes = {AUX_TYPE_NONE, AUX_TYPE_SD, AUX_TYPE_DB};
	private JTable corpusTable;

	private HashMap<String, ArrayList<String>> names = new HashMap<String, ArrayList<String>>();

	private DocumentBuilder builder;
	private XPath xpath;
	
	public CorpusChooserPane(File dir, String currentConfigFilename, Runnable defaultPackageLoader){
		super();

		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		xpath = XPathFactory.newInstance().newXPath();
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		final DefaultTableModel tableModel = new CorpusTableModel(headerNames, 0);
		corpusTable = new JTable(tableModel);
		makeForm(dir, currentConfigFilename, defaultPackageLoader);
		corpusTable.getColumnModel().getColumn(FN_AUX_TYPE).setCellRenderer(new ComboBoxRenderer());
		corpusTable.getColumnModel().getColumn(FN_AUX_TYPE).setCellEditor(new ComboBoxEditor(new JComboBox<String>()));
		corpusTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		corpusTable.getColumnModel().getColumn(FN_CORPUSNAME).setCellRenderer(new LabelRenderer());
		corpusTable.getColumnModel().getColumn(FN_CONFIG).setCellRenderer(new LabelRenderer());
		corpusTable.getColumnModel().getColumn(FN_DELETE).setCellEditor(new ButtonEditor(corpusTable));
		corpusTable.getColumnModel().getColumn(FN_DELETE).setCellRenderer(new ButtonRenderer());

		// select the corpus, if double-clicked
		corpusTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
					// set the value for getValue
					CorpusChooserPane.this.setValue(JOptionPane.OK_OPTION);
					// close this dialog
					SwingUtilities.getWindowAncestor(corpusTable).dispose();
				}
			}
		});
		// set column width
		corpusTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    DefaultTableColumnModel columnModel = (DefaultTableColumnModel)corpusTable.getColumnModel();
	    for(int i = 0; i < columnWidth.length; i++){
			columnModel.getColumn(i).setPreferredWidth(columnWidth[i]);
		}

	    // sort
	    TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableModel);
	    sorter.setSortable(FN_AUX_TYPE, false);
	    sorter.setSortable(FN_DELETE, false);
	    ArrayList<SortKey> sortKeys = new ArrayList<SortKey>();
	    sortKeys.add(new RowSorter.SortKey(FN_CORPUSNAME, SortOrder.ASCENDING));
	    sortKeys.add(new RowSorter.SortKey(FN_CONFIG, SortOrder.ASCENDING));
	    sorter.setSortKeys(sortKeys);
	    corpusTable.setRowSorter(sorter);
	    
	    JScrollPane sp = new JScrollPane(corpusTable);
		mainPanel.add(sp);

		selectCurrentConfigRow(currentConfigFilename);

		setMessage(mainPanel);
		setOptionType(JOptionPane.OK_CANCEL_OPTION);
		setMessageType(JOptionPane.QUESTION_MESSAGE);
		setIcon(new ImageIcon()); // remove the icon by adding a empty icon
		setPreferredSize(new Dimension(640, 400));
	}

	public String getSelectedConfigFilename(){
		return (String)corpusTable.getValueAt(corpusTable.getSelectedRow(), FN_CONFIG);
	}
	
	
	private void makeForm(File dir, String currentConfigFilename, Runnable defaultPackageLoader){
		DefaultTableModel tm = (DefaultTableModel) corpusTable.getModel();
		names.clear();
		final ArrayList<String> auxTypesArray = new ArrayList<String>(Arrays.asList(auxTypes));
		
		// make a list of corpora
		for(File file : dir.listFiles()){
			if(file.isDirectory()){
				continue;
			}
			
			String filename = file.getName();
			
			if(!UserSettings.isConfigurationFile(filename)){
				continue;
			}

			String corpusName = getCorpusName(file);
			String configNameBody = filename.replaceFirst("(\\.sd|\\.db)?\\.xml$", ""); //$NON-NLS-1$ //$NON-NLS-2$
			ArrayList<String> attributes = names.containsKey(configNameBody) ? names.get(configNameBody) : new ArrayList<String>();
			attributes.add(corpusName);
			
			if(filename.matches("^.*\\.sd\\.xml$")){ //$NON-NLS-1$
				attributes.add(AUX_TYPE_SD);
			} else if(filename.matches("^.*\\.db\\.xml$")){ //$NON-NLS-1$
				attributes.add(AUX_TYPE_DB);
			} else if(filename.matches("^.*\\.xml$")){ //$NON-NLS-1$
				attributes.add(AUX_TYPE_NONE);
			} else {
				attributes.add("other"); //$NON-NLS-1$
			}
			attributes.add(filename);
			names.put(configNameBody, attributes);
		}

		// map the list to tableModel
		String currentConfigFilenameBody = currentConfigFilename.replaceFirst("(\\.sd|\\.db)?\\.xml$", ""); //$NON-NLS-1$ //$NON-NLS-2$
		final int nAttr = 3;
		for(final String name : names.keySet()){
			Object data[] = new Object[nAttr+1];
			ArrayList<String> attributes = names.get(name);
			String[] auxTypes = new String[attributes.size()/nAttr];
			String[] configFilenames = new String[attributes.size()/nAttr];
			for(int j = 0; j < attributes.size(); j += nAttr){
				auxTypes[j/nAttr] = attributes.get(j + FN_AUX_TYPE); // aux type
				configFilenames[j/nAttr] = attributes.get(j + FN_CONFIG);
			}
			Arrays.sort(auxTypes, new Comparator<String>() {
				@Override
		        public int compare(String a, String b){
					return auxTypesArray.indexOf(a) - auxTypesArray.indexOf(b); 
				}
			});

			JComboBox<String> comboBox = new JComboBox<String>(auxTypes);
        	comboBox.setOpaque(true);
        	comboBox.setEditable(true);
        	comboBox.setBorder(BorderFactory.createEmptyBorder());
    		JTextField ed = (JTextField)comboBox.getEditor().getEditorComponent(); 
        	ed.setOpaque(true);
        	ed.setEditable(false);
        	ed.setBorder(BorderFactory.createEmptyBorder());
        	JButton deleteButton = new JButton(Messages.getString("CorpusChooserPane.7")); //$NON-NLS-1$
        	deleteButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					int selectedRow = corpusTable.getSelectedRow();
					CorpusManager corpusManager = new CorpusManager();
					
					int result = JOptionPane.showConfirmDialog(
							CorpusChooserPane.this, (String)corpusTable.getValueAt(selectedRow, FN_CORPUSNAME) + Messages.getString("CorpusChooserPane.8"), //$NON-NLS-1$
							Messages.getString("CorpusChooserPane.9"), //$NON-NLS-1$
							JOptionPane.YES_NO_OPTION);

					if (result != JOptionPane.YES_OPTION) {
						return;
					}


					boolean isLoaded = false;
					// Pushing a delete button leads to remove up to three corpora (AUX_TYPE_SD, AUX_TYPE_DB, AUX_TYPE_NONE).  
					for(String configFilename : configFilenames) {
						if(currentConfigFilename.equals(configFilename)) {
							isLoaded = true;
							defaultPackageLoader.run(); // Load the default package
							break;
						}
					}
					for(String configFilename : configFilenames) {
						corpusManager.delete(new File(configFilename));
					}
					tm.removeRow(corpusTable.convertRowIndexToModel(selectedRow));
					
					if(isLoaded) {
						// select the first row
						corpusTable.setRowSelectionInterval(0, 0);
					} else {
						// select the row of currentConfigFilename
						CorpusChooserPane.this.selectCurrentConfigRow(currentConfigFilename);
					}
				}
			});
			
			if(currentConfigFilenameBody.equals(name)){
				for(int j = 0; j < attributes.size(); j += nAttr){
					if(attributes.get(j + FN_CONFIG).equals(currentConfigFilename)){
						data[FN_CORPUSNAME] = attributes.get(j); // corpusname
						
						data[FN_AUX_TYPE] = comboBox; // type
						comboBox.setSelectedItem(attributes.get(j+FN_AUX_TYPE));
						data[FN_CONFIG] = attributes.get(j + FN_CONFIG); // configfile
						data[FN_DELETE] = deleteButton;
						if(isBaseCorpus((String)data[FN_CONFIG])) {
							deleteButton.setEnabled(false);
						} 
						break;
					}
				}
			} else {
				for(int j = 0; j < attributes.size(); j += nAttr){
					if(attributes.get(j + FN_AUX_TYPE).equals(auxTypes[0])){
						data[FN_CORPUSNAME] = attributes.get(j); // corpusname
						data[FN_AUX_TYPE] = comboBox; // type
						data[FN_CONFIG] = attributes.get(j + 2); // configfile
						data[FN_DELETE] = deleteButton;
						if(isBaseCorpus((String)data[FN_CONFIG])) {
							deleteButton.setEnabled(false);
						} 
						break;
					}
				}
			}
			comboBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					ArrayList<String> attributes = names.get(name);
					for(int i = 0; i < attributes.size(); i += nAttr){
						if(attributes.get(i+1).equals(e.getItem())){ // type
							int row = corpusTable.getSelectedRow();
							corpusTable.setValueAt(attributes.get(i), row, FN_CORPUSNAME);
							corpusTable.setValueAt(attributes.get(i+2), row, FN_CONFIG);
							break;
						}
					}
				}
			});

			tm.addRow(data);
		}
	}

	
	private int selectCurrentConfigRow(String configFilename) {
		
		for(int i = 0; i <corpusTable.getRowCount(); i++) {
			if(((String)corpusTable.getValueAt(i, FN_CONFIG)).equals(configFilename)) {
				corpusTable.setRowSelectionInterval(i, i);
				return i;
			}
		}
		
		if(corpusTable.getRowCount() > 0) {
			corpusTable.setRowSelectionInterval(0, 0);
		}
		
		return -1;
	}

	
	private boolean isBaseCorpus(String configFilename) {
		String configBody = configFilename.replaceFirst("(\\.sd|\\.db)?\\.xml$", ""); //$NON-NLS-1$ //$NON-NLS-2$
		if(configBody.equals("config") || configBody.equals("config_aozora_sample")) { //$NON-NLS-1$ //$NON-NLS-2$
			return true;
		} else {
			return false;
		}
	}

	
	private String getCorpusName(File file){
		try {
			Document doc = builder.parse(file);
			Node name = (Node)xpath.evaluate("/setting/corpora/@name", doc, XPathConstants.NODE); //$NON-NLS-1$
			return name.getNodeValue();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	class CorpusTableModel extends DefaultTableModel{

		private static final long serialVersionUID = 1L;

		CorpusTableModel(String[] columnNames, int rowNum){
			super(columnNames, rowNum);
		}
		
		@Override
		public Class<?> getColumnClass(int column){
			if(getValueAt(0, column) == null){
				return String.class;
			} else {
				return getValueAt(0, column).getClass();
			}
		}
		
		@Override
		public boolean isCellEditable(int row, int column) {
			if(column == FN_DELETE || column == FN_AUX_TYPE){
				return true;
			} else {
				return false;
			}
		}
	}
	
	class ComboBoxRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		@SuppressWarnings("unchecked")
		public Component getTableCellRendererComponent(
				JTable table, Object value,
				boolean isSelected, boolean hasFocus,
				int row, int column) {
			JComboBox<String> comboBox = (JComboBox<String>)value;
			Component c;
			
			if(comboBox == null) {
				c = new JLabel(""); //$NON-NLS-1$
				((JLabel) c).setOpaque(true);
				System.err.println("Warning:(CorpusChooserPane): type is null"); //$NON-NLS-1$
			} else if(comboBox.getItemCount() == 1){
				c = new JLabel(comboBox.getItemAt(0));
				((JLabel) c).setOpaque(true);
			} else {
				c = comboBox;
			}
			
            if(isSelected){
            	c.setForeground(table.getSelectionForeground());
            	c.setBackground(table.getSelectionBackground());
            	if(c instanceof JComboBox) {
            		JTextField ed = (JTextField)comboBox.getEditor().getEditorComponent();
            		if(System.getProperty("os.name").toLowerCase().startsWith("linux")){ //$NON-NLS-1$ //$NON-NLS-2$
            			// workaround for java on linux (GTK)
            			// https://bugs.java.com/bugdatabase/view_bug.do?bug_id=5043225
                    	ed.setForeground(Color.blue);
            		} else {
                    	ed.setForeground(table.getSelectionForeground());
            		}
                	ed.setBackground(table.getSelectionBackground());
            	}
			} else {
				c.setForeground(table.getForeground());
				c.setBackground(table.getBackground());
            	if(c instanceof JComboBox) {
            		JTextField ed = (JTextField)comboBox.getEditor().getEditorComponent();
            		ed.setForeground(table.getForeground());
                	ed.setBackground(table.getBackground());
            	}
			}
            
            return c;
		}
	}

	
	class ComboBoxEditor extends DefaultCellEditor {
		private static final long serialVersionUID = 1L;
		JComboBox<String> comboBox;
		
		public ComboBoxEditor(JComboBox<String> comboBox) {
			super(comboBox);
			this.comboBox = comboBox;
		}
		
		@Override
		public Object getCellEditorValue() {
			return comboBox;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			comboBox = (JComboBox<String>)value;
			
			if(value == null || comboBox.getItemCount() < 2){
				// disable to return this editor
				return null;
			} else {
				return comboBox;
			}
		}
	}
	
	
	class LabelRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(
				JTable table, Object value,
				boolean isSelected, boolean hasFocus,
				int row, int column) {

			JLabel label = new JLabel((String)value);
			label.setToolTipText((String)value);
			label.setOpaque(true);
			
            if(isSelected){
            	label.setForeground(table.getSelectionForeground());
            	label.setBackground(table.getSelectionBackground());
			} else {
				label.setForeground(table.getForeground());
				label.setBackground(table.getBackground());
			}

            return label;
		}
	}
	
	class ButtonEditor extends AbstractCellEditor implements TableCellEditor {
		JTable table;
		
		public ButtonEditor(JTable table) {
			this.table = table;
		}

		private static final long serialVersionUID = 1L;

		@Override
		public Object getCellEditorValue() {
			return table.getValueAt(table.getSelectedRow(), table.getSelectedColumn());
		}

		@Override
		public Component getTableCellEditorComponent(JTable arg0, Object arg1, boolean arg2, int arg3, int arg4) {
			return (JButton)arg1;
		}
	}
	

	class ButtonRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;
		
		public Component getTableCellRendererComponent(
				JTable table, Object value,
				boolean isSelected, boolean hasFocus,
				int row, int column) {

            return (JButton)value;
		}
	}
}

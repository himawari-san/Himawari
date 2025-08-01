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
 * @(#)Frame1_AboutBox.java	0.9.4 2003-10-20
 *
 * Copyright 2003
 * National Institute for Japanese Language All rights reserved.
 *
 * Written by Masaya YAMAGUCHI
 */
package jp.ac.ninjal.himawari;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class Frame1_AboutBox extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;

	JPanel panel1 = new JPanel();

	JPanel panel2 = new JPanel();

	JPanel insetsPanel1 = new JPanel();

	JPanel insetsPanel2 = new JPanel();

	JPanel insetsPanel3 = new JPanel();

	JButton button1 = new JButton();

	JLabel imageLabel = new JLabel();

	JLabel label1 = new JLabel();

	JLabel label2 = new JLabel();

	JLabel label3 = new JLabel();

	JLabel label4 = new JLabel();
	
	JLabel urlLabel = new JLabel();

	BorderLayout borderLayout1 = new BorderLayout();

	BorderLayout borderLayout2 = new BorderLayout();

	String product = Messages.getString("Frame1_AboutBox.0"); //$NON-NLS-1$

	String version = "Ver. 1.8a20250703 [released 20250731]"; //$NON-NLS-1$

	String copyright = "Copyright(c) 2004-2025 Masaya YAMAGUCHI"; //$NON-NLS-1$

	String comments = "<html><body>" + //$NON-NLS-1$
			"<em>Credit:</em>" + //$NON-NLS-1$ 
			"<p style=\"margin-left: 5px\">Localization: Eran Kim, Keigo KAWASAKI(Korean), <br />Motoki SANO(English), Masaya YAMAGUCHI(English, Japanese)</p>" + //$NON-NLS-1$
			"<p style=\"margin-left: 5px\">Icon Design: Nona ICHIHARA, Miwa NAKAGAWA, Toshinobu OGISO</p>" + //$NON-NLS-1$
			"<p>Java: " + System.getProperty("java.version") + ", " + System.getProperty("java.vm.name") + "</p>" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"</body></html>"; //$NON-NLS-1$

	BorderLayout borderLayout3 = new BorderLayout();

	GridBagLayout gridBagLayout1 = new GridBagLayout();

	public Frame1_AboutBox(Frame parent) {
		super(parent);
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		pack();
	}

	/** コンポーネントの初期化 */
	private void jbInit() throws Exception {
		this.setTitle(Messages.getString("Frame1_AboutBox.9")); //$NON-NLS-1$
		setResizable(false);
		panel1.setLayout(borderLayout1);
		panel2.setLayout(borderLayout2);
		insetsPanel2.setLayout(borderLayout3);
		insetsPanel2.setAlignmentX((float) 0.2);
		insetsPanel2.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		insetsPanel2.setDebugGraphicsOptions(0);
		label1.setText(product);
		label2.setText(version);
		label3.setText(copyright);
		label4.setText(comments);
		insetsPanel3.setLayout(gridBagLayout1);
		insetsPanel3.setBorder(BorderFactory.createEmptyBorder(10, 60, 10, 10));
		button1.setText(Messages.getString("Frame1_AboutBox.10")); //$NON-NLS-1$
		button1.addActionListener(this);
		imageLabel.setMaximumSize(new Dimension(64, 64));
		imageLabel.setIcon(new ImageIcon(Frame1_AboutBox.class
				.getResource("/jp/ac/ninjal/himawari/images/ogiso.png"))); //$NON-NLS-1$
		insetsPanel2.add(imageLabel, BorderLayout.CENTER);
		panel2.add(insetsPanel2, BorderLayout.WEST);
		this.getContentPane().add(panel1, null);
		
		JEditorPane je = new JEditorPane("text/html", getJavaRecommend()); //$NON-NLS-1$
		je.setEditable(false);
		je.setOpaque(false);
		je.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);	
		je.setFont(label1.getFont());
		je.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					try {
						Desktop.getDesktop().browse(e.getURL().toURI());
					} catch (IOException | URISyntaxException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		
		insetsPanel3.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 8, 0));
		insetsPanel3.add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 57, 0));
		insetsPanel3.add(label3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 7, 0));
		insetsPanel3.add(label4, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 52, 0));
		insetsPanel3.add(je, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		panel2.add(insetsPanel3, BorderLayout.CENTER);
		insetsPanel1.add(button1, null);
		panel1.add(insetsPanel1, BorderLayout.SOUTH);
		panel1.add(panel2, BorderLayout.NORTH);
	}

	/** ウィンドウが閉じられたときに終了するようにオーバーライド */
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			cancel();
		}
		super.processWindowEvent(e);
	}

	/** ダイアログを閉じる */
	void cancel() {
		dispose();
	}

	/** ボタンイベントでダイアログを閉じる */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == button1) {
			cancel();
		}
	}
	
	
	public String getJavaRecommend(){
		if(System.getProperty("os.name").toLowerCase().startsWith("windows")){ //$NON-NLS-1$ //$NON-NLS-2$
			if(System.getProperty("java.vm.name").toLowerCase().contains("64-bit")){ //$NON-NLS-1$ //$NON-NLS-2$
				return Messages.getString("Frame1_AboutBox.7"); //$NON-NLS-1$
			} else if(System.getenv("PROCESSOR_ARCHITEW6432").toLowerCase().contains("amd64")){ //$NON-NLS-1$ //$NON-NLS-2$
				return Messages.getString("Frame1_AboutBox.12"); //$NON-NLS-1$
			} else if(System.getenv("PROCESSOR_ARCHITECTURE").toLowerCase().contains("amd64")){ //$NON-NLS-1$ //$NON-NLS-2$
				return Messages.getString("Frame1_AboutBox.15"); //$NON-NLS-1$
			}
		}
		return ""; //$NON-NLS-1$
	}
}

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

import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jp.ac.ninjal.himawari.Util;
import jp.ac.ninjal.soundplayer.SoundPlayer.SoundPlayerCallback;



/**
 * <p>タイトル: </p>
 * <p>説明: </p>
 * <p>著作権: Copyright (c) 2003</p>
 * <p>会社名: </p>
 * @author Masaya YAMAGUCHI
 * @version 0.9.4
 */

public class SoundPlayerFrame extends JFrame {
	private static final long serialVersionUID = 110698933493048148L;

	private ImageIcon iconPlay = new ImageIcon(getClass().getResource("/jp/ac/ninjal/soundplayer/images/play.png")); //$NON-NLS-1$
	private ImageIcon iconStop = new ImageIcon(getClass().getResource("/jp/ac/ninjal/soundplayer/images/stop.png")); //$NON-NLS-1$
	private ImageIcon iconPause = new ImageIcon(getClass().getResource("/jp/ac/ninjal/soundplayer/images/pause.png")); //$NON-NLS-1$

	String SoundFileName;
	float startTime = 0;
	float endTime = -1;
	SoundPlayer player;

	BorderLayout borderLayout1 = new BorderLayout();
	JPanel jPanel1 = new JPanel();
	JPanel jPanel2 = new JPanel();
	JPanel jPanel3 = new JPanel();
	BorderLayout borderLayout2 = new BorderLayout();
	JPanel jPanel4 = new JPanel();
	JButton jButton_play = new JButton(iconPlay);
	JButton jButton_stop = new JButton(iconStop);
	TimeSlider timeSlider = new TimeSlider();
	GridLayout gridLayout1 = new GridLayout();
	GridLayout gridLayout2 = new GridLayout();
	JLabel jLabel_time_start = new JLabel();
	BorderLayout borderLayout4 = new BorderLayout();
	JLabel jLabel_time_end = new JLabel();
	JLabel jLabel_status = new JLabel();
	SoundPlayerCallback spb;
	boolean playerTerminating = false;

	public SoundPlayerFrame(String soundFileName, float startTime, float endTime) throws HeadlessException {
		this.SoundFileName = soundFileName;
		this.startTime = startTime;
		this.endTime = endTime;

		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		this.getContentPane().setLayout(borderLayout1);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				try {
					if(player.getStatus() != SoundPlayer.PLAYER_STATUS_STOP) {
						player.myStop();
					}
				} catch (InterruptedException | IOException e1) {
					e1.printStackTrace();
				}
				super.windowClosing(e);
			}
		});
		jPanel1.setLayout(borderLayout2);
		jButton_play.setPreferredSize(new Dimension(50, 22));
		jButton_play.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (player.getStatus() == SoundPlayer.PLAYER_STATUS_STOP) {
						player = new SoundPlayer(new URL(SoundFileName), spb);
						player.setTime(startTime, endTime);
						player.myPlay();
					} else if (player.getStatus() == SoundPlayer.PLAYER_STATUS_SUSPEND) {
						player.myPlay();
					} else if (player.getStatus() == SoundPlayer.PLAYER_STATUS_PLAY) {
						player.mySuspend();
					}
				} catch (UnsupportedAudioFileException | IOException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(SoundPlayerFrame.this,
							Messages.getString("SoundPlayerFrame.0"), //$NON-NLS-1$
							Messages.getString("SoundPlayerFrame.1"), //$NON-NLS-1$
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
		});
		jButton_stop.setPreferredSize(new Dimension(50, 22));
		jButton_stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					player.myStop();
				} catch (InterruptedException | IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		jPanel2.setLayout(gridLayout1);
		jPanel4.setLayout(gridLayout2);
		jLabel_time_start.setText("0"); //$NON-NLS-1$
		jLabel_time_start.setVerticalAlignment(SwingConstants.TOP);
		jLabel_time_start.setVerticalTextPosition(SwingConstants.CENTER);
		timeSlider.setLayout(borderLayout4);
		jLabel_time_end.setText("0"); //$NON-NLS-1$
		jLabel_time_end.setVerticalAlignment(SwingConstants.TOP);
		jLabel_time_end.setVerticalTextPosition(SwingConstants.CENTER);
		this.setSize(new Dimension(640, 125));

		timeSlider.setMajorTickSpacing(300);
		timeSlider.setMinorTickSpacing(60);
		timeSlider.setPaintTicks(true);
		timeSlider.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
		timeSlider.setFocusable(false);
		timeSlider.addChangeListener(new ChangeListener() {
			boolean isDragged = false;
			int value = 0;

			public void stateChanged(ChangeEvent e) {

				// while dragging
				if (timeSlider.getValueIsAdjusting()) {
					try {
						playerTerminating = true;
						value = timeSlider.getValue();
						isDragged = true;
						player.myStop();
						playerTerminating = false;
						jLabel_status.setText(Util.formatTime(value));
					} catch (InterruptedException | IOException e1) {
						e1.printStackTrace();
					}
					
				} else if (isDragged) {
					// end of dragging
					try {
						player = new SoundPlayer(new URL(SoundFileName), spb);
						player.init();
						player.setTime(0);
						player.setPlayTime(value);
						player.start();
					} catch (UnsupportedAudioFileException | IOException e2) {
						e2.printStackTrace();
					}
					isDragged = false;
				}
			}
		});
		timeSlider.addMouseListener(new MouseAdapter() {
			ToolTipManager ttm = ToolTipManager.sharedInstance();
			int defaultDelay = ttm.getInitialDelay();

			@Override
			public void mouseEntered(MouseEvent e) {
				ttm.setInitialDelay(0);
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				ttm.setInitialDelay(defaultDelay);
			}
		});
		
		timeSlider.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				timeSlider.setTipTime(e.getX());
			}
		});

		
		this.getContentPane().add(jPanel1, BorderLayout.CENTER);
		jPanel1.add(jPanel3, BorderLayout.SOUTH);
		JPanel jPanel3a = new JPanel();
		jPanel3.setLayout(new GridLayout(1,3));
		jPanel3.add(jLabel_status);
		jPanel3.add(jPanel3a);
		jPanel3.add(new JPanel());
		jPanel3a.add(jButton_play, BorderLayout.WEST);
		jPanel3a.add(jButton_stop, BorderLayout.EAST);
		jPanel1.add(jPanel4, BorderLayout.CENTER);
		jPanel4.add(timeSlider, null);
		this.getContentPane().add(jPanel2, BorderLayout.SOUTH);
		timeSlider.add(jLabel_time_start, BorderLayout.WEST);
		timeSlider.add(jLabel_time_end, BorderLayout.EAST);
		setTitle(SoundFileName);

		spb = new SoundPlayerCallback() {
			public float oggDataLength = -1;
			
			@Override
			public void init() {
				float soundLength;
				if(oggDataLength == -1) {
					soundLength = player.getMediaLength();
					oggDataLength = soundLength;
				} else {
					soundLength = oggDataLength;
				}
				jButton_play.setEnabled(true);
				jButton_play.setIcon(iconPlay);
				jButton_stop.setEnabled(false);
				timeSlider.setMaximum((int) soundLength);
				jLabel_time_end.setText(Util.formatTime(soundLength));
			}

			@Override
			public void play() {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						jButton_play.setIcon(iconPause);
						jButton_stop.setEnabled(true);
					}
				});
			}

			@Override
			public void stop() {
				player.setTime(startTime);

				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						jButton_play.setIcon(iconPlay);
						jButton_stop.setEnabled(false);
					}
				});
			}

			@Override
			public void changeStatus() {
				int currentTime = (int) player.getCurrentTime();
				if(!playerTerminating) {
					timeSlider.setValue(currentTime);
					jLabel_status.setText(Util.formatTime(currentTime));
				}
			}

			@Override
			public void suspend() {
				jButton_play.setIcon(iconPlay);
			}

			@Override
			public float getDataLength() {
				return oggDataLength;
				
			}
		};
	}

	public void start() {
		try {
			player = new SoundPlayer(new URL(SoundFileName), spb);
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
		}
		player.setTime(startTime, endTime);
		try {
			player.init();
		} catch (UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					Messages.getString("SoundPlayerFrame.2"), //$NON-NLS-1$
					Messages.getString("SoundPlayerFrame.3"), //$NON-NLS-1$
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		player.start();
	}

	public void setStatusBar(String text) {
		jLabel_status.setText(text);
	}

	public JLabel getStatusBar() {
		return jLabel_status;
	}

	public JLabel getEndTimeLabel() {
		return jLabel_time_end;
	}

	public JSlider getSlider() {
		return timeSlider;
	}

	public void setEndTimeLabel(String text) {
		jLabel_time_end.setText(text);
	}
}

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

import java.io.*;
import java.net.*;

import javax.sound.sampled.*;

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

public class SoundPlayer extends Thread {
	private static final int EXTERNAL_BUFFER_SIZE = 128;

	public static final int PLAYER_STATUS_STOP = 0;
	public static final int PLAYER_STATUS_PLAY = 1;
	public static final int PLAYER_STATUS_SUSPEND = 2;
	
	private volatile boolean flagRun = true;

	private URL url;

	private SourceDataLine line;

	private AudioInputStream audioInputStream;

	private AudioFormat audioFormat;

	private float frameRate;

	private int frameSize;

	private float totalsec = -1;

	private long totalRead = 0;

	private float startTime = 0;
	private float endTime = Integer.MAX_VALUE;
	private float currentTime = 0;
	private float clickedTime = 0;
	
	private SoundPlayerCallback callback;

	private Waiter waiter = new Waiter();
	private volatile int playerStatus = PLAYER_STATUS_STOP;


	public SoundPlayer(URL url, SoundPlayerCallback callback) {
		super("name"); //$NON-NLS-1$
		this.url = url;
		this.callback = callback;
	}

	public void init() throws UnsupportedAudioFileException, IOException {
		if (url.getFile().endsWith(".ogg")) { //$NON-NLS-1$
			AudioInputStream sourceAudioInputStream;
			sourceAudioInputStream = getAudioInputStream(url);
			AudioFormat sourceFormat = sourceAudioInputStream.getFormat();

			audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sourceFormat.getSampleRate(), 16,
					sourceFormat.getChannels(), sourceFormat.getChannels() * 2, sourceFormat.getSampleRate(), false);
			audioInputStream = AudioSystem.getAudioInputStream(audioFormat, sourceAudioInputStream);
			frameRate = audioFormat.getSampleRate();
			frameSize = audioFormat.getFrameSize();
			if (callback.getDataLength() == -1) {
				totalsec = getOggDataLength() / frameRate / frameSize;
			} else {
				totalsec = callback.getDataLength();
			}
		} else {
			audioInputStream = getAudioInputStream(url);
			audioFormat = audioInputStream.getFormat();
			frameRate = audioFormat.getSampleRate();
			frameSize = audioFormat.getFrameSize();
			totalsec = audioInputStream.getFrameLength() / frameRate;
		}

		callback.init();
		System.out.println("frameRate: " + frameRate); //$NON-NLS-1$
		System.out.println("frameSize: " + frameSize); //$NON-NLS-1$
	}

	private long getOggDataLength() throws UnsupportedAudioFileException, IOException {
		AudioInputStream sourceAudioInputStream = getAudioInputStream(url);
		AudioFormat sourceAudioFormat = sourceAudioInputStream.getFormat();
		AudioFormat decodedAudioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sourceAudioFormat.getSampleRate(),
				16, sourceAudioFormat.getChannels(), sourceAudioFormat.getChannels() * 2, sourceAudioFormat.getSampleRate(), false);
		AudioInputStream decodedAudioInputStream = AudioSystem.getAudioInputStream(decodedAudioFormat, sourceAudioInputStream);
		
		int nBytesRead = 0;
		long totalRead = 0;

		byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];

		while (nBytesRead != -1) {
			nBytesRead = decodedAudioInputStream.read(abData);
			totalRead += nBytesRead;
		}
		
		decodedAudioInputStream.close();

		return totalRead;
	}
	
	private AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException {
		String filename = url.getFile();
		if(filename.isEmpty() && url.getHost().isEmpty()) {
			return AudioSystem.getAudioInputStream(url);
		} else {
			return AudioSystem.getAudioInputStream(new File(filename));
		}
	}
	
	
	public void close() throws IOException {
		audioInputStream.close();
	}

	
	@Override
	public void start() {
		playerStatus = PLAYER_STATUS_PLAY;
		flagRun = true;
		callback.play();
		super.start();
	}

	@Override
	public void run() {
		try {
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(audioFormat);
			line.start();

			int nBytesRead = 0;
			totalRead = 0;
			byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];

			while (nBytesRead != -1 && flagRun) {
				nBytesRead = audioInputStream.read(abData);
				totalRead += nBytesRead;
				currentTime = totalRead / frameRate / frameSize;
				if (nBytesRead >= 0 && currentTime >= startTime && currentTime >= clickedTime) {
					audioInputStream.available();
					callback.changeStatus();

					// write data to the mixer
					line.write(abData, 0, nBytesRead);
				}
				if (currentTime > endTime) {
					flagRun = false;
				}
				if (getStatus() == PLAYER_STATUS_SUSPEND) {
					waiter.suspend();
				}
			}

			line.drain();
			line.stop();

			// buffered data close
			line.close();

			playerStatus = PLAYER_STATUS_STOP;
			callback.stop(); // currentTime will be changed in this callback

			totalRead = 0;
			currentTime = startTime; // set currentTime after callback.stop
			
			callback.changeStatus();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	
	public int getStatus() {
		return playerStatus;
	}

	
	public void myPlay() throws UnsupportedAudioFileException, IOException {

		if(playerStatus == PLAYER_STATUS_SUSPEND) {
			playerStatus = PLAYER_STATUS_PLAY;
			waiter.resume();
			line.start();
			callback.play();
		} else if(playerStatus == PLAYER_STATUS_STOP){
			init();
			start();
			playerStatus = PLAYER_STATUS_PLAY;
		}
	}
	
	public void mySuspend() {
		if(playerStatus == PLAYER_STATUS_PLAY) {
			line.stop();
			callback.suspend();
			playerStatus = PLAYER_STATUS_SUSPEND;
		}
	}

	public void myStop() throws InterruptedException, IOException {
		waiter.resume();
		flagRun = false;
		join();
		close();
		playerStatus = PLAYER_STATUS_STOP;
		callback.stop();
	}

//	public void updateTime() {
//		callback.changeStatus();
//
////		tf.setTime(jslider.getValue());
////		jLabel_statusbar.setText(tf.getTotalSec() + " / " + url); //$NON-NLS-1$
//	}

	public void setTime(float startTime) {
		this.startTime = startTime;
		this.endTime = getMediaLength();
		currentTime = startTime;
		clickedTime = startTime;
	}

	public void setTime(float startTime, float endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
		currentTime = startTime;
		clickedTime = startTime;
	}
	
	public void setPlayTime(float time) {
		clickedTime = time;
		if(clickedTime < startTime || clickedTime > endTime) {
			startTime = 0;
			endTime = getMediaLength();
		}
	}
	
	public float getMediaLength() {
		return totalsec;
	}

	public float getCurrentTime() {
		return currentTime;
	}
	
	
	public interface SoundPlayerCallback {
		void init();
		void play();
		void stop();
		void suspend();
		void changeStatus();
		float getDataLength();
	}
	
	private class Waiter {
		public synchronized void suspend() throws InterruptedException {
			this.wait();
		}

		public synchronized void resume() {
			this.notifyAll();
		}
	}
}

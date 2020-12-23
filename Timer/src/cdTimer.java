import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class cdTimer extends Frame {
	private int timeH, timeM, timeS;
	final static int hourMult = 3600000;
	final static int minMult = 60000;
	final static int secMult = 1000;
	private static String filePath;
	private static String resourcePath = "\\classic.wav";;
	private boolean exit;
	private boolean active;
	private Clip audioClip;

	// Assume within given bounds of 99/99/99
	cdTimer(int hour, int min, int sec) {
		while (sec >= 60) {
			sec -= 60;
			min++;
		}
		while (min >= 60) {
			min -= 60;
			hour++;
		}
		timeH = hour;
		timeM = min;
		timeS = sec;
	}

	private void buildFrame(JFrame f) {

		JLabel hourLabel = new JLabel("Hours:");
		hourLabel.setBounds(30, 50, 100, 40);// x, y, width, height
		hourLabel.setHorizontalAlignment(JLabel.LEFT);
		f.add(hourLabel);

		JTextField hourTextField = new JTextField("0");
		hourTextField.setEditable(true);
		hourTextField.setBounds(80, 50, 30, 40);// x, y, width, height
		hourTextField.setHorizontalAlignment(JTextField.CENTER);
		f.add(hourTextField);

		JLabel minLabel = new JLabel("Minutes:");
		minLabel.setBounds(120, 50, 100, 40);// x, y, width, height
		minLabel.setHorizontalAlignment(JLabel.LEFT);
		f.add(minLabel);

		JTextField minTextField = new JTextField("0");
		minTextField.setEditable(true);
		minTextField.setBounds(180, 50, 30, 40);// x, y, width, height
		minTextField.setHorizontalAlignment(JTextField.CENTER);
		f.add(minTextField);

		JLabel secLabel = new JLabel("Seconds:");
		secLabel.setBounds(220, 50, 100, 40);// x, y, width, height
		secLabel.setHorizontalAlignment(JLabel.LEFT);
		f.add(secLabel);

		JTextField secTextField = new JTextField("0");
		secTextField.setEditable(true);
		secTextField.setBounds(280, 50, 30, 40);// x, y, width, height
		secTextField.setHorizontalAlignment(JTextField.CENTER);
		f.add(secTextField);

		JLabel timerLabel = new JLabel("00:00:00");
		timerLabel.setBounds(30, 150, 150, 40);// x, y, width, height
		timerLabel.setHorizontalAlignment(JLabel.LEFT);
		timerLabel.setFont(new Font("Serif", Font.BOLD, 28));
		f.add(timerLabel);

		JButton setButton = new JButton("set time");// creating instance of JButton
		setButton.setBounds(30, 100, 100, 40);// x axis, y axis, width, height
		setButton.addActionListener(setButtonAction(f, hourTextField, minTextField, secTextField, timerLabel));
		f.add(setButton);// adding button in JFrame

		JButton startButton = new JButton("start timer");// creating instance of JButton
		startButton.setBounds(30, 200, 100, 40);// x axis, y axis, width, height
		startButton.addActionListener(startButtonAction(timerLabel));
		f.add(startButton);// adding button in JFrame

		JButton stopButton = new JButton("stop timer");// creating instance of JButton
		stopButton.setBounds(150, 200, 100, 40);// x axis, y axis, width, height
		stopButton.addActionListener(stopButtonAction());
		f.add(stopButton);// adding button in JFrame

		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				System.out.println("Window Event Called");
				System.exit(0);
			}
		});

		JLabel soundLabel = new JLabel("Select Alarm Sound:");
		soundLabel.setBounds(30, 270, 150, 40);// x, y, width, height
		soundLabel.setHorizontalAlignment(JLabel.LEFT);
		f.add(soundLabel);

		String[] sounds = { "Classic", "BakaBuzzer", "BakaBuzzer (Chain)" };
		JComboBox soundOptions = new JComboBox(sounds);
		soundOptions.setBounds(30, 300, 200, 30);
		f.add(soundOptions);

		JButton setAlarm = new JButton("Set");
		setAlarm.setBounds(240, 300, 60, 30);
		f.add(setAlarm);
		setAlarm.addActionListener(new ActionListener() {  
	        public void actionPerformed(ActionEvent e) { 
	        	try {
	        		System.out.println("Index: "+soundOptions.getSelectedIndex());
					setAudio2(soundOptions.getSelectedIndex());
				} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e1) {
					e1.printStackTrace();
				} 
	        	}  
		});		

		f.setSize(400, 400);// width and height
		f.setLayout(null);// using no layout managers
		f.setVisible(true);// making the frame visible
	}

	private ActionListener setButtonAction(JFrame f, JTextField h, JTextField m, JTextField s, JLabel t) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				boolean valid = true;
				int hours, minutes, seconds;
				hours = minutes = seconds = -1;
				try {
					hours = Integer.parseInt(h.getText());
					minutes = Integer.parseInt(m.getText());
					seconds = Integer.parseInt(s.getText());
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(f,
							"Invalid Input: Input Type. Please input int " + "for hours/minutes/seconds.",
							"User Input Error", JOptionPane.ERROR_MESSAGE);
					System.out.println("Invalid Input: Input Type. Please input int for hours/minutes/seconds.");
					valid = false;
				}
				if (hours < 0 || minutes < 0 || seconds < 0) {
					System.out
							.println("Invalid Input: Input Type. Please input positive int for hours/minutes/seconds.");
					JOptionPane.showMessageDialog(f,
							"Invalid Input: Input Type. Please input positive int " + "for hours/minutes/seconds.",
							"User Input Error", JOptionPane.ERROR_MESSAGE);
					valid = false;
				}
				// Testing
				System.out.println("Hour: " + hours + ". Minute: " + minutes + ". Seconds: " + seconds + ".");

				// Convert if seconds/minutes > 60, also check hour overflow > 99
				if (seconds > 60) {
					int mMulti = seconds / 60;
					seconds = seconds % 60;
					minutes = minutes + (mMulti);
				}
				if (minutes > 60) {
					int hMulti = minutes / 60;
					minutes = minutes % 60;
					hours = hours + (hMulti);
				}
				if (hours > 99) {
					JOptionPane
							.showMessageDialog(f,
									"Invalid Input: Overflow. Please do not enter a value over 99 hours, "
											+ "59 minutes, and 59 seconds.",
									"User Input Error", JOptionPane.ERROR_MESSAGE);
					System.out.println("Invalid Input: Overflow. Please do not enter a value over 99 hours,"
							+ " 59 minutes, and 59 seconds.");
					valid = false;
				}

				// Set Display Text Strings
				if (valid) {
					String hDisplay, mDisplay, sDisplay;
					hDisplay = String.valueOf(hours);
					mDisplay = String.valueOf(minutes);
					sDisplay = String.valueOf(seconds);
					String output = formatTimer(hDisplay, mDisplay, sDisplay);
					t.setText(output);
				}
			}
		};
	}

	private String formatTimer(String hDisplay, String mDisplay, String sDisplay) {
		int hours, minutes, seconds;
		hours = Integer.parseInt(hDisplay);
		minutes = Integer.parseInt(mDisplay);
		seconds = Integer.parseInt(sDisplay);
		if (hours < 10 && hours >= 0 && hDisplay.length() == 1)
			hDisplay = "0" + hDisplay;
		if (minutes < 10 && minutes >= 0 && mDisplay.length() == 1)
			mDisplay = "0" + mDisplay;
		if (seconds < 10 && seconds >= 0 && sDisplay.length() == 1)
			sDisplay = "0" + sDisplay;
		String output = (hDisplay + ":" + mDisplay + ":" + sDisplay);
		return output;
	}

	private ActionListener startButtonAction(JLabel t) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				Thread m = new Thread(() -> {
					runTimer(t);
				});
				exit = false;
				m.start(); // starts thread in background..
				// runTimer(t);
			}
		};
	}

	private ActionListener stopButtonAction() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				exit = true;
				active = false;
				audioClip.stop();
				System.out.println("Stop Button Pressed.");
			}
		};
	}

	private void runTimer(JLabel t) {
		if (!active) {
			while (!exit) {
				boolean running = true;
				active = true;

				String tText = t.getText();
				int hour = stripHour(tText);
				int minute = stripMinute(tText);
				int second = stripSecond(tText);

				long startTime = System.currentTimeMillis();
				long netRunTime = (hour * hourMult) + (minute * minMult) + (second * secMult);
				long endTime = startTime + netRunTime;
				long secLeft2 = -1;

				while (running) {
					if (exit)
						break;
					// refresh timeRemaining
					long runTimeRemain = endTime - System.currentTimeMillis();

					// convert timeLeft to H/M/S
					long hoursLeft = (runTimeRemain / hourMult);
					long newEnd = (runTimeRemain % hourMult);
					long minLeft = (newEnd / minMult);
					newEnd = (newEnd % minMult);
					long secLeft = (newEnd / secMult);

					if (secLeft != secLeft2) {
						System.out.println("Hours Remain: " + hoursLeft + ". Minutes Remain: " + minLeft
								+ ". Seconds Remain: " + secLeft);
						String h, m, s;
						h = String.valueOf(hoursLeft);
						m = String.valueOf(minLeft);
						s = String.valueOf(secLeft);
						String result = formatTimer(h, m, s);
						System.out.println(result);
						t.setText(result);
					}

					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					secLeft2 = secLeft;

					if (runTimeRemain <= 0) {
						running = false;
						playAlarmFromResource();
						//try {
							//playAlarm();
						//} catch (UnsupportedAudioFileException | IOException | LineUnavailableException
							//	| InterruptedException e) {
							//e.printStackTrace();
						//} 
					}
				}
			}
		}
	}

	private static int stripHour(String label) {
		String delims = "[:]";
		String[] tokens = label.split(delims);
		String output = tokens[0];
		return Integer.parseInt(output);
	}

	private static int stripMinute(String label) {
		String delims = "[:]";
		String[] tokens = label.split(delims);
		String output = tokens[1];
		return Integer.parseInt(output);
	}

	private static int stripSecond(String label) {
		String delims = "[:]";
		String[] tokens = label.split(delims);
		String output = tokens[2];
		return Integer.parseInt(output);
	}

	private void startTimerCA()
			throws InterruptedException, LineUnavailableException, IOException, UnsupportedAudioFileException {
		boolean running = true;

		long startTime = System.currentTimeMillis();
		long netRunTime = (timeH * hourMult) + (timeM * minMult) + (timeS * secMult);
		long endTime = startTime + netRunTime;
		long secLeft2 = -1;

		while (running) {
			// refresh timeRemaining
			long runTimeRemain = endTime - System.currentTimeMillis();

			// convert timeLeft to H/M/S
			long hoursLeft = (runTimeRemain / hourMult);
			long newEnd = (runTimeRemain % hourMult);
			long minLeft = (newEnd / minMult);
			newEnd = (newEnd % minMult);
			long secLeft = (newEnd / secMult);

			if (secLeft != secLeft2) {
				System.out.println(
						"Hours Remain: " + hoursLeft + ". Minutes Remain: " + minLeft + ". Seconds Remain: " + secLeft);
			}
			Thread.sleep(100);
			secLeft2 = secLeft;

			if (runTimeRemain <= 0) {
				running = false;
				playAlarm();
			}
		}
	}

	private void setAudio(int setting) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		if (setting == 3) {
			// incomplete, bad version
			// filePath = "C:\\Users\\Vincent\\eclipse-workspace\\Timer\\src\\sound2.wav";
		} else if (setting == 2) {
			filePath = "C:\\Users\\Vincent\\git\\repository\\Timer\\src\\bakabuzzer5.wav";
			System.out.println("setting = 2");
		} else if (setting == 1) {
			filePath = "C:\\Users\\Vincent\\git\\repository\\Timer\\src\\bakabuzzer3.wav";
			System.out.println("setting = 1");
		} else {
			filePath = "C:\\Users\\Vincent\\git\\repository\\Timer\\src\\classic.wav";
			System.out.println("setting = default");
		}
	}
	
	private void setAudio2(int setting) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		if (setting == 3) {
			// incomplete, bad version
			// filePath = "C:\\Users\\Vincent\\eclipse-workspace\\Timer\\src\\sound2.wav";
		} else if (setting == 2) {
			resourcePath = "\\bakabuzzer5.wav";
			System.out.println("setting = 2");
		} else if (setting == 1) {
			resourcePath = "\\bakabuzzer3.wav";
			System.out.println("setting = 1");
		} else {
			resourcePath = "\\classic.wav";
			System.out.println("setting = default");
		}
	}

	private void playAlarm()
			throws UnsupportedAudioFileException, IOException, LineUnavailableException, InterruptedException {
		File audioFile = new File(filePath);
		AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
		AudioFormat format = audioStream.getFormat();
		DataLine.Info info = new DataLine.Info(Clip.class, format);
		audioClip = (Clip) AudioSystem.getLine(info);
		audioClip.open(audioStream);
		audioClip.loop(Clip.LOOP_CONTINUOUSLY);

		// sleep after 5min of straight playing
		Thread.sleep(minMult * 5);
		audioClip.close();
		audioStream.close();
		// testing
	}
	
	private void playAlarmFromResource(){
		try
		{
			InputStream inputStream = cdTimer.class.getResourceAsStream(resourcePath);
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(inputStream);
			AudioFormat format = audioStream.getFormat();
			DataLine.Info info = new DataLine.Info(Clip.class, format);
			audioClip = (Clip) AudioSystem.getLine(info);
			audioClip.open(audioStream);
			audioClip.start();
			Thread.sleep(minMult * 5);
			audioClip.flush();
			audioClip.close();
			audioStream.close();
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException | InterruptedException e)
		{
			e.printStackTrace();
		} finally
		{
			System.out.println("Played");
		}

	}

	public static void main(String args[])
			throws InterruptedException, LineUnavailableException, IOException, UnsupportedAudioFileException {
		/*
		 * //Set Timer with time H/M/S cdTimer cd = new cdTimer(0, 9, 3); //Choose alarm
		 * tone cd.setAudio(3); //Start Timer cd.startTimerCA();
		 */
		cdTimer cd = new cdTimer(0, 0, 0);
		JFrame f = new JFrame("Barebones Timer Application");
		cd.buildFrame(f);

	}
}
package canisius.jim;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;


/**
 * Allows a {@code Ruppet} to talk by reading the timing information from a text file and storing 
 * the timing information into a {@code Track} with the corresponding MIDI events and plays the 
 * {@code Track} synched with a .wav audio file. The {@code Voice} class implements the use of the
 * lower jaw of the {@code Ruppet} to speak rendering it unavailable for emotional expressions.
 * 
 * @author Jon Mrowczynski
 */

public class Voice implements Runnable {

	/* The two save files that store the names of the files that contain the timing information	
		for the mouth movements and the desired audio file to be played respectively */
	public static final File voiceSaveFile = new File("VoiceSaveFile.txt");
	public static final File audioSaveFile = new File("AudioSaveFile.txt");

	/* The Ruppet that this voice belongs to as well as it's mouth */

	private Ruppet ruppet = null;
	private Movable mouth = null;
	
	/* We want to pre-load the audio file into a Clip to avoid delays when speaking */

	private Clip clip = null;
	
	/* The Track that stores the timing information for the Ruppet's mouth movements based 
	   the timing information gathered from the ArrayLists. */

	private Track voiceTrack = null;
	
	/* ArrayLists that that store the specific timings for moving the mouth up and down. 
	   *NOTE*: The timeOpen and timeClose ArrayLists are used to help with making the mouth
	   movements more continuous and less sudden by determining when to turn the motors off	
	   before turning them back on to move the mouth up or down. 
	   The method (RuppetControl.convergeTimes) helps to converge the motor off time closer 
	   to the motor on time so that it makes the Ruppet's mouth movements seem less robotic */

	private ArrayList<Integer> down = new ArrayList<>();
	private ArrayList<Integer> up = new ArrayList<>();
	private ArrayList<Integer> timeOpen = new ArrayList<>();
	private ArrayList<Integer> timeClose = new ArrayList<>();

	/* Constructor that calls methods that reads in the lower jaw timing information, stores the 
		timing information into the voice Track which is passed in as the only parameter 
		and pre-loads an audio clip to speak later. */

	public Voice (Ruppet ruppet, Track mouthTimings) {
		this.ruppet = ruppet;
		mouth = ruppet.getLowerJaw();
		voiceTrack = mouthTimings;
		readTimingLabels();
		openAudioFile();
		setupTimings();
	}
	
	public void run() { givePresentation();	}

	public void givePresentation() { moveMouth(); }
	
	/* Reads an array of doubles from a text file, manipulates the timing values into ms
	   instead of seconds and stores the times into Integer ArrayLists for later use in the 
	   setupMouthTimings method. 
	   *NOTE*: Integer ArrayLists because the timings for MidiEvents can NOT be 
	   floating point values. */

	public void readTimingLabels() {
		try { RuppetControl.checkSaveFile(voiceSaveFile); } 
		catch(IOException ex) { ex.printStackTrace(); }
		try (final Scanner reader = new Scanner(new FileReader(voiceSaveFile))) {
			final int sec_to_ms_factor = 1000;
			while(reader.hasNext()) {
				down.add( (int) Math.round(reader.nextDouble() * sec_to_ms_factor)); //read starting value
				reader.nextDouble();
				reader.nextLine();
				up.add( (int) Math.round(reader.nextDouble() * sec_to_ms_factor));
				reader.nextDouble();
				reader.nextLine();
			}
		} catch (FileNotFoundException e) { e.printStackTrace(); }

		/* These times are in milliseconds in order to make the MidiEvents properly */

		for(int i = 0; i < down.size(); ++i) {
			timeOpen.add(up.get(i) - down.get(i));
			if(i + 1 < down.size())
				timeClose.add(down.get(i + 1) - up.get(i));
		}
		timeClose.add(2000);
		down.trimToSize();
		up.trimToSize();
		timeOpen.trimToSize();
		timeClose.trimToSize();
	}
	
	/* Uses the data stored in the ArrayLists to create MIDI Tracks with the corresponding MIDI events. */

	public void setupTimings() {
		final int delay_end_of_seq = 10000;
		final ShortMessage[] mouthDown = mouth.getLowerBoundState();
		final ShortMessage[] mouthUp = mouth.getUpperBoundState();
		for(int i = 0; i < timeClose.size(); ++i) {
			mouth.addStateToTrack(voiceTrack, mouthDown, down.get(i));
			mouth.addStateToTrack(voiceTrack, mouthUp, up.get(i));
		}

		/* Added another two tracks to prevent (a) blip(s) at the end of the presentation. 
		   This prevents us from hearing the sequence being looping over again to early. */

		mouth.addStateToTrack(voiceTrack, mouthDown, (down.get(down.size() - 1) + delay_end_of_seq));
		mouth.addStateToTrack(voiceTrack, mouthUp, (down.get(down.size() - 1) + delay_end_of_seq));
	}
	
	/* Pre-loads the user defined audio file. Currently this has only been tested with a .wav	
	   file, but other audio files should work as well...*/

	private void openAudioFile() {
		try { RuppetControl.checkSaveFile(audioSaveFile); } 
		catch(IOException e) { e.printStackTrace(); }
		try {
			clip = AudioSystem.getClip();
			clip.open(AudioSystem.getAudioInputStream(audioSaveFile));
		} catch (LineUnavailableException | IOException e) { e.printStackTrace(); }
		  catch (UnsupportedAudioFileException e) {
			System.out.println("ERROR:");
			System.out.println("\nFile: " + audioSaveFile.getName() + " is not supported!");
			System.out.println("Make sure that you are using a .wav file!");
			RuppetControl.clearSaveFile(audioSaveFile);
		}
    }
	
	/* This method plays the emotions and mouth movement Tracks allowing 
	   the Ruppet to speak. */
		//Rename this to give presentation since this seems to work more efficiently
	// Make this method the runnable function.
	private void moveMouth() {
		final int us_to_ms_factor = 1000;
		MidiConnection.getSequencer().setTrackSolo(ruppet.getTracks().indexOf(ruppet.getEmotionTrack()), true); 
		MidiConnection.getSequencer().setTrackSolo(ruppet.getTracks().indexOf(ruppet.getVoiceTrack()), true);
		clip.stop();
		MidiConnection.getSequencer().stop();
		clip.setMicrosecondPosition(0);
		MidiConnection.getSequencer().setMicrosecondPosition(0);
		/*
		clip.start();
		Connect.getSequencer().start(); 
		*/
		( new Thread( new SyncVoiceWithScript(clip) ) ).start();
		RuppetControl.pause_ms( (int) (clip.getMicrosecondLength() / us_to_ms_factor));
		MidiConnection.getSequencer().setTrackSolo(ruppet.getTracks().indexOf(ruppet.getEmotionTrack()), false);
		MidiConnection.getSequencer().setTrackSolo(ruppet.getTracks().indexOf(ruppet.getVoiceTrack()), false);
	}

	private class SyncVoiceWithScript extends Thread {
		private final Clip syncClip;
		public SyncVoiceWithScript(Clip clip) { syncClip = clip; }
		
		/* Start both the clip and the sequence of MIDI notes */

		public void run() {
			syncClip.start();
			MidiConnection.getSequencer().start(); 
		}

	}
	
} // end of Voice class
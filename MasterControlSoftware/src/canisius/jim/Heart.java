package canisius.jim;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

/**
 *	The Heart mainly deals with the Emotional states of the Ruppet that can either 
 *	be expressed on command or added to Track. These methods to accomplish those
 *	tasks are contained within the Emotion class
 *	
 *	@author Jon Mrowczynski
 *	@version 1.2
 */

class Heart {
	
	/**
	 * The file in which the name of the file for the emotional timing information for expressing is stored.
	 */

	private static final File emoteSaveFile = new File("EmotionSaveFile.txt");

	/**
	 * Represents the neutral emotional state of the {@code Ruppet}.
	 */

	private final Emotion neutral;
	
	/**
	 * Represents the happy emotional state of the {@code Ruppet}.
	 */
	
	private final Emotion happy;
	
	/**
	 * Represents the sad emotional state of the {@code Ruppet}.
	 */
	
	private final Emotion sad;
	
	/**
	 * Represents the angry emotional state of the {@code Ruppet}.
	 */
	
	private final Emotion angry;
	
	/**
	 * Represents the scared emotional state of the {@code Ruppet}.
	 */
	
	private final Emotion scared;
	
	/**
	 * Represents the smile emotional state of the {@code Ruppet}.
	 */
	
	private final Emotion smile;
		
	/**
	 * The {@code Track} that stores all of the emotion expression timings.
	 */

	private final Track emotionTrack;
	
	/**
	 * Stores the times at which a specified {@code Emotion} should be expressed.
	 */

	private final ArrayList<Integer> emotionTimes = new ArrayList<>();
	
	/**
	 * Stores the names of the {@code Emotion}s that are associated with the {@code Emotion}s that 
	 * are stored in the {@code emotionTimes ArrayList}.
	 */
	
	private final ArrayList<String> emotions = new ArrayList<>();
	
	/* This is the only constructor for the Heart class. It takes a Track as a parameter 
	   so that the Heart class can setup the emotions Track with MidiEvents whose timings 
	   are set from the ArrayLists. */ 
	
	/**
	 * This is the only constructor for the {@code Heart} class. It takes a {@code Track} as a parameter
	 * so that the {@code Heart} class can setup the emotions {@code Track} with {@code MidiEvent}s 
	 * whose timings are set from the {@code emotionTimes ArrayList}.
	 * 
	 * @param ruppet The {@code Ruppet} that this {@code Heart} object belongs to.
	 * @param emotionTrack The {@code Track} that is to store all of the {@code Emotion} transition times.
	 */

	Heart(Ruppet ruppet, Track emotionTrack) {

		final Movable lowerJaw   = ruppet.getLowerJaw();
		final Movable lipCorners = ruppet.getLipCorners();
		final Movable eyebrow 	 = ruppet.getEyebrows();
		final Movable eyelid 	 = ruppet.getEyelids();

		/* Initialize all of the Emotions of the Ruppet 
		   *NOTE*: That the values for the lipCorners are relative to EDDIE's left cheek */

		neutral = new Emotion(
			ruppet,
			lowerJaw.getNeutralPartState(), 
			lipCorners.getNeutralPartState(), 
			eyebrow.getNeutralPartState(), 
			eyelid.getNeutralPartState()
		);

		happy = new Emotion(
			ruppet,
			lowerJaw.getPartState(lowerJaw.getUpperBound()), 
			lipCorners.getPartState(lipCorners.getUpperBound())
		);

		sad = new Emotion(
			ruppet,
			lowerJaw.getPartState(lowerJaw.getUpperBound()), 
			lipCorners.getPartState(lipCorners.getLowerBound()), 
			eyebrow.getPartState(eyebrow.getUpperBound())
		);

		angry = new Emotion(
			ruppet,
			lowerJaw.getPartState(lowerJaw.getUpperBound()), 
			lipCorners.getPartState(lipCorners.getLowerBound()), 
			eyebrow.getPartState(eyebrow.getLowerBound())
		);

		scared = new Emotion(
			ruppet,
			lowerJaw.getPartState(lowerJaw.getUpperBound()), 
			lipCorners.getPartState(lipCorners.getLowerBound()), 
			eyebrow.getPartState(eyebrow.getLowerBound())
		);

		smile = new Emotion(
			ruppet,
			lipCorners.getPartState(lipCorners.getUpperBound())
		);

		this.emotionTrack = emotionTrack;
		readTimingLabels();
		setupTimings();

	} // end of Heart constructor 
	
	/**
	 * Display a {@code Emotion} on command.
	 * 
	 * @param emotion The {@code Emotion} that the {@code Ruppet} is to feel.
	 */

	final void feel(Emotion emotion) {
		for (final ShortMessage msg : emotion.getAttributes())
			MidiConnection.getUsbReceiver().send(msg, -1);
	}

	/**
	 * Gets the neutral {@code Emotion} of the {@code Ruppet}.
	 * 
	 * @return The {@code Ruppet}'s neutral {@code Emotion}
	 */

	final Emotion getNeutral() { return neutral; }
	
	/**
	 * Gets the happy {@code Emotion} of the {@code Ruppet}.
	 * 
	 * @return The {@code Ruppet}'s neutral {@code Emotion}.
	 */
	
	final Emotion getHappy() { return happy;   }
	
	/**
	 * Gets the sad {@code Emotion} of the {@code Ruppet}.
	 * 
	 * @return The {@code Ruppet}'s sad {@code Emotion}.
	 */
	
	final Emotion getSad() { return sad; }
	
	/**
	 * Gets the angry {@code Emotion} of the {@code Ruppet}.
	 * 
	 * @return The {@code Ruppet}'s angry {@code Emotion}.
	 */
	
	final Emotion getAngry() { return angry; }
	
	/**
	 * Gets the scared {@code Emotion} of the {@code Ruppet}.
	 * 
	 * @return The {@code Ruppet}'s angry {@code Emotion}.
	 */
	
	final Emotion getScared() { return scared; }
	
	/**
	 * Gets the smile {@code Emotion} of the {@code Ruppet}.
	 * 
	 * @return The {@code Ruppet}'s smile {@code Emotion}.
	 */
	
	final Emotion getSmile() { return smile; }
	
	/**
	 * Reads {@code Emotion} transition timings from a text file and stores that data into the 
	 * corresponding {@code ArrayList}.
	 */

	private void readTimingLabels() {
		final int sec_to_ms_factor = 1000;
		try { RuppetControl.checkSaveFile(emoteSaveFile); } 
		catch(IOException ex) {ex.printStackTrace();}
		try (final Scanner reader = new Scanner(new FileReader(emoteSaveFile))) {
			while(reader.hasNext()) {

				/* We want to multiply the time by 1000 because we want the time to be in ms not sec */

				emotionTimes.add( (int) Math.round(reader.nextDouble() * sec_to_ms_factor));
				emotions.add(reader.nextLine().trim());
			}
		} catch (FileNotFoundException ex) {ex.printStackTrace();}
		emotionTimes.trimToSize();
		emotions.trimToSize();
	}
		
	/**
	 * Sets up all of the emotional timings for a prerecorded script.
	 */

	private void setupTimings() {
		for(short i = 0; i < emotions.size(); i++) {
			switch(emotions.get(i).toUpperCase()) {
                case "HAPPY":
                    happy.addEmotionToTrack(emotionTrack, emotionTimes.get(i));
                    break;
                case "SAD":
                    sad.addEmotionToTrack(emotionTrack, emotionTimes.get(i));
                    break;
                case "ANGRY":
                    angry.addEmotionToTrack(emotionTrack, emotionTimes.get(i));
                    break;
                case "SCARED":
                    scared.addEmotionToTrack(emotionTrack, emotionTimes.get(i));
                    break;
                case "NEUTRAL":
                    neutral.addEmotionToTrack(emotionTrack, emotionTimes.get(i));
                    break;
                case "SMILE":
                    smile.addEmotionToTrack(emotionTrack, emotionTimes.get(i));
                    break;
                default:
                    System.out.println("\nEMOTION DEFINITION ERROR:");
                    System.out.println("\tEmotion: \"" + emotions.get(i) + "\" is not currently defined.");
                    System.out.println("\n\tPlease define what to do for \"" + emotions.get(i) + "\" before operating ruppet.");
                    System.out.println("\nTerminating Program.");
                    System.exit(1);
                    break;
            }
		}
	}

	/* This method automatically counts how many different emotions Jim will be expressing
	   in the Emotion Labels Track, by counting the number of different strings labeled in
	   the third column of the file */
	
	/**
	 * Automatically counts how many unique emotions the {@code Ruppet} will be expressing
	 * in the {@code emotion ArrayList}.
	 * 
	 * @return The number of unique {@code Emotion}s that will be expressed in a script.
	 */

	private int getNumOfEmotes(){
		if (emotions.size() <= 0) {
			System.out.println("FATAL ERROR: ArrayList emotions size == 0");
			System.out.println("Make sure that the emotion timing labels are labeled properly " 
				+ "and that they were read before calling the getNumOfEmotes method.");
			System.out.println("Terminating Program.");
			RuppetControl.pause();
			System.exit(1);
		}
		return new HashSet<>(emotions).size();
	} 

} // end of Heart class
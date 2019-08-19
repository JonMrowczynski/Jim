package canisius.jim;

import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

/**
 *	The {@code Heart} mainly deals with the {@code Emotion}al states of the {@code Ruppet} that can either be expressed
 *	on command or added to a {@code Track} to be sequenced.
 *	
 *	@author Jon Mrowczynski
 */

final class Heart {
	
	/**
	 * The {@code File} in which the name of the {@code File} for expressing emotions over a period of time is stored.
	 */

	private static final File emoteSaveFile = new File("EmotionSaveFile.txt");

	/**
	 * The neutral {@code Emotion} state of the {@code Ruppet}.
	 */

	private final Emotion neutral;
	
	/**
	 * The happy {@code Emotion} state of the {@code Ruppet}.
	 */
	
	private final Emotion happy;
	
	/**
	 * The sad {@code Emotion} state of the {@code Ruppet}.
	 */
	
	private final Emotion sad;
	
	/**
	 * The angry {@code Emotion} state of the {@code Ruppet}.
	 */
	
	private final Emotion angry;
	
	/**
	 * The scared {@code Emotion} state of the {@code Ruppet}.
	 */
	
	private final Emotion scared;
	
	/**
	 * The smile {@code Emotion} state of the {@code Ruppet}.
	 */
	
	private final Emotion smile;
		
	/**
	 * The {@code Track} that stores all of the {@code Emotion} expression timings.
	 */

	private final Track emotionTrack;

    /**
     * Maps times in ms to a {@code String} which represents the {@code Emotion} that should be transitioned to at the
     * corresponding time.
     */

	private final Map<Integer, String> emotionTimingsMap = new LinkedHashMap<>();

	/**
	 * This is the only constructor for the {@code Heart} class. It takes a {@code Track} as a parameter so that the
     * {@code Heart} class can setup the emotions {@code Track} with {@code MidiEvent}s whose timings are set from the
     * {@code emotionTimes ArrayList}.
	 * 
	 * @param ruppet that this {@code Heart} object belongs to.
	 * @param actions that is used to create a {@code Track} to store all of the {@code Emotion} expression timings.
	 */

	Heart(final Ruppet ruppet, final Sequence actions) {
		final Movable lowerJaw   = ruppet.getLowerJaw();
		final Movable lipCorners = ruppet.getLipCorners();
		final Movable eyebrow 	 = ruppet.getEyebrows();
		final Movable eyelid 	 = ruppet.getEyelids();

		neutral = new Emotion(ruppet, lowerJaw.getNeutralPartState(), lipCorners.getNeutralPartState(), eyebrow.getNeutralPartState(), eyelid.getNeutralPartState());
		happy = new Emotion(ruppet, lowerJaw.getUpperBoundPartState(), lipCorners.getUpperBoundPartState());
		sad = new Emotion(ruppet, lowerJaw.getUpperBoundPartState(), lipCorners.getLowerBoundPartState(), eyebrow.getUpperBoundPartState());
		angry = new Emotion(ruppet, lowerJaw.getUpperBoundPartState(), lipCorners.getLowerBoundPartState(), eyebrow.getLowerBoundPartState());
		scared = new Emotion(ruppet, lowerJaw.getUpperBoundPartState(), lipCorners.getLowerBoundPartState(), eyebrow.getLowerBoundPartState());
		smile = new Emotion(ruppet, lipCorners.getUpperBoundPartState());

		emotionTrack = actions.createTrack();
		readTimingLabels();
		setupTimings();
	}
	
	/**
	 * Display an {@code Emotion} on command.
	 * 
	 * @param emotion The {@code Emotion} that the {@code Ruppet} is to feel.
	 */

	final void feel(Emotion emotion) { emotion.getAttributes().forEachRemaining(msg -> MidiConnection.getUsbReceiver().send(msg, -1)); }

	/**
	 * Gets the neutral {@code Emotion} of the {@code Ruppet}.
	 * 
	 * @return The {@code Ruppet}'s neutral {@code Emotion}.
	 */

	final Emotion getNeutral() { return neutral; }
	
	/**
	 * Gets the happy {@code Emotion} of the {@code Ruppet}.
	 * 
	 * @return The {@code Ruppet}'s happy {@code Emotion}.
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
	 * @return The {@code Ruppet}'s scared {@code Emotion}.
	 */
	
	final Emotion getScared() { return scared; }
	
	/**
	 * Gets the smile {@code Emotion} of the {@code Ruppet}.
	 * 
	 * @return The {@code Ruppet}'s smile {@code Emotion}.
	 */
	
	final Emotion getSmile() { return smile; }
	
	/**
	 * Reads {@code Emotion} states and transition timings from a text file and stores that data into
     * {@code emotionTimingsMap}.
	 */

	private void readTimingLabels() {
		try {
		    RuppetControl.checkFileExistence(emoteSaveFile);
            try (final Scanner reader = new Scanner(new FileReader(emoteSaveFile))) {
                while(reader.hasNext()) { emotionTimingsMap.put((int) Math.round(reader.nextDouble() * 1000), reader.nextLine().trim()); }
            } catch (FileNotFoundException ex) {ex.printStackTrace();}
		}
		catch(IOException ex) {ex.printStackTrace();}
	}
		
	/**
	 * Sets up all of the {@code Emotion} timings for a prerecorded script.
	 */

	private void setupTimings() {
	    for (final Map.Entry<Integer, String> entry : emotionTimingsMap.entrySet()) {
	        final String emotion = entry.getValue();
	        final int emotionTransitionTime = entry.getKey();
            switch(emotion.toUpperCase()) {
                case "HAPPY": happy.addEmotionToTrack(emotionTrack, emotionTransitionTime);       break;
                case "SAD": sad.addEmotionToTrack(emotionTrack, emotionTransitionTime);           break;
                case "ANGRY": angry.addEmotionToTrack(emotionTrack, emotionTransitionTime);       break;
                case "SCARED": scared.addEmotionToTrack(emotionTrack, emotionTransitionTime);     break;
                case "NEUTRAL": neutral.addEmotionToTrack(emotionTrack, emotionTransitionTime);   break;
                case "SMILE": smile.addEmotionToTrack(emotionTrack, emotionTransitionTime);       break;
                default:
                    System.out.println("\nEMOTION DEFINITION ERROR:");
                    System.out.println("\tEmotion: \"" + emotion + "\" is not currently defined.");
                    System.out.println("\n\tPlease define what to do for \"" + emotion + "\" before operating ruppet.");
                    System.out.println("\nTerminating Program.");
                    System.exit(1);
                    break;
            }
        }
	}

	/**
	 * Gets the {@code Track} that contains all of the {@code Emotion} transition timings.
	 *
	 * @return The {@code Track} that stores the {@code Emotion} transition timings.
	 */

	final Track getEmotionTrack() { return emotionTrack; }

} // end of Heart class
package canisius.jim;

import javafx.application.Application;
import javafx.stage.Stage;

/**	
	This class allows one lone {@code Ruppet} to live, to see the world, things dangerous to come to, to 
	see behind walls, draw closer, to find each other, and to feel.
	
	@author Jon Mrowczynski
	@version 1.3
*/

public final class BreathOfLife extends Application {
	
	/**
	 * Creates a new {@code Ruppet} and lets it live its life.
	 * 
	 * @param args The command line arguments.
	 */
	
	public static void main(final String[] args) {
	
		launch(args);
		
	}

	@Override
	public void start(Stage stage) {
		
		final Ruppet eddie = new Ruppet();
		eddie.live();
		
	} 

} // end of class BreathOfLife
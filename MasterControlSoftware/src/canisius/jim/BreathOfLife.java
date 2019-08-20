package canisius.jim;

import canisius.jim.ruppet.Ruppet;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * 	This class allows one lone {@code Ruppet} to live, to see the world, things dangerous to come to, to see behind
 * 	walls, draw closer, to find each other, and to feel.
 *
 *  @author Jon Mrowczynski
 */

public final class BreathOfLife extends Application {
	
	public static void main(final String[] args) { launch(args); }

	@Override
	public final void start(final Stage stage) {
		final Ruppet jim = new Ruppet();
		jim.live();
	} 

}
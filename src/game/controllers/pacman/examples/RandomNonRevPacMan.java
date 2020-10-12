package game.controllers.pacman.examples;

import game.controllers.pacman.PacManHijackController;
import game.core.G;
import game.core.Game;

public final class RandomNonRevPacMan extends PacManHijackController
{	
	@Override
	public void tick(Game game, long timeDue) {
		int[] directions=game.getPossiblePacManDirs(false);		//set flag as false to prevent reversals	
		pacman.set(directions[G.rnd.nextInt(directions.length)]);		
	}
}

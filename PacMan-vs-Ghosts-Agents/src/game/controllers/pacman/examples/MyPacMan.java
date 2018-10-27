package game.controllers.pacman.examples;

import game.PacManSimulator;
import game.controllers.ghosts.game.GameGhosts;
import game.controllers.pacman.PacManHijackController;
import game.core.G;
import game.core.Game;

public final class MyPacMan extends PacManHijackController
{	
	@Override
	public void tick(Game game, long timeDue) {
		
		// Code your agent here.
		
		// Dummy implementation: move in a random direction.  You won't live long this way,
		int[] directions=game.getPossiblePacManDirs(false);	
		pacman.set(directions[G.rnd.nextInt(directions.length)]);
		
	}
		
	public static void main(String[] args) {
		PacManSimulator.play(new MyPacMan(), new GameGhosts());
	}
}
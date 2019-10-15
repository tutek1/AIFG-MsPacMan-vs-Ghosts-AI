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
		// If seed = 0, a random seed is chosen on every run.  Set seed to a positive value
		// for repeatable play.
		int seed = 0;
		
		// The number of lives to start with.
		int lives = 3;
		
		// The number of milliseconds between frames; 40 ms = 25 frames per second.
		int thinkTime = 40;
		
		PacManSimulator.play(new MyPacMan(), new GameGhosts(), seed, lives, thinkTime);
	}
}

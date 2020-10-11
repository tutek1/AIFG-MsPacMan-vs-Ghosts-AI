import game.PacManSimulator;
import game.controllers.ghosts.game.GameGhosts;

class RunMyPacMan {
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

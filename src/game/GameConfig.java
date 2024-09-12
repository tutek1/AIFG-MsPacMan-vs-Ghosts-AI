package game;

import game.core.G;

public class GameConfig {
	
	public int seed = -1;
	
	/**
	 * Total percentage of PILLS present within the level. If < 1, some (random) pills will be taken away.
	 */
	public double totalPills = 1;
    
    public int startingLevel = 1;

	/**
	 * How many levels Ms PacMan may play (-1 => unbound).
	 */
	public int levelsToPlay = -1;
	
	/**
	 * How many lives to start with.
	 */
	public int lives = G.NUM_LIVES;
}

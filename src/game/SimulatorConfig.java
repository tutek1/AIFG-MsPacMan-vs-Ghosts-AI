package game;

import controllers.ghosts.IGhostsController;
import controllers.pacman.IPacManController;

import java.io.File;

public class SimulatorConfig {

	public GameConfig game = new GameConfig();
	
	public boolean visualize = true;
	public int visualizationScale = 3;
	
	public boolean mayBePaused = true;
	
	public IPacManController pacManController;
	public IGhostsController ghostsController;
	
	/**
	 * How long can PacMan / Ghost controller think about the game before we compute next frame.
	 * If {@ #visualize} than it also determines the speed of the game.
	 * 
	 * DEFAULT: 25 FPS
	 */
	public int thinkTimeMillis = 40;
	
	public boolean replay = false;
	public File replayFile = null;
	
	public SimulatorConfig clone() {
		SimulatorConfig result = new SimulatorConfig();
		
		result.game = game.clone();
		
		result.visualize = visualize;
		result.visualizationScale = visualizationScale;
		
		result.mayBePaused = mayBePaused;
		
		result.pacManController = pacManController;
		result.ghostsController = ghostsController;
		
		result.thinkTimeMillis = thinkTimeMillis;
				
		result.replay = replay;
		result.replayFile = replayFile;
		
		return result;
	}
}

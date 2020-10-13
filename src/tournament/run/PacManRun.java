package tournament.run;

import game.PacManSimulator;
import game.SimulatorConfig;
import game.controllers.pacman.IPacManController;
import game.core.Game;

public class PacManRun {
	private SimulatorConfig config;
	
	public PacManRun(SimulatorConfig config) {
		this.config = config;
	}
	
	public synchronized PacManRunResult run(IPacManController pacMan) {		
		PacManRunResult result = new PacManRunResult(config);
		
        config.pacManController = pacMan;
        
        Game info = PacManSimulator.play(config);
        result.addResult(info);
        
        System.out.printf("seed %2d: score = %5d\n", config.game.seed, info.getScore());

		return result;		
	}

	public SimulatorConfig getConfig() {
		return config;
	}
}

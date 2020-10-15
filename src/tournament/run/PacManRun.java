package tournament.run;

import game.PacManSimulator;
import game.SimulatorConfig;
import controllers.pacman.IPacManController;
import game.core.Game;

public class PacManRun {
	private SimulatorConfig config;
	
	public PacManRun(SimulatorConfig config) {
		this.config = config;
	}
	
	public synchronized PacManRunResult run(IPacManController pacMan, boolean verbose) {		
        config.pacManController = pacMan;
        
        Game info = PacManSimulator.play(config);
		PacManRunResult result = new PacManRunResult(config, info);
		
        if (verbose)
            System.out.printf(
                "seed %2d: reached level %d, score = %5d\n",
                config.game.seed, info.getCurLevel(), info.getScore());

		return result;		
	}

	public SimulatorConfig getConfig() {
		return config;
	}
}

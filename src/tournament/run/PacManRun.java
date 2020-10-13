package tournament.run;

import game.PacManSimulator;
import game.SimulatorConfig;
import game.controllers.pacman.IPacManController;
import game.core.Game;

import java.io.File;

import tournament.PacManConfig;

public class PacManRun {
	
	private PacManConfig config;
	
	private File origReplayFile;

	public PacManRun(PacManConfig config) {
		this.config = config;
	}
	
	/**
	 * @param pacManFQCN
	 * @return
	 */
	public synchronized PacManRunResult run(IPacManController pacMan) {		
		SimulatorConfig simulatorConfig = config.config;
		if (simulatorConfig.replay) {
			origReplayFile = simulatorConfig.replayFile;
		}
		
		PacManRunResult result = new PacManRunResult(config);
		
		for (int i = 0; i < config.repetitions; ++i) {
			
			if (config.repetitions > 1)
				System.out.println("ITERATION " + (i+1) + " / " + config.repetitions);
			
			simulatorConfig.pacManController = pacMan;
			
			if (simulatorConfig.replay) {
				String file = origReplayFile.getName();
				int index = file.lastIndexOf(".");
				String newFile = file.substring(0, index) + "-Iter-" + i + "." + file.substring(index+1);
				simulatorConfig.replayFile = new File(origReplayFile.getParentFile(), newFile);
			}
			
			Game info = PacManSimulator.play(simulatorConfig);
			result.addResult(info);
			
            System.out.printf("seed %2d: score = %5d\n",
                simulatorConfig.game.seed, info.getScore());
		}
		return result;		
	}

	public PacManConfig getConfig() {
		return config;
	}
}

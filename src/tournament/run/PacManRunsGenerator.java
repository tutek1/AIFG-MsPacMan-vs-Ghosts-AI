package tournament.run;

import game.SimulatorConfig;
import tournament.PacManConfig;

public class PacManRunsGenerator {
	public static int[] generateSeeds(int randomSeed, int count) {
		int[] seeds = new int[count];
		
		for (int i = 0; i < count; ++i) {
			seeds[i] = randomSeed + i;
		}
		
		return seeds;
	}
	
	public static PacManConfig[] generateConfigs(int randomSeed, SimulatorConfig prototypeOptions, int runCount, int oneRunRepetitions) {
		
		int[] seeds = generateSeeds(randomSeed, runCount);
				
		PacManConfig[] configs = new PacManConfig[runCount];
		
		for (int i = 0; i < runCount; ++i) {
			SimulatorConfig config = prototypeOptions.clone();
			
			config.game.seed = seeds[i];
						
			PacManConfig result = new PacManConfig();
			
			result.config = config;
			result.repetitions = oneRunRepetitions;
			
			configs[i] = result;
		}
		
		return configs;
	}
	
	public static PacManRun[] generateRunList(int randomSeed, SimulatorConfig prototypeOptions, int runCount, int oneRunRepetitions) {
		PacManConfig[] configs = generateConfigs(randomSeed, prototypeOptions, runCount, oneRunRepetitions);
		PacManRun[] runs = new PacManRun[runCount];
		for (int i = 0; i < runCount; ++i) {
			runs[i] = new PacManRun(configs[i]);
		}
		return runs;
	}
}

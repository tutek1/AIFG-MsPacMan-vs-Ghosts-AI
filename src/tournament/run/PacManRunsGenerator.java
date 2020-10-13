package tournament.run;

import game.SimulatorConfig;

public class PacManRunsGenerator {
	public static int[] generateSeeds(int randomSeed, int count) {
		int[] seeds = new int[count];
		
		for (int i = 0; i < count; ++i) {
			seeds[i] = randomSeed + i;
		}
		
		return seeds;
	}
	
	public static SimulatorConfig[] generateConfigs(int randomSeed, SimulatorConfig prototypeOptions, int runCount, int oneRunRepetitions) {
		int[] seeds = generateSeeds(randomSeed, runCount);
				
		SimulatorConfig[] configs = new SimulatorConfig[runCount];
		
		for (int i = 0; i < runCount; ++i) {
			configs[i] = prototypeOptions.clone();
			configs[i].game.seed = seeds[i];
		}
		
		return configs;
	}
	
	public static PacManRun[] generateRunList(int randomSeed, SimulatorConfig prototypeOptions, int runCount, int oneRunRepetitions) {
		SimulatorConfig[] configs = generateConfigs(randomSeed, prototypeOptions, runCount, oneRunRepetitions);
		PacManRun[] runs = new PacManRun[runCount];
		for (int i = 0; i < runCount; ++i) {
			runs[i] = new PacManRun(configs[i]);
		}
		return runs;
	}
}

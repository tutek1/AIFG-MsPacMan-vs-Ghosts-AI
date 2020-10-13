package tournament.run;

import java.util.ArrayList;
import java.util.List;

import game.SimulatorConfig;
import tournament.EvaluationInfos;

public class PacManResults extends EvaluationInfos {
	
	private List<SimulatorConfig> configs = new ArrayList<SimulatorConfig>();
	private List<PacManRunResult> runResults = new ArrayList<PacManRunResult>();
	
	public void addRunResults(PacManRunResult... results) {
		for (PacManRunResult result : results) {
			runResults.add(result);
			configs.add(result.getConfig());
			addResults(result.getResults());
		}
	}
	
	public List<PacManRunResult> getRunResults() {
		return runResults;
	}
}

package tournament.run;

import game.SimulatorConfig;
import tournament.EvaluationInfos;

public class PacManRunResult extends EvaluationInfos {
	
	private SimulatorConfig config;	
		
	public PacManRunResult(SimulatorConfig config) {
		this.config = config;
	}
	
	public SimulatorConfig getConfig() {
		return config;
	}
	
	public String getCSVHeader() {
		return super.getCSVHeader() + ";" + config.getCSVHeader();
	}
	
	public String getCSV() {
		return super.getCSV() + ";" + config.getCSV();		
	}
	
}

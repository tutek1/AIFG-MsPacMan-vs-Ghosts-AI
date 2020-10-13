package tournament.run;

import game.SimulatorConfig;
import game.core.Game;

public class PacManRunResult {
    private SimulatorConfig config;	
    private Game info;
		
	public PacManRunResult(SimulatorConfig config, Game info) {
        this.config = config;
        this.info = info;
    }
    
    public Game getInfo() { return info; }
	
	public String getCSVHeader() {
		return config.getCSVHeader() + ";levelReached;score;timeSpent";
	}
	
	public String getCSV() {
        return config.getCSV() + ";" + info.getCurLevel() + ";" + info.getScore() +
            ";" + info.getTotalTime();
	}
	
}

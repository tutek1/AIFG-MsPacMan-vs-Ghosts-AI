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
		return "seed;levelReached;score;timeSpent";
	}
	
	public String getCSV() {
        return config.game.seed + ";" + info.getCurLevel() + ";" + info.getScore() +
            ";" + info.getTotalTime();
	}
	
}

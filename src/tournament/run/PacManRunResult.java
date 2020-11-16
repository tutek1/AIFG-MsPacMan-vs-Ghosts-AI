package tournament.run;

import game.core.Game;

public class PacManRunResult {
    private int seed;	
    private Game info;
		
	public PacManRunResult(int seed, Game info) {
        this.seed = seed;
        this.info = info;
    }
    
    public Game getInfo() { return info; }
	
	public String getCSVHeader() {
		return "seed;levelReached;score;timeSpent";
	}
	
	public String getCSV() {
        return seed + ";" + info.getCurLevel() + ";" + info.getScore() + ";" + info.getTotalTime();
	}
	
}

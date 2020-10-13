package tournament;

import game.core.Game;

import java.util.ArrayList;
import java.util.List;

public class EvaluationInfos {
	private List<Game> results = new ArrayList<Game>();
	
	public int totalScore;
	public double avgScore;
	
	public int totalTimeSpent;
	public double avgTimeSpent;
		
	public List<Game> getResults() {
		return results;
	}

	public void addResult(Game result) {
		totalScore     += result.getScore();		
		totalTimeSpent += result.getTotalTime();
		
		results.add(result);
		
		avgScore     = (double) totalScore     / results.size();		
		avgTimeSpent = (double) totalTimeSpent / results.size();
	}
	
	public void addResults(List<Game> results) {
		for (Game info : results) {
			addResult(info);
		}
	}
	
	public String getCSVHeader() {
		return "resultCount;totalScore;avgScore;totalTimeSpent;avgTimeSpent";
	}
	
	public String getCSV() {
		return results.size() 
			   + ";" + totalScore     + ";" + avgScore
			   + ";" + totalTimeSpent + ";" + avgTimeSpent;		
	}
	
	@Override
	public String toString() {
		return String.format("avg score = %.1f", avgScore);
	}
	
}

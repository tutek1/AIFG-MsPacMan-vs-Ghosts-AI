package tournament;

import java.io.*;

import tournament.run.PacManResults;
import tournament.run.PacManRun;
import tournament.run.PacManRunResult;
import tournament.run.PacManRunsGenerator;
import tournament.utils.Sanitize;
import game.SimulatorConfig;
import controllers.pacman.IPacManController;

public class EvaluateAgent {
	private int seed = 0;

	private SimulatorConfig prototypeConfig;
	
	private int runCount;
	
	private File resultDirFile;
	
	public EvaluateAgent(int seed, SimulatorConfig prototypeConfig, int runCount, File resultDirFile) {
		this.seed = seed;
		this.prototypeConfig = prototypeConfig;
		this.runCount = runCount;
		this.resultDirFile = resultDirFile;
	}
	
	public PacManResults evaluateAgent(String agentId, IPacManController agent, boolean verbose) {
		agentId = Sanitize.idify(agentId);
		
        System.out.println("Evaluating agent...");
        
		PacManRun[] runs = PacManRunsGenerator.generateRunList(seed, prototypeConfig, runCount);
		
		PacManResults results = new PacManResults();
		
		File replayDir = null;

		if (resultDirFile != null) {
            resultDirFile.mkdirs();
            if (prototypeConfig.replay) {
                replayDir = new File(resultDirFile, "replays");
                replayDir.mkdirs();
            }
		}
						
		for (int i = 0; i < runs.length; ++i) {
			if (runs[i].getConfig().replay) {
				if (replayDir != null) {
					if (runs[i].getConfig().replayFile == null) {
						runs[i].getConfig().replayFile = new File(replayDir, agentId + "-Run-" + i + ".replay");
					} else {
						String file = runs[i].getConfig().replayFile.getName();
						int index = file.lastIndexOf(".");
						String newFile = file.substring(0, index) + "-Run-" + i + "." + file.substring(index+1);
						runs[i].getConfig().replayFile = new File(runs[i].getConfig().replayFile.getParentFile(), newFile);
					}
				} else runs[i].getConfig().replay = false;
			}
			
			PacManRunResult result = runs[i].run(agent, verbose);
			
			results.addRunResults(result);
		}
		
		System.out.println(results);
		
		if (resultDirFile != null)
			outputResults(agentId, results);

		return results;
	}

	private void outputResults(String agentId, PacManResults results) {		
		resultDirFile.mkdirs();
		
		outputRuns(agentId, results);
		outputAverages(agentId, results);
	}
	
	private void outputRuns(String agentId, PacManResults results) {
		File file = new File(resultDirFile, agentId + ".runs.csv");
		System.out.println("Writing runs into " + file.getPath());
		
		try (PrintWriter writer = new PrintWriter(new FileOutputStream(file))) {
			writer.println("agentId;" + results.getRunResults().get(0).getCSVHeader());
			for (PacManRunResult run : results.getRunResults()) {
				writer.println(agentId + ";" + run.getCSV());				
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Failed to write results into " + file.getPath());
		}
	}
	
	private void outputAverages(String agentId, PacManResults results) {
		File file = new File(resultDirFile, "results.csv");		
		System.out.println("Writing averages into " + file.getPath());
		
        boolean outputHeaders = !file.exists();
		try (PrintWriter writer = new PrintWriter(new FileOutputStream(file, true))) {
			if (outputHeaders) {
				writer.println("agentId;configSeed;" + results.getCSVHeader());
			}
			writer.print(agentId + ";");
			writer.print(seed + ";");
			writer.println(results.getCSV());
		} catch (Exception e) {
			throw new RuntimeException("Failed to write results into: " + file.getPath());
		}
	}

}

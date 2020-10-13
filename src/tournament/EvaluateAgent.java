package tournament;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import tournament.run.PacManResults;
import tournament.run.PacManRun;
import tournament.run.PacManRunResult;
import tournament.run.PacManRunsGenerator;
import tournament.utils.Sanitize;
import game.SimulatorConfig;
import game.controllers.pacman.IPacManController;

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
			replayDir = new File(resultDirFile, "replays");
			replayDir.mkdirs();
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
		
		outputAgentAvgs(agentId, results);
		outputAgentGlobalAvgs(agentId, results);
	}
	
	private void outputAgentAvgs(String agentId, PacManResults results) {
		File file = new File(resultDirFile, agentId + ".runs.csv");
		System.out.println("[" + agentId + "] Outputing runs into: " + file.getAbsolutePath());
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileOutputStream(file));
			
			writer.println("agentId;runNumber;" + results.getRunResults().get(0).getCSVHeader());
			int configNumber = 0;
			for (PacManRunResult run : results.getRunResults()) {
				++configNumber;
				writer.print(agentId);
				writer.print(";" + configNumber);
				writer.println(";" + run.getCSV());				
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to write results into: " + file.getAbsolutePath());
		} finally {
			if (writer != null) writer.close();
		}
	}
	
	private void outputAgentGlobalAvgs(String agentId, PacManResults results) {
		File file = new File(resultDirFile, "results.csv");		
		System.out.println("[" + agentId + "] Outputing total avgs into: " + file.getAbsolutePath());
		
		PrintWriter writer = null;
		try {
			boolean outputHeaders = !file.exists();
			writer = new PrintWriter(new FileOutputStream(file, true));
			if (outputHeaders) {
				writer.println("agentId;configSeed;" + results.getCSVHeader());
			}
			writer.print(agentId + ";");
			writer.print(seed + ";");
			writer.println(results.getCSV());
		} catch (Exception e) {
			throw new RuntimeException("Failed to write results into: " + file.getAbsolutePath());
		} finally {
			if (writer != null) writer.close();
		}
	}

}

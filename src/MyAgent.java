import controllers.pacman.PacManControllerBase;
import game.core.Game;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;


// 269 110, lvlc 1000, reverse -20, dth -1000000, powerPillScore 700, total score change, quadratic depth mult, near death

// 272 020, lvlc 1000, reverse -20, dth -inf, powerPillScore 0, total score change, quadratic depth mult, easy no death
//		heuristic - depth, score/50, 30 fruit active, nextLvl -inf, max manhattan dist ghosts from pacman * 3, time in lair 1.5



public final class MyAgent extends PacManControllerBase
{
	private int maxReachedDepthPrint = 300;

	// Value Reward variables for evaluating a states
	private final float vLvlComplete = 1f;
	private final float vReverseDir = -15f;
	private final float vDeath = Float.NEGATIVE_INFINITY;
    private final float vPowerPillPenalty = -400;
    private final float vGhostScoreAdd = 0.f;
	private final float vDepthMult = 0.1f;

	// Heuristic Reward variables for evaluating a state
    private final float hNextEdibleMult = 0.5f;
    private final float hGhostDistMinPercent = 0.f;
	private final float hGhostDistMax = 130f;
    private final float hGhostDistReward = 200;
    private final float hDepthMult = 1f;
	private final float hFinalPillScore = 1f;
	private final float hScoreGhostMult = 0.05f;
	private final float hScoreMult = 1f;
	private final float hPowerPillActive = 20;

    // 10ms - old laptop mode
	// 5ms - i5-8600 mode
	private final int waitTime = 10;
	
	// TODO distance with level num
	// TODO two ghosts with same score -> schizo no ghost
	// TODO 16 level timeout

	class State {
		public Game gameCopy;
		public int parentDir;
		public int depth;
		public int powerPillsActiveBefore;
		public int lastScore;
		public float value;
		public float heuristicValue;
		public boolean wasPowerPillActive;

		public State(Game game, int parentDir, int depth, int powerPillsActiveBefore, int lastScore, boolean wasPowerPillActive)
		{
			this.gameCopy = game;
			this.parentDir = parentDir;
			this.depth = depth;
			this.powerPillsActiveBefore = powerPillsActiveBefore;
			this.lastScore = lastScore;
			this.wasPowerPillActive = wasPowerPillActive;
			evaluate();
			evaluateHeuristic();
		}
		
		private void evaluate()
		{
            // Dies
			if (game.getLivesRemaining() > gameCopy.getLivesRemaining())
            {
                value = vDeath;
                return;
            }
			if (game.getCurLevel() < gameCopy.getCurLevel() && wasPowerPillActive)
			{
				value = vDeath;
				return;
			}
			
            // Completes level
			if (game.getCurLevel() < gameCopy.getCurLevel() && !wasPowerPillActive)
            {
                value = vLvlComplete;
                return;
            };

            // Else calculate the value of the state (higher value is better)
			value = gameCopy.getScore() - game.getScore();

            //if (isPowerPillActive && (gameCopy.getScore() - lastScore >= 200)) value += gameCopy.getScore() - lastScore * vPowerPillScore;

            // Add active power pills to make them costly

			//if(!isPowerPillActive) value += (gameCopy.getNextEdibleGhostScore()) * vNextEdibleMult;
			//if (isPowerPillActive) value += vPowerPillActivePenalty;
			int gainedScore = gameCopy.getScore() - lastScore;
            if (gainedScore > 190 && gainedScore < 1900) value += vGhostScoreAdd;

			//value += (gameCopy.getNumberPills() - gameCopy.getNumActivePills()) * vPerPill;
			if (wasPowerPillActive) value += vPowerPillPenalty;

			value += (depth * vDepthMult);

			// if going backwards make the score lower to not overuse it
			if (game.getCurPacManDir() == gameCopy.getReverse(parentDir))
			{
                value += vReverseDir;
			}

		}

		private void evaluateHeuristic()
		{
//            float depthMult = depth / (float) maxReachedDepth;
//            depthMult = (float) Math.pow(depthMult, 2);
//            depthMult = 1 - depthMult;

            heuristicValue = -depth * hDepthMult;
			int gainedScore = gameCopy.getScore() - lastScore;
			if (gainedScore < 100)heuristicValue += (gainedScore) * hScoreMult;
			if (gainedScore > 199 && gainedScore < 1999) heuristicValue += (gainedScore) * hScoreGhostMult;

			//heuristicValue += gameCopy.getNumActivePowerPills() * hPowerPill;
            //System.out.println("depth " + -depth);

            //System.out.println("score " + (gameCopy.getScore() - game.getScore()) * hScoreMult);
            int ghost1EdibleTime = game.getEdibleTime(0);
            int ghost2EdibleTime = game.getEdibleTime(1);
            int ghost3EdibleTime = game.getEdibleTime(2);
            int ghost4EdibleTime = game.getEdibleTime(3);

            int ghost1Edible = ghost1EdibleTime > 0? 1 : 0;
            int ghost2Edible = ghost2EdibleTime > 0? 1 : 0;
            int ghost3Edible = ghost3EdibleTime > 0? 1 : 0;
            int ghost4Edible = ghost4EdibleTime > 0? 1 : 0;

            boolean isPowerPillActive = Math.max(Math.max(Math.max(
                    ghost1Edible,
                    ghost2Edible),
                    ghost3Edible),
                    ghost4Edible) > 0;

            if (gameCopy.getNumActivePowerPills() > 0 && !wasPowerPillActive)
            {
				int pacManLoc = gameCopy.getCurPacManLoc();

				double closestPill = Float.POSITIVE_INFINITY;
				int pillIdx = -1;
				for (int powerPillIdx : gameCopy.getPowerPillIndicesActive())
				{
					double ghost1Dist = gameCopy.getPathDistance(gameCopy.getCurGhostLoc(0), powerPillIdx);
					double ghost2Dist = gameCopy.getPathDistance(gameCopy.getCurGhostLoc(1), powerPillIdx);
					double ghost3Dist = gameCopy.getPathDistance(gameCopy.getCurGhostLoc(2), powerPillIdx);
					double ghost4Dist = gameCopy.getPathDistance(gameCopy.getCurGhostLoc(3), powerPillIdx);
					
					double pillDist = Math.max(ghost1Dist , Math.max(ghost2Dist, Math.max(ghost3Dist, ghost4Dist)));
					if (pillDist < closestPill)
					{
						closestPill = pillDist;
						pillIdx = powerPillIdx;
					}
				}

				//double ghost1Dist = gameCopy.getManhattanDistance(gameCopy.getCurGhostLoc(0), pillIdx);
				//double ghost2Dist = gameCopy.getManhattanDistance(gameCopy.getCurGhostLoc(1), pillIdx);
				//double ghost3Dist = gameCopy.getManhattanDistance(gameCopy.getCurGhostLoc(2), pillIdx);
				//double ghost4Dist = gameCopy.getManhattanDistance(gameCopy.getCurGhostLoc(3), pillIdx);

				//double dist = (ghost1Dist + ghost2Dist + ghost3Dist + ghost4Dist) / 4d;

				//double dist = Math.max(ghost1Dist , Math.max(ghost2Dist , Math.max(ghost3Dist , ghost4Dist)));

                float interpolator = (float) Math.clamp((closestPill) / hGhostDistMax, hGhostDistMinPercent, 1f);
                interpolator = 1 - (interpolator * interpolator);// * (1f - interpolator) * (1f - interpolator);

                heuristicValue += (float) ((interpolator * hGhostDistReward));
            }
			else if (wasPowerPillActive)
			{
				heuristicValue += hPowerPillActive;

//				int gainedScore = gameCopy.getScore() - lastScore;
//				if (gainedScore == 200 || gainedScore == 210 ||
//						gainedScore == 400 || gainedScore == 410 ||
//						gainedScore == 800 || gainedScore == 810 ||
//						gainedScore == 1600 || gainedScore == 1610 )

			}
			else
			{
				if (gainedScore < 100) heuristicValue += (gainedScore) * hFinalPillScore;
				
			}
			

		}
	}

	// Debug
	long ticks = 0;
	
	@Override
	public void tick(Game game, long timeDue) {

		// Debug
		ticks += 1;
		if (ticks % 12 == 0)
		{
			double ghost1Dist = game.getPathDistance(game.getCurGhostLoc(0), game.getCurPacManLoc());
			double ghost2Dist = game.getPathDistance(game.getCurGhostLoc(1), game.getCurPacManLoc());
			double ghost3Dist = game.getPathDistance(game.getCurGhostLoc(2), game.getCurPacManLoc());
			double ghost4Dist = game.getPathDistance(game.getCurGhostLoc(3), game.getCurPacManLoc());

			double maxDist = Math.max(Math.max(Math.max(ghost1Dist, ghost2Dist), ghost3Dist), ghost4Dist);

			int ghost1EdibleTime = game.getEdibleTime(0);
			int ghost2EdibleTime = game.getEdibleTime(1);
			int ghost3EdibleTime = game.getEdibleTime(2);
			int ghost4EdibleTime = game.getEdibleTime(3);
			
			int ghost1Edible = ghost1EdibleTime > 0? 1 : 0;
			int ghost2Edible = ghost2EdibleTime > 0? 1 : 0;
			int ghost3Edible = ghost3EdibleTime > 0? 1 : 0;
			int ghost4Edible = ghost4EdibleTime > 0? 1 : 0;

			boolean isPowerPillActive = Math.max(Math.max(Math.max(
									ghost1Edible,
									ghost2Edible),
							ghost3Edible),
					ghost4Edible) > 0;
			
			String state = "None";
			if (game.getNumActivePowerPills() > 0 && !isPowerPillActive)
			{
				state = "Power pills exist, not eaten";
			}
			else if (isPowerPillActive)
			{
				state = "Power pill eaten";
			}
			else
			{
				state = "No power pills only pills";
			}
			
			System.out.println("Score: " + game.getScore() +
							   ", lvl: " + game.getCurLevel() +
							   ", lives: " + game.getLivesRemaining() +
							   ", max depth reached (since last print): " + maxReachedDepthPrint +
							   ", next edible score: " + game.getNextEdibleGhostScore() +
							   ", fruit pos: " + game.getFruitLoc() +
							   ", max dist from ghost: " + maxDist +
							   ", state: " + state
							   );
			maxReachedDepthPrint = 1;
		}


		int[] directions = game.getPossiblePacManDirs(true);
		if (directions.length == 1)
		{
			pacman.set(directions[0]);
			// TODO try precalculation of the next junction
			return;
		}
		float[] dirScores = new float[4];
		float[] dirNumExplored = new float[4];

		// Create a queue and put the initial states in
		Queue<State> statesToVisit = //new LinkedList<>();
		new PriorityQueue<>(new Comparator<State>() {
			@Override
			public int compare(State o1, State o2) {
				return Float.compare(o2.heuristicValue, o1.heuristicValue);
			}
		});
		for (int dir : directions)
		{
			Game gameCopy = game.copy();
			gameCopy.advanceGame(dir);
			State newState = new State(gameCopy, dir, 1, gameCopy.getNumActivePowerPills(), 0, false);
			if (newState.value <= vDeath) continue;
			statesToVisit.add(newState);
		}

		// While we have time, explore the decision tree
		int numStatesVisited = 0;
		float bestScore = -100000000;
		while (System.currentTimeMillis() < timeDue - waitTime) {
			// Get current state
			State currState = statesToVisit.poll();
			if (currState == null) break;

			numStatesVisited += 1;

			if (currState.value > bestScore)
			{
				bestScore = currState.value;
				pacman.set(currState.parentDir);
			}
			
			// Skip states where we are going in a straight line
			boolean skipDirection = false;
			while (currState.gameCopy.getPossiblePacManDirs(false).length == 1)
			{
				int lastNumPowerPillsActive = currState.gameCopy.getNumActivePowerPills();
				int lastLives = currState.gameCopy.getLivesRemaining();
				currState.lastScore = currState.gameCopy.getScore();
				currState.gameCopy.advanceGame(currState.gameCopy.getPossiblePacManDirs(false)[0]);
				currState.depth += 1;
				currState.powerPillsActiveBefore = lastNumPowerPillsActive;
				if (lastLives > currState.gameCopy.getLivesRemaining())
				{
					skipDirection = true;
					break;
				}
			}
			if (skipDirection) continue;

			if (System.currentTimeMillis() >= timeDue - waitTime) break;
			
			maxReachedDepthPrint = Math.max(maxReachedDepthPrint, currState.depth);

			// Add all possible next states
			for (int dir : currState.gameCopy.getPossiblePacManDirs(true))
			{
				Game gameCopy = currState.gameCopy.copy();

				int lastScore = gameCopy.getScore();

				boolean wasPowerPillActive = gameCopy.isEdible(0) ||
											 gameCopy.isEdible(1) ||
											 gameCopy.isEdible(2) ||
											 gameCopy.isEdible(3);

				gameCopy.advanceGame(dir);

				State newState = new State(gameCopy,currState.parentDir,
						currState.depth + 1,
						currState.gameCopy.getNumActivePowerPills(),
						lastScore,
						wasPowerPillActive);
				if (currState.value <= vDeath) continue;

				statesToVisit.add(newState);
			}
		}
	}
}

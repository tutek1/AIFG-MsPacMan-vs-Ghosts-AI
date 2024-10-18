import controllers.pacman.PacManControllerBase;
import game.core.Game;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;


// 275 810, lvlc 100, reverse -20, dth -1000000, powerPillScore 700, total score change, quadratic depth mult
// 269 110, lvlc 1000, reverse -20, dth -1000000, powerPillScore 700, total score change, quadratic depth mult, near death
// 269 110, lvlc 5000, reverse -20, dth -1000000, powerPillScore 700, total score change, quadratic depth mult
// 227 620, lvlc 1000, reverse -20, dth -1000000, powerPillScore 0, total score change, quadratic depth mult, easy no death

// 220 110, lvlc 1000, reverse -20, dth -inf, powerPillScore 0, total score change, quadratic depth mult, easy no death
//		heuristic - depth only

// 244 620, lvlc 1000, reverse -20, dth -inf, powerPillScore 0, total score change, quadratic depth mult, easy no death
//		heuristic - depth + score/10

// 251 720, lvlc 1000, reverse -20, dth -inf, powerPillScore 0, total score change, quadratic depth mult, easy no death
//		heuristic - depth, score/50, 20 fruit active, nextLvl -inf, max manhattan dist ghosts from pacman * 1

// 259 520, lvlc 1000, reverse -20, dth -inf, powerPillScore 0, total score change, quadratic depth mult, easy no death
//		heuristic - depth, score/50, 20 fruit active, nextLvl -inf, max manhattan dist ghosts from pacman * 3

// 259 ???, lvlc 1000, reverse -20, dth -inf, powerPillScore 0, total score change, quadratic depth mult, lost 2 lives
//		heuristic - depth, score/50, 30 fruit active, nextLvl -inf, max manhattan dist ghosts from pacman * 3, time in lair 0.5

// 272 020, lvlc 1000, reverse -20, dth -inf, powerPillScore 0, total score change, quadratic depth mult, easy no death
//		heuristic - depth, score/50, 30 fruit active, nextLvl -inf, max manhattan dist ghosts from pacman * 3, time in lair 1.5

// 269 220, lvlc 1000, reverse -20, dth -inf, powerPillScore 0, total score change, quadratic depth mult, easy no death
//		heuristic - depth, score/20, 40 fruit active, nextLvl -inf, max manhattan dist ghosts from pacman * 1, time in lair 3

// .., lvlc 1000, reverse -20, dth -inf, powerPillScore 0, total score change, quadratic depth mult, easy no death
//		heuristic - depth, score/10, 40 fruit active, nextLvl -inf, max manhattan dist ghosts from pacman * 1, time in lair 2



public final class MyAgent extends PacManControllerBase
{
	private int maxReachedDepth = 1;
	private int maxReachedDepthPrint = 1;

	// Value Reward variables for evaluating a states
	private final float rLvlComplete = 1000f;
	private final float rReverseDir = -20f;
	private final float rDeath = Float.NEGATIVE_INFINITY;
	private final float rPerPowerPill = 0;
	//private final float rPowerAndFruit = -1000;
	//private final float rNotAllEaten = -10000;
	//private final float rFruitMult = 1.5f;

	// Heuristic Cost variables for evaluating a state
	private final float cPerPowerPill = 700;
	private final float cPerPill = 0;
	private final float cScore = 0.5f;
	private final float cNextLevel = -1f;//Float.NEGATIVE_INFINITY;
	private final float cGhostDistFromPowerPill = 400;
	private final float cGhostDistCutoff = 0.25f;
	private final float cGhostMaxDist = 60f;
	private final float cFruitActive = 70f;
	private final float cNotAllGhostsEatenState = 0.4f;
	// 10ms - old laptop mode
	// 5ms - i5-8600 mode
	private final int waitTime = 10;
	
	

	class State {
		public Game gameCopy;
		public int parentDir;
		public int depth;
		public int powerPillsActiveBefore;
		public int lastScore;
		public float value;
		public float heuristicValue;

		public State(Game game, int parentDir, int depth, int powerPillsActiveBefore, int lastScore)
		{
			this.gameCopy = game;
			this.parentDir = parentDir;
			this.depth = depth;
			this.powerPillsActiveBefore = powerPillsActiveBefore;
			this.lastScore = lastScore;
			this.value = evaluate();
			this.heuristicValue = evaluateHeuristic();
		}
		
		private float evaluate()
		{
			if (game.getLivesRemaining() > gameCopy.getLivesRemaining()) return rDeath;
			if (game.getCurLevel() < gameCopy.getCurLevel()) return rLvlComplete;

			float score = 0;
			score = gameCopy.getScore() - game.getScore();
			score += gameCopy.getNumActivePowerPills() * rPerPowerPill;
			// if fruit exists, target it
			//if (gameCopy.getFruitLoc() != -1)
			//{
			//	boolean willPickupFruit = (gameCopy.getManhattanDistance(gameCopy.getCurPacManLoc(), gameCopy.getFruitLoc())) < 10;

				//if (willPickupFruit) System.out.println("Fruity");
				//			int fruitScore = state.game.getFruitValue()*2 - state.game.getPathDistance(state.game.getCurPacManLoc(), state.game.getFruitLoc());
//			fruitScore = Math.max(fruitScore, 0);
//			fruitScore *= 10;
//			System.out.println(fruitScore + " fruit");
//			GameView.addPoints(game, );
//			score += fruitScore;
			//}

			boolean isGhost1Edible = gameCopy.isEdible(0);
			boolean isGhost2Edible = gameCopy.isEdible(1);
			boolean isGhost3Edible = gameCopy.isEdible(2);
			boolean isGhost4Edible = gameCopy.isEdible(3);

			// Power pill is active
			boolean isPowerPillActive = (isGhost1Edible ||
					isGhost2Edible ||
					isGhost3Edible ||
					isGhost4Edible);
			//boolean pickedUpPowerPill = gameCopy.getNumActivePowerPills() < powerPillsActiveBefore;

			//int ghostsInLair =  (gameCopy.getLairTime(0) > 0 ? 1 : 0) +
			//		(gameCopy.getLairTime(1) > 0 ? 1 : 0) +
			//		(gameCopy.getLairTime(2) > 0 ? 1 : 0) +
			//		(gameCopy.getLairTime(3) > 0 ? 1 : 0);


			//if (isPowerPillActive) score += (gameCopy.getNextEdibleGhostScore() - 400) * 10;
			//if (gameCopy.getNextEdibleGhostScore() != 3200) score += -3000;//rNotAllEaten;
			//if (isPowerPillActive && state.game.getFruitLoc() != -1) score += rPowerAndFruit;

			
			// if going backwards make the score lower to not overuse it
			if (game.getCurPacManDir() == gameCopy.getReverse(parentDir))
			{
				score += rReverseDir;
			}
			
			float depthMult = depth / (float) maxReachedDepth;
			depthMult = (float) Math.pow(depthMult, 2);
			depthMult = 1 - depthMult;
			if (score < 0) score -= Math.abs(score) - Math.abs(score) * depthMult;
			else score *= depthMult;

			//score /= gameCopy.getLevelTime();
			//System.out.println("depth: " + depth + ", max depth: " + maxReachedDepth + ", depth mult: " + depthMult);

			//System.out.println(score);
			return score;
		}

		private float evaluateHeuristic()
		{
			if (gameCopy.getCurLevel() != game.getCurLevel()) return cNextLevel;
			float heuristic = depth;
			heuristic += -(gameCopy.getScore() - game.getScore()) * cScore;

			boolean isGhost1Edible = gameCopy.isEdible(0);
			boolean isGhost2Edible = gameCopy.isEdible(1);
			boolean isGhost3Edible = gameCopy.isEdible(2);
			boolean isGhost4Edible = gameCopy.isEdible(3);

			// Power pill is active
			boolean isPowerPillActive =
					(isGhost1Edible ||
					isGhost2Edible ||
					isGhost3Edible ||
					isGhost4Edible);

			// Power pill and ghost eating handleing
			if (gameCopy.getNumActivePowerPills() > 0 || isPowerPillActive)
			{
				int pacManLoc = gameCopy.getCurPacManLoc();

				int ghost1InLairTime = gameCopy.getLairTime(0);
				int ghost2InLairTime = gameCopy.getLairTime(1);
				int ghost3InLairTime = gameCopy.getLairTime(2);
				int ghost4InLairTime = gameCopy.getLairTime(3);
				int numGhostsInLair = (ghost1InLairTime > 0 ? 1 : 0) +
									  (ghost2InLairTime > 0 ? 1 : 0) +
									  (ghost3InLairTime > 0 ? 1 : 0) +
									  (ghost4InLairTime > 0 ? 1 : 0);

				double ghost1Dist = gameCopy.getPathDistance(gameCopy.getCurGhostLoc(0), pacManLoc);
				//ghost1Dist -= gameCopy.getEdibleTime(0) * 0.5f;
				//if (gameCopy.getLevelTime() > 200 && ghost1InLairTime > 0) ghost1Dist = 0;
				double ghost2Dist = gameCopy.getPathDistance(gameCopy.getCurGhostLoc(1), pacManLoc);
				//ghost2Dist -= gameCopy.getEdibleTime(1) * 0.5f;
				//if (gameCopy.getLevelTime() > 200 && ghost2InLairTime > 0) ghost2Dist = 0;
				double ghost3Dist = gameCopy.getPathDistance(gameCopy.getCurGhostLoc(2), pacManLoc);
				//ghost3Dist -= gameCopy.getEdibleTime(2) * 0.5f;
				//if (gameCopy.getLevelTime() > 200 && ghost3InLairTime > 0) ghost3Dist = 0;
				double ghost4Dist = gameCopy.getPathDistance(gameCopy.getCurGhostLoc(3), pacManLoc);
				//ghost4Dist -= gameCopy.getEdibleTime(3) * 0.5f;
				//if (gameCopy.getLevelTime() > 200 && ghost4InLairTime > 0) ghost4Dist = 0;

				double avgDist = (ghost1Dist + ghost2Dist + ghost3Dist + ghost4Dist) / 4d;

				float interpolator = (float) Math.clamp(avgDist/cGhostMaxDist, cGhostDistCutoff, 1f);
				interpolator = 1f- (1f-interpolator)*(1f-interpolator);
				heuristic += interpolator * cGhostDistFromPowerPill;
				//System.out.println((float) (maxDist*cGhostDistFromPowerPillMult));

//				float closestPowerPillDist = Float.POSITIVE_INFINITY;
//				for (int powerPillIdx : gameCopy.getPowerPillIndices())
//				{
//					float dist = gameCopy.getPathDistance(pacManLoc, powerPillIdx);
//					if (dist < closestPowerPillDist)
//					{
//						closestPowerPillDist = dist;
//					}
//				}
//
//				heuristic += closestPowerPillDist * cPowerPillDist;
				heuristic += (3200 - gameCopy.getNextEdibleGhostScore()) * cNotAllGhostsEatenState;

				//heuristic += -(ghost1InLairTime + ghost2InLairTime + ghost3InLairTime + ghost4InLairTime) * cGhostTimeInLairMult;
			}

			// Else go for pills
			else if (game.getNumActivePowerPills() == 0 && !isPowerPillActive)
			{
				heuristic += gameCopy.getNumActivePills() * cPerPill;
			}

			if (gameCopy.getFruitLoc() != -1)
			{
				heuristic += cFruitActive;
			}

			return heuristic;
		}
	}

	// Debug
	long ticks = 0;
	
	@Override
	public void tick(Game game, long timeDue) {

		// Debug
		ticks += 1;
		if (ticks % 10 == 0)
		{
			double ghost1Dist = game.getPathDistance(game.getCurGhostLoc(0), game.getCurPacManLoc());
			double ghost2Dist = game.getPathDistance(game.getCurGhostLoc(1), game.getCurPacManLoc());
			double ghost3Dist = game.getPathDistance(game.getCurGhostLoc(2), game.getCurPacManLoc());
			double ghost4Dist = game.getPathDistance(game.getCurGhostLoc(3), game.getCurPacManLoc());

			double maxDist = Math.max(Math.max(Math.max(ghost1Dist, ghost2Dist), ghost3Dist), ghost4Dist);

			System.out.println("Score: " + game.getScore() +
							   ", lvl: " + game.getCurLevel() +
							   ", lives: " + game.getLivesRemaining() +
							   ", max depth reached (since last print): " + maxReachedDepthPrint +
							   ", next edible score: " + game.getNextEdibleGhostScore() +
							   ", fruit pos: " + game.getFruitLoc() +
							   ", lvl Time: " + game.getLevelTime() +
							   ", max dist from ghost: " + maxDist);
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
				return Float.compare(o1.heuristicValue, o2.heuristicValue);
			}
		});
		for (int dir : directions)
		{
			Game gameCopy = game.copy();
			gameCopy.advanceGame(dir);
			State newState = new State(gameCopy, dir, 1, gameCopy.getNumActivePowerPills(), 0);
			if (newState.value <= rDeath) continue;
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

			// Evaluate current state using a heuristic function
			
			//dirNumExplored[currState.parentDir] += 1;
			//dirScores[currState.parentDir] += stateScore;//Math.max(dirScores[currState.parentDir], stateScore);

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

			if (currState.heuristicValue == cNextLevel) continue;

			maxReachedDepth = Math.max(maxReachedDepth, currState.depth);
			maxReachedDepthPrint = Math.max(maxReachedDepthPrint, currState.depth);

			// Add all possible next states
			for (int dir : currState.gameCopy.getPossiblePacManDirs(false))
			{
				Game gameCopy = currState.gameCopy.copy();
				int lastScore = gameCopy.getScore();
				gameCopy.advanceGame(dir);
				State newState = new State(gameCopy,currState.parentDir,
						currState.depth + 1,
						currState.gameCopy.getNumActivePowerPills(),
						lastScore);
				if (currState.value <= rDeath) continue;

				statesToVisit.add(newState);
			}
		}

//		float bestScored = -100000000;
//		for (int i = 0; i < 4; i++)
//		{
//			float score = dirScores[i] / dirNumExplored[i];
//			if (score > bestScored)
//			{
//				bestScored = score;
//				pacman.set(i);
//			}
//		}
		//System.out.println("max depth " + maxReachedDepth);
		//System.out.println(numStatesVisited + " visited"); // avg 5800

		
		//float currScore = dirScores[currDir];// / dirNumExplored[currDir]; // TODO try
	}

}

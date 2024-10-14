import controllers.pacman.PacManControllerBase;
import game.core.Game;

import java.util.LinkedList;
import java.util.Queue;

// Score with timeDue - 5
// 200 020, rdebuff 0.35, best score = max from tree, score with depth^2 lowering, reverse
// 213 386, rdebuff 0.45, best score = max from tree, score with depth^2 lowering, reverse
// 221 640, rdebuff 0.55, best score = max from tree, score with depth^2 lowering, reverse
// 212 120, rdebuff 0.55, best score = max from tree, score with no depth, reverse
// 197 920, rdebuff 0.55, best score = max from tree, score with depth^2 lowering, no reverse
// 218 600, rdebuff 0.55, best score = max from tree, score with depth^3 lowering, reverse
// ., rdebuff 0.5, best score = max from tree, score with depth^2 lowering, reverse
//
// Score with timeDue - 15
// 223 620, rdebuff 0.5, best score = max from tree, score with depth^2 lowering
// 215 820, rdebuff 0.65, best score = max from tree, score with depth^2 lowering

public final class MyAgent extends PacManControllerBase
{
	private int maxReachedDepth = 100;
	
	// Reward variables for different properties of states
	private final float rReverseDir = 0.75f;
	private final float rLvlComplete = 10000;
	private final float rDeath = -10000;
	private final float rTwoPowerPills = -1000;
	private final float rAllGhostsEatenMult = 2.f;
	

	class State {
		public Game game;
		public int parentDir;
		public int depth;
		public int powerPillsActiveBefore;

		public State(Game game, int parentDir, int depth, int powerPillsActiveBefore)
		{
			this.game = game;
			this.parentDir = parentDir;
			this.depth = depth;
			this.powerPillsActiveBefore = powerPillsActiveBefore;
		}
	}

	// Debug
	long ticks = 0;
	
	@Override
	public void tick(Game game, long timeDue) {
		
		// Debug
		ticks += 1;
		if (ticks % 25 == 0)
		{
			System.out.println("Score: " + game.getScore() + ", lvl: " + game.getCurLevel() + ", lives: " + game.getLivesRemaining() + ", max depth reached: " + maxReachedDepth + ", next edible score: " + game.getNextEdibleGhostScore());
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
		Queue<State> statesToVisit = new LinkedList<>();
		for (int dir : directions)
		{
			Game gameCopy = game.copy();
			gameCopy.advanceGame(dir);
			statesToVisit.add(new State(gameCopy, dir, 1, gameCopy.getNumActivePowerPills()));
		}

		// While we have time, explore the decision tree
		int numStatesVisited = 0;
		float bestScore = -100000000;
		while (System.currentTimeMillis() < timeDue - 5) {
			// Get current state
			State currState = statesToVisit.poll();
			if (currState == null) break;

			// Evaluate current state using a heuristic function
			float stateScore = evaluateState(game, currState);
			if (stateScore == rDeath) continue;

			// if going backwards make the score lower to not overuse it
			if (game.getCurPacManDir() == currState.game.getReverse(currState.parentDir))
			{
				stateScore *= rReverseDir;
			}

			dirNumExplored[currState.parentDir] += 1;
			dirScores[currState.parentDir] += stateScore;//Math.max(dirScores[currState.parentDir], stateScore);

			if (stateScore > bestScore)
			{
				bestScore = stateScore;
				pacman.set(currState.parentDir);
			}
			
			// Skip states where we are going in a straight line
			while (currState.game.getPossiblePacManDirs(false).length == 1)
			{
				int lastNumPowerPillsActive = currState.game.getNumActivePowerPills();
				currState.game.advanceGame(currState.game.getPossiblePacManDirs(false)[0]);
				currState = new State(currState.game,
									  currState.parentDir,
								currState.depth+1,
									  lastNumPowerPillsActive);
			}

			// Add all possible next states
			for (int dir : currState.game.getPossiblePacManDirs(false))
			{
				Game gameCopy = currState.game.copy();
				gameCopy.advanceGame(dir);
				statesToVisit.add(new State(gameCopy,
											currState.parentDir,
										currState.depth + 1,
											currState.game.getNumActivePowerPills()));
			}
			numStatesVisited += 1;
			maxReachedDepth = Math.max(maxReachedDepth, currState.depth);
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

	private float evaluateState(Game game, State state)
	{
		if (game.getCurLevel() < state.game.getCurLevel()) return rLvlComplete;
		if (game.getLivesRemaining() > state.game.getLivesRemaining()) return rDeath;
		float score = 0;
		score = (state.game.getScore() - game.getScore()) / 10f;	// lower the order so that smallest score change is 1 (single pill)
		//todo remove delta use state - game
		
		// if fruit exists, target it
//		if (state.game.getFruitLoc() != -1) {
//			int fruitScore = state.game.getFruitValue()*2 - state.game.getPathDistance(state.game.getCurPacManLoc(), state.game.getFruitLoc());
//			fruitScore = Math.max(fruitScore, 0);
//			fruitScore *= 10;
//			System.out.println(fruitScore + " fruit");
//			GameView.addPoints(game, );
//			score += fruitScore;
//		}

		boolean isGhost1Edible = state.game.getEdibleTime(0) > 0;
		boolean isGhost2Edible = state.game.getEdibleTime(1) > 0;
		boolean isGhost3Edible = state.game.getEdibleTime(2) > 0;
		boolean isGhost4Edible = state.game.getEdibleTime(3) > 0;

		// Power pill is active
		boolean isPowerPillActive = (isGhost1Edible ||
									 isGhost2Edible ||
									 isGhost3Edible ||
									 isGhost4Edible);
		boolean pickedUpPowerPill = state.game.getNumActivePowerPills() < state.powerPillsActiveBefore;
//
		// Picked up power pill while ghosts are eaten // todo maybd time to eat again
//		if (isPowerPillActive && pickedUpPowerPill)
//		{
//			score += rTwoPowerPills;
//		}

		if (pickedUpPowerPill)
		{
			score += -(20 + 40 + 80 + 160);
		}

//		int ghostsInLair =  (state.game.getLairTime(0) > 0 ? 1 : 0) +
//				(state.game.getLairTime(1) > 0 ? 1 : 0) +
//				(state.game.getLairTime(2) > 0 ? 1 : 0) +
//				(state.game.getLairTime(3) > 0 ? 1 : 0);

//		if (pickedUpPowerPill)
//		{
//			int pacLoc = state.game.getCurPacManLoc();
//			score += ((state.game.getManhattanDistance(pacLoc, state.game.getCurGhostLoc(0))) +
//					 (state.game.getManhattanDistance(pacLoc, state.game.getCurGhostLoc(1))) +
//					 (state.game.getManhattanDistance(pacLoc, state.game.getCurGhostLoc(2))) +
//					 (state.game.getManhattanDistance(pacLoc, state.game.getCurGhostLoc(3)))) * -1000;
//		}

//		float ghostScore = state.game.getNextEdibleGhostScore();
//		if (isPowerPillActive)
//		{
//			score += ghostScore * rAllGhostsEatenMult;
//
//		}
//		else
//		{
//
//		}

		//System.out.println(ghostsInLair + " ghosts in lair");
		

//		float depthMult = state.depth/(float)maxReachedDepth;
//		depthMult *= depthMult;
//		depthMult = 1 - depthMult;
//		score *= depthMult;

		//System.out.println(score);
		return score;

	}

}

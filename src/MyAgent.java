import controllers.pacman.PacManControllerBase;
import game.core.Game;

import java.util.LinkedList;
import java.util.Queue;


// 223 620 record
public final class MyAgent extends PacManControllerBase
{
	private int maxReachedDepth = 100;
	
	private float reverseDebuff = 0.5f;
	private float rLvlComplete = 100000;
	private float rDeath = -100000;
	private float rTwoPowerPills = -10000;
	private float rLowerPillCount = 10;
	
	class State {
		public Game game;
		public int parentDir;
		public int depth;
		public int scoreDelta;
		public int powerPillsActiveBefore;

		public State(Game game, int parentDir, int depth, int scoreDelta, int powerPillsActiveBefore)
		{
			this.game = game;
			this.parentDir = parentDir;
			this.depth = depth;
			this.scoreDelta = scoreDelta;
			this.powerPillsActiveBefore = powerPillsActiveBefore;
		}
	}

	@Override
	public void tick(Game game, long timeDue) {
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
			statesToVisit.add(new State(gameCopy, dir, 1, gameCopy.getScore() - game.getScore(), gameCopy.getNumActivePowerPills()));
		}

		// While we have time, explore the decision tree
		int numStatesVisited = 0;
		float bestScore = -100000000;
		while (System.currentTimeMillis() < timeDue - 15) { // TODO try lower
			// Get current state
			State currState = statesToVisit.poll();
			if (currState == null) break;

			// Evaluate current state using a heuristic function
			float stateScore = evaluateState(currState);
			dirScores[currState.parentDir] = Math.max(dirScores[currState.parentDir], stateScore);

			// if going backwards make the score lower to not overuse it
			if (Math.abs(currState.parentDir - game.getCurPacManDir()) == 2)
			{
				stateScore *= reverseDebuff;
			}
			
			if (stateScore > bestScore)
			{
				bestScore = stateScore;
				pacman.set(currState.parentDir);
			}
			
			//dirNumExplored[currState.parentDir] += 1;

			if (stateScore < 0) continue;

			// Skip states where we are going in a straight line
			while (currState.game.getPossiblePacManDirs(false).length == 1)
			{
				int lastScore = currState.game.getScore();
				int lastNumPowerPillsActive = currState.game.getNumActivePowerPills();
				currState.game.advanceGame(currState.game.getPossiblePacManDirs(false)[0]);
				currState = new State(currState.game,
									  currState.parentDir,
								currState.depth+1,
							currState.scoreDelta + currState.game.getScore() - lastScore,
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
									currState.scoreDelta + gameCopy.getScore() - currState.game.getScore(),
											currState.game.getNumActivePowerPills()));
			}
			numStatesVisited += 1;
			maxReachedDepth = Math.max(maxReachedDepth, currState.depth);
		}
		System.out.println("max depth " + maxReachedDepth);
		//System.out.println(numStatesVisited + " visited"); // avg 5800

		
		//float currScore = dirScores[currDir];// / dirNumExplored[currDir]; // TODO try
	}

	private float evaluateState(State state)
	{
		if (game.getCurLevel() < state.game.getCurLevel()) return rLvlComplete;
		if (game.getLivesRemaining() > state.game.getLivesRemaining()) return rDeath;
		float score = 0;
		score = state.scoreDelta;

		//		if (state.scoreDelta == 10)
//		{
//			score += 10;
//		}
		// if fruit exists, target it
//		if (state.game.getFruitLoc() != -1) {
//			int fruitScore = state.game.getFruitValue()*2 - state.game.getPathDistance(state.game.getCurPacManLoc(), state.game.getFruitLoc());
//			fruitScore = Math.max(fruitScore, 0);
//			fruitScore *= 10;
//			System.out.println(fruitScore + " fruit");
//			GameView.addPoints(game, );
//			score += fruitScore;
//		}

		// Don't pickup more powerpills
		if (state.game.getEdibleTime(0) > 0 &&
			state.game.getEdibleTime(1) > 0 &&
			state.game.getEdibleTime(2) > 0 &&
			state.game.getEdibleTime(3) > 0)
		{
			if (state.game.getNumActivePowerPills() < state.powerPillsActiveBefore)
			{
				score += rTwoPowerPills;
			}
		}
		else
		{
			//score += rLowerPillCount * (1 - (state.game.getNumActivePills() / ((float)state.game.getNumberPills())));
		}


		float depthMult = state.depth/(float)maxReachedDepth;
		depthMult *= depthMult;
		depthMult = 1 - depthMult;
		score *= depthMult;

		return score;

	}

}

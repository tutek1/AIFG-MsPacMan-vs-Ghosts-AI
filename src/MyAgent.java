import controllers.pacman.PacManControllerBase;
import game.core.Game;
import game.core.GameView;
import tournament.EvaluateAgent;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;

public final class MyAgent extends PacManControllerBase
{
	private int maxDepth = 200;

	class State {
		public Game game;
		public int parentDir;
		public int depth;
		public int scoreDelta;

		public State(Game game, int parentDir, int depth, int scoreDelta)
		{
			this.game = game;
			this.parentDir = parentDir;
			this.depth = depth;
			this.scoreDelta = scoreDelta;
		}
	}

	@Override
	public void tick(Game game, long timeDue) {
		int[] directions = game.getPossiblePacManDirs(true); // TODO make backwards with penalty
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
			statesToVisit.add(new State(gameCopy, dir, 1, gameCopy.getScore() - game.getScore()));
		}

		// While we have time, explore the decision tree
		int numStatesVisited = 0;
		while (System.currentTimeMillis() < timeDue - 15) { // TODO try lower
			// Get current state
			State currState = statesToVisit.poll();
			if (currState == null) break;

			// Evaluate current state using a heuristic function
			float stateScore = evaluateState(currState);
			dirScores[currState.parentDir] = Math.max(dirScores[currState.parentDir], stateScore);
			//dirNumExplored[currState.parentDir] += 1;

			if (stateScore < 0) continue;

			// Skip states where we are going in a straight line
			while (currState.game.getPossiblePacManDirs(false).length == 1)
			{
				int lastScore = currState.game.getScore();
				currState.game.advanceGame(currState.game.getPossiblePacManDirs(false)[0]);
				currState = new State(currState.game, currState.parentDir, currState.depth+1, currState.game.getScore() - lastScore);
			}

			// Add all possible next states
			for (int dir : currState.game.getPossiblePacManDirs(false))
			{
				Game gameCopy = currState.game.copy();
				gameCopy.advanceGame(dir);
				statesToVisit.add(new State(gameCopy, currState.parentDir, currState.depth + 1, gameCopy.getScore() - currState.game.getScore()));
			}
			numStatesVisited += 1;
			maxDepth = Math.max(maxDepth, currState.depth);
		}
		//System.out.println("max depth " + maxDepth);
		//System.out.println(numStatesVisited + " visited"); // avg 5800

		float bestScore = -10000000;
		int bestDir = directions[0];
		for (int currDir : directions)
		{

			float currScore = dirScores[currDir];// / dirNumExplored[currDir]; // TODO try
			if (currScore >= bestScore)
			{
				bestScore = currScore;
				bestDir = currDir;
			}
		}

		//System.out.println(bestScore + " best score");
		pacman.set(bestDir);
	}

	private float evaluateState(State state)
	{
		if (game.getLivesRemaining() > state.game.getLivesRemaining()) return -100000;
		if (game.getCurLevel() != state.game.getCurLevel()) return 100000;

		float score = 0;
		score = state.scoreDelta * 10;

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
			if (state.game.getNumActivePowerPills() + 1 < game.getNumActivePowerPills())
			{
				score -= 10000;
			}
		}
		else
		{
			score += (state.game.getNumberPills() - state.game.getNumActivePills()) * 2;
		}


		//System.out.println("score " + score);
		score *= Math.max(1,(float) state.depth/maxDepth);

		return score;

	}

}

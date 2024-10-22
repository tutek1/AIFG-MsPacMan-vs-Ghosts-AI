import controllers.pacman.PacManControllerBase;
import game.core.Game;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

// Made by Bc. Ondřej Kyzr               


// 	A score maximizing A* PacMan AI. Due to the game rewarding the player for advancing (pills) it
// also accomplishes the goal of beating all 16 levels. It can sometimes get confused in some situations
// and rapidly twitch in place.

// 	My AI values states with more score. The heuristic function prefers states with ghosts closer to a power pill
// and states with more score gain. This makes it hunt the ghosts and during the hunt it takes into account normal
// pills and fruits (+ pretzel). It takes into account moving in reverse, but skips straight paths to make search
// much more effective.
// (depth no heuristic =~25 -> +straight path skip =~170 -> +heuristic =~500).

// 	Even after all the efforts this version of the AI can do score around 220 000, which is not bad, but during
// testing I was somehow able to make an AI that had avg score around 260 000, which I lost due to poor use of git.

// --------Bugs--------
// - In later levels it wastes some power pills. Either randomly or because a fruit appeared.
// - In rare cases it can timeout on a level if ghost don't align close to a pill in time.
// - In other rare cases life can be lost from touching ghosts (very rare should not lead to game over)


public final class MyAgent extends PacManControllerBase
{

	// Value Reward variables for evaluating a states
	private final float vReverseDir = -15f;				  // Constant penalty for reverse initial direction
	private final float vDeath = Float.NEGATIVE_INFINITY; // A set value to know if death happened
    private final float vPowerPillPenalty = -1700;		  // Penalty for an active power pill (incentives better powerpill usage)
	private final float vDepthMult = 0.001f;			  // A small depth penalty to make pacman more 'focused'

	// Heuristic Reward variables for evaluating a state
	private final float hGhostDistMax = 150f;			  // Max distance a ghost can be away
    private final float hGhostDistReward = 200;			  // The maximum reward given if ghosts are as close as possible
    private final float hDepthMult = 1f;				  // Currently unused, but can make the search deeper/shallower
	private final float hFinalPillScore = 10f;			  // Makes PacMac go for the final few pills faster
	private final float hScoreThreshold = 400f;		  // A threshold so that we only prioritize states with very high score gain
	private final float hScoreMult = 0.15f;				  // Makes A* search states with higher score with more priority

	// Number of ms to be subtracted from timeDue, to make the "PacMan still thinking..." go away
	private final int timeDueSubtract = 1;


	// A class for unifying and evaluating a gameCopy
	class State {
		public Game gameCopy;
		public int parentDir;				// Direction in which the first node headed
		public int depth;					// The current depth of the search
		public float value;					// The value of the state i.e. how much PacMan wants to be here (higher == better)
		public float heuristicValue;		// The heuristic value of the state i.e. how it is prioritized in search (higher == better)
		public boolean wasPowerPillActive;	// Indicates if in the last state the power pill was active (useful for eating the last ghost)

		// Constructor
		public State(Game game, int parentDir, int depth, boolean wasPowerPillActive)
		{
			this.gameCopy = game;
			this.parentDir = parentDir;
			this.depth = depth;
			this.wasPowerPillActive = wasPowerPillActive;
			evaluate();
			evaluateHeuristic();
		}

		// Calculates the value of the state (higher == better)
		private void evaluate()
		{
            // Death
			if (game.getLivesRemaining() > gameCopy.getLivesRemaining())
            {
                value = vDeath;
                return;
            }

            // Add score change from the start of the simulated state
			value = gameCopy.getScore() - game.getScore();

            // Add power pills penalty to make them costly and used effectively
			if (wasPowerPillActive) value += vPowerPillPenalty;

			// Add the depth to make pacman complete levels faster
			value += (depth * vDepthMult);

			// if going backwards make the score lower to not overuse it and spin
			if (game.getCurPacManDir() == gameCopy.getReverse(parentDir))
			{
                value += vReverseDir;
			}
		}

		// Calculates the heuristic value of the state (higher == better)
		private void evaluateHeuristic()
		{

			// Add the depth as a base
            heuristicValue = -depth * hDepthMult;

			// Add the gained score if it is over a threshold
			int gainedScore = gameCopy.getScore() - game.getScore();
			if (gainedScore > hScoreThreshold) heuristicValue += gainedScore * hScoreMult;

			// A power pill is not active, and we are hunting the ghosts
            if (gameCopy.getNumActivePowerPills() > 0 && !wasPowerPillActive)
            {
				// Find the closest distance from power pills to ghosts
				double closestPill = Float.POSITIVE_INFINITY;
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
					}
				}

				// Use quadratic easing to make the distance from pill non-linear
                float interpolator = (float) Math.clamp((closestPill) / hGhostDistMax, 0f, 1f);
                interpolator = 1 - (interpolator * interpolator);

				// Add the distance to the nearest pill
                heuristicValue += (interpolator * hGhostDistReward);
            }

			// No power pills exist -> look for final pill
			else if (!wasPowerPillActive)
			{
				heuristicValue += gainedScore * hFinalPillScore;
			}
		}
	}

	// Debug
	private long ticks = 0;
	private int maxReachedDepthPrint = 1;
	private final int printInfoEveryXTicks = 50;

	@Override
	public void tick(Game game, long timeDue) {

		// Debug prints so that I can easily evaluate the -sim command
		ticks += 1;
		if (ticks % printInfoEveryXTicks == 0)
		{
			double ghost1Dist = game.getPathDistance(game.getCurGhostLoc(0), game.getCurPacManLoc());
			double ghost2Dist = game.getPathDistance(game.getCurGhostLoc(1), game.getCurPacManLoc());
			double ghost3Dist = game.getPathDistance(game.getCurGhostLoc(2), game.getCurPacManLoc());
			double ghost4Dist = game.getPathDistance(game.getCurGhostLoc(3), game.getCurPacManLoc());

			double maxDist = Math.max(Math.max(Math.max(ghost1Dist, ghost2Dist), ghost3Dist), ghost4Dist);

			int ghost1Edible = game.getEdibleTime(0) > 0? 1 : 0;
			int ghost2Edible = game.getEdibleTime(1) > 0? 1 : 0;
			int ghost3Edible = game.getEdibleTime(2) > 0? 1 : 0;
			int ghost4Edible = game.getEdibleTime(3) > 0? 1 : 0;

			boolean isPowerPillActive = Math.max(Math.max(Math.max(
									ghost1Edible,
									ghost2Edible),
									ghost3Edible),
									ghost4Edible) > 0;
			
			String state = "None";
			if (game.getNumActivePowerPills() > 0 && !isPowerPillActive)
			{
				state = "Hunting ghosts!";
			}
			else if (isPowerPillActive)
			{
				state = "Eating ghosts!";
			}
			else
			{
				state = "Looking for pills!";
			}
			
			System.out.println("Score: " + game.getScore() +
							   ", lvl: " + game.getCurLevel() +
							   ", lives: " + game.getLivesRemaining() +
							   ", max depth reached (since last print): " + maxReachedDepthPrint +
							   ", next ghost score: " + game.getNextEdibleGhostScore() +
							   ", fruit exists: " + (game.getFruitLoc() != -1) +
							   ", max dist from ghost: " + maxDist +
							   ", state: " + state +
							   ", lvl time: " + game.getLevelTime()
							   );
			maxReachedDepthPrint = 1;
		}

		// Create a priority queue and a comparator based on heuristic
		Queue<State> statesToVisit = //new LinkedList<>();
		new PriorityQueue<>(new Comparator<State>() {
			@Override
			public int compare(State o1, State o2) {
				return Float.compare(o2.heuristicValue, o1.heuristicValue);
			}
		});

		// Put the initial directions in the queue
		for (int dir : game.getPossiblePacManDirs(true))
		{
			Game gameCopy = game.copy();
			gameCopy.advanceGame(dir);
			State newState = new State(gameCopy, dir, 1, false);
			if (newState.value == vDeath) continue;
			statesToVisit.add(newState);
		}

		// While we have time, explore the decision tree
		float bestScore = -100000000;
		while (System.currentTimeMillis() < timeDue - timeDueSubtract) {
			// Get current state
			State currState = statesToVisit.poll();
			if (currState == null) break;

			// Check the value if it is the best found
			if (currState.value > bestScore)
			{
				bestScore = currState.value;
				pacman.set(currState.parentDir);
			}

			// Skip states where we are going in a straight line to deepen the search
			boolean skipDirection = false;
			while (currState.gameCopy.getPossiblePacManDirs(false).length == 1)
			{
				int lastLives = currState.gameCopy.getLivesRemaining();
				currState.gameCopy.advanceGame(currState.gameCopy.getPossiblePacManDirs(false)[0]);
				currState.depth += 1;

				// Skip death states
				if (lastLives > currState.gameCopy.getLivesRemaining())
				{
					skipDirection = true;
					break;
				}
			}
			if (skipDirection) continue;

			// Add all possible next states and copy over some info (if power pill was active, depth, etc.)
			for (int dir : currState.gameCopy.getPossiblePacManDirs(true))
			{
				Game gameCopy = currState.gameCopy.copy();

				// Check if power pill was active
				boolean wasPowerPillActive = gameCopy.isEdible(0) ||
											 gameCopy.isEdible(1) ||
											 gameCopy.isEdible(2) ||
											 gameCopy.isEdible(3);

				// Advance the game and skip death states
				int lastLives = currState.gameCopy.getLivesRemaining();
				gameCopy.advanceGame(dir);
				if (lastLives > currState.gameCopy.getLivesRemaining()) continue;

				// Create a new state and evaluate it
				State newState = new State(gameCopy,currState.parentDir,
						currState.depth + 1,
						wasPowerPillActive);

				// Add it to queue
				statesToVisit.add(newState);

				// Timeout check to not print "PacMan is still thinking..."
				if (System.currentTimeMillis() >= timeDue - timeDueSubtract) break;
			}

			// Debug to know the max depth
			maxReachedDepthPrint = Math.max(maxReachedDepthPrint, currState.depth);
		}
	}
}

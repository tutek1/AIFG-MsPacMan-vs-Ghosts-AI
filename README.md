# Ms. Pac-Man vs. Ghosts Version 2.3

![screenshot](mspac.png)

## Overview

This is a Java implementation of the classic video game Ms. Pac-Man.  You can play it live from the keyboard, but it is mostly intended for experimenting with algorithms that play the game automatically. You can write your own agent that plays the game using the API described below.

## History

Midway released the arcade game Ms. Pac-Man in 1981.  Like its predecessor Pac-Man, it quickly became popular around the world.

In recent years, the [University of Essex](https://www.essex.ac.uk/) ran a series of competitions for software agents that attempt to play Ms. Pac-Man automatically, including the [Ms. Pac-Man versus Ghosts Competition](https://ieeexplore.ieee.org/document/5949599) from 2011-12.  Simon Lucas wrote an implementation of Ms. Pac-Man for that competition, and it was later modified by Philipp Rohlfshagen and other authors.  The implementation here is derived from that code, with further changes by Jakub Gemrot and [Adam Dingle](https://ksvi.mff.cuni.cz/~dingle/) from the Faculty of Mathematics and Physics at [Charles University](https://cuni.cz/UKEN-1.html).

The more recent [Ms. Pac-Man Vs. Ghost Team Competition](http://www.pacmanvghosts.co.uk/) has its own updated implementation of Ms. Pac-Man.  Perhaps we will merge in changes from that codebase at some point.

## Building the game

This version of Ms. Pac-Man vs. Ghosts works with Java 11 or newer, and possibly older Java versions as well.

This project includes Maven build files.  You should easily be able to load it into Eclipse, Intelli/J, or Visual Studio Code, all of which understand Maven builds.

## Playing the game

To play the game from the keyboard in a default configuration, run the class PacManSimulator in src/game/PacManSimulator.java.  Use the arrow keys to move.  You can press 'P' to pause the game, or 'N' to advance the game by a single frame.

This is a fairly faithful recreation of the original Ms. Pac-Man game, with some differences.  In particular, there are no bonus fruits.

## Writing an agent

You can enhance the MyPacMan class to build a custom agent that controls Ms. Pac-Man.  On each tick, the game will call your implementation of the tick() method, where you can decide which action Ms. Pac-Man should take.

The Game interface has everything you need to find about the game state. Note that

- The maze is represented as a graph of __nodes__.  Each node is a distinct position in the maze and has a unique integer index.  There are about 1,000 nodes in each maze (the exact number varies from level to level) and they are evenly spaced throughout the maze.
- Each pill in the maze has a unique index.
- Ghosts are numbered from 0 to 3.
- Directions are listed at the top of interface Game (UP=0, RIGHT=1, etc.)
- The game normally runs at 25 ticks per second.

The Game interface includes these methods:

- `int getCurPacManLoc()` – return Ms. Pac-Man's current position, i.e. the graph node where she is currently located.
- `int getCurGhostLoc(int whichGhost)` – return the given ghost's current position.
- `int getCurGhostDir(int whichGhost)` – return the given ghost's current direction.
- `int getX(int nodeIndex), int getY(int nodeIndex)` - return the X/Y pixel positions of the given graph node
- `int getNeighbour(int nodeIndex, int direction)` – return the given node's neighbor in the given direction, or -1 if none.
- `boolean isEdible(int whichGhost)` – true if the given ghost is currently edible
- `int getEdibleTime(int whichGhost)` - the number of remaining ticks during which the given ghost will be edible.
- `int getPillIndex(int nodeIndex)` – return the index of the pill that is/was at the given position, or -1 if this is not a position that ever holds a pill in this maze. Note that this method returns the same value whether or not the pill has already been eaten. You must call `checkPill` to find out whether the pill still exists. (Do not pass -1 to `checkPill`; that will result in an out of bounds error.)
- `boolean checkPill(int pillIndex)` – return true if the given pill still exists, i.e. has not been eaten.
- `double getEuclideanDistance(int from, int to)`<br>
  `int getManhattanDistance(int from, int to)`

	Return the Euclidean or Manhattan distance between two maze nodes.
- `int getPathDistance(int from, int to)`<br>

	Return the shortest-path distance walking through the maze without going through walls. This method uses a precomputed table so it is very fast (it doesn't need to perform a depth-first search of its own!)
	
	Note that if g is the position of a ghost in its lair, and p is any position in the maze, then `getPathDistance(g, p)` will return -1, meaning that there is no path from the ghost's position to the maze position since the lair is surrounded by walls.

- `public int getGhostPathDistance(int whichGhost, int to)`

  Return the shortest distance that the given ghost may travel through the maze to reach the given position, taking into consideration that ghosts may not reverse direction.

	Warning: this method is about 50-150x slower than `getPathDistance`!  (In my experiments it typically runs in tens of microseconds, whereas `getPathDistance` takes under 1 microsecond.)  That's because it (unfortunately) does not use a precomputed table, so it must trace a path through the maze.

The GameView class has various static methods that draw text or graphics overlaid on the game view.  These are handy for debugging.  Perhaps the most useful is

- `public static void addPoints(Game game, Color color, int... nodeIndices)`

  Draw a set of points at the given maze nodes, in the given color.  This is a nice way to draw a path through the maze.  (You must call this method anew on every tick if you want the path you draw to remain visible.)

See the sample agent NearestPillPacManVS.java for an example of using these drawing methods.

## Evaluating your agent

The Evaluate class will evaluate your MyPacMan agent by running it on a series of random games.  As it does so, it will generate a couple of CSV files showing the scores achieved in each game and other statistics.  It will also print output to the console showing some of this data, including the average score your agent achieves.

The evaluation will also generate a series of replay files.  Each of these files logs all the activity in a single game.  To replay any game, run the PacManReplayer class (in src/game).  Change the filename in the main() method to the name of the replay file you wish to run.

## Other notes

The game.controllers package contains a set of sample agents (some that control Ms. Pac-Man, and also some that control ghosts).

When running an agent, press the 'H' key to "hijack" control and manually navigate Ms. Pac-Man.

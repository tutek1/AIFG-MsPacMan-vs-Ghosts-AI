import controllers.pacman.PacManControllerBase;
import game.core.Game;

public final class RandomNonRevPacMan extends PacManControllerBase
{	
	@Override
	public void tick(Game game, long timeDue) {
		int[] directions=game.getPossiblePacManDirs(false);		//set flag as false to prevent reversals	
		pacman.set(directions[game.rand().nextInt(directions.length)]);		
	}
}

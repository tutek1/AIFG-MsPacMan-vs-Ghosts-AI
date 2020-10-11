import game.PacManSimulator;
import game.controllers.ghosts.game.GameGhosts;
import game.controllers.pacman.*;

public class MsPacMan {
	public static void main(String[] args) throws Exception {
        IPacManController controller = new HumanPacMan();

        if (args.length > 0)
            controller = (IPacManController) Class.forName(args[0]).getConstructor().newInstance();

        PacManSimulator.play(controller, new GameGhosts(4, false));
    }
}

import static java.lang.System.out;

import game.*;
import game.controllers.ghosts.game.GameGhosts;
import game.controllers.pacman.*;
import tournament.EvaluateAgent;

public class MsPacMan {
    static void usage() {
        out.println("usage: mspac [<agent-classname>] [<option>...]");
        out.println("options:");
        out.println("  -sim <count> : simulate a series of games without visualization");
        out.println("  -v : verbose");
        System.exit(1);
    }
    public static void main(String[] args) throws Exception {
        IPacManController agent = null;
        String agentName = null;
        int sim = 0;
        boolean verbose = false;

        for (int i = 0 ; i < args.length ; ++i) {
            String s = args[i];
            switch (s) {
                case "-sim":
                    sim = Integer.parseInt(args[++i]);;
                    break;
                case "-v":
                    verbose = true;
                    break;
                default:
                    if (s.startsWith("-"))
                        usage();
                    agentName = s;
                    agent =
                        (IPacManController) Class.forName(agentName).getConstructor().newInstance();
            }
        }

        if (sim > 0) {
            if (agent == null) {
                System.out.println("must specify agent with -sim");
                return;
            }
            SimulatorConfig config = new SimulatorConfig();
            config.ghostsController = new GameGhosts(4);
            config.visualize = false;
            EvaluateAgent evaluate = new EvaluateAgent(0, config, sim, null);
            evaluate.evaluateAgent(agentName, agent, verbose);		
        } else {
            if (agent == null)
                agent = new HumanPacMan();
            PacManSimulator.play(agent, new GameGhosts(4));
        }
    }
}

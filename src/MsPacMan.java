import static java.lang.System.out;

import game.*;
import game.controllers.ghosts.game.GameGhosts;
import game.controllers.pacman.*;
import tournament.EvaluateAgent;

public class MsPacMan {
    static void usage() {
        out.println("usage: mspac [<agent-classname>] [<option>...]");
        out.println("options:");
        out.println("  -seed <num> : random seed");
        out.println("  -sim <count> : simulate a series of games without visualization");
        out.println("  -v : verbose");
        System.exit(1);
    }
    public static void main(String[] args) throws Exception {
        IPacManController agent = null;
        String agentName = null;
        int seed = 0;
        boolean seedSpecified = false;
        int sim = 0;
        boolean verbose = false;

        for (int i = 0 ; i < args.length ; ++i) {
            String s = args[i];
            switch (s) {
                case "-seed":
                    seed = Integer.parseInt(args[++i]);
                    seedSpecified = true;
                    break;
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
            if (!seedSpecified)
                seed = 0;
            SimulatorConfig config = new SimulatorConfig();
            config.ghostsController = new GameGhosts(4);
            config.visualize = false;
            EvaluateAgent evaluate = new EvaluateAgent(seed, config, sim, null);
            evaluate.evaluateAgent(agentName, agent, verbose);		
        } else {
            if (agent == null)
                agent = new HumanPacMan();
            if (!seedSpecified)
                seed = -1;      // random game
            PacManSimulator.play(agent, new GameGhosts(4), seed);
        }
    }
}

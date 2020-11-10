import static java.lang.System.out;

import java.io.File;

import game.*;
import controllers.ghosts.game.GameGhosts;
import controllers.pacman.*;
import tournament.EvaluateAgent;

public class MsPacMan {
    static void usage() {
        out.println("usage: mspac [<agent-classname>] [<option>...]");
        out.println("options:");
        out.println("  -level <num> : starting level");
        out.println("  -resultdir <path> : directory for results in CSV format");
        out.println("  -seed <num> : random seed");
        out.println("  -sim <count> : simulate a series of games without visualization");
        out.println("  -v : verbose");
        System.exit(1);
    }
    public static void main(String[] args) throws Exception {
        IPacManController agent = null;
        String agentName = null;
        int level = 1;
        String resultdir = null;
        int seed = 0;
        boolean seedSpecified = false;
        int sim = 0;
        boolean verbose = false;

        for (int i = 0 ; i < args.length ; ++i) {
            String s = args[i];
            switch (s) {
                case "-level":
                    level = Integer.parseInt(args[++i]); 
                    break;
                case "-resultdir":
                    resultdir = args[++i];
                    break;
                case "-seed":
                    seed = Integer.parseInt(args[++i]);
                    seedSpecified = true;
                    break;
                case "-sim":
                    sim = Integer.parseInt(args[++i]);
                    break;
                case "-v":
                    verbose = true;
                    break;
                default:
                    if (s.startsWith("-"))
                        usage();
                    agent =
                        (IPacManController) Class.forName(s).getConstructor().newInstance();
                    agentName = s.substring(s.lastIndexOf(".") + 1);
            }
        }

		SimulatorConfig config = new SimulatorConfig();
        config.ghostsController = new GameGhosts(4);
        config.game.startingLevel = level;

        if (sim > 0) {
            if (agent == null) {
                System.out.println("must specify agent with -sim");
                return;
            }
            if (!seedSpecified)
                seed = 0;
            config.visualize = false;
            EvaluateAgent evaluate =
                new EvaluateAgent(seed, config, sim,
                                  resultdir == null ? null : new File(resultdir));
            evaluate.evaluateAgent(agentName, agent, verbose);		
        } else {
            if (agent == null)
                agent = new HumanPacMan();
            config.pacManController = agent;
            if (!seedSpecified)
                seed = -1;      // random game
            config.game.seed = seed;
            PacManSimulator.play(config);
        }
    }
}

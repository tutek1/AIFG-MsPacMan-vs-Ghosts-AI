package controllers.ghosts;

public final class GhostsActions {
	
	private static GhostAction NONE;
	
	public GhostAction[] actions = new GhostAction[4];
	
	public final int ghostCount;	
	
	public GhostsActions(int ghostCount) {
		this.ghostCount = ghostCount;
		for (int i = 0; i < actions.length; ++i) {
			actions[i] = new GhostAction();
		}
	}
	
	public GhostAction ghost(int index) {
		if (index < 0 || index > actions.length) return NONE;
		return actions[index];
	}
	
	public void set(int[] directions) {
		for (int i = 0; i < directions.length && i < actions.length; ++i) {
			actions[i].set(directions[i]);
		}
	}
	
	public void reset() {
		for (int i = 0; i < actions.length; ++i) {
			actions[i].reset();
		}
	}		
	
	public GhostsActions clone() {
		GhostsActions result = new GhostsActions(ghostCount);
		
		for (int i = 0; i < actions.length; ++i) {
			result.actions[i] = actions[i].clone();
		}
		
		return result;
	}
}

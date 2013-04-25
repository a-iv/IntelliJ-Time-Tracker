package net.roarsoftware.tracker.core.filters;

import net.roarsoftware.tracker.model.Task;
import net.roarsoftware.tracker.model.State;

/**
 * @author Janni Kovacs
 */
public class StateFilter implements TaskFilter {
	private State state;

	public StateFilter(State state) {
		this.state = state;
	}

	public boolean accept(Task t) {
		return t.getState() == state;
	}
}

package net.roarsoftware.tracker.core.filters;

import net.roarsoftware.tracker.model.Task;

/**
 * @author Janni Kovacs
 */
public class NegationFilter implements TaskFilter {
	private TaskFilter target;

	public NegationFilter(TaskFilter target) {
		this.target = target;
	}

	public boolean accept(Task t) {
		return !target.accept(t);
	}
}

package net.roarsoftware.tracker.core.filters;

import net.roarsoftware.tracker.model.Task;
import net.roarsoftware.tracker.model.Priority;

/**
 * @author Janni Kovacs
 */
public class PriorityFilter implements TaskFilter {
	private Priority priority;

	public PriorityFilter(Priority priority) {
		this.priority = priority;
	}

	public boolean accept(Task t) {
		return t.getPriority() == priority;
	}
}

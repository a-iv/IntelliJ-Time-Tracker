package net.roarsoftware.tracker.core.filters;

import net.roarsoftware.tracker.model.Task;

/**
 * <code>TaskFilter</code>s accept or reject specific {@link Task}s based on their properties.
 *
 * @author Janni Kovacs
 */
public interface TaskFilter {

	/**
	 * Returns <code>true</code> if this filter accepts a given Task.
	 * @param t The Task to test
	 * @return <code>true</code> if accepted
	 */
	public boolean accept(Task t);
}

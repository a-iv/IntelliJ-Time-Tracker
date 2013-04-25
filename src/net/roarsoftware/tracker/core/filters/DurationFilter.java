package net.roarsoftware.tracker.core.filters;

import net.roarsoftware.tracker.model.Task;

/**
 * @author Janni Kovacs
 */
public class DurationFilter implements TaskFilter{
	private long duration;
	private boolean shorter;

	public DurationFilter(long duration, boolean shorter) {
		this.duration = duration;
		this.shorter = shorter;
	}

	public boolean accept(Task t) {
		return shorter ? t.getTotalDuration() < duration : t.getTotalDuration() >= duration;
	}
}

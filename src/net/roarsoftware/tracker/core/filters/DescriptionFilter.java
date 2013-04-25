package net.roarsoftware.tracker.core.filters;

import net.roarsoftware.tracker.model.Task;

/**
 * @author Janni Kovacs
 */
public class DescriptionFilter implements TaskFilter{
	private String description;

	public DescriptionFilter(String description) {
		this.description = description;
	}

	public boolean accept(Task t) {
		return t.getDescription().toLowerCase().contains(description.toLowerCase());
	}
}

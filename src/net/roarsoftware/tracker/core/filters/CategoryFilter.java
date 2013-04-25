package net.roarsoftware.tracker.core.filters;

import net.roarsoftware.tracker.model.Task;

/**
 * @author Janni Kovacs
 */
public class CategoryFilter implements TaskFilter {
	private String category;

	public CategoryFilter(String category) {
		this.category = category;
	}

	public boolean accept(Task t) {
		return t.getCategory().equalsIgnoreCase(category);
	}
}

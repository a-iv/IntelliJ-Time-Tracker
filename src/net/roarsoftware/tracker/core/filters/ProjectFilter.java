package net.roarsoftware.tracker.core.filters;

import net.roarsoftware.tracker.model.Task;

/**
 * @author Janni Kovacs
 */
public class ProjectFilter implements TaskFilter {
	private String project;

	public ProjectFilter(String project) {
		this.project = project;
	}

	public boolean accept(Task t) {
		return t.getProject().equals(project);
	}
}

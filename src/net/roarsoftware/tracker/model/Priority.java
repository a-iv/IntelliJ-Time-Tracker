package net.roarsoftware.tracker.model;

/**
 * @author Janni Kovacs
 */
public enum Priority {

	HIGH("High"),
	MEDIUM("Medium"),
	LOW("Low");

	private String s;

	Priority(String s) {
		this.s = s;
	}

	@Override
	public String toString() {
		return s;
	}
}

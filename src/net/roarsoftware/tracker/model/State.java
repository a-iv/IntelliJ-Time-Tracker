package net.roarsoftware.tracker.model;

/**
 * @author Janni Kovacs
 */
public enum State {

	NEW("New"),
	STARTED("Started"),
//	WORKING("Working"),
	DONE("Done");

	private String s;

	State(String s) {
		this.s = s;
	}

	@Override
	public String toString() {
		return s;
	}
}

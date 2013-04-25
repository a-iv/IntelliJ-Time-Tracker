package net.roarsoftware.tracker.core.filters;

/**
 * @author Janni Kovacs
 */
public class TaskFilterInfo {

	private TaskFilter filter;
	private FilterFactory.Rule rule;
	private Object value;
	private FilterFactory factory;

	public TaskFilterInfo(TaskFilter filter, FilterFactory factory, FilterFactory.Rule rule, Object value) {
		this.filter = filter;
		this.factory = factory;
		this.rule = rule;
		this.value = value;
	}

	public FilterFactory getFactory() {
		return factory;
	}

	public TaskFilter getFilter() {
		return filter;
	}

	public FilterFactory.Rule getRule() {
		return rule;
	}

	public Object getValue() {
		return value;
	}
}

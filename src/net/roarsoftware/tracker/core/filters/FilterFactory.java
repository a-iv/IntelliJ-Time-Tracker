package net.roarsoftware.tracker.core.filters;

import net.roarsoftware.tracker.model.Priority;
import net.roarsoftware.tracker.model.State;
import net.roarsoftware.tracker.ui.TimeFormatter;

/**
 * @author Janni Kovacs
 */
public abstract class FilterFactory {
	private static FilterFactory[] factories = new FilterFactory[6];
	static {
		factories[0] = new FilterFactory("Project", new Rule[]{Rule.EQUALS, Rule.NOT_EQUALS}, null) {
			protected TaskFilter createFilterInternal(Rule rule, Object value) {
				return new ProjectFilter(value.toString());
			}
		};
		factories[1] = new FilterFactory("Category", new Rule[]{Rule.EQUALS, Rule.NOT_EQUALS}, null) {
			protected TaskFilter createFilterInternal(Rule rule, Object value) {
				return new CategoryFilter(value.toString());
			}
		};
		factories[2] = new FilterFactory("Description", new Rule[]{Rule.CONTAINS, Rule.NOT_CONTAINS}, null) {
			protected TaskFilter createFilterInternal(Rule rule, Object value) {
				return new DescriptionFilter(value.toString());
			}
		};
		factories[3] = new FilterFactory("Priority", new Rule[]{Rule.EQUALS, Rule.NOT_EQUALS}, Priority.values()) {
			protected TaskFilter createFilterInternal(Rule rule, Object value) {
				return new PriorityFilter((Priority) value);
			}
		};
		factories[4] = new FilterFactory("State", new Rule[]{Rule.EQUALS, Rule.NOT_EQUALS}, State.values()) {
			protected TaskFilter createFilterInternal(Rule rule, Object value) {
				return new StateFilter((State) value);
			}
		};
		factories[5] = new FilterFactory("Duration", new Rule[]{Rule.IS_LESS, Rule.IS_MORE}, new String[]{"00:00:00"}) {
			protected TaskFilter createFilterInternal(Rule rule, Object value) {
				return new DurationFilter(TimeFormatter.parse(value.toString()), rule == Rule.IS_LESS);
			}
		};
//		factories[6] = new FilterFactory("Progress", new Rule[]{Rule.IS_LESS, Rule.IS_MORE}, null) {
//			protected TaskFilter createFilterInternal(Rule rule, Object value) {
//
//			}
//		};
	}

	private String name;
	private Rule[] rules;
	private Object[] values;

	private FilterFactory(String name, Rule[] rules, Object[] values) {
		this.name = name;
		this.rules = rules;
		this.values = values;
	}

	public static FilterFactory[] getFactories() {
		return factories;
	}

	public static FilterFactory getFactory(String name) {
		for (FilterFactory factory : factories) {
			if(factory.name.equals(name))
				return factory;
		}
		return null;
	}

	public String getName() {
		return name;
	}

	public Rule[] getRules() {
		return rules;
	}

	public Object[] getValues() {
		return values;
	}

	public TaskFilter createFilter(Rule rule, Object value) {
		TaskFilter original = createFilterInternal(rule, value);
		if (rule == Rule.NOT_CONTAINS || rule == Rule.NOT_EQUALS) {
			return new NegationFilter(original);
		}
		return original;
	}

	@Override
	public String toString() {
		return name;
	}

	protected abstract TaskFilter createFilterInternal(Rule rule, Object value);

	public static enum Rule {
		EQUALS("equals"),
		NOT_EQUALS("not equals"),
		CONTAINS("contains"),
		NOT_CONTAINS("not contains"),
		IS_LESS("less than"),
		IS_MORE("more than");

		private String s;

		Rule(String s) {
			this.s = s;
		}

		@Override
		public String toString() {
			return s;
		}
	}
}

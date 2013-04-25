package net.roarsoftware.tracker.model;

import java.util.HashMap;
import java.util.Map;

import net.roarsoftware.tracker.core.filters.DateFilter;

/**
 * @author Janni Kovacs
 */
public class Task {

	private String description;
	private String category;
	private String project;

	private Priority priority;

	private Day dateStarted, dateFinished;
	private Map<Day, Long> workPerDay = new HashMap<Day, Long>();
	private long totalDuration;

	private long maxDuration;
	private double progress;

	public Task(Task copy) {
		this(copy.description, copy.category, copy.project, copy.priority, copy.maxDuration);
		this.workPerDay = new HashMap<Day, Long>(copy.workPerDay);
		this.totalDuration = copy.totalDuration;
		this.progress = copy.progress;
		this.dateStarted = copy.dateStarted;
		this.dateFinished = copy.dateFinished;
	}

	public Task(String description, String category, String project, Priority priority) {
		this(description, category, project, priority, -1);
	}

	public Task(String description, String category, String project, Priority priority, long maxDuration) {
		this.description = description;
		this.category = category;
		this.project = project;
		this.priority = priority;
		this.maxDuration = maxDuration;
	}

	public String getCategory() {
		return category;
	}

	public String getDescription() {
		return description;
	}

	public String getProject() {
		return project;
	}

	public Priority getPriority() {
		return priority;
	}

	public Day getDateFinished() {
		return dateFinished;
	}

	public void setDateFinished(Day dateFinished) {
		this.dateFinished = dateFinished;
	}

	public Day getDateStarted() {
		return dateStarted;
	}

	public void setDateStarted(Day dateStarted) {
		this.dateStarted = dateStarted;
	}

	public long getTotalDuration() {
		return totalDuration;
	}

	public void setTotalDuration(long totalDuration) {
		this.totalDuration = totalDuration;
	}

	public long getMaxDuration() {
		return maxDuration;
	}

	public double getProgress() {
		return progress;
	}

	public void setProgress(double progress) {
		if(progress < 0 || progress > 1)
			throw new IllegalArgumentException();
		this.progress = progress;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setMaxDuration(long maxDuration) {
		this.maxDuration = maxDuration;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public void addDuration(Day day, long duration) {
		totalDuration += duration;
		Long d = workPerDay.get(day);
		workPerDay.put(day, d != null ? d + duration : duration);
	}

	public void addDurationToday(long duration) {
		addDuration(Day.today(), duration);
	}

	public Map<Day, Long> getWorkPerDay() {
		return workPerDay;
	}

	/**
	 * Returns the amount of time spent on this task between the two given days (both inclusive).
	 *
	 * @param from start date
	 * @param to end date
	 * @return time spent in milliseconds
	 */
	public long getDurationInBetween(Day from, Day to) {
		if (from.after(to)) {
			throw new IllegalArgumentException("from after to");
		}
		DateFilter filter = new DateFilter(from, to);
		long time = 0;
		for (Map.Entry<Day, Long> entry : workPerDay.entrySet()) {
			if(filter.contains(entry.getKey()))
				time += entry.getValue();
		}
		return time;
	}

	public State getState() {
		if(dateStarted == null)
			return State.NEW;
		if(dateFinished == null)
			return State.STARTED;
		return State.DONE;
	}
}

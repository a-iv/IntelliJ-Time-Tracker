/**
 *
 * @author Janni Kovacs
 */
package net.roarsoftware.tracker.model;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Timer;

import com.intellij.openapi.project.Project;
import net.roarsoftware.tracker.core.filters.DateFilter;
import net.roarsoftware.tracker.core.filters.TaskFilterInfo;

/**
 * Main model class for the Time Tracker plugin. The <code>GlobalTaskModel</code> contains information
 * about the Tasks, the current task, the working status, the idling process and the global date filter.
 * It also manages the list of {@link TaskListener}s and a list of applies {@link TaskFilterInfo}s.
 *
 * This class is a singleton and can be accessed everywhere with the {@link #getInstance} method.
 *
 * This class contains the internal Timer, which recalculates the time spent on a task or the time idled
 * every second.
 */
public class GlobalTaskModel implements ActionListener {

	private static GlobalTaskModel instance = new GlobalTaskModel();

	private List<Task> tasks = new ArrayList<Task>();
	private List<TaskListener> listeners = new ArrayList<TaskListener>();
	private Task currentTask;
	private boolean working;

	private Timer timer;
	private long idlingTime = 0;
	private long globalTime = 0;
	private Set<String> categories;

	private DateFilter dateFilter;
	private boolean dateFilterActive;

	private List<TaskFilterInfo> filters = new ArrayList<TaskFilterInfo>();

	private GlobalTaskModel() {
		timer = new Timer(1000, this);
		categories = new HashSet<String>();
		Day today = new Day();
		dateFilter = new DateFilter(today, today);
	}

	public static GlobalTaskModel getInstance() {
		return instance;
	}

	public void start() {
		timer.start();
	}

	public void stop() {
		timer.stop();
	}

	public long getIdlingTime() {
		return idlingTime;
	}

	public void setIdlingTime(long idlingTime) {
		this.idlingTime = idlingTime;
	}

	public DateFilter getDateFilter() {
		return dateFilter;
	}

	public void setDateRangeStart(Day start) {
		dateFilter.setStart(start);
		for (TaskListener listener : listeners) {
			listener.dateRangeChanged(dateFilter);
		}
	}

	public void setDateRangeEnd(Day end) {
		dateFilter.setEnd(end);
		for (TaskListener listener : listeners) {
			listener.dateRangeChanged(dateFilter);
		}
	}

	public long getGlobalTime() {
		return globalTime;
	}

	public void setGlobalTime(long globalTime) {
		this.globalTime = globalTime;
	}

	public List<Task> getAllTasks() {
		return tasks;
	}

	public void addTask(Task t) {
		tasks.add(t);
		categories.add(t.getCategory());
		for (TaskListener listener : listeners) {
			listener.taskAdded(t);
		}
	}

	public void removeTask(Task t) {
		tasks.remove(t);
		for (TaskListener listener : listeners) {
			listener.taskRemoved(t);
		}
	}

	public void setCurrentTask(Task t) {
		setWorking(false);
		this.currentTask = t;
		for (TaskListener listener : listeners) {
			listener.setCurrentTask(t);
		}
	}

	public Task getCurrentTask() {
		return currentTask;
	}

	public void setWorking(boolean working) {
		if (currentTask == null) {
			return;
		}
		this.working = working;
		for (TaskListener listener : listeners) {
			listener.setWorking(working);
		}
	}

	public boolean isWorking() {
		return working;
	}

	public void addTaskListener(TaskListener l) {
		listeners.add(l);
	}

	public void removeTaskListener(TaskListener l) {
		listeners.remove(l);
	}

	public void removeProjectTaskListeners(Project project) {
		listeners.removeIf(taskListener -> taskListener.getProject() == project);
	}

	public void actionPerformed(ActionEvent e) {
		globalTime += 1000;
		if (working) {
			currentTask.addDurationToday(1000);
			for (TaskListener listener : listeners) {
				listener.durationChanged(currentTask, currentTask.getTotalDuration());
			}
		} else {
			idlingTime += 1000;
			for (TaskListener listener : listeners) {
				listener.durationChanged(null, idlingTime);
			}
		}
	}

	public Object[] getCategories() {
		return categories.toArray();
	}

	public Task findTask(String description) {
		for (Task task : tasks) {
			if(task.getDescription().equals(description))
				return task;
		}
		return null;
	}

	public void addFilter(TaskFilterInfo filter) {
		filters.add(filter);
	}

	public void removeFilter(TaskFilterInfo filter) {
		filters.remove(filter);
	}

	public List<TaskFilterInfo> getFilters() {
		return filters;
	}

	public void setDateFilterActive(boolean dateFilterActive) {
		this.dateFilterActive = dateFilterActive;
		for (TaskListener listener : listeners) {
			listener.dateFilterActivated(dateFilterActive);
		}
	}

	public boolean isDateFilterActive() {
		return dateFilterActive;
	}
}

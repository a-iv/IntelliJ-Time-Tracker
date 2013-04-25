package net.roarsoftware.tracker.model;

import net.roarsoftware.tracker.core.filters.DateFilter;

/**
 * <code>TaskListener</code>s get notified if something in the {@link net.roarsoftware.tracker.model.GlobalTaskModel}
 * changes. Usually every {@link net.roarsoftware.tracker.ui.TrackerWindow} and every
 * {@link net.roarsoftware.tracker.ui.HistoryWindow} acts as a TaskListener.
 * 
 * @author Janni Kovacs
 */
public interface TaskListener {

	public void taskAdded(Task t);

	public void taskRemoved(Task t);

	public void setCurrentTask(Task t);

	public void setWorking(boolean working);

	public void durationChanged(Task t, long duration);

	public void dateRangeChanged(DateFilter dateFilter);

	public void dateFilterActivated(boolean dateFilterActive);
}

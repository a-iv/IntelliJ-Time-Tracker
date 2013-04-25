package net.roarsoftware.tracker.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.table.AbstractTableModel;

import net.roarsoftware.tracker.core.filters.TaskFilter;
import net.roarsoftware.tracker.model.GlobalTaskModel;
import net.roarsoftware.tracker.model.Priority;
import net.roarsoftware.tracker.model.State;
import net.roarsoftware.tracker.model.Task;

/**
 * @author Janni Kovacs
 */
public class TaskTableModel extends AbstractTableModel {

	private String[] titles = {"Priority", "Project", "Category", "Description", "Duration", "Progress", "State"};
	private GlobalTaskModel model = GlobalTaskModel.getInstance();
	private Set<TaskFilter> filters = new HashSet<TaskFilter>();
	private List<Task> filteredData;
	private boolean editable = true;

	@Override
	public int getColumnCount() {
		return titles.length;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return editable && columnIndex == 5 && getTask(rowIndex).getMaxDuration() == -1;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
			case 0:
				return Priority.class;
			case 4:
				return Long.class;
			case 5:
				return Double.class;
			case 6:
				return State.class;
			default:
				return String.class;
		}
	}

	@Override
	public String getColumnName(int column) {
		return titles[column];
	}

	@Override
	public int getRowCount() {
		if (filteredData != null)
			return filteredData.size();
		return model.getAllTasks().size();
	}

	@Override
	public Object getValueAt(int row, int column) {
		Task task = getTask(row);
		switch (column) {
			case 0:
				return task.getPriority();
			case 1:
				return task.getProject();
			case 2:
				return task.getCategory();
			case 3:
				return task.getDescription();
			case 4:
				return task.getTotalDuration();
			case 5:
				if(task.getMaxDuration() == -1)
					return task.getProgress();
				return (double) task.getTotalDuration() / task.getMaxDuration();
			case 6:
				return task.getState();
		}
		return null;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex == 5) {
			getTask(rowIndex).setProgress((Double) aValue);
		}
	}

	public Task getTask(int row) {
		if (filteredData != null) {
			return filteredData.get(row);
		}
		return model.getAllTasks().get(row);
	}

	public void addFilter(TaskFilter filter) {
		filters.add(filter);
		applyFilters();
	}

	public void removeFilter(TaskFilter filter) {
		filters.remove(filter);
		applyFilters();
	}

	public void clearFilters() {
		filters.clear();
		applyFilters();
	}

	public boolean containsFilter(TaskFilter filter) {
		return filters.contains(filter);
	}

	protected void applyFilters() {
		if (filters.size() > 0) {
			List<Task> newFilteredData = new ArrayList<Task>();
			filteredData = new ArrayList<Task>();
			taskLoop:
			for (Task task : model.getAllTasks()) {
				for (TaskFilter filter : filters) {
					if (!filter.accept(task)) {
						continue taskLoop;
					}
				}
				newFilteredData.add(task);
			}
			filteredData = newFilteredData;
		} else {
			filteredData = null;
		}
		fireTableDataChanged();

	}

//	public Set<TaskFilter> getFilters() {
//		return filters;
//	}

	public boolean isCurrentTask(int row) {
		return getTask(row) == model.getCurrentTask();
	}

	public int getRowFor(Task t) {
		if (filteredData != null) {
			return filteredData.indexOf(t);
		}
		return model.getAllTasks().indexOf(t);
	}

	public void taskDurationChanged(Task t) {
		int row = getRowFor(t);
		fireTableCellUpdated(row, 4);
		fireTableCellUpdated(row, 5);
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}
}

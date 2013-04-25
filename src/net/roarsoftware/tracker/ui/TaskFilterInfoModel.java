package net.roarsoftware.tracker.ui;

import java.util.List;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

import net.roarsoftware.tracker.core.filters.TaskFilterInfo;

/**
 * @author Janni Kovacs
 */
public class TaskFilterInfoModel extends AbstractTableModel {

	private List<TaskFilterInfo> filters = new ArrayList<TaskFilterInfo>();
	private String[] titles = new String[]{"Name", "Rule", "Value"};

	public void addFilter(TaskFilterInfo filter) {
		filters.add(filter);
		fireTableDataChanged();
	}

	public void removeFilter(TaskFilterInfo filter) {
		filters.remove(filter);
		fireTableDataChanged();
	}

	public int getRowCount() {
		return filters.size();
	}

	public int getColumnCount() {
		return titles.length;
	}

	@Override
	public String getColumnName(int column) {
		return titles[column];
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		TaskFilterInfo i = filters.get(rowIndex);
		switch (columnIndex) {
			case 0:
				return i.getFactory().getName();
			case 1:
				return i.getRule();
			case 2:
				return i.getValue();
		}
		return null;
	}

	public TaskFilterInfo getFilter(int row) {
		return filters.get(row);
	}

	public List<TaskFilterInfo> getFilters() {
		return filters;
	}

	public void clear() {
		filters.clear();
	}
}

package net.roarsoftware.tracker.ui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * @author Janni Kovacs
 */
public class TaskTableRenderer extends DefaultTableCellRenderer {
	private TaskTableModel model;
	private Color selectionBackground;
	private Color defaultBackground;
	private Color selectionForeground;
	private Color defaultForeground;

	private Color progressGreen = new Color(109, 201, 101);
	private Color progressRed = new Color(201, 101, 101);
	private TableSlider slider;

	public TaskTableRenderer(TaskTableModel model) {
		this.model = model;
		slider = new TableSlider();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
												   int row, int column) {
		Component comp;
		if (value instanceof Double) {
			double progress = (Double) value;
			slider.setColor(progress <= 1 ? progressGreen : progressRed);
			slider.setValue(progress);
			comp = slider;
		} else {
			comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (value instanceof Long) {
				setText(TimeFormatter.format((Long) value));
			}
		}
		if (model.isCurrentTask(table.getRowSorter().convertRowIndexToModel(row))) {
			comp.setBackground(Color.LIGHT_GRAY);
			comp.setForeground(Color.BLACK);
		} else {
			comp.setBackground(isSelected ? selectionBackground : defaultBackground);
			comp.setForeground(isSelected ? selectionForeground : defaultForeground);
		}
		return comp;
	}

	@Override
	public void updateUI() {
		super.updateUI();
		selectionBackground = UIManager.getColor("Table.selectionBackground");
		defaultBackground = UIManager.getColor("Table.background");
		selectionForeground = UIManager.getColor("Table.selectionForeground");
		defaultForeground = UIManager.getColor("Table.foreground");
	}

}

package net.roarsoftware.tracker.ui;

import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 * @author Janni Kovacs
 */
public class ProgressCellEditor extends AbstractCellEditor implements TableCellEditor {

	private TableSlider slider;

	public ProgressCellEditor() {
		slider = new TableSlider();
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		slider.setValue((Double) value);
		return slider;
	}

	public Object getCellEditorValue() {
		return slider.getValue();
	}


}

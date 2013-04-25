package net.roarsoftware.tracker.core.report;

import java.util.List;

import org.jfree.chart.JFreeChart;

import net.roarsoftware.tracker.model.Day;
import net.roarsoftware.tracker.model.Task;

/**
 * Generates a Chart for a report.
 *
 * @author Janni Kovacs
 */
public interface ChartGenerator {

	public String getTitle();

	public String getDescription();

	public JFreeChart generateChart(List<Task> tasks, Day from, Day to);
}

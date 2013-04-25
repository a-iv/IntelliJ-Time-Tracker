package net.roarsoftware.tracker.core.report;

import java.awt.Color;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.PieDataset;

import net.roarsoftware.tracker.model.Day;
import net.roarsoftware.tracker.model.Task;

/**
 * @author Janni Kovacs
 */
public abstract class TimeDistributionChartGenerator implements ChartGenerator {

	public String getTitle() {
		return "Time per ";
	}

	public String getDescription() {
		return "Generates a pie chart showing the distribution of work time per ";
	}

	public JFreeChart generateChart(List<Task> tasks, Day from, Day to) {
		JFreeChart chart = ChartFactory.createPieChart3D(getTitle(), getDataSet(tasks, from, to), true, false, false);
		PiePlot plot = (PiePlot) chart.getPlot();
		plot.setForegroundAlpha(0.5f);
		plot.setLabelGenerator(null);
		plot.setBackgroundPaint(Color.WHITE);
		return chart;
	}

	protected abstract PieDataset getDataSet(List<Task> tasks, Day from, Day to);
}

package net.roarsoftware.tracker.core.report;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;
import org.jfree.util.SortOrder;

import net.roarsoftware.tracker.model.Day;
import net.roarsoftware.tracker.model.GlobalTaskModel;
import net.roarsoftware.tracker.model.Task;
import net.roarsoftware.tracker.ui.TimeFormatter;

/**
 * @author Janni Kovacs
 */
public class WorkPerDayChartGenerator implements ChartGenerator {

	public String getTitle() {
		return "Work per Day";
	}

	public String getDescription() {
		return "Displays the amount of time spent each day for each task in a stacked bar chart.";
	}

	public JFreeChart generateChart(List<Task> tasks, Day from, Day to) {
		Comparable NULL = new Comparable() {
			public int compareTo(Object o) {
				return o == this ? 0 : 1;
			}
		};
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		int numDays = from.totalDays(to);
		for (int i = 0; i < numDays; i++) {
			dataset.addValue(0, NULL, from.move(i));
		}
		long averageWorkTimePerDay = 0;
		GlobalTaskModel model = GlobalTaskModel.getInstance();
		for (Task task : tasks) {
			for (Map.Entry<Day, Long> entry : task.getWorkPerDay().entrySet()) {
				Day date = entry.getKey();
				if (!model.isDateFilterActive() || model.getDateFilter().contains(date)) {
					String s = TimeFormatter.formatDateShort(date.getDate());
					dataset.setValue(entry.getValue() / 1000d / 60d / 60d, task.getDescription(), date);
					averageWorkTimePerDay += entry.getValue();
				}
			}
		}
		dataset.removeRow(NULL);
		averageWorkTimePerDay /= numDays;
		JFreeChart chart = ChartFactory
				.createStackedBarChart3D(getTitle(), "date", "hours", dataset, PlotOrientation.VERTICAL, true, false,
						false);
		CategoryPlot plot = chart.getCategoryPlot();
		plot.setForegroundAlpha(0.75f);
		plot.setBackgroundPaint(Color.WHITE);
		plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
		plot.getDomainAxis().setCategoryMargin(0.05);
		plot.setColumnRenderingOrder(SortOrder.DESCENDING);
		plot.setRowRenderingOrder(SortOrder.DESCENDING);
		TickUnits tickUnits = new TickUnits();
		tickUnits.add(new NumberTickUnit(1));
		plot.getRangeAxis().setStandardTickUnits(tickUnits);
		StackedBarRenderer r = new StackedBarRenderer();
//		r.setTotalFormatter(new DecimalFormat("#.#"));
		r.setShadowVisible(false);
		r.setBarPainter(new StandardBarPainter());
		plot.setRenderer(r);
		Marker marker = new ValueMarker(averageWorkTimePerDay / 1000d / 60d / 60d);
		marker.setLabel("Average");
		marker.setLabelAnchor(RectangleAnchor.LEFT);
		marker.setLabelTextAnchor(TextAnchor.BOTTOM_LEFT);
		marker.setLabelFont(marker.getLabelFont().deriveFont(11f));
		plot.addRangeMarker(marker, Layer.FOREGROUND);

		return chart;
	}
}

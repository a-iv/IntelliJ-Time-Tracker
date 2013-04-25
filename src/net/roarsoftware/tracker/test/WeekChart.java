package net.roarsoftware.tracker.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;
import org.jfree.util.SortOrder;

import net.roarsoftware.tracker.model.Day;
import net.roarsoftware.tracker.ui.TimeFormatter;

/**
 * @author Janni Kovacs
 */
public class WeekChart {

	private static class ITask {

		private Map<Day, Long> workTime = new HashMap<Day, Long>();
		private String name;

		public ITask(String s) {
			name = s;
		}
	}

	public static void main(String[] args) throws InterruptedException {
		List<ITask> tasks = new ArrayList<ITask>();
		Day today = new Day();
		Day end = today.move(6);
		for (int i = 0; i < 3; i++) {
			ITask task = new ITask("task "+ i);
			for (int j = 0; j < 7; j++) {
				Day d = today.move(j);
				if(j != 3)
				task.workTime.put(d, (long) new Random((d.getTime() * System.currentTimeMillis()) % 375637187).nextInt(120));
			}
			tasks.add(task);
		}
		Comparable NULL = new Comparable() {
			public int compareTo(Object o) {
				return o == this ? 0 : 1;
			}
		};
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		int num = today.totalDays(end);
		for (int i = 0; i < num; i++) {
			dataset.addValue(3, NULL, today.move(i));
		}
		long averageWorkTimePerDay = 0;
		int numDays = 7;
		for (ITask task : tasks) {
			for (Map.Entry<Day, Long> entry : task.workTime.entrySet()) {
				Day date = entry.getKey();
				String s = TimeFormatter.formatDateShort(date.getDate());
				dataset.setValue(entry.getValue() / 60d, task.name, date);
				averageWorkTimePerDay += entry.getValue();
			}
		}
		dataset.removeRow(NULL);
		averageWorkTimePerDay /= numDays;
		JFreeChart chart = ChartFactory
				.createStackedBarChart("title", "day", "hours worked", dataset, PlotOrientation.VERTICAL, true, false, false);
		CategoryPlot plot = chart.getCategoryPlot();
		plot.getDomainAxis().setCategoryMargin(0.05);
		plot.setColumnRenderingOrder(SortOrder.DESCENDING);
		plot.setRowRenderingOrder(SortOrder.DESCENDING);
		TickUnits tickUnits = new TickUnits();
		tickUnits.add(new NumberTickUnit(1));
		plot.getRangeAxis().setStandardTickUnits(tickUnits);
//		r.setTotalFormatter(new DecimalFormat("#.#"));
		Marker marker = new ValueMarker(averageWorkTimePerDay / 60d);
		marker.setLabel("Average");
		marker.setLabelAnchor(RectangleAnchor.LEFT);
		marker.setLabelTextAnchor(TextAnchor.BOTTOM_LEFT);
		marker.setLabelFont(marker.getLabelFont().deriveFont(11f));
		plot.addRangeMarker(marker, Layer.FOREGROUND);
		ChartFrame f = new ChartFrame("ulla", chart);
		f.pack();
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}
}

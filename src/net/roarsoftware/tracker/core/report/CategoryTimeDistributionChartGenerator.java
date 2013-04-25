package net.roarsoftware.tracker.core.report;

import java.util.List;

import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import net.roarsoftware.tracker.model.Day;
import net.roarsoftware.tracker.model.Task;

/**
 * @author Janni Kovacs
 */
public class CategoryTimeDistributionChartGenerator extends TimeDistributionChartGenerator {

	@Override
	public String getDescription() {
		return super.getDescription() + "category.";
	}

	@Override
	public String getTitle() {
		return super.getTitle() + "category";
	}

	protected PieDataset getDataSet(List<Task> tasks, Day from, Day to) {
		DefaultPieDataset ds = new DefaultPieDataset();
		for (Task task : tasks) {
			String key = task.getCategory();
			long val = task.getDurationInBetween(from, to) + (ds.getIndex(key) != -1 ? ds.getValue(key)
					.longValue() : 0);
			ds.setValue(key, val);
		}
		return ds;
	}
}

package net.roarsoftware.tracker.core.report;

import java.util.List;

import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import net.roarsoftware.tracker.model.Day;
import net.roarsoftware.tracker.model.Task;

/**
 * @author Janni Kovacs
 */
public class TaskTimeDistributionChartGenerator extends TimeDistributionChartGenerator {

	@Override
	public String getDescription() {
		return super.getDescription() + "task.";
	}

	@Override
	public String getTitle() {
		return super.getTitle() + "task";
	}

	protected PieDataset getDataSet(List<Task> tasks, Day from, Day to) {
		DefaultPieDataset ds = new DefaultPieDataset();
		for (Task task : tasks) {
			ds.setValue(task.getDescription(), task.getDurationInBetween(from, to));
		}
		return ds;
	}
}

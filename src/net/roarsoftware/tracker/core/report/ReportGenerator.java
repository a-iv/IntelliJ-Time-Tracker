package net.roarsoftware.tracker.core.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import net.roarsoftware.tracker.core.filters.DateFilter;
import net.roarsoftware.tracker.core.filters.TaskFilterInfo;
import net.roarsoftware.tracker.model.Day;
import net.roarsoftware.tracker.model.GlobalTaskModel;
import net.roarsoftware.tracker.model.Task;
import net.roarsoftware.tracker.ui.TimeFormatter;
import static net.roarsoftware.tracker.ui.TimeFormatter.formatDate;
import net.roarsoftware.xml.builder.Document;
import net.roarsoftware.xml.builder.Element;

/**
 * @author Janni Kovacs
 */
public class ReportGenerator {

	public static boolean generateReport(String name, File targetFile, Collection<TaskFilterInfo> filters,
										 ChartGenerator... chartGenerators) {
		GlobalTaskModel model = GlobalTaskModel.getInstance();
		Day firstDay = null, lastDay = null;
		DateFilter dateFilter = model.getDateFilter();
		if (model.isDateFilterActive()) {
			firstDay = dateFilter.getStart();
			lastDay = dateFilter.getEnd();
		}
		List<Task> tasks = new ArrayList<Task>();
		taskLoop:
		for (Task task : model.getAllTasks()) {
			// find first and last day
			if (!model.isDateFilterActive()) {
				if (task.getDateStarted() != null && (firstDay == null || task.getDateStarted().before(firstDay))) {
					firstDay = task.getDateStarted();
				}
				if (task.getDateFinished() != null && (lastDay == null || task.getDateFinished().before(lastDay))) {
					lastDay = task.getDateFinished();
				}
			}
			for (TaskFilterInfo filter : filters) {
				if (!filter.getFilter().accept(task))
					continue taskLoop;
			}
			if (model.isDateFilterActive() && !dateFilter.accept(task)) {
				System.out.println("dont accept " + task.getDescription());
				System.out.println(task.getDateStarted() + " - " + task.getDateFinished());
				System.out.println(model.getDateFilter().getStart() + " - " + model.getDateFilter().getEnd());
				continue;
			}
			tasks.add(task);
		}
		if (firstDay == null)
			firstDay = Day.today();
		if (lastDay == null)
			lastDay = Day.today();

		Document html = Document.newDocument("html");
		html.addChild("head").addChild("title", "Work report");
		Element body = html.addChild("body");
		body.addChild("h1", "Work report");
		Element table = body.addChild("table");
		Element tr = table.addChild("tr");
		tr.addChild("td").addChild("u", "Description:");
		tr.addChild("td").setText(name);
//		if (model.isDateFilterActive()) {
		tr = table.addChild("tr");
		tr.addChild("td").addChild("u", "Period:");
		tr.addChild("td").setText(
				formatDate(firstDay.getDate()) + " - " + formatDate(lastDay.getDate()) + " (" + firstDay
						.totalDays(lastDay) + " days)");
//		}
		tr = table.addChild("tr");
		tr.addChild("td").addChild("u", "Generated at:");
		tr.addChild("td").setText(TimeFormatter.formatDate(new Date()));
		body.addChild("br");
		// create charts
		for (int i = 0; i < chartGenerators.length; i++) {
			ChartGenerator chartGenerator = chartGenerators[i];
			JFreeChart chart = chartGenerator.generateChart(tasks, firstDay, lastDay);
			String fileName = "chart" + i + ".png";
			try {
				ChartUtilities.saveChartAsPNG(new File(targetFile.getParentFile(), fileName), chart, 600, 450);
			} catch (IOException e) {
			}
			body.addChild("img").addAttribute("src", fileName);
			body.addChild("br");
		}
		if (filters.size() != 0) {
			body.addChild("u", "Applied filters:").addChild("br");
			Element ul = body.addChild("ul");
			for (TaskFilterInfo filter : filters) {
				ul.addChild("li").addText(filter.getFactory().getName() + " " + filter.getRule() + " ")
						.addChild("b", filter.getValue().toString());
			}
		}
		body.addChild("u", "Tasks:").addChild("br");
		table = body.addChild("table").addAttribute("border", "1").addAttribute("width", "60%");
		table.addChild("tr").addAttribute("bgcolor", "CCCCCC").addChild("th", "Priority").addChild("th", "Project")
				.addChild("th", "Category")
				.addChild("th", "Description").addChild("th", "Progress").addChild("th", "Time*");
		long totalTime = 0;
		NumberFormat percentageFormat = NumberFormat.getPercentInstance();
		String green = "#6dc965", red = "#c96565";
		for (Task task : tasks) {
			long duration;
			if (model.isDateFilterActive()) {
				duration = task.getDurationInBetween(firstDay, lastDay);
			} else {
				duration = task.getTotalDuration();
			}
			totalTime += duration;
			tr = table.addChild("tr");
			tr.addChild("td", task.getPriority().toString()).addChild("td", task.getProject())
					.addChild("td", task.getCategory()).addChild("td", task.getDescription());
			double progress = task.getMaxDuration() == -1 ? task.getProgress() : (double) task.getTotalDuration() / task
					.getMaxDuration();
			String color = progress <= 1 ? green : red;
			Element progressTd = tr.addChild("td");
			progressTd.addAttribute("align", "center")
					.addAttribute("style", "background-color: " + color).setText(percentageFormat.format(progress));
			if (task.getMaxDuration() != -1) {
				progressTd.addAttribute("title", "Maximum time for this task was " + TimeFormatter.format(task.getMaxDuration()));
			}
			tr.addChild("td", TimeFormatter.format(duration));
		}
		body.addChild("u", "Total Time:").addText(" " + TimeFormatter.format(totalTime) + "*").addChild("br");
		body.addText("* Only time spent in the selected period is included");

		try {
			html.write(new FileOutputStream(targetFile));
		} catch (IOException e) {
			return false;
		}
		return true;
	}

}

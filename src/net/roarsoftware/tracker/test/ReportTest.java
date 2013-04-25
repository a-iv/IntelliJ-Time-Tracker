package net.roarsoftware.tracker.test;

import java.io.File;
import java.util.Collections;
import java.util.Random;

import net.roarsoftware.tracker.core.report.ChartGeneratorFactory;
import net.roarsoftware.tracker.core.report.ReportGenerator;
import net.roarsoftware.tracker.model.Day;
import net.roarsoftware.tracker.model.GlobalTaskModel;
import net.roarsoftware.tracker.model.Priority;
import net.roarsoftware.tracker.model.Task;

/**
 * @author Janni Kovacs
 */
public class ReportTest {

	public static void main(String[] args) {
		GlobalTaskModel model = GlobalTaskModel.getInstance();
		Day today = new Day();
		Day first = today.move(-6);
		for (int i = 0; i < 3; i++) {
			Task task = new Task("Task " + i, i == 1 ? "core" : "ui", i == 0 ? "Project 1" : "Project 2", Priority.MEDIUM);
			task.setDateStarted(first);
			for (int j = -6; j <= 0; j++) {
				Day d = today.move(j);
				if (j != -3)
					task.addDuration(d,
							(long) new Random(System.nanoTime() * 2481 % System.currentTimeMillis())
									.nextInt(120) * 1000 * 60);
			}
			System.out.println(task.getDescription() + ": " + task.getTotalDuration());
			model.addTask(task);
		}
//		model.setDateFilterActive(true);
		model.setDateRangeStart(today.move(-3));
		model.setDateRangeEnd(today.move(-1));
		System.out.println("generieren");
		ReportGenerator.generateReport("test", new File("C:\\Dokumente und Einstellungen\\JRoar\\Desktop\\test.html"),
				Collections.EMPTY_LIST, ChartGeneratorFactory.getInstance().getChartGenerators());
		System.out.println("fertig");
	}
}

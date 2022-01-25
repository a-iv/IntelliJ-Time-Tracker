package net.roarsoftware.tracker;

import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerAdapter;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManagerAdapter;
import com.intellij.ui.content.ContentManagerEvent;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import net.roarsoftware.tracker.core.filters.DateFilter;
import net.roarsoftware.tracker.core.filters.FilterFactory;
import net.roarsoftware.tracker.core.filters.TaskFilterInfo;
import net.roarsoftware.tracker.model.Day;
import net.roarsoftware.tracker.model.GlobalTaskModel;
import net.roarsoftware.tracker.model.Priority;
import net.roarsoftware.tracker.model.State;
import net.roarsoftware.tracker.model.Task;
import net.roarsoftware.tracker.ui.HistoryWindow;
import net.roarsoftware.tracker.ui.TrackerWindow;

/**
 * @author Janni Kovacs
 */
public class TimeTrackerPlugin implements ApplicationComponent, JDOMExternalizable {
	public static final String TOOL_WINDOW_ID = "Time Tracker";
	private static final Icon TOOL_WINDOW_ICON = new ImageIcon(TimeTrackerPlugin.class.getResource("ui/res/tt.png"));

	public TimeTrackerPlugin() {
	}

	public void initComponent() {
		ProjectManager pm = ProjectManager.getInstance();
		Project[] projects = pm.getOpenProjects();
		for (Project project : projects) {
			projectOpened(project);
		}
		pm.addProjectManagerListener(new ProjectManagerAdapter() {
			public void projectOpened(Project project) {
				TimeTrackerPlugin.this.projectOpened(project);
			}

			public void projectClosed(Project project) {
				TimeTrackerPlugin.this.projectClosed(project);
			}
		});
		GlobalTaskModel.getInstance().start();
	}

	public void disposeComponent() {
		GlobalTaskModel model = GlobalTaskModel.getInstance();
		model.setWorking(false);
		model.stop();
	}

	@NotNull
	public String getComponentName() {
		return "TimeTrackerPlugin";
	}

	public void projectOpened(Project project) {
//		VcsConfiguration configuration = VcsConfiguration.getInstance(project);
//		System.out.println("configuration = " + configuration);
//		if (configuration != null) {
//			ArrayList<String> list = configuration.getRecentMessages();
//			System.out.println("recent messages:");
//			for (String s : list) {
//				System.out.println("\t"+s);
//			}
//		}
		ToolWindowManager m = ToolWindowManager.getInstance(project);

		ToolWindow window = m.registerToolWindow(TOOL_WINDOW_ID, false, ToolWindowAnchor.BOTTOM);
		window.setIcon(TOOL_WINDOW_ICON);
		TrackerWindow trackerWindow = new TrackerWindow(project);
		GlobalTaskModel.getInstance().addTaskListener(trackerWindow);
		final HistoryWindow historyWindow = new HistoryWindow(project);
		GlobalTaskModel.getInstance().addTaskListener(historyWindow);

		Content content = ContentFactory.SERVICE.getInstance().createContent(trackerWindow, "Tasks", false);
		window.getContentManager().addContent(content);
		content = ContentFactory.SERVICE.getInstance().createContent(historyWindow, "History", false);
		window.getContentManager().addContent(content);
		window.getContentManager().addContentManagerListener(new ContentManagerAdapter() {
			@Override
			public void selectionChanged(ContentManagerEvent event) {
				if (event.getIndex() == 1) {
					historyWindow.update();
				}
			}
		});
	}

	public void projectClosed(Project project) {
		ToolWindowManager m = ToolWindowManager.getInstance(project);
		GlobalTaskModel.getInstance().removeProjectTaskListeners(project);
		m.unregisterToolWindow(TOOL_WINDOW_ID);
	}

	public void readExternal(Element element) throws InvalidDataException {
		GlobalTaskModel model = GlobalTaskModel.getInstance();
/*		// generating dummy task data:
		Day today = new Day();
		Day first = today.move(-6);
		for (int i = 0; i < 3; i++) {
			Task task = new Task("Task " + i, i == 1 ? "core" : "ui", "jackage", Priority.MEDIUM);
			task.setDateStarted(first);
			for (int j = -6; j <= 0; j++) {
				Day d = today.move(j);
				if (j != -3)
					task.addDuration(d,
							(long) new Random((d.getTime() * System.nanoTime()) % 375637187).nextInt(120) * 1000 * 60);
			}
			System.out.println(task.getDescription() +": "+ task.getTotalDuration());
			model.addTask(task);
		}
*/
		Element taskList = element.getChild("taskList");
		if (taskList != null) {
			for (Object o : taskList.getChildren("task")) {
				Element e = (Element) o;
				Task t = readTask(e);
				model.addTask(t);
			}
			model.setCurrentTask(model.findTask(element.getChild("currentTask").getAttributeValue("description")));
			model.setIdlingTime(Long.parseLong(element.getChildText("idlingTime")));
			model.setGlobalTime(Long.parseLong(element.getChildText("globalTime")));
		}
		Element fe = element.getChild("filters");
		if (fe != null) {
			for (Object o : fe.getChildren("filter")) {
				Element e = (Element) o;
				FilterFactory factory = FilterFactory.getFactory(e.getAttributeValue("name"));
				FilterFactory.Rule rule = FilterFactory.Rule.valueOf(e.getAttributeValue("rule"));
				String valueString = e.getAttributeValue("value");
				Object value = valueString;
				if (factory.getName().equals("Priority"))
					value = Priority.valueOf(valueString.toUpperCase());
				else if (factory.getName().equals("State"))
					value = State.valueOf(valueString.toUpperCase());
				model.addFilter(new TaskFilterInfo(factory.createFilter(rule, value), factory, rule, value));
			}
		}
		Element dateRange = element.getChild("dateRange");
		if (dateRange != null) {
			model.setDateRangeStart(new Day(Long.parseLong(dateRange.getAttributeValue("start"))));
			model.setDateRangeEnd(new Day(Long.parseLong(dateRange.getAttributeValue("end"))));
			model.setDateFilterActive(Boolean.parseBoolean(dateRange.getAttributeValue("enabled")));
		}

	}

	public void writeExternal(Element element) throws WriteExternalException {
		GlobalTaskModel model = GlobalTaskModel.getInstance();
		Element taskList = new Element("taskList");
		element.addContent(taskList);
		for (Task task : model.getAllTasks()) {
			writeTask(taskList, task);
		}
		if (model.getCurrentTask() == null)
			element.addContent(new Element("currentTask"));
		else
			element.addContent(
					new Element("currentTask").setAttribute("description", model.getCurrentTask().getDescription()));
		element.addContent(new Element("idlingTime").addContent(String.valueOf(model.getIdlingTime())));
		element.addContent(new Element("globalTime").addContent(String.valueOf(model.getGlobalTime())));
		Element fe = new Element("filters");
		for (TaskFilterInfo filter : model.getFilters()) {
			fe.addContent(new Element("filter").setAttribute("name", filter.getFactory().getName()).setAttribute("rule",
					filter.getRule().name()).setAttribute("value", filter.getValue().toString()));
		}
		element.addContent(fe);
		Element dateRange = new Element("dateRange");
		DateFilter dateFilter = model.getDateFilter();
		dateRange.setAttribute("start", String.valueOf(dateFilter.getStart().getTime()))
				.setAttribute("end", String.valueOf(dateFilter.getEnd().getTime()))
				.setAttribute("enabled", String.valueOf(model.isDateFilterActive()));
		element.addContent(dateRange);
	}

	private Task readTask(Element e) {
		if (e.getAttributes().size() == 0)
			return null;
		String description = e.getAttributeValue("description");
		String category = e.getAttributeValue("category");
		String project = e.getAttributeValue("project");
		Priority priority = Priority.valueOf(e.getAttributeValue("priority"));
		long duration = Long.parseLong(e.getAttributeValue("totalDuration"));
		long maxDuration = -1;
		if (e.getAttributeValue("maxDuration") != null)
			maxDuration = Long.parseLong(e.getAttributeValue("maxDuration"));
		Task task = new Task(description, category, project, priority, maxDuration);
		if (e.getAttributeValue("progress") != null)
			task.setProgress(Double.valueOf(e.getAttributeValue("progress")));
		for (Object o : e.getChildren("workday")) {
			Element workday = (Element) o;
			Day day = new Day(Long.parseLong(workday.getAttributeValue("day")));
			long dayDuration = Long.parseLong(workday.getAttributeValue("duration"));
			task.addDuration(day, dayDuration);
		}
//		if (duration != task.getTotalDuration()) {
//		task.setTotalDuration(duration);
//		}
		Element startDate = e.getChild("startDate");
		if (startDate != null) {
			task.setDateStarted(new Day(Long.parseLong(startDate.getText())));
		}
		Element finishDate = e.getChild("finishDate");
		if (finishDate != null) {
			task.setDateFinished(new Day(Long.parseLong(finishDate.getText())));
		}
		return task;
	}

	private void writeTask(Element element, Task task) {
		Element child = new Element("task");
		if (task != null) {
			child.setAttribute("description", task.getDescription());
			child.setAttribute("category", task.getCategory());
			child.setAttribute("priority", task.getPriority().name());
			child.setAttribute("project", task.getProject());
			child.setAttribute("totalDuration", String.valueOf(task.getTotalDuration()));
			child.setAttribute("maxDuration", String.valueOf(task.getMaxDuration()));
			child.setAttribute("progress", String.valueOf(task.getProgress()));
			Map<Day, Long> workPerDay = task.getWorkPerDay();
			for (Map.Entry<Day, Long> entry : workPerDay.entrySet()) {
				Element day = new Element("workday");
				day.setAttribute("day", String.valueOf(entry.getKey().getTime()));
				day.setAttribute("duration", String.valueOf(entry.getValue()));
				child.addContent(day);
			}
			if (task.getDateStarted() != null) {
				child.addContent(new Element("startDate").addContent(String.valueOf(task.getDateStarted().getTime())));
			}
			if (task.getDateFinished() != null) {
				child.addContent(
						new Element("finishDate").addContent(String.valueOf(task.getDateFinished().getTime())));
			}
		}
		element.addContent(child);
	}
}

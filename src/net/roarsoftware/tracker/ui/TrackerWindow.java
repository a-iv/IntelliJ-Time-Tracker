package net.roarsoftware.tracker.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.Icons;

import net.roarsoftware.tracker.TimeTrackerPlugin;
import net.roarsoftware.tracker.core.filters.DateFilter;
import net.roarsoftware.tracker.core.filters.NegationFilter;
import net.roarsoftware.tracker.core.filters.ProjectFilter;
import net.roarsoftware.tracker.core.filters.StateFilter;
import net.roarsoftware.tracker.core.filters.TaskFilter;
import net.roarsoftware.tracker.model.Day;
import net.roarsoftware.tracker.model.GlobalTaskModel;
import net.roarsoftware.tracker.model.State;
import net.roarsoftware.tracker.model.Task;
import net.roarsoftware.tracker.model.TaskListener;

/**
 * @author Janni Kovacs
 */
public class TrackerWindow extends JPanel implements TaskListener, ActionListener {
	private JLabel currentTaskLabel, durationLabel;
	private TaskTableModel model;

	private JButton work, pause, done, idle;
	private JTable table;

	private Project project;

	public TrackerWindow(final Project project) {
		super(new BorderLayout());
		this.project = project;

		currentTaskLabel = new JLabel();
		currentTaskLabel.setFont(currentTaskLabel.getFont().deriveFont(Font.BOLD));
		durationLabel = new JLabel("idling");
		durationLabel.setFont(durationLabel.getFont().deriveFont(Font.BOLD));
		JPanel northWest = new JPanel(new FlowLayout(FlowLayout.LEFT));
		northWest.add(new JLabel("Current task: "));
		northWest.add(currentTaskLabel);
		work = new JButton("Work");
		work.addActionListener(this);
		pause = new JButton("Pause");
		pause.addActionListener(this);
		done = new JButton("Done");
		done.addActionListener(this);
		northWest.add(work);
		northWest.add(pause);
		northWest.add(done);
		northWest.add(new JLabel("Duration: "));
		northWest.add(durationLabel);
		JPanel north = new JPanel(new BorderLayout(0, 0));
		north.add(northWest, BorderLayout.WEST);
		idle = new JButton("Idle");
		idle.addActionListener(this);
		JPanel northEast = new JPanel();
		northEast.add(idle);
		north.add(northEast, BorderLayout.EAST);
		add(north, BorderLayout.NORTH);

		model = new TaskTableModel();
		table = new JTable(model);
		TableColumnModel cm = table.getColumnModel();
		cm.getColumn(0).setPreferredWidth(100);
		cm.getColumn(1).setPreferredWidth(100);
		cm.getColumn(2).setPreferredWidth(100);
		cm.getColumn(3).setPreferredWidth(600);
		cm.getColumn(4).setPreferredWidth(100);
		cm.getColumn(5).setPreferredWidth(100);
		cm.getColumn(6).setPreferredWidth(100);
		table.setRowSorter(new TableRowSorter<TableModel>(model));
		TaskTableRenderer taskTableRenderer = new TaskTableRenderer(model);
		table.setDefaultRenderer(Object.class, taskTableRenderer);
		table.setDefaultRenderer(Long.class, taskTableRenderer);
		table.setDefaultRenderer(Double.class, taskTableRenderer);
		table.setDefaultEditor(Double.class, new ProgressCellEditor());
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && getSelectedRow() != -1) {
					GlobalTaskModel.getInstance().setCurrentTask(model.getTask(getSelectedRow()));
				}
			}
		});
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JPanel center = new JPanel(new BorderLayout());
		center.add(new JScrollPane(table));
		DefaultActionGroup group = new DefaultActionGroup();
		group.add(new FilterThisProjectAction());
		group.add(new HideDoneTasksAction());
		group.addSeparator();
		group.add(new AddTaskAction());
		group.add(new RemoveTaskAction());
		group.add(new EditTaskAction());
		ActionToolbar toolBar = ActionManager.getInstance()
				.createActionToolbar(TimeTrackerPlugin.TOOL_WINDOW_ID, group, false);
		center.add(toolBar.getComponent(), BorderLayout.WEST);
		add(center);

//		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
//		select = new JButton("Select");
//		select.addActionListener(this);
//		select.setEnabled(false);
//		bottom.add(select);
//		add(bottom, BorderLayout.SOUTH);

		setCurrentTask(GlobalTaskModel.getInstance().getCurrentTask());
		for (Task task : GlobalTaskModel.getInstance().getAllTasks()) {
			taskAdded(task);
		}
	}

	private int getSelectedRow() {
		int row = table.getSelectedRow();
		if (row == -1)
			return -1;
		return table.getRowSorter().convertRowIndexToModel(row);
	}

	public void dispose() {
	}

	public void taskAdded(Task t) {
		model.fireTableDataChanged();
	}

	public void taskRemoved(Task t) {
		model.fireTableDataChanged();
	}

	public void setCurrentTask(Task t) {
		if (t == null) {
			currentTaskLabel.setText("none");
			work.setEnabled(false);
			pause.setEnabled(false);
			done.setEnabled(false);
			durationLabel.setText("idling");
		} else {
			currentTaskLabel.setText(t.getDescription());
			boolean isDone = t.getState() == State.DONE;
			boolean isWorking = GlobalTaskModel.getInstance().isWorking();
			work.setEnabled(!isWorking);
			pause.setEnabled(isWorking);
			done.setEnabled(!isDone);
			updateDurationLabel(t.getTotalDuration());
		}
	}

	private void updateDurationLabel(long duration) {
		durationLabel.setText(TimeFormatter.format(duration));
	}

	public void setWorking(boolean working) {
		Task currentTask = GlobalTaskModel.getInstance().getCurrentTask();
		work.setEnabled(!working);
		pause.setEnabled(working);
		done.setEnabled(currentTask.getState() != State.DONE);
		this.model.fireTableDataChanged();
	}

	public void durationChanged(Task t, long duration) {
		if (t != null) {
			updateDurationLabel(duration);
			model.taskDurationChanged(t);
		}
	}

	public void dateRangeChanged(DateFilter dateFilter) {
	}

	public void dateFilterActivated(boolean dateFilterActive) {
	}

	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		GlobalTaskModel model = GlobalTaskModel.getInstance();
		Task task = model.getCurrentTask();
		if (s == work) {
			if (task.getDateStarted() == null) {
				task.setDateStarted(Day.today());
			}
			if (task.getDateFinished() != null) {
				task.setDateFinished(null);
			}
			model.setWorking(true);
		} else if (s == pause) {
			model.setWorking(false);
		} else if (s == done) {
			Day date = new Day();
			if (task.getDateStarted() == null) {
				task.setDateStarted(date);
			}
			task.setDateFinished(date);
			model.setWorking(false);
		} else if (s == idle) {
			GlobalTaskModel taskModel = GlobalTaskModel.getInstance();
			taskModel.setWorking(false);
			taskModel.setCurrentTask(null);
		}
		this.model.fireTableDataChanged();
	}

	private class AddTaskAction extends AnAction {
		public AddTaskAction() {
			super("Add Task", "Adds a new Task", Icons.ADD_ICON);
		}

		public void actionPerformed(AnActionEvent e) {
			AddTaskDialog d = new AddTaskDialog((JFrame) SwingUtilities.windowForComponent(TrackerWindow.this));
			d.setVisible(true);
			Task task = d.getTask();
			if (task != null) {
				GlobalTaskModel.getInstance().addTask(task);
			}
		}
	}

	private class EditTaskAction extends AnAction {
		public EditTaskAction() {
			super("Edit Task", "Edits an existing Task", IconLoader.findIcon("/modules/edit.png"));
		}

		public void actionPerformed(AnActionEvent e) {
			int row = getSelectedRow();
			if (row != -1) {
				Task oldTask = model.getTask(row);
				AddTaskDialog d = new AddTaskDialog((JFrame) SwingUtilities.windowForComponent(TrackerWindow.this));
				d.setTask(oldTask);
				d.setVisible(true);
				Task newTask = d.getTask();
				if (newTask != null) {
					GlobalTaskModel taskModel = GlobalTaskModel.getInstance();
					taskModel.addTask(newTask);
					if (taskModel.getCurrentTask() == oldTask) {
						boolean wasWorking = taskModel.isWorking();
						taskModel.setCurrentTask(newTask);
						if(wasWorking)
							taskModel.setWorking(true);
					}
					taskModel.removeTask(oldTask);
				}
			}
		}

		@Override
		public void update(AnActionEvent e) {
			e.getPresentation().setEnabled(table.getSelectedRow() != -1);
		}
	}

	private class RemoveTaskAction extends AnAction {
		public RemoveTaskAction() {
			super("Remove Task", "Removes the selected Task", Icons.DELETE_ICON);
		}

		public void actionPerformed(AnActionEvent e) {
			if (getSelectedRow() != -1) {
				GlobalTaskModel model = GlobalTaskModel.getInstance();
				Task selectedTask = TrackerWindow.this.model.getTask(getSelectedRow());
				if (selectedTask == model.getCurrentTask()) {
					model.setWorking(false);
					model.setCurrentTask(null);
				}
				model.removeTask(selectedTask);
			}
		}

		@Override
		public void update(AnActionEvent e) {
			e.getPresentation().setEnabled(table.getSelectedRow() != -1);
		}
	}

	private class FilterThisProjectAction extends ToggleAction {
		private TaskFilter filter = new ProjectFilter(project.getName());

		private FilterThisProjectAction() {
			super("Show only tasks for this project", "Hides all tasks for other projects",
					IconLoader.findIcon("/nodes/project.png"));
		}

		public boolean isSelected(AnActionEvent e) {
			return model.containsFilter(filter);
		}

		public void setSelected(AnActionEvent e, boolean state) {
			if (state)
				model.addFilter(filter);
			else
				model.removeFilter(filter);
		}
	}

	private class HideDoneTasksAction extends ToggleAction {
		private TaskFilter filter = new NegationFilter(new StateFilter(State.DONE));

		private HideDoneTasksAction() {
			super("Hide done tasks", "Hides all tasks that are done", IconLoader.findIcon("/gutter/check.png"));
		}

		public boolean isSelected(AnActionEvent e) {
			return model.containsFilter(filter);
		}

		public void setSelected(AnActionEvent e, boolean state) {
			if (state)
				model.addFilter(filter);
			else
				model.removeFilter(filter);
		}
	}

}

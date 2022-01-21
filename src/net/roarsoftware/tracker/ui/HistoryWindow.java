package net.roarsoftware.tracker.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.Icons;
import com.toedter.calendar.JDateChooser;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import net.roarsoftware.tracker.TimeTrackerPlugin;
import net.roarsoftware.tracker.core.filters.DateFilter;
import net.roarsoftware.tracker.core.filters.TaskFilterInfo;
import net.roarsoftware.tracker.core.report.ReportGenerator;
import net.roarsoftware.tracker.model.Day;
import net.roarsoftware.tracker.model.GlobalTaskModel;
import net.roarsoftware.tracker.model.Priority;
import net.roarsoftware.tracker.model.Task;
import net.roarsoftware.tracker.model.TaskListener;

/**
 * @author Janni Kovacs
 */
public class HistoryWindow extends JPanel implements TaskListener {

	private static final Icon TODO_DEFAULT = IconLoader.findIcon("/general/todoDefault.png");
	private static final Icon TODO_IMPORTANT = IconLoader.findIcon("/general/todoImportant.png");

	private DefaultMutableTreeNode root;
	private DefaultTreeModel treeModel;
	private JTable filteredTasksTable;
	private TaskTableModel tableModel;
	private TaskFilterInfoModel filtersTableModel;
	private JTable filtersTable;
	private ChartPanel chartPanel;
	private Map<String, DefaultMutableTreeNode> projectNodes;
	private Map<String, Map<String, DefaultMutableTreeNode>> categoryNodes;
	private Map<Task, DefaultMutableTreeNode> taskNodes;
	private NodeInfo rootInfo;
	private NodeInfo idleInfo;
	private JTree tree;
	private JDateChooser startDateChooser;
	private JDateChooser endDateChooser;
	private JCheckBox dateFilterActive;

	public HistoryWindow() {
		super(new BorderLayout());

		root = new DefaultMutableTreeNode();
		treeModel = new DefaultTreeModel(root);
		tree = new JTree(treeModel);
		tree.setCellRenderer(new DefaultTreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
														  boolean leaf,
														  int row, boolean hasFocus) {
				super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
				Object obj = node.getUserObject();
				if (obj instanceof NodeInfo) {
					NodeInfo info = (NodeInfo) obj;
					setText(info + " [" + TimeFormatter.format(info.getDuration()) + "]");
					if (info.getTask() != null) {
						Priority p = info.getTask().getPriority();
						if(p == Priority.HIGH)
							setIcon(TODO_IMPORTANT);
						else
							setIcon(TODO_DEFAULT);
					}
				}
				if (row == 1) {
					setIcon(getClosedIcon());
				}
				return this;
			}
		});

		chartPanel = new ChartPanel(null, false, false, false, false, false);
		JPanel projectsPanel = new JPanel(new BorderLayout());
		projectsPanel.add(new JScrollPane(tree));
		projectsPanel.add(chartPanel, BorderLayout.EAST);

		tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				TreePath selectionPath = tree.getSelectionPath();
				if (selectionPath != null) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
					Object obj = node.getUserObject();
					if (obj instanceof NodeInfo) {
						NodeInfo info = (NodeInfo) obj;
						JFreeChart chart = ChartFactory.createPieChart3D(null, info.getDataSet(), true, true, false);
						PiePlot plot = (PiePlot) chart.getPlot();
						plot.setForegroundAlpha(0.5f);
						plot.setLabelGenerator(null);
						plot.setBackgroundPaint(Color.WHITE);
						chartPanel.setChart(chart);
					}
				}
			}
		});

		startDateChooser = new JDateChooser(new Date());
		endDateChooser = new JDateChooser(new Date());
		startDateChooser.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				super.focusLost(e);
				GlobalTaskModel.getInstance().setDateRangeStart(new Day(startDateChooser.getDate()));
			}
		});
		endDateChooser.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				super.focusLost(e);
				GlobalTaskModel.getInstance().setDateRangeEnd(new Day(endDateChooser.getDate()));
			}
		});
		JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));
		dateFilterActive = new JCheckBox("Select tasks from: ");
		dateFilterActive.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GlobalTaskModel.getInstance().setDateFilterActive(dateFilterActive.isSelected());
			}
		});
		north.add(dateFilterActive);
		north.add(startDateChooser);
		north.add(new JLabel("to: "));
		north.add(endDateChooser);
		north.add(new JButton(new AbstractAction("Generate report") {
			public ReportSettingsDialog d;

			public void actionPerformed(ActionEvent e) {
				if(d == null)
					d = new ReportSettingsDialog((JFrame) SwingUtilities.windowForComponent(HistoryWindow.this));
				d.setVisible(true);
				if (d.ok()) {
					boolean ok = false;
					if (d.getName().length() != 0 && d.getPath().length() != 0) {
						ok = ReportGenerator.generateReport(d.getName(), new File(d.getPath()), filtersTableModel.getFilters(),
								d.getCharts());
					}
					if (!ok) {
						JOptionPane.showMessageDialog(null, "Report generation was not successful.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}));

		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		tableModel = new TaskTableModel();
		tableModel.setEditable(false);
		filteredTasksTable = new JTable(tableModel);
		TableColumnModel cm = filteredTasksTable.getColumnModel();
		cm.getColumn(0).setPreferredWidth(100);
		cm.getColumn(1).setPreferredWidth(100);
		cm.getColumn(2).setPreferredWidth(100);
		cm.getColumn(3).setPreferredWidth(600);
		cm.getColumn(4).setPreferredWidth(100);
		cm.getColumn(5).setPreferredWidth(100);
		cm.getColumn(6).setPreferredWidth(100);
		filteredTasksTable.setRowSorter(new TableRowSorter<TableModel>(tableModel));
		TableCellRenderer renderer = new TaskTableRenderer(tableModel);
		filteredTasksTable.setDefaultRenderer(Object.class, renderer);
		filteredTasksTable.setDefaultRenderer(Long.class, renderer);
		filteredTasksTable.setDefaultRenderer(Double.class, renderer);

		JPanel filtersPanel = new JPanel(new BorderLayout());
		filtersTableModel = new TaskFilterInfoModel();
		filtersTable = new JTable(filtersTableModel);
		filtersTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		DefaultActionGroup group = new DefaultActionGroup();
		group.add(new AddFilterAction());
		group.add(new RemoveFilterAction());
		group.add(new EditFilterAction());
		ActionToolbar toolBar = ActionManager.getInstance()
				.createActionToolbar(TimeTrackerPlugin.TOOL_WINDOW_ID, group, false);
		toolBar.setTargetComponent(startDateChooser);
		filtersPanel.add(new JLabel("Filters:"), BorderLayout.NORTH);
		filtersPanel.add(toolBar.getComponent(), BorderLayout.WEST);
		filtersPanel.add(new JScrollPane(filtersTable));
		JPanel tasksTablePanel = new JPanel(new BorderLayout());
		tasksTablePanel.add(new JLabel("Tasks:"), BorderLayout.NORTH);
		tasksTablePanel.add(new JScrollPane(filteredTasksTable));
		split.setTopComponent(filtersPanel);
		split.setBottomComponent(tasksTablePanel);
		JPanel tasksPanel = new JPanel(new BorderLayout());
		tasksPanel.add(split);
		tasksPanel.add(north, BorderLayout.NORTH);

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Projects", projectsPanel);
		tabs.addTab("Tasks", tasksPanel);
		add(tabs);

		update();

		projectNodes = new HashMap<String, DefaultMutableTreeNode>();
		categoryNodes = new HashMap<String, Map<String, DefaultMutableTreeNode>>();
		taskNodes = new HashMap<Task, DefaultMutableTreeNode>();

		GlobalTaskModel tm = GlobalTaskModel.getInstance();
		rootInfo = new NodeInfo("All projects", tm.getGlobalTime());
		root.setUserObject(rootInfo);
		idleInfo = new NodeInfo("idle", tm.getIdlingTime());
		idleInfo.getDataSet().setValue("idling", 1d);
		rootInfo.getDataSet().setValue("idle", tm.getIdlingTime());
		root.add(new DefaultMutableTreeNode(idleInfo));

		for (Task task : tm.getAllTasks()) {
			taskAdded(task);
		}
		dateRangeChanged(tm.getDateFilter());
	}

	public void update() {
		GlobalTaskModel tm = GlobalTaskModel.getInstance();
		tableModel.clearFilters();
		filtersTableModel.clear();
		for (TaskFilterInfo info : tm.getFilters()) {
			filtersTableModel.addFilter(info);
			tableModel.addFilter(info.getFilter());
		}
		if (tm.isDateFilterActive())
			tableModel.addFilter(tm.getDateFilter());
	}

	public void taskAdded(Task task) {
		String project = task.getProject();
		// load or create and and project node
		DefaultMutableTreeNode projNode = projectNodes.get(project);
		if (projNode == null) {
			projNode = new DefaultMutableTreeNode(new NodeInfo(project));
			projectNodes.put(project, projNode);
			root.add(projNode);
			// create category nodes map for this project
			categoryNodes.put(project, new HashMap<String, DefaultMutableTreeNode>());
//			categoryDuration.put(project, new HashMap<String, Long>());
			treeModel.nodesWereInserted(root, new int[]{root.getChildCount() - 1});
		}
		String category = task.getCategory();
		// load or create and add category node
		DefaultMutableTreeNode catNode = categoryNodes.get(project).get(category);
		if (catNode == null) {
			catNode = new DefaultMutableTreeNode(new NodeInfo(category));
			categoryNodes.get(project).put(category, catNode);
			projNode.add(catNode);
			treeModel.nodesWereInserted(projNode, new int[]{projNode.getChildCount() - 1});
		}
//		Map<String, Long> projectCatDurations = categoryDuration.get(project);
//		if (!projectCatDurations.containsKey(category)) {
//			projectCatDurations.put(category, task.getDuration());
//		} else {
//			projectCatDurations.put(category, task.getDuration() + projectCatDurations.get(category));
//		}

		// add duration to category
		NodeInfo catNodeInfo = (NodeInfo) catNode.getUserObject();
		catNodeInfo.addDuration(task.getTotalDuration());
		catNodeInfo.getDataSet().setValue(task.getDescription(), task.getTotalDuration());

		// add duration to project
		NodeInfo projNodeInfo = (NodeInfo) projNode.getUserObject();
		projNodeInfo.addDuration(task.getTotalDuration());
		projNodeInfo.getDataSet().setValue(task.getCategory(), catNodeInfo.getDuration());

		rootInfo.getDataSet().setValue(project, projNodeInfo.getDuration());

		NodeInfo taskInfo = new NodeInfo(task.getDescription(), task.getTotalDuration());
		taskInfo.getDataSet().setValue(task.getDescription(), 1d);
		taskInfo.setTask(task);
		DefaultMutableTreeNode taskNode = new DefaultMutableTreeNode(taskInfo);
		catNode.add(taskNode);
		treeModel.nodesWereInserted(catNode, new int[]{catNode.getChildCount() - 1});
		taskNodes.put(task, taskNode);

		tree.repaint();

//		treeModel.reload(projNode);
//		treeModel.reload(catNode);
//
//		treeModel.nodeChanged(catNode);
//		treeModel.nodeChanged(projNode);
//		treeModel.nodeChanged(root);
	}

	public void taskRemoved(Task t) {
		String project = t.getProject();
		String category = t.getCategory();
		DefaultMutableTreeNode taskNode = taskNodes.get(t);
		DefaultMutableTreeNode projectNode = projectNodes.get(project);
		DefaultMutableTreeNode categoryNode = categoryNodes.get(project).get(category);
		taskNode.removeFromParent();
		NodeInfo catInfo = (NodeInfo) categoryNode.getUserObject();
		catInfo.addDuration(-t.getTotalDuration());
		catInfo.getDataSet().remove(t.getDescription());
		NodeInfo projInfo = (NodeInfo) projectNode.getUserObject();
		projInfo.addDuration(-t.getTotalDuration());
		if (categoryNode.getChildCount() == 0) {
			categoryNode.removeFromParent();
			projInfo.getDataSet().remove(category);
			if (projectNode.getChildCount() == 0) {
				projectNode.removeFromParent();
				rootInfo.getDataSet().remove(project);
				treeModel.reload(root);
			} else {
				treeModel.reload(projectNode);
			}
		} else {
			treeModel.reload(categoryNode);
		}
		taskNodes.remove(t);
	}

	public void setCurrentTask(Task t) {
	}

	public void setWorking(boolean working) {
	}

	public void durationChanged(Task t, long duration) {
		if (t == null) {
			idleInfo.setDuration(duration);
			rootInfo.getDataSet().setValue("idle", duration);
		} else {
			String project = t.getProject();
			String category = t.getCategory();
			DefaultMutableTreeNode taskNode = taskNodes.get(t);
			DefaultMutableTreeNode projectNode = projectNodes.get(project);
			DefaultMutableTreeNode categoryNode = categoryNodes.get(project).get(category);
			NodeInfo taskInfo = (NodeInfo) taskNode.getUserObject();
			long delta = duration - taskInfo.getDuration();
			taskInfo.setDuration(duration);
			NodeInfo catInfo = (NodeInfo) categoryNode.getUserObject();
			catInfo.addDuration(delta);
			catInfo.getDataSet().setValue(t.getDescription(), duration);
			NodeInfo projInfo = (NodeInfo) projectNode.getUserObject();
			projInfo.addDuration(delta);
			projInfo.getDataSet().setValue(category, catInfo.getDuration());
			rootInfo.getDataSet().setValue(project, projInfo.getDuration());
			treeModel.nodeChanged(taskNode);
			treeModel.nodeChanged(categoryNode);
			treeModel.nodeChanged(projectNode);
		}
		rootInfo.setDuration(GlobalTaskModel.getInstance().getGlobalTime());
		treeModel.nodeChanged(root);
	}

	public void dateRangeChanged(DateFilter dateFilter) {
		startDateChooser.setDate(dateFilter.getStart().getDate());
		endDateChooser.setDate(dateFilter.getEnd().getDate());
		tableModel.applyFilters();
//		tableModel.removeFilter(dateFilter);
//		tableModel.addFilter(dateFilter);
		startDateChooser.setMaxSelectableDate(endDateChooser.getDate());
		endDateChooser.setMinSelectableDate(startDateChooser.getDate());
		// update tree:
//		Set<Task> s = new HashSet<Task>(taskNodes.keySet());
//		for (Task task : s) {
//			taskRemoved(task);
//		}
//		for (Task task : GlobalTaskModel.getInstance().getAllTasks()) {
//			if(dateFilter.accept(task))
//				taskAdded(task);
//		}
	}

	public void dateFilterActivated(boolean dateFilterActive) {
		this.dateFilterActive.setSelected(dateFilterActive);
		if (dateFilterActive) {
			tableModel.addFilter(GlobalTaskModel.getInstance().getDateFilter());
		} else {
			tableModel.removeFilter(GlobalTaskModel.getInstance().getDateFilter());
		}
	}

	private class RemoveFilterAction extends AnAction {
		private RemoveFilterAction() {
			super("Remove Filter", "Removes the selected filter from the list", Icons.DELETE_ICON);
		}

		public void actionPerformed(AnActionEvent e) {
			TaskFilterInfo filterInfo = filtersTableModel.getFilter(filtersTable.getSelectedRow());
			filtersTableModel.removeFilter(filterInfo);
			tableModel.removeFilter(filterInfo.getFilter());
			GlobalTaskModel.getInstance().removeFilter(filterInfo);
		}

		@Override
		public void update(AnActionEvent e) {
			e.getPresentation().setEnabled(filtersTable.getSelectedRow() != -1);
		}
	}

	private class AddFilterAction extends AnAction {
		private AddFilterAction() {
			super("Add Filter", "Adds a new filter to the list", Icons.ADD_ICON);
		}

		public void actionPerformed(AnActionEvent e) {
			CreateFilterDialog d = new CreateFilterDialog(
					(Frame) SwingUtilities.windowForComponent(HistoryWindow.this));
			d.setVisible(true);
			TaskFilterInfo filterInfo = d.getFilterInfo();
			if (filterInfo != null) {
				filtersTableModel.addFilter(filterInfo);
				tableModel.addFilter(filterInfo.getFilter());
				GlobalTaskModel.getInstance().addFilter(filterInfo);
			}
		}
	}

	private class EditFilterAction extends AnAction {
		private EditFilterAction() {
			super("Edit Filter", "Edits the selected filter", IconLoader.findIcon("/modules/edit.png"));
		}

		public void actionPerformed(AnActionEvent e) {
			TaskFilterInfo filterInfo = filtersTableModel.getFilter(filtersTable.getSelectedRow());
			CreateFilterDialog d = new CreateFilterDialog(
					(Frame) SwingUtilities.windowForComponent(HistoryWindow.this));
			d.setFilterInfo(filterInfo);
			d.setVisible(true);
			TaskFilterInfo newFilterInfo = d.getFilterInfo();
			if (newFilterInfo != null) {
				filtersTableModel.removeFilter(filterInfo);
				filtersTableModel.addFilter(newFilterInfo);
				tableModel.removeFilter(filterInfo.getFilter());
				tableModel.addFilter(newFilterInfo.getFilter());
				GlobalTaskModel model = GlobalTaskModel.getInstance();
				model.removeFilter(filterInfo);
				model.addFilter(newFilterInfo);
			}
		}

		@Override
		public void update(AnActionEvent e) {
			e.getPresentation().setEnabled(filtersTable.getSelectedRow() != -1);
		}
	}

	private class NodeInfo {
		private String title;
		private DefaultPieDataset dataSet;
		private long duration;
		private Task task;

		public NodeInfo(String title) {
			this(title, 0);
		}

		private NodeInfo(String title, long duration) {
			this.title = title;
			this.duration = duration;
			dataSet = new DefaultPieDataset();
		}

		public DefaultPieDataset getDataSet() {
			return dataSet;
		}

		public long getDuration() {
			return duration;
		}

		public void addDuration(long duration) {
			this.duration += duration;
		}

		public void setDuration(long duration) {
			this.duration = duration;
		}

		@Override
		public String toString() {
			return title;
		}

		public void setTask(Task task) {
			this.task = task;
		}

		public Task getTask() {
			return task;
		}
	}
}

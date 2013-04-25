package net.roarsoftware.tracker.core.report;

/**
 * @author Janni Kovacs
 */
public class ChartGeneratorFactory {

	private static final ChartGeneratorFactory INSTANCE = new ChartGeneratorFactory();
	private ChartGenerator[] generators = {
			new WorkPerDayChartGenerator(),
			new ProjectTimeDistributionChartGenerator(),
			new TaskTimeDistributionChartGenerator(),
			new CategoryTimeDistributionChartGenerator()
	};

	private ChartGeneratorFactory() {
	}

	public static ChartGeneratorFactory getInstance() {
		return INSTANCE;
	}
	
	public ChartGenerator[] getChartGenerators() {
		return generators;
	}
}
